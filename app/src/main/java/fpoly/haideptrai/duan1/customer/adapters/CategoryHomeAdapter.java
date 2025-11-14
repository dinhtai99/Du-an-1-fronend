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

        // Load image if available
        String imageUrl = category.getImage();
        if (imageUrl != null && !imageUrl.trim().isEmpty() && !imageUrl.contains("example.com")) {
            Glide.with(holder.imgDanhMuc.getContext())
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(holder.imgDanhMuc);
        } else {
            Glide.with(holder.imgDanhMuc.getContext())
                    .load(R.mipmap.ic_launcher)
                    .into(holder.imgDanhMuc);
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

