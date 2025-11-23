package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;
import fpoly.haideptrai.duan1.api.services.InvoiceService;
import fpoly.haideptrai.duan1.customer.adapters.TimelineAdapter;
import fpoly.haideptrai.duan1.customer.models.TimelineItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TheoDoiDonHangActivity extends AppCompatActivity {

    private ImageView imgSanPham;
    private TextView txtMaDonHang, txtTrangThai, txtTenSanPham, txtDiemGui, txtDiemDen, txtDonViVanChuyen, txtCanNang;
    private TextView txtTongTien, txtPhuongThucThanhToan, txtNgayTao;
    private RecyclerView rvTimeline;
    private ImageButton btnBack, btnMenu;
    private com.google.android.material.button.MaterialButton btnDanhGia;
    private TimelineAdapter timelineAdapter;
    private InvoiceService invoiceService;
    private fpoly.haideptrai.duan1.api.services.ReviewService reviewService;
    private String invoiceId;
    private InvoiceResponse currentInvoice;
    private java.text.NumberFormat currency = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theo_doi_don_hang);

        invoiceId = getIntent().getStringExtra("invoice_id");
        if (invoiceId == null || invoiceId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        invoiceService = ApiClient.getClient().create(InvoiceService.class);
        reviewService = ApiClient.getClient().create(fpoly.haideptrai.duan1.api.services.ReviewService.class);
        loadOrderDetails();
    }

    private void initViews() {
        imgSanPham = findViewById(R.id.imgSanPham);
        txtMaDonHang = findViewById(R.id.txtMaDonHang);
        txtTrangThai = findViewById(R.id.txtTrangThai);
        txtTenSanPham = findViewById(R.id.txtTenSanPham);
        txtDiemGui = findViewById(R.id.txtDiemGui);
        txtDiemDen = findViewById(R.id.txtDiemDen);
        txtDonViVanChuyen = findViewById(R.id.txtDonViVanChuyen);
        txtCanNang = findViewById(R.id.txtCanNang);
        txtTongTien = findViewById(R.id.txtTongTien);
        txtPhuongThucThanhToan = findViewById(R.id.txtPhuongThucThanhToan);
        txtNgayTao = findViewById(R.id.txtNgayTao);
        rvTimeline = findViewById(R.id.rvTimeline);
        btnBack = findViewById(R.id.btnBack);
        btnMenu = findViewById(R.id.btnMenu);
        btnDanhGia = findViewById(R.id.btnDanhGia);

        btnBack.setOnClickListener(v -> finish());
        btnMenu.setOnClickListener(v -> {
            // Reload order details
            loadOrderDetails();
            Toast.makeText(this, "Đang làm mới thông tin đơn hàng...", Toast.LENGTH_SHORT).show();
        });

        btnDanhGia.setOnClickListener(v -> {
            if (currentInvoice != null && currentInvoice.getItems() != null && !currentInvoice.getItems().isEmpty()) {
                // Lấy sản phẩm đầu tiên để đánh giá
                InvoiceResponse.Item firstItem = currentInvoice.getItems().get(0);
                if (firstItem.getProduct() != null && firstItem.getProduct().get_id() != null) {
                    showReviewDialog(firstItem.getProduct().get_id(), invoiceId);
                }
            }
        });

        timelineAdapter = new TimelineAdapter();
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        rvTimeline.setAdapter(timelineAdapter);
    }

    private void loadOrderDetails() {
        Call<ApiResponse<InvoiceResponse>> call = invoiceService.getById(invoiceId);
        call.enqueue(new Callback<ApiResponse<InvoiceResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<InvoiceResponse>> call, Response<ApiResponse<InvoiceResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<InvoiceResponse> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        InvoiceResponse invoice = apiResponse.getData();
                        currentInvoice = invoice; // Lưu invoice để dùng cho đánh giá

                        // Log toàn bộ thông tin để debug
                        android.util.Log.d("TheoDoiDonHang", "=== INVOICE DATA ===");
                        android.util.Log.d("TheoDoiDonHang", "Invoice Number: " + invoice.getInvoiceNumber());
                        android.util.Log.d("TheoDoiDonHang", "Status: " + invoice.getStatus());
                        android.util.Log.d("TheoDoiDonHang", "Total: " + invoice.getTotal());
                        android.util.Log.d("TheoDoiDonHang", "Payment Method: " + invoice.getPaymentMethod());
                        android.util.Log.d("TheoDoiDonHang", "Created At: " + invoice.getCreatedAt());
                        android.util.Log.d("TheoDoiDonHang", "Items count: " + (invoice.getItems() != null ? invoice.getItems().size() : 0));

                        if (invoice.getShippingAddress() != null) {
                            android.util.Log.d("TheoDoiDonHang", "ShippingAddress - Address: " + invoice.getShippingAddress().getAddress() +
                                    ", Ward: " + invoice.getShippingAddress().getWard() +
                                    ", District: " + invoice.getShippingAddress().getDistrict() +
                                    ", City: " + invoice.getShippingAddress().getCity());
                        } else {
                            android.util.Log.d("TheoDoiDonHang", "ShippingAddress is null");
                        }

                        if (invoice.getCustomer() != null) {
                            android.util.Log.d("TheoDoiDonHang", "Customer address: " + invoice.getCustomer().getAddress());
                        }

                        // Log raw JSON để xem toàn bộ dữ liệu
                        try {
                            String json = new com.google.gson.Gson().toJson(invoice);
                            android.util.Log.d("TheoDoiDonHang", "Full Invoice JSON: " + json);
                        } catch (Exception e) {
                            android.util.Log.e("TheoDoiDonHang", "Error logging JSON", e);
                        }

                        displayOrderDetails(invoice);
                    } else {
                        android.util.Log.e("TheoDoiDonHang", "API response not successful or data is null. Message: " +
                                (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"));
                        Toast.makeText(TheoDoiDonHangActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Không tải được thông tin đơn hàng",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    android.util.Log.e("TheoDoiDonHang", "Failed to load invoice. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("TheoDoiDonHang", "Error body: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("TheoDoiDonHang", "Error reading error body", e);
                        }
                    }
                    Toast.makeText(TheoDoiDonHangActivity.this, "Không tải được thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<InvoiceResponse>> call, Throwable t) {
                android.util.Log.e("TheoDoiDonHang", "Error loading invoice", t);
                Toast.makeText(TheoDoiDonHangActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrderDetails(InvoiceResponse invoice) {
        // Mã đơn hàng
        String invoiceNumber = invoice.getInvoiceNumber();
        if (invoiceNumber != null && !invoiceNumber.isEmpty()) {
            txtMaDonHang.setText(invoiceNumber);
        } else if (invoice.get_id() != null && !invoice.get_id().isEmpty()) {
            String id = invoice.get_id();
            txtMaDonHang.setText("ĐH" + id.substring(Math.max(0, id.length() - 8)));
        } else {
            txtMaDonHang.setText("ĐH");
        }

        // Trạng thái với màu
        String status = invoice.getStatus();
        String statusLabel = getStatusLabel(status);
        txtTrangThai.setText(statusLabel);
        setStatusBadgeColor(txtTrangThai, status);

        // Hiển thị thông tin sản phẩm
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            var firstItem = invoice.getItems().get(0);
            if (firstItem.getProduct() != null) {
                txtTenSanPham.setText(firstItem.getProduct().getName());

                // Load hình ảnh sản phẩm
                String imageUrl = firstItem.getProduct().getImage();
                if (imageUrl != null && !imageUrl.trim().isEmpty() && !imageUrl.contains("example.com")) {
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .into(imgSanPham);
                }
            }
        }

        // Tổng tiền - format giống như màn hình danh sách
        Double total = invoice.getTotal();
        if (total != null && total > 0) {
            txtTongTien.setText(formatPrice(total));
        } else {
            txtTongTien.setText("0 vnd");
        }

        // Phương thức thanh toán - format giống như màn hình danh sách
        String paymentMethod = invoice.getPaymentMethod();
        String paymentLabel = getPaymentMethodLabel(paymentMethod);
        txtPhuongThucThanhToan.setText(paymentLabel);

        // Ngày tạo đơn - format giống như màn hình danh sách
        String createdAt = invoice.getCreatedAt();
        if (createdAt != null && !createdAt.isEmpty()) {
            String formattedDate = formatDate(createdAt);
            txtNgayTao.setText(formattedDate);
        } else {
            txtNgayTao.setText("");
        }

        // Hiển thị thông tin vận chuyển
        // Điểm gửi - lấy từ API nếu có, nếu không thì hiển thị "Chưa cập nhật"
        String diemGui = "Chưa cập nhật";
        // TODO: Nếu backend có field cho điểm gửi, lấy từ đó
        // Hiện tại không có field trong InvoiceResponse, nên để "Chưa cập nhật"
        txtDiemGui.setText(diemGui);

        // Điểm đến - ưu tiên từ shippingAddress, sau đó customer address
        String address = null;
        InvoiceResponse.ShippingAddress shippingAddress = invoice.getShippingAddress();

        android.util.Log.d("TheoDoiDonHang", "ShippingAddress: " + (shippingAddress != null ? "exists" : "null"));

        if (shippingAddress != null) {
            // Format địa chỉ từ object - ưu tiên các field có giá trị
            StringBuilder addressBuilder = new StringBuilder();

            // Số nhà, đường
            if (shippingAddress.getAddress() != null && !shippingAddress.getAddress().trim().isEmpty()) {
                addressBuilder.append(shippingAddress.getAddress().trim());
            }

            // Phường/Xã
            if (shippingAddress.getWard() != null && !shippingAddress.getWard().trim().isEmpty()) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(shippingAddress.getWard().trim());
            }

            // Quận/Huyện
            if (shippingAddress.getDistrict() != null && !shippingAddress.getDistrict().trim().isEmpty()) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(shippingAddress.getDistrict().trim());
            }

            // Tỉnh/Thành phố
            if (shippingAddress.getCity() != null && !shippingAddress.getCity().trim().isEmpty()) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(shippingAddress.getCity().trim());
            }

            address = addressBuilder.toString();
            android.util.Log.d("TheoDoiDonHang", "Address from shippingAddress: " + address);
        }

        // Nếu không có từ shippingAddress, thử lấy từ customer
        if ((address == null || address.trim().isEmpty()) && invoice.getCustomer() != null) {
            if (invoice.getCustomer().getAddress() != null && !invoice.getCustomer().getAddress().trim().isEmpty()) {
                address = invoice.getCustomer().getAddress().trim();
                android.util.Log.d("TheoDoiDonHang", "Address from customer: " + address);
            }
        }

        // Hiển thị địa chỉ hoặc thông báo
        if (address != null && !address.trim().isEmpty()) {
            txtDiemDen.setText(address);
        } else {
            txtDiemDen.setText("Chưa cập nhật");
            android.util.Log.w("TheoDoiDonHang", "No address found in invoice data");
        }

        // Đơn vị vận chuyển - lấy từ API nếu có, nếu không thì hiển thị "Chưa cập nhật"
        // TODO: Nếu backend có field cho đơn vị vận chuyển, lấy từ đó
        // Hiện tại không có field trong InvoiceResponse
        txtDonViVanChuyen.setText("Chưa cập nhật");

        // Cân nặng/Số lượng - tính từ items
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            int totalQuantity = 0;
            for (var item : invoice.getItems()) {
                if (item.getQuantity() != null) {
                    totalQuantity += item.getQuantity();
                }
            }
            if (totalQuantity > 0) {
                txtCanNang.setText(totalQuantity + " sản phẩm");
            } else {
                txtCanNang.setText("Chưa cập nhật");
            }
        } else {
            txtCanNang.setText("Chưa cập nhật");
        }

        // Tạo timeline từ status
        List<TimelineItem> timeline = createTimelineFromInvoice(invoice);
        timelineAdapter.setItems(timeline);

        // Hiển thị nút đánh giá nếu đơn hàng đã giao thành công
        if ("completed".equals(status) || "delivered".equals(status)) {
            // Kiểm tra xem đã đánh giá chưa
            checkAndShowReviewButton(invoice);
        } else {
            btnDanhGia.setVisibility(View.GONE);
        }
    }

    private void checkAndShowReviewButton(InvoiceResponse invoice) {
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            btnDanhGia.setVisibility(View.GONE);
            return;
        }

        // Lấy sản phẩm đầu tiên
        InvoiceResponse.Item firstItem = invoice.getItems().get(0);
        if (firstItem.getProduct() == null || firstItem.getProduct().get_id() == null) {
            btnDanhGia.setVisibility(View.GONE);
            return;
        }

        String productId = firstItem.getProduct().get_id();

        // Kiểm tra xem đã đánh giá chưa
        retrofit2.Call<java.util.List<fpoly.haideptrai.duan1.api.models.ReviewResponse>> call = reviewService.getMyReviews();
        call.enqueue(new retrofit2.Callback<java.util.List<fpoly.haideptrai.duan1.api.models.ReviewResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<fpoly.haideptrai.duan1.api.models.ReviewResponse>> call,
                                   retrofit2.Response<java.util.List<fpoly.haideptrai.duan1.api.models.ReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean hasReviewed = false;
                    for (fpoly.haideptrai.duan1.api.models.ReviewResponse review : response.body()) {
                        if (review.getProduct() != null && productId.equals(review.getProduct())) {
                            hasReviewed = true;
                            break;
                        }
                    }

                    // Chỉ hiển thị nút nếu chưa đánh giá
                    if (!hasReviewed) {
                        btnDanhGia.setVisibility(View.VISIBLE);
                    } else {
                        btnDanhGia.setVisibility(View.GONE);
                    }
                } else {
                    // Nếu không load được, vẫn hiển thị nút
                    btnDanhGia.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<fpoly.haideptrai.duan1.api.models.ReviewResponse>> call, Throwable t) {
                // Nếu lỗi, vẫn hiển thị nút
                btnDanhGia.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showReviewDialog(String productId, String orderId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Đánh giá sản phẩm");

        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);
        builder.setView(dialogView);

        android.widget.RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        com.google.android.material.textfield.TextInputEditText edtComment = dialogView.findViewById(R.id.edtComment);

        builder.setPositiveButton("Gửi đánh giá", (dialog, which) -> {
            int rating = (int) ratingBar.getRating();
            String comment = edtComment.getText() != null ? edtComment.getText().toString().trim() : "";

            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            submitReview(productId, orderId, rating, comment);
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void submitReview(String productId, String orderId, int rating, String comment) {
        fpoly.haideptrai.duan1.api.models.ReviewRequest request = new fpoly.haideptrai.duan1.api.models.ReviewRequest();
        request.setProductId(productId);
        request.setOrderId(orderId);
        request.setRating(rating);
        request.setComment(comment);

        retrofit2.Call<fpoly.haideptrai.duan1.api.models.ApiResponse<fpoly.haideptrai.duan1.api.models.ReviewResponse>> call = reviewService.createReview(request);
        call.enqueue(new retrofit2.Callback<fpoly.haideptrai.duan1.api.models.ApiResponse<fpoly.haideptrai.duan1.api.models.ReviewResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<fpoly.haideptrai.duan1.api.models.ApiResponse<fpoly.haideptrai.duan1.api.models.ReviewResponse>> call,
                                   retrofit2.Response<fpoly.haideptrai.duan1.api.models.ApiResponse<fpoly.haideptrai.duan1.api.models.ReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fpoly.haideptrai.duan1.api.models.ApiResponse<fpoly.haideptrai.duan1.api.models.ReviewResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(TheoDoiDonHangActivity.this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        btnDanhGia.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(TheoDoiDonHangActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Không thể gửi đánh giá",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TheoDoiDonHangActivity.this, "Không thể gửi đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<fpoly.haideptrai.duan1.api.models.ApiResponse<fpoly.haideptrai.duan1.api.models.ReviewResponse>> call, Throwable t) {
                Toast.makeText(TheoDoiDonHangActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<TimelineItem> createTimelineFromInvoice(InvoiceResponse invoice) {
        List<TimelineItem> timeline = new ArrayList<>();
        String status = invoice.getStatus();

        // Sử dụng thời gian từ API nếu có
        String createdAt = invoice.getCreatedAt();
        String updatedAt = invoice.getUpdatedAt();
        String currentTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());

        if (createdAt != null && !createdAt.isEmpty()) {
            try {
                // Parse ISO date format nếu có
                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                java.util.Date date = inputFormat.parse(createdAt);
                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                currentTime = outputFormat.format(date);
            } catch (Exception e) {
                // Nếu parse lỗi, dùng thời gian hiện tại
            }
        }

        // Timeline dựa trên status
        if (status == null || "pending".equals(status)) {
            timeline.add(new TimelineItem("Xác nhận đơn hàng", currentTime, true));
            timeline.add(new TimelineItem("Đưa hàng cho đơn vị vận chuyển", "", false));
            timeline.add(new TimelineItem("Đến kho chung chuyển", "", false));
            timeline.add(new TimelineItem("Giao thành công", "", false));
        } else if ("processing".equals(status) || "confirmed".equals(status)) {
            timeline.add(new TimelineItem("Xác nhận đơn hàng", currentTime, true));
            timeline.add(new TimelineItem("Đưa hàng cho đơn vị vận chuyển", updatedAt != null ? formatDate(updatedAt) : "", true));
            timeline.add(new TimelineItem("Đến kho chung chuyển", "", false));
            timeline.add(new TimelineItem("Giao thành công", "", false));
        } else if ("shipping".equals(status) || "shipped".equals(status)) {
            timeline.add(new TimelineItem("Xác nhận đơn hàng", currentTime, true));
            timeline.add(new TimelineItem("Đưa hàng cho đơn vị vận chuyển", formatDate(createdAt), true));
            timeline.add(new TimelineItem("Đến kho chung chuyển", updatedAt != null ? formatDate(updatedAt) : "", true));
            timeline.add(new TimelineItem("Giao thành công", "", false));
        } else if ("completed".equals(status) || "delivered".equals(status)) {
            timeline.add(new TimelineItem("Xác nhận đơn hàng", currentTime, true));
            timeline.add(new TimelineItem("Đưa hàng cho đơn vị vận chuyển", formatDate(createdAt), true));
            timeline.add(new TimelineItem("Đến kho chung chuyển", formatDate(updatedAt), true));
            timeline.add(new TimelineItem("Giao thành công", updatedAt != null ? formatDate(updatedAt) : currentTime, true));
        } else if ("cancelled".equals(status)) {
            timeline.add(new TimelineItem("Đơn hàng đã hủy", currentTime, true));
        } else {
            // Fallback
            timeline.add(new TimelineItem("Xác nhận đơn hàng", currentTime, true));
        }

        return timeline;
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }
        try {
            // Parse ISO 8601 format: "2025-11-17T15:41:40.507Z"
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());

            // Remove milliseconds and timezone if present
            String cleanDate = dateString.split("\\.")[0];
            if (cleanDate.contains("Z")) {
                cleanDate = cleanDate.replace("Z", "");
            }

            java.util.Date date = inputFormat.parse(cleanDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            // Fallback: return original string
            return dateString;
        }
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Chưa xác định";
        switch (status.toLowerCase()) {
            case "completed":
            case "delivered":
                return "Đã giao";
            case "pending":
            case "processing":
            case "confirmed":
                return "Đang xử lý";
            case "shipping":
            case "shipped":
                return "Đang giao";
            case "cancelled":
            case "canceled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private void setStatusBadgeColor(TextView textView, String status) {
        int colorRes;
        if (status == null) {
            colorRes = R.color.text_secondary;
        } else if ("completed".equals(status) || "delivered".equals(status)) {
            colorRes = R.color.green;
        } else if ("shipping".equals(status) || "processing".equals(status) || "pending".equals(status)) {
            colorRes = R.color.orange;
        } else if ("cancelled".equals(status)) {
            colorRes = R.color.red;
        } else {
            colorRes = R.color.text_secondary;
        }
        textView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                textView.getContext().getResources().getColor(colorRes, null)
        ));
    }

    private String getPaymentMethodLabel(String method) {
        if (method == null) return "Chưa xác định";
        switch (method.toLowerCase()) {
            case "cod":
            case "cash":
                return "Tiền mặt (COD)";
            case "zalopay":
                return "ZaloPay";
            case "momo":
                return "MoMo";
            case "transfer":
                return "Chuyển khoản";
            case "card":
                return "Thẻ";
            case "visa":
                return "VISA";
            case "mastercard":
                return "Mastercard";
            default:
                return method;
        }
    }

    private String formatPrice(Double price) {
        if (price == null || price == 0) {
            return "0 vnd";
        }
        try {
            return currency.format(price).replace("₫", "vnd");
        } catch (Exception e) {
            return String.format("%.0f vnd", price);
        }
    }
}

