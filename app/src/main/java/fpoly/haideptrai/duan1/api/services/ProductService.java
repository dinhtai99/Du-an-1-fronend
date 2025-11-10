package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.ProductListResponse;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import retrofit2.Call;
import retrofit2.http.GET;
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
}
