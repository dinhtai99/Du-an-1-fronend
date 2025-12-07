package fpoly.haideptrai.duan1.api.services;

import java.util.List;

import fpoly.haideptrai.duan1.api.models.AddressRequest;
import fpoly.haideptrai.duan1.api.models.AddressResponse;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AddressService {
    /**
     * Lấy danh sách địa chỉ của user hiện tại
     */
    @GET("api/addresses")
    Call<List<AddressResponse>> getAddresses();

    /**
     * Lấy địa chỉ mặc định
     */
    @GET("api/addresses/default")
    Call<AddressResponse> getDefaultAddress();

    /**
     * Thêm địa chỉ mới
     */
    @POST("api/addresses")
    Call<ApiResponse<AddressResponse>> createAddress(@Body AddressRequest request);

    /**
     * Cập nhật địa chỉ
     */
    @PUT("api/addresses/{id}")
    Call<ApiResponse<AddressResponse>> updateAddress(@Path("id") String id, @Body AddressRequest request);

    /**
     * Xóa địa chỉ
     */
    @DELETE("api/addresses/{id}")
    Call<ApiResponse<Void>> deleteAddress(@Path("id") String id);
}

