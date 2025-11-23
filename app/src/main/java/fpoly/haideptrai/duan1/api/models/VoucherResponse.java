package fpoly.haideptrai.duan1.api.models;

import java.util.List;

public class VoucherResponse {
    private String _id;
    private String code;
    private String name;
    private String description;
    private Double discount;
    private String discountType; // "percentage" or "fixed"
    private Double minOrderAmount;
    private Integer quantity;
    private Integer used;
    private String startDate;
    private String endDate;
    private String status; // "active", "inactive", "expired"
    private List<Object> applicableUsers; // Danh sách user ID được phép dùng voucher (có thể là string hoặc object với _id, empty = tất cả user)
    private String createdAt;
    private String updatedAt;

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public Double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getUsed() { return used; }
    public void setUsed(Integer used) { this.used = used; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<Object> getApplicableUsers() { return applicableUsers; }
    public void setApplicableUsers(List<Object> applicableUsers) { this.applicableUsers = applicableUsers; }
}

