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
import fpoly.haideptrai.duan1.api.models.UserInfo;
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
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_ca_nhan);

        initViews();
        setupBottomNavigation();
        sessionManager = new SessionManager(this);
        userService = ApiClient.getClient().create(UserService.class);
        loadUserInfo();
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

        // TODO: Use actual API to get user info
        // For now, use session data
        txtHoTen.setText(sessionManager.getHoTen());
        txtEmail.setText(sessionManager.getUsername() + "@example.com");
        txtSoDienThoai.setText("0966686868");
        txtSoDonHang.setText("0");
        txtDiaChi.setText("Hà Nội");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }
}

