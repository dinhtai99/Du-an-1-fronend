package fpoly.haideptrai.duan1.api.models;

public class ZaloPayCreateResponse {
    private boolean success;
    private String message;
    private String zp_trans_token;
    private String order_url;
    private String order_token;
    private String orderId;
    private String orderNumber;
    private String error; // Nếu có lỗi
    private Integer return_code; // Mã lỗi từ ZaloPay API
    private Integer sub_return_code; // Mã lỗi chi tiết từ ZaloPay API

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getZp_trans_token() { return zp_trans_token; }
    public void setZp_trans_token(String zp_trans_token) { this.zp_trans_token = zp_trans_token; }

    public String getOrder_url() { return order_url; }
    public void setOrder_url(String order_url) { this.order_url = order_url; }

    public String getOrder_token() { return order_token; }
    public void setOrder_token(String order_token) { this.order_token = order_token; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Integer getReturn_code() { return return_code; }
    public void setReturn_code(Integer return_code) { this.return_code = return_code; }

    public Integer getSub_return_code() { return sub_return_code; }
    public void setSub_return_code(Integer sub_return_code) { this.sub_return_code = sub_return_code; }
}

