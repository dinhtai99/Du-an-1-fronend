package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ReviewListResponse;
import fpoly.haideptrai.duan1.api.models.ReviewResponse;
import fpoly.haideptrai.duan1.api.services.ReviewService;
import fpoly.haideptrai.duan1.customer.adapters.ReviewAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DanhSachDanhGiaActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtProductName, txtRatingAverage, txtReviewCount;
    private android.widget.LinearLayout txtNoReviews;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private ReviewService reviewService;
    private String productId;
    private String productName;
    private int currentPage = 1;
    private int limit = 20;
    private boolean isLoading = false;
    private boolean hasMorePages = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_danh_gia);

        productId = getIntent().getStringExtra("product_id");
        productName = getIntent().getStringExtra("product_name");

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        reviewService = ApiClient.getClient().create(ReviewService.class);
        loadReviews(1);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtProductName = findViewById(R.id.txtProductName);
        txtRatingAverage = findViewById(R.id.txtRatingAverage);
        txtReviewCount = findViewById(R.id.txtReviewCount);
        txtNoReviews = findViewById(R.id.txtNoReviews);
        rvReviews = findViewById(R.id.rvReviews);

        if (productName != null && !productName.isEmpty()) {
            txtProductName.setText(productName);
        } else {
            txtProductName.setText("Đánh giá sản phẩm");
        }

        reviewAdapter = new ReviewAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadReviews(int page) {
        if (isLoading) return;
        isLoading = true;

        Call<ReviewListResponse> call = reviewService.getProductReviews(productId, page, limit);
        call.enqueue(new Callback<ReviewListResponse>() {
            @Override
            public void onResponse(Call<ReviewListResponse> call, Response<ReviewListResponse> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    ReviewListResponse reviewList = response.body();
                    List<ReviewResponse> reviews = reviewList.getReviews();
                    Integer total = reviewList.getTotal();
                    Integer totalPages = reviewList.getTotalPages();

                    if (reviews != null && !reviews.isEmpty()) {
                        if (page == 1) {
                            reviewAdapter.setItems(reviews);
                        } else {
                            reviewAdapter.addItems(reviews);
                        }
                        rvReviews.setVisibility(View.VISIBLE);
                        txtNoReviews.setVisibility(View.GONE);

                        // Update rating and count
                        if (total != null) {
                            txtReviewCount.setText("(" + total + " đánh giá)");
                        }

                        // Calculate average rating
                        double avgRating = 0.0;
                        int count = 0;
                        for (ReviewResponse review : reviewAdapter.getItems()) {
                            if (review.getRating() != null) {
                                avgRating += review.getRating();
                                count++;
                            }
                        }
                        if (count > 0) {
                            avgRating = avgRating / count;
                            txtRatingAverage.setText(String.format(Locale.getDefault(), "%.1f", avgRating));
                        }

                        // Check if there are more pages
                        hasMorePages = totalPages != null && page < totalPages;
                    } else {
                        if (page == 1) {
                            reviewAdapter.setItems(java.util.Collections.emptyList());
                            rvReviews.setVisibility(View.GONE);
                            txtNoReviews.setVisibility(View.VISIBLE);
                            txtRatingAverage.setText("0.0");
                            txtReviewCount.setText("(0 đánh giá)");
                        }
                        hasMorePages = false;
                    }
                } else {
                    if (page == 1) {
                        Toast.makeText(DanhSachDanhGiaActivity.this, "Không tải được đánh giá", Toast.LENGTH_SHORT).show();
                        reviewAdapter.setItems(java.util.Collections.emptyList());
                        rvReviews.setVisibility(View.GONE);
                        txtNoReviews.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ReviewListResponse> call, Throwable t) {
                isLoading = false;
                if (page == 1) {
                    Toast.makeText(DanhSachDanhGiaActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                    reviewAdapter.setItems(java.util.Collections.emptyList());
                    rvReviews.setVisibility(View.GONE);
                    txtNoReviews.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}

