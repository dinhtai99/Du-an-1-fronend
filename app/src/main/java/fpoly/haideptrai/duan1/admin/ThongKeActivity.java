package fpoly.haideptrai.duan1.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.services.StatisticsService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThongKeActivity extends AppCompatActivity {

    private static final String TAG = "STATISTICS";
    private TextView txtTongDoanhThu, txtTongHoaDon;
    private StatisticsService statisticsService;
    private NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);

        txtTongDoanhThu = findViewById(R.id.txtTongDoanhThu);
        txtTongHoaDon = findViewById(R.id.txtTongHoaDon);

        statisticsService = ApiClient.getClient().create(StatisticsService.class);
        loadOverview();
    }

    private void loadOverview() {
        Call<ApiResponse<Object>> call = statisticsService.getOverview(null, null);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Parse response - backend trả về object với các field
                        // Cần parse JSON hoặc dùng Gson để convert
                        // Tạm thời hiển thị placeholder, có thể cải thiện sau
                        Log.i(TAG, "Statistics loaded");
                        // TODO: Parse và hiển thị dữ liệu thực tế từ response.body().getData()
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing statistics", e);
                    }
                } else {
                    Toast.makeText(ThongKeActivity.this, "Không tải được thống kê", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "API error: " + t.getMessage(), t);
                Toast.makeText(ThongKeActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

