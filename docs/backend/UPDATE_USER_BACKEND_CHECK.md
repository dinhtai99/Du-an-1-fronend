# 🔧 Kiểm tra Backend - Update User API

## ⚠️ QUAN TRỌNG: Đảm bảo Route đã được đăng ký

### Bước 1: Kiểm tra file `server.js` hoặc `app.js`

**Tìm và thêm dòng này (nếu chưa có):**

```javascript
// Import routes
const userRoutes = require('./routes/users');

// ... các middleware khác ...

// Mount routes
app.use('/api/users', userRoutes);
```

**Vị trí đúng:** Sau khi đã có các middleware như `express.json()`, `cors()`, nhưng trước error handler.

**Ví dụ đầy đủ:**
```javascript
const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
const userRoutes = require('./routes/users'); // ← Thêm dòng này

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/users', userRoutes); // ← Thêm dòng này

// Error handler
app.use((err, req, res, next) => {
  // ...
});

app.listen(3000, () => {
  console.log("Server running on port 3000");
});
```

### Bước 2: Kiểm tra file `routes/users.js`

Đảm bảo file tồn tại và có route:
- ✅ `PUT /api/users/:id` - Cập nhật thông tin user

**Ví dụ route:**
```javascript
router.put("/:id", verifyToken, async (req, res) => {
  try {
    const { id } = req.params;
    const { fullName, phone, username } = req.body;
    
    // Validate
    if (!fullName || !username) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập đầy đủ thông tin!"
      });
    }
    
    // Update user
    const user = await User.findByIdAndUpdate(
      id,
      { 
        fullName, 
        phone, 
        username 
      },
      { new: true }
    );
    
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy người dùng!"
      });
    }
    
    res.json({
      success: true,
      message: "Cập nhật thông tin thành công!",
      data: user
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: "Lỗi server!",
      error: error.message
    });
  }
});
```

### Bước 3: Kiểm tra Model `models/User.js`

Đảm bảo model có các field:
- `fullName` (hoặc `hoTen`)
- `phone` (hoặc `soDienThoai`)
- `username` (hoặc `tenDangNhap`)

### Bước 4: Restart Server

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

### Bước 5: Test Backend

**Test với Postman hoặc curl:**

```bash
# 1. Đăng nhập để lấy token
POST http://localhost:3000/api/auth/login
Body: { "email": "...", "password": "..." }

# 2. Cập nhật thông tin user
PUT http://localhost:3000/api/users/USER_ID
Headers: 
  Authorization: Bearer YOUR_TOKEN
  Content-Type: application/json
Body: { 
  "fullName": "Nguyễn Văn A",
  "phone": "0123456789",
  "username": "newemail@example.com"
}

# Expected Response:
{
  "success": true,
  "message": "Cập nhật thông tin thành công!",
  "data": {
    "_id": "...",
    "fullName": "Nguyễn Văn A",
    "phone": "0123456789",
    "username": "newemail@example.com",
    ...
  }
}
```

### Bước 6: Kiểm tra Logs

Backend code nên có logging, xem console:
```
PUT /api/users/:id - Request received
Updating user: ...
✅ User updated successfully
```

Nếu không thấy logs này → Route chưa được đăng ký!

## 🔍 Troubleshooting

### Lỗi 404: Cannot PUT /api/users/:id

**Nguyên nhân:** Route chưa được đăng ký trong `server.js`

**Giải pháp:**
1. Mở file `server.js`
2. Tìm dòng `app.use('/api/...`
3. Thêm: `app.use('/api/users', userRoutes);`
4. Restart server

### Lỗi 401: Unauthorized

**Nguyên nhân:** JWT token không hợp lệ hoặc thiếu

**Giải pháp:**
- Kiểm tra token trong request header
- Đảm bảo đã đăng nhập và token còn hiệu lực
- Kiểm tra middleware `verifyToken` đã được thêm vào route chưa

### Lỗi 400: Bad Request

**Nguyên nhân:** Request body không đúng format

**Giải pháp:**
- Frontend đã gửi đúng: `{ "fullName": "...", "phone": "...", "username": "..." }`
- Kiểm tra backend có validate đúng không
- Kiểm tra field names có khớp với model không

### Lỗi 500: Internal Server Error

**Nguyên nhân:** Lỗi database hoặc code

**Giải pháp:**
- Xem logs backend để biết chi tiết
- Kiểm tra database connection
- Kiểm tra model User đã được tạo chưa
- Kiểm tra field names trong model có khớp với request body không

### Lỗi: "Không tìm thấy người dùng"

**Nguyên nhân:** User ID không đúng hoặc user không tồn tại

**Giải pháp:**
- Kiểm tra user ID trong request
- Đảm bảo user đã tồn tại trong database
- Kiểm tra xem có dùng đúng MongoDB ObjectId không

## ✅ Checklist

- [ ] File `routes/users.js` tồn tại
- [ ] File `models/User.js` tồn tại
- [ ] Trong `server.js` có: `const userRoutes = require('./routes/users');`
- [ ] Trong `server.js` có: `app.use('/api/users', userRoutes);`
- [ ] Route `PUT /api/users/:id` có middleware `verifyToken`
- [ ] Route xử lý đúng request body: `{ fullName, phone, username }`
- [ ] Route trả về đúng format: `{ success, message, data }`
- [ ] Đã restart server sau khi thêm route
- [ ] Test với Postman thành công
- [ ] Backend logs hiển thị request khi cập nhật user

## 📝 Quick Fix Script

Nếu chưa có route, thêm vào `server.js`:

```javascript
// Tìm dòng này (thường ở đầu file):
const express = require("express");

// Thêm sau các require khác:
const userRoutes = require('./routes/users');

// Tìm dòng này (sau middleware):
app.use(express.json());

// Thêm sau các route khác (trước error handler):
app.use('/api/users', userRoutes);
```

Sau đó restart server!

## 🔄 Frontend đã được cập nhật

Frontend đã được cải thiện với:
- ✅ Validation tốt hơn (email format, phone length)
- ✅ Error handling chi tiết với logging
- ✅ Cập nhật session sau khi update thành công
- ✅ Refresh màn hình thông tin cá nhân sau khi sửa

## 📱 Test trên App

1. Mở app → Vào "Thông tin cá nhân"
2. Click "Sửa thông tin"
3. Sửa thông tin và click "Lưu thông tin"
4. Xem Logcat trong Android Studio:
   - Filter: "UpdateUser"
   - Tìm "Updating user ID", "Response code", "User updated successfully"
5. Kiểm tra thông tin đã được cập nhật chưa

