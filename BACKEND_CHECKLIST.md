# Checklist kiểm tra Backend Server (Visual Studio Code)

## 🔍 Các điểm cần kiểm tra

### 1. ✅ Server có đang chạy không?

**Kiểm tra:**
```bash
# Trong terminal của VS Code, chạy:
node server.js
# hoặc
npm start
```

**Kết quả mong đợi:**
```
✅ Connected to MongoDB Atlas
🚀 Server running on port 3000
```

**Nếu không chạy được:**
- Kiểm tra file `package.json` có script `start` không
- Kiểm tra port 3000 có bị chiếm không: `lsof -i :3000` (Mac/Linux) hoặc `netstat -ano | findstr :3000` (Windows)

---

### 2. ✅ MongoDB có kết nối được không?

**Kiểm tra trong file `server.js` hoặc file config:**
```javascript
mongoose.connect(process.env.MONGO_URI, { 
  useNewUrlParser: true,
  useUnifiedTopology: true,
  family: 4
})
```

**Kiểm tra:**
- File `.env` có tồn tại không?
- `MONGO_URI` có đúng format không?
- MongoDB Atlas có whitelist IP của bạn không?

**Test kết nối:**
```javascript
// Thêm vào server.js để test
mongoose.connection.on('error', (err) => {
  console.error('❌ MongoDB connection error:', err);
});
```

---

### 3. ✅ Route `/api/auth/login` có tồn tại không?

**Kiểm tra file `routes/auth.js`:**

```javascript
// routes/auth.js phải có:
router.post('/login', async (req, res) => {
  // ... code xử lý đăng nhập
});
```

**Kiểm tra:**
- File `routes/auth.js` có tồn tại không?
- Route `/login` có được export không?
- File `server.js` có import route này không?

**Ví dụ đúng:**
```javascript
// server.js
const authRoutes = require("./routes/auth");
app.use("/api/auth", authRoutes);
```

---

### 4. ✅ Format Request từ Android có đúng không?

**Android gửi:**
```json
{
  "tenDangNhap": "admin",
  "matKhau": "admin123",
  "vaiTro": "admin"
}
```

**Kiểm tra trong `routes/auth.js`:**
```javascript
router.post('/login', async (req, res) => {
  console.log('📥 Request body:', req.body);
  // Phải log ra được object trên
});
```

**Nếu không nhận được body:**
- Kiểm tra `app.use(express.json())` trong `server.js`
- Kiểm tra middleware CORS

---

### 5. ✅ Format Response có đúng không?

**Android mong đợi:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "_id": "...",
    "hoTen": "...",
    "tenDangNhap": "...",
    "vaiTro": "...",
    ...
  }
}
```

**Kiểm tra trong `routes/auth.js`:**
```javascript
router.post('/login', async (req, res) => {
  try {
    // ... logic xử lý
    
    res.json({
      success: true,
      message: "Đăng nhập thành công",
      data: user // user object từ database
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message,
      data: null
    });
  }
});
```

---

### 6. ✅ CORS có được cấu hình đúng không?

**Kiểm tra trong `server.js`:**
```javascript
app.use(cors()); // Cho phép tất cả origins

