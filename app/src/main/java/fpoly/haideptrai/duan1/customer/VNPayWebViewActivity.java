package fpoly.haideptrai.duan1.customer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.utils.ToastManager;

/**
 * Activity để hiển thị WebView cho thanh toán VNPay
 * Xử lý payment URL và return URL từ VNPay
 */
public class VNPayWebViewActivity extends AppCompatActivity {
    private static final String TAG = "VNPayWebView";
    
    private WebView webView;
    private String paymentUrl;
    private String orderId;
    private String returnUrl; // Lưu return URL để hiển thị trong error message
    private boolean isErrorHandled = false; // Flag để tránh xử lý lỗi trùng lặp
    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_webview);
        
        // Lấy payment URL, order ID và return URL từ intent
        paymentUrl = getIntent().getStringExtra("payment_url");
        orderId = getIntent().getStringExtra("order_id");
        String returnUrlFromIntent = getIntent().getStringExtra("return_url");
        
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Log.e(TAG, "Payment URL is null or empty");
            ToastManager.showToast(this, "Không có URL thanh toán", Toast.LENGTH_SHORT);
            finish();
            return;
        }
        
        Log.d(TAG, "Payment URL: " + paymentUrl);
        Log.d(TAG, "Order ID: " + orderId);
        
        // Log và lưu return URL từ payment request để debug
        try {
            Uri paymentUri = Uri.parse(paymentUrl);
            returnUrl = paymentUri.getQueryParameter("vnp_ReturnUrl");
            
            // Nếu không tìm thấy trong payment URL, dùng từ intent
            if (returnUrl == null && returnUrlFromIntent != null) {
                returnUrl = returnUrlFromIntent;
                Log.d(TAG, "📋 VNPay Return URL từ intent: " + returnUrl);
            }
            
            if (returnUrl != null) {
                Log.e(TAG, "📋 VNPay Return URL: " + returnUrl);
                Log.e(TAG, "⚠️ Đảm bảo VNPAY_RETURN_URL trong backend .env khớp với URL trên!");
                
                // Kiểm tra nếu return URL là localhost
                if (returnUrl.contains("localhost") || returnUrl.contains("127.0.0.1")) {
                    Log.e(TAG, "❌ ERROR: Return URL chứa localhost! VNPay sẽ không thể redirect về!");
                    Log.e(TAG, "❌ Cần cập nhật VNPAY_RETURN_URL trong backend .env với IP thật!");
                }
            } else {
                Log.e(TAG, "⚠️ WARNING: Không tìm thấy vnp_ReturnUrl trong payment URL hoặc intent");
                Log.e(TAG, "⚠️ Backend có thể đã set return URL nhưng không pass qua payment URL");
                Log.e(TAG, "⚠️ VNPay sẽ dùng return URL từ backend config, cần đảm bảo đúng!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing payment URL: " + e.getMessage(), e);
        }
        
        // Kiểm tra Return URL trong payment URL để debug lỗi chữ ký
        if (paymentUrl != null && paymentUrl.contains("vnp_ReturnUrl")) {
            try {
                Uri paymentUri = Uri.parse(paymentUrl);
                String returnUrl = paymentUri.getQueryParameter("vnp_ReturnUrl");
                if (returnUrl != null) {
                    Log.d(TAG, "VNPay Return URL: " + returnUrl);
                    // Cảnh báo nếu return URL là localhost
                    if (returnUrl.contains("localhost") || returnUrl.contains("127.0.0.1")) {
                        Log.e(TAG, "⚠️ WARNING: Return URL is localhost! This will cause signature error!");
                        Log.e(TAG, "⚠️ Backend must use real IP address in VNPAY_RETURN_URL");
                        Log.e(TAG, "⚠️ Current machine IP: 192.168.25.97");
                        Log.e(TAG, "⚠️ Update backend .env: VNPAY_RETURN_URL=http://192.168.25.97:3000/api/payment/vnpay/return");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing payment URL: " + e.getMessage());
            }
        }
        
        // Setup WebView với đầy đủ settings
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        
        // JavaScript settings
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        
        // DOM Storage (cần cho VNPay)
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        
        // Viewport settings
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        
        // Cache và network settings
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // Note: setAppCacheEnabled() đã bị deprecated và xóa trong Android API level cao hơn
        
        // Mixed content (cho phép HTTPS load HTTP resources nếu cần)
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        
        // User agent (đảm bảo VNPay nhận diện đúng)
        webSettings.setUserAgentString(webSettings.getUserAgentString() + " VNPayApp");
        
        // Zoom settings
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        // Set WebChromeClient để xử lý console messages
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // Chỉ log các lỗi nghiêm trọng, bỏ qua các lỗi JavaScript nhỏ từ VNPay
                String message = consoleMessage.message();
                ConsoleMessage.MessageLevel level = consoleMessage.messageLevel();
                
                // Bỏ qua các lỗi JavaScript từ VNPay error page (không ảnh hưởng đến thanh toán)
                if (message != null) {
                    // Bỏ qua lỗi "timer is not defined" từ VNPay
                    if (message.contains("timer is not defined")) {
                        Log.d(TAG, "Ignoring VNPay JavaScript warning: " + message);
                        return true; // Đã xử lý, không hiển thị
                    }
                    // Bỏ qua lỗi "Unexpected end of input" từ VNPay error page
                    if (message.contains("Unexpected end of input") || 
                        message.contains("SyntaxError")) {
                        Log.d(TAG, "Ignoring VNPay error page JavaScript error: " + message);
                        return true; // Đã xử lý, không hiển thị
                    }
                }
                
                // Log các lỗi nghiêm trọng khác
                if (level == ConsoleMessage.MessageLevel.ERROR) {
                    Log.e(TAG, "JavaScript Error: " + message + " at " + 
                          consoleMessage.sourceId() + ":" + consoleMessage.lineNumber());
                } else {
                    Log.d(TAG, "JavaScript Console: " + message);
                }
                
                return true; // Đã xử lý
            }
        });
        
        // Set WebViewClient để xử lý return URL và lỗi
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d(TAG, "Loading URL: " + url);
                
                // Xử lý intent:// scheme (khi VNPay QR cố mở ứng dụng ngân hàng)
                if (url.startsWith("intent://")) {
                    try {
                        Log.d(TAG, "Detected intent URL, attempting to parse: " + url);
                        
                        // Sử dụng Intent.parseUri() để parse intent URL tự động
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        
                        // Kiểm tra xem có ứng dụng nào có thể xử lý intent này không
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            String packageName = intent.getPackage();
                            Log.d(TAG, "Opening banking app: " + (packageName != null ? packageName : "unknown"));
                            startActivity(intent);
                            return true; // Đã xử lý, không cho WebView load
                        } else {
                            // Không có ứng dụng có thể xử lý intent
                            String packageName = intent.getPackage();
                            Log.w(TAG, "Banking app not installed: " + (packageName != null ? packageName : "unknown"));
                            
                            // Hiển thị thông báo cho user
                            runOnUiThread(() -> {
                                ToastManager.showToast(VNPayWebViewActivity.this, 
                                    "Ứng dụng ngân hàng chưa được cài đặt. Vui lòng quét QR code bằng ứng dụng ngân hàng của bạn hoặc chọn phương thức thanh toán khác.", 
                                    Toast.LENGTH_LONG);
                            });
                            
                            // Không cần đóng WebView, user vẫn có thể chọn phương thức thanh toán khác
                            return true; // Đã xử lý, bỏ qua intent URL
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling intent URL: " + e.getMessage(), e);
                        // Nếu không thể parse intent, bỏ qua để tránh lỗi ERR_UNKNOWN_URL_SCHEME
                        // Điều này không ảnh hưởng đến thanh toán, user vẫn có thể chọn phương thức khác
                        return true; // Bỏ qua intent URL để tránh lỗi ERR_UNKNOWN_URL_SCHEME
                    }
                }
                
                // Kiểm tra nếu URL là return URL từ VNPay
                if (url.contains("/api/payment/vnpay/return") || 
                    url.contains("vnp_ResponseCode") ||
                    url.contains("vnp_TransactionStatus")) {
                    // Parse kết quả thanh toán từ URL
                    handlePaymentResult(url);
                    return true; // Ngăn WebView load URL này
                }
                
                // Kiểm tra nếu URL là VNPay Error page
                if ((url.contains("/Payment/Error.html") || url.contains("code=")) && !isErrorHandled) {
                    Log.e(TAG, "VNPay Error page detected: " + url);
                    isErrorHandled = true; // Đánh dấu đã xử lý
                    handleVNPayError(url);
                    return true; // Ngăn WebView load error page
                }
                
                // Nếu URL là localhost (không thể truy cập từ điện thoại), bỏ qua
                if (url.contains("localhost") || url.contains("127.0.0.1")) {
                    Log.w(TAG, "Ignoring localhost URL: " + url);
                    // VNPay có thể redirect về localhost, nhưng app không thể truy cập
                    // Thử parse từ URL hiện tại nếu có query params
                    if (url.contains("vnp_ResponseCode") || url.contains("vnp_TransactionStatus")) {
                        handlePaymentResult(url);
                    }
                    return true; // Ngăn WebView load localhost
                }
                
                return false; // Cho phép WebView load URL bình thường
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Page finished loading: " + url);
                
                // Kiểm tra nếu URL là VNPay Error page (chỉ xử lý nếu chưa xử lý)
                if ((url.contains("/Payment/Error.html") || url.contains("code=")) && !isErrorHandled) {
                    Log.e(TAG, "VNPay Error page loaded: " + url);
                    isErrorHandled = true; // Đánh dấu đã xử lý
                    handleVNPayError(url);
                    return;
                }
                
                // Kiểm tra nếu URL chứa kết quả thanh toán
                if (url.contains("vnp_ResponseCode") || url.contains("vnp_TransactionStatus")) {
                    handlePaymentResult(url);
                }
                
                // Kiểm tra nếu trang có thông báo lỗi từ VNPay (đặc biệt là lỗi chữ ký)
                try {
                    // Inject JavaScript để kiểm tra có thông báo lỗi không
                    view.evaluateJavascript(
                        "(function() { " +
                        "  var errorMsg = document.querySelector('.error-message, .alert-danger, [class*=\"error\"], .text-danger'); " +
                        "  if (errorMsg) return errorMsg.innerText || errorMsg.textContent; " +
                        "  var title = document.querySelector('h1, h2, .title, .page-title'); " +
                        "  if (title && (title.textContent.includes('lỗi') || title.textContent.includes('error') || " +
                        "      title.textContent.includes('chữ ký') || title.textContent.includes('signature'))) { " +
                        "    return title.textContent; " +
                        "  } " +
                        "  // Kiểm tra body text cho lỗi chữ ký" +
                        "  var bodyText = document.body.innerText || document.body.textContent; " +
                        "  if (bodyText && (bodyText.includes('chữ ký') || bodyText.includes('signature') || " +
                        "      bodyText.includes('Checksum') || bodyText.includes('checksum'))) { " +
                        "    return bodyText.substring(0, 200); " +
                        "  } " +
                        "  return null; " +
                        "})();",
                        result -> {
                            if (result != null && !result.equals("null") && !result.isEmpty()) {
                                String errorText = result.replace("\"", "").trim();
                                Log.w(TAG, "Detected error message on page: " + errorText);
                                
                                // Nếu là lỗi chữ ký, hiển thị thông báo cụ thể
                                if (errorText.toLowerCase().contains("chữ ký") || 
                                    errorText.toLowerCase().contains("signature") ||
                                    errorText.toLowerCase().contains("checksum")) {
                                    runOnUiThread(() -> {
                                        ToastManager.showToast(VNPayWebViewActivity.this, 
                                            "Lỗi chữ ký VNPay. Vui lòng kiểm tra cấu hình Return URL ở backend.", 
                                            Toast.LENGTH_LONG);
                                        Log.e(TAG, "⚠️ VNPay Signature Error Detected!");
                                        Log.e(TAG, "⚠️ Backend must update VNPAY_RETURN_URL in .env file");
                                        Log.e(TAG, "⚠️ Current machine IP: 192.168.25.97");
                                        Log.e(TAG, "⚠️ Should be: VNPAY_RETURN_URL=http://192.168.25.97:3000/api/payment/vnpay/return");
                                    });
                                }
                            }
                        }
                    );
                } catch (Exception e) {
                    Log.e(TAG, "Error checking page for errors: " + e.getMessage());
                }
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "WebView Error: " + errorCode + " - " + description + " for URL: " + failingUrl);
                
                // Bỏ qua lỗi ERR_UNKNOWN_URL_SCHEME cho intent:// URLs (đã xử lý trong shouldOverrideUrlLoading)
                if (errorCode == -10 && failingUrl != null && failingUrl.startsWith("intent://")) {
                    Log.d(TAG, "Ignoring ERR_UNKNOWN_URL_SCHEME for intent URL (already handled)");
                    return; // Đã xử lý trong shouldOverrideUrlLoading
                }
                
                // Nếu lỗi là localhost không thể truy cập, thử parse từ URL
                if (failingUrl != null && (failingUrl.contains("localhost") || failingUrl.contains("127.0.0.1"))) {
                    if (failingUrl.contains("vnp_ResponseCode") || failingUrl.contains("vnp_TransactionStatus")) {
                        Log.d(TAG, "Trying to parse result from localhost URL");
                        handlePaymentResult(failingUrl);
                        return;
                    }
                }
                
                // Chỉ hiển thị thông báo lỗi cho các lỗi nghiêm trọng khác
                if (errorCode != -10) { // -10 là ERR_UNKNOWN_URL_SCHEME, đã xử lý
                    runOnUiThread(() -> {
                        ToastManager.showToast(VNPayWebViewActivity.this, 
                            "Lỗi tải trang thanh toán: " + description, 
                            Toast.LENGTH_LONG);
                    });
                }
            }
        });
        
        // Load payment URL
        webView.loadUrl(paymentUrl);
    }
    
    /**
     * Xử lý kết quả thanh toán từ return URL
     */
    private void handlePaymentResult(String returnUrl) {
        Log.d(TAG, "Handling payment result from URL: " + returnUrl);
        
        try {
            Uri uri = Uri.parse(returnUrl);
            String vnpResponseCode = uri.getQueryParameter("vnp_ResponseCode");
            String vnpTransactionStatus = uri.getQueryParameter("vnp_TransactionStatus");
            String vnpTxnRef = uri.getQueryParameter("vnp_TxnRef");
            String vnpTransactionNo = uri.getQueryParameter("vnp_TransactionNo");
            
            Log.d(TAG, "VNPay Response Code: " + vnpResponseCode);
            Log.d(TAG, "VNPay Transaction Status: " + vnpTransactionStatus);
            Log.d(TAG, "VNPay TxnRef: " + vnpTxnRef);
            Log.d(TAG, "VNPay Transaction No: " + vnpTransactionNo);
            
            // Kiểm tra kết quả thanh toán
            // vnp_ResponseCode = "00" và vnp_TransactionStatus = "00" nghĩa là thành công
            if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
                // Thanh toán thành công
                Log.d(TAG, "Payment succeeded");
                
                // Trả kết quả về ThanhToanActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("payment_status", "success");
                resultIntent.putExtra("order_id", orderId);
                resultIntent.putExtra("vnp_txn_ref", vnpTxnRef);
                resultIntent.putExtra("vnp_transaction_no", vnpTransactionNo);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                // Thanh toán thất bại
                Log.d(TAG, "Payment failed. Response Code: " + vnpResponseCode + 
                      ", Transaction Status: " + vnpTransactionStatus);
                
                String errorMessage = "Thanh toán thất bại";
                if (vnpResponseCode != null) {
                    switch (vnpResponseCode) {
                        case "07":
                            errorMessage = "Trừ tiền thành công nhưng giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
                            break;
                        case "09":
                            errorMessage = "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking";
                            break;
                        case "10":
                            errorMessage = "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
                            break;
                        case "11":
                            errorMessage = "Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch";
                            break;
                        case "12":
                            errorMessage = "Thẻ/Tài khoản bị khóa";
                            break;
                        case "13":
                            errorMessage = "Nhập sai mật khẩu xác thực giao dịch (OTP)";
                            break;
                        case "51":
                            errorMessage = "Tài khoản không đủ số dư để thực hiện giao dịch";
                            break;
                        case "65":
                            errorMessage = "Tài khoản đã vượt quá hạn mức giao dịch trong ngày";
                            break;
                        case "75":
                            errorMessage = "Ngân hàng thanh toán đang bảo trì";
                            break;
                        case "79":
                            errorMessage = "Nhập sai mật khẩu thanh toán quá số lần quy định";
                            break;
                        default:
                            errorMessage = "Thanh toán thất bại. Mã lỗi: " + vnpResponseCode;
                            break;
                    }
                }
                
                Intent resultIntent = new Intent();
                resultIntent.putExtra("payment_status", "failed");
                resultIntent.putExtra("order_id", orderId);
                resultIntent.putExtra("error_message", errorMessage);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing payment result: " + e.getMessage(), e);
            ToastManager.showToast(this, "Lỗi xử lý kết quả thanh toán", Toast.LENGTH_SHORT);
            
            Intent resultIntent = new Intent();
            resultIntent.putExtra("payment_status", "failed");
            resultIntent.putExtra("order_id", orderId);
            resultIntent.putExtra("error_message", "Lỗi xử lý kết quả thanh toán");
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
    
    /**
     * Xử lý lỗi từ VNPay Error page
     */
    private void handleVNPayError(String errorUrl) {
        Log.e(TAG, "Handling VNPay error from URL: " + errorUrl);
        
        try {
            Uri uri = Uri.parse(errorUrl);
            String errorCode = uri.getQueryParameter("code");
            
            Log.e(TAG, "VNPay Error Code: " + errorCode);
            
            String errorMessage = "Lỗi thanh toán VNPay";
            
            if (errorCode != null) {
                switch (errorCode) {
                    case "70":
                        // Tạo error message chi tiết với return URL nếu có
                        StringBuilder errorMsgBuilder = new StringBuilder();
                        errorMsgBuilder.append("❌ Lỗi chữ ký VNPay (Code 70)\n\n");
                        errorMsgBuilder.append("Nguyên nhân:\n");
                        errorMsgBuilder.append("• Return URL trong backend .env không khớp\n");
                        errorMsgBuilder.append("• Chữ ký (checksum) bị sai do URL không khớp\n\n");
                        errorMsgBuilder.append("Giải pháp:\n");
                        errorMsgBuilder.append("1. Kiểm tra IP máy tính hiện tại\n");
                        errorMsgBuilder.append("2. Mở file .env trong backend\n");
                        errorMsgBuilder.append("3. Tìm dòng VNPAY_RETURN_URL\n");
                        errorMsgBuilder.append("4. Cập nhật thành:\n");
                        errorMsgBuilder.append("   VNPAY_RETURN_URL=http://[IP_MÁY_TÍNH]:3000/api/payment/vnpay/return\n");
                        errorMsgBuilder.append("5. Restart backend server\n");
                        errorMsgBuilder.append("6. Test lại thanh toán\n\n");
                        
                        if (returnUrl != null && !returnUrl.isEmpty()) {
                            errorMsgBuilder.append("📋 Return URL từ request:\n");
                            errorMsgBuilder.append(returnUrl);
                            errorMsgBuilder.append("\n\n");
                            
                            // Kiểm tra nếu return URL là localhost
                            if (returnUrl.contains("localhost") || returnUrl.contains("127.0.0.1")) {
                                errorMsgBuilder.append("⚠️ CẢNH BÁO: Return URL chứa localhost!\n");
                                errorMsgBuilder.append("⚠️ VNPay không thể redirect về localhost!\n");
                                errorMsgBuilder.append("⚠️ Cần thay bằng IP thật của máy tính!\n\n");
                            }
                        } else {
                            errorMsgBuilder.append("⚠️ Không tìm thấy Return URL trong request\n");
                            errorMsgBuilder.append("⚠️ Backend có thể đã set nhưng không pass qua URL\n\n");
                        }
                        
                        errorMsgBuilder.append("💡 Lưu ý: IP máy tính có thể thay đổi khi đổi WiFi\n");
                        errorMsgBuilder.append("💡 Cần cập nhật VNPAY_RETURN_URL mỗi khi IP thay đổi");
                        
                        errorMessage = errorMsgBuilder.toString();
                        Log.e(TAG, "❌ VNPay Code 70 - Signature Error");
                        Log.e(TAG, "Return URL từ request: " + (returnUrl != null ? returnUrl : "NULL"));
                        Log.e(TAG, "⚠️ Cần cập nhật VNPAY_RETURN_URL trong backend .env");
                        break;
                    case "97":
                        errorMessage = "Lỗi chữ ký (Checksum failed)";
                        break;
                    case "99":
                        errorMessage = "Lỗi không xác định từ VNPay";
                        break;
                    default:
                        errorMessage = "Lỗi VNPay (Mã lỗi: " + errorCode + ")\n\n" +
                                      "Vui lòng kiểm tra cấu hình backend hoặc thử lại sau.";
                        break;
                }
            } else {
                // Không có error code, thử lấy từ URL
                if (errorUrl.contains("code=")) {
                    int codeIndex = errorUrl.indexOf("code=");
                    if (codeIndex >= 0) {
                        String codeStr = errorUrl.substring(codeIndex + 5);
                        int endIndex = codeStr.indexOf("&");
                        if (endIndex > 0) {
                            codeStr = codeStr.substring(0, endIndex);
                        }
                        errorCode = codeStr;
                        Log.e(TAG, "Extracted error code from URL: " + errorCode);
                        
                        if ("70".equals(errorCode)) {
                            // Tạo error message chi tiết với return URL nếu có
                            StringBuilder errorMsgBuilder = new StringBuilder();
                            errorMsgBuilder.append("❌ Lỗi chữ ký VNPay (Code 70)\n\n");
                            errorMsgBuilder.append("Nguyên nhân:\n");
                            errorMsgBuilder.append("• Return URL trong backend .env không khớp\n");
                            errorMsgBuilder.append("• Chữ ký (checksum) bị sai do URL không khớp\n\n");
                            errorMsgBuilder.append("Giải pháp:\n");
                            errorMsgBuilder.append("1. Kiểm tra IP máy tính hiện tại\n");
                            errorMsgBuilder.append("2. Mở file .env trong backend\n");
                            errorMsgBuilder.append("3. Tìm dòng VNPAY_RETURN_URL\n");
                            errorMsgBuilder.append("4. Cập nhật thành:\n");
                            errorMsgBuilder.append("   VNPAY_RETURN_URL=http://[IP_MÁY_TÍNH]:3000/api/payment/vnpay/return\n");
                            errorMsgBuilder.append("5. Restart backend server\n");
                            errorMsgBuilder.append("6. Test lại thanh toán\n\n");
                            
                            if (returnUrl != null && !returnUrl.isEmpty()) {
                                errorMsgBuilder.append("📋 Return URL từ request:\n");
                                errorMsgBuilder.append(returnUrl);
                                errorMsgBuilder.append("\n\n");
                                
                                // Kiểm tra nếu return URL là localhost
                                if (returnUrl.contains("localhost") || returnUrl.contains("127.0.0.1")) {
                                    errorMsgBuilder.append("⚠️ CẢNH BÁO: Return URL chứa localhost!\n");
                                    errorMsgBuilder.append("⚠️ VNPay không thể redirect về localhost!\n");
                                    errorMsgBuilder.append("⚠️ Cần thay bằng IP thật của máy tính!\n\n");
                                }
                            } else {
                                errorMsgBuilder.append("⚠️ Không tìm thấy Return URL trong request\n");
                                errorMsgBuilder.append("⚠️ Backend có thể đã set nhưng không pass qua URL\n\n");
                            }
                            
                            errorMsgBuilder.append("💡 Lưu ý: IP máy tính có thể thay đổi khi đổi WiFi\n");
                            errorMsgBuilder.append("💡 Cần cập nhật VNPAY_RETURN_URL mỗi khi IP thay đổi");
                            
                            errorMessage = errorMsgBuilder.toString();
                            Log.e(TAG, "❌ VNPay Code 70 - Signature Error (from URL parsing)");
                            Log.e(TAG, "Return URL từ request: " + (returnUrl != null ? returnUrl : "NULL"));
                            Log.e(TAG, "⚠️ Cần cập nhật VNPAY_RETURN_URL trong backend .env");
                        }
                    }
                }
            }
            
            // Tạo biến final để sử dụng trong lambda
            final String finalErrorMessage = errorMessage;
            final String finalErrorCode = errorCode;
            
            // Hiển thị thông báo lỗi
            runOnUiThread(() -> {
                ToastManager.showToast(VNPayWebViewActivity.this, finalErrorMessage, Toast.LENGTH_LONG);
            });
            
            // Trả kết quả về ThanhToanActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("payment_status", "failed");
            resultIntent.putExtra("order_id", orderId);
            resultIntent.putExtra("error_message", finalErrorMessage);
            resultIntent.putExtra("vnpay_error_code", finalErrorCode);
            setResult(RESULT_OK, resultIntent);
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing VNPay error: " + e.getMessage(), e);
            
            final String errorMessage = "Lỗi xử lý kết quả thanh toán VNPay";
            runOnUiThread(() -> {
                ToastManager.showToast(VNPayWebViewActivity.this, errorMessage, Toast.LENGTH_LONG);
            });
            
            Intent resultIntent = new Intent();
            resultIntent.putExtra("payment_status", "failed");
            resultIntent.putExtra("order_id", orderId);
            resultIntent.putExtra("error_message", errorMessage);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Nếu user nhấn back, hủy thanh toán
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("payment_status", "cancelled");
            resultIntent.putExtra("order_id", orderId);
            setResult(RESULT_CANCELED, resultIntent);
            finish();
        }
    }
}

