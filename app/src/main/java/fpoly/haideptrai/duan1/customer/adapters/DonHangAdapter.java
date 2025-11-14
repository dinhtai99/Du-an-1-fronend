package fpoly.haideptrai.duan1.customer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;

public class DonHangAdapter extends RecyclerView.Adapter<DonHangAdapter.ViewHolder> {

    private final List<InvoiceResponse> items = new ArrayList<>();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OnOrderClickListener onOrderClickListener;

    public void setItems(List<InvoiceResponse> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_don_hang_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvoiceResponse invoice = items.get(position);
        holder.txtMaDonHang.setText(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : invoice.get_id());
        holder.txtTrangThai.setText(getStatusLabel(invoice.getStatus()));
        holder.txtTongTien.setText(formatPrice(invoice.getTotal()));
        
        if (invoice.getCustomer() != null) {
            holder.txtKhachHang.setText("Khách hàng: " + invoice.getCustomer().getName());
        }
        
        holder.txtPhuongThucThanhToan.setText(getPaymentMethodLabel(invoice.getPaymentMethod()));

        // Load product image if available
        if (invoice.getItems() != null && !invoice.getItems().isEmpty() && 
            invoice.getItems().get(0).getProduct() != null) {
            String imageUrl = invoice.getItems().get(0).getProduct().getImage();
            if (imageUrl != null && !imageUrl.trim().isEmpty() && !imageUrl.contains("example.com")) {
                Glide.with(holder.imgSanPham.getContext())
                        .load(imageUrl)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(holder.imgSanPham);
            } else {
                Glide.with(holder.imgSanPham.getContext())
                        .load(R.mipmap.ic_launcher)
                        .into(holder.imgSanPham);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (onOrderClickListener != null) {
                onOrderClickListener.onClick(invoice);
            }
        });
    }

    private String getStatusLabel(String status) {
        if (status == null) return "";
        switch (status) {
            case "completed": return "Đã giao";
            case "pending": return "Đang xử lý";
            case "shipping": return "Đang giao";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    private String getPaymentMethodLabel(String method) {
        if (method == null) return "";
        switch (method) {
            case "cash": return "Trực tiếp khi nhận hàng";
            case "transfer": return "Chuyển khoản";
            case "card": return "Thẻ";
            case "visa": return "VISA";
            case "mastercard": return "Mastercard";
            default: return method;
        }
    }

    private String formatPrice(Double price) {
        if (price == null) return "0 vnđ";
        return currency.format(price).replace("₫", "vnđ");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.onOrderClickListener = listener;
    }

    public interface OnOrderClickListener {
        void onClick(InvoiceResponse order);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSanPham;
        TextView txtMaDonHang, txtTrangThai, txtKhachHang, txtTongTien, txtPhuongThucThanhToan;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSanPham = itemView.findViewById(R.id.imgSanPham);
            txtMaDonHang = itemView.findViewById(R.id.txtMaDonHang);
            txtTrangThai = itemView.findViewById(R.id.txtTrangThai);
            txtKhachHang = itemView.findViewById(R.id.txtKhachHang);
            txtTongTien = itemView.findViewById(R.id.txtTongTien);
            txtPhuongThucThanhToan = itemView.findViewById(R.id.txtPhuongThucThanhToan);
        }
    }
}

