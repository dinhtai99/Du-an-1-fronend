package fpoly.haideptrai.duan1.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.models.UserResponse;

public class NhanVienAdapter extends RecyclerView.Adapter<NhanVienAdapter.ViewHolder> {

    private final List<UserResponse> items = new ArrayList<>();
    private OnUserClickListener onUserClickListener;

    public void setItems(List<UserResponse> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nhan_vien, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse user = items.get(position);
        holder.txtTenNhanVien.setText(user.getHoTen() != null ? user.getHoTen() : "");
        holder.txtSoDienThoaiNhanVien.setText(user.getSoDienThoai() != null ? user.getSoDienThoai() : "");
        holder.txtVaiTroNhanVien.setText(user.getVaiTro() != null ? (user.getVaiTro().equals("admin") ? "Quản trị viên" : "Nhân viên") : "");
        
        if (user.getAnhDaiDien() != null && !user.getAnhDaiDien().isEmpty()) {
            Glide.with(holder.imgAvatarNhanVien.getContext())
                    .load(user.getAnhDaiDien())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.imgAvatarNhanVien);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onClick(user);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onLongClick(user);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    public interface OnUserClickListener {
        void onClick(UserResponse user);
        void onLongClick(UserResponse user);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatarNhanVien;
        TextView txtTenNhanVien;
        TextView txtSoDienThoaiNhanVien;
        TextView txtVaiTroNhanVien;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarNhanVien = itemView.findViewById(R.id.imgAvatarNhanVien);
            txtTenNhanVien = itemView.findViewById(R.id.txtTenNhanVien);
            txtSoDienThoaiNhanVien = itemView.findViewById(R.id.txtSoDienThoaiNhanVien);
            txtVaiTroNhanVien = itemView.findViewById(R.id.txtVaiTroNhanVien);
        }
    }
}
