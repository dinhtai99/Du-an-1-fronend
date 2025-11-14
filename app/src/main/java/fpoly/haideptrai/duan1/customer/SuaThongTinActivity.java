package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.UserRequest;
import fpoly.haideptrai.duan1.api.models.UserResponse;
import fpoly.haideptrai.duan1.api.services.UserService;
import fpoly.haideptrai.duan1.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SuaThongTinActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextInputEditText edtHoTen, edtEmail, edtSoDienThoai, edtDiaChi;
    private MaterialButton btnLuuThongTin;
    
    private UserService userService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_thong_tin);

        initViews();
        sessionManager = new SessionManager(this);
        userService = ApiClient.getClient().create(UserService.class);
        loadUserInfo();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        edtHoTen = findViewById(R.id.edtHoTen);
        edtEmail = findViewById(R.id.edtEmail);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtDiaChi = findViewById(R.id.edtDiaChi);
        btnLuuThongTin = findViewById(R.id.btnLuuThongTin);

        btnLuuThongTin.setOnClickListener(v -> handleSave());
    }

    private void loadUserInfo() {
        // TODO: Load from API
        edtHoTen.setText(sessionManager.getHoTen());
        edtEmail.setText(sessionManager.getUsername() + "@example.com");
        edtSoDienThoai.setText("0966686868");
        edtDiaChi.setText("Hà Nội");
    }

    private void handleSave() {
        String hoTen = edtHoTen.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String soDienThoai = edtSoDienThoai.getText().toString().trim();
        String diaChi = edtDiaChi.getText().toString().trim();

        if (TextUtils.isEmpty(hoTen)) {
            edtHoTen.setError("Vui lòng nhập họ tên");
            edtHoTen.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        btnLuuThongTin.setEnabled(false);
        btnLuuThongTin.setText("Đang lưu...");

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            btnLuuThongTin.setEnabled(true);
            btnLuuThongTin.setText("Lưu thông tin");
            return;
        }

        UserRequest request = new UserRequest();
        request.setFullName(hoTen);
        request.setPhone(soDienThoai);
        // TODO: Add address field to UserRequest if needed

        Call<ApiResponse<UserResponse>> call = userService.updateUser(String.valueOf(userId), request);
        call.enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                btnLuuThongTin.setEnabled(true);
                btnLuuThongTin.setText("Lưu thông tin");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SuaThongTinActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SuaThongTinActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                btnLuuThongTin.setEnabled(true);
                btnLuuThongTin.setText("Lưu thông tin");
                Toast.makeText(SuaThongTinActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

