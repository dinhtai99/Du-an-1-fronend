package fpoly.haideptrai.duan1.customer.adapters;

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
import fpoly.haideptrai.duan1.api.models.CategoryResponse;

public class CategoryHomeAdapter extends RecyclerView.Adapter<CategoryHomeAdapter.ViewHolder> {

    private final List<CategoryResponse> items = new ArrayList<>();
    private OnCategoryClickListener onCategoryClickListener;

    public void setItems(List<CategoryResponse> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_danh_muc_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryResponse category = items.get(position);
        holder.txtTenDanhMuc.setText(category.getName());

        // Set beautiful gradient background based on position
        int[] gradientBackgrounds = {
            R.drawable.category_bg_gradient_1,
            R.drawable.category_bg_gradient_2,
            R.drawable.category_bg_gradient_3,
            R.drawable.category_bg_gradient_4,
            R.drawable.category_bg_gradient_5,
            R.drawable.category_bg_gradient_6
        };
        int gradientIndex = position % gradientBackgrounds.length;
        holder.imgDanhMuc.setBackgroundResource(gradientBackgrounds[gradientIndex]);

        // Get appropriate icon based on category name
        int defaultIcon = getCategoryIcon(category.getName());
        
        // Load image if available
        String imageUrl = category.getImage();
        if (imageUrl != null && !imageUrl.trim().isEmpty() && !imageUrl.contains("example.com")) {
            Glide.with(holder.imgDanhMuc.getContext())
                    .load(imageUrl)
                    .placeholder(defaultIcon)
                    .error(defaultIcon)
                    .into(holder.imgDanhMuc);
        } else {
            holder.imgDanhMuc.setImageResource(defaultIcon);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onCategoryClickListener != null) {
                onCategoryClickListener.onClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.onCategoryClickListener = listener;
    }

    private int getCategoryIcon(String categoryName) {
        if (categoryName == null) {
            return R.drawable.ic_category_default;
        }
        String name = categoryName.toLowerCase();
        if (name.contains("laptop") || name.contains("máy tính")) {
            return R.drawable.ic_laptop;
        } else if (name.contains("phone") || name.contains("điện thoại") || name.contains("smartphone")) {
            return R.drawable.ic_phone;
        } else if (name.contains("phụ kiện") || name.contains("accessories") || name.contains("phu kien")) {
            return R.drawable.ic_accessories;
        }
        return R.drawable.ic_category_default;
    }

    public interface OnCategoryClickListener {
        void onClick(CategoryResponse category);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDanhMuc;
        TextView txtTenDanhMuc;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDanhMuc = itemView.findViewById(R.id.imgDanhMuc);
            txtTenDanhMuc = itemView.findViewById(R.id.txtTenDanhMuc);
        }
    }
}

