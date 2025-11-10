// ============================================
// TEMPLATE CODE CHO routes/auth.js
// Copy code này vào file routes/auth.js của bạn
// ============================================

const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
// Điều chỉnh path model theo cấu trúc project của bạn
const User = require('../models/User'); // hoặc '../models/NhanVien'

// ============================================
// POST /api/auth/login
// ============================================
router.post('/login', async (req, res) => {
  console.log('=== 📥 LOGIN REQUEST ===');
  console.log('Body:', JSON.stringify(req.body, null, 2));
  console.log('Headers:', req.headers);
  
  try {
    const { tenDangNhap, matKhau, vaiTro } = req.body;
    
    // ============================================
    // 1. VALIDATE INPUT
    // ============================================
    if (!tenDangNhap || !matKhau) {
      console.log('❌ Missing fields');
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập tài khoản và mật khẩu",
        data: null
      });
    }
    
    // ============================================
    // 2. TÌM USER TRONG DATABASE
    // ============================================
    console.log('🔍 Searching for user:', tenDangNhap);
    
    // Lưu ý: Field name có thể khác tùy model của bạn
    // Có thể là: username, tenDangNhap, email, etc.
    const user = await User.findOne({ 
      tenDangNhap: tenDangNhap 
      // Hoặc: username: tenDangNhap
      // Hoặc: email: tenDangNhap
    });
    
    if (!user) {
      console.log('❌ User not found');
      return res.status(404).json({
        success: false,
        message: "Tài khoản không tồn tại",
        data: null
      });
    }
    
    console.log('✅ User found:', {
      id: user._id,
      tenDangNhap: user.tenDangNhap,
      vaiTro: user.vaiTro
    });
    
    // ============================================
    // 3. VERIFY PASSWORD
    // ============================================
    console.log('🔐 Verifying password...');
    
    // Nếu mật khẩu chưa được hash (plain text)
    // Bỏ qua bước này và so sánh trực tiếp:
    // if (user.matKhau !== matKhau) { ... }
    
    // Nếu mật khẩu đã được hash với bcrypt:
    const isMatch = await bcrypt.compare(matKhau, user.matKhau);
    // Hoặc nếu dùng field khác: user.password, user.matKhauHash, etc.
    
    if (!isMatch) {
      console.log('❌ Password incorrect');
      return res.status(401).json({
        success: false,
        message: "Mật khẩu không đúng",
        data: null
      });
    }
    
    console.log('✅ Password verified');
    
    // ============================================
    // 4. KIỂM TRA VAI TRÒ (NẾU CẦN)
    // ============================================
    if (vaiTro && user.vaiTro !== vaiTro) {
      console.log('❌ Role mismatch. User role:', user.vaiTro, 'Requested:', vaiTro);
      // Có thể bỏ qua check này nếu muốn cho phép user chọn vai trò
      // return res.status(403).json({
      //   success: false,
      //   message: "Bạn không có quyền truy cập với vai trò này",
      //   data: null
      // });
    }
    
    // ============================================
    // 5. TRẢ VỀ RESPONSE THÀNH CÔNG
    // ============================================
    console.log('✅ Login success!');
    
    // Điều chỉnh các field này theo model của bạn
    res.json({
      success: true,
      message: "Đăng nhập thành công",
      data: {
        _id: user._id || user.id,
        hoTen: user.hoTen || user.fullName || user.name,
        gioiTinh: user.gioiTinh || user.gender || "",
        ngaySinh: user.ngaySinh || user.dateOfBirth || "",
        soDienThoai: user.soDienThoai || user.phone || "",
        tenDangNhap: user.tenDangNhap || user.username || user.email,
        vaiTro: user.vaiTro || user.role || user.vaiTro,
        anhDaiDien: user.anhDaiDien || user.avatar || user.profileImage || ""
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

// ============================================
// POST /api/auth/register (Nếu cần)
// ============================================
router.post('/register', async (req, res) => {
  console.log('=== 📥 REGISTER REQUEST ===');
  console.log('Body:', JSON.stringify(req.body, null, 2));
  
  try {
    const { hoTen, gioiTinh, ngaySinh, soDienThoai, tenDangNhap, matKhau, vaiTro } = req.body;
    
    // Validate
    if (!tenDangNhap || !matKhau || !hoTen) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng điền đầy đủ thông tin",
        data: null
      });
    }
    
    // Kiểm tra user đã tồn tại chưa
    const existingUser = await User.findOne({ tenDangNhap });
    if (existingUser) {
      return res.status(400).json({
        success: false,
        message: "Tài khoản đã tồn tại",
        data: null
      });
    }
    
    // Hash password
    const hashedPassword = await bcrypt.hash(matKhau, 10);
    
    // Tạo user mới
    const newUser = new User({
      hoTen,
      gioiTinh: gioiTinh || "",
      ngaySinh: ngaySinh || "",
      soDienThoai: soDienThoai || "",
      tenDangNhap,
      matKhau: hashedPassword,
      vaiTro: vaiTro || "nhan_vien",
      anhDaiDien: ""
    });
    
    await newUser.save();
    
    res.json({
      success: true,
      message: "Đăng ký thành công",
      data: {
        _id: newUser._id,
        hoTen: newUser.hoTen,
        tenDangNhap: newUser.tenDangNhap,
        vaiTro: newUser.vaiTro
      }
    });
    
  } catch (error) {
    console.error('❌ Register error:', error);
    res.status(500).json({
      success: false,
      message: "Lỗi server: " + error.message,
      data: null
    });
  }
});

module.exports = router;

