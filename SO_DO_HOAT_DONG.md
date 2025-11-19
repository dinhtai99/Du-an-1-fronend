# SƠ ĐỒ HOẠT ĐỘNG ỨNG DỤNG E-COMMERCE

## 📱 TỔNG QUAN
Ứng dụng mua sắm trực tuyến cho khách hàng với các tính năng: xem sản phẩm, giỏ hàng, thanh toán, quản lý đơn hàng, voucher, và thông tin cá nhân.

---

## 🔄 LUỒNG HOẠT ĐỘNG CHÍNH

### 1. **KHỞI ĐỘNG VÀ XÁC THỰC**

```
┌─────────────────┐
│  DangNhapActivity │ (Launcher Activity)
└────────┬────────┘
         │
         ├─► Đăng nhập thành công
         │   └─► Lưu JWT token
         │   └─► Lưu MongoDB User ID
         │   └─► Lưu session (SessionManager)
         │
         └─► Chưa có tài khoản?
             └─► DangKyActivity
                 └─► Đăng ký thành công
                     └─► Quay lại DangNhapActivity
```

**Chi tiết:**
- **DangNhapActivity**: Màn hình đầu tiên khi mở app
  - Nhập username/password
  - Gọi API `/api/auth/login`
  - Lưu token vào `TokenStore`
  - Lưu MongoDB User ID vào `SessionManager`
  - Chuyển đến `HomeActivity` nếu thành công

- **DangKyActivity**: Đăng ký tài khoản mới
  - Gọi API `/api/auth/register`
  - Quay lại `DangNhapActivity` sau khi đăng ký thành công

---

### 2. **TRANG CHỦ (HOME)**

```
┌─────────────────┐
│  HomeActivity   │
└────────┬────────┘
         │
         ├─► Load danh mục (Categories)
         ├─► Load sản phẩm (Products)
         │
         ├─► Click vào danh mục
         │   └─► Filter sản phẩm theo danh mục
         │
         ├─► Click vào sản phẩm
         │   └─► ChiTietSanPhamActivity
         │
         └─► Bottom Navigation
             ├─► Home (đang ở đây)
             ├─► Voucher → QuanLyVoucherActivity
             ├─► Giỏ hàng → GioHangActivity
             └─► Profile → ThongTinCaNhanActivity
```

**Chi tiết:**
- Hiển thị banner, danh mục sản phẩm, danh sách sản phẩm
- Gọi API:
  - `GET /api/categories` - Lấy danh sách danh mục
  - `GET /api/products` - Lấy danh sách sản phẩm
- Click sản phẩm → Chuyển đến `ChiTietSanPhamActivity`

---

### 3. **CHI TIẾT SẢN PHẨM**

```
┌──────────────────────┐
│ ChiTietSanPhamActivity│
└──────────┬───────────┘
           │
           ├─► Xem thông tin sản phẩm
           │   ├─► Tên, giá, mô tả
           │   ├─► Hình ảnh
           │   └─► Số lượng tồn kho
           │
           ├─► Chọn số lượng
           │
           └─► Thêm vào giỏ hàng
               └─► Lưu vào CartManager (local)
               └─► Hiển thị thông báo thành công
```

**Chi tiết:**
- Gọi API `GET /api/products/{id}` để lấy thông tin chi tiết
- Thêm sản phẩm vào giỏ hàng (lưu local bằng `CartManager`)
- Có thể quay lại Home hoặc chuyển đến Giỏ hàng

---

### 4. **GIỎ HÀNG**

```
┌─────────────────┐
│ GioHangActivity │
└────────┬────────┘
         │
         ├─► Hiển thị danh sách sản phẩm
         │   ├─► Tên, giá, số lượng
         │   └─► Tổng tiền
         │
         ├─► Tăng/Giảm số lượng
         ├─► Xóa sản phẩm
         │
         └─► Thanh toán
             └─► ThanhToanActivity
```

**Chi tiết:**
- Load sản phẩm từ `CartManager` (local storage)
- Cập nhật số lượng, xóa sản phẩm
- Click "Thanh toán" → Chuyển đến `ThanhToanActivity` với danh sách sản phẩm

---

### 5. **THANH TOÁN**

