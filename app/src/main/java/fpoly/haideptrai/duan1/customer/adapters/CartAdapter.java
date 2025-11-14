package fpoly.haideptrai.duan1.customer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.customer.models.CartItem;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final List<CartItem> items;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OnCartItemChangeListener onCartItemChangeListener;
    private OnRemoveItemListener onRemoveItemListener;

    public CartAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.txtTenSanPham.setText(item.getProduct().getName());
        holder.txtGiaSanPham.setText(formatPrice(item.getPrice()));
        holder.txtSoLuong.setText(String.valueOf(item.getQuantity()));

        // Load image
        String imageUrl = item.getProduct().getImage();
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

        holder.btnTang.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            holder.txtSoLuong.setText(String.valueOf(item.getQuantity()));
            if (onCartItemChangeListener != null) {
                onCartItemChangeListener.onChange();
            }
        });

        holder.btnGiam.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                holder.txtSoLuong.setText(String.valueOf(item.getQuantity()));
                if (onCartItemChangeListener != null) {
                    onCartItemChangeListener.onChange();
                }
            }
        });

        holder.btnXoa.setOnClickListener(v -> {
            if (onRemoveItemListener != null) {
                onRemoveItemListener.onRemove(item);
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

    public void setOnCartItemChangeListener(OnCartItemChangeListener listener) {
        this.onCartItemChangeListener = listener;
    }

    public void setOnRemoveItemListener(OnRemoveItemListener listener) {
        this.onRemoveItemListener = listener;
    }

    public interface OnCartItemChangeListener {
        void onChange();
    }

    public interface OnRemoveItemListener {
        void onRemove(CartItem item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSanPham;
        TextView txtTenSanPham, txtGiaSanPham, txtSoLuong;
        Button btnTang, btnGiam;
        View btnXoa;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSanPham = itemView.findViewById(R.id.imgSanPham);
            txtTenSanPham = itemView.findViewById(R.id.txtTenSanPham);
            txtGiaSanPham = itemView.findViewById(R.id.txtGiaSanPham);
            txtSoLuong = itemView.findViewById(R.id.txtSoLuong);
            btnTang = itemView.findViewById(R.id.btnTang);
            btnGiam = itemView.findViewById(R.id.btnGiam);
            btnXoa = itemView.findViewById(R.id.btnXoa);
        }
    }
}

