package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.CustomerListResponse;
import fpoly.haideptrai.duan1.api.models.CustomerRequest;
import fpoly.haideptrai.duan1.api.models.CustomerResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CustomerService {
    @GET("api/customers")
    Call<CustomerListResponse> getCustomers(
            @Query("search") String search,
            @Query("type") String type,
            @Query("active") Boolean active,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    @GET("api/customers/{id}")
    Call<CustomerResponse> getById(@Path("id") String id);

    @POST("api/customers")
    Call<ApiResponse<CustomerResponse>> createCustomer(@Body CustomerRequest request);

    @PUT("api/customers/{id}")
    Call<ApiResponse<CustomerResponse>> updateCustomer(@Path("id") String id, @Body CustomerRequest request);

    @PATCH("api/customers/{id}/active")
    Call<ApiResponse<CustomerResponse>> toggleActive(@Path("id") String id, @Body CustomerRequest request);

    @DELETE("api/customers/{id}")
    Call<ApiResponse<Void>> deleteCustomer(@Path("id") String id);

    @GET("api/customers/{id}/statistics")
    Call<ApiResponse<Object>> getStatistics(@Path("id") String id, @Query("startDate") String startDate, @Query("endDate") String endDate);
}
