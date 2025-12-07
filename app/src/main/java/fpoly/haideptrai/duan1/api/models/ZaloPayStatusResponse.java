package fpoly.haideptrai.duan1.api.models;

public class ZaloPayStatusResponse {
    private String orderId;
    private String orderNumber;
    private String paymentMethod;
    private String paymentStatus; // pending, processing, success, failed
    private String status; // new, processing, shipped, delivered, cancelled
    private Double total;
    private String zalopayTransToken;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getZalopayTransToken() {
        return zalopayTransToken;
    }

    public void setZalopayTransToken(String zalopayTransToken) {
        this.zalopayTransToken = zalopayTransToken;
    }
}

