# Trạng thái di chuyển file

## ✅ Đã hoàn thành:

### Admin Activities:
- ✅ ManHinhChinhAdminActivity.java
- ✅ DanhSachKhachHangActivity.java
- ✅ DanhSachSanPhamActivity.java
- ✅ DanhSachLoaiSanPhamActivity.java
- ✅ DanhSachHoaDonActivity.java
- ✅ DanhSachNhanVienActivity.java
- ✅ ThongKeActivity.java
- ✅ DoiMatKhauActivity.java

### Admin Adapters:
- ✅ SanPhamAdapter.java
- ✅ LoaiSanPhamAdapter.java
- ✅ NhanVienAdapter.java
- ✅ HoaDonAdapter.java
- ✅ KhachHangAdapter.java

### Customer Activities:
- ✅ DangKyActivity.java
- ✅ DangNhapActivity.java

## ⏳ Cần tạo tiếp (với package name đã cập nhật):

### Admin Activities còn lại:
- ThemSuaSanPhamActivity.java → admin/ThemSuaSanPhamActivity.java
- ThemSuaLoaiSanPhamActivity.java → admin/ThemSuaLoaiSanPhamActivity.java
- ThemSuaKhachHangActivity.java → admin/ThemSuaKhachHangActivity.java
- ThemSuaNhanVienActivity.java → admin/ThemSuaNhanVienActivity.java
- TaoHoaDonActivity.java → admin/TaoHoaDonActivity.java
- ManHinhChinhNhanVienActivity.java → admin/ManHinhChinhNhanVienActivity.java

## 📝 Cần cập nhật:

1. **AndroidManifest.xml**: Cập nhật tất cả package names cho các Activity
2. **Xóa file cũ**: Sau khi test, xóa các file cũ trong folder gốc

## 🔧 Cách làm:

1. Đọc file gốc
2. Tạo file mới trong admin/ với package: `package fpoly.haideptrai.duan1.admin;`
3. Cập nhật imports:
   - `import fpoly.haideptrai.duan1.adapters.*` → `import fpoly.haideptrai.duan1.admin.adapters.*`
   - `import fpoly.haideptrai.duan1.DanhSach*` → `import fpoly.haideptrai.duan1.admin.DanhSach*`
   - `import fpoly.haideptrai.duan1.DangNhapActivity` → `import fpoly.haideptrai.duan1.customer.DangNhapActivity`
4. Thêm `import fpoly.haideptrai.duan1.R;` nếu thiếu
