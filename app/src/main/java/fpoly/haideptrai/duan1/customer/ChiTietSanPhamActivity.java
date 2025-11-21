package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import fpoly.haideptrai.duan1.api.models.ReviewListResponse;
import fpoly.haideptrai.duan1.api.models.ReviewResponse;
import fpoly.haideptrai.duan1.api.services.ProductService;
import fpoly.haideptrai.duan1.api.services.ReviewService;
import fpoly.haideptrai.duan1.customer.adapters.ReviewAdapter;
import fpoly.haideptrai.duan1.customer.models.CartItem;
import fpoly.haideptrai.duan1.utils.CartManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietSanPhamActivity extends AppCompatActivity {

    private ImageView imgSanPham;
    private TextView txtTenSanPham, txtLoaiSanPham, txtGiaBan, txtMoTa;
    private TextView txtRatingAverage, txtReviewCount, txtNoReviews;
    private MaterialButton btnThemVaoGioHang, btnDanhGia;
    private BottomNavigationView bottomNavigation;
    private RecyclerView rvReviews;
    
    private ProductService productService;
    private ReviewService reviewService;
    private CartManager cartManager;
    private ReviewAdapter reviewAdapter;
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
        reviewService = ApiClient.getClient().create(ReviewService.class);
        cartManager = new CartManager(this);
        loadProductDetails();
        loadReviews();
    }

    private void initViews() {
        imgSanPham = findViewById(R.id.imgSanPham);
        txtTenSanPham = findViewById(R.id.txtTenSanPham);
        txtLoaiSanPham = findViewById(R.id.txtLoaiSanPham);
        txtGiaBan = findViewById(R.id.txtGiaBan);
        txtMoTa = findViewById(R.id.txtMoTa);
        txtRatingAverage = findViewById(R.id.txtRatingAverage);
        txtReviewCount = findViewById(R.id.txtReviewCount);
        txtNoReviews = findViewById(R.id.txtNoReviews);
        btnThemVaoGioHang = findViewById(R.id.btnThemVaoGioHang);
        btnDanhGia = findViewById(R.id.btnDanhGia);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvReviews = findViewById(R.id.rvReviews);

        // Setup RecyclerView cho reviews
        reviewAdapter = new ReviewAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
        rvReviews.setNestedScrollingEnabled(false);

        btnThemVaoGioHang.setOnClickListener(v -> {
            if (product != null) {
                addToCart();
            }
        });

        // Ẩn nút đánh giá - chỉ hiển thị sau khi thanh toán thành công
        btnDanhGia.setVisibility(View.GONE);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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
        // Tên sản phẩm
        txtTenSanPham.setText(product.getName());
        
        // Loại sản phẩm
        if (product.getCategory() != null) {
            txtLoaiSanPham.setText(product.getCategory().getName());
        } else {
            txtLoaiSanPham.setText("Chưa phân loại");
        }
        
        // Giá bán (chỉ hiển thị giá bán, không hiển thị giá nhập)
        txtGiaBan.setText(formatPrice(product.getPrice()));
        
        // Mô tả sản phẩm (nếu có)
        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            txtMoTa.setText(product.getDescription());
            txtMoTa.setVisibility(android.view.View.VISIBLE);
        } else {
            txtMoTa.setVisibility(android.view.View.GONE);
        }

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

    private void addToCart() {
        if (product == null) {
            Toast.makeText(this, "Không thể thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra tồn kho
        if (product.getStock() != null && product.getStock() <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = product.getPrice() != null ? product.getPrice() : 0;
        CartItem cartItem = new CartItem(product, 1, price);
        cartManager.addToCart(cartItem);

        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    private void loadReviews() {
        Call<ReviewListResponse> call = reviewService.getProductReviews(productId, 1, 10);
        call.enqueue(new Callback<ReviewListResponse>() {
            @Override
            public void onResponse(Call<ReviewListResponse> call, Response<ReviewListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReviewListResponse reviewList = response.body();
                    if (reviewList.getReviews() != null && !reviewList.getReviews().isEmpty()) {
                        reviewAdapter.setItems(reviewList.getReviews());
                        rvReviews.setVisibility(View.VISIBLE);
                        txtNoReviews.setVisibility(View.GONE);
                        
                        // Hiển thị rating và count
                        int total = reviewList.getTotal() != null ? reviewList.getTotal() : reviewList.getReviews().size();
                        txtReviewCount.setText("(" + total + ")");
                        
                        // Tính rating trung bình từ reviews
                        double avgRating = 0.0;
                        for (ReviewResponse review : reviewList.getReviews()) {
                            if (review.getRating() != null) {
                                avgRating += review.getRating();
                            }
                        }
                        if (reviewList.getReviews().size() > 0) {
                            avgRating = avgRating / reviewList.getReviews().size();
                        }
                        
                        txtRatingAverage.setText(String.format(Locale.getDefault(), "%.1f", avgRating));
                    } else {
                        reviewAdapter.setItems(java.util.Collections.emptyList());
                        rvReviews.setVisibility(View.GONE);
                        txtNoReviews.setVisibility(View.VISIBLE);
                        txtRatingAverage.setText("0.0");
                        txtReviewCount.setText("(0)");
                    }
                } else {
                    // Silent fail - không hiển thị lỗi nếu không load được reviews
                    reviewAdapter.setItems(java.util.Collections.emptyList());
                    rvReviews.setVisibility(View.GONE);
                    txtNoReviews.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ReviewListResponse> call, Throwable t) {
                // Silent fail
                android.util.Log.e("ChiTietSanPham", "Error loading reviews: " + t.getMessage());
                reviewAdapter.setItems(java.util.Collections.emptyList());
                rvReviews.setVisibility(View.GONE);
                txtNoReviews.setVisibility(View.VISIBLE);
            }
        });
    }
}

