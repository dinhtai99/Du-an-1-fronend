# 🔧 Kiểm tra Backend - Favorites API

## ⚠️ QUAN TRỌNG: Đảm bảo Route đã được đăng ký

### Bước 1: Kiểm tra file `server.js` hoặc `app.js`

**Tìm và thêm dòng này (nếu chưa có):**

```javascript
// Import routes
const favoritesRoutes = require('./routes/favorites');

// ... các middleware khác ...

// Mount routes
app.use('/api/favorites', favoritesRoutes);
```

**Vị trí đúng:** Sau khi đã có các middleware như `express.json()`, `cors()`, nhưng trước error handler.

**Ví dụ đầy đủ:**
```javascript
const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
const favoritesRoutes = require('./routes/favorites'); // ← Thêm dòng này

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/favorites', favoritesRoutes); // ← Thêm dòng này

// Error handler
app.use((err, req, res, next) => {
  // ...
});

app.listen(3000, () => {
  console.log("Server running on port 3000");
});
```

### Bước 2: Kiểm tra file `routes/favorites.js`

Đảm bảo file tồn tại và có các routes:
- ✅ `GET /api/favorites` - Lấy danh sách sản phẩm yêu thích
- ✅ `GET /api/favorites/check/:productId` - Kiểm tra sản phẩm đã yêu thích chưa
- ✅ `POST /api/favorites/:productId` - Thêm vào yêu thích
- ✅ `DELETE /api/favorites/:productId` - Xóa khỏi yêu thích

### Bước 3: Kiểm tra Model `models/Favorite.js`

Đảm bảo model đã được tạo với structure:
```javascript
{
  user: ObjectId (ref: User),
  product: ObjectId (ref: Product),
  createdAt: Date
}
```

### Bước 4: Kiểm tra Middleware

Đảm bảo route có middleware:
- `verifyToken` - Xác thực JWT token
- `requireCustomer` - Chỉ cho phép customer (không phải admin)

### Bước 5: Restart Server

Sau khi thêm route, **BẮT BUỘC** phải restart server:

```bash
# Dừng server hiện tại (Ctrl+C)
# Sau đó chạy lại:
node server.js
# hoặc
npm start
# hoặc nếu dùng nodemon (tự động restart)
nodemon server.js
```

### Bước 6: Test Backend

**Test với Postman hoặc curl:**

```bash
# 1. Đăng nhập để lấy token
POST http://localhost:3000/api/auth/login
Body: { "email": "...", "password": "..." }

# 2. Lấy danh sách yêu thích
GET http://localhost:3000/api/favorites
Headers: Authorization: Bearer YOUR_TOKEN

# Expected Response:
[
  {
    "_id": "...",
    "name": "...",
    "price": 100000,
    ...
  },
  ...
]

# 3. Kiểm tra sản phẩm đã yêu thích chưa
GET http://localhost:3000/api/favorites/check/PRODUCT_ID
Headers: Authorization: Bearer YOUR_TOKEN

# Expected Response:
{
  "isFavorite": true
}

# 4. Thêm vào yêu thích
POST http://localhost:3000/api/favorites/PRODUCT_ID
Headers: Authorization: Bearer YOUR_TOKEN

# Expected Response:
{
  "message": "Đã thêm vào yêu thích!",
  "favorite": { ... }
}

# 5. Xóa khỏi yêu thích
DELETE http://localhost:3000/api/favorites/PRODUCT_ID
Headers: Authorization: Bearer YOUR_TOKEN

# Expected Response:
{
  "message": "Đã xóa khỏi yêu thích!"
}
```

### Bước 7: Kiểm tra Logs

Backend code đã có logging, xem console:
```
GET /api/favorites - Request received
POST /api/favorites/:productId - Request received
✅ Added favorite to server
```

Nếu không thấy logs này → Route chưa được đăng ký!

## 🔍 Troubleshooting

### Lỗi 404: Cannot GET /api/favorites

**Nguyên nhân:** Route chưa được đăng ký trong `server.js`

**Giải pháp:**
1. Mở file `server.js`
2. Tìm dòng `app.use('/api/...`
3. Thêm: `app.use('/api/favorites', favoritesRoutes);`
4. Restart server

### Lỗi 401: Unauthorized

**Nguyên nhân:** JWT token không hợp lệ hoặc thiếu

**Giải pháp:**
- Kiểm tra token trong request header
- Đảm bảo đã đăng nhập và token còn hiệu lực
- Kiểm tra middleware `verifyToken` đã được thêm vào route chưa

### Lỗi 403: Forbidden

**Nguyên nhân:** User không phải customer (có thể là admin)

**Giải pháp:**
- Kiểm tra middleware `requireCustomer` đã được thêm vào route chưa
- Đảm bảo user đang đăng nhập với role "customer"

### Lỗi 400: Bad Request

**Nguyên nhân:** Sản phẩm đã có trong danh sách yêu thích

**Giải pháp:**
- Frontend đã xử lý optimistic update, nên lỗi này sẽ không ảnh hưởng UX
- Backend trả về lỗi nhưng frontend vẫn giữ local change

### Lỗi 500: Internal Server Error

**Nguyên nhân:** Lỗi database hoặc code

**Giải pháp:**
- Xem logs backend để biết chi tiết
- Kiểm tra database connection
- Kiểm tra model Favorite đã được tạo chưa

## ✅ Checklist

- [ ] File `routes/favorites.js` tồn tại
- [ ] File `models/Favorite.js` tồn tại
- [ ] Trong `server.js` có: `const favoritesRoutes = require('./routes/favorites');`
- [ ] Trong `server.js` có: `app.use('/api/favorites', favoritesRoutes);`
- [ ] Route có middleware `verifyToken` và `requireCustomer`
- [ ] Đã restart server sau khi thêm route
- [ ] Test với Postman thành công
- [ ] Backend logs hiển thị request khi thao tác yêu thích

## 📝 Quick Fix Script

Nếu chưa có route, thêm vào `server.js`:

```javascript
// Tìm dòng này (thường ở đầu file):
const express = require("express");

// Thêm sau các require khác:
const favoritesRoutes = require('./routes/favorites');

// Tìm dòng này (sau middleware):
app.use(express.json());

// Thêm sau các route khác (trước error handler):
app.use('/api/favorites', favoritesRoutes);
```

Sau đó restart server!

## 🔄 Frontend đã được cập nhật

Frontend đã được tích hợp với API:
- ✅ **FavoriteService** - Interface để gọi API
- ✅ **FavoriteManager** - Quản lý yêu thích với optimistic update
- ✅ **ProductHomeAdapter** - Tích hợp API khi toggle favorite
- ✅ **SanPhamYeuThichActivity** - Load từ API thay vì local

### Tính năng Optimistic Update

Frontend sử dụng **optimistic update** để UX tốt hơn:
1. **Cập nhật UI ngay lập tức** khi user click
2. **Gọi API ở background**
3. **Nếu API thành công** → Giữ nguyên UI
4. **Nếu API thất bại** → Revert UI về trạng thái cũ

### Offline Support

Frontend vẫn lưu local cache để:
- Hoạt động khi offline
- Tải nhanh khi mở app
- Sync với server khi có internet

## 📱 Test trên App

1. Mở app → Đăng nhập
2. Vào màn hình Home
3. Click trái tim trên sản phẩm
4. Xem Logcat trong Android Studio:
   - Filter: "FavoriteManager"
   - Tìm "Added favorite locally", "Added favorite to server"
5. Vào "Thông tin cá nhân" → "Sản phẩm yêu thích"
6. Kiểm tra danh sách đã được load từ API

