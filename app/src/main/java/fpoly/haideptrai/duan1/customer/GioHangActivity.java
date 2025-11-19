package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.customer.adapters.CartAdapter;
import fpoly.haideptrai.duan1.customer.models.CartItem;
import fpoly.haideptrai.duan1.utils.CartManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GioHangActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvGioHang;
    private TextView txtTongTien;
    private MaterialButton btnThanhToan;
    private BottomNavigationView bottomNavigation;
    
    private CartAdapter cartAdapter;
    private CartManager cartManager;
    private List<CartItem> cartItems = new ArrayList<>();
    private NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gio_hang);

        initViews();
        setupBottomNavigation();
        cartManager = new CartManager(this);
        setupRecyclerView();
        loadCartItems();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvGioHang = findViewById(R.id.rvGioHang);
        txtTongTien = findViewById(R.id.txtTongTien);
        btnThanhToan = findViewById(R.id.btnThanhToan);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnBack.setOnClickListener(v -> finish());
        btnThanhToan.setOnClickListener(v -> {
            // Lấy danh sách sản phẩm đã chọn
            List<CartItem> selectedItems = getSelectedItems();
            
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Lưu giỏ hàng trước khi chuyển màn hình
            cartManager.saveCart(cartItems);
            
            // Gửi danh sách sản phẩm đã chọn sang màn hình thanh toán
            Intent intent = new Intent(this, ThanhToanActivity.class);
            // Lưu danh sách đã chọn vào SharedPreferences tạm thời
            android.content.SharedPreferences prefs = getSharedPreferences("temp_cart", MODE_PRIVATE);
            String selectedItemsJson = new com.google.gson.Gson().toJson(selectedItems);
            prefs.edit().putString("selected_items", selectedItemsJson).apply();
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_support) {
                Intent intent = new Intent(this, ChamSocKhachHangActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_discount) {
                Intent intent = new Intent(this, QuanLyVoucherActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_cart) {
                // Already on cart
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ThongTinCaNhanActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
        bottomNavigation.setSelectedItemId(R.id.nav_cart);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItems);
        rvGioHang.setLayoutManager(new LinearLayoutManager(this));
        rvGioHang.setAdapter(cartAdapter);

        cartAdapter.setOnCartItemChangeListener(() -> {
            // Lưu giỏ hàng khi có thay đổi
            cartManager.saveCart(cartItems);
            cartAdapter.notifyDataSetChanged();
            updateTotal();
        });

        cartAdapter.setOnRemoveItemListener(item -> {
            cartItems.remove(item);
            cartManager.saveCart(cartItems);
            cartAdapter.notifyDataSetChanged();
            updateTotal();
        });

        cartAdapter.setOnSelectionChangeListener(() -> {
            // Lưu giỏ hàng khi có thay đổi selection
            cartManager.saveCart(cartItems);
            updateTotal();
        });
    }

    private void loadCartItems() {
        List<CartItem> loadedItems = cartManager.loadCart();
        android.util.Log.d("GioHangActivity", "Loaded " + loadedItems.size() + " items from cart");
        cartItems.clear();
        cartItems.addAll(loadedItems);
        android.util.Log.d("GioHangActivity", "Cart items list now has " + cartItems.size() + " items");
        cartAdapter.notifyDataSetChanged();
        updateTotal();
        
        // Hiển thị empty state nếu giỏ hàng trống
        if (cartItems.isEmpty()) {
            android.util.Log.d("GioHangActivity", "Cart is empty");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload cart khi quay lại màn hình để đảm bảo hiển thị đúng
        loadCartItems();
    }

    private void updateTotal() {
        // Chỉ tính tổng các sản phẩm đã chọn
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        txtTongTien.setText(currency.format(total).replace("₫", "₫"));
    }

    /**
     * Lấy danh sách các sản phẩm đã được chọn
     */
    private List<CartItem> getSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }
}

