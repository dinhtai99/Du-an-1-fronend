package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.UserListResponse;
import fpoly.haideptrai.duan1.api.models.UserRequest;
import fpoly.haideptrai.duan1.api.models.UserResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserService {
    @GET("api/users")
    Call<UserListResponse> getUsers(
            @Query("search") String search,
            @Query("role") String role,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    @GET("api/users/{id}")
    Call<UserResponse> getById(@Path("id") String id);

    @POST("api/users")
    Call<ApiResponse<UserResponse>> createUser(@Body UserRequest request);

    @PUT("api/users/{id}")
    Call<ApiResponse<UserResponse>> updateUser(@Path("id") String id, @Body UserRequest request);

    @DELETE("api/users/{id}")
    Call<ApiResponse<Void>> deleteUser(@Path("id") String id);
}
