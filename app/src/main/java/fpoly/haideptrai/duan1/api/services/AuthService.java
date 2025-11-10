package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.LoginRequest;
import fpoly.haideptrai.duan1.api.models.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}