```
┌──────────────────┐
│ ThanhToanActivity│
└────────┬─────────┘
         │
         ├─► Nhập địa chỉ giao hàng
         │   ├─► Nhập thủ công
         │   └─► Hoặc dùng vị trí hiện tại (GPS)
         │
         ├─► Chọn voucher (tùy chọn)
         │   ├─► Nhập mã voucher
         │   ├─► Hoặc chọn từ danh sách
         │   └─► Validate voucher với backend
         │
         ├─► Chọn phương thức thanh toán
         │   ├─► COD (Tiền mặt)
         │   ├─► ZaloPay
         │   ├─► MoMo
         │   └─► Các phương thức khác
         │
         └─► Xác nhận thanh toán
             │
             ├─► COD
             │   └─► Gọi POST /api/invoices
             │       ├─► Tạo đơn hàng
             │       ├─► Trừ tồn kho ngay
             │       ├─► Tăng số lượt dùng voucher
             │       └─► Chuyển đến DonHangActivity
             │
             ├─► ZaloPay
             │   ├─► Gọi POST /api/invoices
             │   │   └─► Backend tạo order + gọi ZaloPay API
             │   │       └─► Trả về zp_trans_token, order_url
             │   ├─► Mở ZaloPay app (deep link)
             │   ├─► Xử lý callback từ ZaloPay
             │   └─► Chuyển đến DonHangActivity
             │
             └─► MoMo
                 ├─► Gọi POST /api/invoices
                 │   └─► Backend tạo order + gọi MoMo API
                 │       └─► Trả về payUrl, deeplink, qrCodeUrl
                 ├─► Mở MoMo app (deep link)
                 ├─► Xử lý callback từ MoMo
                 └─► Chuyển đến DonHangActivity
```

**Chi tiết:**
- **Địa chỉ giao hàng:**
  - Nhập thủ công hoặc dùng GPS để lấy vị trí hiện tại
  - Parse địa chỉ thành object `ShippingAddress` (address, ward, district, city)

- **Voucher:**
  - Validate voucher: `GET /api/vouchers/validate/{code}`
  - Kiểm tra minimum order amount
  - Áp dụng giảm giá vào tổng tiền

- **Thanh toán:**
  - **COD**: Tạo đơn hàng ngay, trừ tồn kho, xóa giỏ hàng
  - **ZaloPay**: Tạo đơn hàng, mở ZaloPay app, xử lý callback
  - **MoMo**: Tạo đơn hàng, mở MoMo app, xử lý callback

- **API Endpoint:** `POST /api/invoices`
  - Body: `customer`, `items`, `shippingAddress`, `paymentMethod`, `voucherCode`
  - Response: `InvoiceResponse` với thông tin đơn hàng

---

### 6. **QUẢN LÝ ĐƠN HÀNG**

```
┌─────────────────┐
│ DonHangActivity │
└────────┬────────┘
         │
         ├─► Load danh sách đơn hàng
         │   └─► GET /api/invoices?customer={userId}
         │
         ├─► Hiển thị danh sách
         │   ├─► Mã đơn hàng
         │   ├─► Trạng thái
         │   ├─► Tổng tiền
         │   ├─► Số lượng sản phẩm
         │   ├─► Phương thức thanh toán
         │   └─► Ngày tạo
         │
         └─► Click vào đơn hàng
             └─► TheoDoiDonHangActivity
```

**Chi tiết:**
- Gọi API `GET /api/invoices?customer={mongoUserId}` để lấy đơn hàng của user
- Hiển thị danh sách đơn hàng với adapter `DonHangAdapter`
- Empty state nếu chưa có đơn hàng
- Click đơn hàng → Chuyển đến `TheoDoiDonHangActivity`

---

### 7. **THEO DÕI ĐƠN HÀNG**

```
┌──────────────────────┐
│ TheoDoiDonHangActivity│
└──────────┬───────────┘
           │
           ├─► Load chi tiết đơn hàng
           │   └─► GET /api/invoices/{invoiceId}
           │
           ├─► Hiển thị thông tin
           │   ├─► Mã đơn hàng
           │   ├─► Trạng thái (timeline)
           │   ├─► Danh sách sản phẩm
           │   ├─► Địa chỉ giao hàng
           │   ├─► Phương thức thanh toán
           │   ├─► Tổng tiền (subtotal, shipping, voucher, total)
           │   └─► Ngày tạo
           │
           └─► Timeline trạng thái
               ├─► Mới
               ├─► Đã xác nhận
               ├─► Đang giao hàng
               └─► Đã giao hàng
```

