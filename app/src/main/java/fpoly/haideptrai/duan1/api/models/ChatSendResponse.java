package fpoly.haideptrai.duan1.api.models;

public class ChatSendResponse {
    private boolean success;
    private String message;
    private ChatResponse chat;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ChatResponse getChat() {
        return chat;
    }

    public void setChat(ChatResponse chat) {
        this.chat = chat;
    }

    // Inner class cho Chat object từ backend
    public static class ChatResponse {
        private String _id;
        private String customer;
        private java.util.List<ChatMessageResponse> messages;
        private Integer adminUnreadCount;
        private Integer customerUnreadCount;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getCustomer() {
            return customer;
        }

        public void setCustomer(String customer) {
            this.customer = customer;
        }

        public java.util.List<ChatMessageResponse> getMessages() {
            return messages;
        }

        public void setMessages(java.util.List<ChatMessageResponse> messages) {
            this.messages = messages;
        }

        public Integer getAdminUnreadCount() {
            return adminUnreadCount;
        }

        public void setAdminUnreadCount(Integer adminUnreadCount) {
            this.adminUnreadCount = adminUnreadCount;
        }

        public Integer getCustomerUnreadCount() {
            return customerUnreadCount;
        }

        public void setCustomerUnreadCount(Integer customerUnreadCount) {
            this.customerUnreadCount = customerUnreadCount;
        }
    }
}

