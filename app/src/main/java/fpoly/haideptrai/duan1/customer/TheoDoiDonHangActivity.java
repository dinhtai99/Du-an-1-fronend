package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;
import fpoly.haideptrai.duan1.api.services.InvoiceService;
import fpoly.haideptrai.duan1.customer.adapters.TimelineAdapter;
import fpoly.haideptrai.duan1.customer.models.TimelineItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TheoDoiDonHangActivity extends AppCompatActivity {

    private ImageView imgSanPham;
    private TextView txtTenSanPham, txtDiemGui, txtDiemDen, txtDonViVanChuyen, txtCanNang;
    private RecyclerView rvTimeline;
    private ImageButton btnBack, btnMenu;
    private TimelineAdapter timelineAdapter;
    private InvoiceService invoiceService;
    private String invoiceId;

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
        loadOrderDetails();
    }

    private void initViews() {
        imgSanPham = findViewById(R.id.imgSanPham);
        txtTenSanPham = findViewById(R.id.txtTenSanPham);
        txtDiemGui = findViewById(R.id.txtDiemGui);
        txtDiemDen = findViewById(R.id.txtDiemDen);
        txtDonViVanChuyen = findViewById(R.id.txtDonViVanChuyen);
        txtCanNang = findViewById(R.id.txtCanNang);
        rvTimeline = findViewById(R.id.rvTimeline);
        btnBack = findViewById(R.id.btnBack);
        btnMenu = findViewById(R.id.btnMenu);

        btnBack.setOnClickListener(v -> finish());
        btnMenu.setOnClickListener(v -> {
            // Reload order details
            loadOrderDetails();
            Toast.makeText(this, "Đang làm mới thông tin đơn hàng...", Toast.LENGTH_SHORT).show();
        });

        timelineAdapter = new TimelineAdapter();
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        rvTimeline.setAdapter(timelineAdapter);
    }

    private void loadOrderDetails() {
        Call<InvoiceResponse> call = invoiceService.getById(invoiceId);
        call.enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InvoiceResponse invoice = response.body();
                    android.util.Log.d("TheoDoiDonHang", "Invoice loaded: " + invoice.getInvoiceNumber());
                    android.util.Log.d("TheoDoiDonHang", "ShippingAddress exists: " + (invoice.getShippingAddress() != null));
                    if (invoice.getShippingAddress() != null) {
                        android.util.Log.d("TheoDoiDonHang", "ShippingAddress details - Address: " + invoice.getShippingAddress().getAddress() +
                                ", Ward: " + invoice.getShippingAddress().getWard() +
                                ", District: " + invoice.getShippingAddress().getDistrict() +
                                ", City: " + invoice.getShippingAddress().getCity());
                    }
                    if (invoice.getCustomer() != null) {
                        android.util.Log.d("TheoDoiDonHang", "Customer address: " + invoice.getCustomer().getAddress());
                    }
                    displayOrderDetails(invoice);
                } else {
                    android.util.Log.e("TheoDoiDonHang", "Failed to load invoice. Code: " + response.code());
                    Toast.makeText(TheoDoiDonHangActivity.this, "Không tải được thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                android.util.Log.e("TheoDoiDonHang", "Error loading invoice", t);
                Toast.makeText(TheoDoiDonHangActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrderDetails(InvoiceResponse invoice) {
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

        // Hiển thị thông tin vận chuyển
        // Điểm gửi - mặc định hoặc từ staff/store
        String diemGui = "Hà Nội"; // Có thể lấy từ store info nếu có
        if (invoice.getStaff() != null) {
            // Có thể lấy từ staff location nếu có
        }
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

        // Đơn vị vận chuyển - mặc định
        txtDonViVanChuyen.setText("JnE Express");

        // Cân nặng - tính từ items
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            int totalQuantity = 0;
            for (var item : invoice.getItems()) {
                if (item.getQuantity() != null) {
                    totalQuantity += item.getQuantity();
                }
            }
            txtCanNang.setText(totalQuantity + " sản phẩm");
        } else {
            txtCanNang.setText("1Kg");
        }

        // Tạo timeline từ status
        List<TimelineItem> timeline = createTimelineFromInvoice(invoice);
        timelineAdapter.setItems(timeline);
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
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateString);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }
}