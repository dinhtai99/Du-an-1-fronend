package fpoly.haideptrai.duan1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.adapters.NhanVienAdapter;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.UserListResponse;
import fpoly.haideptrai.duan1.api.models.UserResponse;
import fpoly.haideptrai.duan1.api.services.UserService;
import fpoly.haideptrai.duan1.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DanhSachNhanVienActivity extends AppCompatActivity {

    private static final String TAG = "USERS";
    private RecyclerView rvNhanVien;
    private SearchView searchView;
    private NhanVienAdapter adapter;
    private UserService userService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_nhan_vien);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Chỉ Admin mới có quyền truy cập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvNhanVien = findViewById(R.id.rvNhanVien);
        searchView = findViewById(R.id.searchViewNhanVien);
        adapter = new NhanVienAdapter();
        rvNhanVien.setLayoutManager(new LinearLayoutManager(this));
        rvNhanVien.setAdapter(adapter);

        userService = ApiClient.getClient().create(UserService.class);

        adapter.setOnUserClickListener(new NhanVienAdapter.OnUserClickListener() {
            @Override
            public void onClick(UserResponse user) {
                // Mở màn hình chi tiết/sửa
                Intent intent = new Intent(DanhSachNhanVienActivity.this, ThemSuaNhanVienActivity.class);
                intent.putExtra("user_id", user.get_id());
                startActivity(intent);
            }

            @Override
            public void onLongClick(UserResponse user) {
                showDeleteDialog(user);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fabThemNhanVien);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(DanhSachNhanVienActivity.this, ThemSuaNhanVienActivity.class);
            startActivity(intent);
        });

        fetchUsers(null);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    fetchUsers(null);
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUsers(null);
    }

    private void fetchUsers(String search) {
        Log.d(TAG, "Fetching users, search: " + search);
        Call<UserListResponse> call = userService.getUsers(search, null, 1, 50);
        call.enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                Log.d(TAG, "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    UserListResponse body = response.body();
                    List<UserResponse> list = body.getUsers();
                    Log.i(TAG, "Response body - total: " + body.getTotal() + ", users list size: " + (list != null ? list.size() : 0));
                    
                    if (list != null && !list.isEmpty()) {
                        Log.i(TAG, "Loaded " + list.size() + " users");
                        for (UserResponse user : list) {
                            Log.d(TAG, "User: " + user.getHoTen() + " (" + user.getTenDangNhap() + ")");
                        }
                        adapter.setItems(list);
                    } else {
                        Log.w(TAG, "Users list is null or empty");
                        adapter.setItems(new ArrayList<>());
                        Toast.makeText(DanhSachNhanVienActivity.this, "Không có nhân viên nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String msg = "Không tải được nhân viên (" + response.code() + ")";
                    Log.e(TAG, msg);
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    if (response.code() == 401) {
                        msg = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!";
                    }
                    Toast.makeText(DanhSachNhanVienActivity.this, msg, Toast.LENGTH_SHORT).show();
                    adapter.setItems(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                Log.e(TAG, "API error: " + t.getMessage(), t);
                Toast.makeText(DanhSachNhanVienActivity.this, "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                adapter.setItems(new ArrayList<>());
            }
        });
    }

    private void showDeleteDialog(UserResponse user) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa nhân viên")
                .setMessage("Bạn có chắc chắn muốn xóa nhân viên \"" + user.getHoTen() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteUser(user.get_id()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteUser(String userId) {
        Call<ApiResponse<Void>> call = userService.deleteUser(userId);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DanhSachNhanVienActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    fetchUsers(null);
                } else {
                    Toast.makeText(DanhSachNhanVienActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(DanhSachNhanVienActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}










