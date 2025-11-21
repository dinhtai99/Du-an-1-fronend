package fpoly.haideptrai.duan1.api.models;

import java.util.List;

public class ReviewResponse {
    private String _id;
    private String user;
    private UserInfo userInfo; // populated user info
    private String product;
    private ProductInfo productInfo; // populated product info
    private String order;
    private Integer rating;
    private String comment;
    private List<String> images;
    private String createdAt;
    private String updatedAt;

    public static class UserInfo {
        private String fullName;
        private String avatar;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }

    public static class ProductInfo {
        private String name;
        private String image;
        private Double price;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public UserInfo getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public ProductInfo getProductInfo() { return productInfo; }
    public void setProductInfo(ProductInfo productInfo) { this.productInfo = productInfo; }
    public String getOrder() { return order; }
    public void setOrder(String order) { this.order = order; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}

