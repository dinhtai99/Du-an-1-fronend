# 🔄 Tích hợp ZaloPay Frontend - Kiểm tra và Cập nhật

## ✅ Đã kiểm tra và cập nhật

### 1. **PaymentService** (`api/services/PaymentService.java`)

✅ **Endpoint đã đúng:**
- `POST /api/payment/zalopay/create` - Tạo đơn hàng thanh toán
- `GET /api/payment/zalopay/status/{orderId}` - Kiểm tra trạng thái thanh toán

### 2. **ZaloPayCreateRequest** (`api/models/ZaloPayCreateRequest.java`)

✅ **Request format khớp với backend:**
```java
{
  "shippingAddress": {
    "fullName": "...",
    "phone": "...",
    "address": "...",
    "ward": "...",
    "district": "...",
    "city": "..."
  },
  "items": [
    {
      "product": "...",
      "quantity": 1,
      "price": 100000,
      "color": "",
      "size": ""
    }
  ],
  "notes": "...",
  "voucherCode": "..." // Optional
}
```

### 3. **ZaloPayCreateResponse** (`api/models/ZaloPayCreateResponse.java`)

✅ **Response format khớp với backend:**
```java
{
  "success": true,
  "message": "Tạo đơn hàng thanh toán ZaloPay thành công!",
  "zp_trans_token": "...",
  "order_url": "...",
  "order_token": "...",
  "orderId": "...",
  "orderNumber": "...",
  "error": null // Nếu có lỗi
}
```

### 4. **ThanhToanActivity** - Flow thanh toán

✅ **Flow đã được implement đúng:**

1. **User chọn ZaloPay** → `processZaloPayPayment()`
2. **Lấy thông tin user** → `getUserInfo()`
3. **Tạo request** → `createZaloPayOrder()`
4. **Gọi API** → `POST /api/payment/zalopay/create`
5. **Nhận zp_trans_token** → `ZaloPayCreateResponse`
6. **Mở ZaloPay app** → `ZaloPaySDK.payOrder()`
7. **Xử lý callback** → `PayOrderListener`

### 5. **Cải thiện mới**

✅ **Thêm method check payment status:**
- `checkZaloPayPaymentStatus(orderId)` - Kiểm tra trạng thái từ server
- Được gọi khi quay lại app từ ZaloPay (deep link)
- Backup nếu PayOrderListener không được gọi

✅ **Cải thiện deep link handling:**
- `handleZaloPayDeepLink()` - Xử lý deep link từ ZaloPay
- Tự động check payment status nếu có `orderId` trong deep link
- Gọi trong `onResume()` để đảm bảo check khi quay lại app

---

## 🔍 So sánh Frontend vs Backend

### Request Format

| Field | Frontend | Backend | Status |
|-------|----------|---------|--------|
| `shippingAddress` | ✅ | ✅ | ✅ Khớp |
| `items` | ✅ | ✅ | ✅ Khớp |
| `notes` | ✅ | ✅ | ✅ Khớp |
| `voucherCode` | ✅ | ✅ | ✅ Khớp |

### Response Format

| Field | Frontend | Backend | Status |
|-------|----------|---------|--------|
| `success` | ✅ | ✅ | ✅ Khớp |
| `message` | ✅ | ✅ | ✅ Khớp |
| `zp_trans_token` | ✅ | ✅ | ✅ Khớp |
| `order_url` | ✅ | ✅ | ✅ Khớp |
| `order_token` | ✅ | ✅ | ✅ Khớp |
| `orderId` | ✅ | ✅ | ✅ Khớp |
| `orderNumber` | ✅ | ✅ | ✅ Khớp |

---

## 🧪 Test Flow

### Test 1: Tạo đơn hàng ZaloPay

1. Mở app → Đăng nhập
2. Thêm sản phẩm vào giỏ hàng
3. Vào "Giỏ hàng" → Chọn sản phẩm → "Thanh toán"
4. Chọn "ZaloPay" làm phương thức thanh toán
5. Nhập địa chỉ giao hàng
6. Click "Thanh toán"

**Expected:**
- ✅ Gọi API `POST /api/payment/zalopay/create`
- ✅ Nhận `zp_trans_token` từ backend
- ✅ Mở ZaloPay app với `zp_trans_token`
- ✅ Hiển thị màn hình thanh toán trong ZaloPay app

