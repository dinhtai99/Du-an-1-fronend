package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import fpoly.haideptrai.duan1.R;

public class ThanhToanActivity extends AppCompatActivity {

    private TextInputEditText edtDiaChi;
    private MaterialButton btnSuDungViTriHienTai, btnThanhToan;
    private LinearLayout layoutVisa, layoutMastercard, layoutNganHang, layoutQR, layoutCOD;
    private String selectedPaymentMethod = "cod"; // Default: COD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        edtDiaChi = findViewById(R.id.edtDiaChi);
        btnSuDungViTriHienTai = findViewById(R.id.btnSuDungViTriHienTai);
        btnThanhToan = findViewById(R.id.btnThanhToan);
        layoutVisa = findViewById(R.id.layoutVisa);
        layoutMastercard = findViewById(R.id.layoutMastercard);
        layoutNganHang = findViewById(R.id.layoutNganHang);
        layoutQR = findViewById(R.id.layoutQR);
        layoutCOD = findViewById(R.id.layoutCOD);
    }

    private void setupClickListeners() {
        btnSuDungViTriHienTai.setOnClickListener(v -> {
            // TODO: Get current location
            Toast.makeText(this, "Lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
        });

        edtDiaChi.setOnClickListener(v -> {
            // TODO: Open address picker
            Toast.makeText(this, "Chọn địa chỉ", Toast.LENGTH_SHORT).show();
        });

        layoutVisa.setOnClickListener(v -> selectPaymentMethod("visa", layoutVisa));
        layoutMastercard.setOnClickListener(v -> selectPaymentMethod("mastercard", layoutMastercard));
        layoutNganHang.setOnClickListener(v -> selectPaymentMethod("bank", layoutNganHang));
        layoutQR.setOnClickListener(v -> selectPaymentMethod("qr", layoutQR));
        layoutCOD.setOnClickListener(v -> selectPaymentMethod("cod", layoutCOD));

        btnThanhToan.setOnClickListener(v -> {
            String address = edtDiaChi.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: Process payment
            Toast.makeText(this, "Đang xử lý thanh toán...", Toast.LENGTH_SHORT).show();
        });
    }

    private void selectPaymentMethod(String method, LinearLayout selectedLayout) {
        selectedPaymentMethod = method;
        // Reset all backgrounds
        layoutVisa.setBackground(null);
        layoutMastercard.setBackground(null);
        layoutNganHang.setBackground(null);
        layoutQR.setBackground(null);
        layoutCOD.setBackground(null);
        
        // Highlight selected
        selectedLayout.setBackgroundResource(R.color/primary_blue_light);
    }
}

