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
import fpoly.haideptrai.duan1.api.TokenStore;
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
    private MaterialButton btnSuaThongTin, btnDonHangCuaToi, btnSanPhamYeuThich, btnDangXuat;
    private BottomNavigationView bottomNavigation;
    
    private UserService userService;
    private fpoly.haideptrai.duan1.api.services.AuthService authService;
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
        authService = ApiClient.getClient().create(fpoly.haideptrai.duan1.api.services.AuthService.class);
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
        btnDonHangCuaToi = findViewById(R.id.btnDonHangCuaToi);
        btnSanPhamYeuThich = findViewById(R.id.btnSanPhamYeuThich);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnSuaThongTin.setOnClickListener(v -> {
            Intent intent = new Intent(this, SuaThongTinActivity.class);
            startActivityForResult(intent, 100); // Request code 100
        });

        btnDonHangCuaToi.setOnClickListener(v -> {
            Intent intent = new Intent(this, DonHangActivity.class);
            startActivity(intent);
        });
        
        btnSanPhamYeuThich.setOnClickListener(v -> {
            Intent intent = new Intent(this, SanPhamYeuThichActivity.class);
            startActivity(intent);
        });

        btnDangXuat.setOnClickListener(v -> {
            handleLogout();
        });

        // Click vào số đơn hàng cũng mở danh sách đơn hàng
        txtSoDonHang.setOnClickListener(v -> {
            Intent intent = new Intent(this, DonHangActivity.class);
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
            } else if (itemId == R.id.nav_support) {
                Intent intent = new Intent(this, ChamSocKhachHangActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_discount) {
                Intent intent = new Intent(this, QuanLyVoucherActivity.class);
                startActivity(intent);
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
        // ✅ Ưu tiên load từ session trước (đã được update sau khi sửa thông tin)
        String hoTen = sessionManager.getHoTen();
        String username = sessionManager.getUsername();
        
        android.util.Log.d("ThongTinCaNhan", "=== LOADING USER INFO ===");
        android.util.Log.d("ThongTinCaNhan", "From session - Họ tên: " + hoTen);
        android.util.Log.d("ThongTinCaNhan", "From session - Username: " + username);
        
        // Hiển thị ngay từ session (để user thấy update ngay lập tức)
        if (hoTen != null && !hoTen.isEmpty()) {
            txtHoTen.setText(hoTen);
        }
        if (username != null && !username.isEmpty()) {
            txtEmail.setText(username);
        }
        txtSoDienThoai.setText("");
        txtDiaChi.setText("");

        // ✅ Gọi API /api/auth/me để lấy thông tin mới nhất từ server
        // Endpoint này tự động lấy từ JWT token, không cần userId
        Call<UserInfo> call = authService.getMe();
        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfo user = response.body();
                    android.util.Log.d("ThongTinCaNhan", "API response - FullName: " + user.getFullName());
                    android.util.Log.d("ThongTinCaNhan", "API response - Username: " + user.getUsername());
                    android.util.Log.d("ThongTinCaNhan", "API response - Phone: " + user.getPhone());
                    android.util.Log.d("ThongTinCaNhan", "API response - Address: " + user.getAddress());
                    
                    // Cập nhật UI với data từ API
                    if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                        txtHoTen.setText(user.getFullName());
                        // Cập nhật session nếu khác
                        if (!user.getFullName().equals(sessionManager.getHoTen())) {
                            sessionManager.setHoTen(user.getFullName());
                        }
                    }
                    
                    if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                        txtEmail.setText(user.getUsername());
                        // Cập nhật session nếu khác
                        if (!user.getUsername().equals(sessionManager.getUsername())) {
                            sessionManager.setUsername(user.getUsername());
                        }
                    }
                    
                    if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                        txtSoDienThoai.setText(user.getPhone());
                    } else {
                        txtSoDienThoai.setText("");
                    }
                    
                    if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                        txtDiaChi.setText(user.getAddress());
                        android.util.Log.d("ThongTinCaNhan", "✅ Address loaded from getMe: " + user.getAddress());
                    } else {
                        // Nếu getMe không có address, thử load từ UserService.getById()
                        loadAddressFromUserService();
                    }
                    
                    android.util.Log.d("ThongTinCaNhan", "✅ User info loaded and displayed");
                } else {
                    android.util.Log.w("ThongTinCaNhan", "API failed, using session data");
                    // Thử load từ UserService như fallback
                    loadAddressFromUserService();
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                android.util.Log.e("ThongTinCaNhan", "API error: " + t.getMessage());
                // Giữ nguyên data từ session đã hiển thị
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
        // Reload user info khi quay lại màn hình (có thể đã cập nhật ở màn hình khác)
        loadUserInfo();
    }

    private void loadAddressFromUserService() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            return;
        }
        
        android.util.Log.d("ThongTinCaNhan", "Loading address from UserService.getById()");
        Call<UserResponse> call = userService.getById(String.valueOf(userId));
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                        txtDiaChi.setText(user.getAddress());
                        android.util.Log.d("ThongTinCaNhan", "✅ Address loaded from UserService: " + user.getAddress());
                    } else {
                        android.util.Log.d("ThongTinCaNhan", "⚠️ No address in UserResponse");
                        txtDiaChi.setText("");
                    }
                } else {
                    android.util.Log.w("ThongTinCaNhan", "UserService.getById() failed");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                android.util.Log.e("ThongTinCaNhan", "UserService.getById() error: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            android.util.Log.d("ThongTinCaNhan", "=== REFRESHING AFTER UPDATE ===");
            // Refresh user info sau khi sửa thông tin thành công
            // Session đã được update trong SuaThongTinActivity
            loadUserInfo();
        }
    }

    private void handleLogout() {
        // Hiển thị dialog xác nhận
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Đăng xuất", (dialog, which) -> {
                performLogout();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void performLogout() {
        // Gọi API logout (optional - có thể bỏ qua nếu backend không yêu cầu)
        Call<ApiResponse<Void>> logoutCall = authService.logout();
        logoutCall.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                // Dù API thành công hay thất bại, vẫn clear session và chuyển về màn đăng nhập
                clearSessionAndRedirect();
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // Nếu API lỗi, vẫn clear session và chuyển về màn đăng nhập
                android.util.Log.w("ThongTinCaNhan", "Logout API failed, but still clearing session: " + t.getMessage());
                clearSessionAndRedirect();
            }
        });
    }

    private void clearSessionAndRedirect() {
        // Clear token
        TokenStore.clearToken();
        
        // Clear session
        sessionManager.clearSession();
        
        android.util.Log.d("ThongTinCaNhan", "Session cleared, redirecting to login");
        
        // Chuyển về màn đăng nhập và clear back stack
        Intent intent = new Intent(this, DangNhapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
    }
}

