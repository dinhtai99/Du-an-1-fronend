# Hướng dẫn kết nối API với Backend Server

## Đã hoàn thành

✅ Đã thêm dependencies:
- Retrofit 2.9.0
- OkHttp 4.12.0
- Gson 2.10.1

✅ Đã thêm permissions:
- INTERNET
- ACCESS_NETWORK_STATE

✅ Đã tạo cấu trúc API:
- `ApiClient.java` - Retrofit client
- `AuthService.java` - API service interface cho authentication
- Models: `ApiResponse`, `LoginRequest`, `UserResponse`
- `ApiHelper.java` - Helper để convert API response

✅ Đã cập nhật `DangNhapActivity` để sử dụng API thay vì Room database

## Cấu hình BASE_URL

### Bước 1: Mở file `ApiClient.java`

File nằm tại: `app/src/main/java/fpoly/haideptrai/duan1/api/ApiClient.java`

### Bước 2: Thay đổi BASE_URL

Tìm dòng:
```java
private static final String BASE_URL = "http://10.0.2.2:3000/";
```

Thay đổi theo môi trường của bạn:

#### Nếu dùng Android Emulator:
```java
private static final String BASE_URL = "http://10.0.2.2:3000/";
```
- `10.0.2.2` là địa chỉ localhost của máy host trong Android Emulator

#### Nếu dùng thiết bị thật:
```java
private static final String BASE_URL = "http://192.168.x.x:3000/";
```
- Thay `192.168.x.x` bằng IP máy tính chạy server
- Để tìm IP: 
  - Windows: `ipconfig` trong CMD
  - Mac/Linux: `ifconfig` hoặc `ip addr`

#### Nếu server đã deploy:
```java
private static final String BASE_URL = "https://your-domain.com/";
```

## Kiểm tra kết nối

### 1. Đảm bảo server đang chạy
```bash
# Trong thư mục backend
node server.js
# hoặc
npm start
```

Server phải chạy trên port 3000 (hoặc port bạn đã cấu hình)

### 2. Test API từ Android

1. Mở app trên emulator/thiết bị
2. Thử đăng nhập
3. Kiểm tra Logcat trong Android Studio để xem:
   - Request/Response logs (OkHttp logging interceptor)
   - Lỗi kết nối nếu có

### 3. Kiểm tra Logcat

Trong Android Studio:
- Mở tab **Logcat**
- Filter: `OkHttp` hoặc `ApiClient`
- Xem request/response logs

## Cấu trúc API Endpoints

Dựa trên code server bạn cung cấp, các endpoints:

### Authentication
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/register` - Đăng ký

### Users
- `GET /api/users` - Lấy danh sách users
- `GET /api/users/:id` - Lấy user theo ID
- `PUT /api/users/:id` - Cập nhật user
- `DELETE /api/users/:id` - Xóa user

### Products
- `GET /api/products` - Lấy danh sách sản phẩm
- `POST /api/products` - Tạo sản phẩm mới
- `PUT /api/products/:id` - Cập nhật sản phẩm
- `DELETE /api/products/:id` - Xóa sản phẩm

### Categories
- `GET /api/categories` - Lấy danh sách loại sản phẩm
- `POST /api/categories` - Tạo loại sản phẩm mới
- `PUT /api/categories/:id` - Cập nhật loại sản phẩm
- `DELETE /api/categories/:id` - Xóa loại sản phẩm

### Customers
- `GET /api/customers` - Lấy danh sách khách hàng
- `POST /api/customers` - Tạo khách hàng mới
- `PUT /api/customers/:id` - Cập nhật khách hàng
- `DELETE /api/customers/:id` - Xóa khách hàng

### Invoices
- `GET /api/invoices` - Lấy danh sách hóa đơn
- `POST /api/invoices` - Tạo hóa đơn mới
- `PUT /api/invoices/:id` - Cập nhật hóa đơn
- `DELETE /api/invoices/:id` - Xóa hóa đơn

### Statistics
- `GET /api/statistics` - Lấy thống kê

## Tạo thêm API Services

Để thêm các service khác, tạo file mới trong `api/services/`:

Ví dụ: `ProductService.java`
```java
package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Body;

public interface ProductService {
    @GET("api/products")
    Call<ApiResponse<List<ProductResponse>>> getAll();
    
    @GET("api/products/{id}")
    Call<ApiResponse<ProductResponse>> getById(@Path("id") String id);
    
    @POST("api/products")
    Call<ApiResponse<ProductResponse>> create(@Body ProductResponse product);
    
    @PUT("api/products/{id}")
    Call<ApiResponse<ProductResponse>> update(@Path("id") String id, @Body ProductResponse product);
    
    @DELETE("api/products/{id}")
    Call<ApiResponse<Void>> delete(@Path("id") String id);
}
```

Sử dụng:
```java
ProductService productService = ApiClient.getClient().create(ProductService.class);
Call<ApiResponse<List<ProductResponse>>> call = productService.getAll();
```

## Xử lý lỗi

App đã xử lý các trường hợp:
- ✅ Kết nối timeout
- ✅ Server error (401, 404, 500...)
- ✅ Network error
- ✅ Response không hợp lệ

## Lưu ý

1. **CORS**: Nếu gặp lỗi CORS, đảm bảo server đã cấu hình CORS đúng
2. **HTTPS**: Nếu dùng HTTPS, cần cấu hình certificate
3. **Firewall**: Đảm bảo firewall không chặn port 3000
4. **Network**: Thiết bị và server phải cùng mạng (hoặc server public)

## Troubleshooting

### Lỗi: "Unable to resolve host"
- Kiểm tra BASE_URL có đúng không
- Kiểm tra kết nối mạng
- Kiểm tra server có đang chạy không

### Lỗi: "Connection refused"
- Server chưa chạy
- Port không đúng
- Firewall chặn

### Lỗi: "Timeout"
- Server chậm
- Mạng không ổn định
- Tăng timeout trong `ApiClient.java`

### Response null hoặc empty
- Kiểm tra format response từ server
- Kiểm tra models có khớp với API response không

