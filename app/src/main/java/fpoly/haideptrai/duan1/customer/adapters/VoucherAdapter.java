package fpoly.haideptrai.duan1.customer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.customer.models.Voucher;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    private final List<Voucher> items;
    private OnVoucherClickListener listener;

    public interface OnVoucherClickListener {
        void onVoucherClick(int position);
    }

    public VoucherAdapter(List<Voucher> items) {
        this.items = items;
    }

    public void setOnVoucherClickListener(OnVoucherClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Voucher voucher = items.get(position);
        holder.txtNgay.setText(voucher.getNgay());
        holder.txtMucGiamGia.setText(voucher.getMucGiamGia());
        holder.txtDieuKien.setText(voucher.getDieuKien());
        holder.txtSoLuong.setText("Số lượng: " + voucher.getSoLuong());
        holder.txtTrangThai.setText("Trạng thái: " + voucher.getTrangThai());

        // Kiểm tra trạng thái voucher
        boolean isActive = "Hoạt động".equals(voucher.getTrangThai());

        if (isActive) {
            // Voucher hoạt động - sáng lên
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setEnabled(true);
            holder.txtMucGiamGia.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black, null));
            holder.txtTrangThai.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.primary_blue, null));
        } else {
            // Voucher không hoạt động - mờ đi
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setEnabled(false);
            holder.txtMucGiamGia.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary, null));
            holder.txtTrangThai.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary, null));
        }

        // Thêm click listener cho item (chỉ cho voucher hoạt động)
        holder.itemView.setOnClickListener(v -> {
            if (isActive && listener != null) {
                listener.onVoucherClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNgay, txtMucGiamGia, txtDieuKien, txtSoLuong, txtTrangThai;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNgay = itemView.findViewById(R.id.txtNgay);
            txtMucGiamGia = itemView.findViewById(R.id.txtMucGiamGia);
            txtDieuKien = itemView.findViewById(R.id.txtDieuKien);
            txtSoLuong = itemView.findViewById(R.id.txtSoLuong);
            txtTrangThai = itemView.findViewById(R.id.txtTrangThai);
        }
    }
}

