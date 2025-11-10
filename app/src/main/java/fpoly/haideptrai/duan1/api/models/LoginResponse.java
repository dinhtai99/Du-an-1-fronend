package fpoly.haideptrai.duan1.api.models;

public class LoginResponse {
    private String message;
    private String token;
    private UserInfo user;
    
    public LoginResponse() {}
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public void setUser(UserInfo user) {
        this.user = user;
    }
}
