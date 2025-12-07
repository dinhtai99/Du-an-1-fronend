package fpoly.haideptrai.duan1.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InvoiceResponse {
    @SerializedName("_id")
    private String _id;
    @SerializedName("orderNumber")
    private String invoiceNumber;
    private CustomerResponse customer;
    private UserInfo staff;
    private List<Item> items;
    private Double subtotal;
    private Double discount;
    private Double total;
    private String status;
    private String paymentMethod;
    private ShippingAddress shippingAddress;
    private String createdAt;
    private String updatedAt;
    
    // Return/Exchange fields
    @SerializedName("returnRequestedAt")
    private String returnRequestedAt;
    
    @SerializedName("returnReason")
    private String returnReason;
    
    @SerializedName("returnItems")
    private List<ReturnItem> returnItems;
    
    @SerializedName("exchangeItems")
    private List<ExchangeItem> exchangeItems;
    
    @SerializedName("returnProcessedAt")
    private String returnProcessedAt;
    
    @SerializedName("returnProcessedBy")
    private UserInfo returnProcessedBy;

    public static class ShippingAddress {
        private String fullName;
        private String phone;
        private String address;
        private String ward;
        private String district;
        private String city;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getWard() { return ward; }
        public void setWard(String ward) { this.ward = ward; }

        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }

    public static class Item {
        private ProductResponse product;
        private Integer quantity;
        private Double price;
        private Double discount;
        private Double subtotal;
        public ProductResponse getProduct() { return product; }
        public void setProduct(ProductResponse product) { this.product = product; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Double getDiscount() { return discount; }
        public void setDiscount(Double discount) { this.discount = discount; }
        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    }

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public CustomerResponse getCustomer() { return customer; }
    public void setCustomer(CustomerResponse customer) { this.customer = customer; }
    public UserInfo getStaff() { return staff; }
    public void setStaff(UserInfo staff) { this.staff = staff; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    // Return/Exchange getters and setters
    public String getReturnRequestedAt() { return returnRequestedAt; }
    public void setReturnRequestedAt(String returnRequestedAt) { this.returnRequestedAt = returnRequestedAt; }
    
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    
    public List<ReturnItem> getReturnItems() { return returnItems; }
    public void setReturnItems(List<ReturnItem> returnItems) { this.returnItems = returnItems; }
    
    public List<ExchangeItem> getExchangeItems() { return exchangeItems; }
    public void setExchangeItems(List<ExchangeItem> exchangeItems) { this.exchangeItems = exchangeItems; }
    
    public String getReturnProcessedAt() { return returnProcessedAt; }
    public void setReturnProcessedAt(String returnProcessedAt) { this.returnProcessedAt = returnProcessedAt; }
    
    public UserInfo getReturnProcessedBy() { return returnProcessedBy; }
    public void setReturnProcessedBy(UserInfo returnProcessedBy) { this.returnProcessedBy = returnProcessedBy; }
    
    // Return/Exchange nested classes
    // Note: Backend trả về product là string ID, không phải object
    public static class ReturnItem {
        @SerializedName("product")
        private String product; // Product ID (string)
        private Integer quantity;
        private String reason;
        
        public String getProduct() { return product; }
        public void setProduct(String product) { this.product = product; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class ExchangeItem {
        @SerializedName("oldProduct")
        private String oldProduct; // Product ID (string)
        
        @SerializedName("newProduct")
        private String newProduct; // Product ID (string)
        
        private Integer quantity;
        
        public String getOldProduct() { return oldProduct; }
        public void setOldProduct(String oldProduct) { this.oldProduct = oldProduct; }
        
        public String getNewProduct() { return newProduct; }
        public void setNewProduct(String newProduct) { this.newProduct = newProduct; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
