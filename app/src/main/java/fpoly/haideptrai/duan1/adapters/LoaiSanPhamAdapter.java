package fpoly.haideptrai.duan1.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.models.CategoryResponse;

public class LoaiSanPhamAdapter extends RecyclerView.Adapter<LoaiSanPhamAdapter.ViewHolder> {

    private final List<CategoryResponse> original = new ArrayList<>();
    private final List<CategoryResponse> visible = new ArrayList<>();
    private OnCategoryClickListener onCategoryClickListener;

    public void setItems(List<CategoryResponse> items) {
        original.clear();
        if (items != null) original.addAll(items);
        filter("");
    }

    public void filter(String query) {
        visible.clear();
        if (query == null || query.trim().isEmpty()) {
            visible.addAll(original);
        } else {
            String q = query.trim().toLowerCase();
            for (CategoryResponse c : original) {
                String name = c.getName() != null ? c.getName() : "";
                String desc = c.getDescription() != null ? c.getDescription() : "";
                if (name.toLowerCase().contains(q) || desc.toLowerCase().contains(q)) {
                    visible.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loai_san_pham, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryResponse item = visible.get(position);
        holder.txtTenLoaiSanPham.setText(item.getName());
        holder.itemView.setOnClickListener(v -> {
            if (onCategoryClickListener != null) {
                onCategoryClickListener.onClick(item);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (onCategoryClickListener != null) {
                onCategoryClickListener.onLongClick(item);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return visible.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTenLoaiSanPham;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTenLoaiSanPham = itemView.findViewById(R.id.txtTenLoaiSanPham);
        }
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.onCategoryClickListener = listener;
    }

    public interface OnCategoryClickListener {
        void onClick(CategoryResponse category);
        void onLongClick(CategoryResponse category);
    }
}
