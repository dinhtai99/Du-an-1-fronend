package fpoly.haideptrai.duan1.customer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.InvoiceItemRequest;
import fpoly.haideptrai.duan1.api.models.InvoiceRequest;
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;
import fpoly.haideptrai.duan1.api.models.MoMoCreateRequest;
import fpoly.haideptrai.duan1.api.models.MoMoCreateResponse;
import fpoly.haideptrai.duan1.api.models.ZaloPayCreateRequest;
import fpoly.haideptrai.duan1.api.models.ZaloPayCreateResponse;
import fpoly.haideptrai.duan1.api.models.UserResponse;
import fpoly.haideptrai.duan1.api.services.InvoiceService;
import fpoly.haideptrai.duan1.api.services.PaymentService;
import fpoly.haideptrai.duan1.api.services.UserService;
import fpoly.haideptrai.duan1.api.services.VoucherService;
import fpoly.haideptrai.duan1.api.models.VoucherResponse;
import fpoly.haideptrai.duan1.customer.models.CartItem;
import fpoly.haideptrai.duan1.utils.CartManager;
import fpoly.haideptrai.duan1.utils.SessionManager;
import fpoly.haideptrai.duan1.utils.ToastManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// ZaloPay SDK
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.listeners.PayOrderListener;

// MoMo Payment SDK - COMMENT TẠM THỜI VÌ SDK KHÔNG TẢI ĐƯỢC
// TODO: Tải SDK thủ công từ https://developers.momo.vn/ và thêm vào libs/
// Sau đó uncomment các dòng sau:
// import vn.momo.momo_partner.MoMoPayment;
// import vn.momo.momo_partner.MoMoPaymentResponse;
// Hoặc có thể là:
// import com.momo.momo_partner.MoMoPayment;
// import com.momo.momo_partner.MoMoPaymentResponse;

public class ThanhToanActivity extends AppCompatActivity {

    // ZaloPay Configuration
    // TODO: Thay đổi APP_ID theo thông tin từ ZaloPay Developer Portal
    // Sandbox: 2554 (phải khớp với backend .env)
    // Production: Lấy từ ZaloPay Developer Portal
    private static final int ZALOPAY_APP_ID = 2554; // App ID sandbox
    
    // MoMo Configuration
    // TODO: Thay đổi các thông tin sau theo thông tin từ MoMo Developer Portal
    private static final String MOMO_MERCHANT_NAME = "Shop THB"; // Tên thương nhân
    private static final String MOMO_MERCHANT_CODE = "MOMO_TEST"; // Mã thương nhân (lấy từ MoMo)
    private static final int MOMO_REQUEST_CODE = 1002; // Request code cho MoMo payment

    private TextInputEditText edtDiaChi, edtVoucherCode;
    private MaterialButton btnSuDungViTriHienTai, btnThanhToan, btnApDungVoucher, btnChonVoucher;
    private android.widget.TextView txtVoucherInfo;
    private LinearLayout layoutVisa, layoutMastercard, layoutNganHang, layoutQR, layoutZaloPay, layoutMoMo, layoutCOD;
    private String selectedPaymentMethod = "COD"; // Default: COD (uppercase)
    
    private InvoiceService invoiceService;
    private PaymentService paymentService;
    private UserService userService;
    private VoucherService voucherService;
    
    // Voucher
    private VoucherResponse selectedVoucher;
    private String voucherCode;
    private CartManager cartManager;
    private SessionManager sessionManager;
    private List<CartItem> cartItems;
    private long lastClickTime = 0;
    private static final long CLICK_DELAY = 1000; // 1 second
    
    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        initViews();
        invoiceService = ApiClient.getClient().create(InvoiceService.class);
        paymentService = ApiClient.getClient().create(PaymentService.class);
        userService = ApiClient.getClient().create(UserService.class);
        voucherService = ApiClient.getClient().create(VoucherService.class);
        cartManager = new CartManager(this);
        sessionManager = new SessionManager(this);
        
        // Lấy danh sách sản phẩm đã chọn từ SharedPreferences (nếu có)
        // Nếu không có thì load toàn bộ giỏ hàng (fallback)
        cartItems = loadSelectedItems();
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // ZaloPay SDK đã được khởi tạo trong MyApplication.onCreate()
        // Nếu cần re-init với AppID khác, có thể gọi lại:
        // ZaloPaySDK.init(ZALOPAY_APP_ID, Environment.SANDBOX);
        
        // MoMo SDK sẽ được khởi tạo khi gọi requestPayment()
        
        setupClickListeners();
        
