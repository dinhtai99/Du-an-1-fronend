package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.CustomerListResponse;
import fpoly.haideptrai.duan1.api.models.CustomerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
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
}
