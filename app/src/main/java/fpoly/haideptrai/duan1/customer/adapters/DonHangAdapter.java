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
import android.graphics.Color;

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
        
        // Format mã đơn hàng
        String invoiceNumber = invoice.getInvoiceNumber();
        if (invoiceNumber != null && !invoiceNumber.isEmpty()) {
            holder.txtMaDonHang.setText(invoiceNumber);
        } else if (invoice.get_id() != null && !invoice.get_id().isEmpty()) {
            // Fallback: lấy 8 ký tự cuối của _id
            String id = invoice.get_id();
            holder.txtMaDonHang.setText("ĐH" + id.substring(Math.max(0, id.length() - 8)));
        } else {
            holder.txtMaDonHang.setText("ĐH");
        }
        
        // Set status với màu badge
        String status = invoice.getStatus();
        String statusLabel = getStatusLabel(status);
        holder.txtTrangThai.setText(statusLabel);
        setStatusBadgeColor(holder.txtTrangThai, status);
        
        // Tổng tiền
        holder.txtTongTien.setText("Tổng tiền: " + formatPrice(invoice.getTotal()));
        
        // Số lượng sản phẩm
        int itemCount = 0;
        if (invoice.getItems() != null) {
            for (InvoiceResponse.Item item : invoice.getItems()) {
                if (item.getQuantity() != null) {
                    itemCount += item.getQuantity();
                }
            }
        }
        holder.txtSoLuongSanPham.setText(itemCount + " sản phẩm");
        
        // Phương thức thanh toán
        String paymentMethod = invoice.getPaymentMethod();
        String paymentLabel = getPaymentMethodLabel(paymentMethod);
        holder.txtPhuongThucThanhToan.setText("Thanh toán: " + paymentLabel);
        
        // Ngày tạo đơn
        String createdAt = invoice.getCreatedAt();
        if (createdAt != null && !createdAt.isEmpty()) {
            String formattedDate = formatDate(createdAt);
            holder.txtNgayTao.setText(formattedDate);
        } else {
            holder.txtNgayTao.setText("");
        }

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
        if (method == null) return "Chưa xác định";
        switch (method.toLowerCase()) {
            case "cod":
            case "cash": 
                return "Tiền mặt (COD)";
            case "zalopay": 
                return "ZaloPay";
            case "momo": 
                return "MoMo";
            case "transfer": 
                return "Chuyển khoản";
            case "card": 
                return "Thẻ";
            case "visa": 
                return "VISA";
            case "mastercard": 
                return "Mastercard";
            default: 
                return method;
        }
    }
    
    private String formatDate(String dateString) {
        try {
            // Parse ISO 8601 format: "2025-11-17T15:41:40.507Z"
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            
            // Remove milliseconds and timezone if present
            String cleanDate = dateString.split("\\.")[0];
            if (cleanDate.contains("Z")) {
                cleanDate = cleanDate.replace("Z", "");
            }
            
            java.util.Date date = inputFormat.parse(cleanDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            // Fallback: return original string
            return dateString;
        }
    }

    private String formatPrice(Double price) {
        if (price == null) return "0 vnd";
        return currency.format(price).replace("₫", "vnd");
    }

    private void setStatusBadgeColor(TextView textView, String status) {
        int colorRes;
        if (status == null) {
            colorRes = R.color.text_secondary;
        } else if ("completed".equals(status) || "delivered".equals(status)) {
            colorRes = R.color.green;
        } else if ("shipping".equals(status) || "processing".equals(status) || "pending".equals(status)) {
            colorRes = R.color.orange;
        } else if ("cancelled".equals(status)) {
            colorRes = R.color.red;
        } else {
            colorRes = R.color.text_secondary;
        }
        textView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            textView.getContext().getResources().getColor(colorRes, null)
        ));
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
        TextView txtMaDonHang, txtTrangThai, txtTongTien, txtPhuongThucThanhToan, txtSoLuongSanPham, txtNgayTao;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSanPham = itemView.findViewById(R.id.imgSanPham);
            txtMaDonHang = itemView.findViewById(R.id.txtMaDonHang);
            txtTrangThai = itemView.findViewById(R.id.txtTrangThai);
            txtTongTien = itemView.findViewById(R.id.txtTongTien);
            txtPhuongThucThanhToan = itemView.findViewById(R.id.txtPhuongThucThanhToan);
            txtSoLuongSanPham = itemView.findViewById(R.id.txtSoLuongSanPham);
            txtNgayTao = itemView.findViewById(R.id.txtNgayTao);
        }
    }
}

