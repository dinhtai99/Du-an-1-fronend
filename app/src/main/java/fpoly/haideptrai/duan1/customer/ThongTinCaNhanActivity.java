package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.InvoiceListResponse;
import fpoly.haideptrai.duan1.api.models.UserInfo;
import fpoly.haideptrai.duan1.api.models.UserResponse;
import fpoly.haideptrai.duan1.api.services.InvoiceService;
import fpoly.haideptrai.duan1.api.services.UserService;
import fpoly.haideptrai.duan1.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThongTinCaNhanActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView txtHoTen, txtEmail, txtSoDienThoai, txtSoDonHang, txtDiaChi;
    private MaterialButton btnSuaThongTin;
    private BottomNavigationView bottomNavigation;
    
    private UserService userService;
    private InvoiceService invoiceService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_ca_nhan);

        initViews();
        setupBottomNavigation();
        sessionManager = new SessionManager(this);
        userService = ApiClient.getClient().create(UserService.class);
        invoiceService = ApiClient.getClient().create(InvoiceService.class);
        loadUserInfo();
        loadOrderCount();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        txtHoTen = findViewById(R.id.txtHoTen);
        txtEmail = findViewById(R.id.txtEmail);
        txtSoDienThoai = findViewById(R.id.txtSoDienThoai);
        txtSoDonHang = findViewById(R.id.txtSoDonHang);
        txtDiaChi = findViewById(R.id.txtDiaChi);
        btnSuaThongTin = findViewById(R.id.btnSuaThongTin);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnSuaThongTin.setOnClickListener(v -> {
            Intent intent = new Intent(this, SuaThongTinActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_discount) {
                Toast.makeText(this, "Khuyến mãi", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_cart) {
                Intent intent = new Intent(this, GioHangActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Already on profile
                return true;
            }
            return false;
        });
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    private void loadUserInfo() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load từ session trước (fallback)
        txtHoTen.setText(sessionManager.getHoTen());
        txtEmail.setText(sessionManager.getUsername());
        txtSoDienThoai.setText("");
        txtDiaChi.setText("");

        // Gọi API để lấy thông tin chi tiết
        Call<UserResponse> call = userService.getById(String.valueOf(userId));
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    txtHoTen.setText(user.getHoTen() != null ? user.getHoTen() : sessionManager.getHoTen());
                    txtEmail.setText(user.getTenDangNhap() != null ? user.getTenDangNhap() : sessionManager.getUsername());
                    txtSoDienThoai.setText(user.getSoDienThoai() != null ? user.getSoDienThoai() : "");
                    // UserResponse không có address field, có thể thêm sau
                    txtDiaChi.setText("");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Silent fail, sử dụng session data
            }
        });
    }

    private void loadOrderCount() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            txtSoDonHang.setText("0");
            return;
        }

        String customerId = String.valueOf(userId);
        Call<InvoiceListResponse> call = invoiceService.getInvoices(null, customerId, null, null, null, null, null, 1, 1000);
        call.enqueue(new Callback<InvoiceListResponse>() {
            @Override
            public void onResponse(Call<InvoiceListResponse> call, Response<InvoiceListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InvoiceListResponse invoiceList = response.body();
                    int count = invoiceList.getInvoices() != null ? invoiceList.getInvoices().size() : 0;
                    txtSoDonHang.setText(String.valueOf(count));
                } else {
                    txtSoDonHang.setText("0");
                }
            }

            @Override
            public void onFailure(Call<InvoiceListResponse> call, Throwable t) {
                txtSoDonHang.setText("0");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }
}

