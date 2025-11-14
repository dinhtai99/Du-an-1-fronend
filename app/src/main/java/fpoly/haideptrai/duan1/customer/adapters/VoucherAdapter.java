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

    public VoucherAdapter(List<Voucher> items) {
        this.items = items;
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

