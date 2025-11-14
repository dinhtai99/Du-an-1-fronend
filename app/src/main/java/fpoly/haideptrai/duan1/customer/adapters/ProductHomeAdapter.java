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
import fpoly.haideptrai.duan1.api.models.ProductResponse;

public class ProductHomeAdapter extends RecyclerView.Adapter<ProductHomeAdapter.ViewHolder> {

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_san_pham_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductResponse product = items.get(position);
        holder.txtTenSanPham.setText(product.getName());
        holder.txtGiaSanPham.setText(formatPrice(product.getPrice()));

        // Show discount badge if applicable (example: if price < importPrice)
        if (product.getImportPrice() != null && product.getPrice() != null && 
            product.getPrice() < product.getImportPrice()) {
            double discount = ((product.getImportPrice() - product.getPrice()) / product.getImportPrice()) * 100;
            holder.badgeGiamGia.setText((int)discount + "% OFF");
            holder.badgeGiamGia.setVisibility(View.VISIBLE);
        } else {
            holder.badgeGiamGia.setVisibility(View.GONE);
        }

        // Load image
        String imageUrl = product.getImage();
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

        holder.itemView.setOnClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onClick(product);
            }
        });
    }

    private String formatPrice(Double price) {
        if (price == null) return "";
        return currency.format(price).replace("₫", "₫");
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
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSanPham;
        TextView txtTenSanPham, txtGiaSanPham, badgeGiamGia, txtXemChiTiet;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSanPham = itemView.findViewById(R.id.imgSanPham);
            txtTenSanPham = itemView.findViewById(R.id.txtTenSanPham);
            txtGiaSanPham = itemView.findViewById(R.id.txtGiaSanPham);
            badgeGiamGia = itemView.findViewById(R.id.badgeGiamGia);
            txtXemChiTiet = itemView.findViewById(R.id.txtXemChiTiet);
        }
    }
}

