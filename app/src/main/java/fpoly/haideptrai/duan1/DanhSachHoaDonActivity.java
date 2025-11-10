package fpoly.haideptrai.duan1;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fpoly.haideptrai.duan1.adapters.HoaDonAdapter;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.InvoiceListResponse;
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;
import fpoly.haideptrai.duan1.api.services.InvoiceService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DanhSachHoaDonActivity extends AppCompatActivity {

    private static final String TAG = "INVOICES";

    private RecyclerView rvHoaDon;
    private SearchView searchView;
    private HoaDonAdapter adapter;
    private InvoiceService invoiceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_hoa_don);

        rvHoaDon = findViewById(R.id.rvHoaDon);
        searchView = findViewById(R.id.searchViewHoaDon);
        adapter = new HoaDonAdapter();
        rvHoaDon.setLayoutManager(new LinearLayoutManager(this));
        rvHoaDon.setAdapter(adapter);

        invoiceService = ApiClient.getClient().create(InvoiceService.class);
        fetchInvoices(null);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchInvoices(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void fetchInvoices(String search) {
        Call<InvoiceListResponse> call = invoiceService.getInvoices(search, null, null, null, null, null, null, 1, 50);
        call.enqueue(new Callback<InvoiceListResponse>() {
            @Override
            public void onResponse(Call<InvoiceListResponse> call, Response<InvoiceListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<InvoiceResponse> list = response.body().getInvoices();
                    Log.i(TAG, "Loaded invoices: " + (list != null ? list.size() : 0));
                    adapter.setItems(list);
                } else {
                    String msg = "Không tải được hóa đơn (" + response.code() + ")";
                    Log.e(TAG, msg);
                    Toast.makeText(DanhSachHoaDonActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InvoiceListResponse> call, Throwable t) {
                Log.e(TAG, "API error: " + t.getMessage(), t);
                Toast.makeText(DanhSachHoaDonActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}










