package fpoly.haideptrai.duan1.customer.models;

public class TimelineItem {
    private String status;
    private String dateTime;
    private boolean isCompleted;

    public TimelineItem(String status, String dateTime, boolean isCompleted) {
        this.status = status;
        this.dateTime = dateTime;
        this.isCompleted = isCompleted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}

