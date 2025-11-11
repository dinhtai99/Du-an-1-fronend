package fpoly.haideptrai.duan1.api.models;

import java.util.List;

public class UserListResponse {
    private List<UserResponse> users;
    private Integer total;
    private Integer page;
    private Integer limit;
    private Integer totalPages;

    public List<UserResponse> getUsers() { return users; }
    public void setUsers(List<UserResponse> users) { this.users = users; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
}
