package fpoly.haideptrai.duan1.api.models;

public class ChatMessageRequest {
    private String message;
    // Backend không cần type cho customer, chỉ cần message
    // Admin mới cần customerId

    public ChatMessageRequest() {
    }

    public ChatMessageRequest(String message) {
        this.message = message;
    }

    public String getMessage() { 
        return message; 
    }
    
    public void setMessage(String message) { 
        this.message = message; 
    }
}

