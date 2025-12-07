package fpoly.haideptrai.duan1.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewRequest {
    @SerializedName("productId")
    private String productId;
    @SerializedName("orderId")
    private String orderId;
    private Integer rating;
    private String comment;
    private List<String> images;

    public ReviewRequest() {}

    public ReviewRequest(String productId, String orderId, Integer rating, String comment, List<String> images) {
        this.productId = productId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.images = images;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}

