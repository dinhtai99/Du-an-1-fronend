package fpoly.haideptrai.duan1.api.models;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReviewResponse {
    private String _id;
    @JsonAdapter(UserTypeAdapter.class)
    @SerializedName("user")
    private UserInfo userInfo; // Can be string ID or object from API
    @JsonAdapter(ProductTypeAdapter.class)
    @SerializedName("product")
    private ProductInfo productInfo; // Can be string ID or object from API
    private String order;
    private Integer rating;
    private String comment;
    private List<String> images;
    private Boolean isVisible; // Admin có thể ẩn/hiện đánh giá
    private String createdAt;
    private String updatedAt;

    public static class UserInfo {
        private String _id;
        private String fullName;
        private String avatar;

        public String get_id() { return _id; }
        public void set_id(String _id) { this._id = _id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }

    public static class ProductInfo {
        private String _id;
        private String name;
        private String image;
        private Double price;

        public String get_id() { return _id; }
        public void set_id(String _id) { this._id = _id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public UserInfo getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }
    // Helper method to get user ID (from userInfo._id or userInfo itself)
    public String getUser() { 
        return userInfo != null ? userInfo.get_id() : null; 
    }
    public ProductInfo getProductInfo() { return productInfo; }
    public void setProductInfo(ProductInfo productInfo) { this.productInfo = productInfo; }
    // Helper method to get product ID (from productInfo._id)
    public String getProduct() {
        return productInfo != null ? productInfo.get_id() : null;
    }
    public String getOrder() { return order; }
    public void setOrder(String order) { this.order = order; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}

