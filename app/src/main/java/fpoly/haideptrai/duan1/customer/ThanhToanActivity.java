package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.InvoiceItemRequest;
import fpoly.haideptrai.duan1.api.models.InvoiceRequest;
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;
import fpoly.haideptrai.duan1.api.services.InvoiceService;
import fpoly.haideptrai.duan1.customer.models.CartItem;
import fpoly.haideptrai.duan1.utils.CartManager;
import fpoly.haideptrai.duan1.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThanhToanActivity extends AppCompatActivity {

    private TextInputEditText edtDiaChi;
    private MaterialButton btnSuDungViTriHienTai, btnThanhToan;
    private LinearLayout layoutVisa, layoutMastercard, layoutNganHang, layoutQR, layoutCOD;
    private String selectedPaymentMethod = "cod"; // Default: COD
    
    private InvoiceService invoiceService;
    private CartManager cartManager;
    private SessionManager sessionManager;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        initViews();
        invoiceService = ApiClient.getClient().create(InvoiceService.class);
        cartManager = new CartManager(this);
        sessionManager = new SessionManager(this);
        cartItems = cartManager.loadCart();
        setupClickListeners();
    }

    private void initViews() {
        edtDiaChi = findViewById(R.id.edtDiaChi);
        btnSuDungViTriHienTai = findViewById(R.id.btnSuDungViTriHienTai);
        btnThanhToan = findViewById(R.id.btnThanhToan);
        layoutVisa = findViewById(R.id.layoutVisa);
        layoutMastercard = findViewById(R.id.layoutMastercard);
        layoutNganHang = findViewById(R.id.layoutNganHang);
        layoutQR = findViewById(R.id.layoutQR);
        layoutCOD = findViewById(R.id.layoutCOD);
    }

    private void setupClickListeners() {
        btnSuDungViTriHienTai.setOnClickListener(v -> {
            // TODO: Get current location
            Toast.makeText(this, "Lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
        });

        edtDiaChi.setOnClickListener(v -> {
            // TODO: Open address picker
            Toast.makeText(this, "Chọn địa chỉ", Toast.LENGTH_SHORT).show();
        });

        layoutVisa.setOnClickListener(v -> selectPaymentMethod("visa", layoutVisa));
        layoutMastercard.setOnClickListener(v -> selectPaymentMethod("mastercard", layoutMastercard));
        layoutNganHang.setOnClickListener(v -> selectPaymentMethod("bank", layoutNganHang));
        layoutQR.setOnClickListener(v -> selectPaymentMethod("qr", layoutQR));
        layoutCOD.setOnClickListener(v -> selectPaymentMethod("cod", layoutCOD));

        btnThanhToan.setOnClickListener(v -> {
            String address = edtDiaChi.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            processPayment(address);
        });
    }

    private void selectPaymentMethod(String method, LinearLayout selectedLayout) {
        selectedPaymentMethod = method;
        // Reset all backgrounds
        layoutVisa.setBackground(null);
        layoutMastercard.setBackground(null);
        layoutNganHang.setBackground(null);
        layoutQR.setBackground(null);
        layoutCOD.setBackground(null);
        
        // Highlight selected
        selectedLayout.setBackgroundResource(R.color.primary_blue_light);
    }

    private void processPayment(String address) {
        btnThanhToan.setEnabled(false);
        btnThanhToan.setText("Đang xử lý...");

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        // Tạo InvoiceRequest
        InvoiceRequest request = new InvoiceRequest();
        request.setCustomer(String.valueOf(userId));
        request.setPaymentMethod(selectedPaymentMethod);
        request.setNotes("Địa chỉ giao hàng: " + address);

        // Convert cart items to invoice items
        List<InvoiceItemRequest> invoiceItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            InvoiceItemRequest item = new InvoiceItemRequest();
            item.setProduct(cartItem.getProduct().get_id());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getPrice());
            item.setDiscount(0.0);
            invoiceItems.add(item);
        }
        request.setItems(invoiceItems);

        // Gọi API tạo đơn hàng
        Call<ApiResponse<InvoiceResponse>> call = invoiceService.createInvoice(request);
        call.enqueue(new Callback<ApiResponse<InvoiceResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<InvoiceResponse>> call, Response<ApiResponse<InvoiceResponse>> response) {
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<InvoiceResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        // Xóa giỏ hàng sau khi thanh toán thành công
                        cartManager.clearCart();
                        
                        Toast.makeText(ThanhToanActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Chuyển đến màn hình đơn hàng
                        android.content.Intent intent = new android.content.Intent(ThanhToanActivity.this, DonHangActivity.class);
                        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Thanh toán thất bại";
                        Toast.makeText(ThanhToanActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ThanhToanActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<InvoiceResponse>> call, Throwable t) {
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");
                Toast.makeText(ThanhToanActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

