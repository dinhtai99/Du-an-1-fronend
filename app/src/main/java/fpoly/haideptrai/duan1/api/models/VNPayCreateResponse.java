package fpoly.haideptrai.duan1.api.models;

public class VNPayCreateResponse {
    private boolean success;
    private String message;
    private String paymentUrl; // URL để mở WebView/Browser thanh toán
    private String orderId;
    private String orderNumber;
    private String vnp_TxnRef; // Transaction reference từ VNPay
    private String error; // Nếu có lỗi

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getVnp_TxnRef() { return vnp_TxnRef; }
    public void setVnp_TxnRef(String vnp_TxnRef) { this.vnp_TxnRef = vnp_TxnRef; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}

