package fpoly.haideptrai.duan1.api;

public class TokenStore {
    private static volatile String token;

    public static void setToken(String t) {
        token = t;
    }

    public static String getToken() {
        return token;
    }
}
