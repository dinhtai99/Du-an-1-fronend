package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
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
                    displayOrderDetails(invoice);
                } else {
                    Toast.makeText(TheoDoiDonHangActivity.this, "Không tải được thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
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

        // Hiển thị thông tin vận chuyển (nếu có)
        if (invoice.getCustomer() != null && invoice.getCustomer().getAddress() != null) {
            txtDiemDen.setText(invoice.getCustomer().getAddress());
        }
        
        // Tạo timeline từ status
        List<TimelineItem> timeline = createTimelineFromInvoice(invoice);
        timelineAdapter.setItems(timeline);
    }

    private List<TimelineItem> createTimelineFromInvoice(InvoiceResponse invoice) {
        List<TimelineItem> timeline = new ArrayList<>();
        String status = invoice.getStatus();
        String currentTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
        
        // Timeline mặc định dựa trên status
        if (status == null || "pending".equals(status)) {
            timeline.add(new TimelineItem("Xác nhận đơn hàng", currentTime, true));
        } else if ("processing".equals(status) || "shipped".equals(status)) {
            timeline.add(new TimelineItem("Xác nhận đơn hàng", currentTime, true));
            timeline.add(new TimelineItem("Đưa hàng cho đơn vị vận chuyển", currentTime, true));
            timeline.add(new TimelineItem("Đến kho chung chuyển", currentTime, false));
        } else if ("completed".equals(status) || "delivered".equals(status)) {
            timeline.add(new TimelineItem("Xác nhận đơn hàng", currentTime, true));
            timeline.add(new TimelineItem("Đưa hàng cho đơn vị vận chuyển", currentTime, true));
            timeline.add(new TimelineItem("Đến kho chung chuyển", currentTime, true));
            timeline.add(new TimelineItem("Giao thành công", currentTime, true));
        } else {
            // Fallback
            timeline.add(new TimelineItem("Xác nhận đơn hàng", currentTime, true));
        }
        
        return timeline;
    }
}

