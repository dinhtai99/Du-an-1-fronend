package fpoly.haideptrai.duan1.customer.models;

public class Message {
    private String text;
    private String time;
    private boolean isUser;

    public Message(String text, String time, boolean isUser) {
        this.text = text;
        this.time = time;
        this.isUser = isUser;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
}

