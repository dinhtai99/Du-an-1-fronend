package fpoly.haideptrai.duan1.api.models;

import java.util.List;

public class InvoiceResponse {
    private String _id;
    private String invoiceNumber;
    private CustomerResponse customer;
    private UserInfo staff;
    private List<Item> items;
    private Double subtotal;
    private Double discount;
    private Double total;
    private String status;

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
}
