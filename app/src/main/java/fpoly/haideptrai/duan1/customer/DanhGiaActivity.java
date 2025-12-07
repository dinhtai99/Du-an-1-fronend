package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import fpoly.haideptrai.duan1.api.models.ReviewRequest;
import fpoly.haideptrai.duan1.api.models.ReviewResponse;
import fpoly.haideptrai.duan1.api.services.ProductService;
import fpoly.haideptrai.duan1.api.services.ReviewService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DanhGiaActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView imgProduct;
    private TextView txtProductName;
    private RatingBar ratingBar;
    private TextInputEditText edtComment;
    private MaterialButton btnSubmit;

    private ReviewService reviewService;
    private ProductService productService;
    private String productId;
    private String orderId;
    private String reviewId; // For editing existing review
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_gia);

        productId = getIntent().getStringExtra("product_id");
        orderId = getIntent().getStringExtra("order_id");
        reviewId = getIntent().getStringExtra("review_id");

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        isEditMode = reviewId != null && !reviewId.isEmpty();

        initViews();
        reviewService = ApiClient.getClient().create(ReviewService.class);
        productService = ApiClient.getClient().create(ProductService.class);
        
        loadProductInfo();
        if (isEditMode) {
            loadReviewInfo();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgProduct = findViewById(R.id.imgProduct);
        txtProductName = findViewById(R.id.txtProductName);
        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtComment);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            if (isEditMode) {
                updateReview();
            } else {
                submitReview();
            }
        });

        if (isEditMode) {
            btnSubmit.setText("Cập nhật đánh giá");
        }
    }

    private void loadProductInfo() {
        Call<ProductResponse> call = productService.getById(productId);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse product = response.body();
                    txtProductName.setText(product.getName());
                    
                    String imageUrl = product.getImage();
                    if (imageUrl != null && !imageUrl.trim().isEmpty() && !imageUrl.contains("example.com")) {
                        Glide.with(DanhGiaActivity.this)
                                .load(imageUrl)
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher)
                                .centerCrop()
                                .into(imgProduct);
                    } else {
                        imgProduct.setImageResource(R.mipmap.ic_launcher);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                // Silent fail
            }
        });
    }

    private void loadReviewInfo() {
        // For edit mode, we would need to load the review first
        // This could be done by getting user's reviews and finding the one with matching productId
        // For now, we'll skip this and let user edit from scratch
        // TODO: Implement loading review data for edit mode
    }

    private void submitReview() {
        int rating = (int) ratingBar.getRating();
        String comment = edtComment.getText() != null ? edtComment.getText().toString().trim() : "";

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rating < 1 || rating > 5) {
            Toast.makeText(this, "Đánh giá phải từ 1 đến 5 sao!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang gửi...");

        ReviewRequest request = new ReviewRequest();
        request.setProductId(productId);
        request.setOrderId(orderId);
        request.setRating(rating);
        request.setComment(comment.isEmpty() ? null : comment);

        Call<ApiResponse<ReviewResponse>> call = reviewService.createReview(request);
        call.enqueue(new Callback<ApiResponse<ReviewResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReviewResponse>> call, Response<ApiResponse<ReviewResponse>> response) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Gửi đánh giá");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ReviewResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(DanhGiaActivity.this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String message = apiResponse.getMessage();
                        if (message != null && message.contains("đã đánh giá")) {
                            Toast.makeText(DanhGiaActivity.this, message, Toast.LENGTH_LONG).show();
                        } else if (message != null && message.contains("chưa mua")) {
                            Toast.makeText(DanhGiaActivity.this, message, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(DanhGiaActivity.this, 
                                message != null ? message : "Không thể gửi đánh giá", 
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // Try to parse error message
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("đã đánh giá")) {
                                Toast.makeText(DanhGiaActivity.this, "Bạn đã đánh giá sản phẩm này rồi!", Toast.LENGTH_LONG).show();
                            } else if (errorBody.contains("chưa mua")) {
                                Toast.makeText(DanhGiaActivity.this, "Bạn chưa mua sản phẩm này!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(DanhGiaActivity.this, "Không thể gửi đánh giá", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DanhGiaActivity.this, "Không thể gửi đánh giá", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(DanhGiaActivity.this, "Không thể gửi đánh giá", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ReviewResponse>> call, Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Gửi đánh giá");
                Toast.makeText(DanhGiaActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReview() {
        int rating = (int) ratingBar.getRating();
        String comment = edtComment.getText() != null ? edtComment.getText().toString().trim() : "";

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rating < 1 || rating > 5) {
            Toast.makeText(this, "Đánh giá phải từ 1 đến 5 sao!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang cập nhật...");

        ReviewRequest request = new ReviewRequest();
        request.setRating(rating);
        request.setComment(comment.isEmpty() ? null : comment);

        Call<ApiResponse<ReviewResponse>> call = reviewService.updateReview(reviewId, request);
        call.enqueue(new Callback<ApiResponse<ReviewResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReviewResponse>> call, Response<ApiResponse<ReviewResponse>> response) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Cập nhật đánh giá");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ReviewResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(DanhGiaActivity.this, "Cập nhật đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(DanhGiaActivity.this, 
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Không thể cập nhật đánh giá", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DanhGiaActivity.this, "Không thể cập nhật đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ReviewResponse>> call, Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Cập nhật đánh giá");
                Toast.makeText(DanhGiaActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

