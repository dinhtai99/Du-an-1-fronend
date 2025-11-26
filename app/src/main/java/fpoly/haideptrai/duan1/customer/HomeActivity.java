package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
import fpoly.haideptrai.duan1.customer.adapters.BannerAdapter;
import fpoly.haideptrai.duan1.customer.adapters.CategoryHomeAdapter;
import fpoly.haideptrai.duan1.customer.adapters.ProductHomeAdapter;
import fpoly.haideptrai.duan1.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView txtGreeting, txtSubGreeting, txtXemTatCa;
    private ImageButton btnMenu, btnSearch;
    private ViewPager2 viewPagerBanner;
    private LinearLayout layoutBannerIndicators;
    private RecyclerView rvDanhMuc, rvSanPham;
    private BottomNavigationView bottomNavigation;

    private BannerAdapter bannerAdapter;
    private CategoryHomeAdapter categoryAdapter;
    private ProductHomeAdapter productAdapter;
    private Handler bannerHandler;
    private Runnable bannerRunnable;
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
        setupBanner();
        loadCategories();
        loadProducts();
    }

    private void initViews() {
        txtGreeting = findViewById(R.id.txtGreeting);
        txtSubGreeting = findViewById(R.id.txtSubGreeting);
        txtXemTatCa = findViewById(R.id.txtXemTatCa);
        btnMenu = findViewById(R.id.btnMenu);
        btnSearch = findViewById(R.id.btnSearch);
        viewPagerBanner = findViewById(R.id.viewPagerBanner);
        layoutBannerIndicators = findViewById(R.id.layoutBannerIndicators);
        rvDanhMuc = findViewById(R.id.rvDanhMuc);
        rvSanPham = findViewById(R.id.rvSanPham);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Setup RecyclerViews
        rvDanhMuc.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryHomeAdapter();
        rvDanhMuc.setAdapter(categoryAdapter);

        rvSanPham.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductHomeAdapter();
        productAdapter.setContext(this);
        rvSanPham.setAdapter(productAdapter);
    }

    private void setupBanner() {
        // Tạo danh sách banner images (có thể thay bằng URL từ API)
        List<Integer> bannerImages = new ArrayList<>();
        bannerImages.add(R.drawable.logo); // Banner 1
        bannerImages.add(R.drawable.logo); // Banner 2 - có thể thay bằng hình khác
        bannerImages.add(R.drawable.logo); // Banner 3 - có thể thay bằng hình khác

        // Nếu chỉ có 1 banner, không cần slider
        if (bannerImages.size() <= 1) {
            viewPagerBanner.setVisibility(View.GONE);
            layoutBannerIndicators.setVisibility(View.GONE);
            return;
        }

        bannerAdapter = new BannerAdapter(bannerImages);
        viewPagerBanner.setAdapter(bannerAdapter);

        // Thêm page transformer để có hiệu ứng đẹp hơn (fade và scale nhẹ)
        viewPagerBanner.setPageTransformer((page, position) -> {
            float absPosition = Math.abs(position);
            // Fade effect
            page.setAlpha(1 - absPosition * 0.3f);
            // Scale effect nhẹ hơn
            float scale = 1 - absPosition * 0.05f;
            page.setScaleX(scale);
            page.setScaleY(scale);
        });

        // Tăng offscreen page limit để smooth hơn
        viewPagerBanner.setOffscreenPageLimit(2);

        // Setup indicators
        setupBannerIndicators(bannerImages.size());

        // Setup page change listener
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBannerIndicators(position);
            }
        });

        // Auto scroll banner
        bannerHandler = new Handler(Looper.getMainLooper());
        startAutoScrollBanner(bannerImages.size());
    }

    private void setupBannerIndicators(int count) {
        layoutBannerIndicators.removeAllViews();

        for (int i = 0; i < count; i++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(8), dpToPx(8)
            );
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(R.drawable.bg_indicator_inactive);
            layoutBannerIndicators.addView(indicator);
        }

        // Set first indicator as active
        if (count > 0) {
            updateBannerIndicators(0);
        }
    }

    private void updateBannerIndicators(int position) {
        int childCount = layoutBannerIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View indicator = layoutBannerIndicators.getChildAt(i);
            if (i == position) {
                indicator.setBackgroundResource(R.drawable.bg_indicator_active);
            } else {
                indicator.setBackgroundResource(R.drawable.bg_indicator_inactive);
            }
        }
    }

    private void startAutoScrollBanner(int itemCount) {
        if (itemCount <= 1) return;

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPagerBanner != null) {
                    int currentItem = viewPagerBanner.getCurrentItem();
                    int nextItem = (currentItem + 1) % itemCount;
                    viewPagerBanner.setCurrentItem(nextItem, true);
                }
                bannerHandler.postDelayed(this, 3000); // Auto scroll mỗi 3 giây
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dừng auto scroll khi activity pause
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tiếp tục auto scroll khi activity resume
        if (bannerAdapter != null && bannerAdapter.getItemCount() > 1) {
            startAutoScrollBanner(bannerAdapter.getItemCount());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    private void setupClickListeners() {
        btnMenu.setOnClickListener(v -> {
            // TODO: Open drawer menu
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, TimKiemSanPhamActivity.class);
            startActivity(intent);
        });

        txtXemTatCa.setOnClickListener(v -> {
            // TODO: Navigate to all products
            Toast.makeText(this, "Xem tất cả sản phẩm", Toast.LENGTH_SHORT).show();
        });

        categoryAdapter.setOnCategoryClickListener(category -> {
            // Filter products by category
            categoryAdapter.setSelectedCategory(category.get_id());
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
            } else if (itemId == R.id.nav_support) {
                Intent intent = new Intent(this, ChamSocKhachHangActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_discount) {
                Intent intent = new Intent(this, QuanLyVoucherActivity.class);
                startActivity(intent);
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

