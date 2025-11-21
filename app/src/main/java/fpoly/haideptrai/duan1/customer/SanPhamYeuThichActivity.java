package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ProductListResponse;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import fpoly.haideptrai.duan1.api.services.ProductService;
import fpoly.haideptrai.duan1.customer.adapters.ProductHomeAdapter;
import fpoly.haideptrai.duan1.utils.FavoriteManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SanPhamYeuThichActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvSanPhamYeuThich;
    private LinearLayout layoutEmpty;
    private TextView txtEmptyMessage;
    
    private ProductService productService;
    private ProductHomeAdapter productAdapter;
    private FavoriteManager favoriteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_san_pham_yeu_thich);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        favoriteManager = FavoriteManager.getInstance(this);
        productService = ApiClient.getClient().create(ProductService.class);
        
        loadFavoriteProducts();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvSanPhamYeuThich = findViewById(R.id.rvSanPhamYeuThich);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        txtEmptyMessage = findViewById(R.id.txtEmptyMessage);
    }

    private void setupRecyclerView() {
        rvSanPhamYeuThich.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductHomeAdapter();
        productAdapter.setContext(this);
        rvSanPhamYeuThich.setAdapter(productAdapter);
        
        productAdapter.setOnProductClickListener(product -> {
            Intent intent = new Intent(this, ChiTietSanPhamActivity.class);
            intent.putExtra("product_id", product.get_id());
            startActivity(intent);
        });
        
        // Listen for favorite changes
        productAdapter.setOnFavoriteChangeListener((productId, isFavorite) -> {
            if (!isFavorite) {
                // Nếu bỏ yêu thích, reload danh sách
                loadFavoriteProducts();
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadFavoriteProducts() {
        // Kiểm tra token trước
        String token = fpoly.haideptrai.duan1.api.TokenStore.getToken();
        if (token == null || token.isEmpty()) {
            android.util.Log.w("SanPhamYeuThich", "No token found, user may not be logged in");
            showEmptyState();
            txtEmptyMessage.setText("Vui lòng đăng nhập để xem sản phẩm yêu thích");
            return;
        }
        
        // Load favorites from API
        fpoly.haideptrai.duan1.api.services.FavoriteService favoriteService = 
            fpoly.haideptrai.duan1.api.ApiClient.getClient().create(fpoly.haideptrai.duan1.api.services.FavoriteService.class);
        
        retrofit2.Call<java.util.List<ProductResponse>> call = favoriteService.getFavorites();
        call.enqueue(new retrofit2.Callback<java.util.List<ProductResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<ProductResponse>> call, 
                                 retrofit2.Response<java.util.List<ProductResponse>> response) {
                android.util.Log.d("SanPhamYeuThich", "Response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductResponse> favoriteProducts = response.body();
                    android.util.Log.d("SanPhamYeuThich", "Received " + favoriteProducts.size() + " favorite products");
                    
                    if (favoriteProducts.isEmpty()) {
                        showEmptyState();
                    } else {
                        productAdapter.setItems(favoriteProducts);
                        rvSanPhamYeuThich.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                        android.util.Log.d("SanPhamYeuThich", "Displayed " + favoriteProducts.size() + " favorite products");
                    }
                } else {
                    android.util.Log.e("SanPhamYeuThich", "API response not successful. Code: " + response.code());
                    
                    // Xử lý các mã lỗi phổ biến
                    if (response.code() == 401) {
                        android.util.Log.w("SanPhamYeuThich", "Unauthorized - token may be invalid");
                        txtEmptyMessage.setText("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại");
                    } else if (response.code() == 403) {
                        android.util.Log.w("SanPhamYeuThich", "Forbidden - access denied");
                        txtEmptyMessage.setText("Không có quyền truy cập");
                    } else {
                        txtEmptyMessage.setText("Không thể tải danh sách yêu thích");
                    }
                    
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            android.util.Log.e("SanPhamYeuThich", "Error body: " + errorString);
                        } catch (Exception e) {
                            android.util.Log.e("SanPhamYeuThich", "Error reading error body", e);
                        }
                    }
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<ProductResponse>> call, Throwable t) {
                android.util.Log.e("SanPhamYeuThich", "Error loading favorites: " + t.getMessage(), t);
                txtEmptyMessage.setText("Lỗi kết nối. Vui lòng thử lại");
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        rvSanPhamYeuThich.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        txtEmptyMessage.setText("Chưa có sản phẩm yêu thích nào");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload khi quay lại màn hình (có thể đã thay đổi yêu thích ở màn hình khác)
        loadFavoriteProducts();
    }
}

