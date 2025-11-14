package fpoly.haideptrai.duan1.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.customer.models.CartItem;

public class CartManager {
    private static final String TAG = "CartManager";
    private static final String PREF_NAME = "cart_prefs";
    private static final String KEY_CART_ITEMS = "cart_items";
    
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    public CartManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * Lưu giỏ hàng vào SharedPreferences
     */
    public void saveCart(List<CartItem> cartItems) {
        String json = gson.toJson(cartItems);
        sharedPreferences.edit().putString(KEY_CART_ITEMS, json).apply();
        Log.d(TAG, "Cart saved: " + cartItems.size() + " items");
    }
    
    /**
     * Load giỏ hàng từ SharedPreferences
     */
    public List<CartItem> loadCart() {
        String json = sharedPreferences.getString(KEY_CART_ITEMS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            Type type = new TypeToken<List<CartItem>>(){}.getType();
            List<CartItem> items = gson.fromJson(json, type);
            Log.d(TAG, "Cart loaded: " + (items != null ? items.size() : 0) + " items");
            return items != null ? items : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error loading cart: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public void addToCart(CartItem newItem) {
        List<CartItem> cartItems = loadCart();
        
        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        boolean found = false;
        for (CartItem item : cartItems) {
            if (item.getProduct().get_id().equals(newItem.getProduct().get_id())) {
                // Tăng số lượng nếu đã có
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                found = true;
                break;
            }
        }
        
        // Nếu chưa có thì thêm mới
        if (!found) {
            cartItems.add(newItem);
        }
        
        saveCart(cartItems);
    }
    
    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    public void updateQuantity(String productId, int quantity) {
        List<CartItem> cartItems = loadCart();
        for (CartItem item : cartItems) {
            if (item.getProduct().get_id().equals(productId)) {
                item.setQuantity(quantity);
                break;
            }
        }
        saveCart(cartItems);
    }
    
    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    public void removeFromCart(String productId) {
        List<CartItem> cartItems = loadCart();
        cartItems.removeIf(item -> item.getProduct().get_id().equals(productId));
        saveCart(cartItems);
    }
    
    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void clearCart() {
        sharedPreferences.edit().remove(KEY_CART_ITEMS).apply();
        Log.d(TAG, "Cart cleared");
    }
    
    /**
     * Lấy tổng số lượng sản phẩm trong giỏ hàng
     */
    public int getTotalQuantity() {
        List<CartItem> cartItems = loadCart();
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }
    
    /**
     * Lấy tổng tiền trong giỏ hàng
     */
    public double getTotalPrice() {
        List<CartItem> cartItems = loadCart();
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }
}