        // Xử lý deep link từ ZaloPay app
        handleZaloPayDeepLink();
    }

    private void initViews() {
        edtDiaChi = findViewById(R.id.edtDiaChi);
        edtVoucherCode = findViewById(R.id.edtVoucherCode);
        btnSuDungViTriHienTai = findViewById(R.id.btnSuDungViTriHienTai);
        btnThanhToan = findViewById(R.id.btnThanhToan);
        btnApDungVoucher = findViewById(R.id.btnApDungVoucher);
        btnChonVoucher = findViewById(R.id.btnChonVoucher);
        txtVoucherInfo = findViewById(R.id.txtVoucherInfo);
        layoutVisa = findViewById(R.id.layoutVisa);
        layoutMastercard = findViewById(R.id.layoutMastercard);
        layoutNganHang = findViewById(R.id.layoutNganHang);
        layoutQR = findViewById(R.id.layoutQR);
        layoutZaloPay = findViewById(R.id.layoutZaloPay);
        layoutMoMo = findViewById(R.id.layoutMoMo);
        layoutCOD = findViewById(R.id.layoutCOD);
    }

    private void setupClickListeners() {
        btnSuDungViTriHienTai.setOnClickListener(v -> {
            if (isClickTooFast()) return;
            getCurrentLocation();
        });

        edtDiaChi.setOnClickListener(v -> {
            if (isClickTooFast()) return;
            // TODO: Open address picker
            showToast("Chọn địa chỉ");
        });

        layoutVisa.setOnClickListener(v -> selectPaymentMethod("VISA", layoutVisa));
        layoutMastercard.setOnClickListener(v -> selectPaymentMethod("MASTERCARD", layoutMastercard));
        layoutNganHang.setOnClickListener(v -> selectPaymentMethod("BANK", layoutNganHang));
        layoutQR.setOnClickListener(v -> selectPaymentMethod("QR", layoutQR));
        layoutZaloPay.setOnClickListener(v -> selectPaymentMethod("zalopay", layoutZaloPay));
        layoutMoMo.setOnClickListener(v -> selectPaymentMethod("momo", layoutMoMo));
        layoutCOD.setOnClickListener(v -> selectPaymentMethod("COD", layoutCOD));
        
        // Voucher
        btnApDungVoucher.setOnClickListener(v -> applyVoucher());
        btnChonVoucher.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(ThanhToanActivity.this, QuanLyVoucherActivity.class);
            startActivityForResult(intent, 1001);
        });

        btnThanhToan.setOnClickListener(v -> {
            // Prevent multiple clicks
            if (isClickTooFast() || !btnThanhToan.isEnabled()) {
                return;
            }
            
            String address = edtDiaChi.getText().toString().trim();
            if (address.isEmpty()) {
                showToast("Vui lòng chọn địa chỉ giao hàng");
                return;
            }
            if (cartItems.isEmpty()) {
                showToast("Giỏ hàng trống");
                return;
            }
            
            // Xử lý thanh toán ZaloPay và MoMo riêng
            // Xử lý thanh toán ZaloPay và MoMo
            // Kiểm tra payment method (case-insensitive)
            if ("zalopay".equalsIgnoreCase(selectedPaymentMethod)) {
                processZaloPayPayment(address);
            } else if ("momo".equalsIgnoreCase(selectedPaymentMethod)) {
                processMoMoPayment(address);
            } else {
                // COD, cash, hoặc các payment methods khác đều dùng processPayment()
                processPayment(address);
            }
        });
    }

    private void selectPaymentMethod(String method, LinearLayout selectedLayout) {
        selectedPaymentMethod = method;
        // Reset all backgrounds
        layoutVisa.setBackground(null);
        layoutMastercard.setBackground(null);
        layoutNganHang.setBackground(null);
        layoutQR.setBackground(null);
        layoutZaloPay.setBackground(null);
        layoutMoMo.setBackground(null);
        layoutCOD.setBackground(null);
        
        // Highlight selected
        selectedLayout.setBackgroundResource(R.color.primary_blue_light);
    }

    private void processPayment(String address) {
        btnThanhToan.setEnabled(false);
        btnThanhToan.setText("Đang xử lý...");

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            showToast("Vui lòng đăng nhập");
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        // Parse địa chỉ từ string thành ShippingAddress object
        InvoiceRequest.ShippingAddress shippingAddress = parseAddressForInvoice(address);
        if (shippingAddress == null) {
            showToast("Địa chỉ không hợp lệ. Vui lòng nhập đầy đủ thông tin.");
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        // Lấy thông tin user để điền fullName và phone
        String hoTen = sessionManager.getHoTen();
        shippingAddress.setFullName(hoTen != null && !hoTen.isEmpty() ? hoTen : "Khách hàng");
        
        // Tạo các biến final để sử dụng trong inner class
        final InvoiceRequest.ShippingAddress finalShippingAddress = shippingAddress;
        final String finalAddress = address;

        // Gọi API để lấy phone từ user info
        Call<UserResponse> userCall = userService.getById(String.valueOf(userId));
        userCall.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                String phone = "";
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    phone = user.getSoDienThoai() != null ? user.getSoDienThoai() : "";
                }
                
                // Nếu không có phone, dùng số mặc định hoặc yêu cầu nhập
                if (phone == null || phone.trim().isEmpty()) {
                    phone = "0000000000"; // Fallback
                }
                
                finalShippingAddress.setPhone(phone);
                
                // Tiếp tục tạo đơn hàng
                createInvoiceRequest(finalShippingAddress, finalAddress);
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("Invoice", "Error loading user info: " + t.getMessage());
                // Fallback: dùng số mặc định
                finalShippingAddress.setPhone("0000000000");
                createInvoiceRequest(finalShippingAddress, finalAddress);
            }
        });
    }

    /**
     * Tạo invoice request và gọi API
     */
    private void createInvoiceRequest(InvoiceRequest.ShippingAddress shippingAddress, String address) {
        int userId = sessionManager.getUserId();

        // Tạo InvoiceRequest
        InvoiceRequest request = new InvoiceRequest();
        request.setCustomer(String.valueOf(userId));
        
        // Normalize paymentMethod: "cod" -> "COD", "cash" -> "COD"
        String normalizedPaymentMethod = selectedPaymentMethod;
        if ("cod".equalsIgnoreCase(selectedPaymentMethod) || "cash".equalsIgnoreCase(selectedPaymentMethod)) {
            normalizedPaymentMethod = "COD";
        } else if ("zalopay".equalsIgnoreCase(selectedPaymentMethod)) {
            normalizedPaymentMethod = "zalopay"; // Keep lowercase for ZaloPay
        } else if ("momo".equalsIgnoreCase(selectedPaymentMethod)) {
            normalizedPaymentMethod = "momo"; // Keep lowercase for MoMo
        }
        request.setPaymentMethod(normalizedPaymentMethod);
        
        request.setShippingAddress(shippingAddress);
        request.setNotes("Địa chỉ giao hàng: " + address);
        
        // Thêm voucher code nếu có
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            request.setVoucherCode(voucherCode);
        }

        // Convert cart items to invoice items
        List<InvoiceItemRequest> invoiceItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            InvoiceItemRequest item = new InvoiceItemRequest();
            item.setProduct(cartItem.getProduct().get_id());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getPrice());
            item.setDiscount(0.0);
            invoiceItems.add(item);
        }
        request.setItems(invoiceItems);

        // Gọi API tạo đơn hàng
        Call<ApiResponse<InvoiceResponse>> call = invoiceService.createInvoice(request);
        call.enqueue(new Callback<ApiResponse<InvoiceResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<InvoiceResponse>> call, Response<ApiResponse<InvoiceResponse>> response) {
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<InvoiceResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        // Xử lý response dựa trên payment method
                        handleInvoiceResponse(apiResponse, selectedPaymentMethod);
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Thanh toán thất bại";
                        ToastManager.showToast(ThanhToanActivity.this, errorMsg);
                    }
                } else {
                    // Parse error response
                    String errorMsg = "Lỗi kết nối server";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("Invoice", "Error response code: " + response.code());
                            Log.e("Invoice", "Error response body: " + errorBody);
                            
                            // Thử parse JSON error
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                ApiResponse<?> errorResponse = gson.fromJson(errorBody, ApiResponse.class);
                                if (errorResponse != null && errorResponse.getMessage() != null) {
                                    errorMsg = errorResponse.getMessage();
                                }
                            } catch (Exception jsonEx) {
                                // Không phải JSON, dùng error body
                                if (errorBody.length() < 200) {
                                    errorMsg = errorBody;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Invoice", "Error parsing error body", e);
                    }
                    ToastManager.showToast(ThanhToanActivity.this, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<InvoiceResponse>> call, Throwable t) {
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");
                Log.e("Invoice", "Network error: " + t.getMessage(), t);
                ToastManager.showToast(ThanhToanActivity.this, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Xử lý response từ API invoices dựa trên payment method
     */
    private void handleInvoiceResponse(ApiResponse<InvoiceResponse> apiResponse, String paymentMethod) {
        try {
            // Parse response data để lấy các trường payment (nếu có)
            com.google.gson.JsonObject dataObject = null;
            if (apiResponse.getData() != null) {
                // Convert InvoiceResponse sang JsonObject để lấy các trường bổ sung
                com.google.gson.Gson gson = new com.google.gson.Gson();
                String dataJson = gson.toJson(apiResponse.getData());
                dataObject = gson.fromJson(dataJson, com.google.gson.JsonObject.class);
            }

            if ("COD".equalsIgnoreCase(paymentMethod) || "cash".equalsIgnoreCase(paymentMethod)) {
                // COD/Cash: Xóa giỏ hàng và chuyển màn hình
                cartManager.clearCart();
                ToastManager.showToast(ThanhToanActivity.this, "Đặt hàng thành công!");
                
                android.content.Intent intent = new android.content.Intent(ThanhToanActivity.this, DonHangActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else if ("zalopay".equalsIgnoreCase(paymentMethod)) {
                // ZaloPay: Lấy zp_trans_token, order_url, order_token và mở ZaloPay app
                if (dataObject != null && dataObject.has("zp_trans_token")) {
                    String zpTransToken = dataObject.get("zp_trans_token").getAsString();
                    String orderUrl = dataObject.has("order_url") ? dataObject.get("order_url").getAsString() : null;
                    String orderToken = dataObject.has("order_token") ? dataObject.get("order_token").getAsString() : null;
                    
                    // Mở ZaloPay app với zp_trans_token
                    openZaloPayApp(zpTransToken, orderUrl, orderToken);
                } else {
                    ToastManager.showToast(ThanhToanActivity.this, "Không nhận được thông tin thanh toán ZaloPay");
                }
            } else if ("momo".equalsIgnoreCase(paymentMethod)) {
                // MoMo: Lấy payUrl, deeplink, qrCodeUrl và mở MoMo app
                if (dataObject != null && dataObject.has("payUrl")) {
                    String payUrl = dataObject.get("payUrl").getAsString();
                    String deeplink = dataObject.has("deeplink") ? dataObject.get("deeplink").getAsString() : null;
                    String qrCodeUrl = dataObject.has("qrCodeUrl") ? dataObject.get("qrCodeUrl").getAsString() : null;
                    
                    // Mở MoMo app với payUrl
                    openMoMoApp(payUrl, deeplink, qrCodeUrl);
                } else {
                    ToastManager.showToast(ThanhToanActivity.this, "Không nhận được thông tin thanh toán MoMo");
                }
            } else {
                // Payment method khác: Xử lý mặc định
                cartManager.clearCart();
                ToastManager.showToast(ThanhToanActivity.this, "Đặt hàng thành công!");
                
                android.content.Intent intent = new android.content.Intent(ThanhToanActivity.this, DonHangActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e("Invoice", "Error handling invoice response: " + e.getMessage(), e);
            ToastManager.showToast(ThanhToanActivity.this, "Lỗi xử lý response: " + e.getMessage());
        }
    }

    /**
     * Mở ZaloPay app với zp_trans_token
     */
    private void openZaloPayApp(String zpTransToken, String orderUrl, String orderToken) {
        if (zpTransToken == null || zpTransToken.isEmpty()) {
            ToastManager.showToast(this, "Không có thông tin thanh toán ZaloPay");
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        try {
            // Mở ZaloPay app với zp_trans_token
            ZaloPaySDK.getInstance().payOrder(
                this,
                zpTransToken,
                "demozpdk://app",
                new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                        // Thanh toán thành công
                        Log.d("ZaloPay", "Payment succeeded - Transaction ID: " + transactionId + ", App Trans ID: " + appTransID);
                        
                        // Xóa giỏ hàng
                        cartManager.clearCart();
                        
                        // Hiển thị thông báo
                        ToastManager.showToast(ThanhToanActivity.this, 
                            "Thanh toán thành công!", 
                            Toast.LENGTH_LONG);
                        
                        // Chuyển đến màn hình đơn hàng
                        android.content.Intent intent = new android.content.Intent(
                            ThanhToanActivity.this, DonHangActivity.class);
                        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                                       android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onPaymentCanceled(String zpTransToken, String appTransID) {
                        // User hủy thanh toán
                        Log.d("ZaloPay", "Payment canceled - App Trans ID: " + appTransID);
                        
                        ToastManager.showToast(ThanhToanActivity.this, 
                            "Đã hủy thanh toán", 
                            Toast.LENGTH_SHORT);
                        
                        btnThanhToan.setEnabled(true);
                        btnThanhToan.setText("Thanh toán");
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                        // Lỗi thanh toán
                        Log.e("ZaloPay", "Payment error: " + zaloPayError.toString() + 
                              ", App Trans ID: " + appTransID);
                        
                        ToastManager.showToast(ThanhToanActivity.this, 
                            "Lỗi thanh toán: " + zaloPayError.toString(), 
                            Toast.LENGTH_LONG);
                        
                        btnThanhToan.setEnabled(true);
                        btnThanhToan.setText("Thanh toán");
                    }
                }
            );
        } catch (Exception e) {
            Log.e("ZaloPay", "Error opening ZaloPay app: " + e.getMessage(), e);
            ToastManager.showToast(this, "Lỗi mở ZaloPay app: " + e.getMessage());
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
        }
    }

    /**
     * Mở MoMo app với payUrl, deeplink, qrCodeUrl
     */
    private void openMoMoApp(String payUrl, String deeplink, String qrCodeUrl) {
        // Ưu tiên dùng payUrl (web URL) để mở trong browser
        boolean usePayUrl = (payUrl != null && !payUrl.isEmpty());
        String paymentUrl = usePayUrl ? payUrl : deeplink;
        
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            ToastManager.showToast(this, "Không có thông tin thanh toán MoMo");
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        Log.d("MoMo", "Opening payment URL: " + paymentUrl);
        Log.d("MoMo", "Using payUrl: " + usePayUrl);
        
        // Mở MoMo app hoặc browser
        boolean opened = false;
        
        try {
            android.content.Intent intent = new android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(paymentUrl));
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // Kiểm tra xem có app nào có thể xử lý intent này không
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                opened = true;
                
                // Hiển thị thông báo
                ToastManager.showToast(this, 
                    "Đang mở MoMo để thanh toán...", 
                    Toast.LENGTH_SHORT);
            } else {
                Log.d("MoMo", "Cannot resolve activity for: " + paymentUrl);
            }
        } catch (Exception e) {
            Log.e("MoMo", "Error opening payment URL: " + e.getMessage(), e);
        }
        
        // Nếu không mở được và đang dùng deeplink, thử fallback sang payUrl
        if (!opened && !usePayUrl && payUrl != null && !payUrl.isEmpty()) {
            Log.d("MoMo", "Cannot open deeplink, trying payUrl instead: " + payUrl);
            try {
                android.content.Intent browserIntent = new android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(payUrl));
                browserIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                
                if (browserIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(browserIntent);
                    opened = true;
                    
                    ToastManager.showToast(this, 
                        "Đang mở trình duyệt để thanh toán MoMo...", 
                        Toast.LENGTH_SHORT);
                } else {
                    Log.e("MoMo", "Cannot open payUrl in browser either");
                }
            } catch (Exception e) {
                Log.e("MoMo", "Error opening payUrl in browser: " + e.getMessage(), e);
            }
        }
        
        // Nếu vẫn không mở được, hiển thị thông báo
        if (!opened) {
            String message = "Không thể mở MoMo. ";
            if (payUrl != null && !payUrl.isEmpty()) {
                message += "Vui lòng kiểm tra kết nối mạng hoặc cài đặt app MoMo.";
            } else {
                message += "Vui lòng cài đặt app MoMo hoặc kiểm tra lại.";
            }
            
            ToastManager.showToast(this, message, Toast.LENGTH_LONG);
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
        }
    }

    /**
     * Parse địa chỉ string thành InvoiceRequest.ShippingAddress object
     */
    private InvoiceRequest.ShippingAddress parseAddressForInvoice(String addressString) {
        if (addressString == null || addressString.trim().isEmpty()) {
            return null;
        }
        
        try {
            InvoiceRequest.ShippingAddress shippingAddress = new InvoiceRequest.ShippingAddress();
            
            // Parse đơn giản: tách bằng dấu phẩy
            String[] parts = addressString.split(",");
            
            if (parts.length >= 1) {
                shippingAddress.setAddress(parts[0].trim());
            }
            if (parts.length >= 2) {
                shippingAddress.setWard(parts[1].trim());
            }
            if (parts.length >= 3) {
                shippingAddress.setDistrict(parts[2].trim());
            }
            if (parts.length >= 4) {
                shippingAddress.setCity(parts[3].trim());
            } else if (parts.length >= 3) {
                // Nếu chỉ có 3 phần, phần cuối là thành phố
                shippingAddress.setCity(parts[2].trim());
                shippingAddress.setDistrict("");
            }
            
            // Nếu thiếu thông tin, dùng toàn bộ string làm address
            if (shippingAddress.getAddress() == null || shippingAddress.getAddress().isEmpty()) {
                shippingAddress.setAddress(addressString);
            }
            if (shippingAddress.getCity() == null || shippingAddress.getCity().isEmpty()) {
                shippingAddress.setCity("Hồ Chí Minh"); // Default
            }
            
            return shippingAddress;
        } catch (Exception e) {
            Log.e("ThanhToanActivity", "Error parsing address: " + e.getMessage(), e);
            // Fallback: dùng toàn bộ string làm address
            InvoiceRequest.ShippingAddress shippingAddress = new InvoiceRequest.ShippingAddress();
            shippingAddress.setAddress(addressString);
            shippingAddress.setCity("Hồ Chí Minh");
            shippingAddress.setWard("");
            shippingAddress.setDistrict("");
            return shippingAddress;
        }
    }

    /**
     * Xử lý thanh toán qua ZaloPay
     * Flow: Lấy user info -> Gọi API /api/payment/zalopay/create -> Nhận zp_trans_token -> Mở ZaloPay app
     */
    private void processZaloPayPayment(String address) {
        btnThanhToan.setEnabled(false);
        btnThanhToan.setText("Đang xử lý...");

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            showToast("Vui lòng đăng nhập");
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        // Parse địa chỉ từ string thành ShippingAddress object
        // Format: "Số nhà, Đường, Phường, Quận, Thành phố"
        ZaloPayCreateRequest.ShippingAddress shippingAddress = parseAddress(address);
        if (shippingAddress == null) {
            showToast("Địa chỉ không hợp lệ. Vui lòng nhập đầy đủ thông tin.");
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        // Lấy thông tin user để điền fullName và phone
        String hoTen = sessionManager.getHoTen();
        shippingAddress.setFullName(hoTen != null && !hoTen.isEmpty() ? hoTen : "Khách hàng");
        
        // Gọi API để lấy phone từ user info
        Call<UserResponse> userCall = userService.getById(String.valueOf(userId));
        userCall.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                String phone = "";
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    phone = user.getSoDienThoai() != null ? user.getSoDienThoai() : "";
                }
                
                // Nếu không có phone, dùng số mặc định hoặc yêu cầu nhập
                if (phone == null || phone.trim().isEmpty()) {
                    phone = "0000000000"; // Fallback
                }
                
                shippingAddress.setPhone(phone);
                
                // Tiếp tục tạo đơn hàng ZaloPay
                createZaloPayOrder(shippingAddress, address);
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("ZaloPay", "Error loading user info: " + t.getMessage());
                // Fallback: dùng số mặc định
                shippingAddress.setPhone("0000000000");
                createZaloPayOrder(shippingAddress, address);
            }
        });
    }
    
    /**
     * Tạo đơn hàng ZaloPay sau khi đã có đầy đủ thông tin
     */
    private void createZaloPayOrder(ZaloPayCreateRequest.ShippingAddress shippingAddress, String address) {

        // Tạo request
        ZaloPayCreateRequest request = new ZaloPayCreateRequest();
        request.setShippingAddress(shippingAddress);
        request.setNotes("Địa chỉ giao hàng: " + address);
        
        // Thêm voucher code nếu có
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            request.setVoucherCode(voucherCode);
        }
        
        // Gửi cart items từ client (vì giỏ hàng được quản lý local)
        List<ZaloPayCreateRequest.CartItem> requestItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            ZaloPayCreateRequest.CartItem item = new ZaloPayCreateRequest.CartItem();
            item.setProduct(cartItem.getProduct().get_id());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getPrice());
            item.setColor(""); // Có thể lấy từ cartItem nếu có
            item.setSize(""); // Có thể lấy từ cartItem nếu có
            requestItems.add(item);
        }
        request.setItems(requestItems);
        
        // Log để debug
        Log.d("ZaloPay", "Cart items count: " + cartItems.size());
        Log.d("ZaloPay", "Request items count: " + requestItems.size());
        if (!requestItems.isEmpty()) {
            Log.d("ZaloPay", "First item product ID: " + requestItems.get(0).getProduct());
            Log.d("ZaloPay", "First item quantity: " + requestItems.get(0).getQuantity());
        }
        
        // Tính tổng tiền
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        
        // Convert request to JSON để log
        com.google.gson.Gson gson = new com.google.gson.Gson();
        String requestJson = gson.toJson(request);
        Log.d("ZaloPay", "=== ZALOPAY CREATE REQUEST ===");
        Log.d("ZaloPay", "Request JSON: " + requestJson);
        Log.d("ZaloPay", "Cart items count: " + cartItems.size());
        Log.d("ZaloPay", "Request items count: " + requestItems.size());
        Log.d("ZaloPay", "Total amount: " + total);
        Log.d("ZaloPay", "Voucher code: " + (voucherCode != null ? voucherCode : "null"));

        // Gọi API tạo đơn hàng ZaloPay
        Call<ZaloPayCreateResponse> call = paymentService.createZaloPayOrder(request);
        call.enqueue(new Callback<ZaloPayCreateResponse>() {
            @Override
            public void onResponse(Call<ZaloPayCreateResponse> call, Response<ZaloPayCreateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ZaloPayCreateResponse zalopayResponse = response.body();
                    
                    if (zalopayResponse.isSuccess() && zalopayResponse.getZp_trans_token() != null) {
                        String zpTransToken = zalopayResponse.getZp_trans_token();
                        String orderId = zalopayResponse.getOrderId();
                        String orderNumber = zalopayResponse.getOrderNumber();
                        String orderUrl = zalopayResponse.getOrder_url();
                        
                        Log.d("ZaloPay", "Received zp_trans_token: " + zpTransToken);
                        Log.d("ZaloPay", "Order ID: " + orderId);
                        Log.d("ZaloPay", "Order Number: " + orderNumber);
                        
                        // Mở ZaloPay app với zp_trans_token
                        final String finalOrderUrl = orderUrl;
                        ZaloPaySDK.getInstance().payOrder(
                            ThanhToanActivity.this,
                            zpTransToken,
                            "demozpdk://app",
                            new PayOrderListener() {
                                @Override
                                public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                                    // Thanh toán thành công
                                    Log.d("ZaloPay", "Payment succeeded - Transaction ID: " + transactionId + ", App Trans ID: " + appTransID);
                                    
                                    // Xóa giỏ hàng
                                    cartManager.clearCart();
                                    
                                    // Hiển thị thông báo
                                    ToastManager.showToast(ThanhToanActivity.this, 
                                        "Thanh toán thành công!\nMã đơn: " + orderNumber, 
                                        Toast.LENGTH_LONG);
                                    
                                    // Chuyển đến màn hình đơn hàng
                                    android.content.Intent intent = new android.content.Intent(
                                        ThanhToanActivity.this, DonHangActivity.class);
                                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                                                   android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onPaymentCanceled(String zpTransToken, String appTransID) {
                                    // User hủy thanh toán
                                    Log.d("ZaloPay", "Payment canceled - App Trans ID: " + appTransID);
                                    
                                    ToastManager.showToast(ThanhToanActivity.this, 
                                        "Đã hủy thanh toán", 
                                        Toast.LENGTH_SHORT);
                                    
                                    btnThanhToan.setEnabled(true);
                                    btnThanhToan.setText("Thanh toán");
                                }

                                @Override
                                public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                                    // Lỗi thanh toán
                                    Log.e("ZaloPay", "Payment error: " + zaloPayError.toString() + 
                                          ", App Trans ID: " + appTransID);
                                    
                                    if (zaloPayError == ZaloPayError.PAYMENT_APP_NOT_FOUND) {
                                        Log.w("ZaloPay", "ZaloPay app not found. Fallback to order url: " + finalOrderUrl);
                                        showZaloPayAppNotFoundDialog(finalOrderUrl);
                                    } else {
                                        ToastManager.showToast(ThanhToanActivity.this, 
                                            "Lỗi thanh toán: " + zaloPayError.toString(), 
                                            Toast.LENGTH_LONG);
                                        
                                        btnThanhToan.setEnabled(true);
                                        btnThanhToan.setText("Thanh toán");
                                    }
                                }
                            }
                        );
                    } else {
                        // Log chi tiết lỗi
                        Log.e("ZaloPay", "=== ZALOPAY CREATE ERROR ===");
                        Log.e("ZaloPay", "Success: " + zalopayResponse.isSuccess());
                        Log.e("ZaloPay", "Error: " + zalopayResponse.getError());
                        Log.e("ZaloPay", "Message: " + zalopayResponse.getMessage());
                        Log.e("ZaloPay", "Response body: " + new com.google.gson.Gson().toJson(zalopayResponse));
                        
                        String errorMsg = zalopayResponse.getError() != null ? zalopayResponse.getError() : 
                                         (zalopayResponse.getMessage() != null ? zalopayResponse.getMessage() : "Tạo đơn hàng thất bại");
                        
                        // Hiển thị thông báo lỗi chi tiết hơn
                        String userFriendlyMsg = errorMsg;
                        if (errorMsg.contains("Giao dịch thất bại")) {
                            userFriendlyMsg = "Lỗi cấu hình ZaloPay. Vui lòng kiểm tra:\n" +
                                            "1. APP_ID, KEY1, KEY2 trong backend .env\n" +
                                            "2. Backend logs để xem chi tiết lỗi";
                        }
                        
                        ToastManager.showToast(ThanhToanActivity.this, userFriendlyMsg, Toast.LENGTH_LONG);
                        btnThanhToan.setEnabled(true);
                        btnThanhToan.setText("Thanh toán");
                    }
                } else {
                    // Parse error response
                    String errorMsg = "Lỗi kết nối server";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("ZaloPay", "Error response code: " + response.code());
                            Log.e("ZaloPay", "Error response body: " + errorBody);
                            
                            // Kiểm tra nếu là HTML error (route không tồn tại)
                            if (errorBody.contains("Cannot POST") || errorBody.contains("<!DOCTYPE html>")) {
                                errorMsg = "Endpoint không tồn tại. Vui lòng kiểm tra backend server!\n" +
                                          "Đảm bảo route /api/payment/zalopay/create đã được đăng ký.";
                            } else {
                                // Thử parse JSON error
                                try {
                                    com.google.gson.Gson gson = new com.google.gson.Gson();
                                    ApiResponse<?> errorResponse = gson.fromJson(errorBody, ApiResponse.class);
                                    if (errorResponse != null && errorResponse.getMessage() != null) {
                                        errorMsg = errorResponse.getMessage();
                                    }
                                } catch (Exception jsonEx) {
                                    // Không phải JSON, dùng error body
                                    if (errorBody.length() < 200) {
                                        errorMsg = errorBody;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ZaloPay", "Error parsing error body", e);
                    }
                    ToastManager.showToast(ThanhToanActivity.this, errorMsg);
                    btnThanhToan.setEnabled(true);
                    btnThanhToan.setText("Thanh toán");
                }
            }

            @Override
            public void onFailure(Call<ZaloPayCreateResponse> call, Throwable t) {
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");
                Log.e("ZaloPay", "Network error: " + t.getMessage(), t);
                ToastManager.showToast(ThanhToanActivity.this, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showZaloPayAppNotFoundDialog(String orderUrl) {
        btnThanhToan.setEnabled(true);
        btnThanhToan.setText("Thanh toán");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chưa cài ZaloPay");
        builder.setMessage("Thiết bị của bạn chưa cài ứng dụng ZaloPay sandbox. " +
                "Bạn có thể cài app hoặc mở liên kết thanh toán trong trình duyệt.");
        builder.setPositiveButton("Mở liên kết", (dialog, which) -> openZaloPayOrderUrl(orderUrl));
        builder.setNegativeButton("Để sau", null);
        builder.show();
    }

    private void openZaloPayOrderUrl(String orderUrl) {
        if (orderUrl == null || orderUrl.trim().isEmpty()) {
            ToastManager.showToast(this, "Không tìm thấy liên kết thanh toán ZaloPay");
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl));
            startActivity(intent);
        } catch (Exception e) {
            Log.e("ZaloPay", "Không mở được liên kết ZaloPay", e);
            ToastManager.showToast(this, "Không mở được trình duyệt. Vui lòng cài ZaloPay.");
        }
    }
    
    /**
     * Parse địa chỉ string thành ShippingAddress object
     * Format đơn giản: "Địa chỉ, Phường, Quận, Thành phố"
     */
    private ZaloPayCreateRequest.ShippingAddress parseAddress(String addressString) {
        if (addressString == null || addressString.trim().isEmpty()) {
            return null;
        }
        
        try {
            ZaloPayCreateRequest.ShippingAddress shippingAddress = new ZaloPayCreateRequest.ShippingAddress();
            
            // Parse đơn giản: tách bằng dấu phẩy
            String[] parts = addressString.split(",");
            
            if (parts.length >= 1) {
                shippingAddress.setAddress(parts[0].trim());
            }
            if (parts.length >= 2) {
                shippingAddress.setWard(parts[1].trim());
            }
            if (parts.length >= 3) {
                shippingAddress.setDistrict(parts[2].trim());
            }
            if (parts.length >= 4) {
                shippingAddress.setCity(parts[3].trim());
            } else if (parts.length >= 3) {
                // Nếu chỉ có 3 phần, phần cuối là thành phố
                shippingAddress.setCity(parts[2].trim());
                shippingAddress.setDistrict("");
            }
            
            // Nếu thiếu thông tin, dùng toàn bộ string làm address
            if (shippingAddress.getAddress() == null || shippingAddress.getAddress().isEmpty()) {
                shippingAddress.setAddress(addressString);
            }
            if (shippingAddress.getCity() == null || shippingAddress.getCity().isEmpty()) {
                shippingAddress.setCity("Hồ Chí Minh"); // Default
            }
            
            return shippingAddress;
        } catch (Exception e) {
            Log.e("ThanhToanActivity", "Error parsing address: " + e.getMessage(), e);
            // Fallback: dùng toàn bộ string làm address
            ZaloPayCreateRequest.ShippingAddress shippingAddress = new ZaloPayCreateRequest.ShippingAddress();
            shippingAddress.setAddress(addressString);
            shippingAddress.setCity("Hồ Chí Minh");
            return shippingAddress;
        }
    }
    
    /**
     * Kiểm tra xem click có quá nhanh không (debounce)
     */
    private boolean isClickTooFast() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_DELAY) {
            return true;
        }
        lastClickTime = currentTime;
        return false;
    }
    
    /**
     * Hiển thị Toast với quản lý tốt hơn (tránh spam)
     */
    private void showToast(String message) {
        ToastManager.showToast(this, message);
    }
    
    /**
     * Lấy vị trí hiện tại
     */
    private void getCurrentLocation() {
        // Kiểm tra permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED 
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, 
                                 Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        
        // Hiển thị loading
        btnSuDungViTriHienTai.setEnabled(false);
        btnSuDungViTriHienTai.setText("Đang lấy vị trí...");
        
        // Lấy vị trí
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        btnSuDungViTriHienTai.setEnabled(true);
                        btnSuDungViTriHienTai.setText("Sử dụng vị trí hiện tại");
                        
                        if (location != null) {
                            // Chuyển đổi tọa độ thành địa chỉ
                            getAddressFromLocation(location.getLatitude(), location.getLongitude());
                        } else {
                            showToast("Không thể lấy vị trí. Vui lòng bật GPS và thử lại.");
                        }
                    }
                });
    }
    
    /**
     * Chuyển đổi tọa độ thành địa chỉ (Reverse Geocoding)
     * Chạy trên background thread để tránh ANR
     */
    private void getAddressFromLocation(double latitude, double longitude) {
        // Hiển thị loading
        runOnUiThread(() -> {
            btnSuDungViTriHienTai.setText("Đang lấy địa chỉ...");
            btnSuDungViTriHienTai.setEnabled(false);
        });
        
        // Chạy trên background thread
        new Thread(() -> {
            android.os.Handler mainHandler = new android.os.Handler(getMainLooper());
            
            try {
                Geocoder geocoder = new Geocoder(this, java.util.Locale.getDefault());
                
                // Thêm timeout bằng cách sử dụng Future
                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
                java.util.concurrent.Future<List<Address>> future = executor.submit(() -> {
                    return geocoder.getFromLocation(latitude, longitude, 1);
                });
                
                List<Address> addresses = null;
                try {
                    // Timeout sau 5 giây
                    addresses = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
                } catch (java.util.concurrent.TimeoutException e) {
                    future.cancel(true);
                    android.util.Log.e("ThanhToanActivity", "Geocoding timeout");
                    mainHandler.post(() -> {
                        btnSuDungViTriHienTai.setEnabled(true);
                        btnSuDungViTriHienTai.setText("Sử dụng vị trí hiện tại");
                        showToast("Lỗi: Quá thời gian chờ. Vui lòng thử lại hoặc nhập địa chỉ thủ công.");
                    });
                    executor.shutdown();
                    return;
                } catch (java.util.concurrent.ExecutionException e) {
                    android.util.Log.e("ThanhToanActivity", "Geocoding execution error", e);
                    // Kiểm tra nếu cause là IOException
                    Throwable cause = e.getCause();
                    if (cause instanceof IOException) {
                        android.util.Log.e("ThanhToanActivity", "Geocoding IOException: " + cause.getMessage());
                    }
                    mainHandler.post(() -> {
                        btnSuDungViTriHienTai.setEnabled(true);
                        btnSuDungViTriHienTai.setText("Sử dụng vị trí hiện tại");
                        showToast("Lỗi khi lấy địa chỉ. Vui lòng thử lại hoặc nhập địa chỉ thủ công.");
                    });
                    executor.shutdown();
                    return;
                }
                
                executor.shutdown();
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    
                    // Tạo địa chỉ đầy đủ
                    StringBuilder addressBuilder = new StringBuilder();
                    
                    // Số nhà, đường
                    if (address.getThoroughfare() != null) {
                        addressBuilder.append(address.getThoroughfare());
                    }
                    
                    // Phường/Xã
                    if (address.getSubLocality() != null) {
                        if (addressBuilder.length() > 0) addressBuilder.append(", ");
                        addressBuilder.append(address.getSubLocality());
                    }
                    
                    // Quận/Huyện
                    if (address.getSubAdminArea() != null) {
                        if (addressBuilder.length() > 0) addressBuilder.append(", ");
                        addressBuilder.append(address.getSubAdminArea());
                    }
                    
                    // Tỉnh/Thành phố
                    if (address.getAdminArea() != null) {
                        if (addressBuilder.length() > 0) addressBuilder.append(", ");
                        addressBuilder.append(address.getAdminArea());
                    }
                    
                    String fullAddress = addressBuilder.toString();
                    
                    // Cập nhật UI trên main thread
                    mainHandler.post(() -> {
                        if (!fullAddress.isEmpty()) {
                            edtDiaChi.setText(fullAddress);
                            showToast("Đã lấy địa chỉ từ vị trí hiện tại");
                        } else {
                            // Fallback: Hiển thị tọa độ
                            edtDiaChi.setText(String.format("Lat: %.6f, Lng: %.6f", latitude, longitude));
                            showToast("Đã lấy tọa độ. Vui lòng nhập địa chỉ thủ công.");
                        }
                        btnSuDungViTriHienTai.setEnabled(true);
                        btnSuDungViTriHienTai.setText("Sử dụng vị trí hiện tại");
                    });
                } else {
                    mainHandler.post(() -> {
                        // Fallback: Hiển thị tọa độ
                        edtDiaChi.setText(String.format("Lat: %.6f, Lng: %.6f", latitude, longitude));
                        showToast("Không tìm thấy địa chỉ. Vui lòng nhập thủ công.");
                        btnSuDungViTriHienTai.setEnabled(true);
                        btnSuDungViTriHienTai.setText("Sử dụng vị trí hiện tại");
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("ThanhToanActivity", "Unexpected error: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    btnSuDungViTriHienTai.setEnabled(true);
                    btnSuDungViTriHienTai.setText("Sử dụng vị trí hiện tại");
                    showToast("Lỗi không xác định. Vui lòng thử lại.");
                });
            }
        }).start();
    }
    
    /**
     * Xử lý kết quả request permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getCurrentLocation();
            } else {
                // Permission denied
                showToast("Cần quyền truy cập vị trí để sử dụng tính năng này");
            }
        }
    }
    
    /**
     * Xử lý thanh toán qua MoMo
     * Flow: Gọi backend API để tạo payment request -> Nhận payment URL -> Mở MoMo app
     */
    private void processMoMoPayment(String address) {
        btnThanhToan.setEnabled(false);
        btnThanhToan.setText("Đang xử lý...");

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            showToast("Vui lòng đăng nhập");
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        // Tính tổng tiền
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        
        if (total <= 0) {
            showToast("Tổng tiền không hợp lệ");
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        // Parse địa chỉ từ string thành ShippingAddress object
        // Format: "Số nhà, Đường, Phường, Quận, Thành phố"
        ZaloPayCreateRequest.ShippingAddress shippingAddress = parseAddress(address);
        if (shippingAddress == null) {
            showToast("Địa chỉ không hợp lệ. Vui lòng nhập đầy đủ thông tin.");
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        // Tạo các biến final để sử dụng trong inner class
        final ZaloPayCreateRequest.ShippingAddress finalShippingAddress = shippingAddress;
        final String finalAddress = address;
        final double finalTotal = total;

        // Lấy thông tin user để điền fullName và phone
        String hoTen = sessionManager.getHoTen();
        finalShippingAddress.setFullName(hoTen != null && !hoTen.isEmpty() ? hoTen : "Khách hàng");
        
        // Gọi API để lấy phone từ user info
        Call<UserResponse> userCall = userService.getById(String.valueOf(userId));
        userCall.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                String phone = "";
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    phone = user.getSoDienThoai() != null ? user.getSoDienThoai() : "";
                }
                
                // Nếu không có phone, dùng số mặc định hoặc yêu cầu nhập
                if (phone == null || phone.trim().isEmpty()) {
                    phone = "0000000000"; // Fallback
                }
                
                finalShippingAddress.setPhone(phone);
                
                // Tiếp tục tạo đơn hàng MoMo
                createMoMoPayment(finalShippingAddress, finalAddress, finalTotal);
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("MoMo", "Error loading user info: " + t.getMessage());
                // Fallback: dùng số mặc định
                finalShippingAddress.setPhone("0000000000");
                createMoMoPayment(finalShippingAddress, finalAddress, finalTotal);
            }
        });
    }

    /**
     * Tạo payment request qua backend API và mở MoMo app/browser
     * Flow: Gọi API /api/payment/momo/create -> Nhận payUrl/deeplink -> Mở MoMo app hoặc browser
     */
    private void createMoMoPayment(ZaloPayCreateRequest.ShippingAddress shippingAddress, String address, double total) {
        // Tạo request object
        MoMoCreateRequest request = new MoMoCreateRequest();
        
        // Convert ShippingAddress từ ZaloPay format sang MoMo format
        MoMoCreateRequest.ShippingAddress momoShippingAddress = new MoMoCreateRequest.ShippingAddress();
        momoShippingAddress.setFullName(shippingAddress.getFullName());
        momoShippingAddress.setPhone(shippingAddress.getPhone());
        momoShippingAddress.setAddress(shippingAddress.getAddress());
        momoShippingAddress.setWard(shippingAddress.getWard());
        momoShippingAddress.setDistrict(shippingAddress.getDistrict());
        momoShippingAddress.setCity(shippingAddress.getCity());
        
        request.setShippingAddress(momoShippingAddress);
        request.setNotes("Địa chỉ giao hàng: " + address);
        
        // Thêm voucher code nếu có
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            request.setVoucherCode(voucherCode);
        }
        
        // Thêm cart items vào request
        List<MoMoCreateRequest.CartItem> requestItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            MoMoCreateRequest.CartItem item = new MoMoCreateRequest.CartItem();
            item.setProduct(cartItem.getProduct().get_id());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getPrice());
            item.setColor(""); // Có thể lấy từ cartItem nếu có
            item.setSize(""); // Có thể lấy từ cartItem nếu có
            requestItems.add(item);
        }
        request.setItems(requestItems);
        
        Log.d("MoMo", "Cart items count: " + cartItems.size());
        Log.d("MoMo", "Request items count: " + requestItems.size());
        
        // Gọi API tạo đơn hàng MoMo
        Call<MoMoCreateResponse> call = paymentService.createMoMoOrder(request);
        call.enqueue(new Callback<MoMoCreateResponse>() {
            @Override
            public void onResponse(Call<MoMoCreateResponse> call, Response<MoMoCreateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MoMoCreateResponse momoResponse = response.body();
                    
                    // Kiểm tra response có data wrapper không
                    String payUrl = null;
                    String deeplink = null;
                    
                    if (momoResponse.getData() != null) {
                        // Response có format { success, message, data: {...} }
                        payUrl = momoResponse.getData().getPayUrl();
                        deeplink = momoResponse.getData().getDeeplink();
                    } else {
                        // Response có format trực tiếp
                        payUrl = momoResponse.getPayUrl();
                        deeplink = momoResponse.getDeeplink();
                    }
                    
                    // ✅ Ưu tiên dùng payUrl (web URL) để mở trong browser
                    // Luôn ưu tiên payUrl trước, chỉ dùng deeplink nếu không có payUrl
                    boolean usePayUrl = (payUrl != null && !payUrl.isEmpty());
                    String paymentUrl = usePayUrl ? payUrl : deeplink;
                    
                    if (paymentUrl != null && !paymentUrl.isEmpty()) {
                        Log.d("MoMo", "Received payment URL: " + paymentUrl);
                        Log.d("MoMo", "Using payUrl: " + usePayUrl);
                        Log.d("MoMo", "Order ID: " + (momoResponse.getData() != null ? 
                            momoResponse.getData().getOrderId() : momoResponse.getOrderId()));
                        
                        // Mở MoMo app hoặc browser
                        boolean opened = false;
                        
                        try {
                            android.content.Intent intent = new android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(paymentUrl));
                            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                            
                            // Kiểm tra xem có app nào có thể xử lý intent này không
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                                opened = true;
                                
                                // Hiển thị thông báo
                                ToastManager.showToast(ThanhToanActivity.this, 
                                    "Đang mở MoMo để thanh toán...", 
                                    Toast.LENGTH_SHORT);
                            } else {
                                Log.d("MoMo", "Cannot resolve activity for: " + paymentUrl);
                            }
                        } catch (Exception e) {
                            Log.e("MoMo", "Error opening payment URL: " + e.getMessage(), e);
                        }
                        
                        // Nếu không mở được và đang dùng deeplink, thử fallback sang payUrl
                        if (!opened && !usePayUrl && payUrl != null && !payUrl.isEmpty()) {
                            Log.d("MoMo", "Cannot open deeplink, trying payUrl instead: " + payUrl);
                            try {
                                android.content.Intent browserIntent = new android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(payUrl));
                                browserIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                                
                                if (browserIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(browserIntent);
                                    opened = true;
                                    
                                    ToastManager.showToast(ThanhToanActivity.this, 
                                        "Đang mở trình duyệt để thanh toán MoMo...", 
                                        Toast.LENGTH_SHORT);
                                } else {
                                    Log.e("MoMo", "Cannot open payUrl in browser either");
                                }
                            } catch (Exception e) {
                                Log.e("MoMo", "Error opening payUrl in browser: " + e.getMessage(), e);
                            }
                        }
                        
                        // Nếu vẫn không mở được, hiển thị thông báo
                        if (!opened) {
                            String message = "Không thể mở MoMo. ";
                            if (payUrl != null && !payUrl.isEmpty()) {
                                message += "Vui lòng kiểm tra kết nối mạng hoặc cài đặt app MoMo.";
                            } else {
                                message += "Vui lòng cài đặt app MoMo hoặc kiểm tra lại.";
                            }
                            
                            ToastManager.showToast(ThanhToanActivity.this, message, Toast.LENGTH_LONG);
                            btnThanhToan.setEnabled(true);
                            btnThanhToan.setText("Thanh toán");
                        }
                    } else {
                        String errorMsg = momoResponse.getError() != null ? momoResponse.getError() : 
                                         (momoResponse.getMessage() != null ? momoResponse.getMessage() : "Tạo đơn hàng thất bại");
                        Log.e("MoMo", "No payment URL in response");
                        ToastManager.showToast(ThanhToanActivity.this, errorMsg, Toast.LENGTH_LONG);
                        btnThanhToan.setEnabled(true);
                        btnThanhToan.setText("Thanh toán");
                    }
                } else {
                    // Xử lý lỗi response
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("MoMo", "Error parsing error body", e);
                    }
                    
                    Log.e("MoMo", "Error response code: " + response.code());
                    Log.e("MoMo", "Error response body: " + errorBody);
                    
                    ToastManager.showToast(ThanhToanActivity.this, 
                        "Lỗi tạo đơn hàng MoMo. Vui lòng thử lại sau.", 
                        Toast.LENGTH_LONG);
                    btnThanhToan.setEnabled(true);
                    btnThanhToan.setText("Thanh toán");
                }
            }

            @Override
            public void onFailure(Call<MoMoCreateResponse> call, Throwable t) {
                Log.e("MoMo", "Network error: " + t.getMessage(), t);
                ToastManager.showToast(ThanhToanActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_LONG);
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");
            }
        });
    }
    
    /**
     * Xử lý kết quả từ MoMo app
     * COMMENT TẠM THỜI VÌ SDK KHÔNG TẢI ĐƯỢC
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Xử lý voucher từ QuanLyVoucherActivity
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String voucherCodeFromIntent = data.getStringExtra("voucherCode");
            if (voucherCodeFromIntent != null && !voucherCodeFromIntent.trim().isEmpty()) {
                edtVoucherCode.setText(voucherCodeFromIntent);
                applyVoucher();
            }
        }
        
        // TODO: Uncomment khi đã tải được MoMo SDK thủ công
        /*
        if (requestCode == MOMO_REQUEST_CODE) {
            MoMoPaymentResponse response = MoMoPayment.getResponse(data);
            if (response != null) {
                if (response.getStatus() == MoMoPaymentResponse.STATUS_SUCCESS) {
                    // Thanh toán thành công
                    Log.d("MoMo", "Payment succeeded from onActivityResult");
                    
                    // Xóa giỏ hàng
                    cartManager.clearCart();
                    
                    // Hiển thị thông báo
                    ToastManager.showToast(this, 
                        "Thanh toán thành công qua MoMo!", 
                        Toast.LENGTH_LONG);
                    
                    // Chuyển đến màn hình đơn hàng
                    android.content.Intent intent = new android.content.Intent(
                        this, DonHangActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                                   android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Thanh toán thất bại hoặc bị hủy
                    Log.e("MoMo", "Payment failed from onActivityResult - Status: " + response.getStatus());
                    
                    String errorMsg = "Thanh toán thất bại";
                    if (response.getMessage() != null) {
                        errorMsg = response.getMessage();
                    }
                    
                    ToastManager.showToast(this, errorMsg, Toast.LENGTH_LONG);
                    
                    btnThanhToan.setEnabled(true);
                    btnThanhToan.setText("Thanh toán");
                }
            } else {
                // User có thể đã hủy thanh toán
                Log.d("MoMo", "Payment canceled by user");
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");
            }
        }
        */
    }
    
    /**
     * Xử lý deep link từ ZaloPay app khi thanh toán xong
     * ZaloPay SDK sẽ tự động gọi PayOrderListener callback, nhưng deep link này
     * đảm bảo app được mở lại nếu user quay về từ ZaloPay app
     */
    private void handleZaloPayDeepLink() {
        android.content.Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            android.net.Uri data = intent.getData();
            if (data != null && "demozpdk".equals(data.getScheme()) && "app".equals(data.getHost())) {
                // ZaloPay app đã mở lại app qua deep link
                Log.d("ZaloPay", "Received deep link from ZaloPay: " + data.toString());
                
                // ZaloPay SDK đã tự động xử lý callback qua PayOrderListener
                // Deep link này chỉ đảm bảo app được mở lại
                // Có thể check payment status từ server nếu cần
                String appTransId = data.getQueryParameter("app_trans_id");
                String orderId = data.getQueryParameter("orderId");
                if (orderId != null && !orderId.isEmpty()) {
                    // Check payment status từ server
                    checkZaloPayPaymentStatus(orderId);
                } else if (appTransId != null && !appTransId.isEmpty()) {
                    // Nếu chỉ có app_trans_id, có thể query order từ database
                    Log.d("ZaloPay", "Received app_trans_id from deep link: " + appTransId);
                }
            }
        }
    }
    
    /**
     * Kiểm tra trạng thái thanh toán ZaloPay từ server
     * Sử dụng khi quay lại app từ ZaloPay hoặc khi cần verify payment status
     */
    private void checkZaloPayPaymentStatus(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            Log.w("ZaloPay", "Cannot check payment status: orderId is null or empty");
            return;
        }
        
        Log.d("ZaloPay", "Checking payment status for order: " + orderId);
        
        Call<ZaloPayCreateResponse> call = paymentService.getZaloPayStatus(orderId);
        call.enqueue(new Callback<ZaloPayCreateResponse>() {
            @Override
            public void onResponse(Call<ZaloPayCreateResponse> call, Response<ZaloPayCreateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ZaloPayCreateResponse statusResponse = response.body();
                    Log.d("ZaloPay", "Payment status response: " + new com.google.gson.Gson().toJson(statusResponse));
                    
                    // Backend trả về payment status trong response
                    // Có thể có các field như: paymentStatus, paymentStatusMessage, etc.
                    // Tùy vào backend implementation, có thể cần parse thêm
                    
                    // Nếu thanh toán thành công, xóa giỏ hàng và chuyển màn hình
                    // (Thường thì PayOrderListener đã xử lý rồi, nhưng đây là backup)
                    if (statusResponse.isSuccess()) {
                        Log.d("ZaloPay", "Payment confirmed successful from server");
                        // Không cần làm gì thêm vì PayOrderListener đã xử lý
                    }
                } else {
                    Log.w("ZaloPay", "Failed to check payment status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ZaloPayCreateResponse> call, Throwable t) {
                Log.e("ZaloPay", "Error checking payment status: " + t.getMessage());
                // Không cần hiển thị lỗi cho user vì PayOrderListener đã xử lý
            }
        });
    }
    
    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleZaloPayDeepLink();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Khi quay lại app từ ZaloPay, có thể check payment status nếu cần
        // (Thường thì PayOrderListener đã xử lý rồi)
        handleZaloPayDeepLink();
    }
    
    /**
     * Áp dụng voucher từ mã nhập vào
     */
    private void applyVoucher() {
        String code = edtVoucherCode.getText().toString().trim();
        if (code.isEmpty()) {
            showToast("Vui lòng nhập mã voucher");
            return;
        }
        
        // Tính tổng tiền hiện tại - phải là final để dùng trong inner class
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        final double finalTotal = total;
        
        // Validate voucher với backend
        Call<ApiResponse<VoucherResponse>> call = voucherService.validateCode(code);
        call.enqueue(new Callback<ApiResponse<VoucherResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<VoucherResponse>> call, Response<ApiResponse<VoucherResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<VoucherResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        VoucherResponse voucher = apiResponse.getData();
                        
                        // Kiểm tra điều kiện đơn hàng tối thiểu
                        if (voucher.getMinOrderAmount() != null && finalTotal < voucher.getMinOrderAmount()) {
                            showToast("Đơn hàng tối thiểu " + formatPrice(voucher.getMinOrderAmount()) + " để sử dụng voucher này");
                            return;
                        }
                        
                        // Lưu voucher đã chọn
                        selectedVoucher = voucher;
                        voucherCode = voucher.getCode();
                        
                        // Hiển thị thông tin voucher
                        String discountText = "";
                        if ("percentage".equals(voucher.getDiscountType())) {
                            discountText = "Giảm " + voucher.getDiscount().intValue() + "%";
                        } else {
                            discountText = "Giảm " + formatPrice(voucher.getDiscount());
                        }
                        
                        txtVoucherInfo.setText("✓ Đã áp dụng: " + voucher.getName() + " - " + discountText);
                        txtVoucherInfo.setVisibility(View.VISIBLE);
                        txtVoucherInfo.setTextColor(getResources().getColor(R.color.primary_blue));
                        
                        showToast("Áp dụng voucher thành công!");
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Voucher không hợp lệ";
                        showToast(errorMsg);
                        clearVoucher();
                    }
                } else {
                    String errorMsg = "Không thể kiểm tra voucher";
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            // Parse error message nếu có
                            if (errorBody.contains("message")) {
                                // Có thể parse JSON nếu cần
                            }
                        } catch (Exception e) {
                            Log.e("Voucher", "Error parsing error body", e);
                        }
                    }
                    showToast(errorMsg);
                    clearVoucher();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<VoucherResponse>> call, Throwable t) {
                showToast("Lỗi kết nối: " + t.getMessage());
                clearVoucher();
            }
        });
    }
    
    /**
     * Xóa voucher đã chọn
     */
    private void clearVoucher() {
        selectedVoucher = null;
        voucherCode = null;
        txtVoucherInfo.setVisibility(View.GONE);
        edtVoucherCode.setText("");
    }
    
    /**
     * Format giá tiền
     */
    private String formatPrice(Double price) {
        if (price == null) return "0 VNĐ";
        return String.format(java.util.Locale.getDefault(), "%.0f VNĐ", price);
    }

    /**
     * Load danh sách sản phẩm đã chọn từ SharedPreferences
     * Nếu không có thì load toàn bộ giỏ hàng (fallback)
     */
    private List<CartItem> loadSelectedItems() {
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("temp_cart", MODE_PRIVATE);
            String selectedItemsJson = prefs.getString("selected_items", null);
            
            if (selectedItemsJson != null && !selectedItemsJson.isEmpty()) {
                // Parse danh sách sản phẩm đã chọn
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.google.gson.reflect.TypeToken<List<CartItem>> typeToken = new com.google.gson.reflect.TypeToken<List<CartItem>>(){};
                List<CartItem> selectedItems = gson.fromJson(selectedItemsJson, typeToken.getType());
                
                // Xóa dữ liệu tạm sau khi đã load
                prefs.edit().remove("selected_items").apply();
                
                if (selectedItems != null && !selectedItems.isEmpty()) {
                    Log.d("ThanhToanActivity", "Loaded " + selectedItems.size() + " selected items");
                    return selectedItems;
                }
            }
        } catch (Exception e) {
            Log.e("ThanhToanActivity", "Error loading selected items: " + e.getMessage(), e);
        }
        
        // Fallback: Load toàn bộ giỏ hàng nếu không có selected items
        List<CartItem> allItems = cartManager.loadCart();
        Log.d("ThanhToanActivity", "Fallback: Loaded " + allItems.size() + " items from cart");
        return allItems;
    }
    
}