**Check Logcat:**
```
D/ZaloPay: === ZALOPAY CREATE REQUEST ===
D/ZaloPay: Request JSON: {...}
D/ZaloPay: Received zp_trans_token: xxx...
D/ZaloPay: Order ID: xxx...
D/ZaloPay: Order Number: ORD-xxx...
```

### Test 2: Thanh toán thành công

1. Trong ZaloPay app → Thanh toán thành công
2. ZaloPay app tự động quay lại app

**Expected:**
- ✅ `PayOrderListener.onPaymentSucceeded()` được gọi
- ✅ Xóa giỏ hàng
- ✅ Hiển thị toast "Thanh toán thành công!"
- ✅ Chuyển đến màn hình "Đơn hàng"

**Check Logcat:**
```
D/ZaloPay: Payment succeeded - Transaction ID: xxx, App Trans ID: xxx
```

### Test 3: Hủy thanh toán

1. Trong ZaloPay app → Hủy thanh toán
2. ZaloPay app tự động quay lại app

**Expected:**
- ✅ `PayOrderListener.onPaymentCanceled()` được gọi
- ✅ Hiển thị toast "Đã hủy thanh toán"
- ✅ Button "Thanh toán" được enable lại

**Check Logcat:**
```
D/ZaloPay: Payment canceled - App Trans ID: xxx
```

### Test 4: Lỗi thanh toán

1. Trong ZaloPay app → Xảy ra lỗi (ví dụ: không đủ tiền)
2. ZaloPay app tự động quay lại app

**Expected:**
- ✅ `PayOrderListener.onPaymentError()` được gọi
- ✅ Hiển thị toast với thông báo lỗi
- ✅ Button "Thanh toán" được enable lại

**Check Logcat:**
```
E/ZaloPay: Payment error: xxx, App Trans ID: xxx
```

### Test 5: Check Payment Status (Deep Link)

1. Thanh toán trong ZaloPay app
2. Quay lại app qua deep link `demozpdk://app?orderId=xxx`

**Expected:**
- ✅ `handleZaloPayDeepLink()` được gọi
- ✅ `checkZaloPayPaymentStatus(orderId)` được gọi
- ✅ Gọi API `GET /api/payment/zalopay/status/{orderId}`
- ✅ Log payment status từ server

**Check Logcat:**
```
D/ZaloPay: Received deep link from ZaloPay: demozpdk://app?orderId=xxx
D/ZaloPay: Checking payment status for order: xxx
D/ZaloPay: Payment status response: {...}
```

---

## 🔧 Cấu hình

### 1. **ZaloPay App ID**

**File:** `MyApplication.java` và `ThanhToanActivity.java`

```java
private static final int ZALOPAY_APP_ID = 2554; // Sandbox
```

**Lưu ý:**
- ✅ Phải khớp với `ZALOPAY_APP_ID` trong backend `.env`
- ✅ Sandbox: `2554`
- ✅ Production: Lấy từ ZaloPay Developer Portal

### 2. **Deep Link Scheme**

**File:** `AndroidManifest.xml`

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="demozpdk" android:host="app" />
</intent-filter>
```

**Lưu ý:**
- ✅ Scheme `demozpdk` phải khớp với scheme trong `ZaloPaySDK.payOrder()`
- ✅ Hiện tại: `"demozpdk://app"`

### 3. **ZaloPay SDK**

**File:** `MyApplication.java`

```java
ZaloPaySDK.init(ZALOPAY_APP_ID, Environment.SANDBOX);
```

**Lưu ý:**
- ✅ SDK đã được khởi tạo trong `MyApplication.onCreate()`
- ✅ Không cần khởi tạo lại trong `ThanhToanActivity`

---

## 🐛 Troubleshooting

### Lỗi 1: "Endpoint không tồn tại"

**Nguyên nhân:** Backend route chưa được đăng ký

**Giải pháp:**
1. Kiểm tra `server.js` có:
   ```javascript
   app.use('/api/payment', paymentRoutes);
   ```
2. Kiểm tra `routes/payment.js` có route:
   ```javascript
   router.post('/zalopay/create', ...);
   ```
3. Restart backend server

**Check Logcat:**
```
E/ZaloPay: Error response code: 404
E/ZaloPay: Error response body: Cannot POST /api/payment/zalopay/create
```

### Lỗi 2: "Lỗi cấu hình ZaloPay"

**Nguyên nhân:** Backend thiếu biến môi trường hoặc sai config

**Giải pháp:**
1. Kiểm tra backend `.env` có:
   ```env
   ZALOPAY_APP_ID=2554
   ZALOPAY_KEY1=xxx
   ZALOPAY_KEY2=xxx
   ```
2. Kiểm tra backend logs để xem chi tiết lỗi
3. Đảm bảo `ZALOPAY_APP_ID` khớp giữa frontend và backend

**Check Logcat:**
```
E/ZaloPay: === ZALOPAY CREATE ERROR ===
E/ZaloPay: Success: false
E/ZaloPay: Error: Giao dịch thất bại
```

### Lỗi 3: "Không thể mở ZaloPay app"

**Nguyên nhân:** ZaloPay app chưa được cài đặt hoặc SDK lỗi

**Giải pháp:**
1. Cài đặt ZaloPay app trên thiết bị
2. Kiểm tra ZaloPay SDK đã được thêm vào `libs/` chưa
3. Kiểm tra `build.gradle` có dependency:
   ```gradle
   implementation files('libs/zpdk-release-v3.1.aar')
   ```

**Check Logcat:**
```
E/ZaloPay: Error opening ZaloPay app: xxx
```

### Lỗi 4: "Payment callback không được gọi"

**Nguyên nhân:** Deep link không hoạt động hoặc PayOrderListener không được đăng ký

**Giải pháp:**
1. Kiểm tra `AndroidManifest.xml` có intent-filter cho deep link
2. Kiểm tra `ZaloPaySDK.payOrder()` có đúng `PayOrderListener`
3. Kiểm tra deep link scheme khớp với `demozpdk://app`

