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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import android.location.LocationManager;
import android.provider.Settings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.InvoiceItemRequest;
import fpoly.haideptrai.duan1.api.models.InvoiceRequest;
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;
import fpoly.haideptrai.duan1.api.models.MoMoCreateRequest;
import fpoly.haideptrai.duan1.api.models.MoMoCreateResponse;
import fpoly.haideptrai.duan1.api.models.VNPayCreateRequest;
import fpoly.haideptrai.duan1.api.models.VNPayCreateResponse;
import fpoly.haideptrai.duan1.api.models.VNPayStatusResponse;
import fpoly.haideptrai.duan1.api.models.ZaloPayCreateRequest;
import fpoly.haideptrai.duan1.api.models.ZaloPayCreateResponse;
    import fpoly.haideptrai.duan1.api.models.ZaloPayStatusResponse;
import fpoly.haideptrai.duan1.api.models.UserResponse;
import fpoly.haideptrai.duan1.api.models.UserInfo;
import fpoly.haideptrai.duan1.api.services.InvoiceService;
import fpoly.haideptrai.duan1.api.services.PaymentService;
import fpoly.haideptrai.duan1.api.services.UserService;
import fpoly.haideptrai.duan1.api.services.AuthService;
import fpoly.haideptrai.duan1.api.services.VoucherService;
    import fpoly.haideptrai.duan1.api.services.AddressService;
