# 📱 Tích hợp MoMo Payment SDK

## ✅ Đã hoàn thành

### 1. Thêm MoMo SDK Dependency
**File:** `app/build.gradle.kts`
```kotlin
// MoMo Payment SDK
implementation("com.github.momo-wallet:payment-sdk:2.0.2")
```

### 2. Thêm UI cho MoMo
**File:** `app/src/main/res/layout/activity_thanh_toan.xml`
- Đã thêm `layoutMoMo` vào danh sách phương thức thanh toán

### 3. Khởi tạo MoMo SDK
**File:** `app/src/main/java/fpoly/haideptrai/duan1/customer/ThanhToanActivity.java`
```java
// Initialize MoMo Payment SDK
MomoPayment.getInstance().setEnvironment(ENVIRONMENT.DEVELOPMENT);
```

### 4. Thêm logic xử lý
- Đã thêm `processMoMoPayment()` method
- Đã thêm click listener cho MoMo option
- Đã thêm vào `selectPaymentMethod()`

## ⚠️ Cần hoàn thiện

### 1. Backend API cho MoMo
Cần tạo endpoint trong backend:
```
POST /api/payment/momo/create
```

**Request Body:**
```json
{
  "shippingAddress": {
    "fullName": "Nguyễn Văn A",
    "phone": "0987654321",
    "address": "123 Đường ABC",
    "ward": "Phường XYZ",
    "district": "Quận 1",
    "city": "Hồ Chí Minh"
  },
  "items": [
    {
      "product": "product_id",
      "quantity": 2,
      "price": 8990000
    }
  ],
  "notes": "Địa chỉ giao hàng: ..."
}
```

**Response:**
```json
{
  "success": true,
  "paymentUrl": "momo://payment?...",
  "requestId": "momo_request_id",
  "orderId": "order_id",
  "orderNumber": "DH123"
}
```

### 2. Implement MoMo Payment Flow

Cần cập nhật `processMoMoPayment()` để:

1. **Gọi backend API** để tạo payment request
2. **Nhận payment URL** từ backend
3. **Mở MoMo app** với payment URL:
   ```java
   // Ví dụ (cần kiểm tra API chính xác của MoMo SDK)
   Intent intent = new Intent(Intent.ACTION_VIEW);
   intent.setData(Uri.parse(paymentUrl));
   startActivity(intent);
   ```

4. **Xử lý callback** khi MoMo app mở lại app:
   - Thêm intent-filter trong AndroidManifest
   - Xử lý trong `onNewIntent()` hoặc `onResume()`

### 3. Cấu hình MoMo

Cần có thông tin từ MoMo Developer Portal:
- **PartnerCode**: Mã đối tác
- **AccessKey**: Key truy cập
- **SecretKey**: Key bí mật (dùng ở backend)

Cập nhật code:
```java
// Trong onCreate() hoặc MyApplication
MomoPayment.getInstance().setEnvironment(ENVIRONMENT.DEVELOPMENT);
// TODO: Thêm cấu hình PartnerCode, AccessKey nếu SDK yêu cầu
```

### 4. Deep Link Configuration

Thêm intent-filter trong `AndroidManifest.xml`:
```xml
<activity
    android:name=".customer.ThanhToanActivity"
    android:exported="false">
    <!-- ZaloPay Deep Link -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="demozpdk" android:host="app" />
    </intent-filter>
    
    <!-- MoMo Deep Link -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="momo" android:host="payment" />
    </intent-filter>
</activity>
```

## 📚 Tài liệu tham khảo

- MoMo Payment Platform: https://developers.momo.vn/
- MoMo SDK Documentation: Xem trong tài liệu chính thức của MoMo

## 🔄 Flow hoàn chỉnh (khi đã implement)

1. User chọn MoMo → Click "Thanh toán"
2. App gọi `POST /api/payment/momo/create` → Backend
3. Backend tạo payment request với MoMo API
4. Backend trả về `paymentUrl`
5. App mở MoMo app với `paymentUrl`
6. User thanh toán trong MoMo app
7. MoMo app mở lại app qua deep link
8. App xử lý kết quả và cập nhật order status

## 📝 Checklist

- [x] Thêm MoMo SDK dependency
- [x] Thêm UI cho MoMo
- [x] Khởi tạo MoMo SDK
- [x] Thêm click listener
- [ ] Tạo backend API `/api/payment/momo/create`
- [ ] Implement `processMoMoPayment()` đầy đủ
- [ ] Cấu hình deep link cho MoMo
- [ ] Test flow thanh toán

