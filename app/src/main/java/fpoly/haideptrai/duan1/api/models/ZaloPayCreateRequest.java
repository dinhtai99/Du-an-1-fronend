package fpoly.haideptrai.duan1.api.models;

import java.util.List;

public class ZaloPayCreateRequest {
    private ShippingAddress shippingAddress;
    private String notes;
    private String voucherCode;
    private String orderId; // Optional: nếu đã tạo order trước
    private List<CartItem> items; // Cart items từ client (nếu không có trong database)

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

    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    // Cart item model để gửi từ client
    public static class CartItem {
        private String product;
        private Integer quantity;
        private Double price;
        private String color;
        private String size;

        public String getProduct() { return product; }
        public void setProduct(String product) { this.product = product; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
    }
}

