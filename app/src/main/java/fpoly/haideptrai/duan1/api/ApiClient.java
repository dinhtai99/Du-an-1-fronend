package fpoly.haideptrai.duan1.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // Thay đổi BASE_URL theo địa chỉ server của bạn
    // Ví dụ: "http://192.168.1.100:3000" (local network)
    // Hoặc: "https://your-domain.com" (production)
    private static final String BASE_URL = "http://10.0.2.2:3000/"; // Android Emulator localhost
    // Nếu dùng thiết bị thật, thay bằng IP máy chạy server: "http://192.168.x.x:3000/"
    
    private static Retrofit retrofit = null;
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Logging interceptor để debug API calls
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Authorization interceptor từ TokenStore
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    String token = TokenStore.getToken();
                    if (token != null && !token.isEmpty()) {
                        Request authed = original.newBuilder()
                                .addHeader("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(authed);
                    }
                    return chain.proceed(original);
                }
            };
            
            // OkHttp client với timeout
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    public static void setBaseUrl(String baseUrl) {
        retrofit = null;
        // Có thể tạo method để thay đổi BASE_URL động nếu cần
    }
}

