package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.ChangePasswordRequest;
import fpoly.haideptrai.duan1.api.models.LoginRequest;
import fpoly.haideptrai.duan1.api.models.LoginResponse;
import fpoly.haideptrai.duan1.api.models.UserInfo;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/logout")
    Call<ApiResponse<Void>> logout();

    @PUT("api/auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest request);

    @GET("api/auth/me")
    Call<UserInfo> getMe();
}

