package fpoly.haideptrai.duan1.api.models;

import java.util.List;

public class ProductListResponse {
    private List<ProductResponse> products;
    private int total;
    private int page;
    private int limit;
    private int totalPages;

    public List<ProductResponse> getProducts() { return products; }
    public void setProducts(List<ProductResponse> products) { this.products = products; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
