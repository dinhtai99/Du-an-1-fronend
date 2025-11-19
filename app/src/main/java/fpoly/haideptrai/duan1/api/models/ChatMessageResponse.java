package fpoly.haideptrai.duan1.api.models;

public class ChatMessageResponse {
    private String _id;
    private String senderId;
    private String senderRole; // "customer" or "admin"
    private String message;
    private String createdAt;
    private Boolean isRead;

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
    // Helper method để tương thích với code cũ
    public String getType() {
        return "customer".equals(senderRole) ? "user" : "system";
    }
}

