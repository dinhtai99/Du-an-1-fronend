package fpoly.haideptrai.duan1.customer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import fpoly.haideptrai.duan1.utils.FavoriteManager;

public class ProductHomeAdapter extends RecyclerView.Adapter<ProductHomeAdapter.ViewHolder> {

    private final List<ProductResponse> items = new ArrayList<>();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OnProductClickListener onProductClickListener;
    private FavoriteManager favoriteManager;
    private Context context;

    public void setItems(List<ProductResponse> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }
    
    public void setContext(Context context) {
        this.context = context;
        if (context != null) {
            this.favoriteManager = FavoriteManager.getInstance(context);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
            favoriteManager = FavoriteManager.getInstance(context);
        }
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

        // Xử lý icon yêu thích
        if (favoriteManager == null && context != null) {
            favoriteManager = FavoriteManager.getInstance(context);
        }
        
        boolean isFavorite = favoriteManager != null && favoriteManager.isFavorite(product.get_id());
        
        // Set icon và màu
        if (isFavorite) {
            holder.btnYeuThich.setImageResource(R.drawable.ic_heart_filled);
            holder.btnYeuThich.setColorFilter(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.btnYeuThich.setImageResource(R.drawable.ic_heart_outline);
            holder.btnYeuThich.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
        }
        
        holder.btnYeuThich.setOnClickListener(v -> {
            if (favoriteManager != null) {
                // Optimistic update: update UI immediately
                boolean currentState = favoriteManager.isFavorite(product.get_id());
                boolean newFavoriteState = !currentState;
                
                // Update icon và màu immediately (optimistic)
                if (newFavoriteState) {
                    holder.btnYeuThich.setImageResource(R.drawable.ic_heart_filled);
                    holder.btnYeuThich.setColorFilter(ContextCompat.getColor(context, R.color.red));
                } else {
                    holder.btnYeuThich.setImageResource(R.drawable.ic_heart_outline);
                    holder.btnYeuThich.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
                }
                
                // Call API with callback
                favoriteManager.toggleFavorite(product.get_id(), new FavoriteManager.OnFavoriteCallback() {
                    @Override
                    public void onSuccess(boolean isFavorite) {
                        // API success - UI already updated (optimistic)
                        android.util.Log.d("ProductAdapter", "Favorite toggled successfully: " + isFavorite);
                        
                        // Notify listener
                        if (onFavoriteChangeListener != null) {
                            onFavoriteChangeListener.onFavoriteChanged(product.get_id(), isFavorite);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // API failed - revert UI change
                        android.util.Log.e("ProductAdapter", "Error toggling favorite: " + error);
                        
                        // Revert to original state
                        if (currentState) {
                            holder.btnYeuThich.setImageResource(R.drawable.ic_heart_filled);
                            holder.btnYeuThich.setColorFilter(ContextCompat.getColor(context, R.color.red));
                        } else {
                            holder.btnYeuThich.setImageResource(R.drawable.ic_heart_outline);
                            holder.btnYeuThich.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
                        }
                        
                        // Show error toast if needed
                        android.widget.Toast.makeText(context, "Không thể cập nhật yêu thích: " + error, 
                            android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

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
    
    private OnFavoriteChangeListener onFavoriteChangeListener;
    
    public void setOnFavoriteChangeListener(OnFavoriteChangeListener listener) {
        this.onFavoriteChangeListener = listener;
    }

    public interface OnProductClickListener {
        void onClick(ProductResponse product);
    }
    
    public interface OnFavoriteChangeListener {
        void onFavoriteChanged(String productId, boolean isFavorite);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSanPham;
        ImageButton btnYeuThich;
        TextView txtTenSanPham, txtGiaSanPham, badgeGiamGia, txtXemChiTiet;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSanPham = itemView.findViewById(R.id.imgSanPham);
            btnYeuThich = itemView.findViewById(R.id.btnYeuThich);
            txtTenSanPham = itemView.findViewById(R.id.txtTenSanPham);
            txtGiaSanPham = itemView.findViewById(R.id.txtGiaSanPham);
            badgeGiamGia = itemView.findViewById(R.id.badgeGiamGia);
            txtXemChiTiet = itemView.findViewById(R.id.txtXemChiTiet);
        }
    }
}

