package fpoly.haideptrai.duan1.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.models.CustomerResponse;

public class KhachHangAdapter extends RecyclerView.Adapter<KhachHangAdapter.ViewHolder> {

    private final List<CustomerResponse> items = new ArrayList<>();
    private OnCustomerClickListener onCustomerClickListener;

    public void setItems(List<CustomerResponse> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_khach_hang, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerResponse c = items.get(position);
        holder.txtTenKhachHang.setText(c.getName() != null ? c.getName() : "");
        holder.txtHangKhachHang.setText(c.getType() != null ? c.getType() : "");
        
        // Hiển thị badge CLIENT nếu có type
        if (c.getType() != null && !c.getType().isEmpty()) {
            holder.badgeClient.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.badgeClient.setVisibility(android.view.View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onCustomerClickListener != null) {
                onCustomerClickListener.onClick(c);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onCustomerClickListener != null) {
                onCustomerClickListener.onLongClick(c);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnCustomerClickListener(OnCustomerClickListener listener) {
        this.onCustomerClickListener = listener;
    }

    public interface OnCustomerClickListener {
        void onClick(CustomerResponse customer);
        void onLongClick(CustomerResponse customer);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatarKhachHang;
        TextView txtTenKhachHang;
        TextView txtHangKhachHang;
        TextView badgeClient;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarKhachHang = itemView.findViewById(R.id.imgAvatarKhachHang);
            txtTenKhachHang = itemView.findViewById(R.id.txtTenKhachHang);
            txtHangKhachHang = itemView.findViewById(R.id.txtHangKhachHang);
            badgeClient = itemView.findViewById(R.id.badgeClient);
        }
    }
}
