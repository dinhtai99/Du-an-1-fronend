package fpoly.haideptrai.duan1.api.models;

import java.util.List;

public class InvoiceRequest {
    private String customer;
    private List<InvoiceItemRequest> items;
    private Double discount;
    private String paymentMethod;
    private String notes;

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
}