**Check Logcat:**
```
D/ZaloPay: Received deep link from ZaloPay: demozpdk://app
```

---

## ✅ Checklist

### Code Frontend

- [x] PaymentService có endpoint `/api/payment/zalopay/create`
- [x] PaymentService có endpoint `/api/payment/zalopay/status/{orderId}`
- [x] ZaloPayCreateRequest khớp với backend request format
- [x] ZaloPayCreateResponse khớp với backend response format
- [x] ThanhToanActivity gọi đúng API endpoint
- [x] ThanhToanActivity xử lý response đúng
- [x] ThanhToanActivity mở ZaloPay app với `zp_trans_token`
- [x] ThanhToanActivity xử lý PayOrderListener callbacks
- [x] ThanhToanActivity xử lý deep link
- [x] ThanhToanActivity có method check payment status

### Configuration

- [x] ZALOPAY_APP_ID = 2554 (sandbox) - khớp với backend
- [x] Deep link scheme = `demozpdk://app`
- [x] AndroidManifest có intent-filter cho deep link
- [x] ZaloPay SDK đã được khởi tạo trong MyApplication

### Testing

- [ ] Test tạo đơn hàng thành công
- [ ] Test thanh toán thành công
- [ ] Test hủy thanh toán
- [ ] Test lỗi thanh toán
- [ ] Test check payment status
- [ ] Test deep link handling

---

## 📝 Notes

1. **Optimistic Update:**
   - Frontend không cần optimistic update cho ZaloPay vì phải chờ user thanh toán trong ZaloPay app
   - Chỉ xóa giỏ hàng sau khi thanh toán thành công

2. **Error Handling:**
   - Frontend đã có error handling đầy đủ
   - Hiển thị toast với thông báo lỗi rõ ràng
   - Log chi tiết để debug

3. **Payment Status Check:**
   - Method `checkZaloPayPaymentStatus()` là backup
   - Thường thì `PayOrderListener` đã xử lý rồi
   - Chỉ cần check khi deep link có `orderId` parameter

4. **Deep Link:**
   - Deep link `demozpdk://app` được ZaloPay SDK tự động gọi
   - Frontend chỉ cần handle để check payment status nếu cần
   - Không cần xử lý phức tạp vì PayOrderListener đã xử lý

---

## 🎯 Kết luận

**Frontend đã được cập nhật và khớp với backend!**

✅ **Request/Response format:** Khớp 100%
✅ **API endpoints:** Đúng và đầy đủ
✅ **Error handling:** Đầy đủ và rõ ràng
✅ **Payment flow:** Hoàn chỉnh
✅ **Deep link handling:** Đã được cải thiện
✅ **Payment status check:** Đã được thêm

**Chỉ cần:**
1. ✅ Đảm bảo backend route đã được đăng ký
2. ✅ Đảm bảo backend `.env` đã được cấu hình
3. ✅ Test flow thanh toán trên thiết bị thật

Frontend sẵn sàng để test với backend!

