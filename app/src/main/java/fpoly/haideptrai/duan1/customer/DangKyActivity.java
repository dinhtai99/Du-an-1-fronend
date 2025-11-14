package fpoly.haideptrai.duan1.customer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.RegisterRequest;
import fpoly.haideptrai.duan1.api.models.UserInfo;
import fpoly.haideptrai.duan1.api.services.AuthService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangKyActivity extends AppCompatActivity {

    private static final String TAG = "REGISTER";

    private TextInputEditText edtHoTen, edtEmail, edtNgaySinh, edtSoDienThoai, edtMatKhau;
    private TextInputLayout tilHoTen, tilEmail, tilNgaySinh, tilSoDienThoai, tilMatKhau;
    private MaterialButton btnDangKy;
    private AuthService authService;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);

        initViews();
        authService = ApiClient.getClient().create(AuthService.class);
        calendar = Calendar.getInstance();

        // Chọn ngày sinh
        edtNgaySinh.setOnClickListener(v -> showDatePicker());

        btnDangKy.setOnClickListener(v -> handleDangKy());

        // Chuyển về màn hình đăng nhập
        findViewById(R.id.txtDangNhap).setOnClickListener(v -> {
            Intent intent = new Intent(DangKyActivity.this, DangNhapActivity.class);
            startActivity(intent);
            finish();
        });

        // Nút quay lại
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        edtHoTen = findViewById(R.id.edtHoTen);
        edtEmail = findViewById(R.id.edtEmail);
        edtNgaySinh = findViewById(R.id.edtNgaySinh);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        tilHoTen = findViewById(R.id.tilHoTen);
        tilEmail = findViewById(R.id.tilEmail);
        tilNgaySinh = findViewById(R.id.tilNgaySinh);
        tilSoDienThoai = findViewById(R.id.tilSoDienThoai);
        tilMatKhau = findViewById(R.id.tilMatKhau);
        btnDangKy = findViewById(R.id.btnDangKy);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    edtNgaySinh.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void handleDangKy() {
        String hoTen = edtHoTen.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String ngaySinh = edtNgaySinh.getText().toString().trim();
        String soDienThoai = edtSoDienThoai.getText().toString().trim();
        String matKhau = edtMatKhau.getText().toString().trim();

        // Reset lỗi
        tilHoTen.setError(null);
        tilEmail.setError(null);
        tilNgaySinh.setError(null);
        tilSoDienThoai.setError(null);
        tilMatKhau.setError(null);

        // Validate
        if (TextUtils.isEmpty(hoTen)) {
            tilHoTen.setError("Vui lòng nhập tên đầy đủ");
            edtHoTen.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(ngaySinh)) {
            tilNgaySinh.setError("Vui lòng chọn ngày sinh");
            edtNgaySinh.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(soDienThoai)) {
            tilSoDienThoai.setError("Vui lòng nhập số điện thoại");
            edtSoDienThoai.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(matKhau)) {
            tilMatKhau.setError("Vui lòng nhập mật khẩu");
            edtMatKhau.requestFocus();
            return;
        }

        if (matKhau.length() < 6) {
            tilMatKhau.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtMatKhau.requestFocus();
            return;
        }

        // Disable button
        btnDangKy.setEnabled(false);
        btnDangKy.setText("Đang xử lý...");

        // Tạo request
        RegisterRequest request = new RegisterRequest();
        request.setHoTen(hoTen);
        request.setEmail(email);
        request.setNgaySinh(ngaySinh);
        request.setSoDienThoai(soDienThoai);
        request.setTenDangNhap(email); // Dùng email làm tên đăng nhập
        request.setMatKhau(matKhau);
        request.setGioiTinh("");
        request.setVaiTro("khach_hang"); // Vai trò khách hàng

        Log.d(TAG, "Register request: " + email);

        // Gọi API đăng ký
        Call<ApiResponse<UserInfo>> call = authService.register(request);
        call.enqueue(new Callback<ApiResponse<UserInfo>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserInfo>> call, Response<ApiResponse<UserInfo>> response) {
                btnDangKy.setEnabled(true);
                btnDangKy.setText("Đăng ký");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserInfo> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(DangKyActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Chuyển về màn hình đăng nhập và truyền thông tin
                        Intent intent = new Intent(DangKyActivity.this, DangNhapActivity.class);
                        intent.putExtra("username", email);
                        intent.putExtra("password", matKhau);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đăng ký thất bại!";
                        Toast.makeText(DangKyActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Lỗi kết nối server. Vui lòng thử lại!";
                    if (response.code() == 400) {
                        errorMsg = "Thông tin không hợp lệ!";
                    } else if (response.code() == 409) {
                        errorMsg = "Email đã được sử dụng!";
                    }
                    Toast.makeText(DangKyActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserInfo>> call, Throwable t) {
                btnDangKy.setEnabled(true);
                btnDangKy.setText("Đăng ký");
                
                String errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng!";
                Log.e(TAG, "Network failure: " + t.getMessage(), t);
                Toast.makeText(DangKyActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

