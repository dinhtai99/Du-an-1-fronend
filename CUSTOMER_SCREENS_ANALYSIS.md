# Phân tích màn hình khách hàng từ Figma

## 📱 Các màn hình khách hàng cần có (từ hình ảnh):

### ✅ Đã có (2/12):
1. ✅ DangKyActivity (Đăng ký)
2. ✅ DangNhapActivity (Đăng nhập)

### ❌ Còn thiếu (10/12):

#### Từ hình ảnh 1:
3. ❌ ChamSocKhachHangActivity (Chăm sóc khách hàng - Chat)
   - Header: "Chăm sóc khách hàng"
   - Chat conversation với system và user messages
   - Input box để nhập tin nhắn

4. ❌ ThongTinCaNhanActivity (Thông tin cá nhân - Xem)
   - Hiển thị: Họ tên, Email, Số điện thoại, Số đơn hàng, Địa chỉ
   - Nút "Sửa thông tin"

5. ❌ SuaThongTinActivity (Sửa thông tin cá nhân)
   - Form chỉnh sửa: Họ tên, Email, Số điện thoại, Địa chỉ
   - Nút "Lưu thông tin"

#### Từ hình ảnh 2:
6. ❌ HomeActivity (Màn hình khách hàng - Trang chủ)
   - Header: Menu, Search, "Xin chào, [Tên]"
   - Banner sản phẩm
   - Danh mục sản phẩm (icons)
   - Danh sách sản phẩm với giá, tag giảm giá
   - Bottom Navigation Bar

7. ❌ ChiTietSanPhamActivity (Chi tiết sản phẩm)
   - Hình ảnh sản phẩm lớn
   - Thông tin: Tên, Loại, Giá nhập, Giá bán, Số lượng
   - Nút "Thêm vào giỏ hàng"
   - Bottom Navigation Bar

8. ❌ GioHangActivity (Giỏ hàng)
   - Danh sách sản phẩm trong giỏ
   - Mỗi item: Hình, Tên, Giá, Quantity selector (+/-)
   - Tổng tiền

9. ❌ ThanhToanActivity (Thanh toán)
   - Chọn địa chỉ giao hàng
   - Phương thức thanh toán: VISA, Mastercard, Ngân hàng, QR, COD
   - Nút "Thanh toán"

10. ❌ DonHangActivity (Đơn hàng của khách hàng)
    - Danh sách đơn hàng
    - Mỗi đơn: Mã đơn, Trạng thái, Khách hàng, Số tiền, Phương thức thanh toán
    - Hình ảnh sản phẩm

11. ❌ TheoDoiDonHangActivity (Theo dõi đơn hàng)
    - Thông tin đơn hàng: Sản phẩm, Điểm gửi, Điểm đến, Đơn vị vận chuyển, Cân nặng
    - Timeline: Xác nhận → Giao cho vận chuyển → Đến kho → Giao thành công

12. ❌ QuanLyVoucherActivity (Quản lý voucher - có thể là xem voucher của khách hàng)
    - Danh sách voucher
    - Mỗi voucher: Ngày, Mức giảm, Điều kiện, Số lượng, Trạng thái

## 📊 Tổng kết:
- **Đã có**: 2/12 màn hình (17%)
- **Còn thiếu**: 10/12 màn hình (83%)

## 🎯 Ưu tiên tạo:
1. HomeActivity (Trang chủ - màn hình chính)
2. ChiTietSanPhamActivity (Chi tiết sản phẩm)
3. GioHangActivity (Giỏ hàng)
4. ThanhToanActivity (Thanh toán)
5. DonHangActivity (Đơn hàng)
6. TheoDoiDonHangActivity (Theo dõi đơn hàng)
7. ThongTinCaNhanActivity (Thông tin cá nhân)
8. SuaThongTinActivity (Sửa thông tin)
9. ChamSocKhachHangActivity (Chat)
10. QuanLyVoucherActivity (Voucher)
