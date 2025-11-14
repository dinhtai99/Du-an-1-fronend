package fpoly.haideptrai.duan1.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;

public class HoaDonAdapter extends RecyclerView.Adapter<HoaDonAdapter.ViewHolder> {

    private final List<InvoiceResponse> items = new ArrayList<>();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public void setItems(List<InvoiceResponse> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hoa_don, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvoiceResponse inv = items.get(position);
        holder.txtMaHoaDon.setText(inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : inv.get_id());
        holder.txtTrangThaiHoaDon.setText(statusLabel(inv.getStatus()));
        String tenKh = inv.getCustomer() != null ? inv.getCustomer().getName() : "";
        holder.txtTenKhachHangHoaDon.setText("Khách hàng: " + tenKh);
        holder.txtTongTienHoaDon.setText("Tổng tiền: " + formatAmount(inv.getTotal()));
    }

    private String formatAmount(Double amount) {
        if (amount == null) return "";
        return currency.format(amount).replace("₫", "đ");
    }

    private String statusLabel(String s) {
        if (s == null) return "";
        switch (s) {
            case "completed": return "Đã thanh toán";
            case "pending": return "Đang xử lý";
            case "cancelled": return "Đã hủy";
            default: return s;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMaHoaDon;
        TextView txtTrangThaiHoaDon;
        TextView txtTenKhachHangHoaDon;
        TextView txtTongTienHoaDon;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMaHoaDon = itemView.findViewById(R.id.txtMaHoaDon);
            txtTrangThaiHoaDon = itemView.findViewById(R.id.txtTrangThaiHoaDon);
            txtTenKhachHangHoaDon = itemView.findViewById(R.id.txtTenKhachHangHoaDon);
            txtTongTienHoaDon = itemView.findViewById(R.id.txtTongTienHoaDon);
        }
    }
}

