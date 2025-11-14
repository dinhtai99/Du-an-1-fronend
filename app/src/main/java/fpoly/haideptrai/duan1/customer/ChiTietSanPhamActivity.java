package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import fpoly.haideptrai.duan1.api.services.ProductService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietSanPhamActivity extends AppCompatActivity {

    private ImageView imgSanPham;
    private TextView txtTenSanPham, txtLoaiSanPham, txtGiaNhap, txtGiaBan, txtSoLuong;
    private MaterialButton btnThemVaoGioHang;
    private BottomNavigationView bottomNavigation;
    
    private ProductService productService;
    private String productId;
    private ProductResponse product;
    private NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_san_pham);

        productId = getIntent().getStringExtra("product_id");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupBottomNavigation();
        productService = ApiClient.getClient().create(ProductService.class);
        loadProductDetails();
    }

    private void initViews() {
        imgSanPham = findViewById(R.id.imgSanPham);
        txtTenSanPham = findViewById(R.id.txtTenSanPham);
        txtLoaiSanPham = findViewById(R.id.txtLoaiSanPham);
        txtGiaNhap = findViewById(R.id.txtGiaNhap);
        txtGiaBan = findViewById(R.id.txtGiaBan);
        txtSoLuong = findViewById(R.id.txtSoLuong);
        btnThemVaoGioHang = findViewById(R.id.btnThemVaoGioHang);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnThemVaoGioHang.setOnClickListener(v -> {
            if (product != null) {
                // TODO: Add to cart
                Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
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
                Intent intent = new Intent(this, ThongTinCaNhanActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadProductDetails() {
        Call<ProductResponse> call = productService.getById(productId);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    product = response.body();
                    displayProduct(product);
                } else {
                    Toast.makeText(ChiTietSanPhamActivity.this, "Không tải được thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(ChiTietSanPhamActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayProduct(ProductResponse product) {
        txtTenSanPham.setText(product.getName());
        
        if (product.getCategory() != null) {
            txtLoaiSanPham.setText(product.getCategory().getName());
        }
        
        txtGiaNhap.setText(formatPrice(product.getImportPrice()));
        txtGiaBan.setText(formatPrice(product.getPrice()));
        txtSoLuong.setText(String.valueOf(product.getStock() != null ? product.getStock() : 0));

        // Load image
        String imageUrl = product.getImage();
        if (imageUrl != null && !imageUrl.trim().isEmpty() && !imageUrl.contains("example.com")) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(imgSanPham);
        } else {
            Glide.with(this)
                    .load(R.mipmap.ic_launcher)
                    .into(imgSanPham);
        }
    }

    private String formatPrice(Double price) {
        if (price == null) return "0 vnđ";
        return currency.format(price).replace("₫", "vnđ");
    }
}

