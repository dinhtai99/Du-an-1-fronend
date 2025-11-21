package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.VoucherResponse;
import fpoly.haideptrai.duan1.api.services.VoucherService;
import fpoly.haideptrai.duan1.customer.adapters.VoucherAdapter;
import fpoly.haideptrai.duan1.customer.models.Voucher;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyVoucherActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvVoucher;
    private MaterialButton btnThemVoucher, btnChinhSuaVoucher;
    private ProgressBar progressBar;

    private VoucherAdapter voucherAdapter;
    private List<Voucher> vouchers = new ArrayList<>();
    private List<VoucherResponse> voucherResponses = new ArrayList<>(); // Lưu VoucherResponse gốc để lấy code
    private VoucherService voucherService;

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
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());

        // Ẩn các button không cần thiết cho khách hàng
        btnThemVoucher.setVisibility(View.GONE);
        btnChinhSuaVoucher.setVisibility(View.GONE);

        // Nếu muốn có nút "Làm mới" để reload voucher, có thể bỏ comment dòng dưới
        // btnThemVoucher.setText("Làm mới");
        // btnThemVoucher.setVisibility(View.VISIBLE);
        // btnThemVoucher.setOnClickListener(v -> loadVouchers());

        voucherService = ApiClient.getClient().create(VoucherService.class);
    }

    private void setupRecyclerView() {
        voucherAdapter = new VoucherAdapter(vouchers);
        rvVoucher.setLayoutManager(new LinearLayoutManager(this));
        rvVoucher.setAdapter(voucherAdapter);

        // Thêm click listener để chọn voucher
        voucherAdapter.setOnVoucherClickListener(new VoucherAdapter.OnVoucherClickListener() {
            @Override
            public void onVoucherClick(int position) {
                if (position >= 0 && position < voucherResponses.size()) {
                    VoucherResponse selectedVoucher = voucherResponses.get(position);
                    if (selectedVoucher != null && selectedVoucher.getCode() != null) {
                        // Trả về voucher code cho activity gọi
                        android.content.Intent resultIntent = new android.content.Intent();
                        resultIntent.putExtra("voucherCode", selectedVoucher.getCode());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(QuanLyVoucherActivity.this, "Voucher không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void loadVouchers() {
        progressBar.setVisibility(View.VISIBLE);
        vouchers.clear();
        voucherAdapter.notifyDataSetChanged();

        // ✅ Load voucher từ API (lấy tất cả voucher có status=1)
        // Không dùng active=true để lấy tất cả voucher, không filter theo thời gian
        // Nếu muốn chỉ lấy voucher đang hoạt động, dùng active=true
        Call<ApiResponse<List<VoucherResponse>>> call = voucherService.getVouchers(null, null);
        call.enqueue(new Callback<ApiResponse<List<VoucherResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<VoucherResponse>>> call, Response<ApiResponse<List<VoucherResponse>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<VoucherResponse>> apiResponse = response.body();

                    if (apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        // ✅ Có voucher từ API
                        List<VoucherResponse> apiVouchers = apiResponse.getData();
                        vouchers.clear();
                        voucherResponses.clear(); // Clear danh sách cũ

                        for (VoucherResponse voucherResponse : apiVouchers) {
                            Voucher voucher = convertToVoucher(voucherResponse);
                            vouchers.add(voucher);
                            voucherResponses.add(voucherResponse); // Lưu VoucherResponse gốc
                        }

                        voucherAdapter.notifyDataSetChanged();

                        // Log để debug
                        android.util.Log.d("Voucher", "Loaded " + vouchers.size() + " vouchers from API");
                    } else {
                        // ✅ Không có voucher nào (empty list)
                        vouchers.clear();
                        voucherResponses.clear();
                        voucherAdapter.notifyDataSetChanged();
                        Toast.makeText(QuanLyVoucherActivity.this,
                                "Hiện tại không có voucher nào",
                                Toast.LENGTH_SHORT).show();
                        android.util.Log.d("Voucher", "No vouchers found in API");
                    }
                } else {
                    // ❌ API trả về lỗi
                    vouchers.clear();
                    voucherResponses.clear();
                    voucherAdapter.notifyDataSetChanged();

                    String errorMsg = "Không thể tải voucher";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            errorMsg = "Lỗi: " + response.code();
                        } catch (Exception e) {
                            errorMsg = "Lỗi kết nối API";
                        }
                    }

                    Toast.makeText(QuanLyVoucherActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    android.util.Log.e("Voucher", "API error: " + response.code() + " - " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<VoucherResponse>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);

                // ❌ Lỗi kết nối
                vouchers.clear();
                voucherResponses.clear();
                voucherAdapter.notifyDataSetChanged();

                String errorMsg = "Không thể kết nối đến server";
                if (t.getMessage() != null) {
                    errorMsg = "Lỗi: " + t.getMessage();
                }

                Toast.makeText(QuanLyVoucherActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                android.util.Log.e("Voucher", "Network error: " + t.getMessage(), t);
            }
        });
    }

    private Voucher convertToVoucher(VoucherResponse response) {
        // ✅ Format ngày: Ưu tiên format từ startDate/endDate, nếu không có thì dùng code
        String ngay = formatDateRange(response.getStartDate(), response.getEndDate());
        if ("N/A".equals(ngay) && response.getCode() != null) {
            ngay = response.getCode();
        }

        // ✅ Format mức giảm giá: Ưu tiên dùng name, nếu không có thì dùng description, nếu không có thì format từ discount
        String mucGiamGia;
        if (response.getName() != null && !response.getName().isEmpty()) {
            mucGiamGia = response.getName();
        } else if (response.getDescription() != null && !response.getDescription().isEmpty()) {
            mucGiamGia = response.getDescription();
        } else if (response.getDiscount() != null) {
            mucGiamGia = formatDiscount(response.getDiscount(), response.getDiscountType());
        } else {
            mucGiamGia = "Không có mô tả";
        }

        // ✅ Format điều kiện - hiển thị rõ ràng điều kiện sử dụng
        String dieuKien;
        Double minOrderAmount = response.getMinOrderAmount();

        if (minOrderAmount != null && minOrderAmount > 0) {
            dieuKien = "Điều kiện: Đơn hàng tối thiểu " + formatPrice(minOrderAmount);
        } else {
            dieuKien = "Áp dụng cho mọi đơn hàng";
        }

        // ✅ Số lượng còn lại
        int soLuong = 0;
        if (response.getQuantity() != null && response.getUsed() != null) {
            soLuong = Math.max(0, response.getQuantity() - response.getUsed());
        } else if (response.getQuantity() != null) {
            soLuong = response.getQuantity();
        }

        // ✅ Trạng thái
        String trangThai = "Hoạt động";
        if (response.getStatus() != null) {
            if ("inactive".equals(response.getStatus()) || "expired".equals(response.getStatus())) {
                trangThai = "Dừng hoạt động";
            } else if ("active".equals(response.getStatus())) {
                trangThai = "Hoạt động";
            }
        }

        return new Voucher(ngay, mucGiamGia, dieuKien, soLuong, trangThai);
    }

    private String formatDateRange(String startDate, String endDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

            if (startDate != null && !startDate.isEmpty()) {
                Date start = inputFormat.parse(startDate);
                if (endDate != null && !endDate.isEmpty()) {
                    Date end = inputFormat.parse(endDate);
                    // Nếu cùng ngày, chỉ hiển thị 1 ngày
                    if (start.equals(end)) {
                        return outputFormat.format(start);
                    }
                    return outputFormat.format(start) + " - " + outputFormat.format(end);
                }
                return outputFormat.format(start);
            }
        } catch (Exception e) {
            android.util.Log.e("Voucher", "Error parsing date: " + e.getMessage());
        }
        return "N/A";
    }

    private String formatDiscount(Double discount, String type) {
        if (discount == null) return "0%";

        if ("percentage".equals(type)) {
            return "giảm " + discount.intValue() + "%";
        } else {
            return "giảm " + formatPrice(discount);
        }
    }
    private String formatPrice(Double price) {
        if (price == null) return "0 vnd";
        return String.format(Locale.getDefault(), "%.0f vnd", price);
    }
}