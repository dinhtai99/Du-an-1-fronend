# Hướng dẫn tích hợp ZaloPay vào Android App

## 📋 Tổng quan

ZaloPay tích hợp theo flow App-to-App:
1. User chọn ZaloPay trên app Merchant
2. Merchant gọi API tạo đơn thanh toán → ZaloPay trả về `zp_trans_token`
3. Merchant mở ZaloPay app với token
4. User thanh toán trên ZaloPay app
5. ZaloPay app mở lại Merchant app với kết quả

## 🔧 Bước 1: Thêm ZaloPay SDK

### 1.1. Thêm repository vào `build.gradle.kts` (project level)

```kotlin
allprojects {
    repositories {
        maven {
            url = uri("https://zalopay-sdk.s3.ap-southeast-1.amazonaws.com")
        }
    }
}
```

### 1.2. Thêm dependency vào `app/build.gradle.kts`

```kotlin
dependencies {
    // ZaloPay SDK
    implementation("com.zalopay.sdk:zalopaysdk:2.0.0")
}
```

## 🔑 Bước 2: Cấu hình AndroidManifest.xml

Thêm vào `<application>` tag:

```xml
<activity
    android:name="com.zalopay.sdk.ZaloPaySDK"
    android:exported="true"
    android:launchMode="singleTop">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="zalopaysdk" />
    </intent-filter>
</activity>
```

## 💻 Bước 3: Cập nhật Backend API

Backend cần implement ZaloPay Order Creation API:

### 3.1. Endpoint: `POST /api/invoices` với `paymentMethod: "zalopay"`

Backend cần:
1. Gọi ZaloPay Order Creation API
2. Trả về `zp_trans_token` trong response

**Response format:**
```json
{
  "success": true,
  "message": "Tạo đơn hàng thành công",
  "data": {
    "_id": "...",
    "zaloPayToken": "zp_trans_token_here",  // ← Thêm field này
    ...
  }
}
```

### 3.2. Cập nhật InvoiceResponse model

Thêm field `zaloPayToken` vào `InvoiceResponse.java`:

```java
private String zaloPayToken;

public String getZaloPayToken() {
    return zaloPayToken;
}

public void setZaloPayToken(String zaloPayToken) {
    this.zaloPayToken = zaloPayToken;
}
```

## 📱 Bước 4: Cập nhật ThanhToanActivity

### 4.1. Import ZaloPay SDK

```java
import com.zalopay.sdk.ZaloPaySDK;
import com.zalopay.sdk.enums.ZaloPayEnvironment;
import com.zalopay.sdk.listener.PayOrderListener;
```

