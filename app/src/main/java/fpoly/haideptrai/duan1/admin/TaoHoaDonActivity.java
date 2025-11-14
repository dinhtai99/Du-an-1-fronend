package fpoly.haideptrai.duan1.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.admin.adapters.SanPhamAdapter;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.CustomerListResponse;
import fpoly.haideptrai.duan1.api.models.CustomerResponse;
import fpoly.haideptrai.duan1.api.models.InvoiceItemRequest;
import fpoly.haideptrai.duan1.api.models.InvoiceRequest;
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;
import fpoly.haideptrai.duan1.api.models.ProductListResponse;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import fpoly.haideptrai.duan1.api.services.CustomerService;
import fpoly.haideptrai.duan1.api.services.InvoiceService;
import fpoly.haideptrai.duan1.api.services.ProductService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaoHoaDonActivity extends AppCompatActivity {

    private TextInputEditText edtGiamGia;
    private TextInputLayout tilGiamGia;
    private Spinner spnKhachHang, spnPhuongThucThanhToan;
    private RecyclerView rvSanPhamChon;
    private TextView txtTongTien;
    private MaterialButton btnThemSanPham, btnTaoHoaDon;
    private CustomerService customerService;
    private ProductService productService;
    private InvoiceService invoiceService;
    private List<CustomerResponse> customers = new ArrayList<>();
    private List<CartItem> cartItems = new ArrayList<>();
    private CartAdapter cartAdapter;
    private NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_hoa_don);

        initViews();
        setupSpinners();
        setupRecyclerView();

        customerService = ApiClient.getClient().create(CustomerService.class);
        productService = ApiClient.getClient().create(ProductService.class);
        invoiceService = ApiClient.getClient().create(InvoiceService.class);

        loadCustomers();
        btnThemSanPham.setOnClickListener(v -> showAddProductDialog());
        btnTaoHoaDon.setOnClickListener(v -> handleCreateInvoice());

        edtGiamGia.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotal();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void initViews() {
        spnKhachHang = findViewById(R.id.spnKhachHang);
        spnPhuongThucThanhToan = findViewById(R.id.spnPhuongThucThanhToan);
        edtGiamGia = findViewById(R.id.edtGiamGia);
        tilGiamGia = findViewById(R.id.tilGiamGia);
        rvSanPhamChon = findViewById(R.id.rvSanPhamChon);
        txtTongTien = findViewById(R.id.txtTongTien);
        btnThemSanPham = findViewById(R.id.btnThemSanPham);
        btnTaoHoaDon = findViewById(R.id.btnTaoHoaDon);
    }

    private void setupSpinners() {
        ArrayAdapter<String> customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnKhachHang.setAdapter(customerAdapter);

        String[] paymentMethods = {"Tiền mặt", "Chuyển khoản", "Thẻ"};
        String[] paymentValues = {"cash", "transfer", "card"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPhuongThucThanhToan.setAdapter(paymentAdapter);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter();
        rvSanPhamChon.setLayoutManager(new LinearLayoutManager(this));
        rvSanPhamChon.setAdapter(cartAdapter);
    }

    private void loadCustomers() {
        Call<CustomerListResponse> call = customerService.getCustomers(null, null, true, 1, 100);
        call.enqueue(new Callback<CustomerListResponse>() {
            @Override
            public void onResponse(Call<CustomerListResponse> call, Response<CustomerListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    customers.clear();
                    customers.addAll(response.body().getCustomers());
                    List<String> customerNames = new ArrayList<>();
                    customerNames.add("-- Chọn khách hàng --");
                    for (CustomerResponse c : customers) {
                        customerNames.add(c.getName());
                    }
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spnKhachHang.getAdapter();
                    adapter.clear();
                    adapter.addAll(customerNames);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CustomerListResponse> call, Throwable t) {
                Toast.makeText(TaoHoaDonActivity.this, "Lỗi tải danh sách khách hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddProductDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_chon_san_pham, null);
        TextInputEditText edtTimSanPham = dialogView.findViewById(R.id.edtTimSanPham);
        RecyclerView rvSanPham = dialogView.findViewById(R.id.rvSanPham);
        Button btnChon = dialogView.findViewById(R.id.btnChon);

        List<ProductResponse> products = new ArrayList<>();
        SanPhamAdapter adapter = new SanPhamAdapter();
        adapter.setItems(products);
        rvSanPham.setLayoutManager(new LinearLayoutManager(this));
        rvSanPham.setAdapter(adapter);

        ProductResponse[] selectedProduct = new ProductResponse[1];

        adapter.setOnProductClickListener(new SanPhamAdapter.OnProductClickListener() {
            @Override
            public void onClick(ProductResponse product) {
                selectedProduct[0] = product;
                btnChon.setEnabled(true);
            }

            @Override
            public void onLongClick(ProductResponse product) {
                // Không cần xử lý
            }
        });

        edtTimSanPham.setOnEditorActionListener((v, actionId, event) -> {
            String search = edtTimSanPham.getText().toString().trim();
            searchProducts(search, adapter);
            return true;
        });

        searchProducts("", adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chọn sản phẩm")
                .setView(dialogView)
                .setNegativeButton("Hủy", null)
                .create();

        btnChon.setOnClickListener(v -> {
            if (selectedProduct[0] != null) {
                showQuantityDialog(selectedProduct[0]);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void searchProducts(String search, SanPhamAdapter adapter) {
        Call<ProductListResponse> call = productService.getProducts(search, null, null, null, 1, null, 1, 50);
        call.enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body().getProducts());
                }
            }

            @Override
            public void onFailure(Call<ProductListResponse> call, Throwable t) {
                Toast.makeText(TaoHoaDonActivity.this, "Lỗi tìm kiếm sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuantityDialog(ProductResponse product) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nhap_so_luong, null);
        TextInputEditText edtSoLuong = dialogView.findViewById(R.id.edtSoLuong);
        TextInputEditText edtGia = dialogView.findViewById(R.id.edtGia);
        TextInputEditText edtGiamGiaItem = dialogView.findViewById(R.id.edtGiamGiaItem);

        edtGia.setText(String.valueOf(product.getPrice()));
        edtSoLuong.setText("1");

        new AlertDialog.Builder(this)
                .setTitle("Nhập số lượng: " + product.getName())
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    try {
                        int quantity = Integer.parseInt(edtSoLuong.getText().toString().trim());
                        double price = Double.parseDouble(edtGia.getText().toString().trim());
                        double discount = TextUtils.isEmpty(edtGiamGiaItem.getText().toString().trim()) ? 0 :
                                Double.parseDouble(edtGiamGiaItem.getText().toString().trim());

                        if (quantity <= 0) {
                            Toast.makeText(this, "Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        CartItem item = new CartItem(product, quantity, price, discount);
                        cartItems.add(item);
                        cartAdapter.notifyDataSetChanged();
                        updateTotal();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateTotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getSubtotal();
        }
        String discountStr = edtGiamGia.getText().toString().trim();
        double discount = 0;
        try {
            if (!TextUtils.isEmpty(discountStr)) {
                discount = Double.parseDouble(discountStr);
            }
        } catch (NumberFormatException e) {
            discount = 0;
        }
        double total = subtotal * (1 - discount / 100);
        txtTongTien.setText("Tổng tiền: " + currency.format(total).replace("₫", "đ"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTotal();
    }

    private void handleCreateInvoice() {
        if (spnKhachHang.getSelectedItemPosition() <= 0) {
            Toast.makeText(this, "Vui lòng chọn khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomerResponse customer = customers.get(spnKhachHang.getSelectedItemPosition() - 1);
        String paymentMethod = spnPhuongThucThanhToan.getSelectedItemPosition() == 0 ? "cash" :
                spnPhuongThucThanhToan.getSelectedItemPosition() == 1 ? "transfer" : "card";
        String discountStr = edtGiamGia.getText().toString().trim();
        double discount = 0;
        try {
            if (!TextUtils.isEmpty(discountStr)) {
                discount = Double.parseDouble(discountStr);
            }
        } catch (NumberFormatException e) {
            discount = 0;
        }

        List<InvoiceItemRequest> items = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            InvoiceItemRequest item = new InvoiceItemRequest();
            item.setProduct(cartItem.product.get_id());
            item.setQuantity(cartItem.quantity);
            item.setPrice(cartItem.price);
            item.setDiscount(cartItem.discount);
            items.add(item);
        }

        InvoiceRequest request = new InvoiceRequest();
        request.setCustomer(customer.get_id());
        request.setItems(items);
        request.setDiscount(discount);
        request.setPaymentMethod(paymentMethod);
        request.setNotes("");

        btnTaoHoaDon.setEnabled(false);
        btnTaoHoaDon.setText("Đang tạo...");

        Call<ApiResponse<InvoiceResponse>> call = invoiceService.createInvoice(request);
        call.enqueue(new Callback<ApiResponse<InvoiceResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<InvoiceResponse>> call, Response<ApiResponse<InvoiceResponse>> response) {
                btnTaoHoaDon.setEnabled(true);
                btnTaoHoaDon.setText("Tạo hóa đơn");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(TaoHoaDonActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TaoHoaDonActivity.this, "Tạo hóa đơn thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<InvoiceResponse>> call, Throwable t) {
                btnTaoHoaDon.setEnabled(true);
                btnTaoHoaDon.setText("Tạo hóa đơn");
                Toast.makeText(TaoHoaDonActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class CartItem {
        ProductResponse product;
        int quantity;
        double price;
        double discount;

        CartItem(ProductResponse product, int quantity, double price, double discount) {
            this.product = product;
            this.quantity = quantity;
            this.price = price;
            this.discount = discount;
        }

        double getSubtotal() {
            return price * quantity * (1 - discount / 100);
        }
    }

    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CartItem item = cartItems.get(position);
            holder.txtTen.setText(item.product.getName());
            holder.txtSoLuong.setText("SL: " + item.quantity);
            holder.txtGia.setText(currency.format(item.price).replace("₫", "đ"));
            holder.txtTong.setText(currency.format(item.getSubtotal()).replace("₫", "đ"));

            holder.btnXoa.setOnClickListener(v -> {
                cartItems.remove(position);
                notifyDataSetChanged();
                updateTotal();
            });
        }

        @Override
        public int getItemCount() {
            return cartItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTen, txtSoLuong, txtGia, txtTong;
            Button btnXoa;

            ViewHolder(View itemView) {
                super(itemView);
                txtTen = itemView.findViewById(R.id.txtTen);
                txtSoLuong = itemView.findViewById(R.id.txtSoLuong);
                txtGia = itemView.findViewById(R.id.txtGia);
                txtTong = itemView.findViewById(R.id.txtTong);
                btnXoa = itemView.findViewById(R.id.btnXoa);
            }
        }
    }
}

