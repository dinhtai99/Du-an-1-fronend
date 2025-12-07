package fpoly.haideptrai.duan1.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // ⚠️ QUAN TRỌNG: Thay đổi BASE_URL theo môi trường của bạn!
    // 
    // 📱 Android Emulator:
    //    private static final String BASE_URL = "http://10.0.2.2:3000/";
    //
    // 📱 Thiết bị thật (cùng WiFi):
    //    private static final String BASE_URL = "http://192.168.x.x:3000/"; // IP máy tính của bạn
    //    Để lấy IP: ifconfig (Mac/Linux) hoặc ipconfig (Windows)
    //
    // 📱 Hotspot/Mạng di động (điện thoại tạo hotspot):
    //    Nếu máy tính kết nối vào hotspot của điện thoại:
    //    - Lấy IP của máy tính khi kết nối hotspot (thường là 192.168.x.x hoặc 172.20.x.x)
    //    - Cập nhật BASE_URL với IP đó
    //
    // 🌐 Production:
    //    private static final String BASE_URL = "https://your-domain.com/";
    
    // ⚠️ QUAN TRỌNG: Chọn đúng BASE_URL theo thiết bị bạn đang dùng!
    // 
    // 📱 Android Emulator:
    //    private static final String BASE_URL = "http://10.0.2.2:3000/";
    //
    // 📱 Thiết bị thật (cùng WiFi):
    //    private static final String BASE_URL = "http://172.20.10.5:3000/"; // IP máy tính của bạn
    //    Để lấy IP: ifconfig (Mac/Linux) hoặc ipconfig (Windows)
    //
    // ⚠️ LƯU Ý: Điện thoại và máy tính PHẢI CÙNG MẠNG (cùng WiFi hoặc máy tính kết nối hotspot)
    // ⚠️ Nếu IP thay đổi, cập nhật BASE_URL và rebuild app
    // 
    // 🔍 Để lấy IP hiện tại của máy tính:
    //    Mac/Linux: ifconfig | grep "inet " | grep -v 127.0.0.1
    //    Windows: ipconfig
    //
    // 📱 IP hiện tại: 192.168.25.104 (mạng hiện tại)
    // 📱 Nếu dùng Emulator, đổi thành: http://10.0.2.2:3000/
    private static final String BASE_URL = "http://192.168.25.104:3000/"; // IP hiện tại của máy tính (thiết bị thật)
    
    // 📝 Các IP đã dùng trước đây (để tham khảo):
    // - http://10.24.25.34:3000/ (WiFi cũ)
    // - http://192.168.1.126:3000/ (WiFi cũ)
    // - http://172.20.10.3:3000/ (Hotspot cũ)
    // - http://172.20.10.5:3000/ (Hotspot/WiFi khác mạng)
    // - http://192.168.25.97:3000/ (WiFi hiện tại - cùng mạng với thiết bị)

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Logging interceptor để debug API calls
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Authorization interceptor từ TokenStore
            // Không gửi token cho các endpoint public (login, register)
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    String url = original.url().toString();

                    // Các endpoint public không cần token
                    boolean isPublicEndpoint = url.contains("/api/auth/login") ||
                                              url.contains("/api/auth/register");

                    if (!isPublicEndpoint) {
                    String token = TokenStore.getToken();
                    if (token != null && !token.isEmpty()) {
                        Request authed = original.newBuilder()
                                .addHeader("Authorization", "Bearer " + token)
                                .build();
                        android.util.Log.d("ApiClient", "Adding Authorization header with token");
                        return chain.proceed(authed);
                    } else {
                        android.util.Log.w("ApiClient", "No token available, request sent without Authorization header");
                        }
                    } else {
                        android.util.Log.d("ApiClient", "Public endpoint, skipping Authorization header");
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

            // Configure Gson - by default, Gson excludes null fields from JSON
            // This means null values won't be sent in the request body
            Gson gson = new GsonBuilder()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static void setBaseUrl(String baseUrl) {
        retrofit = null;
        // Có thể tạo method để thay đổi BASE_URL động nếu cần
    }
}

