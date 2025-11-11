package fpoly.haideptrai.duan1;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.UserRequest;
import fpoly.haideptrai.duan1.api.models.UserResponse;
import fpoly.haideptrai.duan1.api.services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThemSuaNhanVienActivity extends AppCompatActivity {

    private TextInputEditText edtHoTen, edtTenDangNhap, edtMatKhau, edtSoDienThoai, edtNgaySinh;
    private TextInputLayout tilHoTen, tilTenDangNhap, tilMatKhau, tilSoDienThoai, tilNgaySinh;
    private Spinner spnGioiTinh, spnVaiTro;
    private MaterialButton btnLuu;
    private UserService userService;
    private String userId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_sua_nhan_vien);

        userId = getIntent().getStringExtra("user_id");
        isEditMode = userId != null && !userId.isEmpty();

        initViews();
        setupSpinners();
        userService = ApiClient.getClient().create(UserService.class);

        if (isEditMode) {
            setTitle("Sửa nhân viên");
            btnLuu.setText("Cập nhật");
            edtTenDangNhap.setEnabled(false);
            tilMatKhau.setHint("Mật khẩu mới (để trống nếu không đổi)");
            loadUserData();
        } else {
            setTitle("Thêm nhân viên");
            btnLuu.setText("Thêm mới");
        }

        btnLuu.setOnClickListener(v -> handleSave());
    }

    private void initViews() {
        edtHoTen = findViewById(R.id.edtHoTen);
        edtTenDangNhap = findViewById(R.id.edtTenDangNhap);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtNgaySinh = findViewById(R.id.edtNgaySinh);
        tilHoTen = findViewById(R.id.tilHoTen);
        tilTenDangNhap = findViewById(R.id.tilTenDangNhap);
        tilMatKhau = findViewById(R.id.tilMatKhau);
        tilSoDienThoai = findViewById(R.id.tilSoDienThoai);
        tilNgaySinh = findViewById(R.id.tilNgaySinh);
        spnGioiTinh = findViewById(R.id.spnGioiTinh);
        spnVaiTro = findViewById(R.id.spnVaiTro);
        btnLuu = findViewById(R.id.btnLuu);
    }

    private void setupSpinners() {
        String[] gioiTinhArray = {"Nam", "Nữ"};
        ArrayAdapter<String> gioiTinhAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gioiTinhArray);
        gioiTinhAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGioiTinh.setAdapter(gioiTinhAdapter);

        String[] vaiTroArray = {"admin", "staff"};
        String[] vaiTroDisplay = {"Quản trị viên", "Nhân viên"};
        ArrayAdapter<String> vaiTroAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vaiTroDisplay);
        vaiTroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnVaiTro.setAdapter(vaiTroAdapter);
    }

    private void loadUserData() {
        Call<UserResponse> call = userService.getById(userId);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    edtHoTen.setText(user.getHoTen());
                    edtTenDangNhap.setText(user.getTenDangNhap());
                    edtSoDienThoai.setText(user.getSoDienThoai());
                    edtNgaySinh.setText(user.getNgaySinh());
                    
                    if (user.getGioiTinh() != null) {
                        spnGioiTinh.setSelection(user.getGioiTinh().equals("Nữ") ? 1 : 0);
                    }
                    if (user.getVaiTro() != null) {
                        spnVaiTro.setSelection(user.getVaiTro().equals("admin") ? 0 : 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(ThemSuaNhanVienActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSave() {
        String hoTen = edtHoTen.getText().toString().trim();
        String tenDangNhap = edtTenDangNhap.getText().toString().trim();
        String matKhau = edtMatKhau.getText().toString().trim();
        String soDienThoai = edtSoDienThoai.getText().toString().trim();
        String ngaySinh = edtNgaySinh.getText().toString().trim();
        String gioiTinh = spnGioiTinh.getSelectedItem().toString();
        String vaiTro = spnVaiTro.getSelectedItemPosition() == 0 ? "admin" : "staff";

        tilHoTen.setError(null);
        tilTenDangNhap.setError(null);
        tilMatKhau.setError(null);

        if (TextUtils.isEmpty(hoTen)) {
            tilHoTen.setError("Vui lòng nhập họ tên");
            edtHoTen.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(tenDangNhap)) {
            tilTenDangNhap.setError("Vui lòng nhập tên đăng nhập");
            edtTenDangNhap.requestFocus();
            return;
        }

        if (!isEditMode && TextUtils.isEmpty(matKhau)) {
            tilMatKhau.setError("Vui lòng nhập mật khẩu");
            edtMatKhau.requestFocus();
            return;
        }

        if (!isEditMode && matKhau.length() < 6) {
            tilMatKhau.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtMatKhau.requestFocus();
            return;
        }

        btnLuu.setEnabled(false);
        btnLuu.setText("Đang xử lý...");

        UserRequest request = new UserRequest();
        request.setFullName(hoTen);
        request.setUsername(tenDangNhap);
        if (!TextUtils.isEmpty(matKhau)) {
            request.setPassword(matKhau);
        }
        request.setPhone(soDienThoai);
        request.setDateOfBirth(ngaySinh.isEmpty() ? null : ngaySinh);
        request.setGender(gioiTinh.equals("Nam") ? "male" : "female");
        request.setRole(vaiTro);

        Call<ApiResponse<UserResponse>> call;
        if (isEditMode) {
            call = userService.updateUser(userId, request);
        } else {
            call = userService.createUser(request);
        }

        call.enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                btnLuu.setEnabled(true);
                btnLuu.setText(isEditMode ? "Cập nhật" : "Thêm mới");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ThemSuaNhanVienActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = isEditMode ? "Cập nhật thất bại" : "Thêm mới thất bại";
                    if (response.code() == 400) {
                        errorMsg = "Tên đăng nhập đã tồn tại";
                        tilTenDangNhap.setError(errorMsg);
                    }
                    Toast.makeText(ThemSuaNhanVienActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                btnLuu.setEnabled(true);
                btnLuu.setText(isEditMode ? "Cập nhật" : "Thêm mới");
                Toast.makeText(ThemSuaNhanVienActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

