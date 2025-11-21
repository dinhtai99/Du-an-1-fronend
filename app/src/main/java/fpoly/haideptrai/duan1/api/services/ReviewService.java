package fpoly.haideptrai.duan1.api.services;

import java.util.List;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.ReviewListResponse;
import fpoly.haideptrai.duan1.api.models.ReviewRequest;
import fpoly.haideptrai.duan1.api.models.ReviewResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReviewService {
    // Lấy đánh giá của sản phẩm
    @GET("api/reviews/product/{productId}")
    Call<ReviewListResponse> getProductReviews(
            @Path("productId") String productId,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    // Lấy đánh giá của user (chỉ customer)
    @GET("api/reviews/my")
    Call<List<ReviewResponse>> getMyReviews();

    // Thêm đánh giá (chỉ customer đã mua sản phẩm)
    @POST("api/reviews")
    Call<ApiResponse<ReviewResponse>> createReview(@Body ReviewRequest request);

    // Cập nhật đánh giá (chỉ customer)
    @PUT("api/reviews/{id}")
    Call<ApiResponse<ReviewResponse>> updateReview(@Path("id") String id, @Body ReviewRequest request);

    // Xóa đánh giá (chỉ customer)
    @DELETE("api/reviews/{id}")
    Call<ApiResponse<Void>> deleteReview(@Path("id") String id);
}

