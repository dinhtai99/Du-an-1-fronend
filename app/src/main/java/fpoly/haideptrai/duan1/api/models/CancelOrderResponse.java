package fpoly.haideptrai.duan1.api.models;

import com.google.gson.annotations.SerializedName;

public class CancelOrderResponse {
    @SerializedName("success")
    private Boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private CancelOrderData data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CancelOrderData getData() {
        return data;
    }

    public void setData(CancelOrderData data) {
        this.data = data;
    }

    public static class CancelOrderData {
        @SerializedName("order")
        private InvoiceResponse order;
        
        @SerializedName("refundInfo")
        private RefundInfo refundInfo;

        public InvoiceResponse getOrder() {
            return order;
        }

        public void setOrder(InvoiceResponse order) {
            this.order = order;
        }

        public RefundInfo getRefundInfo() {
            return refundInfo;
        }

        public void setRefundInfo(RefundInfo refundInfo) {
            this.refundInfo = refundInfo;
        }
    }

    public static class RefundInfo {
        @SerializedName("needsRefund")
        private Boolean needsRefund;
        
        @SerializedName("amount")
        private Double amount;
        
        @SerializedName("paymentMethod")
        private String paymentMethod;
        
        @SerializedName("message")
        private String message;

        public Boolean getNeedsRefund() {
            return needsRefund;
        }

        public void setNeedsRefund(Boolean needsRefund) {
            this.needsRefund = needsRefund;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

