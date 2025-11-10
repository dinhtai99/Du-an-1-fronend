package fpoly.haideptrai.duan1.api.services;

import java.util.List;

import fpoly.haideptrai.duan1.api.models.CategoryListResponse;
import fpoly.haideptrai.duan1.api.models.CategoryResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Path;

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
}
