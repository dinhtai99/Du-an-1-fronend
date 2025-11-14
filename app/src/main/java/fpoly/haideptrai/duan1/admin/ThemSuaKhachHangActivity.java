package fpoly.haideptrai.duan1.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.CustomerRequest;
import fpoly.haideptrai.duan1.api.models.CustomerResponse;
import fpoly.haideptrai.duan1.api.services.CustomerService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThemSuaKhachHangActivity extends AppCompatActivity {

    private TextInputEditText edtTenKhachHang, edtSoDienThoai, edtDiaChi;
    private TextInputLayout tilTenKhachHang, tilSoDienThoai, tilDiaChi;
    private Spinner spnHangKhachHang;
    private MaterialButton btnLuu;
    private CustomerService customerService;
    private String customerId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_sua_khach_hang);

        customerId = getIntent().getStringExtra("customer_id");
        isEditMode = customerId != null && !customerId.isEmpty();

        initViews();
        setupSpinner();
        customerService = ApiClient.getClient().create(CustomerService.class);

        if (isEditMode) {
            setTitle("Sửa khách hàng");
            btnLuu.setText("Cập nhật");
            loadCustomerData();
        } else {
            setTitle("Thêm khách hàng");
            btnLuu.setText("Thêm mới");
        }

        btnLuu.setOnClickListener(v -> handleSave());
    }

    private void initViews() {
        edtTenKhachHang = findViewById(R.id.edtTenKhachHang);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtDiaChi = findViewById(R.id.edtDiaChi);
        tilTenKhachHang = findViewById(R.id.tilTenKhachHang);
        tilSoDienThoai = findViewById(R.id.tilSoDienThoai);
        tilDiaChi = findViewById(R.id.tilDiaChi);
        spnHangKhachHang = findViewById(R.id.spnHangKhachHang);
        btnLuu = findViewById(R.id.btnLuu);
    }

    private void setupSpinner() {
        String[] hangArray = {"Normal", "VIP"};
        String[] hangDisplay = {"Thường", "VIP"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hangDisplay);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnHangKhachHang.setAdapter(adapter);
    }

    private void loadCustomerData() {
        Call<CustomerResponse> call = customerService.getById(customerId);
        call.enqueue(new Callback<CustomerResponse>() {
            @Override
            public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CustomerResponse customer = response.body();
                    edtTenKhachHang.setText(customer.getName());
                    edtSoDienThoai.setText(customer.getPhone());
                    edtDiaChi.setText(customer.getAddress());
                    if (customer.getType() != null) {
                        spnHangKhachHang.setSelection(customer.getType().equals("VIP") ? 1 : 0);
                    }
                }
            }

            @Override
            public void onFailure(Call<CustomerResponse> call, Throwable t) {
                Toast.makeText(ThemSuaKhachHangActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSave() {
        String tenKhachHang = edtTenKhachHang.getText().toString().trim();
        String soDienThoai = edtSoDienThoai.getText().toString().trim();
        String diaChi = edtDiaChi.getText().toString().trim();
        String hang = spnHangKhachHang.getSelectedItemPosition() == 1 ? "VIP" : "Normal";

        tilTenKhachHang.setError(null);

        if (TextUtils.isEmpty(tenKhachHang)) {
            tilTenKhachHang.setError("Vui lòng nhập tên khách hàng");
            edtTenKhachHang.requestFocus();
            return;
        }

        btnLuu.setEnabled(false);
        btnLuu.setText("Đang xử lý...");

        CustomerRequest request = new CustomerRequest();
        request.setName(tenKhachHang);
        request.setPhone(soDienThoai);
        request.setAddress(diaChi);
        request.setType(hang);
        request.setActive(true);

        Call<ApiResponse<CustomerResponse>> call;
        if (isEditMode) {
            call = customerService.updateCustomer(customerId, request);
        } else {
            call = customerService.createCustomer(request);
        }

        call.enqueue(new Callback<ApiResponse<CustomerResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CustomerResponse>> call, Response<ApiResponse<CustomerResponse>> response) {
                btnLuu.setEnabled(true);
                btnLuu.setText(isEditMode ? "Cập nhật" : "Thêm mới");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ThemSuaKhachHangActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ThemSuaKhachHangActivity.this, isEditMode ? "Cập nhật thất bại" : "Thêm mới thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CustomerResponse>> call, Throwable t) {
                btnLuu.setEnabled(true);
                btnLuu.setText(isEditMode ? "Cập nhật" : "Thêm mới");
                Toast.makeText(ThemSuaKhachHangActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
