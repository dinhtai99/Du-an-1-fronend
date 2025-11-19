# ⚙️ Cấu hình ZaloPay SDK

## 📋 Thông tin cần thiết

### 1. APP_ID
- **Sandbox (Test)**: `2553` (mặc định)
- **Production**: Lấy từ [ZaloPay Developer Portal](https://developers.zalopay.vn/)

### 2. Environment
- **SANDBOX**: Dùng cho môi trường test
- **PRODUCTION**: Dùng cho môi trường thật

## 🔧 Cấu hình hiện tại

**File:** `app/src/main/java/fpoly/haideptrai/duan1/customer/ThanhToanActivity.java`

```java
// ZaloPay Configuration
private static final int ZALOPAY_APP_ID = 2553; // App ID sandbox

// Trong onCreate():
ZaloPaySDK.init(ZALOPAY_APP_ID, Environment.SANDBOX);
```

## 📝 Cách thay đổi

### Để test với Sandbox (hiện tại)
```java
private static final int ZALOPAY_APP_ID = 2553;
ZaloPaySDK.init(ZALOPAY_APP_ID, Environment.SANDBOX);
```

### Để deploy lên Production
1. Đăng ký tài khoản tại [ZaloPay Developer Portal](https://developers.zalopay.vn/)
2. Tạo app và lấy APP_ID
3. Cập nhật code:
```java
private static final int ZALOPAY_APP_ID = YOUR_PRODUCTION_APP_ID;
ZaloPaySDK.init(ZALOPAY_APP_ID, Environment.PRODUCTION);
```

## ⚠️ Lưu ý

1. **APP_ID phải khớp với backend**: Backend cũng cần dùng cùng APP_ID
2. **Environment phải khớp**: 
   - Nếu backend dùng SANDBOX → App cũng dùng SANDBOX
   - Nếu backend dùng PRODUCTION → App cũng dùng PRODUCTION
3. **Key1 và Key2**: Backend cần có Key1 và Key2 từ ZaloPay Developer Portal để ký và verify callback

## 🔗 Tài liệu tham khảo

- ZaloPay Developer Portal: https://developers.zalopay.vn/
- ZaloPay SDK Documentation: Xem trong file `ZALOPAY_INTEGRATION.md`

