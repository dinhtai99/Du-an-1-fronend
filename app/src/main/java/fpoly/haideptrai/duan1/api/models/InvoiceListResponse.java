package fpoly.haideptrai.duan1.api.models;

import java.util.List;

public class InvoiceListResponse {
    // Backend có thể trả về "invoices" hoặc "data"
    private List<InvoiceResponse> invoices;
    private List<InvoiceResponse> data; // Backend format: { success: true, data: [...] }

    // Pagination có thể ở root hoặc trong object "pagination"
    private int total;
    private int page;
    private int limit;
    private int totalPages;

    private Pagination pagination; // Backend format: { pagination: { page, limit, total, pages } }

    // Getter cho invoices - ưu tiên data nếu có
    public List<InvoiceResponse> getInvoices() {
        if (data != null && !data.isEmpty()) {
            return data;
        }
        return invoices;
    }

    public void setInvoices(List<InvoiceResponse> invoices) {
        this.invoices = invoices;
    }

    public List<InvoiceResponse> getData() {
        return data;
    }

    public void setData(List<InvoiceResponse> data) {
        this.data = data;
    }

    // Getter cho pagination - ưu tiên pagination object nếu có
    public int getTotal() {
        if (pagination != null) {
            return pagination.getTotal();
        }
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        if (pagination != null) {
            return pagination.getPage();
        }
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        if (pagination != null) {
            return pagination.getLimit();
        }
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getTotalPages() {
        if (pagination != null) {
            return pagination.getPages();
        }
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    // Inner class cho pagination object
    public static class Pagination {
        private int page;
        private int limit;
        private int total;
        private int pages;

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getPages() { return pages; }
        public void setPages(int pages) { this.pages = pages; }
    }
}
