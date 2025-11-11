package fpoly.haideptrai.duan1.adapters;

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
import fpoly.haideptrai.duan1.api.models.ProductResponse;

public class SanPhamAdapter extends RecyclerView.Adapter<SanPhamAdapter.ViewHolder> {

    private final List<ProductResponse> items = new ArrayList<>();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OnProductClickListener onProductClickListener;

    public void setItems(List<ProductResponse> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_san_pham, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductResponse p = items.get(position);
        holder.txtTenSanPham.setText(p.getName());
        holder.txtGiaBan.setText(formatPrice(p.getPrice()));
        holder.txtTonKho.setText("Tồn kho: " + (p.getStock() != null ? p.getStock() : 0));
        holder.badgeCanhBao.setVisibility(p.isLowStockWarning() ? View.VISIBLE : View.GONE);
        String url = p.getImage();
        Glide.with(holder.imgSanPham.getContext())
                .load(url)
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgSanPham);

        holder.itemView.setOnClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onClick(p);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onLongClick(p);
                return true;
            }
            return false;
        });
    }

    private String formatPrice(Double price) {
        if (price == null) return "";
        return currency.format(price).replace("₫", "đ");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.onProductClickListener = listener;
    }

    public interface OnProductClickListener {
        void onClick(ProductResponse product);
        void onLongClick(ProductResponse product);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSanPham;
        TextView txtTenSanPham;
        TextView txtGiaBan;
        TextView txtTonKho;
        TextView badgeCanhBao;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSanPham = itemView.findViewById(R.id.imgSanPham);
            txtTenSanPham = itemView.findViewById(R.id.txtTenSanPham);
            txtGiaBan = itemView.findViewById(R.id.txtGiaBan);
            txtTonKho = itemView.findViewById(R.id.txtTonKho);
            badgeCanhBao = itemView.findViewById(R.id.badgeCanhBao);
        }
    }
}
