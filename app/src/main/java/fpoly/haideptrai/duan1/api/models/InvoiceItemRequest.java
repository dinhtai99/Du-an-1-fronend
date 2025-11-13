package fpoly.haideptrai.duan1.api.models;

public class InvoiceItemRequest {
    private String product;
    private Integer quantity;
    private Double price;
    private Double discount;

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }
}

