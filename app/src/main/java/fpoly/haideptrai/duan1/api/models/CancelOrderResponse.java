package fpoly.haideptrai.duan1.api.models;

public class CancelOrderResponse {
    private String _id;
    private String name;
    private String description;
    private Integer status; // 1 active, 0 inactive (if provided)
    private String image; // Image URL for category icon

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
