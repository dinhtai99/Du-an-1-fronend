package fpoly.haideptrai.duan1.api.services;

import java.util.List;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.VoucherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VoucherService {
    @GET("api/vouchers")
    Call<ApiResponse<List<VoucherResponse>>> getVouchers(
            @Query("status") String status,
            @Query("active") Boolean active
    );

    @GET("api/vouchers/{id}")
    Call<ApiResponse<VoucherResponse>> getById(@Path("id") String id);

    @GET("api/vouchers/validate/{code}")
    Call<ApiResponse<VoucherResponse>> validateCode(@Path("code") String code);
}