### 4.2. Khởi tạo ZaloPay SDK trong `onCreate()`

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_thanh_toan);
    
    // Khởi tạo ZaloPay SDK
    ZaloPaySDK.init(2553, ZaloPayEnvironment.SANDBOX); // Thay 2553 bằng App ID của bạn
    
    // ... rest of code
}
```

### 4.3. Cập nhật method `processZaloPayPayment()`

```java
private void processZaloPayPayment(String address) {
    btnThanhToan.setEnabled(false);
    btnThanhToan.setText("Đang xử lý...");

    int userId = sessionManager.getUserId();
    if (userId == -1) {
        Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
        btnThanhToan.setEnabled(true);
        btnThanhToan.setText("Thanh toán");
        return;
    }

    // Tạo InvoiceRequest
    InvoiceRequest request = new InvoiceRequest();
    request.setCustomer(String.valueOf(userId));
    request.setPaymentMethod("zalopay");
    request.setNotes("Địa chỉ giao hàng: " + address);

    // Convert cart items
    List<InvoiceItemRequest> invoiceItems = new ArrayList<>();
    for (CartItem cartItem : cartItems) {
        InvoiceItemRequest item = new InvoiceItemRequest();
        item.setProduct(cartItem.getProduct().get_id());
        item.setQuantity(cartItem.getQuantity());
        item.setPrice(cartItem.getPrice());
        item.setDiscount(0.0);
        invoiceItems.add(item);
    }
    request.setItems(invoiceItems);

    // Gọi API tạo đơn hàng
    Call<ApiResponse<InvoiceResponse>> call = invoiceService.createInvoice(request);
    call.enqueue(new Callback<ApiResponse<InvoiceResponse>>() {
        @Override
        public void onResponse(Call<ApiResponse<InvoiceResponse>> call, Response<ApiResponse<InvoiceResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
                ApiResponse<InvoiceResponse> apiResponse = response.body();
                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    InvoiceResponse invoice = apiResponse.getData();
                    String zpTransToken = invoice.getZaloPayToken();
                    
                    if (zpTransToken != null && !zpTransToken.isEmpty()) {
                        // Mở ZaloPay app
                        ZaloPaySDK.getInstance().payOrder(ThanhToanActivity.this, zpTransToken, 
                            "demozpdk://app", new PayOrderListener() {
                            @Override
                            public void onPaymentSucceeded(String transactionId, String transToken) {
                                // Thanh toán thành công
                                cartManager.clearCart();
                                Toast.makeText(ThanhToanActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                                
                                Intent intent = new Intent(ThanhToanActivity.this, DonHangActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onPaymentCanceled(String zpTransToken, String appTransID) {
                                // User hủy thanh toán
                                Toast.makeText(ThanhToanActivity.this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                                btnThanhToan.setEnabled(true);
                                btnThanhToan.setText("Thanh toán");
                            }

                            @Override
                            public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                                // Lỗi thanh toán
                                Toast.makeText(ThanhToanActivity.this, 
                                    "Lỗi thanh toán: " + zaloPayError.toString(), 
                                    Toast.LENGTH_SHORT).show();
                                btnThanhToan.setEnabled(true);
                                btnThanhToan.setText("Thanh toán");
                            }
                        });
                    } else {
                        Toast.makeText(ThanhToanActivity.this, "Không nhận được token từ server", Toast.LENGTH_SHORT).show();
                        btnThanhToan.setEnabled(true);
                        btnThanhToan.setText("Thanh toán");
                    }
                } else {
                    String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Tạo đơn hàng thất bại";
                    Toast.makeText(ThanhToanActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    btnThanhToan.setEnabled(true);
                    btnThanhToan.setText("Thanh toán");
                }
            } else {
                Toast.makeText(ThanhToanActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<InvoiceResponse>> call, Throwable t) {
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            Toast.makeText(ThanhToanActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

## 🔗 Bước 5: Cấu hình Deep Link

Thêm vào `AndroidManifest.xml` trong `<activity>` của `ThanhToanActivity`:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="demozpdk" android:host="app" />
</intent-filter>
```

## 📝 Bước 6: Xử lý kết quả trong onNewIntent()

Thêm vào `ThanhToanActivity`:

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    ZaloPaySDK.getInstance().onResult(intent);
}
```

## ✅ Checklist

- [ ] Thêm ZaloPay SDK vào dependencies
- [ ] Cấu hình AndroidManifest.xml
- [ ] Backend implement ZaloPay Order Creation API
- [ ] Backend trả về `zaloPayToken` trong response
- [ ] Cập nhật `InvoiceResponse` model
- [ ] Khởi tạo ZaloPay SDK trong `onCreate()`
- [ ] Implement `processZaloPayPayment()` với ZaloPay SDK
- [ ] Cấu hình Deep Link
- [ ] Xử lý `onNewIntent()`

## 📚 Tài liệu tham khảo

- [ZaloPay Developer Portal](https://developers.zalopay.vn/)
- [ZaloPay Android SDK Documentation](https://developers.zalopay.vn/docs/android-sdk)

## ⚠️ Lưu ý

1. **App ID**: Thay `2553` bằng App ID thực tế từ ZaloPay
2. **Environment**: 
   - `ZaloPayEnvironment.SANDBOX` cho testing
   - `ZaloPayEnvironment.PRODUCTION` cho production
3. **Deep Link Scheme**: Thay `demozpdk` bằng scheme của app bạn
4. **Backend**: Backend phải implement ZaloPay Order Creation API và trả về token