**Chi tiết:**
- Gọi API `GET /api/invoices/{invoiceId}` để lấy chi tiết đơn hàng
- Hiển thị timeline trạng thái đơn hàng
- Hiển thị đầy đủ thông tin sản phẩm, địa chỉ, thanh toán

---

### 8. **QUẢN LÝ VOUCHER**

```
┌──────────────────────┐
│ QuanLyVoucherActivity│
└──────────┬───────────┘
           │
           ├─► Load danh sách voucher
           │   └─► GET /api/vouchers
           │
           ├─► Hiển thị voucher
           │   ├─► Mã voucher
           │   ├─► Mô tả
           │   ├─► Giá trị giảm
           │   ├─► Điều kiện (min order amount)
           │   └─► Ngày hết hạn
           │
           └─► Chọn voucher (từ ThanhToanActivity)
               └─► Trả về voucher code
               └─► Quay lại ThanhToanActivity
```

**Chi tiết:**
- Gọi API `GET /api/vouchers` để lấy danh sách voucher
- Có thể mở từ:
  - Bottom Navigation (tab Voucher)
  - ThanhToanActivity (chọn voucher khi thanh toán)
- Khi chọn voucher từ `ThanhToanActivity`, trả về voucher code qua `onActivityResult`

---

### 9. **THÔNG TIN CÁ NHÂN**

```
┌──────────────────────┐
│ ThongTinCaNhanActivity│
└──────────┬───────────┘
           │
           ├─► Load thông tin user
           │   └─► GET /api/users/{userId}
           │
           ├─► Hiển thị thông tin
           │   ├─► Avatar
           │   ├─► Họ tên
           │   ├─► Email
           │   ├─► Số điện thoại
           │   ├─► Số đơn hàng
           │   └─► Địa chỉ
           │
           ├─► Sửa thông tin
           │   └─► SuaThongTinActivity
           │
           └─► Đơn hàng của tôi
               └─► DonHangActivity
```

**Chi tiết:**
- Gọi API `GET /api/users/{userId}` để lấy thông tin user
- Hiển thị thông tin cá nhân và số đơn hàng
- Có thể sửa thông tin hoặc xem đơn hàng

---

### 10. **SỬA THÔNG TIN**

```
┌──────────────────────┐
│ SuaThongTinActivity  │
└──────────┬───────────┘
           │
           ├─► Load thông tin hiện tại
           ├─► Chỉnh sửa
           │   ├─► Họ tên
           │   ├─► Email
           │   ├─► Số điện thoại
           │   └─► Địa chỉ
           │
           └─► Lưu thông tin
               └─► PUT /api/users/{userId}
                   └─► Quay lại ThongTinCaNhanActivity
```

**Chi tiết:**
- Gọi API `PUT /api/users/{userId}` để cập nhật thông tin
- Sau khi lưu thành công, quay lại `ThongTinCaNhanActivity`

---

## 🔐 XÁC THỰC VÀ BẢO MẬT

### Token Management
- **TokenStore**: Lưu JWT token vào SharedPreferences
- **ApiClient**: Tự động thêm `Authorization: Bearer {token}` vào mọi request (trừ login/register)
- Token được lưu khi đăng nhập thành công

### Session Management
- **SessionManager**: Lưu thông tin user session
  - Local database ID (int)
  - MongoDB User ID (String) - dùng cho API calls
  - Username, Họ tên, Vai trò

---

## 📦 QUẢN LÝ GIỎ HÀNG

### CartManager
- Lưu giỏ hàng vào SharedPreferences (local)
- Cấu trúc: `List<CartItem>` với `productId`, `quantity`, `price`
- Khi thanh toán thành công, xóa giỏ hàng

---

## 💳 TÍCH HỢP THANH TOÁN

### ZaloPay
1. Khởi tạo SDK trong `MyApplication`
2. Tạo đơn hàng qua backend (`POST /api/invoices`)
3. Backend gọi ZaloPay API, trả về `zp_trans_token`, `order_url`
4. Mở ZaloPay app qua deep link
5. Xử lý callback qua `onActivityResult` hoặc deep link handler

### MoMo
1. Tạo đơn hàng qua backend (`POST /api/invoices`)
2. Backend gọi MoMo API, trả về `payUrl`, `deeplink`, `qrCodeUrl`
3. Mở MoMo app qua deep link
4. Xử lý callback qua `onActivityResult`

### COD (Cash on Delivery)
- Tạo đơn hàng trực tiếp
- Trừ tồn kho ngay
- Không cần xử lý callback

---

