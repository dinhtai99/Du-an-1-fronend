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
        // TODO: Use actual API endpoint to get invoice by ID
        // For now, create sample timeline
        List<TimelineItem> timeline = new ArrayList<>();
        timeline.add(new TimelineItem("Xác nhận đơn hàng", "22/11/2021 12:30pm", true));
        timeline.add(new TimelineItem("Đưa hàng cho đơn vị vận chuyển", "22/11/2021 12:30pm", true));
        timeline.add(new TimelineItem("Đến kho chung chuyển", "22/11/2021 12:30pm", true));
        timeline.add(new TimelineItem("Giao thành công", "23/11/2021 12:30pm", true));
        timelineAdapter.setItems(timeline);
    }
}

