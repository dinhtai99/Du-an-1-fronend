package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.customer.adapters.VoucherAdapter;
import fpoly.haideptrai.duan1.customer.models.Voucher;

public class QuanLyVoucherActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvVoucher;
    private MaterialButton btnThemVoucher, btnChinhSuaVoucher;
    
    private VoucherAdapter voucherAdapter;
    private List<Voucher> vouchers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_voucher);

        initViews();
        setupRecyclerView();
        loadVouchers();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvVoucher = findViewById(R.id.rvVoucher);
        btnThemVoucher = findViewById(R.id.btnThemVoucher);
        btnChinhSuaVoucher = findViewById(R.id.btnChinhSuaVoucher);

        btnBack.setOnClickListener(v -> finish());
        btnThemVoucher.setOnClickListener(v -> {
            // TODO: Add voucher
            Toast.makeText(this, "Thêm voucher", Toast.LENGTH_SHORT).show();
        });
        btnChinhSuaVoucher.setOnClickListener(v -> {
            // TODO: Edit voucher
            Toast.makeText(this, "Chỉnh sửa voucher", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        voucherAdapter = new VoucherAdapter(vouchers);
        rvVoucher.setLayoutManager(new LinearLayoutManager(this));
        rvVoucher.setAdapter(voucherAdapter);
    }

    private void loadVouchers() {
        // TODO: Load from API
        // Sample data
        vouchers.add(new Voucher("20/10", "giảm 10% vào ngày 20/10", "ĐK: Đơn từ 50k", 3, "Dừng hoạt động"));
        vouchers.add(new Voucher("20/10", "giảm 10% vào ngày 20/10", "ĐK: Đơn từ 50k", 3, "Dừng hoạt động"));
        vouchers.add(new Voucher("20/10", "giảm 10% vào ngày 20/10", "ĐK: Đơn từ 50k", 3, "Dừng hoạt động"));
        voucherAdapter.notifyDataSetChanged();
    }
}

