# CÁC CHỨC NĂNG VÀ API CÒN LẠI

## 📋 TỔNG QUAN
- **Chức năng chưa hoàn thiện:** 6
- **API chưa được sử dụng:** 3 services
- **TODO trong code:** 8 items

---

## ❌ CHỨC NĂNG CHƯA HOÀN THIỆN

### 1. **Chăm sóc khách hàng (Chat)** ❌
**File:** `ChamSocKhachHangActivity.java`
- ❌ CHƯA có API chat
- ✅ UI đã hoàn thiện
- ✅ Mock messages hoạt động
- ⚠️ **TODO:**
  - Tạo ChatService API (nếu backend có)
  - WebSocket hoặc polling cho real-time chat
  - Lưu lịch sử chat
  - Gửi tin nhắn qua API
  - Xử lý phản hồi tự động từ hệ thống

**Trạng thái:** CẦN BACKEND API

---

### 2. **Quản lý Voucher** ❌
**File:** `QuanLyVoucherActivity.java`
- ❌ CHƯA có API voucher
- ✅ UI đã hoàn thiện
- ✅ Mock data
- ⚠️ **TODO:**
  - Tạo VoucherService API (nếu backend có)
  - Load vouchers từ API
  - Hiển thị vouchers có sẵn
  - Áp dụng voucher khi thanh toán
  - Chức năng thêm/sửa voucher (nếu cần)

**Trạng thái:** CẦN BACKEND API

---

### 3. **Trang chủ - Menu Drawer** ⚠️
**File:** `HomeActivity.java` (line 99)
- ❌ CHƯA có menu drawer
- ⚠️ **TODO:**
  - Tạo Navigation Drawer
  - Menu items: Đơn hàng, Voucher, Chăm sóc khách hàng, Đăng xuất
  - Animation slide in/out

**Trạng thái:** CẦN PHÁT TRIỂN

---

### 4. **Trang chủ - Tìm kiếm** ⚠️
**File:** `HomeActivity.java` (line 104)
- ❌ CHƯA có màn hình tìm kiếm
- ✅ API search đã có: `ProductService.getProducts(search=...)`
- ⚠️ **TODO:**
  - Tạo SearchActivity
  - Search bar với autocomplete
  - Hiển thị kết quả tìm kiếm
  - Filter và sort kết quả

**Trạng thái:** CẦN PHÁT TRIỂN (API đã sẵn)

---

### 5. **Trang chủ - "Xem tất cả" sản phẩm** ⚠️
**File:** `HomeActivity.java` (line 109)
- ❌ CHƯA có màn hình danh sách tất cả sản phẩm
- ✅ API đã có: `ProductService.getProducts()`
- ⚠️ **TODO:**
  - Tạo DanhSachSanPhamActivity (cho customer)
  - Pagination
  - Filter theo danh mục, giá
  - Sort options

**Trạng thái:** CẦN PHÁT TRIỂN (API đã sẵn)

---

### 6. **Sửa thông tin - Load từ API** ⚠️
**File:** `SuaThongTinActivity.java` (line 58)
- ⚠️ CHƯA load thông tin từ API khi vào màn hình
- ✅ Đã có API: `UserService.getById()`
- ✅ Đã có API update: `UserService.updateUser()`
- ⚠️ **TODO:**
  - Gọi `UserService.getById()` khi vào màn hình
  - Load và hiển thị thông tin đầy đủ
  - Xử lý field address (nếu UserResponse có)

**Trạng thái:** CẦN BỔ SUNG (API đã sẵn)

---

### 7. **Bottom Navigation - Khuyến mãi** ⚠️
**File:** `HomeActivity.java`, `GioHangActivity.java`, etc.
- ❌ CHƯA navigate đến QuanLyVoucherActivity
- ⚠️ **TODO:**
  - Thay Toast bằng Intent đến QuanLyVoucherActivity
  - Cập nhật tất cả bottom navigation handlers

**Trạng thái:** CẦN BỔ SUNG

---

### 8. **Chi tiết sản phẩm - Icon yêu thích** ⚠️
**File:** `item_san_pham_home.xml` (đã có UI)
- ❌ CHƯA có chức năng lưu yêu thích
- ⚠️ **TODO:**
  - Lưu danh sách yêu thích vào SharedPreferences
  - Toggle yêu thích khi click
  - Hiển thị trạng thái yêu thích

**Trạng thái:** CẦN PHÁT TRIỂN

---

## 🔌 API SERVICES CHƯA ĐƯỢC SỬ DỤNG

### 1. **StatisticsService** ❌
**File:** `StatisticsService.java`
- ❌ CHƯA được sử dụng ở đâu
- **Endpoints:**
  - `getOverview()` - Tổng quan thống kê
  - `getTopProductsByQuantity()` - Top sản phẩm bán chạy
  - `getTopProductsByRevenue()` - Top sản phẩm doanh thu cao
  - `getDailyRevenue()` - Doanh thu theo ngày
  - `getMonthlyRevenue()` - Doanh thu theo tháng
  - `getYearlyRevenue()` - Doanh thu theo năm
  - `getLowStockProducts()` - Sản phẩm sắp hết hàng
  - `getPaymentMethodsStats()` - Thống kê phương thức thanh toán