// Hoặc cấu hình cụ thể:
app.use(cors({
  origin: '*', // hoặc ['http://localhost', 'http://10.0.2.2']
  methods: ['GET', 'POST', 'PUT', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));
```

**Nếu gặp lỗi CORS:**
- Thêm `origin: '*'` vào cấu hình CORS
- Hoặc thêm domain cụ thể của Android app

---

### 7. ✅ User có tồn tại trong database không?

**Test query trong MongoDB:**
```javascript
// Thêm vào routes/auth.js để debug
const user = await User.findOne({ 
  tenDangNhap: req.body.tenDangNhap 
});
console.log('👤 User found:', user);
```

**Hoặc test trực tiếp trong MongoDB Compass/Atlas:**
```javascript
db.users.findOne({ tenDangNhap: "admin" })
```

---

### 8. ✅ Mật khẩu có được hash/verify đúng không?

**Kiểm tra:**
- Backend có hash mật khẩu khi tạo user không?
- Backend có verify mật khẩu khi login không?

**Ví dụ với bcrypt:**
```javascript
// Khi tạo user
const hashedPassword = await bcrypt.hash(password, 10);

// Khi login
const isMatch = await bcrypt.compare(req.body.matKhau, user.matKhau);
if (!isMatch) {
  return res.status(401).json({
    success: false,
    message: "Mật khẩu không đúng"
  });
}
```

---

### 9. ✅ Logging để debug

**Thêm vào `routes/auth.js`:**
```javascript
router.post('/login', async (req, res) => {
  console.log('=== LOGIN REQUEST ===');
  console.log('Body:', req.body);
  console.log('Headers:', req.headers);
  
  try {
    // ... code xử lý
    
    console.log('✅ Login success');
    res.json({ success: true, message: "...", data: user });
  } catch (error) {
    console.error('❌ Login error:', error);
    res.status(400).json({ success: false, message: error.message });
  }
});
```

---

### 10. ✅ Test API bằng Postman/Thunder Client

**Trong VS Code, cài extension Thunder Client hoặc dùng Postman:**

**Request:**
- Method: `POST`
- URL: `http://localhost:3000/api/auth/login`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "tenDangNhap": "admin",
  "matKhau": "admin123",
  "vaiTro": "admin"
}
```

**Kết quả mong đợi:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": { ... }
}
```

**Nếu Postman/Thunder Client không hoạt động:**
- Backend có vấn đề
- Nếu Postman hoạt động nhưng Android không:
  - Kiểm tra BASE_URL trong Android
  - Kiểm tra network permissions

---

## 🐛 Common Issues

### Issue 1: "Cannot POST /api/auth/login"
**Nguyên nhân:** Route chưa được đăng ký
**Giải pháp:** Kiểm tra `app.use("/api/auth", authRoutes)` trong server.js

### Issue 2: "req.body is undefined"
**Nguyên nhân:** Thiếu `express.json()` middleware
**Giải pháp:** Thêm `app.use(express.json())` trước routes

### Issue 3: "CORS error"
**Nguyên nhân:** CORS chưa được cấu hình
**Giải pháp:** Thêm `app.use(cors())` hoặc cấu hình CORS đúng

### Issue 4: "User not found"
**Nguyên nhân:** 
- User chưa được tạo trong database
- Field name không khớp (tenDangNhap vs username)

**Giải pháp:** 
- Tạo user mẫu trong database
- Kiểm tra field names

### Issue 5: "Password incorrect"
**Nguyên nhân:** 
- Mật khẩu không được hash khi tạo
- Hash/verify không đúng

**Giải pháp:** 
- Kiểm tra logic hash/verify
- Tạo lại user với mật khẩu đã hash

---

## 📝 Template code cho routes/auth.js

```javascript
const express = require('express');
const router = express.Router();
const User = require('../models/User'); // Điều chỉnh path model
const bcrypt = require('bcrypt');

router.post('/login', async (req, res) => {
  console.log('📥 Login request:', req.body);
  
  try {
    const { tenDangNhap, matKhau, vaiTro } = req.body;
    
    // Validate
    if (!tenDangNhap || !matKhau) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập tài khoản và mật khẩu",
        data: null
      });
    }
    
    // Tìm user
    const user = await User.findOne({ tenDangNhap });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Tài khoản không tồn tại",
        data: null
      });
    }
    
    // Verify password
    const isMatch = await bcrypt.compare(matKhau, user.matKhau);
    if (!isMatch) {
      return res.status(401).json({
        success: false,
        message: "Mật khẩu không đúng",
        data: null
      });
    }
    
    // Kiểm tra vai trò nếu cần
    if (vaiTro && user.vaiTro !== vaiTro) {
      return res.status(403).json({
        success: false,
        message: "Bạn không có quyền truy cập với vai trò này",
        data: null
      });
    }
    
    // Success
    res.json({
      success: true,
      message: "Đăng nhập thành công",
      data: {
        _id: user._id,
        hoTen: user.hoTen,
        gioiTinh: user.gioiTinh,
        ngaySinh: user.ngaySinh,
        soDienThoai: user.soDienThoai,
        tenDangNhap: user.tenDangNhap,
        vaiTro: user.vaiTro,
        anhDaiDien: user.anhDaiDien || ""
      }
    });
    
  } catch (error) {
    console.error('❌ Login error:', error);
    res.status(500).json({
      success: false,
      message: "Lỗi server: " + error.message,
      data: null
    });
  }
});

module.exports = router;
```

---

## 🔧 Quick Test Script

Tạo file `test-api.js` trong thư mục backend:

```javascript
const axios = require('axios');

async function testLogin() {
  try {
    const response = await axios.post('http://localhost:3000/api/auth/login', {
      tenDangNhap: 'admin',
      matKhau: 'admin123',
      vaiTro: 'admin'
    });
    
    console.log('✅ Success:', response.data);
  } catch (error) {
    console.error('❌ Error:', error.response?.data || error.message);
  }
}

testLogin();
```

Chạy: `node test-api.js`

