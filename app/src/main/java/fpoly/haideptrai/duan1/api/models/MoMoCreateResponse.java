package fpoly.haideptrai.duan1.api.models;

/**
 * Response model cho MoMo Payment
 * Mapping với response từ backend API /api/payment/momo/create
 */
public class MoMoCreateResponse {
    private boolean success;
    private String message;
    private String payUrl; // URL thanh toán MoMo
    private String deeplink; // Deep link để mở MoMo app
    private String qrCodeUrl; // QR code URL (nếu có)
    private String orderId; // Order ID từ database
    private String orderNumber; // Order number
    private String momoOrderId; // MoMo order ID
    private String momoRequestId; // MoMo request ID
    private String error; // Nếu có lỗi

    // Data wrapper (nếu backend trả về { success, message, data: {...} })
    private Data data;

    public static class Data {
        private String orderId;
        private String orderNumber;
        private String payUrl;
        private String deeplink;
        private String qrCodeUrl;
        private String momoOrderId;
        private String momoRequestId;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

        public String getPayUrl() { return payUrl; }
        public void setPayUrl(String payUrl) { this.payUrl = payUrl; }

        public String getDeeplink() { return deeplink; }
        public void setDeeplink(String deeplink) { this.deeplink = deeplink; }

        public String getQrCodeUrl() { return qrCodeUrl; }
        public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

        public String getMomoOrderId() { return momoOrderId; }
        public void setMomoOrderId(String momoOrderId) { this.momoOrderId = momoOrderId; }

        public String getMomoRequestId() { return momoRequestId; }
        public void setMomoRequestId(String momoRequestId) { this.momoRequestId = momoRequestId; }
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }

    public String getDeeplink() { return deeplink; }
    public void setDeeplink(String deeplink) { this.deeplink = deeplink; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getMomoOrderId() { return momoOrderId; }
    public void setMomoOrderId(String momoOrderId) { this.momoOrderId = momoOrderId; }

    public String getMomoRequestId() { return momoRequestId; }
    public void setMomoRequestId(String momoRequestId) { this.momoRequestId = momoRequestId; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }

    /**
     * Helper method để lấy payUrl từ data hoặc trực tiếp
     */
    public String getPayUrlOrDeeplink() {
        if (data != null && data.getPayUrl() != null) {
            return data.getPayUrl();
        }
        if (data != null && data.getDeeplink() != null) {
            return data.getDeeplink();
        }
        if (payUrl != null) {
            return payUrl;
        }
        return deeplink;
    }
}