import fpoly.haideptrai.duan1.api.models.VoucherResponse;
    import fpoly.haideptrai.duan1.api.models.AddressResponse;
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
    private static final String MOMO_MERCHANT_CODE = "MOMO_TEST"; // Mã thương nhân (Sandbox)
    // Production: Thay bằng mã thương nhân production từ MoMo Developer Portal
    private static final int MOMO_REQUEST_CODE = 1002; // Request code cho MoMo payment
    private static final boolean MOMO_USE_SANDBOX = true; // Set false cho production
    
    // MoMo API Endpoints
    // Sandbox: https://test-payment.momo.vn
    // Production: https://payment.momo.vn
    // Backend sẽ tự động chọn endpoint dựa trên MOMO_ENVIRONMENT trong .env

    private TextInputEditText edtDiaChi, edtVoucherCode;
    private android.widget.AutoCompleteTextView spinnerDiaChiDaLuu;
    private android.widget.RadioGroup radioGroupAddressType;
    private android.widget.RadioButton radioChonDiaChiDaLuu, radioNhapDiaChiMoi;
    private com.google.android.material.textfield.TextInputLayout layoutDiaChiDaLuu, layoutNhapDiaChiMoi;
        private MaterialButton btnThanhToan, btnApDungVoucher, btnChonVoucher;
        private android.widget.TextView txtVoucherInfo, txtSubtotal, txtShippingFee, txtDiscount, txtTotal;
        private LinearLayout layoutVisa, layoutMastercard, layoutNganHang, layoutQR, layoutZaloPay, layoutMoMo, layoutVNPay, layoutCOD, layoutDiscount;
    private String selectedPaymentMethod = "COD"; // Default: COD (uppercase)
    
    private InvoiceService invoiceService;
    private PaymentService paymentService;
    private UserService userService;
    private AuthService authService;
    private VoucherService voucherService;
        private AddressService addressService;
    
    // Voucher
    private VoucherResponse selectedVoucher;
    private String voucherCode;

        // Address
        private AddressResponse selectedAddress;
        private String selectedAddressId; // ID của địa chỉ đã lưu (nếu có)
        private List<AddressResponse> savedAddresses = new ArrayList<>();
    private CartManager cartManager;
    private SessionManager sessionManager;
    private List<CartItem> cartItems;

        // ZaloPay: Lưu orderId và orderNumber để check khi quay lại app
        private String currentZaloPayOrderId;
        private String currentZaloPayOrderNumber;
        private String currentVNPayOrderId;
        private String currentVNPayOrderNumber;
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
        authService = ApiClient.getClient().create(AuthService.class);
        voucherService = ApiClient.getClient().create(VoucherService.class);
            addressService = ApiClient.getClient().create(AddressService.class);
        cartManager = new CartManager(this);
        sessionManager = new SessionManager(this);
        
        // Lấy danh sách sản phẩm đã chọn từ SharedPreferences (nếu có)
        // Nếu không có thì load toàn bộ giỏ hàng (fallback)
        cartItems = loadSelectedItems();

            // Load danh sách địa chỉ đã lưu
            loadAddresses();
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // ZaloPay SDK đã được khởi tạo trong MyApplication.onCreate()
        // Nếu cần re-init với AppID khác, có thể gọi lại:
        // ZaloPaySDK.init(ZALOPAY_APP_ID, Environment.SANDBOX);
        
        // MoMo SDK sẽ được khởi tạo khi gọi requestPayment()
        
        setupClickListeners();

            // Cập nhật tổng tiền khi activity được tạo
            updateTotalPrice();
        
        // Xử lý deep link từ ZaloPay app
        handleZaloPayDeepLink();
    }

    private void initViews() {
        edtDiaChi = findViewById(R.id.edtDiaChi);
        edtVoucherCode = findViewById(R.id.edtVoucherCode);
        spinnerDiaChiDaLuu = findViewById(R.id.spinnerDiaChiDaLuu);
        radioGroupAddressType = findViewById(R.id.radioGroupAddressType);
        radioChonDiaChiDaLuu = findViewById(R.id.radioChonDiaChiDaLuu);
        radioNhapDiaChiMoi = findViewById(R.id.radioNhapDiaChiMoi);
        layoutDiaChiDaLuu = findViewById(R.id.layoutDiaChiDaLuu);
        layoutNhapDiaChiMoi = findViewById(R.id.layoutNhapDiaChiMoi);
        btnThanhToan = findViewById(R.id.btnThanhToan);
        btnApDungVoucher = findViewById(R.id.btnApDungVoucher);
        btnChonVoucher = findViewById(R.id.btnChonVoucher);
        txtVoucherInfo = findViewById(R.id.txtVoucherInfo);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtShippingFee = findViewById(R.id.txtShippingFee);
        txtDiscount = findViewById(R.id.txtDiscount);
        txtTotal = findViewById(R.id.txtTotal);
        layoutDiscount = findViewById(R.id.layoutDiscount);
        layoutVisa = findViewById(R.id.layoutVisa);
        layoutMastercard = findViewById(R.id.layoutMastercard);
        layoutNganHang = findViewById(R.id.layoutNganHang);
        layoutQR = findViewById(R.id.layoutQR);
        layoutZaloPay = findViewById(R.id.layoutZaloPay);
        layoutMoMo = findViewById(R.id.layoutMoMo);
        layoutVNPay = findViewById(R.id.layoutVNPay);
        layoutCOD = findViewById(R.id.layoutCOD);
    }

    private void setupClickListeners() {
        // Setup AutoCompleteTextView cho địa chỉ đã lưu
        setupAddressSpinner();
        
        // Setup radio group listener
        setupRadioGroupListener();
        
        // Đảm bảo layout địa chỉ đã lưu luôn hiển thị khi mặc định chọn
        if (radioChonDiaChiDaLuu != null && radioChonDiaChiDaLuu.isChecked()) {
            layoutDiaChiDaLuu.setVisibility(android.view.View.VISIBLE);
            layoutNhapDiaChiMoi.setVisibility(android.view.View.GONE);
        }
    }
    
    /**
     * Setup listener cho RadioGroup (tách riêng để có thể restore sau khi disable)
     */
    private void setupRadioGroupListener() {
        radioGroupAddressType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioChonDiaChiDaLuu) {
                // Chọn địa chỉ đã lưu
                layoutDiaChiDaLuu.setVisibility(android.view.View.VISIBLE);
                layoutNhapDiaChiMoi.setVisibility(android.view.View.GONE);
                // Clear địa chỉ mới nhập
                edtDiaChi.setText("");
                edtDiaChi.clearFocus();
            } else if (checkedId == R.id.radioNhapDiaChiMoi) {
                // Nhập địa chỉ mới
                layoutDiaChiDaLuu.setVisibility(android.view.View.GONE);
                layoutNhapDiaChiMoi.setVisibility(android.view.View.VISIBLE);
                // Clear địa chỉ đã chọn
                selectedAddress = null;
                selectedAddressId = null;
                if (spinnerDiaChiDaLuu != null) {
                    spinnerDiaChiDaLuu.setText("");
                }
                // Focus vào ô nhập địa chỉ
                edtDiaChi.requestFocus();
            }
        });
        
        // Xử lý khi nhập địa chỉ mới
        edtDiaChi.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Khi nhập địa chỉ mới, clear selectedAddressId
                if (s != null && s.length() > 0) {
                    selectedAddress = null;
                    selectedAddressId = null;
                }
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        layoutVisa.setOnClickListener(v -> selectPaymentMethod("VISA", layoutVisa));
        layoutMastercard.setOnClickListener(v -> selectPaymentMethod("MASTERCARD", layoutMastercard));
        layoutNganHang.setOnClickListener(v -> selectPaymentMethod("BANK", layoutNganHang));
        layoutQR.setOnClickListener(v -> selectPaymentMethod("QR", layoutQR));
        layoutZaloPay.setOnClickListener(v -> selectPaymentMethod("zalopay", layoutZaloPay));
        layoutMoMo.setOnClickListener(v -> selectPaymentMethod("momo", layoutMoMo));
        layoutVNPay.setOnClickListener(v -> selectPaymentMethod("vnpay", layoutVNPay));
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
            
            if (cartItems.isEmpty()) {
                showToast("Giỏ hàng trống");
                return;
            }
            
            // Kiểm tra địa chỉ: ưu tiên địa chỉ đã lưu (từ spinner), sau đó mới đến địa chỉ mới nhập
            String address = null;
            boolean isUsingSavedAddress = false;
            
            // Kiểm tra xem user đang chọn mode nào
            boolean isUsingSavedAddressMode = radioChonDiaChiDaLuu != null && radioChonDiaChiDaLuu.isChecked();
            boolean isUsingNewAddressMode = radioNhapDiaChiMoi != null && radioNhapDiaChiMoi.isChecked();
            
            if (isUsingSavedAddressMode) {
                // User đang chọn mode "Chọn địa chỉ đã lưu"
                if (selectedAddress != null) {
                    address = selectedAddress.getFullAddress();
                    isUsingSavedAddress = true;
                    Log.d("ThanhToan", "Using saved address: " + (selectedAddressId != null ? selectedAddressId : "user_profile") + " - " + address);
                    
                    // Nếu không có addressId (địa chỉ từ user profile), cần validate profile trước
                    if (selectedAddressId == null || selectedAddressId.trim().isEmpty()) {
                        // Địa chỉ từ user profile - cần validate profile có đầy đủ thông tin
                        validateUserProfileBeforePayment(address);
                        return;
                    }
                } else {
                    showToast("Vui lòng chọn địa chỉ đã lưu từ danh sách");
                    return;
                }
            } else if (isUsingNewAddressMode) {
                // User đang chọn mode "Nhập địa chỉ mới"
                address = edtDiaChi.getText().toString().trim();
                if (address.isEmpty()) {
                    showToast("Vui lòng nhập địa chỉ giao hàng");
                    return;
                }
                // Nếu nhập địa chỉ mới, cần kiểm tra user profile
                validateUserProfileBeforePayment(address);
                return;
            } else {
                // Fallback: kiểm tra cả hai
                if (selectedAddress != null) {
                    // Có địa chỉ đã chọn từ spinner
                    address = selectedAddress.getFullAddress();
                    isUsingSavedAddress = true;
                    Log.d("ThanhToan", "Using saved address (fallback): " + (selectedAddressId != null ? selectedAddressId : "user_profile") + " - " + address);
                    
                    // Nếu không có addressId (địa chỉ từ user profile), cần validate profile trước
                    if (selectedAddressId == null || selectedAddressId.trim().isEmpty()) {
                        // Địa chỉ từ user profile - cần validate profile có đầy đủ thông tin
                        validateUserProfileBeforePayment(address);
                        return;
                    }
                } else {
                    // Không có địa chỉ đã chọn, kiểm tra địa chỉ mới nhập
                    address = edtDiaChi.getText().toString().trim();
                    if (address.isEmpty()) {
                        showToast("Vui lòng chọn địa chỉ đã lưu hoặc nhập địa chỉ mới");
                        return;
                    }
                    // Nếu nhập địa chỉ mới, cần kiểm tra user profile
                    validateUserProfileBeforePayment(address);
                    return;
                }
            }
            
            // Nếu có địa chỉ đã lưu, tiếp tục thanh toán trực tiếp
            // Xử lý thanh toán ZaloPay, MoMo, VNPay riêng
            // Kiểm tra payment method (case-insensitive)
            if ("zalopay".equalsIgnoreCase(selectedPaymentMethod)) {
                processZaloPayPayment(address);
            } else if ("momo".equalsIgnoreCase(selectedPaymentMethod)) {
                processMoMoPayment(address);
            } else if ("vnpay".equalsIgnoreCase(selectedPaymentMethod)) {
                processVNPayPayment(address);
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
        layoutVNPay.setBackground(null);
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

        // Nếu có addressId: sử dụng địa chỉ đã lưu
        // Nếu không có addressId: backend sẽ tự động tạo từ User profile
        // Cho phép thanh toán với địa chỉ từ user profile hoặc địa chỉ đã nhập
        createInvoiceRequest(address);
    }

    /**
     * Tạo invoice request và gọi API
     * Nếu có addressId: sử dụng địa chỉ đã lưu
     * Nếu không có addressId: backend sẽ tự động tạo từ User profile
     */
    private void createInvoiceRequest(String address) {
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
        } else if ("vnpay".equalsIgnoreCase(selectedPaymentMethod)) {
            normalizedPaymentMethod = "vnpay"; // Keep lowercase for VNPay
        }
        request.setPaymentMethod(normalizedPaymentMethod);
        
        // Nếu có addressId, sử dụng địa chỉ đã lưu
        // Nếu không có addressId (địa chỉ từ user profile), backend sẽ tự động tạo từ User profile
        if (selectedAddressId != null && !selectedAddressId.trim().isEmpty()) {
            request.setAddressId(selectedAddressId);
            Log.d("ThanhToan", "Using saved address ID: " + selectedAddressId);
        } else {
            // Không set addressId - backend sẽ tự động tạo từ User profile
            Log.d("ThanhToan", "No addressId - backend will auto-create from User profile");
        }

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
                
                String errorMsg = "Lỗi kết nối server";
                if (t instanceof java.net.UnknownHostException) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg = "Kết nối timeout. Vui lòng thử lại.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra backend có đang chạy không.";
                } else if (t.getMessage() != null) {
                    errorMsg = "Lỗi: " + t.getMessage();
                }
                ToastManager.showToast(ThanhToanActivity.this, errorMsg, Toast.LENGTH_LONG);
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
                // Không tự động thêm city - để người dùng nhập đầy đủ hoặc backend validate
                // Nếu thiếu city, backend sẽ trả về lỗi yêu cầu nhập đầy đủ
            
            return shippingAddress;
        } catch (Exception e) {
            Log.e("ThanhToanActivity", "Error parsing address: " + e.getMessage(), e);
                // Fallback: dùng toàn bộ string làm address, không tự động thêm city
            InvoiceRequest.ShippingAddress shippingAddress = new InvoiceRequest.ShippingAddress();
            shippingAddress.setAddress(addressString);
                // Không set city - để người dùng nhập đầy đủ
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

        // Nếu có addressId: sử dụng địa chỉ đã lưu
        // Nếu không có addressId: backend sẽ tự động tạo từ User profile
        // Cho phép thanh toán với địa chỉ từ user profile hoặc địa chỉ đã nhập
        createZaloPayOrder(address);
    }
    
    /**
     * Tạo đơn hàng ZaloPay sau khi đã có đầy đủ thông tin
     * Nếu có addressId: sử dụng địa chỉ đã lưu
     * Nếu không có addressId: backend sẽ tự động tạo từ User profile
     */
    private void createZaloPayOrder(String address) {
        // Tạo request
        ZaloPayCreateRequest request = new ZaloPayCreateRequest();

        // Nếu có addressId, sử dụng địa chỉ đã lưu
        // Nếu không có addressId (địa chỉ từ user profile), backend sẽ tự động tạo từ User profile
        if (selectedAddressId != null && !selectedAddressId.trim().isEmpty()) {
            request.setAddressId(selectedAddressId);
            Log.d("ZaloPay", "Using saved address ID: " + selectedAddressId);
        } else {
            // Không set addressId - backend sẽ tự động tạo từ User profile
            Log.d("ZaloPay", "No addressId - backend will auto-create from User profile");
        }

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
        Log.d("ZaloPay", "Address ID: " + (request.getAddressId() != null ? request.getAddressId() : "null"));
        Log.d("ZaloPay", "Cart items count: " + cartItems.size());
        Log.d("ZaloPay", "Request items count: " + requestItems.size());
        Log.d("ZaloPay", "Total amount: " + total);
        Log.d("ZaloPay", "Voucher code: " + (voucherCode != null ? voucherCode : "null"));
            Log.d("ZaloPay", "Notes: " + (request.getNotes() != null ? request.getNotes() : "null"));

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

                            // Lưu orderId và orderNumber để check khi quay lại app
                            currentZaloPayOrderId = orderId;
                            currentZaloPayOrderNumber = orderNumber;
                        
                        // Mở ZaloPay app với zp_trans_token
                        final String finalOrderUrl = orderUrl;
                            final String finalOrderId = orderId;
                            final String finalOrderNumber = orderNumber;

                        ZaloPaySDK.getInstance().payOrder(
                            ThanhToanActivity.this,
                            zpTransToken,
                            "demozpdk://app",
                            new PayOrderListener() {
                                @Override
                                public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                                        // Thanh toán thành công - ZaloPay đã confirm
                                    Log.d("ZaloPay", "Payment succeeded - Transaction ID: " + transactionId + ", App Trans ID: " + appTransID);
                                    
                                    // Xóa giỏ hàng
                                    cartManager.clearCart();
                                    
                                        // Chuyển ngay đến màn hình đơn hàng (không cần chờ server verify)
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
                                            showZaloPayAppNotFoundDialog(finalOrderUrl, finalOrderId, finalOrderNumber);
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

                            // Bắt đầu polling để kiểm tra trạng thái thanh toán (backup mechanism)
                            startPaymentStatusPolling(orderId, orderNumber);
                    } else {
                        // Log chi tiết lỗi
                        Log.e("ZaloPay", "=== ZALOPAY CREATE ERROR ===");
                        Log.e("ZaloPay", "Success: " + zalopayResponse.isSuccess());
                        Log.e("ZaloPay", "Error: " + zalopayResponse.getError());
                        Log.e("ZaloPay", "Message: " + zalopayResponse.getMessage());
                            Log.e("ZaloPay", "Return code: " + zalopayResponse.getReturn_code());
                            Log.e("ZaloPay", "Sub return code: " + zalopayResponse.getSub_return_code());
                        Log.e("ZaloPay", "Response body: " + new com.google.gson.Gson().toJson(zalopayResponse));
                        
                            // Xử lý lỗi chi tiết theo sub_return_code
                            String userFriendlyMsg = handleZaloPayError(zalopayResponse);

                            // Hiển thị dialog thay vì toast để người dùng có thể đọc được hướng dẫn dài
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ThanhToanActivity.this);
                            builder.setTitle("Lỗi thanh toán ZaloPay")
                                   .setMessage(userFriendlyMsg)
                                   .setPositiveButton("Đã hiểu", null)
                                   .show();
                        btnThanhToan.setEnabled(true);
                        btnThanhToan.setText("Thanh toán");
                    }
                } else {
                        // Parse error response theo tài liệu API
                    String errorMsg = "Lỗi kết nối server";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("ZaloPay", "Error response code: " + response.code());
                            Log.e("ZaloPay", "Error response body: " + errorBody);
                            
                                // Kiểm tra các error messages theo tài liệu
                                if (errorBody.contains("Vui lòng cung cấp địa chỉ giao hàng")) {
                                    errorMsg = "Vui lòng chọn địa chỉ giao hàng!";
                                } else if (errorBody.contains("Giỏ hàng trống")) {
                                    errorMsg = "Giỏ hàng trống!";
                                } else if (errorBody.contains("chỉ còn") && errorBody.contains("sản phẩm trong kho")) {
                                    // Sản phẩm hết hàng
                                    errorMsg = errorBody.substring(errorBody.indexOf("\"message\":\"") + 11,
                                                                  errorBody.indexOf("\"", errorBody.indexOf("\"message\":\"") + 11));
                                } else if (errorBody.contains("Mã voucher không tồn tại") ||
                                          errorBody.contains("voucher không hợp lệ")) {
                                    errorMsg = "Mã voucher không hợp lệ!";
                                } else if (errorBody.contains("Không thể tạo đơn hàng thanh toán ZaloPay") ||
                                          errorBody.contains("Giao dịch thất bại") ||
                                          errorBody.contains("return_code")) {
                                    // Thử parse JSON error response để lấy return_code và sub_return_code
                                    try {
                                        com.google.gson.Gson gson = new com.google.gson.Gson();
                                        ZaloPayCreateResponse errorResponse = gson.fromJson(errorBody, ZaloPayCreateResponse.class);
                                        if (errorResponse != null) {
                                            errorMsg = handleZaloPayError(errorResponse);
                                        } else {
                                            // Fallback nếu không parse được
                                            if (errorBody.contains("return_code") && errorBody.contains("2")) {
                                                errorMsg = "❌ Lỗi cấu hình ZaloPay (return_code: 2)\n\n" +
                                                          "Đây là lỗi từ phía backend, không phải frontend.\n\n" +
                                                          "Vui lòng kiểm tra backend:\n" +
                                                          "1. File .env có đầy đủ:\n" +
                                                          "   - ZALOPAY_APP_ID\n" +
                                                          "   - ZALOPAY_KEY1\n" +
                                                          "   - ZALOPAY_KEY2\n" +
                                                          "2. Giá trị APP_ID, KEY1, KEY2 phải đúng từ ZaloPay Merchant Portal\n" +
                                                          "3. Kiểm tra backend logs để xem chi tiết lỗi từ ZaloPay API\n" +
                                                          "4. Đảm bảo backend đã restart sau khi cập nhật .env";
                                            } else {
                                                errorMsg = "Không thể tạo đơn hàng thanh toán ZaloPay.\n" +
                                                          "Vui lòng thử lại sau hoặc kiểm tra backend logs.";
                                            }
                                        }
                                    } catch (Exception jsonEx) {
                                        // Không parse được JSON, dùng fallback
                                        Log.e("ZaloPay", "Error parsing ZaloPay error response", jsonEx);
                                        if (errorBody.contains("return_code") && errorBody.contains("2")) {
                                            errorMsg = "❌ Lỗi cấu hình ZaloPay (return_code: 2)\n\n" +
                                                      "Đây là lỗi từ phía backend, không phải frontend.\n\n" +
                                                      "Vui lòng kiểm tra backend:\n" +
                                                      "1. File .env có đầy đủ:\n" +
                                                      "   - ZALOPAY_APP_ID\n" +
                                                      "   - ZALOPAY_KEY1\n" +
                                                      "   - ZALOPAY_KEY2\n" +
                                                      "2. Giá trị APP_ID, KEY1, KEY2 phải đúng từ ZaloPay Merchant Portal\n" +
                                                      "3. Kiểm tra backend logs để xem chi tiết lỗi từ ZaloPay API\n" +
                                                      "4. Đảm bảo backend đã restart sau khi cập nhật .env";
                                        } else {
                                            errorMsg = "Không thể tạo đơn hàng thanh toán ZaloPay.\n" +
                                                      "Vui lòng thử lại sau hoặc kiểm tra backend logs.";
                                        }
                                    }
                                } else if (errorBody.contains("Cannot POST") || errorBody.contains("<!DOCTYPE html>")) {
                                    errorMsg = "Endpoint không tồn tại. Vui lòng kiểm tra backend server!";
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

                        // Hiển thị dialog cho lỗi dài, toast cho lỗi ngắn
                        if (errorMsg.contains("Lỗi cấu hình ZaloPay") || errorMsg.length() > 100) {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ThanhToanActivity.this);
                            builder.setTitle("Lỗi thanh toán ZaloPay")
                                   .setMessage(errorMsg)
                                   .setPositiveButton("Đã hiểu", null)
                                   .show();
                        } else {
                            ToastManager.showToast(ThanhToanActivity.this, errorMsg, Toast.LENGTH_LONG);
                        }

                    btnThanhToan.setEnabled(true);
                    btnThanhToan.setText("Thanh toán");
                }
            }

            @Override
            public void onFailure(Call<ZaloPayCreateResponse> call, Throwable t) {
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");
                Log.e("ZaloPay", "Network error: " + t.getMessage(), t);
                
                String errorMsg = "Lỗi kết nối server";
                if (t instanceof java.net.UnknownHostException) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg = "Kết nối timeout. Vui lòng thử lại.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra backend có đang chạy không.";
                } else if (t.getMessage() != null) {
                    errorMsg = "Lỗi: " + t.getMessage();
                }
                ToastManager.showToast(ThanhToanActivity.this, errorMsg, Toast.LENGTH_LONG);
                ToastManager.showToast(ThanhToanActivity.this, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

        private void showZaloPayAppNotFoundDialog(String orderUrl, String orderId, String orderNumber) {
        btnThanhToan.setEnabled(true);
        btnThanhToan.setText("Thanh toán");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chưa cài ZaloPay");
            builder.setMessage("Thiết bị của bạn chưa cài ứng dụng ZaloPay. " +
                "Bạn có thể cài app hoặc mở liên kết thanh toán trong trình duyệt.");
            builder.setPositiveButton("Mở liên kết", (dialog, which) -> {
                openZaloPayOrderUrl(orderUrl);
                // Bắt đầu polling khi mở web
                if (orderId != null) {
                    startPaymentStatusPolling(orderId, orderNumber);
                }
            });
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
         * Xử lý lỗi ZaloPay theo return_code và sub_return_code
         * Theo tài liệu: https://developers.zalopay.vn/docs/api/
         */
        private String handleZaloPayError(ZaloPayCreateResponse response) {
            String errorMsg = response.getError() != null ? response.getError() :
                             (response.getMessage() != null ? response.getMessage() : "Tạo đơn hàng thất bại");

            Integer returnCode = response.getReturn_code();
            Integer subReturnCode = response.getSub_return_code();

            // Nếu có return_code = 2 (Giao dịch thất bại), xử lý theo sub_return_code
            if (returnCode != null && returnCode == 2) {
                if (subReturnCode != null) {
                    switch (subReturnCode) {
                        case -401:
                            return "❌ Dữ liệu yêu cầu không hợp lệ\n\n" +
                                   "Vui lòng kiểm tra lại thông tin đơn hàng và thử lại.";

                        case -402:
                            return "❌ Lỗi xác thực (Chữ ký không hợp lệ)\n\n" +
                                   "Đây là lỗi từ phía backend. Vui lòng:\n" +
                                   "1. Kiểm tra ZALOPAY_KEY1 và ZALOPAY_KEY2 trong backend .env\n" +
                                   "2. Kiểm tra backend logs để xem chi tiết\n" +
                                   "3. Đảm bảo backend đã restart sau khi cập nhật .env";

                        case -3:
                            return "❌ Ứng dụng không hợp lệ\n\n" +
                                   "Đây là lỗi cấu hình backend. Vui lòng:\n" +
                                   "1. Kiểm tra ZALOPAY_APP_ID trong backend .env\n" +
                                   "2. Xác nhận APP_ID đúng từ ZaloPay Merchant Portal\n" +
                                   "3. Đảm bảo backend đã restart sau khi cập nhật .env";

                        case -5:
                            return "❌ Số tiền không hợp lệ\n\n" +
                                   "Số tiền thanh toán phải lớn hơn 0. Vui lòng kiểm tra lại giỏ hàng.";

                        case -68:
                            return "❌ Mã giao dịch bị trùng\n\n" +
                                   "Mã giao dịch này đã được sử dụng. Vui lòng thử lại sau vài giây.";

                        default:
                            return "❌ Lỗi thanh toán ZaloPay (return_code: 2, sub_return_code: " + subReturnCode + ")\n\n" +
                                   "Lỗi: " + errorMsg + "\n\n" +
                                   "Vui lòng thử lại sau hoặc liên hệ hỗ trợ.";
                    }
                } else {
                    // return_code = 2 nhưng không có sub_return_code
                    return "❌ Lỗi cấu hình ZaloPay (return_code: 2)\n\n" +
                           "Đây là lỗi từ phía backend, không phải frontend.\n\n" +
                           "Vui lòng kiểm tra backend:\n" +
                           "1. File .env có đầy đủ:\n" +
                           "   - ZALOPAY_APP_ID\n" +
                           "   - ZALOPAY_KEY1\n" +
                           "   - ZALOPAY_KEY2\n" +
                           "2. Giá trị APP_ID, KEY1, KEY2 phải đúng từ ZaloPay Merchant Portal\n" +
                           "3. Kiểm tra backend logs để xem chi tiết lỗi từ ZaloPay API\n" +
                           "4. Đảm bảo backend đã restart sau khi cập nhật .env";
                }
            }

            // Nếu có error message chứa "Giao dịch thất bại"
            if (errorMsg.contains("Giao dịch thất bại")) {
                return "❌ Lỗi cấu hình ZaloPay\n\n" +
                       "Đây là lỗi từ phía backend, không phải frontend.\n\n" +
                       "Vui lòng kiểm tra backend:\n" +
                       "1. File .env có đầy đủ:\n" +
                       "   - ZALOPAY_APP_ID\n" +
                       "   - ZALOPAY_KEY1\n" +
                       "   - ZALOPAY_KEY2\n" +
                       "2. Giá trị APP_ID, KEY1, KEY2 phải đúng từ ZaloPay Merchant Portal\n" +
                       "3. Kiểm tra backend logs để xem chi tiết lỗi từ ZaloPay API\n" +
                       "4. Đảm bảo backend đã restart sau khi cập nhật .env";
            }

            // Trả về error message mặc định
            return errorMsg;
        }
    
    /**
     * Parse địa chỉ string thành ShippingAddress object
     * Format đơn giản: "Địa chỉ, Phường, Quận, Thành phố"
     */
        /**
         * Validate địa chỉ có đầy đủ thông tin không
         * Yêu cầu: fullName, phone, address (city là tùy chọn)
         */
        private boolean isAddressValid(ZaloPayCreateRequest.ShippingAddress address) {
            if (address == null) {
                return false;
            }

            // Yêu cầu: fullName, phone, address (city là tùy chọn)
            boolean hasAddress = address.getAddress() != null && !address.getAddress().trim().isEmpty();
            boolean hasFullName = address.getFullName() != null && !address.getFullName().trim().isEmpty();
            boolean hasPhone = address.getPhone() != null && !address.getPhone().trim().isEmpty();

            // Log để debug
            Log.d("ZaloPay", "Address validation:");
            Log.d("ZaloPay", "  - FullName: " + (hasFullName ? address.getFullName() : "MISSING"));
            Log.d("ZaloPay", "  - Phone: " + (hasPhone ? address.getPhone() : "MISSING"));
            Log.d("ZaloPay", "  - Address: " + (hasAddress ? address.getAddress() : "MISSING"));
            Log.d("ZaloPay", "  - City: " + (address.getCity() != null && !address.getCity().trim().isEmpty() ? address.getCity() : "optional"));
            Log.d("ZaloPay", "  - Ward: " + (address.getWard() != null && !address.getWard().trim().isEmpty() ? address.getWard() : "optional"));
            Log.d("ZaloPay", "  - District: " + (address.getDistrict() != null && !address.getDistrict().trim().isEmpty() ? address.getDistrict() : "optional"));

            // Yêu cầu: fullName, phone, address
            return hasAddress && hasFullName && hasPhone;
        }

    private ZaloPayCreateRequest.ShippingAddress parseAddress(String addressString) {
        if (addressString == null || addressString.trim().isEmpty()) {
            return null;
        }
        
        try {
            ZaloPayCreateRequest.ShippingAddress shippingAddress = new ZaloPayCreateRequest.ShippingAddress();
            
            // Parse đơn giản: tách bằng dấu phẩy
                // Format: "Số nhà, Đường, Phường/Xã, Quận/Huyện, Thành phố"
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
                } else {
                    // Nếu không có city từ parse, thử extract từ địa chỉ
                    String extractedCity = extractCityFromAddress(addressString);
                    if (extractedCity != null && !extractedCity.isEmpty()) {
                        shippingAddress.setCity(extractedCity);
                    } else {
                        // Nếu không extract được, set rỗng (backend yêu cầu field này)
                        shippingAddress.setCity("");
                    }
                }

                // Đảm bảo các field optional luôn có giá trị (không null)
                if (shippingAddress.getWard() == null) {
                    shippingAddress.setWard("");
                }
                if (shippingAddress.getDistrict() == null) {
                    shippingAddress.setDistrict("");
            }
            
            // Nếu thiếu thông tin, dùng toàn bộ string làm address
            if (shippingAddress.getAddress() == null || shippingAddress.getAddress().isEmpty()) {
                shippingAddress.setAddress(addressString);
            }

                // Log để debug
                Log.d("ZaloPay", "Parsed address:");
                Log.d("ZaloPay", "  - Original: " + addressString);
                Log.d("ZaloPay", "  - Address: " + shippingAddress.getAddress());
                Log.d("ZaloPay", "  - Ward: " + (shippingAddress.getWard() != null ? shippingAddress.getWard() : "null"));
                Log.d("ZaloPay", "  - District: " + (shippingAddress.getDistrict() != null ? shippingAddress.getDistrict() : "null"));
                Log.d("ZaloPay", "  - City: " + (shippingAddress.getCity() != null ? shippingAddress.getCity() : "null"));
            
            return shippingAddress;
        } catch (Exception e) {
            Log.e("ThanhToanActivity", "Error parsing address: " + e.getMessage(), e);
            // Fallback: dùng toàn bộ string làm address
            ZaloPayCreateRequest.ShippingAddress shippingAddress = new ZaloPayCreateRequest.ShippingAddress();
            shippingAddress.setAddress(addressString);
                // Đảm bảo các field optional luôn có giá trị (không null)
                shippingAddress.setWard("");
                shippingAddress.setDistrict("");
                shippingAddress.setCity(""); // Backend yêu cầu field này, set rỗng nếu không có
            return shippingAddress;
        }
    }

        /**
         * Extract city từ địa chỉ nếu có thể
         * Ví dụ: "quynh luu nghe an" -> "nghệ an"
         */
        private String extractCityFromAddress(String address) {
            if (address == null || address.trim().isEmpty()) {
                return "";
            }

            String addressLower = address.toLowerCase().trim();

            // Danh sách các tỉnh/thành phố phổ biến (có thể mở rộng)
            String[] cities = {
                "hồ chí minh", "ho chi minh", "hcm", "sài gòn", "sai gon",
                "hà nội", "ha noi", "hanoi",
                "đà nẵng", "da nang", "danang",
                "hải phòng", "hai phong", "haiphong",
                "cần thơ", "can tho", "cantho",
                "nghệ an", "nghe an", "nghean",
                "thanh hóa", "thanh hoa", "thanhhoa",
                "quảng nam", "quang nam", "quangnam",
                "quảng ngãi", "quang ngai", "quangngai",
                "bình định", "binh dinh", "binhdinh",
                "phú yên", "phu yen", "phuyen",
                "khánh hòa", "khanh hoa", "khanhhoa",
                "ninh thuận", "ninh thuan", "ninhthuan",
                "bình thuận", "binh thuan", "binhthuan",
                "bà rịa - vũng tàu", "ba ria - vung tau", "baria vungtau",
                "đồng nai", "dong nai", "dongnai",
                "bình dương", "binh duong", "binhduong",
                "tây ninh", "tay ninh", "tayninh",
                "bình phước", "binh phuoc", "binhphuoc",
                "long an", "longan",
                "tiền giang", "tien giang", "tiengiang",
                "bến tre", "ben tre", "bentre",
                "trà vinh", "tra vinh", "travinh",
                "vĩnh long", "vinh long", "vinhlong",
                "đồng tháp", "dong thap", "dongthap",
                "an giang", "an giang", "angiang",
                "kiên giang", "kien giang", "kiengiang",
                "cà mau", "ca mau", "camau",
                "bạc liêu", "bac lieu", "baclieu",
                "sóc trăng", "soc trang", "soctrang"
            };

            // Tìm city trong địa chỉ
            for (String city : cities) {
                if (addressLower.contains(city)) {
                    // Trả về city với chữ hoa đầu từ
                    String[] words = city.split("\\s+");
                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < words.length; i++) {
                        if (i > 0) result.append(" ");
                        if (words[i].length() > 0) {
                            result.append(words[i].substring(0, 1).toUpperCase())
                                  .append(words[i].substring(1));
                        }
                    }
                    return result.toString();
                }
            }

            // Nếu không tìm thấy, thử tìm từ cuối cùng (có thể là tỉnh/thành phố)
            String[] words = address.trim().split("\\s+");
            if (words.length >= 2) {
                // Lấy 2 từ cuối cùng
                String lastTwo = words[words.length - 2] + " " + words[words.length - 1];
                return lastTwo;
            } else if (words.length == 1) {
                return words[0];
            }

            return "";
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

            // Kiểm tra GPS có bật không
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled && !isNetworkEnabled) {
                // GPS và Network Location đều chưa bật
                showGpsSettingsDialog();
                return;
            }
        
        // Method này không còn được sử dụng - đã thay bằng chọn địa chỉ đã lưu
        
            // Tạo LocationRequest với yêu cầu độ chính xác cao (GPS)
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);

            // Kiểm tra Location Settings
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            builder.setAlwaysShow(true); // Hiển thị dialog yêu cầu bật GPS nếu cần

            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            settingsClient.checkLocationSettings(builder.build())
                    .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            // Location settings OK, lấy vị trí
                            if (ContextCompat.checkSelfPermission(ThanhToanActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.getLastLocation()
                                        .addOnSuccessListener(ThanhToanActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Method này không còn được sử dụng
                        
                        if (location != null) {
                            // Log tọa độ để debug
                            double lat = location.getLatitude();
                            double lng = location.getLongitude();
                            Log.d("Location", "=== GPS Location ===");
                            Log.d("Location", "Latitude: " + lat);
                            Log.d("Location", "Longitude: " + lng);
                            Log.d("Location", "Accuracy: " + location.getAccuracy() + " meters");
                            Log.d("Location", "Provider: " + location.getProvider());
                            
                            // Kiểm tra tọa độ có hợp lý không trước khi geocode
                            boolean isInVietnamBounds = (lat >= 8.0 && lat <= 23.5 && 
                                                          lng >= 102.0 && lng <= 110.0);
                            
                            if (!isInVietnamBounds) {
                                Log.e("Location", "❌ GPS coordinates are NOT in Vietnam bounds!");
                                Log.e("Location", "   This might be emulator default location (Google HQ)");
                                Log.e("Location", "   If using emulator, please set location manually");
                                
                        // Method này không còn được sử dụng
                                
                                new AlertDialog.Builder(ThanhToanActivity.this)
                                        .setTitle("Vị trí không hợp lệ")
                                        .setMessage("GPS trả về tọa độ không phải ở Việt Nam.\n\n" +
                                                   "Tọa độ: " + String.format("%.6f, %.6f", lat, lng) + "\n\n" +
                                                   "Nếu đang dùng emulator:\n" +
                                                   "• Vào Extended Controls (⋯)\n" +
                                                   "• Chọn Location\n" +
                                                   "• Set tọa độ ở Việt Nam (ví dụ: 10.762622, 106.660172 - Hồ Chí Minh)\n\n" +
                                                   "Nếu dùng thiết bị thật:\n" +
                                                   "• Bật GPS và Location Accuracy\n" +
                                                   "• Đảm bảo bạn đang ở Việt Nam\n\n" +
                                                   "Hoặc nhập địa chỉ thủ công.")
                                        .setPositiveButton("Nhập địa chỉ", (dialog, which) -> {
                                            edtDiaChi.setFocusable(true);
                                            edtDiaChi.setFocusableInTouchMode(true);
                                            edtDiaChi.requestFocus();
                                        })
                                        .setNegativeButton("Hủy", null)
                                        .show();
                                return;
                            }
                            
                            // Chuyển đổi tọa độ thành địa chỉ
                            getAddressFromLocation(lat, lng);
                        } else {
                            // Nếu không có last location, request location update
                            requestLocationUpdate(locationRequest);
                        }
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Location settings không OK (GPS chưa bật)
                        // Method này không còn được sử dụng
                            showGpsSettingsDialog();
                    }
                });
    }

        // LocationCallback để lưu reference và có thể remove sau
        private LocationCallback locationCallback;

        /**
         * Request location update nếu không có last location
         */
        private void requestLocationUpdate(LocationRequest locationRequest) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            // Tạo LocationCallback mới
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null && locationResult.getLastLocation() != null) {
                        Location location = locationResult.getLastLocation();
                        // Dừng location updates sau khi có vị trí
                        if (locationCallback != null) {
                            fusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                        // Chuyển đổi tọa độ thành địa chỉ
                        getAddressFromLocation(location.getLatitude(), location.getLongitude());
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }

        /**
         * Hiển thị dialog yêu cầu bật GPS
         */
        private void showGpsSettingsDialog() {
            new AlertDialog.Builder(this)
                    .setTitle("Cần bật GPS")
                    .setMessage("Để lấy vị trí hiện tại, vui lòng bật GPS trên thiết bị của bạn.\n\n" +
                               "Bạn có muốn mở cài đặt để bật GPS không?")
                    .setPositiveButton("Mở cài đặt", (dialog, which) -> {
                        // Mở Settings để bật GPS
                        openLocationSettings();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        /**
         * Mở Settings để bật GPS với error handling
         */
        private void openLocationSettings() {
            try {
                // Thử mở Location Source Settings (màn hình bật GPS)
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                
                // Kiểm tra xem có app nào có thể xử lý intent này không
                if (settingsIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(settingsIntent);
                } else {
                    // Fallback: Mở Settings chung
                    try {
                        Intent generalSettingsIntent = new Intent(Settings.ACTION_SETTINGS);
                        if (generalSettingsIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(generalSettingsIntent);
                            showToast("Vui lòng vào 'Location' để bật GPS");
                        } else {
                            showToast("Không thể mở cài đặt. Vui lòng bật GPS thủ công trong Settings.");
                        }
                    } catch (Exception e2) {
                        Log.e("Location", "Error opening general settings: " + e2.getMessage(), e2);
                        showToast("Không thể mở cài đặt. Vui lòng bật GPS thủ công trong Settings.");
                    }
                }
            } catch (Exception e) {
                Log.e("Location", "Error opening location settings: " + e.getMessage(), e);
                showToast("Không thể mở cài đặt. Vui lòng bật GPS thủ công trong Settings.");
            }
        }
    
    /**
     * Chuyển đổi tọa độ thành địa chỉ (Reverse Geocoding)
     * Chạy trên background thread để tránh ANR
     */
    private void getAddressFromLocation(double latitude, double longitude) {
        // Hiển thị loading
        runOnUiThread(() -> {
            // Method này không còn được sử dụng
        });
        
        // Chạy trên background thread
        new Thread(() -> {
            android.os.Handler mainHandler = new android.os.Handler(getMainLooper());
            
            try {
                // Thử dùng locale tiếng Việt trước, nếu không được thì dùng default
                Geocoder geocoder;
                try {
                    geocoder = new Geocoder(this, new java.util.Locale("vi", "VN"));
                } catch (Exception e) {
                    Log.w("Location", "Cannot create Geocoder with vi_VN locale, using default: " + e.getMessage());
                    geocoder = new Geocoder(this, java.util.Locale.getDefault());
                }
                
                // Tạo biến final để sử dụng trong lambda
                final Geocoder finalGeocoder = geocoder;
                
                // Lấy nhiều kết quả hơn (10) để có nhiều lựa chọn và chọn địa chỉ ở Việt Nam
                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
                java.util.concurrent.Future<List<Address>> future = executor.submit(() -> {
                    return finalGeocoder.getFromLocation(latitude, longitude, 10);
                });
                
                List<Address> addresses = null;
                try {
                    // Timeout sau 10 giây (tăng lên để có thời gian lấy địa chỉ chính xác hơn)
                    addresses = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                } catch (java.util.concurrent.TimeoutException e) {
                    future.cancel(true);
                    android.util.Log.e("ThanhToanActivity", "Geocoding timeout");
                    mainHandler.post(() -> {
                        // Method này không còn được sử dụng
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
                        // Method này không còn được sử dụng
                        showToast("Lỗi khi lấy địa chỉ. Vui lòng thử lại hoặc nhập địa chỉ thủ công.");
                    });
                    executor.shutdown();
                    return;
                }
                
                executor.shutdown();
                
                if (addresses != null && !addresses.isEmpty()) {
                    // Log tất cả addresses để debug
                    Log.d("Location", "=== All addresses from Geocoder ===");
                    for (int i = 0; i < addresses.size(); i++) {
                        Address addr = addresses.get(i);
                        Log.d("Location", "Address #" + i + ":");
                        Log.d("Location", "  - Country: " + addr.getCountryName() + " (Code: " + addr.getCountryCode() + ")");
                        Log.d("Location", "  - AdminArea: " + addr.getAdminArea());
                        Log.d("Location", "  - SubAdminArea: " + addr.getSubAdminArea());
                        Log.d("Location", "  - Locality: " + addr.getLocality());
                        Log.d("Location", "  - SubLocality: " + addr.getSubLocality());
                        Log.d("Location", "  - Thoroughfare: " + addr.getThoroughfare());
                        Log.d("Location", "  - SubThoroughfare: " + addr.getSubThoroughfare());
                        if (addr.getMaxAddressLineIndex() >= 0) {
                            for (int j = 0; j <= addr.getMaxAddressLineIndex(); j++) {
                                Log.d("Location", "  - AddressLine[" + j + "]: " + addr.getAddressLine(j));
                            }
                        }
                    }
                    
                    // Kiểm tra tọa độ có hợp lý không (Việt Nam: lat ~8-23, lng ~102-110)
                    boolean isInVietnamBounds = (latitude >= 8.0 && latitude <= 23.5 && 
                                                  longitude >= 102.0 && longitude <= 110.0);
                    
                    if (!isInVietnamBounds) {
                        Log.e("Location", "❌ Coordinates are NOT in Vietnam bounds!");
                        Log.e("Location", "   Received: " + latitude + ", " + longitude);
                        Log.e("Location", "   Expected: lat 8.0-23.5, lng 102.0-110.0");
                        Log.e("Location", "   This location appears to be: " + 
                              (latitude > 30 ? "USA/Europe" : latitude < 0 ? "Southern Hemisphere" : "Unknown"));
                        
                        mainHandler.post(() -> {
                        // Method này không còn được sử dụng
                            
                            // Hiển thị dialog cảnh báo chi tiết
                            new AlertDialog.Builder(ThanhToanActivity.this)
                                    .setTitle("Vị trí không hợp lệ")
                                    .setMessage("Vị trí hiện tại không phải ở Việt Nam.\n\n" +
                                               "Tọa độ nhận được: " + String.format("%.6f, %.6f", latitude, longitude) + "\n\n" +
                                               "Nguyên nhân có thể:\n" +
                                               "• GPS chưa bật hoặc Location Accuracy chưa bật\n" +
                                               "• Đang dùng emulator (tọa độ mặc định)\n" +
                                               "• Vị trí thật không ở Việt Nam\n\n" +
                                               "Vui lòng:\n" +
                                               "1. Bật GPS và Location Accuracy\n" +
                                               "2. Đảm bảo bạn đang ở Việt Nam\n" +
                                               "3. Nhập địa chỉ thủ công")
                                    .setPositiveButton("Nhập địa chỉ", (dialog, which) -> {
                                        edtDiaChi.setFocusable(true);
                                        edtDiaChi.setFocusableInTouchMode(true);
                                        edtDiaChi.requestFocus();
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        });
                        return;
                    }
                    
                    // Chọn địa chỉ tốt nhất (ưu tiên địa chỉ ở Việt Nam)
                    Address address = selectBestAddress(addresses);
                    
                    if (address == null) {
                        mainHandler.post(() -> {
                        // Method này không còn được sử dụng
                            showToast("Không tìm thấy địa chỉ. Vui lòng nhập địa chỉ thủ công.");
                        });
                        return;
                    }
                    
                    // Kiểm tra lại xem địa chỉ có phải ở Việt Nam không
                    String countryCode = address.getCountryCode();
                    String countryName = address.getCountryName();
                    boolean isVietnam = (countryCode != null && countryCode.equalsIgnoreCase("VN")) ||
                                       (countryName != null && (
                                           countryName.equalsIgnoreCase("Vietnam") ||
                                           countryName.equalsIgnoreCase("Việt Nam") ||
                                           countryName.equalsIgnoreCase("Viet Nam")
                                       ));
                    
                    // Log địa chỉ đã chọn
                    Log.d("Location", "=== Selected address ===");
                    Log.d("Location", "  - Country: " + countryName + " (Code: " + countryCode + ")");
                    Log.d("Location", "  - Is Vietnam: " + isVietnam);
                    Log.d("Location", "  - Feature Name: " + address.getFeatureName());
                    Log.d("Location", "  - Thoroughfare: " + address.getThoroughfare());
                    Log.d("Location", "  - SubThoroughfare: " + address.getSubThoroughfare());
                    Log.d("Location", "  - Locality: " + address.getLocality());
                    Log.d("Location", "  - SubLocality: " + address.getSubLocality());
                    Log.d("Location", "  - AdminArea: " + address.getAdminArea());
                    Log.d("Location", "  - SubAdminArea: " + address.getSubAdminArea());
                    
                    // Nếu địa chỉ KHÔNG phải ở Việt Nam, không sử dụng và yêu cầu nhập thủ công
                    if (!isVietnam) {
                        Log.e("Location", "ERROR: Selected address is NOT in Vietnam! Country: " + countryName);
                        mainHandler.post(() -> {
                        // Method này không còn được sử dụng
                            
                            // Hiển thị dialog cảnh báo
                            new AlertDialog.Builder(ThanhToanActivity.this)
                                    .setTitle("Không thể lấy địa chỉ")
                                    .setMessage("Không tìm thấy địa chỉ ở Việt Nam từ vị trí hiện tại.\n\n" +
                                               "Vui lòng:\n" +
                                               "1. Kiểm tra GPS và Location Accuracy đã bật\n" +
                                               "2. Đảm bảo bạn đang ở Việt Nam\n" +
                                               "3. Nhập địa chỉ thủ công")
                                    .setPositiveButton("Nhập địa chỉ", (dialog, which) -> {
                                        edtDiaChi.setFocusable(true);
                                        edtDiaChi.setFocusableInTouchMode(true);
                                        edtDiaChi.requestFocus();
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        });
                        return;
                    }
                    
                    // Tạo địa chỉ đầy đủ với format tốt hơn
                    String fullAddress = formatAddress(address);
                    
                    // Cập nhật UI trên main thread
                    mainHandler.post(() -> {
                        // Xóa selectedAddressId vì đây là địa chỉ mới từ định vị, không phải địa chỉ đã lưu
                        selectedAddress = null;
                        selectedAddressId = null;

                        if (!fullAddress.isEmpty()) {
                            edtDiaChi.setText(fullAddress);
                            showToast("Đã lấy địa chỉ từ vị trí hiện tại");
                        } else {
                            // Fallback: Hiển thị tọa độ
                            edtDiaChi.setText(String.format("Lat: %.6f, Lng: %.6f", latitude, longitude));
                            showToast("Đã lấy tọa độ. Vui lòng nhập địa chỉ thủ công.");
                        }
                        // Method này không còn được sử dụng
                    });
                } else {
                    mainHandler.post(() -> {
                        // Fallback: Hiển thị tọa độ
                        edtDiaChi.setText(String.format("Lat: %.6f, Lng: %.6f", latitude, longitude));
                        showToast("Không tìm thấy địa chỉ. Vui lòng nhập thủ công.");
                        // Method này không còn được sử dụng
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("ThanhToanActivity", "Unexpected error: " + e.getMessage(), e);
                mainHandler.post(() -> {
                        // Method này không còn được sử dụng
                    showToast("Lỗi không xác định. Vui lòng thử lại.");
                });
            }
        }).start();
    }

    /**
     * Chọn địa chỉ tốt nhất từ danh sách addresses
     * Ưu tiên địa chỉ ở Việt Nam, sau đó mới đến địa chỉ có đầy đủ thông tin nhất
     */
    private Address selectBestAddress(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        // Bước 1: Tìm địa chỉ ở Việt Nam trước
        List<Address> vietnamAddresses = new ArrayList<>();
        for (Address address : addresses) {
            String countryCode = address.getCountryCode();
            String countryName = address.getCountryName();
            
            // Kiểm tra nếu là Việt Nam (VN, Vietnam, Việt Nam, etc.)
            if (countryCode != null && countryCode.equalsIgnoreCase("VN")) {
                vietnamAddresses.add(address);
            } else if (countryName != null && (
                countryName.equalsIgnoreCase("Vietnam") ||
                countryName.equalsIgnoreCase("Việt Nam") ||
                countryName.equalsIgnoreCase("Viet Nam")
            )) {
                vietnamAddresses.add(address);
            }
        }

        // Bước 2: Nếu có địa chỉ ở Việt Nam, chọn địa chỉ tốt nhất trong số đó
        if (!vietnamAddresses.isEmpty()) {
            Log.d("Location", "Found " + vietnamAddresses.size() + " addresses in Vietnam");
            Address bestVietnamAddress = vietnamAddresses.get(0);
            int maxScore = calculateAddressScore(bestVietnamAddress);

            for (Address address : vietnamAddresses) {
                int score = calculateAddressScore(address);
                if (score > maxScore) {
                    maxScore = score;
                    bestVietnamAddress = address;
                }
            }
            return bestVietnamAddress;
        }

        // Bước 3: Nếu không có địa chỉ ở Việt Nam, KHÔNG trả về địa chỉ sai
        // Thay vào đó, log cảnh báo và trả về null để caller xử lý
        Log.e("Location", "ERROR: No Vietnam address found in " + addresses.size() + " addresses!");
        Log.e("Location", "All addresses are from other countries:");
        for (Address addr : addresses) {
            Log.e("Location", "  - " + addr.getCountryName() + " (" + addr.getCountryCode() + ")");
        }
        
        // KHÔNG trả về địa chỉ không phải ở Việt Nam
        // Caller sẽ xử lý và yêu cầu user nhập thủ công
        return null;
    }

    /**
     * Tính điểm cho địa chỉ (địa chỉ có nhiều thông tin hơn sẽ có điểm cao hơn)
     */
    private int calculateAddressScore(Address address) {
        int score = 0;
        // Ưu tiên các field quan trọng hơn
        if (address.getThoroughfare() != null && !address.getThoroughfare().trim().isEmpty()) score += 15;
        if (address.getSubThoroughfare() != null && !address.getSubThoroughfare().trim().isEmpty()) score += 10;
        if (address.getSubLocality() != null && !address.getSubLocality().trim().isEmpty()) score += 12;
        if (address.getLocality() != null && !address.getLocality().trim().isEmpty()) score += 12;
        if (address.getSubAdminArea() != null && !address.getSubAdminArea().trim().isEmpty()) score += 15;
        if (address.getAdminArea() != null && !address.getAdminArea().trim().isEmpty()) score += 15;
        if (address.getCountryName() != null && !address.getCountryName().trim().isEmpty()) score += 5;
        
        // Bonus điểm nếu có AddressLine (địa chỉ đã được format sẵn)
        if (address.getMaxAddressLineIndex() >= 0) {
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                if (address.getAddressLine(i) != null && !address.getAddressLine(i).trim().isEmpty()) {
                    score += 5;
                }
            }
        }
        
        return score;
    }

    /**
     * Format địa chỉ từ Address object thành string đầy đủ và chính xác
     */
    private String formatAddress(Address address) {
        if (address == null) {
            return "";
        }

        StringBuilder addressBuilder = new StringBuilder();

        // Thử dùng getAddressLine() trước (địa chỉ đã được format sẵn)
        try {
            int maxAddressLineIndex = address.getMaxAddressLineIndex();
            if (maxAddressLineIndex >= 0) {
                for (int i = 0; i <= maxAddressLineIndex; i++) {
                    String line = address.getAddressLine(i);
                    if (line != null && !line.trim().isEmpty()) {
                        if (addressBuilder.length() > 0) {
                            addressBuilder.append(", ");
                        }
                        addressBuilder.append(line);
                    }
                }
                // Nếu getAddressLine() có kết quả tốt, dùng nó
                if (addressBuilder.length() > 20) { // Địa chỉ đủ dài, có vẻ chính xác
                    return addressBuilder.toString();
                }
            }
        } catch (Exception e) {
            Log.d("Location", "Cannot use getAddressLine: " + e.getMessage());
        }

        // Fallback: Tự format từ các field riêng lẻ
        addressBuilder.setLength(0);

        // Số nhà (SubThoroughfare) - ví dụ: "123"
        if (address.getSubThoroughfare() != null && !address.getSubThoroughfare().trim().isEmpty()) {
            addressBuilder.append(address.getSubThoroughfare());
        }

        // Đường (Thoroughfare) - ví dụ: "Đường ABC"
        if (address.getThoroughfare() != null && !address.getThoroughfare().trim().isEmpty()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(" ");
            }
            addressBuilder.append(address.getThoroughfare());
        }

        // Phường/Xã (SubLocality hoặc Locality)
        String ward = null;
        if (address.getSubLocality() != null && !address.getSubLocality().trim().isEmpty()) {
            ward = address.getSubLocality();
        } else if (address.getLocality() != null && !address.getLocality().trim().isEmpty()) {
            ward = address.getLocality();
        }

        if (ward != null) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(ward);
        }

        // Quận/Huyện (SubAdminArea)
        if (address.getSubAdminArea() != null && !address.getSubAdminArea().trim().isEmpty()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(address.getSubAdminArea());
        }

        // Tỉnh/Thành phố (AdminArea)
        if (address.getAdminArea() != null && !address.getAdminArea().trim().isEmpty()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(address.getAdminArea());
        }

        return addressBuilder.toString();
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

        // Nếu có addressId: sử dụng địa chỉ đã lưu
        // Nếu không có addressId: backend sẽ tự động tạo từ User profile
        // Cho phép thanh toán với địa chỉ từ user profile hoặc địa chỉ đã nhập
        createMoMoPayment(address, total);
    }

    /**
     * Tạo payment request qua backend API và mở MoMo app/browser
     * Flow: Gọi API /api/payment/momo/create -> Nhận payUrl/deeplink -> Mở MoMo app hoặc browser
     * Nếu có addressId: sử dụng địa chỉ đã lưu
     * Nếu không có addressId: backend sẽ tự động tạo từ User profile
     */
    private void createMoMoPayment(String address, double total) {
        // Tạo request object
        MoMoCreateRequest request = new MoMoCreateRequest();
        
        // Nếu có addressId, sử dụng địa chỉ đã lưu
        // Nếu không có addressId (địa chỉ từ user profile), backend sẽ tự động tạo từ User profile
        if (selectedAddressId != null && !selectedAddressId.trim().isEmpty()) {
            request.setAddressId(selectedAddressId);
            Log.d("MoMo", "Using saved address ID: " + selectedAddressId);
        } else {
            // Không set addressId - backend sẽ tự động tạo từ User profile
            Log.d("MoMo", "No addressId - backend will auto-create from User profile");
        }

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
        
        // Log chi tiết request để debug
        Log.d("MoMo", "=== MOMO CREATE REQUEST ===");
        Log.d("MoMo", "Address ID: " + (request.getAddressId() != null ? request.getAddressId() : "null"));
        Log.d("MoMo", "Cart items count: " + cartItems.size());
        Log.d("MoMo", "Request items count: " + requestItems.size());
        Log.d("MoMo", "Voucher code: " + (voucherCode != null ? voucherCode : "null"));
        Log.d("MoMo", "Notes: " + (request.getNotes() != null ? request.getNotes() : "null"));
        
        // Convert request to JSON để log
        com.google.gson.Gson gson = new com.google.gson.Gson();
        String requestJson = gson.toJson(request);
        Log.d("MoMo", "Request JSON: " + requestJson);
        
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
                        // Response thành công nhưng không có payment URL
                        String errorMsg = "Không nhận được URL thanh toán từ MoMo";
                        if (momoResponse.getError() != null && !momoResponse.getError().isEmpty()) {
                            errorMsg = momoResponse.getError();
                        } else if (momoResponse.getMessage() != null && !momoResponse.getMessage().isEmpty()) {
                            errorMsg = momoResponse.getMessage();
                        }
                        
                        Log.e("MoMo", "=== MOMO CREATE ERROR ===");
                        Log.e("MoMo", "Success: " + momoResponse.isSuccess());
                        Log.e("MoMo", "Error: " + momoResponse.getError());
                        Log.e("MoMo", "Message: " + momoResponse.getMessage());
                        Log.e("MoMo", "PayUrl: " + momoResponse.getPayUrl());
                        Log.e("MoMo", "Deeplink: " + momoResponse.getDeeplink());
                        Log.e("MoMo", "Response body: " + new com.google.gson.Gson().toJson(momoResponse));
                        
                        // Hiển thị dialog với error message chi tiết
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ThanhToanActivity.this);
                        builder.setTitle("Lỗi thanh toán MoMo")
                               .setMessage(errorMsg + "\n\nVui lòng kiểm tra:\n" +
                                          "1. Backend đã cấu hình MoMo trong .env chưa?\n" +
                                          "2. MOMO_PARTNER_CODE, MOMO_ACCESS_KEY, MOMO_SECRET_KEY có đúng không?\n" +
                                          "3. MOMO_ENVIRONMENT có đúng (sandbox/production) không?\n" +
                                          "4. Backend có gọi được MoMo API không? (kiểm tra logs)")
                               .setPositiveButton("Đã hiểu", null)
                               .show();
                        
                        btnThanhToan.setEnabled(true);
                        btnThanhToan.setText("Thanh toán");
                    }
                } else {
                    // Parse error response chi tiết
                    String errorMsg = "Lỗi tạo đơn hàng MoMo";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("MoMo", "Error response code: " + response.code());
                            Log.e("MoMo", "Error response body: " + errorBody);
                            
                            // Thử parse JSON error
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                MoMoCreateResponse errorResponse = gson.fromJson(errorBody, MoMoCreateResponse.class);
                                if (errorResponse != null) {
                                    if (errorResponse.getError() != null && !errorResponse.getError().isEmpty()) {
                                        errorMsg = errorResponse.getError();
                                    } else if (errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                                        errorMsg = errorResponse.getMessage();
                                    }
                                } else {
                                    // Thử parse ApiResponse format
                                    ApiResponse<?> apiErrorResponse = gson.fromJson(errorBody, ApiResponse.class);
                                    if (apiErrorResponse != null && apiErrorResponse.getMessage() != null) {
                                        errorMsg = apiErrorResponse.getMessage();
                                    }
                                }
                            } catch (Exception jsonEx) {
                                // Không phải JSON, dùng error body nếu ngắn
                                if (errorBody.length() < 200) {
                                    errorMsg = errorBody;
                                } else {
                                    errorMsg = "Lỗi từ server: " + errorBody.substring(0, 100) + "...";
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("MoMo", "Error parsing error body", e);
                    }
                    
                    // Hiển thị error message chi tiết
                    Log.e("MoMo", "Final error message: " + errorMsg);
                    
                    // Hiển thị dialog với error message chi tiết
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ThanhToanActivity.this);
                    builder.setTitle("Lỗi thanh toán MoMo")
                           .setMessage(errorMsg + "\n\nVui lòng kiểm tra:\n" +
                                      "1. Backend đã cấu hình MoMo trong .env chưa?\n" +
                                      "2. MOMO_PARTNER_CODE, MOMO_ACCESS_KEY, MOMO_SECRET_KEY có đúng không?\n" +
                                      "3. MOMO_ENVIRONMENT có đúng (sandbox/production) không?\n" +
                                      "4. Backend logs có lỗi gì không?")
                           .setPositiveButton("Đã hiểu", null)
                           .show();
                    
                    btnThanhToan.setEnabled(true);
                    btnThanhToan.setText("Thanh toán");
                }
            }

            @Override
            public void onFailure(Call<MoMoCreateResponse> call, Throwable t) {
                Log.e("MoMo", "Network error: " + t.getMessage(), t);
                
                String errorMsg = "Lỗi kết nối server";
                if (t instanceof java.net.UnknownHostException) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg = "Kết nối timeout. Vui lòng thử lại.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra backend có đang chạy không.";
                } else if (t.getMessage() != null) {
                    errorMsg = "Lỗi: " + t.getMessage();
                }
                ToastManager.showToast(ThanhToanActivity.this, errorMsg, Toast.LENGTH_LONG);
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");
            }
        });
    }
    
    /**
     * Xử lý thanh toán VNPay
     */
    private void processVNPayPayment(String address) {
        btnThanhToan.setEnabled(false);
        btnThanhToan.setText("Đang xử lý...");

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            showToast("Vui lòng đăng nhập");
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            return;
        }

        // Nếu có addressId: sử dụng địa chỉ đã lưu
        // Nếu không có addressId: backend sẽ tự động tạo từ User profile
        // Cho phép thanh toán với địa chỉ từ user profile hoặc địa chỉ đã nhập
        createVNPayOrder(address);
    }
    
    /**
     * Tạo đơn hàng VNPay sau khi đã có đầy đủ thông tin
     * Nếu có addressId: sử dụng địa chỉ đã lưu
     * Nếu không có addressId: backend sẽ tự động tạo từ User profile
     */
    private void createVNPayOrder(String address) {
        // Tạo request
        VNPayCreateRequest request = new VNPayCreateRequest();

        // Nếu có addressId, sử dụng địa chỉ đã lưu
        // Nếu không có addressId (địa chỉ từ user profile), backend sẽ tự động tạo từ User profile
        if (selectedAddressId != null && !selectedAddressId.trim().isEmpty()) {
            request.setAddressId(selectedAddressId);
            Log.d("VNPay", "Using saved address ID: " + selectedAddressId);
        } else {
            // Không set addressId - backend sẽ tự động tạo từ User profile
            Log.d("VNPay", "No addressId - backend will auto-create from User profile");
        }

        request.setNotes("Địa chỉ giao hàng: " + address);
        
        // Thêm voucher code nếu có
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            request.setVoucherCode(voucherCode);
        }
        
        // Thêm cart items vào request
        List<VNPayCreateRequest.CartItem> requestItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            VNPayCreateRequest.CartItem item = new VNPayCreateRequest.CartItem();
            item.setProduct(cartItem.getProduct().get_id());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getPrice());
            item.setColor(""); // Có thể lấy từ cartItem nếu có
            item.setSize(""); // Có thể lấy từ cartItem nếu có
            requestItems.add(item);
        }
        request.setItems(requestItems);
        
        Log.d("VNPay", "Cart items count: " + cartItems.size());
        Log.d("VNPay", "Request items count: " + requestItems.size());
        
        // Gọi API tạo đơn hàng VNPay
        Call<VNPayCreateResponse> call = paymentService.createVNPayOrder(request);
        call.enqueue(new Callback<VNPayCreateResponse>() {
            @Override
            public void onResponse(Call<VNPayCreateResponse> call, Response<VNPayCreateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VNPayCreateResponse vnpayResponse = response.body();
                    
                    if (vnpayResponse.isSuccess() && vnpayResponse.getPaymentUrl() != null) {
                        String paymentUrl = vnpayResponse.getPaymentUrl();
                        String orderId = vnpayResponse.getOrderId();
                        String orderNumber = vnpayResponse.getOrderNumber();
                        String vnpTxnRef = vnpayResponse.getVnp_TxnRef();
                        
                        Log.d("VNPay", "Received payment URL: " + paymentUrl);
                        Log.d("VNPay", "Order ID: " + orderId);
                        Log.d("VNPay", "Order Number: " + orderNumber);
                        Log.d("VNPay", "VNPay TxnRef: " + vnpTxnRef);
                        
                        // Log return URL từ payment URL để debug lỗi chữ ký
                        String returnUrlFromRequest = null;
                        try {
                            android.net.Uri paymentUri = android.net.Uri.parse(paymentUrl);
                            
                            // Log tất cả các query parameters để debug
                            Log.e("VNPay", "=== DEBUG: Parsing payment URL ===");
                            Log.e("VNPay", "Payment URL length: " + paymentUrl.length());
                            
                            // Log toàn bộ payment URL (có thể bị cắt nếu quá dài)
                            if (paymentUrl.length() > 500) {
                                Log.e("VNPay", "Payment URL (first 500 chars): " + paymentUrl.substring(0, 500));
                                Log.e("VNPay", "Payment URL (last 200 chars): " + paymentUrl.substring(paymentUrl.length() - 200));
                            } else {
                                Log.e("VNPay", "Full Payment URL: " + paymentUrl);
                            }
                            
                            // Lấy return URL
                            returnUrlFromRequest = paymentUri.getQueryParameter("vnp_ReturnUrl");
                            if (returnUrlFromRequest != null) {
                                Log.e("VNPay", "📋 VNPay Return URL từ payment request: " + returnUrlFromRequest);
                                Log.e("VNPay", "⚠️ Đảm bảo VNPAY_RETURN_URL trong backend .env khớp với URL trên!");
                                
                                // Kiểm tra nếu return URL là localhost
                                if (returnUrlFromRequest.contains("localhost") || returnUrlFromRequest.contains("127.0.0.1")) {
                                    Log.e("VNPay", "❌ ERROR: Return URL chứa localhost! VNPay sẽ không thể redirect về!");
                                    Log.e("VNPay", "❌ Cần cập nhật VNPAY_RETURN_URL trong backend .env với IP thật!");
                                }
                            } else {
                                Log.e("VNPay", "⚠️ WARNING: Không tìm thấy vnp_ReturnUrl trong payment URL");
                                Log.e("VNPay", "⚠️ Backend có thể đã set return URL nhưng không pass qua payment URL");
                                Log.e("VNPay", "⚠️ VNPay sẽ dùng return URL từ backend config, cần đảm bảo đúng!");
                                
                                // Thử tìm các parameter khác liên quan
                                String ipnUrl = paymentUri.getQueryParameter("vnp_IpAddr");
                                Log.e("VNPay", "vnp_IpAddr: " + ipnUrl);
                                
                                // Log tất cả query parameter names để debug
                                Set<String> paramNames = paymentUri.getQueryParameterNames();
                                Log.e("VNPay", "All query parameters: " + paramNames.toString());
                            }
                            
                            // Log một số parameters quan trọng khác
                            String vnpTxnRefFromUrl = paymentUri.getQueryParameter("vnp_TxnRef");
                            String vnpAmount = paymentUri.getQueryParameter("vnp_Amount");
                            String vnpCommand = paymentUri.getQueryParameter("vnp_Command");
                            String vnpCreateDate = paymentUri.getQueryParameter("vnp_CreateDate");
                            Log.e("VNPay", "vnp_TxnRef từ URL: " + vnpTxnRefFromUrl);
                            Log.e("VNPay", "vnp_Amount: " + vnpAmount);
                            Log.e("VNPay", "vnp_Command: " + vnpCommand);
                            Log.e("VNPay", "vnp_CreateDate: " + vnpCreateDate);
                            
                            // So sánh vnpTxnRef từ response và URL
                            if (vnpTxnRefFromUrl != null && !vnpTxnRefFromUrl.equals(vnpTxnRef)) {
                                Log.w("VNPay", "⚠️ vnp_TxnRef từ URL khác với từ response!");
                            }
                            
                        } catch (Exception e) {
                            Log.e("VNPay", "Error parsing payment URL for return URL: " + e.getMessage(), e);
                            e.printStackTrace();
                        }
                        
                        // Lưu return URL để pass vào WebView
                        if (returnUrlFromRequest != null) {
                            Log.e("VNPay", "Return URL sẽ được pass vào WebView: " + returnUrlFromRequest);
                        } else {
                            Log.e("VNPay", "⚠️ Return URL là NULL - sẽ không pass vào WebView");
                        }
                        
                        // Lưu orderId để check status sau khi thanh toán
                        currentVNPayOrderId = orderId;
                        currentVNPayOrderNumber = orderNumber;
                        
                        // Mở WebView để thanh toán (pass return URL nếu có)
                        openVNPayWebView(paymentUrl, orderId, returnUrlFromRequest);
                    } else {
                        String errorMsg = vnpayResponse.getError() != null ? vnpayResponse.getError() : 
                                         (vnpayResponse.getMessage() != null ? vnpayResponse.getMessage() : "Tạo đơn hàng thất bại");
                        Log.e("VNPay", "Create order failed: " + errorMsg);
                        ToastManager.showToast(ThanhToanActivity.this, errorMsg, Toast.LENGTH_LONG);
                        btnThanhToan.setEnabled(true);
                        btnThanhToan.setText("Thanh toán");
                    }
                } else {
                    // Parse error response
                    String errorMsg = "Lỗi kết nối server";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("VNPay", "Error response code: " + response.code());
                            Log.e("VNPay", "Error response body: " + errorBody);
                            
                            // Thử parse JSON error
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                VNPayCreateResponse errorResponse = gson.fromJson(errorBody, VNPayCreateResponse.class);
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
                        Log.e("VNPay", "Error parsing error body", e);
                    }
                    ToastManager.showToast(ThanhToanActivity.this, errorMsg, Toast.LENGTH_LONG);
                    btnThanhToan.setEnabled(true);
                    btnThanhToan.setText("Thanh toán");
                }
            }

            @Override
            public void onFailure(Call<VNPayCreateResponse> call, Throwable t) {
                Log.e("VNPay", "Network error: " + t.getMessage(), t);
                
                String errorMsg = "Lỗi kết nối server";
                if (t instanceof java.net.UnknownHostException) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg = "Kết nối timeout. Vui lòng thử lại.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra backend có đang chạy không.";
                } else if (t.getMessage() != null) {
                    errorMsg = "Lỗi: " + t.getMessage();
                }
                ToastManager.showToast(ThanhToanActivity.this, errorMsg, Toast.LENGTH_LONG);
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");
            }
        });
    }
    
    /**
     * Mở WebView để thanh toán VNPay
     */
    private void openVNPayWebView(String paymentUrl, String orderId, String returnUrl) {
        try {
            android.content.Intent intent = new android.content.Intent(
                ThanhToanActivity.this, VNPayWebViewActivity.class);
            intent.putExtra("payment_url", paymentUrl);
            intent.putExtra("order_id", orderId);
            if (returnUrl != null) {
                intent.putExtra("return_url", returnUrl);
                Log.d("VNPay", "Passing return URL to WebView: " + returnUrl);
            }
            startActivityForResult(intent, 1003); // Request code cho VNPay
        } catch (Exception e) {
            Log.e("VNPay", "Error opening WebView: " + e.getMessage(), e);
            ToastManager.showToast(this, "Không thể mở trang thanh toán VNPay", Toast.LENGTH_LONG);
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
        }
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
        
        // Xử lý kết quả từ VNPay WebView
        if (requestCode == 1003) {
            if (resultCode == RESULT_OK && data != null) {
                String paymentStatus = data.getStringExtra("payment_status");
                String orderId = data.getStringExtra("order_id");
                
                if ("success".equals(paymentStatus)) {
                    // Thanh toán thành công
                    Log.d("VNPay", "Payment succeeded from WebView");
                    
                    // Xóa giỏ hàng
                    cartManager.clearCart();
                    
                    // Hiển thị thông báo
                    ToastManager.showToast(this, 
                        "Thanh toán thành công qua VNPay!", 
                        Toast.LENGTH_LONG);
                    
                    // Chuyển đến màn hình đơn hàng
                    android.content.Intent intent = new android.content.Intent(
                        this, DonHangActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                                   android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else if ("failed".equals(paymentStatus)) {
                    // Thanh toán thất bại
                    String errorMsg = data.getStringExtra("error_message");
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = "Thanh toán thất bại";
                    }
                    ToastManager.showToast(this, errorMsg, Toast.LENGTH_LONG);
                    btnThanhToan.setEnabled(true);
                    btnThanhToan.setText("Thanh toán");
                } else {
                    // User hủy hoặc chưa xác định
                    btnThanhToan.setEnabled(true);
                    btnThanhToan.setText("Thanh toán");
                }
            } else {
                // User quay lại mà không có kết quả
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("Thanh toán");
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
        
            Call<ZaloPayStatusResponse> call = paymentService.getZaloPayStatus(orderId);
            call.enqueue(new Callback<ZaloPayStatusResponse>() {
            @Override
                public void onResponse(Call<ZaloPayStatusResponse> call, Response<ZaloPayStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                        ZaloPayStatusResponse statusResponse = response.body();
                    Log.d("ZaloPay", "Payment status response: " + new com.google.gson.Gson().toJson(statusResponse));
                    
                        String paymentStatus = statusResponse.getPaymentStatus();
                        Log.d("ZaloPay", "Payment status from server: " + paymentStatus);

                        if ("success".equals(paymentStatus)) {
                            Log.d("ZaloPay", "✅ Payment confirmed successful from server");
                            // Xóa orderId đã lưu vì đã xử lý xong
                            currentZaloPayOrderId = null;
                            currentZaloPayOrderNumber = null;
                            handlePaymentSuccess(statusResponse.getOrderNumber());
                        } else if ("failed".equals(paymentStatus)) {
                            Log.d("ZaloPay", "❌ Payment failed from server");
                            // Xóa orderId đã lưu
                            currentZaloPayOrderId = null;
                            currentZaloPayOrderNumber = null;
                            handlePaymentFailed();
                        } else {
                            // pending, processing: tiếp tục chờ
                            Log.d("ZaloPay", "⏳ Payment status: " + paymentStatus + " - waiting...");
                    }
                } else {
                    Log.w("ZaloPay", "Failed to check payment status: " + response.code());
                }
            }

            @Override
                public void onFailure(Call<ZaloPayStatusResponse> call, Throwable t) {
                Log.e("ZaloPay", "Error checking payment status: " + t.getMessage());
                // Không cần hiển thị lỗi cho user vì PayOrderListener đã xử lý
            }
        });
    }

        /**
         * Verify payment success từ server (sau khi PayOrderListener callback)
         */
        private void verifyPaymentSuccess(String orderId, String orderNumber) {
            checkZaloPayPaymentStatus(orderId);
        }

        /**
         * Bắt đầu polling để kiểm tra trạng thái thanh toán
         * Polling mỗi 5 giây, tối đa 60 lần (5 phút)
         */
        private void startPaymentStatusPolling(String orderId, String orderNumber) {
            if (orderId == null || orderId.isEmpty()) {
                return;
            }

            new Thread(() -> {
                int attempts = 0;
                final int maxAttempts = 60;

                while (attempts < maxAttempts) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Log.e("ZaloPay", "Polling interrupted", e);
                        break;
                    }

                    Call<ZaloPayStatusResponse> call = paymentService.getZaloPayStatus(orderId);
                    try {
                        Response<ZaloPayStatusResponse> response = call.execute();
                        if (response.isSuccessful() && response.body() != null) {
                            ZaloPayStatusResponse statusResponse = response.body();
                            String paymentStatus = statusResponse.getPaymentStatus();

                            if ("success".equals(paymentStatus)) {
                                runOnUiThread(() -> handlePaymentSuccess(statusResponse.getOrderNumber()));
                                break;
                            } else if ("failed".equals(paymentStatus)) {
                                runOnUiThread(() -> handlePaymentFailed());
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ZaloPay", "Error during polling: " + e.getMessage());
                    }

                    attempts++;
                }

                if (attempts >= maxAttempts) {
                    runOnUiThread(() -> showPaymentTimeout(orderNumber));
                }
            }).start();
        }

        private void handlePaymentSuccess(String orderNumber) {
            Log.d("ZaloPay", "handlePaymentSuccess called for order: " + orderNumber);
            cartManager.clearCart();

            // Chuyển ngay đến màn hình đơn hàng (không cần dialog)
            android.content.Intent intent = new android.content.Intent(
                ThanhToanActivity.this, DonHangActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP |
                           android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        private void handlePaymentFailed() {
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
            new AlertDialog.Builder(this)
                .setTitle("Thanh toán thất bại")
                .setMessage("Thanh toán không thành công. Vui lòng thử lại.")
                .setPositiveButton("Thử lại", null)
                .setNegativeButton("Hủy", null)
                .show();
        }

        private void showPaymentTimeout(String orderNumber) {
            new AlertDialog.Builder(this)
                .setTitle("Đang xử lý")
                .setMessage("Thanh toán đang được xử lý. Vui lòng kiểm tra lại sau hoặc liên hệ hỗ trợ.\n\nMã đơn: " +
                           (orderNumber != null ? orderNumber : ""))
                .setPositiveButton("OK", null)
                .show();
            btnThanhToan.setEnabled(true);
            btnThanhToan.setText("Thanh toán");
        }
    
    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
            
            // BẮT BUỘC: Gọi onResult để ZaloPay SDK xử lý callback từ ZaloPay app
            // Đây là bước quan trọng để SDK có thể trigger PayOrderListener callbacks
            try {
                ZaloPaySDK.getInstance().onResult(intent);
                Log.d("ZaloPay", "ZaloPaySDK.onResult() called for deep link");
            } catch (Exception e) {
                Log.e("ZaloPay", "Error calling ZaloPaySDK.onResult(): " + e.getMessage(), e);
            }
            
            // Sau đó mới xử lý deep link nếu cần
        handleZaloPayDeepLink();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
            // Khi quay lại app từ ZaloPay, check payment status
        handleZaloPayDeepLink();

            // Nếu có orderId đã lưu và chưa có deep link, check payment status
            if (currentZaloPayOrderId != null && !currentZaloPayOrderId.isEmpty()) {
                Log.d("ZaloPay", "onResume: Checking payment status for saved orderId: " + currentZaloPayOrderId);
                // Delay một chút để đảm bảo app đã resume hoàn toàn
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    checkZaloPayPaymentStatus(currentZaloPayOrderId);
                }, 1000);
            }
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

                            // Cập nhật tổng tiền sau khi áp dụng voucher
                            updateTotalPrice();
                        
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
                Log.e("Voucher", "Network error: " + t.getMessage(), t);
                
                String errorMsg = "Lỗi kết nối server";
                if (t instanceof java.net.UnknownHostException) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg = "Kết nối timeout. Vui lòng thử lại.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra backend có đang chạy không.";
                } else if (t.getMessage() != null) {
                    errorMsg = "Lỗi: " + t.getMessage();
                }
                showToast(errorMsg);
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

            // Cập nhật tổng tiền sau khi xóa voucher
            updateTotalPrice();
    }
    
    /**
     * Format giá tiền
     */
    private String formatPrice(Double price) {
            if (price == null) return "0 ₫";
            java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault());
            return formatter.format(price) + " ₫";
        }

        /**
         * Cập nhật tổng tiền hiển thị trên UI
         */
        private void updateTotalPrice() {
            if (cartItems == null || cartItems.isEmpty()) {
                txtSubtotal.setText("0 ₫");
                txtShippingFee.setText("30.000 ₫");
                txtTotal.setText("30.000 ₫");
                layoutDiscount.setVisibility(View.GONE);
                return;
            }

            // Tính tổng tiền sản phẩm
            double subtotal = 0;
            for (CartItem item : cartItems) {
                subtotal += item.getPrice() * item.getQuantity();
            }

            // Phí ship mặc định: 30.000 ₫
            double shippingFee = 30000;

            // Tính giảm giá từ voucher
            double discount = 0;
            if (selectedVoucher != null) {
                String discountType = selectedVoucher.getDiscountType();
                Double discountValue = selectedVoucher.getDiscount();

                if (discountValue != null) {
                    if ("percentage".equals(discountType)) {
                        // Giảm theo phần trăm
                        discount = subtotal * discountValue / 100;
                        // Không giới hạn tối đa vì VoucherResponse chưa có field maxDiscount
                    } else {
                        // Giảm theo số tiền cố định
                        discount = discountValue;
                    }
                }
            }

            // Tổng tiền phải thanh toán = subtotal + shippingFee - discount
            double total = subtotal + shippingFee - discount;

            // Đảm bảo tổng tiền không âm
            if (total < 0) {
                total = 0;
            }

            // Hiển thị lên UI
            txtSubtotal.setText(formatPrice(subtotal));
            txtShippingFee.setText(formatPrice(shippingFee));
            txtTotal.setText(formatPrice(total));

            // Hiển thị/ẩn phần giảm giá
            if (discount > 0) {
                txtDiscount.setText("- " + formatPrice(discount));
                layoutDiscount.setVisibility(View.VISIBLE);
            } else {
                layoutDiscount.setVisibility(View.GONE);
            }
        }

        /**
         * Setup AutoCompleteTextView cho địa chỉ đã lưu
         */
        private void setupAddressSpinner() {
            if (spinnerDiaChiDaLuu == null) return;
            
            // Tạo adapter cho AutoCompleteTextView với custom layout để hiển thị nhiều dòng
            android.widget.ArrayAdapter<AddressResponse> adapter = new android.widget.ArrayAdapter<AddressResponse>(
                this,
                android.R.layout.simple_list_item_2,
                savedAddresses
            ) {
                @Override
                public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    android.view.View view = super.getView(position, convertView, parent);
                    android.widget.TextView text1 = view.findViewById(android.R.id.text1);
                    android.widget.TextView text2 = view.findViewById(android.R.id.text2);
                    AddressResponse addr = getItem(position);
                    if (addr != null) {
                        String name = addr.getFullName() + " - " + addr.getPhone();
                        if (addr.getIsDefault() != null && addr.getIsDefault()) {
                            name += " (Mặc định)";
                        }
                        text1.setText(name);
                        text2.setText(addr.getFullAddress());
                    }
                    return view;
                }
                
                @Override
                public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    return getView(position, convertView, parent);
                }
            };
            
            spinnerDiaChiDaLuu.setAdapter(adapter);
            
            // Set threshold để dropdown hiển thị ngay khi click (không cần gõ)
            spinnerDiaChiDaLuu.setThreshold(1);
            
            // Cho phép click để mở dropdown
            spinnerDiaChiDaLuu.setOnClickListener(v -> {
                // Đảm bảo layout luôn hiển thị khi click vào spinner
                if (layoutDiaChiDaLuu != null) {
                    layoutDiaChiDaLuu.setVisibility(android.view.View.VISIBLE);
                }
                if (layoutNhapDiaChiMoi != null) {
                    layoutNhapDiaChiMoi.setVisibility(android.view.View.GONE);
                }
                // Đảm bảo radio được chọn (không trigger listener để tránh conflict)
                if (radioChonDiaChiDaLuu != null && !radioChonDiaChiDaLuu.isChecked()) {
                    // Tạm thời remove listener để tránh trigger khi setChecked
                    radioGroupAddressType.setOnCheckedChangeListener(null);
                    radioChonDiaChiDaLuu.setChecked(true);
                    // Restore listener sau khi setChecked
                    radioGroupAddressType.post(() -> {
                        setupRadioGroupListener();
                    });
                }
                // Delay một chút để đảm bảo layout đã được update
                spinnerDiaChiDaLuu.post(() -> {
                    if (savedAddresses.size() > 0) {
                        spinnerDiaChiDaLuu.showDropDown();
                    } else {
                        showToast("Chưa có địa chỉ đã lưu. Vui lòng nhập địa chỉ mới.");
                    }
                });
            });
            
            // Xử lý khi chọn địa chỉ từ dropdown
            spinnerDiaChiDaLuu.setOnItemClickListener((parent, view, position, id) -> {
                AddressResponse selectedAddr = (AddressResponse) parent.getItemAtPosition(position);
                if (selectedAddr != null) {
                    // Tự động chọn radio "Chọn địa chỉ đã lưu" và đảm bảo layout hiển thị
                    if (radioChonDiaChiDaLuu != null && !radioChonDiaChiDaLuu.isChecked()) {
                        radioChonDiaChiDaLuu.setChecked(true);
                    }
                    // Đảm bảo layout luôn hiển thị
                    if (layoutDiaChiDaLuu != null) {
                        layoutDiaChiDaLuu.setVisibility(android.view.View.VISIBLE);
                    }
                    if (layoutNhapDiaChiMoi != null) {
                        layoutNhapDiaChiMoi.setVisibility(android.view.View.GONE);
                    }
                    selectAddress(selectedAddr);
                }
            });
            
            // Hiển thị dropdown khi touch (đảm bảo luôn hiện khi click)
            // Không dùng onFocusChangeListener vì có thể gây conflict
            spinnerDiaChiDaLuu.setOnTouchListener((v, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    // Đảm bảo layout luôn hiển thị khi touch vào spinner
                    if (layoutDiaChiDaLuu != null) {
                        layoutDiaChiDaLuu.setVisibility(android.view.View.VISIBLE);
                    }
                    if (layoutNhapDiaChiMoi != null) {
                        layoutNhapDiaChiMoi.setVisibility(android.view.View.GONE);
                    }
                    // Đảm bảo radio được chọn (không trigger listener)
                    if (radioChonDiaChiDaLuu != null && !radioChonDiaChiDaLuu.isChecked()) {
                        radioGroupAddressType.setOnCheckedChangeListener(null);
                        radioChonDiaChiDaLuu.setChecked(true);
                        radioGroupAddressType.post(() -> {
                            setupRadioGroupListener();
                        });
                    }
                    // Delay để đảm bảo layout đã được update
                    spinnerDiaChiDaLuu.post(() -> {
                        if (savedAddresses.size() > 0) {
                            spinnerDiaChiDaLuu.showDropDown();
                        }
                    });
                }
                return false; // Cho phép xử lý tiếp
            });
        }
        
        /**
         * Load danh sách địa chỉ đã lưu từ API và địa chỉ từ thông tin cá nhân
         */
        private void loadAddresses() {
            // Load địa chỉ từ thông tin cá nhân trước (async)
            // Sau khi load xong, sẽ gọi loadAddressesFromAPI()
            loadUserAddress(() -> {
                // Sau khi load user address xong, load địa chỉ từ API
                loadAddressesFromAPI();
            });
        }
        
        /**
         * Load địa chỉ từ API (sau khi đã load user address)
         */
        private void loadAddressesFromAPI() {
            // Load danh sách địa chỉ đã lưu từ API
            Call<List<AddressResponse>> call = addressService.getAddresses();
            call.enqueue(new Callback<List<AddressResponse>>() {
                @Override
                public void onResponse(Call<List<AddressResponse>> call, Response<List<AddressResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Không clear savedAddresses vì đã có địa chỉ từ user profile
                        // Chỉ thêm các địa chỉ mới không trùng
                        List<AddressResponse> apiAddresses = response.body();
                        for (AddressResponse apiAddr : apiAddresses) {
                            // Kiểm tra xem đã có trong danh sách chưa (tránh trùng)
                            boolean exists = false;
                            for (AddressResponse savedAddr : savedAddresses) {
                                if (savedAddr.get_id() != null && savedAddr.get_id().equals(apiAddr.get_id())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                savedAddresses.add(apiAddr);
                            }
                        }

                        // Cập nhật adapter cho spinner
                        setupAddressSpinner();

                        // Tự động chọn địa chỉ mặc định nếu có
                        for (AddressResponse addr : savedAddresses) {
                            if (addr.getIsDefault() != null && addr.getIsDefault()) {
                                selectAddress(addr);
                                break;
                            }
                        }

                        // Nếu không có địa chỉ mặc định, chọn địa chỉ đầu tiên (từ user profile)
                        if (selectedAddress == null && !savedAddresses.isEmpty()) {
                            selectAddress(savedAddresses.get(0));
                        }

                        Log.d("ThanhToan", "Loaded " + savedAddresses.size() + " addresses (including user profile address)");
                    } else {
                        Log.d("ThanhToan", "No addresses found or error: " + response.code());
                        // Vẫn cập nhật adapter với địa chỉ từ user profile
                        setupAddressSpinner();
                        if (selectedAddress == null && !savedAddresses.isEmpty()) {
                            selectAddress(savedAddresses.get(0));
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<AddressResponse>> call, Throwable t) {
                    Log.e("ThanhToan", "Error loading addresses: " + t.getMessage());
                    // Vẫn cập nhật adapter với địa chỉ từ user profile
                    setupAddressSpinner();
                    if (selectedAddress == null && !savedAddresses.isEmpty()) {
                        selectAddress(savedAddresses.get(0));
                    }
                }
            });
        }
        
        /**
         * Load địa chỉ từ thông tin cá nhân của user
         * @param callback Callback được gọi sau khi load xong
         */
        private void loadUserAddress(Runnable callback) {
            // Load user info từ API /api/auth/me (tự động lấy từ JWT token)
            Call<UserInfo> call = authService.getMe();
            call.enqueue(new Callback<UserInfo>() {
                @Override
                public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserInfo user = response.body();
                        String userAddress = user.getAddress();
                        String userFullName = user.getFullName();
                        String userPhone = user.getPhone();
                        
                        // Nếu có địa chỉ từ thông tin cá nhân, thêm vào đầu danh sách
                        if (userAddress != null && !userAddress.trim().isEmpty()) {
                            // Kiểm tra xem đã có địa chỉ từ user profile chưa (tránh trùng)
                            boolean alreadyExists = false;
                            for (AddressResponse addr : savedAddresses) {
                                if (addr.get_id() != null && addr.get_id().equals("user_profile")) {
                                    // Cập nhật địa chỉ hiện có
                                    addr.setFullName(userFullName != null ? userFullName : sessionManager.getHoTen());
                                    addr.setPhone(userPhone != null ? userPhone : "");
                                    addr.setAddress(userAddress);
                                    alreadyExists = true;
                                    break;
                                }
                            }
                            
                            if (!alreadyExists) {
                                AddressResponse userAddressResponse = new AddressResponse();
                                userAddressResponse.set_id("user_profile"); // ID đặc biệt để phân biệt
                                userAddressResponse.setFullName(userFullName != null ? userFullName : sessionManager.getHoTen());
                                userAddressResponse.setPhone(userPhone != null ? userPhone : "");
                                userAddressResponse.setAddress(userAddress);
                                userAddressResponse.setIsDefault(true); // Đánh dấu là địa chỉ mặc định từ profile
                                
                                // Thêm vào đầu danh sách
                                savedAddresses.add(0, userAddressResponse);
                            }
                            
                            // Cập nhật adapter sau khi thêm/cập nhật địa chỉ
                            setupAddressSpinner();
                            
                            // Tự động chọn địa chỉ từ user profile nếu chưa có địa chỉ nào được chọn
                            if (selectedAddress == null) {
                                for (AddressResponse addr : savedAddresses) {
                                    if (addr.get_id() != null && addr.get_id().equals("user_profile")) {
                                        selectAddress(addr);
                                        break;
                                    }
                                }
                            }
                            
                            Log.d("ThanhToan", "Added/updated user profile address: " + userAddress);
                        } else {
                            Log.d("ThanhToan", "User profile has no address");
                        }
                    }
                    
                    // Gọi callback sau khi load xong
                    if (callback != null) {
                        callback.run();
                    }
                }
                
                @Override
                public void onFailure(Call<UserInfo> call, Throwable t) {
                    Log.e("ThanhToan", "Error loading user address: " + t.getMessage());
                    // Vẫn gọi callback để tiếp tục load địa chỉ từ API
                    if (callback != null) {
                        callback.run();
                    }
                }
            });
        }

        /**
         * Validate user profile có đầy đủ thông tin trước khi thanh toán
         * Nếu user nhập địa chỉ mới: chỉ cần Họ tên và SĐT trong profile
         * Nếu user không nhập địa chỉ mới: cần Họ tên, SĐT và Địa chỉ trong profile
         */
        private void validateUserProfileBeforePayment(String address) {
            // Load user info từ API /api/auth/me (tự động lấy từ JWT token)
            Call<UserInfo> call = authService.getMe();
            call.enqueue(new Callback<UserInfo>() {
                @Override
                public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserInfo user = response.body();
                        String fullName = user.getFullName();
                        String phone = user.getPhone();
                        String userAddress = user.getAddress();
                        
                        // Kiểm tra đầy đủ thông tin
                        boolean hasFullName = fullName != null && !fullName.trim().isEmpty();
                        boolean hasPhone = phone != null && !phone.trim().isEmpty();
                        boolean hasAddress = userAddress != null && !userAddress.trim().isEmpty();
                        
                        // Nếu user đã nhập địa chỉ mới, chỉ cần Họ tên và SĐT
                        // Nếu user chưa nhập địa chỉ mới, cần cả địa chỉ trong profile
                        boolean isNewAddress = address != null && !address.trim().isEmpty() && 
                                             (selectedAddressId == null || selectedAddressId.trim().isEmpty());
                        
                        if (isNewAddress) {
                            // User nhập địa chỉ mới - chỉ cần Họ tên và SĐT
                            if (!hasFullName || !hasPhone) {
                                // Thiếu thông tin, hiển thị dialog yêu cầu cập nhật
                                StringBuilder missingInfo = new StringBuilder();
                                if (!hasFullName) missingInfo.append("• Họ tên\n");
                                if (!hasPhone) missingInfo.append("• Số điện thoại\n");
                                
                                new AlertDialog.Builder(ThanhToanActivity.this)
                                    .setTitle("Thiếu thông tin")
                                    .setMessage("Để thanh toán với địa chỉ mới, vui lòng cập nhật thông tin trong profile:\n\n" + missingInfo.toString())
                                    .setPositiveButton("Cập nhật profile", (dialog, which) -> {
                                        // Mở màn hình sửa thông tin
                                        android.content.Intent intent = new android.content.Intent(ThanhToanActivity.this, 
                                            fpoly.haideptrai.duan1.customer.SuaThongTinActivity.class);
                                        startActivity(intent);
                                    })
                                    .setNegativeButton("Chọn địa chỉ đã lưu", (dialog, which) -> {
                                        // Hiển thị dialog chọn địa chỉ đã lưu
                                        if (!savedAddresses.isEmpty()) {
                                            spinnerDiaChiDaLuu.showDropDown();
                                        } else {
                                            showToast("Bạn chưa có địa chỉ đã lưu. Vui lòng cập nhật thông tin profile.");
                                        }
                                    })
                                    .setNeutralButton("Hủy", null)
                                    .show();
                            } else {
                                // Đầy đủ thông tin (Họ tên + SĐT), tiếp tục thanh toán với địa chỉ mới
                                proceedWithPayment(address);
                            }
                        } else {
                            // User không nhập địa chỉ mới - cần cả địa chỉ trong profile
                            if (!hasFullName || !hasPhone || !hasAddress) {
                                // Thiếu thông tin, hiển thị dialog yêu cầu cập nhật
                                StringBuilder missingInfo = new StringBuilder();
                                if (!hasFullName) missingInfo.append("• Họ tên\n");
                                if (!hasPhone) missingInfo.append("• Số điện thoại\n");
                                if (!hasAddress) missingInfo.append("• Địa chỉ\n");
                                
                                new AlertDialog.Builder(ThanhToanActivity.this)
                                    .setTitle("Thiếu thông tin")
                                    .setMessage("Để thanh toán, vui lòng cập nhật đầy đủ thông tin trong profile:\n\n" + missingInfo.toString() + "\nHoặc chọn địa chỉ đã lưu từ danh sách, hoặc nhập địa chỉ mới.")
                                    .setPositiveButton("Cập nhật profile", (dialog, which) -> {
                                        // Mở màn hình sửa thông tin
                                        android.content.Intent intent = new android.content.Intent(ThanhToanActivity.this, 
                                            fpoly.haideptrai.duan1.customer.SuaThongTinActivity.class);
                                        startActivity(intent);
                                    })
                                    .setNegativeButton("Chọn địa chỉ đã lưu", (dialog, which) -> {
                                        // Hiển thị dialog chọn địa chỉ đã lưu
                                        if (!savedAddresses.isEmpty()) {
                                            spinnerDiaChiDaLuu.showDropDown();
                                        } else {
                                            showToast("Bạn chưa có địa chỉ đã lưu. Vui lòng cập nhật thông tin profile hoặc nhập địa chỉ mới.");
                                        }
                                    })
                                    .setNeutralButton("Hủy", null)
                                    .show();
                            } else {
                                // Đầy đủ thông tin, tiếp tục thanh toán
                                proceedWithPayment(address);
                            }
                        }
                    } else {
                        // Không load được user info, yêu cầu chọn địa chỉ đã lưu hoặc nhập địa chỉ mới
                        showToast("Không thể kiểm tra thông tin profile. Vui lòng chọn địa chỉ đã lưu hoặc nhập địa chỉ mới.");
                    }
                }
                
                @Override
                public void onFailure(Call<UserInfo> call, Throwable t) {
                    Log.e("ThanhToan", "Error validating user profile: " + t.getMessage());
                    showToast("Không thể kiểm tra thông tin profile. Vui lòng chọn địa chỉ đã lưu hoặc nhập địa chỉ mới.");
                }
            });
        }
        
        /**
         * Tiếp tục thanh toán sau khi đã validate
         */
        private void proceedWithPayment(String address) {
            // Xử lý thanh toán ZaloPay, MoMo, VNPay riêng
            // Kiểm tra payment method (case-insensitive)
            if ("zalopay".equalsIgnoreCase(selectedPaymentMethod)) {
                processZaloPayPayment(address);
            } else if ("momo".equalsIgnoreCase(selectedPaymentMethod)) {
                processMoMoPayment(address);
            } else if ("vnpay".equalsIgnoreCase(selectedPaymentMethod)) {
                processVNPayPayment(address);
            } else {
                // COD, cash, hoặc các payment methods khác đều dùng processPayment()
                processPayment(address);
            }
        }
        
        /**
         * Hiển thị dialog để chọn địa chỉ đã lưu
         */
        private void showAddressPickerDialog() {
            if (savedAddresses.isEmpty()) {
                // Nếu không có địa chỉ đã lưu, yêu cầu nhập địa chỉ mới
                new AlertDialog.Builder(this)
                    .setTitle("Chọn địa chỉ")
                    .setMessage("Bạn chưa có địa chỉ đã lưu. Vui lòng nhập địa chỉ mới hoặc thêm địa chỉ trong phần quản lý địa chỉ.")
                    .setPositiveButton("Nhập địa chỉ", (dialog, which) -> {
                        // Cho phép nhập địa chỉ thủ công
                        edtDiaChi.setFocusable(true);
                        edtDiaChi.setFocusableInTouchMode(true);
                        edtDiaChi.requestFocus();
                        // Xóa selectedAddressId vì đây là địa chỉ mới nhập thủ công
                        selectedAddress = null;
                        selectedAddressId = null;
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
                return;
            }

            // Tạo danh sách địa chỉ để hiển thị
            String[] addressStrings = new String[savedAddresses.size()];
            for (int i = 0; i < savedAddresses.size(); i++) {
                AddressResponse addr = savedAddresses.get(i);
                String displayText = addr.getFullName() + " - " + addr.getPhone();
                if (addr.getIsDefault() != null && addr.getIsDefault()) {
                    displayText += " (Mặc định)";
                }
                displayText += "\n" + addr.getFullAddress();
                addressStrings[i] = displayText;
            }

            // Chỉ thêm option "Nhập địa chỉ mới" (không còn "Sử dụng vị trí hiện tại")
            String[] allOptions = new String[addressStrings.length + 1];
            System.arraycopy(addressStrings, 0, allOptions, 0, addressStrings.length);
            allOptions[addressStrings.length] = "✏️ Nhập địa chỉ mới";

            new AlertDialog.Builder(this)
                .setTitle("Chọn địa chỉ giao hàng")
                .setItems(allOptions, (dialog, which) -> {
                    if (which < savedAddresses.size()) {
                        // Chọn địa chỉ đã lưu
                        selectAddress(savedAddresses.get(which));
                    } else {
                        // Nhập địa chỉ mới
                        edtDiaChi.setFocusable(true);
                        edtDiaChi.setFocusableInTouchMode(true);
                        edtDiaChi.requestFocus();
                        // Xóa selectedAddressId vì đây là địa chỉ mới nhập thủ công
                        selectedAddress = null;
                        selectedAddressId = null;
                    }
                })
                .show();
        }

        /**
         * Chọn địa chỉ và cập nhật UI
         */
        private void selectAddress(AddressResponse address) {
            selectedAddress = address;
            
            // Nếu là địa chỉ từ user profile (không có _id thật), không set selectedAddressId
            // Vì backend chỉ chấp nhận addressId từ danh sách địa chỉ đã lưu
            if (address.get_id() != null && !address.get_id().equals("user_profile")) {
                selectedAddressId = address.get_id();
            } else {
                // Địa chỉ từ user profile - không có addressId, sẽ cần nhập địa chỉ mới hoặc lưu địa chỉ
                selectedAddressId = null;
                Log.w("ThanhToan", "Selected address from user profile - no addressId available");
            }

            // Cập nhật spinner với địa chỉ đã chọn
            if (spinnerDiaChiDaLuu != null) {
                String displayText = address.getFullName() + " - " + address.getPhone();
                if (address.getIsDefault() != null && address.getIsDefault()) {
                    displayText += " (Mặc định)";
                }
                if (address.get_id() != null && address.get_id().equals("user_profile")) {
                    displayText += " [Từ thông tin cá nhân]";
                }
                displayText += "\n" + address.getFullAddress();
                spinnerDiaChiDaLuu.setText(displayText, false);
                // Đóng dropdown nếu đang mở
                spinnerDiaChiDaLuu.dismissDropDown();
            }

            // Đảm bảo radio "Chọn địa chỉ đã lưu" được chọn
            if (radioChonDiaChiDaLuu != null) {
                radioChonDiaChiDaLuu.setChecked(true);
            }

            // Clear EditText nhập địa chỉ mới
            edtDiaChi.setText("");
            edtDiaChi.setFocusable(true);
            edtDiaChi.setFocusableInTouchMode(true);
            // Clear focus để tránh keyboard tự động mở
            edtDiaChi.clearFocus();

            Log.d("ThanhToan", "Selected address: " + (address.get_id() != null ? address.get_id() : "user_profile") + " - " + address.getFullAddress());
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

