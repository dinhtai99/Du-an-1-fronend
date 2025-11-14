package fpoly.haideptrai.duan1.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.CategoryRequest;
import fpoly.haideptrai.duan1.api.models.CategoryResponse;
import fpoly.haideptrai.duan1.api.services.CategoryService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThemSuaLoaiSanPhamActivity extends AppCompatActivity {

    private TextInputEditText edtTenLoai, edtMoTa;
    private TextInputLayout tilTenLoai, tilMoTa;
    private MaterialButton btnLuu;
    private CategoryService categoryService;
    private String categoryId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_sua_loai_san_pham);

        categoryId = getIntent().getStringExtra("category_id");
        isEditMode = categoryId != null && !categoryId.isEmpty();

        initViews();
        categoryService = ApiClient.getClient().create(CategoryService.class);

        if (isEditMode) {
            setTitle("Sửa loại sản phẩm");
            btnLuu.setText("Cập nhật");
            loadCategoryData();
        } else {
            setTitle("Thêm loại sản phẩm");
            btnLuu.setText("Thêm mới");
        }

        btnLuu.setOnClickListener(v -> handleSave());
    }

    private void initViews() {
        edtTenLoai = findViewById(R.id.edtTenLoai);
        edtMoTa = findViewById(R.id.edtMoTa);
        tilTenLoai = findViewById(R.id.tilTenLoai);
        tilMoTa = findViewById(R.id.tilMoTa);
        btnLuu = findViewById(R.id.btnLuu);
    }

    private void loadCategoryData() {
        Call<CategoryResponse> call = categoryService.getById(categoryId);
        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CategoryResponse category = response.body();
                    edtTenLoai.setText(category.getName());
                    edtMoTa.setText(category.getDescription());
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Toast.makeText(ThemSuaLoaiSanPhamActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSave() {
        String tenLoai = edtTenLoai.getText().toString().trim();
        String moTa = edtMoTa.getText().toString().trim();

        tilTenLoai.setError(null);

        if (TextUtils.isEmpty(tenLoai)) {
            tilTenLoai.setError("Vui lòng nhập tên loại sản phẩm");
            edtTenLoai.requestFocus();
            return;
        }

        btnLuu.setEnabled(false);
        btnLuu.setText("Đang xử lý...");

        CategoryRequest request = new CategoryRequest();
        request.setName(tenLoai);
        request.setDescription(moTa);
        request.setStatus(1); // Active

        Call<ApiResponse<CategoryResponse>> call;
        if (isEditMode) {
            call = categoryService.updateCategory(categoryId, request);
        } else {
            call = categoryService.createCategory(request);
        }

        call.enqueue(new Callback<ApiResponse<CategoryResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CategoryResponse>> call, Response<ApiResponse<CategoryResponse>> response) {
                btnLuu.setEnabled(true);
                btnLuu.setText(isEditMode ? "Cập nhật" : "Thêm mới");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ThemSuaLoaiSanPhamActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = isEditMode ? "Cập nhật thất bại" : "Thêm mới thất bại";
                    if (response.code() == 400) {
                        errorMsg = "Tên loại sản phẩm đã tồn tại";
                        tilTenLoai.setError(errorMsg);
                    }
                    Toast.makeText(ThemSuaLoaiSanPhamActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CategoryResponse>> call, Throwable t) {
                btnLuu.setEnabled(true);
                btnLuu.setText(isEditMode ? "Cập nhật" : "Thêm mới");
                Toast.makeText(ThemSuaLoaiSanPhamActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
