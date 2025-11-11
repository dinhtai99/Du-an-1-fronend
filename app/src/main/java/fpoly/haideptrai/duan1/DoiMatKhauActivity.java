package fpoly.haideptrai.duan1;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.ChangePasswordRequest;
import fpoly.haideptrai.duan1.api.services.AuthService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoiMatKhauActivity extends AppCompatActivity {

    private TextInputEditText edtMatKhauCu, edtMatKhauMoi, edtXacNhanMatKhauMoi;
    private TextInputLayout tilMatKhauCu, tilMatKhauMoi, tilXacNhanMatKhauMoi;
    private MaterialButton btnDoiMatKhau;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doi_mat_khau);

        initViews();
        authService = ApiClient.getClient().create(AuthService.class);

        btnDoiMatKhau.setOnClickListener(v -> handleDoiMatKhau());
    }

    private void initViews() {
        edtMatKhauCu = findViewById(R.id.edtMatKhauCu);
        edtMatKhauMoi = findViewById(R.id.edtMatKhauMoi);
        edtXacNhanMatKhauMoi = findViewById(R.id.edtXacNhanMatKhauMoi);
        tilMatKhauCu = findViewById(R.id.tilMatKhauCu);
        tilMatKhauMoi = findViewById(R.id.tilMatKhauMoi);
        tilXacNhanMatKhauMoi = findViewById(R.id.tilXacNhanMatKhauMoi);
        btnDoiMatKhau = findViewById(R.id.btnDoiMatKhau);
    }

    private void handleDoiMatKhau() {
        String matKhauCu = edtMatKhauCu.getText().toString().trim();
        String matKhauMoi = edtMatKhauMoi.getText().toString().trim();
        String xacNhanMatKhauMoi = edtXacNhanMatKhauMoi.getText().toString().trim();

        tilMatKhauCu.setError(null);
        tilMatKhauMoi.setError(null);
        tilXacNhanMatKhauMoi.setError(null);

        if (TextUtils.isEmpty(matKhauCu)) {
            tilMatKhauCu.setError("Vui lòng nhập mật khẩu cũ");
            edtMatKhauCu.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(matKhauMoi)) {
            tilMatKhauMoi.setError("Vui lòng nhập mật khẩu mới");
            edtMatKhauMoi.requestFocus();
            return;
        }

        if (matKhauMoi.length() < 6) {
            tilMatKhauMoi.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            edtMatKhauMoi.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(xacNhanMatKhauMoi)) {
            tilXacNhanMatKhauMoi.setError("Vui lòng xác nhận mật khẩu mới");
            edtXacNhanMatKhauMoi.requestFocus();
            return;
        }

        if (!matKhauMoi.equals(xacNhanMatKhauMoi)) {
            tilXacNhanMatKhauMoi.setError("Mật khẩu xác nhận không khớp");
            edtXacNhanMatKhauMoi.requestFocus();
            return;
        }

        btnDoiMatKhau.setEnabled(false);
        btnDoiMatKhau.setText("Đang xử lý...");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(matKhauCu);
        request.setNewPassword(matKhauMoi);
        request.setConfirmPassword(xacNhanMatKhauMoi);

        Call<ApiResponse<Void>> call = authService.changePassword(request);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                btnDoiMatKhau.setEnabled(true);
                btnDoiMatKhau.setText("Đổi mật khẩu");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DoiMatKhauActivity.this, response.body().getMessage() != null ? response.body().getMessage() : "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Đổi mật khẩu thất bại";
                    if (response.code() == 401) {
                        errorMsg = "Mật khẩu cũ không đúng";
                        tilMatKhauCu.setError(errorMsg);
                    } else if (response.code() == 400) {
                        errorMsg = "Mật khẩu mới và xác nhận mật khẩu không khớp";
                        tilXacNhanMatKhauMoi.setError(errorMsg);
                    }
                    Toast.makeText(DoiMatKhauActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                btnDoiMatKhau.setEnabled(true);
                btnDoiMatKhau.setText("Đổi mật khẩu");
                Toast.makeText(DoiMatKhauActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

