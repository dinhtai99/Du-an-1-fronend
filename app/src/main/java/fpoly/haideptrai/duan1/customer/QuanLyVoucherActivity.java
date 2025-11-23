package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import fpoly.haideptrai.duan1.utils.SessionManager;
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
    private SessionManager sessionManager;

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
        sessionManager = new SessionManager(this);
    }

    private void setupRecyclerView() {
        voucherAdapter = new VoucherAdapter(vouchers);
        rvVoucher.setLayoutManager(new LinearLayoutManager(this));
        rvVoucher.setAdapter(voucherAdapter);

        // Thêm click listener để xem chi tiết voucher
        voucherAdapter.setOnVoucherClickListener(new VoucherAdapter.OnVoucherClickListener() {
            @Override
            public void onVoucherClick(int position) {
                if (position >= 0 && position < voucherResponses.size()) {
                    VoucherResponse selectedVoucher = voucherResponses.get(position);
                    if (selectedVoucher != null) {
                        // Hiển thị chi tiết voucher trong dialog
                        showVoucherDetailDialog(selectedVoucher);
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

        // ✅ Ưu tiên gọi API /api/vouchers/my để lấy voucher của user hiện tại
        // Backend sẽ tự lấy user từ JWT token
        Call<ApiResponse<List<VoucherResponse>>> call = voucherService.getMyVouchers();
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

                        // Lọc voucher theo điều kiện: thời gian, số lượng, trạng thái, và user
                        Date currentDate = new Date();
                        String currentUserId = sessionManager.getMongoUserId();
                        android.util.Log.d("Voucher", "=== FILTERING VOUCHERS ===");
                        android.util.Log.d("Voucher", "Current user ID: " + currentUserId);
                        android.util.Log.d("Voucher", "Total vouchers from API: " + apiVouchers.size());

                        int eligibleCount = 0;
                        for (VoucherResponse voucherResponse : apiVouchers) {
                            // Kiểm tra voucher có hợp lệ không (bao gồm kiểm tra user)
                            if (isVoucherEligible(voucherResponse, currentDate, currentUserId)) {
                                Voucher voucher = convertToVoucher(voucherResponse);
                                vouchers.add(voucher);
                                voucherResponses.add(voucherResponse); // Lưu VoucherResponse gốc
                                eligibleCount++;
                            }
                        }
                        android.util.Log.d("Voucher", "Eligible vouchers after filtering: " + eligibleCount);

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
                    // ❌ API trả về lỗi - thử fallback về endpoint /api/vouchers
                    // Nếu là 404 (not found) hoặc 500 (server error), fallback
                    if (response.code() == 404 || response.code() == 500) {
                        // Endpoint /api/vouchers/my không tồn tại hoặc có lỗi, fallback về /api/vouchers
                        android.util.Log.d("Voucher", "Endpoint /api/vouchers/my returned " + response.code() + ", falling back to /api/vouchers");
                        loadVouchersFallback();
                    } else {
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
            }

            @Override
            public void onFailure(Call<ApiResponse<List<VoucherResponse>>> call, Throwable t) {
                // ❌ Lỗi kết nối - thử fallback
                android.util.Log.d("Voucher", "Failed to call /api/vouchers/my, falling back: " + t.getMessage());
                loadVouchersFallback();
            }
        });
    }

    /**
     * Fallback: Load voucher từ endpoint /api/vouchers (nếu /api/vouchers/my không tồn tại)
     * Backend sẽ tự filter theo JWT token nếu có
     */
    private void loadVouchersFallback() {
        progressBar.setVisibility(View.VISIBLE);
        vouchers.clear();
        voucherAdapter.notifyDataSetChanged();

        // Gọi endpoint /api/vouchers với active=true để lấy voucher đang hoạt động
        // Backend sẽ tự filter theo user từ JWT token nếu có
        Call<ApiResponse<List<VoucherResponse>>> call = voucherService.getVouchers(null, true);
        call.enqueue(new Callback<ApiResponse<List<VoucherResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<VoucherResponse>>> call, Response<ApiResponse<List<VoucherResponse>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<VoucherResponse>> apiResponse = response.body();

                    if (apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        List<VoucherResponse> apiVouchers = apiResponse.getData();
                        vouchers.clear();
                        voucherResponses.clear();

                        // Lọc voucher theo điều kiện: thời gian, số lượng, trạng thái, và user
                        Date currentDate = new Date();
                        String currentUserId = sessionManager.getMongoUserId();
                        android.util.Log.d("Voucher", "=== FILTERING VOUCHERS (FALLBACK) ===");
                        android.util.Log.d("Voucher", "Current user ID: " + currentUserId);
                        android.util.Log.d("Voucher", "Total vouchers from API: " + apiVouchers.size());

                        int eligibleCount = 0;
                        for (VoucherResponse voucherResponse : apiVouchers) {
                            if (isVoucherEligible(voucherResponse, currentDate, currentUserId)) {
                                Voucher voucher = convertToVoucher(voucherResponse);
                                vouchers.add(voucher);
                                voucherResponses.add(voucherResponse);
                                eligibleCount++;
                            }
                        }
                        android.util.Log.d("Voucher", "Eligible vouchers after filtering: " + eligibleCount);

                        voucherAdapter.notifyDataSetChanged();
                        android.util.Log.d("Voucher", "Loaded " + vouchers.size() + " vouchers from fallback API");
                    } else {
                        vouchers.clear();
                        voucherResponses.clear();
                        voucherAdapter.notifyDataSetChanged();
                        Toast.makeText(QuanLyVoucherActivity.this,
                                "Hiện tại không có voucher nào",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    vouchers.clear();
                    voucherResponses.clear();
                    voucherAdapter.notifyDataSetChanged();
                    Toast.makeText(QuanLyVoucherActivity.this,
                            "Không thể tải voucher",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<VoucherResponse>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                vouchers.clear();
                voucherResponses.clear();
                voucherAdapter.notifyDataSetChanged();
                Toast.makeText(QuanLyVoucherActivity.this,
                        "Không thể kết nối đến server",
                        Toast.LENGTH_LONG).show();
                android.util.Log.e("Voucher", "Fallback API error: " + t.getMessage(), t);
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

    /**
     * Kiểm tra voucher có đủ điều kiện để hiển thị cho user không
     */
    private boolean isVoucherEligible(VoucherResponse voucher, Date currentDate, String currentUserId) {
        // 1. Kiểm tra trạng thái
        if (voucher.getStatus() == null || !"active".equals(voucher.getStatus())) {
            android.util.Log.d("Voucher", "Voucher " + voucher.getCode() + " is not active (status: " + voucher.getStatus() + ")");
            return false; // Voucher không active
        }

        // 2. Kiểm tra user có được phép dùng voucher không
        if (voucher.getApplicableUsers() != null && !voucher.getApplicableUsers().isEmpty()) {
            // Voucher có giới hạn user - chỉ hiển thị nếu user hiện tại nằm trong danh sách
            android.util.Log.d("Voucher", "Voucher " + voucher.getCode() + " has " + voucher.getApplicableUsers().size() + " applicable users");

            if (currentUserId == null || currentUserId.isEmpty()) {
                android.util.Log.d("Voucher", "User not logged in, cannot use restricted voucher " + voucher.getCode());
                return false; // User chưa đăng nhập hoặc không có ID
            }

            boolean isUserEligible = false;
            for (Object userItem : voucher.getApplicableUsers()) {
                if (userItem != null) {
                    String userIdStr = null;

                    // Xử lý cả string và object
                    if (userItem instanceof String) {
                        userIdStr = (String) userItem;
                        android.util.Log.d("Voucher", "  - Found string user ID: " + userIdStr);
                    } else if (userItem instanceof java.util.Map) {
                        // Nếu là object (Map), lấy _id
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> userObj = (java.util.Map<String, Object>) userItem;
                        Object idObj = userObj.get("_id");
                        if (idObj != null) {
                            userIdStr = idObj.toString();
                            android.util.Log.d("Voucher", "  - Found object user ID: " + userIdStr);
                        }
                    }

                    if (userIdStr != null && userIdStr.equals(currentUserId)) {
                        isUserEligible = true;
                        android.util.Log.d("Voucher", "✓ User " + currentUserId + " is eligible for voucher " + voucher.getCode());
                        break;
                    }
                }
            }

            if (!isUserEligible) {
                android.util.Log.d("Voucher", "✗ User " + currentUserId + " not eligible for voucher " + voucher.getCode() +
                        " (applicableUsers count: " + voucher.getApplicableUsers().size() + ")");
                return false; // User không được phép dùng voucher này
            }
        } else {
            android.util.Log.d("Voucher", "Voucher " + voucher.getCode() + " is available for all users (no applicableUsers restriction)");
        }
        // Nếu applicableUsers là null hoặc empty, voucher dành cho tất cả user

        // 3. Kiểm tra số lượng còn lại
        int quantity = voucher.getQuantity() != null ? voucher.getQuantity() : 0;
        int used = voucher.getUsed() != null ? voucher.getUsed() : 0;
        if (quantity - used <= 0) {
            return false; // Đã hết voucher
        }

        // 4. Kiểm tra thời gian hiệu lực
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // Kiểm tra startDate
            if (voucher.getStartDate() != null && !voucher.getStartDate().isEmpty()) {
                Date startDate = dateFormat.parse(voucher.getStartDate());
                if (currentDate.before(startDate)) {
                    return false; // Chưa đến thời gian bắt đầu
                }
            }

            // Kiểm tra endDate
            if (voucher.getEndDate() != null && !voucher.getEndDate().isEmpty()) {
                Date endDate = dateFormat.parse(voucher.getEndDate());
                // Thêm 1 ngày để bao gồm cả ngày cuối
                endDate.setTime(endDate.getTime() + 24 * 60 * 60 * 1000);
                if (currentDate.after(endDate)) {
                    return false; // Đã hết hạn
                }
            }
        } catch (Exception e) {
            android.util.Log.e("Voucher", "Error checking voucher date: " + e.getMessage());
            // Nếu lỗi parse date, vẫn hiển thị voucher (backend sẽ validate)
        }

        return true; // Voucher hợp lệ
    }

    private void showVoucherDetailDialog(VoucherResponse voucher) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chi tiết Voucher");

        // Tạo view để hiển thị chi tiết
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_voucher_detail, null);

        TextView txtCode = dialogView.findViewById(R.id.txtCode);
        TextView txtName = dialogView.findViewById(R.id.txtName);
        TextView txtDiscount = dialogView.findViewById(R.id.txtDiscount);
        TextView txtMinOrder = dialogView.findViewById(R.id.txtMinOrder);
        TextView txtQuantity = dialogView.findViewById(R.id.txtQuantity);
        TextView txtUsed = dialogView.findViewById(R.id.txtUsed);
        TextView txtDateRange = dialogView.findViewById(R.id.txtDateRange);
        TextView txtStatus = dialogView.findViewById(R.id.txtStatus);

        // Hiển thị thông tin
        txtCode.setText("Mã: " + (voucher.getCode() != null ? voucher.getCode() : "N/A"));
        txtName.setText("Tên: " + (voucher.getName() != null ? voucher.getName() : "Không có tên"));
        txtDiscount.setText("Giảm giá: " + formatDiscount(voucher.getDiscount(), voucher.getDiscountType()));

        if (voucher.getMinOrderAmount() != null && voucher.getMinOrderAmount() > 0) {
            txtMinOrder.setText("Đơn hàng tối thiểu: " + formatPrice(voucher.getMinOrderAmount()));
        } else {
            txtMinOrder.setText("Đơn hàng tối thiểu: Không có");
        }

        int quantity = voucher.getQuantity() != null ? voucher.getQuantity() : 0;
        int used = voucher.getUsed() != null ? voucher.getUsed() : 0;
        int remaining = Math.max(0, quantity - used);
        txtQuantity.setText("Số lượng: " + quantity);
        txtUsed.setText("Đã dùng: " + used + " | Còn lại: " + remaining);

        String dateRange = formatDateRange(voucher.getStartDate(), voucher.getEndDate());
        if (!"N/A".equals(dateRange)) {
            // Format lại với năm đầy đủ
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                if (voucher.getStartDate() != null && !voucher.getStartDate().isEmpty()) {
                    Date start = inputFormat.parse(voucher.getStartDate());
                    if (voucher.getEndDate() != null && !voucher.getEndDate().isEmpty()) {
                        Date end = inputFormat.parse(voucher.getEndDate());
                        txtDateRange.setText("Thời gian: " + outputFormat.format(start) + " - " + outputFormat.format(end));
                    } else {
                        txtDateRange.setText("Thời gian: Từ " + outputFormat.format(start));
                    }
                } else {
                    txtDateRange.setText("Thời gian: N/A");
                }
            } catch (Exception e) {
                txtDateRange.setText("Thời gian: " + dateRange);
            }
        } else {
            txtDateRange.setText("Thời gian: N/A");
        }

        String status = "Hoạt động";
        if (voucher.getStatus() != null) {
            if ("inactive".equals(voucher.getStatus()) || "expired".equals(voucher.getStatus())) {
                status = "Dừng hoạt động";
            } else if ("active".equals(voucher.getStatus())) {
                status = "Hoạt động";
            } else {
                status = voucher.getStatus();
            }
        }
        txtStatus.setText("Trạng thái: " + status);

        builder.setView(dialogView);
        builder.setPositiveButton("Đóng", null);

        // Nếu được gọi từ activity khác (như ThanhToanActivity), thêm nút "Sử dụng"
        if (getCallingActivity() != null) {
            builder.setNeutralButton("Sử dụng voucher này", (dialog, which) -> {
                if (voucher.getCode() != null) {
                    android.content.Intent resultIntent = new android.content.Intent();
                    resultIntent.putExtra("voucherCode", voucher.getCode());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(QuanLyVoucherActivity.this, "Voucher không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        }

        builder.show();
    }
}

