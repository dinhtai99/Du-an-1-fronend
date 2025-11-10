package fpoly.haideptrai.duan1;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fpoly.haideptrai.duan1.adapters.SanPhamAdapter;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ProductListResponse;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import fpoly.haideptrai.duan1.api.services.ProductService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DanhSachSanPhamActivity extends AppCompatActivity {

    private static final String TAG = "PRODUCTS";

    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private RecyclerView rvSanPham;
    private SearchView searchView;
    private SanPhamAdapter adapter;
    private ProductService productService;
    private String currentCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_san_pham);

        String categoryName = getIntent() != null ? getIntent().getStringExtra(EXTRA_CATEGORY_NAME) : null;
        currentCategoryId = getIntent() != null ? getIntent().getStringExtra(EXTRA_CATEGORY_ID) : null;

        rvSanPham = findViewById(R.id.rvSanPham);
        searchView = findViewById(R.id.searchViewSanPham);
        adapter = new SanPhamAdapter();
        rvSanPham.setLayoutManager(new LinearLayoutManager(this));
        rvSanPham.setAdapter(adapter);

        if (categoryName != null && !categoryName.isEmpty()) {
            setTitle("Sản phẩm: " + categoryName);
            searchView.setQueryHint("Tìm kiếm trong " + categoryName);
        }

        productService = ApiClient.getClient().create(ProductService.class);
        fetchProducts(null);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // optional live search: call API or leave
                return false;
            }
        });
    }

    private void fetchProducts(String search) {
        Call<ProductListResponse> call = productService.getProducts(search, currentCategoryId, null, null, 1, null, 1, 50);
        call.enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductResponse> list = response.body().getProducts();
                    Log.i(TAG, "Loaded products: " + (list != null ? list.size() : 0));
                    adapter.setItems(list);
                } else {
                    String msg = "Không tải được sản phẩm (" + response.code() + ")";
                    Log.e(TAG, msg);
                    Toast.makeText(DanhSachSanPhamActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductListResponse> call, Throwable t) {
                Log.e(TAG, "API error: " + t.getMessage(), t);
                Toast.makeText(DanhSachSanPhamActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}










