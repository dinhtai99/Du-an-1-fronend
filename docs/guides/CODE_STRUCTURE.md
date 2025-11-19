# Cấu trúc Code - Tổ chức Admin và Customer

## 📁 Cấu trúc mới:

```
app/src/main/java/fpoly/haideptrai/duan1/
│
├── admin/                          # 👨‍💼 TẤT CẢ MÀN HÌNH ADMIN
│   ├── ManHinhChinhAdminActivity.java
│   ├── DanhSachSanPhamActivity.java
│   ├── DanhSachLoaiSanPhamActivity.java
│   ├── DanhSachKhachHangActivity.java ✅ (đã di chuyển)
│   ├── DanhSachHoaDonActivity.java
│   ├── DanhSachNhanVienActivity.java
│   ├── ThongKeActivity.java
│   ├── ThemSuaSanPhamActivity.java
│   ├── ThemSuaLoaiSanPhamActivity.java
│   ├── ThemSuaKhachHangActivity.java
│   ├── ThemSuaNhanVienActivity.java
│   ├── TaoHoaDonActivity.java
│   ├── DoiMatKhauActivity.java
│   ├── ManHinhChinhNhanVienActivity.java
│   └── adapters/                   # Adapters cho admin
│       ├── SanPhamAdapter.java
│       ├── LoaiSanPhamAdapter.java
│       ├── KhachHangAdapter.java ✅ (đã di chuyển)
│       ├── NhanVienAdapter.java
│       └── HoaDonAdapter.java
│
├── customer/                       # 👤 TẤT CẢ MÀN HÌNH KHÁCH HÀNG
│   ├── DangKyActivity.java ✅ (đã di chuyển)
│   ├── DangNhapActivity.java (dùng chung)
│   ├── HomeActivity.java (sẽ tạo)
│   ├── ChiTietSanPhamActivity.java (sẽ tạo)
│   ├── GioHangActivity.java (sẽ tạo)
│   └── adapters/                  # Adapters cho customer
│
├── api/                            # 🌐 API Services (DÙNG CHUNG)
│   ├── ApiClient.java
│   ├── TokenStore.java
│   ├── models/
│   └── services/
│
├── database/                       # 💾 Database (DÙNG CHUNG)
│   ├── AppDatabase.java
│   ├── entities/
│   └── daos/
│
└── utils/                          # 🛠️ Utilities (DÙNG CHUNG)
    ├── SessionManager.java
    ├── PasswordHelper.java
    └── DatabaseInitializer.java
```

## ✅ Đã hoàn thành:

1. ✅ Tạo folder `admin/` và `customer/`
2. ✅ Di chuyển `DanhSachKhachHangActivity` → `admin/DanhSachKhachHangActivity`
3. ✅ Di chuyển `KhachHangAdapter` → `admin/adapters/KhachHangAdapter`
4. ✅ Di chuyển `DangKyActivity` → `customer/DangKyActivity`
5. ✅ Cập nhật package names và imports

## 📋 Cần làm tiếp:

### Di chuyển các Activity ADMIN còn lại:
- [ ] ManHinhChinhAdminActivity
- [ ] DanhSachSanPhamActivity
- [ ] DanhSachLoaiSanPhamActivity
- [ ] DanhSachHoaDonActivity
- [ ] DanhSachNhanVienActivity
- [ ] ThongKeActivity
- [ ] ThemSuaSanPhamActivity
- [ ] ThemSuaLoaiSanPhamActivity
- [ ] ThemSuaKhachHangActivity
- [ ] ThemSuaNhanVienActivity
- [ ] TaoHoaDonActivity
- [ ] DoiMatKhauActivity
- [ ] ManHinhChinhNhanVienActivity

### Di chuyển các Adapter ADMIN:
- [ ] SanPhamAdapter → admin/adapters/
- [ ] LoaiSanPhamAdapter → admin/adapters/
- [ ] NhanVienAdapter → admin/adapters/
- [ ] HoaDonAdapter → admin/adapters/

### Cập nhật:
- [ ] AndroidManifest.xml (cập nhật package names)
- [ ] Tất cả imports trong các file liên quan

## 💡 Lợi ích:

1. **Dễ phân biệt**: Nhìn vào folder là biết ngay màn hình nào của admin, màn hình nào của customer
2. **Dễ quản lý**: Code được tổ chức rõ ràng, dễ tìm kiếm
3. **Dễ mở rộng**: Khi thêm màn hình mới, chỉ cần đặt vào đúng folder
4. **Tránh nhầm lẫn**: Không còn lo lắng về việc nhầm lẫn giữa màn hình admin và customer

## 🔧 Cách sử dụng:

Khi tạo màn hình mới:
- **Màn hình Admin** → Đặt trong `admin/`
- **Màn hình Customer** → Đặt trong `customer/`
- **Dùng chung** → Đặt trong `api/`, `database/`, hoặc `utils/`

