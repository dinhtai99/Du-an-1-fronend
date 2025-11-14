package fpoly.haideptrai.duan1.customer.models;

import fpoly.haideptrai.duan1.api.models.ProductResponse;

public class CartItem {
    private ProductResponse product;
    private int quantity;
    private double price;

    public CartItem(ProductResponse product, int quantity, double price) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public ProductResponse getProduct() {
        return product;
    }

    public void setProduct(ProductResponse product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

