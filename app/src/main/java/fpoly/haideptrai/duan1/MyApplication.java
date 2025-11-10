package fpoly.haideptrai.duan1;

import android.app.Application;
import android.util.Log;

import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.services.HealthService;
import fpoly.haideptrai.duan1.utils.DatabaseInitializer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseInitializer.initialize(this);
        pingApi();
    }

    private void pingApi() {
        try {
            HealthService healthService = ApiClient.getClient().create(HealthService.class);
            healthService.ping().enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.i("API_HEALTH", "OK: " + response.code());
                    } else {
                        Log.w("API_HEALTH", "UNHEALTHY: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("API_HEALTH", "FAIL: " + t.getMessage(), t);
                }
            });
        } catch (Exception e) {
            Log.e("API_HEALTH", "EXCEPTION: " + e.getMessage(), e);
        }
    }
}









