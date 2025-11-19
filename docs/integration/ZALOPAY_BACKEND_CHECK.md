# Kiểm tra Backend ZaloPay Integration

## ❌ Lỗi: "Cannot POST /api/payment/zalopay/create"

Lỗi này cho thấy endpoint `/api/payment/zalopay/create` không được tìm thấy trên backend.

## ✅ Các bước kiểm tra:

### 1. Kiểm tra Server có đang chạy không?

```bash
cd /Users/trantai/Desktop/Shop_THB
node server.js
```

**Kết quả mong đợi:**
```
✅ Connected to MongoDB Atlas
🚀 Server running on port 3000
```

### 2. Kiểm tra Route có được đăng ký không?

**File:** `/Users/trantai/Desktop/Shop_THB/server.js`

**Phải có:**
```javascript
const paymentRoutes = require("./routes/payment");
app.use("/api/payment", paymentRoutes);
```

### 3. Kiểm tra Route có tồn tại không?

**File:** `/Users/trantai/Desktop/Shop_THB/routes/payment.js`

**Phải có:**
```javascript
router.post("/zalopay/create", verifyToken, async (req, res) => {
  // ... code
});
```

### 4. Test endpoint bằng Postman/Thunder Client

**Request:**
- Method: `POST`
- URL: `http://localhost:3000/api/payment/zalopay/create`
- Headers:
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`
- Body:
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
  "notes": "Giao hàng giờ hành chính"
}
```

**Nếu Postman/Thunder Client cũng báo lỗi "Cannot POST":**
- Route chưa được load
- Server cần restart
- Có lỗi syntax trong `routes/payment.js`

**Nếu Postman/Thunder Client hoạt động nhưng Android không:**
- Kiểm tra BASE_URL trong Android
- Kiểm tra Authorization header

### 5. Kiểm tra Logs Server

Khi gọi API từ Android, kiểm tra console của server xem có log gì không:

```javascript
// Thêm vào routes/payment.js để debug
router.post("/zalopay/create", verifyToken, async (req, res) => {
  console.log('=== ZALOPAY CREATE REQUEST ===');
  console.log('Body:', req.body);
  console.log('Headers:', req.headers);
  // ... rest of code
});
```

### 6. Restart Server

Sau khi kiểm tra, restart server:

```bash
# Dừng server (Ctrl+C)
# Sau đó chạy lại:
node server.js
```

## 🔧 Giải pháp tạm thời

Nếu endpoint `/api/payment/zalopay/create` không hoạt động, có thể:

1. **Sử dụng endpoint `/api/orders` với `paymentMethod: "zalopay"`**
   - Backend sẽ trả về message redirect đến payment endpoint
   - Hoặc có thể backend đã xử lý ZaloPay trong `/api/orders`

2. **Kiểm tra xem backend có endpoint khác không:**
   - `/api/orders` với paymentMethod="zalopay"
   - `/api/invoices` với paymentMethod="zalopay"

## 📝 Checklist

- [ ] Server đang chạy trên port 3000
- [ ] File `routes/payment.js` tồn tại
- [ ] Route `/zalopay/create` được định nghĩa
- [ ] `module.exports = router` ở cuối file
- [ ] `app.use("/api/payment", paymentRoutes)` trong server.js
- [ ] Server đã được restart sau khi thêm route
- [ ] Test bằng Postman/Thunder Client thành công

