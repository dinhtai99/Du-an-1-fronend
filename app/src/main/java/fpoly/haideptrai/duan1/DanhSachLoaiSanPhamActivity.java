package fpoly.haideptrai.duan1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fpoly.haideptrai.duan1.adapters.LoaiSanPhamAdapter;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.CategoryResponse;
import fpoly.haideptrai.duan1.api.services.CategoryService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DanhSachLoaiSanPhamActivity extends AppCompatActivity {

    private static final String TAG = "CATEGORIES";

    private RecyclerView rvLoaiSanPham;
    private SearchView searchView;
    private LoaiSanPhamAdapter adapter;
    private CategoryService categoryService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_loai_san_pham);

        rvLoaiSanPham = findViewById(R.id.rvLoaiSanPham);
        searchView = findViewById(R.id.searchViewLoaiSanPham);
        adapter = new LoaiSanPhamAdapter();
        adapter.setOnCategoryClickListener(category -> {
            Intent intent = new Intent(DanhSachLoaiSanPhamActivity.this, DanhSachSanPhamActivity.class);
            intent.putExtra(DanhSachSanPhamActivity.EXTRA_CATEGORY_ID, category.get_id());
            intent.putExtra(DanhSachSanPhamActivity.EXTRA_CATEGORY_NAME, category.getName());
            startActivity(intent);
        });
        rvLoaiSanPham.setLayoutManager(new LinearLayoutManager(this));
        rvLoaiSanPham.setAdapter(adapter);

        categoryService = ApiClient.getClient().create(CategoryService.class);
        fetchCategories();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    private void fetchCategories() {
        Call<List<CategoryResponse>> call = categoryService.getAllActive();
        call.enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CategoryResponse> list = response.body();
                    Log.i(TAG, "Loaded categories: " + list.size());
                    adapter.setItems(list);
                } else {
                    String msg = "Không tải được loại sản phẩm (" + response.code() + ")";
                    Log.e(TAG, msg);
                    Toast.makeText(DanhSachLoaiSanPhamActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                Log.e(TAG, "API error: " + t.getMessage(), t);
                Toast.makeText(DanhSachLoaiSanPhamActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}










