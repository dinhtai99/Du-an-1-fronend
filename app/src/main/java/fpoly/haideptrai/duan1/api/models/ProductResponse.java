package fpoly.haideptrai.duan1.api.models;

public class ProductResponse {
    private String _id;
    private String name;
    private CategoryResponse category; // populated
    private Double importPrice;
    private Double price;
    private Integer stock;
    private Integer minStock;
    private String description;
    private String image;
    private boolean lowStockWarning;

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryResponse getCategory() { return category; }
    public void setCategory(CategoryResponse category) { this.category = category; }

    public Double getImportPrice() { return importPrice; }
    public void setImportPrice(Double importPrice) { this.importPrice = importPrice; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public boolean isLowStockWarning() { return lowStockWarning; }
    public void setLowStockWarning(boolean lowStockWarning) { this.lowStockWarning = lowStockWarning; }
}