**Gợi ý sử dụng:**
- Màn hình thống kê cho admin (đã xóa)
- Có thể dùng cho customer: Top sản phẩm bán chạy

---

### 2. **CustomerService** ⚠️
**File:** `CustomerService.java`
- ⚠️ CHƯA được sử dụng cho customer
- **Endpoints:**
  - `getCustomers()` - Danh sách khách hàng
  - `getById()` - Chi tiết khách hàng
  - `createCustomer()` - Tạo khách hàng
  - `updateCustomer()` - Cập nhật khách hàng
  - `toggleActive()` - Bật/tắt trạng thái
  - `deleteCustomer()` - Xóa khách hàng
  - `getStatistics()` - Thống kê khách hàng

**Gợi ý sử dụng:**
- `getById()` - Lấy thông tin chi tiết customer (có thể dùng thay UserService)
- `getStatistics()` - Thống kê đơn hàng của khách hàng

---

### 3. **HealthService** ❌
**File:** `HealthService.java`
- ❌ CHƯA được sử dụng
- **Endpoints:**
  - `checkHealth()` - Kiểm tra server health

**Gợi ý sử dụng:**
- Kiểm tra kết nối server trước khi gọi API
- Hiển thị trạng thái server

---

## 📝 TODO ITEMS TRONG CODE

### HomeActivity.java
1. ✅ Line 99: Open drawer menu → **TODO**
2. ✅ Line 104: Open search screen → **TODO**
3. ✅ Line 109: Navigate to all products → **TODO**
4. ✅ Line 132: Navigate to discount/voucher → **TODO**

### ChamSocKhachHangActivity.java
5. ✅ Line 56: Make phone call → **TODO**
6. ✅ Line 60: Show menu → **TODO**
7. ✅ Line 64: Attach file/image → **TODO**

### QuanLyVoucherActivity.java
8. ✅ Line 47: Add voucher → **TODO**
9. ✅ Line 51: Edit voucher → **TODO**
10. ✅ Line 63: Load from API → **TODO**

### SuaThongTinActivity.java
11. ✅ Line 58: Load from API → **TODO**
12. ✅ Line 97: Add address field to UserRequest → **TODO**

### ThanhToanActivity.java
13. ✅ Line 44: Get current location → **TODO**
14. ✅ Line 49: Open address picker → **TODO**

### ChiTietSanPhamActivity.java
15. ✅ Icon yêu thích chưa hoạt động → **TODO**

---

## 🎯 ƯU TIÊN TRIỂN KHAI

### Ưu tiên cao:
1. **Sửa thông tin - Load từ API** ⭐⭐⭐
   - API đã sẵn, chỉ cần gọi
   - Quan trọng cho UX

2. **Bottom Navigation - Khuyến mãi** ⭐⭐⭐
   - Dễ triển khai
   - Hoàn thiện navigation flow

3. **Trang chủ - "Xem tất cả" sản phẩm** ⭐⭐
   - API đã sẵn
   - Tính năng quan trọng

### Ưu tiên trung bình:
4. **Trang chủ - Tìm kiếm** ⭐⭐
   - API đã sẵn
   - Cải thiện UX

5. **Icon yêu thích** ⭐
   - Chỉ cần lưu local
   - Không cần API

6. **Menu Drawer** ⭐
   - Cải thiện navigation
   - Không cần API

### Ưu tiên thấp (cần backend):
7. **Voucher API** ⭐
   - Cần backend hỗ trợ
   - Có thể để sau

8. **Chat API** ⭐
   - Cần backend hỗ trợ
   - Có thể để sau

---

## 📊 TỔNG KẾT

### Đã hoàn thiện:
- ✅ Đăng nhập/Đăng ký
- ✅ Trang chủ (categories, products)
- ✅ Chi tiết sản phẩm
- ✅ Giỏ hàng (lưu/load)
- ✅ Thanh toán (tạo đơn hàng)
- ✅ Đơn hàng (list, detail)
- ✅ Thông tin cá nhân (load từ API)
- ✅ Sửa thông tin (update API)

### Chưa hoàn thiện:
- ❌ Chat (cần backend API)
- ❌ Voucher (cần backend API)
- ⚠️ Search (API sẵn, chưa có UI)
- ⚠️ "Xem tất cả" sản phẩm (API sẵn, chưa có UI)
- ⚠️ Menu drawer (không cần API)
- ⚠️ Icon yêu thích (không cần API)
- ⚠️ Load user info khi sửa (API sẵn)

### API chưa dùng:
- ❌ StatisticsService (có thể dùng cho top products)
- ⚠️ CustomerService (có thể dùng thay UserService)
- ❌ HealthService (kiểm tra server)

---

## 💡 KHUYẾN NGHỊ

1. **Triển khai ngay:**
   - Load user info khi sửa thông tin
   - Navigate đến voucher từ bottom nav
   - Màn hình "Xem tất cả" sản phẩm

2. **Triển khai sau:**
   - Màn hình tìm kiếm
   - Menu drawer
   - Icon yêu thích

3. **Chờ backend:**
   - Voucher API
   - Chat API

