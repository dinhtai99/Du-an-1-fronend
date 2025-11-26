package fpoly.haideptrai.duan1.api.models;

import java.util.List;

public class InvoiceRequest {
    private String customer;
    private List<InvoiceItemRequest> items;
    private Double discount;
    private String paymentMethod;
    private String notes;
    private String voucherCode;
    private String addressId; // ID của địa chỉ đã lưu (ưu tiên dùng)
    private ShippingAddress shippingAddress; // Địa chỉ trực tiếp (nếu không có addressId)

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

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    public List<InvoiceItemRequest> getItems() { return items; }
    public void setItems(List<InvoiceItemRequest> items) { this.items = items; }
    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }
}
