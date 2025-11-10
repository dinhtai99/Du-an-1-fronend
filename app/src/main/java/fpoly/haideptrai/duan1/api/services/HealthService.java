package fpoly.haideptrai.duan1.api.services;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface HealthService {
    @GET("/")
    Call<ResponseBody> ping();
}
