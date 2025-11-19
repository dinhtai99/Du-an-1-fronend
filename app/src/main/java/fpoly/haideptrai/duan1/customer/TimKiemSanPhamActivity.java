package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ProductListResponse;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import fpoly.haideptrai.duan1.api.services.ProductService;
import fpoly.haideptrai.duan1.customer.adapters.ProductHomeAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimKiemSanPhamActivity extends AppCompatActivity {

    private TextInputLayout tilSearch;
    private TextInputEditText edtSearch;
    private ImageButton btnBack, btnClear;
    private RecyclerView rvKetQua;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    
    private ProductService productService;
    private ProductHomeAdapter productAdapter;
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tim_kiem_san_pham);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSearchListener();
        
        productService = ApiClient.getClient().create(ProductService.class);
        
        // Focus vào search box khi mở màn hình
        edtSearch.requestFocus();
    }

    private void initViews() {
        tilSearch = findViewById(R.id.tilSearch);
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);
        btnClear = findViewById(R.id.btnClear);
        rvKetQua = findViewById(R.id.rvKetQua);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        rvKetQua.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductHomeAdapter();
        productAdapter.setContext(this);
        rvKetQua.setAdapter(productAdapter);
        
        productAdapter.setOnProductClickListener(product -> {
            android.content.Intent intent = new android.content.Intent(this, ChiTietSanPhamActivity.class);
            intent.putExtra("product_id", product.get_id());
            startActivity(intent);
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnClear.setOnClickListener(v -> {
            edtSearch.setText("");
            edtSearch.requestFocus();
            showEmptyState();
        });
    }

    private void setupSearchListener() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hiển thị/ẩn nút clear
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                // Hủy search request trước đó nếu có
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Nếu rỗng, hiển thị empty state
                if (s.length() == 0) {
                    showEmptyState();
                    return;
                }
                
                // Delay 500ms trước khi search (debounce)
                searchRunnable = () -> performSearch(s.toString());
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Xử lý khi nhấn nút search trên bàn phím
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String query = edtSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            showEmptyState();
            return;
        }
        
        android.util.Log.d("Search", "Searching for: " + query);
        
        // Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);
        rvKetQua.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        
        // Gọi API tìm kiếm
        Call<ProductListResponse> call = productService.getProducts(
            query,  // search
            null,   // category
            null,   // minPrice
            null,   // maxPrice
            1,      // status (active)
            null,   // lowStock
            1,      // page
            50      // limit
        );
        
        call.enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductResponse> products = response.body().getProducts();
                    
                    if (products != null && !products.isEmpty()) {
                        productAdapter.setItems(products);
                        rvKetQua.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                        
                        android.util.Log.d("Search", "Found " + products.size() + " products");
                    } else {
                        showNoResults();
                        android.util.Log.d("Search", "No results found");
                    }
                } else {
                    showNoResults();
                    android.util.Log.e("Search", "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProductListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showNoResults();
                
                android.util.Log.e("Search", "Search error: " + t.getMessage(), t);
                Toast.makeText(TimKiemSanPhamActivity.this, 
                    "Lỗi tìm kiếm: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        rvKetQua.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        
        // Cập nhật text empty state
        TextView txtEmpty = layoutEmpty.findViewById(R.id.txtEmptyMessage);
        if (txtEmpty != null) {
            txtEmpty.setText("Nhập từ khóa để tìm kiếm");
        }
    }

    private void showNoResults() {
        rvKetQua.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        
        // Cập nhật text empty state
        TextView txtEmpty = layoutEmpty.findViewById(R.id.txtEmptyMessage);
        if (txtEmpty != null) {
            txtEmpty.setText("Không tìm thấy sản phẩm nào");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy search handler để tránh memory leak
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}

