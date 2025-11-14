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
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        String customerId = String.valueOf(userId);

        Call<InvoiceListResponse> call = invoiceService.getInvoices(null, customerId, null, null, null, null, null, 1, 50);
        call.enqueue(new Callback<InvoiceListResponse>() {
            @Override
            public void onResponse(Call<InvoiceListResponse> call, Response<InvoiceListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<InvoiceResponse> orders = response.body().getInvoices();
                    adapter.setItems(orders != null ? orders : new ArrayList<>());
                } else {
                    Toast.makeText(DonHangActivity.this, "Không tải được đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InvoiceListResponse> call, Throwable t) {
                Toast.makeText(DonHangActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