## 🗺️ NAVIGATION FLOW

```
DangNhapActivity
    │
    ├─► HomeActivity (Bottom Nav: Home)
    │   │
    │   ├─► ChiTietSanPhamActivity
    │   │   └─► (Thêm vào giỏ) → GioHangActivity
    │   │
    │   ├─► GioHangActivity (Bottom Nav: Cart)
    │   │   └─► ThanhToanActivity
    │   │       ├─► QuanLyVoucherActivity (chọn voucher)
    │   │       └─► DonHangActivity (sau khi thanh toán)
    │   │
    │   ├─► QuanLyVoucherActivity (Bottom Nav: Voucher)
    │   │
    │   └─► ThongTinCaNhanActivity (Bottom Nav: Profile)
    │       ├─► SuaThongTinActivity
    │       └─► DonHangActivity
    │           └─► TheoDoiDonHangActivity
```

---

## 🔄 DATA FLOW

### 1. **Đăng nhập**
```
User Input → DangNhapActivity
    ↓
POST /api/auth/login
    ↓
Response: { token, user }
    ↓
TokenStore.setToken(token)
SessionManager.saveMongoUserId(user.id)
    ↓
Navigate to HomeActivity
```

### 2. **Thêm vào giỏ hàng**
```
ChiTietSanPhamActivity
    ↓
CartManager.addItem(productId, quantity, price)
    ↓
Save to SharedPreferences (local)
```

### 3. **Thanh toán**
```
ThanhToanActivity
    ↓
Create InvoiceRequest
    ├─► customer (MongoDB User ID)
    ├─► items (from CartManager)
    ├─► shippingAddress (object)
    ├─► paymentMethod (COD/ZaloPay/MoMo)
    └─► voucherCode (optional)
    ↓
POST /api/invoices
    ↓
Backend:
    ├─► Create Order
    ├─► If COD: Deduct inventory, increase voucher usage
    ├─► If ZaloPay/MoMo: Call payment API
    └─► Return InvoiceResponse
    ↓
If COD: Navigate to DonHangActivity
If ZaloPay/MoMo: Open payment app → Handle callback → Navigate to DonHangActivity
```

### 4. **Load đơn hàng**
```
DonHangActivity
    ↓
GET /api/invoices?customer={mongoUserId}
    ↓
Response: List<InvoiceResponse>
    ↓
Display in RecyclerView
```

---

## 📱 BOTTOM NAVIGATION

Bottom Navigation có 4 tab:
1. **Home** → `HomeActivity`
2. **Voucher** → `QuanLyVoucherActivity`
3. **Cart** → `GioHangActivity`
4. **Profile** → `ThongTinCaNhanActivity`

---

## 🎯 CÁC TÍNH NĂNG CHÍNH

✅ **Xác thực**: Đăng nhập, Đăng ký
✅ **Xem sản phẩm**: Danh sách, Chi tiết, Tìm kiếm theo danh mục
✅ **Giỏ hàng**: Thêm, Sửa số lượng, Xóa
✅ **Thanh toán**: COD, ZaloPay, MoMo
✅ **Voucher**: Xem danh sách, Áp dụng khi thanh toán
✅ **Đơn hàng**: Xem danh sách, Theo dõi chi tiết
✅ **Thông tin cá nhân**: Xem, Sửa
✅ **Location**: Lấy vị trí hiện tại để điền địa chỉ

---

## 🔧 CÔNG NGHỆ SỬ DỤNG

- **Android**: Java, Material Design Components
- **Networking**: Retrofit, OkHttp
- **Local Storage**: SharedPreferences
- **Payment SDKs**: ZaloPay SDK, MoMo Payment SDK
- **Location**: Google Play Services (FusedLocationProviderClient)
- **Image Loading**: (Có thể dùng Glide/Picasso nếu cần)

---

## 📝 LƯU Ý

1. **MongoDB User ID**: Phải lưu MongoDB ObjectId (không phải local database ID) để gọi API đúng
2. **Token**: Tự động thêm vào header mọi request (trừ login/register)
3. **Giỏ hàng**: Lưu local, xóa sau khi thanh toán thành công
4. **Payment Callbacks**: Xử lý qua `onActivityResult` hoặc deep link handler
5. **Voucher**: Validate trước khi áp dụng, kiểm tra minimum order amount

---

*Tài liệu này mô tả sơ đồ hoạt động của ứng dụng E-Commerce Android. Cập nhật: 2025-11-17*

