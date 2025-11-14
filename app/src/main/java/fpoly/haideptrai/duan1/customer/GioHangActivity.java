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
    private List<CartItem> cartItems = new ArrayList<>();
    private NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gio_hang);

        initViews();
        setupBottomNavigation();
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
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ThanhToanActivity.class);
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
            } else if (itemId == R.id.nav_discount) {
                Toast.makeText(this, "Khuyến mãi", Toast.LENGTH_SHORT).show();
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
            updateTotal();
        });

        cartAdapter.setOnRemoveItemListener(item -> {
            cartItems.remove(item);
            cartAdapter.notifyDataSetChanged();
            updateTotal();
        });
    }

    private void loadCartItems() {
        // TODO: Load from SharedPreferences or API
        // For now, using empty list
        cartItems.clear();
        cartAdapter.notifyDataSetChanged();
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        txtTongTien.setText(currency.format(total).replace("₫", "₫"));
    }
}

