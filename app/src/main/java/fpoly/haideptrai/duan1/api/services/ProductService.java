package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.ProductListResponse;
import fpoly.haideptrai.duan1.api.models.ProductRequest;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductService {
    @GET("api/products")
    Call<ProductListResponse> getProducts(
            @Query("search") String search,
            @Query("category") String category,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("status") Integer status,
            @Query("lowStock") Boolean lowStock,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    @GET("api/products/{id}")
    Call<ProductResponse> getById(@Path("id") String id);

    @POST("api/products")
    Call<ApiResponse<ProductResponse>> createProduct(@Body ProductRequest request);

    @PUT("api/products/{id}")
    Call<ApiResponse<ProductResponse>> updateProduct(@Path("id") String id, @Body ProductRequest request);

    @DELETE("api/products/{id}")
    Call<ApiResponse<Void>> deleteProduct(@Path("id") String id, @Query("hardDelete") Boolean hardDelete);

    @GET("api/products/low-stock/all")
    Call<java.util.List<ProductResponse>> getLowStockProducts();

    @GET("api/products/export/excel")
    Call<ApiResponse<Object>> exportExcel();
}
