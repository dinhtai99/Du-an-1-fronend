package fpoly.haideptrai.duan1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fpoly.haideptrai.duan1.adapters.KhachHangAdapter;
import fpoly.haideptrai.duan1.api.models.CustomerResponse;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.CustomerListResponse;
import fpoly.haideptrai.duan1.api.models.CustomerResponse;
import fpoly.haideptrai.duan1.api.services.CustomerService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DanhSachKhachHangActivity extends AppCompatActivity {

    private static final String TAG = "CUSTOMERS";

    private RecyclerView rvKhachHang;
    private KhachHangAdapter adapter;
    private CustomerService customerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_khach_hang);

        rvKhachHang = findViewById(R.id.rvKhachHang);
        adapter = new KhachHangAdapter();
        rvKhachHang.setLayoutManager(new LinearLayoutManager(this));
        rvKhachHang.setAdapter(adapter);
        
        // Nút quay lại
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter.setOnCustomerClickListener(new KhachHangAdapter.OnCustomerClickListener() {
            @Override
            public void onClick(CustomerResponse customer) {
                // TODO: Chuyển sang ChiTietKhachHangActivity khi tạo xong
                Intent intent = new Intent(DanhSachKhachHangActivity.this, ThemSuaKhachHangActivity.class);
                intent.putExtra("customer_id", customer.get_id());
                startActivity(intent);
            }

            @Override
            public void onLongClick(CustomerResponse customer) {
                // Có thể thêm menu xóa
            }
        });

        customerService = ApiClient.getClient().create(CustomerService.class);
        fetchCustomers(null);
    }

    private void fetchCustomers(String search) {
        Call<CustomerListResponse> call = customerService.getCustomers(search, null, null, 1, 50);
        call.enqueue(new Callback<CustomerListResponse>() {
            @Override
            public void onResponse(Call<CustomerListResponse> call, Response<CustomerListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CustomerResponse> list = response.body().getCustomers();
                    Log.i(TAG, "Loaded customers: " + (list != null ? list.size() : 0));
                    adapter.setItems(list);
                } else {
                    String msg = "Không tải được khách hàng (" + response.code() + ")";
                    Log.e(TAG, msg);
                    Toast.makeText(DanhSachKhachHangActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CustomerListResponse> call, Throwable t) {
                Log.e(TAG, "API error: " + t.getMessage(), t);
                Toast.makeText(DanhSachKhachHangActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}










