package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.CategoryResponse;
import fpoly.haideptrai.duan1.api.models.ProductListResponse;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import fpoly.haideptrai.duan1.api.services.CategoryService;
import fpoly.haideptrai.duan1.api.services.ProductService;
import fpoly.haideptrai.duan1.customer.adapters.CategoryHomeAdapter;
import fpoly.haideptrai.duan1.customer.adapters.ProductHomeAdapter;
import fpoly.haideptrai.duan1.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView txtGreeting, txtSubGreeting, txtXemTatCa;
    private ImageButton btnMenu, btnSearch;
    private ImageView imgBanner;
    private RecyclerView rvDanhMuc, rvSanPham;
    private BottomNavigationView bottomNavigation;
    
    private CategoryHomeAdapter categoryAdapter;
    private ProductHomeAdapter productAdapter;
    private CategoryService categoryService;
    private ProductService productService;
    private SessionManager sessionManager;
    private NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_customer);

        initViews();
        setupBottomNavigation();
        sessionManager = new SessionManager(this);
        categoryService = ApiClient.getClient().create(CategoryService.class);
        productService = ApiClient.getClient().create(ProductService.class);

        // Set greeting
        String userName = sessionManager.getHoTen();
        if (userName != null && !userName.isEmpty()) {
            txtGreeting.setText("Xin chào, " + userName);
        }

        setupClickListeners();
        loadCategories();
        loadProducts();
    }

    private void initViews() {
        txtGreeting = findViewById(R.id.txtGreeting);
        txtSubGreeting = findViewById(R.id.txtSubGreeting);
        txtXemTatCa = findViewById(R.id.txtXemTatCa);
        btnMenu = findViewById(R.id.btnMenu);
        btnSearch = findViewById(R.id.btnSearch);
        imgBanner = findViewById(R.id.imgBanner);
        rvDanhMuc = findViewById(R.id.rvDanhMuc);
        rvSanPham = findViewById(R.id.rvSanPham);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Setup RecyclerViews
        rvDanhMuc.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryHomeAdapter();
        rvDanhMuc.setAdapter(categoryAdapter);

        rvSanPham.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductHomeAdapter();
        rvSanPham.setAdapter(productAdapter);
    }

    private void setupClickListeners() {
        btnMenu.setOnClickListener(v -> {
            // TODO: Open drawer menu
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
        });

        btnSearch.setOnClickListener(v -> {
            // TODO: Open search screen
            Toast.makeText(this, "Tìm kiếm", Toast.LENGTH_SHORT).show();
        });

        txtXemTatCa.setOnClickListener(v -> {
            // TODO: Navigate to all products
            Toast.makeText(this, "Xem tất cả sản phẩm", Toast.LENGTH_SHORT).show();
        });

        categoryAdapter.setOnCategoryClickListener(category -> {
            // Filter products by category
            loadProducts(category.get_id());
        });

        productAdapter.setOnProductClickListener(product -> {
            Intent intent = new Intent(HomeActivity.this, ChiTietSanPhamActivity.class);
            intent.putExtra("product_id", product.get_id());
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_discount) {
                // TODO: Navigate to discount/voucher
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

    private void loadCategories() {
        Call<List<CategoryResponse>> call = categoryService.getAllActive();
        call.enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryAdapter.setItems(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                // Silent fail
            }
        });
    }

    private void loadProducts() {
        loadProducts(null);
    }

    private void loadProducts(String categoryId) {
        Call<ProductListResponse> call = productService.getProducts(null, categoryId, null, null, 1, null, 1, 20);
        call.enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductResponse> products = response.body().getProducts();
                    productAdapter.setItems(products);
                }
            }

            @Override
            public void onFailure(Call<ProductListResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

