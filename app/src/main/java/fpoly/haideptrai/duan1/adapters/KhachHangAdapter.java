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
        holder.txtTenKhachHang.setText(c.getName());
        holder.txtSoDienThoaiKhachHang.setText(c.getPhone() != null ? c.getPhone() : "");
        holder.txtHangKhachHang.setText(c.getType() != null ? c.getType() : "");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatarKhachHang;
        TextView txtTenKhachHang;
        TextView txtSoDienThoaiKhachHang;
        TextView txtHangKhachHang;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarKhachHang = itemView.findViewById(R.id.imgAvatarKhachHang);
            txtTenKhachHang = itemView.findViewById(R.id.txtTenKhachHang);
            txtSoDienThoaiKhachHang = itemView.findViewById(R.id.txtSoDienThoaiKhachHang);
            txtHangKhachHang = itemView.findViewById(R.id.txtHangKhachHang);
        }
    }
}
