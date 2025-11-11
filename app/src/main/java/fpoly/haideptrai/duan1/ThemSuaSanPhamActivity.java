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

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.CategoryResponse;
import fpoly.haideptrai.duan1.api.models.ProductRequest;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import fpoly.haideptrai.duan1.api.services.CategoryService;
import fpoly.haideptrai.duan1.api.services.ProductService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThemSuaSanPhamActivity extends AppCompatActivity {

    private TextInputEditText edtTenSanPham, edtGiaNhap, edtGiaBan, edtTonKho, edtMucToiThieu, edtMoTa;
    private TextInputLayout tilTenSanPham, tilGiaNhap, tilGiaBan, tilTonKho, tilMucToiThieu, tilMoTa;
    private Spinner spnLoaiSanPham;
    private MaterialButton btnLuu;
    private ProductService productService;
    private CategoryService categoryService;
    private String productId;
    private boolean isEditMode = false;
    private List<CategoryResponse> categories = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_sua_san_pham);

        productId = getIntent().getStringExtra("product_id");
        isEditMode = productId != null && !productId.isEmpty();

        initViews();
        setupSpinner();
        productService = ApiClient.getClient().create(ProductService.class);
        categoryService = ApiClient.getClient().create(CategoryService.class);

        if (isEditMode) {
            setTitle("Sửa sản phẩm");
            btnLuu.setText("Cập nhật");
            loadProductData();
        } else {
            setTitle("Thêm sản phẩm");
            btnLuu.setText("Thêm mới");
        }

        loadCategories();
        btnLuu.setOnClickListener(v -> handleSave());
    }

    private void initViews() {
        edtTenSanPham = findViewById(R.id.edtTenSanPham);
        edtGiaNhap = findViewById(R.id.edtGiaNhap);
        edtGiaBan = findViewById(R.id.edtGiaBan);
        edtTonKho = findViewById(R.id.edtTonKho);
        edtMucToiThieu = findViewById(R.id.edtMucToiThieu);
        edtMoTa = findViewById(R.id.edtMoTa);
        tilTenSanPham = findViewById(R.id.tilTenSanPham);
        tilGiaNhap = findViewById(R.id.tilGiaNhap);
        tilGiaBan = findViewById(R.id.tilGiaBan);
        tilTonKho = findViewById(R.id.tilTonKho);
        tilMucToiThieu = findViewById(R.id.tilMucToiThieu);
        tilMoTa = findViewById(R.id.tilMoTa);
        spnLoaiSanPham = findViewById(R.id.spnLoaiSanPham);
        btnLuu = findViewById(R.id.btnLuu);
    }

    private void setupSpinner() {
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLoaiSanPham.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        Call<List<CategoryResponse>> call = categoryService.getAllActive();
        call.enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    List<String> categoryNames = new ArrayList<>();
                    for (CategoryResponse cat : categories) {
                        categoryNames.add(cat.getName());
                    }
                    categoryAdapter.clear();
                    categoryAdapter.addAll(categoryNames);
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                Toast.makeText(ThemSuaSanPhamActivity.this, "Lỗi tải danh sách loại sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductData() {
        Call<ProductResponse> call = productService.getById(productId);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse product = response.body();
                    edtTenSanPham.setText(product.getName());
                    edtGiaNhap.setText(product.getImportPrice() != null ? String.valueOf(product.getImportPrice()) : "");
                    edtGiaBan.setText(product.getPrice() != null ? String.valueOf(product.getPrice()) : "");
                    edtTonKho.setText(product.getStock() != null ? String.valueOf(product.getStock()) : "0");
                    edtMucToiThieu.setText(product.getMinStock() != null ? String.valueOf(product.getMinStock()) : "10");
                    edtMoTa.setText(product.getDescription());

                    if (product.getCategory() != null && product.getCategory().get_id() != null) {
                        for (int i = 0; i < categories.size(); i++) {
                            if (categories.get(i).get_id().equals(product.getCategory().get_id())) {
                                spnLoaiSanPham.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(ThemSuaSanPhamActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSave() {
        String tenSanPham = edtTenSanPham.getText().toString().trim();
        String giaNhapStr = edtGiaNhap.getText().toString().trim();
        String giaBanStr = edtGiaBan.getText().toString().trim();
        String tonKhoStr = edtTonKho.getText().toString().trim();
        String mucToiThieuStr = edtMucToiThieu.getText().toString().trim();
        String moTa = edtMoTa.getText().toString().trim();

        tilTenSanPham.setError(null);
        tilGiaNhap.setError(null);
        tilGiaBan.setError(null);

        if (TextUtils.isEmpty(tenSanPham)) {
            tilTenSanPham.setError("Vui lòng nhập tên sản phẩm");
            edtTenSanPham.requestFocus();
            return;
        }

        if (spnLoaiSanPham.getSelectedItemPosition() < 0 || categories.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn loại sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(giaNhapStr)) {
            tilGiaNhap.setError("Vui lòng nhập giá nhập");
            edtGiaNhap.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(giaBanStr)) {
            tilGiaBan.setError("Vui lòng nhập giá bán");
            edtGiaBan.requestFocus();
            return;
        }

        try {
            Double giaNhap = Double.parseDouble(giaNhapStr);
            Double giaBan = Double.parseDouble(giaBanStr);
            Integer tonKho = TextUtils.isEmpty(tonKhoStr) ? 0 : Integer.parseInt(tonKhoStr);
            Integer mucToiThieu = TextUtils.isEmpty(mucToiThieuStr) ? 10 : Integer.parseInt(mucToiThieuStr);

            if (giaNhap < 0 || giaBan < 0) {
                Toast.makeText(this, "Giá không được âm", Toast.LENGTH_SHORT).show();
                return;
            }

            if (giaBan < giaNhap) {
                Toast.makeText(this, "Giá bán phải lớn hơn hoặc bằng giá nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLuu.setEnabled(false);
            btnLuu.setText("Đang xử lý...");

            CategoryResponse selectedCategory = categories.get(spnLoaiSanPham.getSelectedItemPosition());

            ProductRequest request = new ProductRequest();
            request.setName(tenSanPham);
            request.setCategory(selectedCategory.get_id());
            request.setImportPrice(giaNhap);
            request.setPrice(giaBan);
            request.setStock(tonKho);
            request.setMinStock(mucToiThieu);
            request.setDescription(moTa);
            request.setStatus(1); // Active
            request.setImages(new ArrayList<>()); // TODO: Implement image upload
            request.setImage(""); // TODO: Set image URL after upload

            Call<ApiResponse<ProductResponse>> call;
            if (isEditMode) {
                call = productService.updateProduct(productId, request);
            } else {
                call = productService.createProduct(request);
            }

            call.enqueue(new Callback<ApiResponse<ProductResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<ProductResponse>> call, Response<ApiResponse<ProductResponse>> response) {
                    btnLuu.setEnabled(true);
                    btnLuu.setText(isEditMode ? "Cập nhật" : "Thêm mới");

                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(ThemSuaSanPhamActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ThemSuaSanPhamActivity.this, isEditMode ? "Cập nhật thất bại" : "Thêm mới thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ProductResponse>> call, Throwable t) {
                    btnLuu.setEnabled(true);
                    btnLuu.setText(isEditMode ? "Cập nhật" : "Thêm mới");
                    Toast.makeText(ThemSuaSanPhamActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}

