package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.InvoiceListResponse;
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;
import fpoly.haideptrai.duan1.api.services.InvoiceService;
import fpoly.haideptrai.duan1.customer.adapters.DonHangAdapter;
import fpoly.haideptrai.duan1.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonHangActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvDonHang;
    private android.view.View emptyState;
    private DonHangAdapter adapter;
    private InvoiceService invoiceService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_don_hang);

        initViews();
        sessionManager = new SessionManager(this);
        invoiceService = ApiClient.getClient().create(InvoiceService.class);
        loadOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvDonHang = findViewById(R.id.rvDonHang);
        emptyState = findViewById(R.id.emptyState);
        adapter = new DonHangAdapter();
        rvDonHang.setLayoutManager(new LinearLayoutManager(this));
        rvDonHang.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        adapter.setOnOrderClickListener(order -> {
            Intent intent = new Intent(this, TheoDoiDonHangActivity.class);
            intent.putExtra("invoice_id", order.get_id());
            startActivity(intent);
        });
    }

    private void loadOrders() {
        // Backend sẽ tự lấy customer ID từ JWT token
        // Không cần gửi customer ID trong query parameter
        // Nếu backend yêu cầu, có thể gửi null hoặc không gửi parameter này
        
        android.util.Log.d("DonHangActivity", "=== LOADING ORDERS ===");
        android.util.Log.d("DonHangActivity", "User ID from session: " + sessionManager.getUserId());
        android.util.Log.d("DonHangActivity", "MongoDB User ID from session: " + sessionManager.getMongoUserId());
        android.util.Log.d("DonHangActivity", "Username: " + sessionManager.getUsername());
        
        // Gửi null để backend tự lấy từ JWT token (recommended)
        // Hoặc có thể gửi customerId nếu có MongoDB ID
        String customerId = null; // Backend sẽ tự lấy từ JWT token
        
        // Nếu muốn gửi customer ID, uncomment dòng sau:
        // String customerId = sessionManager.getMongoUserId();
        
        android.util.Log.d("DonHangActivity", "Calling API: GET /api/invoices?customer=" + customerId);

        Call<InvoiceListResponse> call = invoiceService.getInvoices(null, customerId, null, null, null, null, null, 1, 50);
        call.enqueue(new Callback<InvoiceListResponse>() {
            @Override
            public void onResponse(Call<InvoiceListResponse> call, Response<InvoiceListResponse> response) {
                android.util.Log.d("DonHangActivity", "=== API RESPONSE ===");
                android.util.Log.d("DonHangActivity", "Response code: " + response.code());
                android.util.Log.d("DonHangActivity", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    InvoiceListResponse invoiceList = response.body();
                    
                    // Log raw response để debug
                    android.util.Log.d("DonHangActivity", "Raw response JSON: " + new com.google.gson.Gson().toJson(invoiceList));
                    android.util.Log.d("DonHangActivity", "Has data field: " + (invoiceList.getData() != null));
                    android.util.Log.d("DonHangActivity", "Has invoices field: " + (invoiceList.getInvoices() != null));
                    android.util.Log.d("DonHangActivity", "Data size: " + (invoiceList.getData() != null ? invoiceList.getData().size() : 0));
                    android.util.Log.d("DonHangActivity", "Invoices size: " + (invoiceList.getInvoices() != null ? invoiceList.getInvoices().size() : 0));
                    
                    List<InvoiceResponse> orders = invoiceList.getInvoices();
                    
                    android.util.Log.d("DonHangActivity", "Total invoices: " + invoiceList.getTotal());
                    android.util.Log.d("DonHangActivity", "Orders list size: " + (orders != null ? orders.size() : 0));
                    
                    if (orders != null && !orders.isEmpty()) {
                        android.util.Log.d("DonHangActivity", "✅ Found " + orders.size() + " orders");
                        // Log một vài đơn hàng để debug
                        for (int i = 0; i < Math.min(3, orders.size()); i++) {
                            InvoiceResponse order = orders.get(i);
                            android.util.Log.d("DonHangActivity", "Order " + (i + 1) + ": ID=" + order.get_id() + 
                                ", Number=" + order.getInvoiceNumber() + 
                                ", Status=" + order.getStatus() +
                                ", Customer=" + (order.getCustomer() != null ? order.getCustomer().get_id() : "null"));
                        }
                        
                        adapter.setItems(orders);
                        rvDonHang.setVisibility(android.view.View.VISIBLE);
                        emptyState.setVisibility(android.view.View.GONE);
                    } else {
                        android.util.Log.w("DonHangActivity", "⚠️ No orders found (empty list)");
                        adapter.setItems(new ArrayList<>());
                        rvDonHang.setVisibility(android.view.View.GONE);
                        emptyState.setVisibility(android.view.View.VISIBLE);
                    }
                } else {
                    String errorMsg = "Không tải được đơn hàng";
                    String errorBody = "";
                    try {
                    if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("DonHangActivity", "Error response body: " + errorBody);
                            
                            // Thử parse JSON error
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                fpoly.haideptrai.duan1.api.models.ApiResponse<?> errorResponse = gson.fromJson(errorBody, fpoly.haideptrai.duan1.api.models.ApiResponse.class);
                                if (errorResponse != null && errorResponse.getMessage() != null) {
                                    errorMsg = errorResponse.getMessage();
                                }
                            } catch (Exception jsonEx) {
                                // Không phải JSON
                                if (errorBody.length() < 200) {
                                    errorMsg = errorBody;
                        }
                    }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("DonHangActivity", "Error parsing error body: " + e.getMessage());
                    }
                    
                    android.util.Log.e("DonHangActivity", "❌ Error loading orders: " + errorMsg);
                    android.util.Log.e("DonHangActivity", "Response code: " + response.code());
                    
                    Toast.makeText(DonHangActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    rvDonHang.setVisibility(android.view.View.GONE);
                    emptyState.setVisibility(android.view.View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<InvoiceListResponse> call, Throwable t) {
                android.util.Log.e("DonHangActivity", "❌ Network error: " + t.getMessage(), t);
                Toast.makeText(DonHangActivity.this, "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                rvDonHang.setVisibility(android.view.View.GONE);
                emptyState.setVisibility(android.view.View.VISIBLE);
            }
        });
    }
}

