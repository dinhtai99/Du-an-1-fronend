# 🔧 Hướng dẫn Setup Backend Chat API

## ⚠️ QUAN TRỌNG: Đảm bảo Route đã được đăng ký

### Bước 1: Kiểm tra file `server.js` hoặc `app.js`

**Tìm và thêm dòng này (nếu chưa có):**

```javascript
// Import routes
const chatRoutes = require('./routes/chat');

// ... các middleware khác ...

// Mount routes
app.use('/api/chat', chatRoutes);
```

**Vị trí đúng:** Sau khi đã có các middleware như `express.json()`, `cors()`, nhưng trước error handler.

**Ví dụ đầy đủ:**
```javascript
const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
const chatRoutes = require('./routes/chat'); // ← Thêm dòng này

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/chat', chatRoutes); // ← Thêm dòng này

// Error handler
app.use((err, req, res, next) => {
  // ...
});

app.listen(3000, () => {
  console.log("Server running on port 3000");
});
```

### Bước 2: Kiểm tra file `routes/chat.js`

Đảm bảo file tồn tại và có các routes:
- ✅ `POST /api/chat/messages` - Gửi tin nhắn
- ✅ `GET /api/chat/messages` - Lấy danh sách tin nhắn
- ✅ `GET /api/chat/unread-count` - Lấy số tin nhắn chưa đọc

### Bước 3: Kiểm tra Model `models/Chat.js`

Đảm bảo model đã được tạo với structure đúng (như code bạn đã cung cấp).

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

# 2. Gửi tin nhắn
POST http://localhost:3000/api/chat/messages
Headers: 
  Authorization: Bearer YOUR_TOKEN
  Content-Type: application/json
Body: { "message": "Test message" }

# Expected Response:
{
  "success": true,
  "message": "Gửi tin nhắn thành công!",
  "chat": {
    "_id": "...",
    "customer": "...",
    "messages": [...]
  }
}

# 3. Lấy tin nhắn
GET http://localhost:3000/api/chat/messages?limit=50
Headers: Authorization: Bearer YOUR_TOKEN

# Expected Response:
{
  "success": true,
  "messages": [...],
  "unreadCount": 0
}
```

### Bước 6: Kiểm tra Logs

Backend code đã có logging, xem console:
```
📨 POST /api/chat/messages - Request received
Request body: { message: '...' }
User: { userId: '...', role: 'customer' }
✅ Message sent successfully
```

Nếu không thấy logs này → Route chưa được đăng ký!

## 🔍 Troubleshooting

### Lỗi 404: Cannot POST /api/chat/messages

**Nguyên nhân:** Route chưa được đăng ký trong `server.js`

**Giải pháp:**
1. Mở file `server.js`
2. Tìm dòng `app.use('/api/...`
3. Thêm: `app.use('/api/chat', chatRoutes);`
4. Restart server

### Lỗi 401: Unauthorized

**Nguyên nhân:** JWT token không hợp lệ hoặc thiếu

**Giải pháp:**
- Kiểm tra token trong request header
- Đảm bảo đã đăng nhập và token còn hiệu lực

### Lỗi 400: Bad Request

**Nguyên nhân:** Request body không đúng format

**Giải pháp:**
- Frontend đã gửi đúng: `{ "message": "..." }`
- Kiểm tra backend có validate đúng không

### Lỗi 500: Internal Server Error

**Nguyên nhân:** Lỗi database hoặc code

**Giải pháp:**
- Xem logs backend để biết chi tiết
- Kiểm tra database connection
- Kiểm tra model Chat đã được tạo chưa

## ✅ Checklist

- [ ] File `routes/chat.js` tồn tại
- [ ] File `models/Chat.js` tồn tại
- [ ] Trong `server.js` có: `const chatRoutes = require('./routes/chat');`
- [ ] Trong `server.js` có: `app.use('/api/chat', chatRoutes);`
- [ ] Đã restart server sau khi thêm route
- [ ] Test với Postman thành công
- [ ] Backend logs hiển thị request khi gửi tin nhắn

## 📝 Quick Fix Script

Nếu chưa có route, thêm vào `server.js`:

```javascript
// Tìm dòng này (thường ở đầu file):
const express = require("express");

// Thêm sau các require khác:
const chatRoutes = require('./routes/chat');

// Tìm dòng này (sau middleware):
app.use(express.json());

// Thêm sau các route khác (trước error handler):
app.use('/api/chat', chatRoutes);
```

Sau đó restart server!

