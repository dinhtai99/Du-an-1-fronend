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
    private fpoly.haideptrai.duan1.api.services.AuthService authService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_thong_tin);

        initViews();
        sessionManager = new SessionManager(this);
        userService = ApiClient.getClient().create(UserService.class);
        authService = ApiClient.getClient().create(fpoly.haideptrai.duan1.api.services.AuthService.class);
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
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            // Fallback to session data if no user ID
            edtHoTen.setText(sessionManager.getHoTen());
            edtEmail.setText(sessionManager.getUsername());
            return;
        }

        // Load from session first (fallback)
        edtHoTen.setText(sessionManager.getHoTen());
        edtEmail.setText(sessionManager.getUsername());
        edtSoDienThoai.setText("");
        edtDiaChi.setText("");

        // Call API to get detailed information
        Call<UserResponse> call = userService.getById(String.valueOf(userId));
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    edtHoTen.setText(user.getHoTen() != null ? user.getHoTen() : sessionManager.getHoTen());
                    edtEmail.setText(user.getTenDangNhap() != null ? user.getTenDangNhap() : sessionManager.getUsername());
                    edtSoDienThoai.setText(user.getSoDienThoai() != null ? user.getSoDienThoai() : "");
                    // UserResponse doesn't have address field, can be added later
                    edtDiaChi.setText("");
                    
                    // Load avatar if available
                    if (user.getAnhDaiDien() != null && !user.getAnhDaiDien().isEmpty()) {
                        Glide.with(SuaThongTinActivity.this)
                                .load(user.getAnhDaiDien())
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(imgAvatar);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Silent fail, using session data
            }
        });
    }

    private void handleSave() {
        String hoTen = edtHoTen.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String soDienThoai = edtSoDienThoai.getText().toString().trim();
        String diaChi = edtDiaChi.getText().toString().trim();

        // Validation
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

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        // Validate phone format (nếu có)
        if (!TextUtils.isEmpty(soDienThoai) && soDienThoai.length() < 10) {
            edtSoDienThoai.setError("Số điện thoại phải có ít nhất 10 số");
            edtSoDienThoai.requestFocus();
            return;
        }

        btnLuuThongTin.setEnabled(false);
        btnLuuThongTin.setText("Đang lưu...");

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            btnLuuThongTin.setEnabled(true);
            btnLuuThongTin.setText("Lưu thông tin");
            return;
        }

        // Tạo request
        UserRequest request = new UserRequest();
        // Backend có thể cần fullName hoặc hoTen - thử cả hai
        request.setFullName(hoTen);
        request.setPhone(soDienThoai);
        request.setUsername(email); // Backend có thể cần username (email)
        // Không set password để không thay đổi password
        // Không set role để giữ nguyên role

        android.util.Log.d("UpdateUser", "=== UPDATE USER REQUEST ===");
        android.util.Log.d("UpdateUser", "User ID: " + userId);
        android.util.Log.d("UpdateUser", "Họ tên: " + hoTen);
        android.util.Log.d("UpdateUser", "Email: " + email);
        android.util.Log.d("UpdateUser", "Số điện thoại: " + soDienThoai);
        android.util.Log.d("UpdateUser", "Request JSON: " + new com.google.gson.Gson().toJson(request));

        // ✅ Dùng PUT /api/auth/me để customer có thể update profile của chính mình
        // Thay vì PUT /api/users/:id (chỉ admin mới có quyền)
        Call<ApiResponse<fpoly.haideptrai.duan1.api.models.UserInfo>> call = authService.updateProfile(request);
        call.enqueue(new Callback<ApiResponse<fpoly.haideptrai.duan1.api.models.UserInfo>>() {
            @Override
            public void onResponse(Call<ApiResponse<fpoly.haideptrai.duan1.api.models.UserInfo>> call, Response<ApiResponse<fpoly.haideptrai.duan1.api.models.UserInfo>> response) {
                btnLuuThongTin.setEnabled(true);
                btnLuuThongTin.setText("Lưu thông tin");

                android.util.Log.d("UpdateUser", "=== UPDATE USER RESPONSE ===");
                android.util.Log.d("UpdateUser", "Response code: " + response.code());
                android.util.Log.d("UpdateUser", "Response successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<fpoly.haideptrai.duan1.api.models.UserInfo> apiResponse = response.body();
                    android.util.Log.d("UpdateUser", "Response success: " + apiResponse.isSuccess());
                    android.util.Log.d("UpdateUser", "Response message: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        fpoly.haideptrai.duan1.api.models.UserInfo updatedUser = apiResponse.getData();
                        android.util.Log.d("UpdateUser", "Updated user data: " + new com.google.gson.Gson().toJson(updatedUser));
                        
                        // Cập nhật session với thông tin mới
                        // UserInfo có fullName
                        if (updatedUser.getFullName() != null && !updatedUser.getFullName().isEmpty()) {
                            sessionManager.setHoTen(updatedUser.getFullName());
                        } else {
                            sessionManager.setHoTen(hoTen);
                        }
                        
                        // UserInfo có username
                        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
                            sessionManager.setUsername(updatedUser.getUsername());
                        } else {
                            sessionManager.setUsername(email);
                        }
                        
                        android.util.Log.d("UpdateUser", "✅ User updated successfully");
                        android.util.Log.d("UpdateUser", "Session updated - Họ tên: " + sessionManager.getHoTen());
                        android.util.Log.d("UpdateUser", "Session updated - Username: " + sessionManager.getUsername());
                        
                        Toast.makeText(SuaThongTinActivity.this, 
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Cập nhật thông tin thành công", 
                            Toast.LENGTH_SHORT).show();
                        
                        // Trả về kết quả để màn hình trước có thể refresh
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        // Lỗi từ server
                        String errorMsg = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Cập nhật thất bại";
                        android.util.Log.e("UpdateUser", "❌ Server error: " + errorMsg);
                        android.util.Log.e("UpdateUser", "Response body: " + new com.google.gson.Gson().toJson(apiResponse));
                        Toast.makeText(SuaThongTinActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Parse error response
                    String errorMsg = "Cập nhật thất bại";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("UpdateUser", "❌ ERROR RESPONSE ===");
                            android.util.Log.e("UpdateUser", "Error response code: " + response.code());
                            android.util.Log.e("UpdateUser", "Error response body: " + errorBody);
                            
                            // Thử parse JSON error
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                ApiResponse<?> errorResponse = gson.fromJson(errorBody, ApiResponse.class);
                                if (errorResponse != null && errorResponse.getMessage() != null) {
                                    errorMsg = errorResponse.getMessage();
                                    android.util.Log.e("UpdateUser", "Parsed error message: " + errorMsg);
                                }
                            } catch (Exception jsonEx) {
                                android.util.Log.e("UpdateUser", "Cannot parse JSON error: " + jsonEx.getMessage());
                                // Không phải JSON, kiểm tra nếu là HTML (404, 500, etc)
                                if (errorBody.contains("Cannot PUT") || errorBody.contains("<!DOCTYPE html>")) {
                                    errorMsg = "Endpoint không tồn tại. Vui lòng kiểm tra backend server!\n" +
                                              "Đảm bảo route PUT /api/auth/me đã được đăng ký.\n" +
                                              "Endpoint này cho phép customer update profile của chính mình.";
                                } else if (errorBody.length() < 200) {
                                    errorMsg = errorBody;
                                }
                            }
                        } else {
                            android.util.Log.e("UpdateUser", "Error body is null");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("UpdateUser", "Error parsing error body: " + e.getMessage(), e);
                    }
                    
                    Toast.makeText(SuaThongTinActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<fpoly.haideptrai.duan1.api.models.UserInfo>> call, Throwable t) {
                btnLuuThongTin.setEnabled(true);
                btnLuuThongTin.setText("Lưu thông tin");
                
                android.util.Log.e("UpdateUser", "Network error: " + t.getMessage(), t);
                Toast.makeText(SuaThongTinActivity.this, 
                    "Lỗi kết nối: " + t.getMessage() + "\nVui lòng kiểm tra kết nối mạng và thử lại.", 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
}

