package fpoly.haideptrai.duan1.api.models;

import com.google.gson.annotations.SerializedName;

public class CancelOrderRequest {
    @SerializedName("reason")
    private String reason;

    public CancelOrderRequest() {
    }

    public CancelOrderRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}