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

// ZaloPay SDK
import vn.zalopay.sdk.ZaloPaySDK;

public class MyApplication extends Application {
    // ZaloPay Configuration
    // TODO: Thay đổi APP_ID theo thông tin từ ZaloPay Developer Portal
    // Sandbox: 2554 (phải khớp với backend .env)
    // Production: Lấy từ ZaloPay Developer Portal
    private static final int ZALOPAY_APP_ID = 2554; // App ID sandbox
    
    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo TokenStore để load token từ SharedPreferences khi app khởi động
        fpoly.haideptrai.duan1.api.TokenStore.initialize(this);
        DatabaseInitializer.initialize(this);
        
        // Khởi tạo ZaloPay SDK
        // TODO: Đổi sang production khi deploy
        try {
            // Dùng reflection để tìm và gọi init method
            // Thử tìm method init với các signature khác nhau
            java.lang.reflect.Method initMethod = null;
            Object environmentValue = null;
            
            // Thử tìm Environment class
            Class<?> environmentClass = null;
            try {
                // Thử inner class
                environmentClass = Class.forName("vn.zalopay.sdk.ZaloPaySDK$Environment");
            } catch (ClassNotFoundException e1) {
                try {
                    // Thử package riêng
                    environmentClass = Class.forName("vn.zalopay.sdk.Environment");
                } catch (ClassNotFoundException e2) {
                    // Không tìm thấy Environment class
                }
            }
            
            if (environmentClass != null) {
                // Tìm field SANDBOX
                try {
                    environmentValue = environmentClass.getField("SANDBOX").get(null);
                    // Tìm method init(int, Environment)
                    initMethod = ZaloPaySDK.class.getMethod("init", int.class, environmentClass);
                } catch (Exception e) {
                    Log.w("ZaloPay", "Could not find SANDBOX field or init method with Environment: " + e.getMessage());
                }
            }
            
            if (initMethod != null && environmentValue != null) {
                // Gọi init với Environment
                initMethod.invoke(null, ZALOPAY_APP_ID, environmentValue);
                Log.i("ZaloPay", "ZaloPay SDK initialized successfully with App ID: " + ZALOPAY_APP_ID);
            } else {
                // Fallback: Thử dùng int
                try {
                    initMethod = ZaloPaySDK.class.getMethod("init", int.class, int.class);
                    initMethod.invoke(null, ZALOPAY_APP_ID, 0); // 0 = SANDBOX
                    Log.i("ZaloPay", "ZaloPay SDK initialized with int parameter (SANDBOX=0)");
                } catch (NoSuchMethodException e) {
                    // Thử method khác
                    initMethod = ZaloPaySDK.class.getMethod("init", int.class);
                    initMethod.invoke(null, ZALOPAY_APP_ID);
                    Log.i("ZaloPay", "ZaloPay SDK initialized with App ID only");
                }
            }
        } catch (Exception e) {
            Log.e("ZaloPay", "Failed to initialize ZaloPay SDK: " + e.getMessage(), e);
            Log.e("ZaloPay", "Please check ZaloPay SDK documentation for correct initialization method");
        }
        
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









