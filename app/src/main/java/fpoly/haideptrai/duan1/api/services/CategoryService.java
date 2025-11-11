package fpoly.haideptrai.duan1.api.services;

import java.util.List;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.CategoryListResponse;
import fpoly.haideptrai.duan1.api.models.CategoryRequest;
import fpoly.haideptrai.duan1.api.models.CategoryResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CategoryService {
    @GET("api/categories")
    Call<CategoryListResponse> getCategories(
            @Query("search") String search,
            @Query("status") Integer status,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    @GET("api/categories/all")
    Call<List<CategoryResponse>> getAllActive();

    @GET("api/categories/{id}")
    Call<CategoryResponse> getById(@Path("id") String id);

    @POST("api/categories")
    Call<ApiResponse<CategoryResponse>> createCategory(@Body CategoryRequest request);

    @PUT("api/categories/{id}")
    Call<ApiResponse<CategoryResponse>> updateCategory(@Path("id") String id, @Body CategoryRequest request);

    @DELETE("api/categories/{id}")
    Call<ApiResponse<Void>> deleteCategory(@Path("id") String id);
}
