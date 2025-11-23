package fpoly.haideptrai.duan1.customer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.models.ReviewResponse;
import fpoly.haideptrai.duan1.customer.adapters.ReviewImageAdapter;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private final List<ReviewResponse> items = new ArrayList<>();

    public void setItems(List<ReviewResponse> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void addItems(List<ReviewResponse> list) {
        if (list != null && !list.isEmpty()) {
            int startPosition = items.size();
            items.addAll(list);
            notifyItemRangeInserted(startPosition, list.size());
        }
    }

    public List<ReviewResponse> getItems() {
        return new ArrayList<>(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewResponse review = items.get(position);
        
        // User name
        String userName = "Người dùng";
        if (review.getUserInfo() != null && review.getUserInfo().getFullName() != null) {
            userName = review.getUserInfo().getFullName();
        }
        holder.txtUserName.setText(userName);
        
        // Avatar
        if (review.getUserInfo() != null && review.getUserInfo().getAvatar() != null 
            && !review.getUserInfo().getAvatar().trim().isEmpty()) {
            Glide.with(holder.imgAvatar.getContext())
                    .load(review.getUserInfo().getAvatar())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.mipmap.ic_launcher);
        }
        
        // Rating stars
        holder.layoutRating.removeAllViews();
        int rating = review.getRating() != null ? review.getRating() : 0;
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView(holder.layoutRating.getContext());
            int starSize = (int) (24 * holder.layoutRating.getContext().getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(starSize, starSize);
            params.setMargins(0, 0, 4, 0);
            star.setLayoutParams(params);
            
            if (i <= rating) {
                star.setImageResource(android.R.drawable.star_big_on);
            } else {
                star.setImageResource(android.R.drawable.star_big_off);
            }
            star.setColorFilter(holder.layoutRating.getContext().getResources().getColor(R.color.orange, null));
            holder.layoutRating.addView(star);
        }
        
        // Comment
        if (review.getComment() != null && !review.getComment().trim().isEmpty()) {
            holder.txtComment.setText(review.getComment());
            holder.txtComment.setVisibility(View.VISIBLE);
        } else {
            holder.txtComment.setVisibility(View.GONE);
        }
        
        // Date
        String dateStr = formatDate(review.getCreatedAt());
        holder.txtDate.setText(dateStr);
        
        // Images
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            holder.rvReviewImages.setVisibility(View.VISIBLE);
            if (holder.rvReviewImages.getAdapter() == null) {
                ReviewImageAdapter imageAdapter = new ReviewImageAdapter();
                holder.rvReviewImages.setLayoutManager(new LinearLayoutManager(
                    holder.rvReviewImages.getContext(), 
                    LinearLayoutManager.HORIZONTAL, 
                    false
                ));
                holder.rvReviewImages.setAdapter(imageAdapter);
            }
            ReviewImageAdapter imageAdapter = (ReviewImageAdapter) holder.rvReviewImages.getAdapter();
            imageAdapter.setImageUrls(review.getImages());
        } else {
            holder.rvReviewImages.setVisibility(View.GONE);
        }
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            
            String cleanDate = dateString.split("\\.")[0];
            if (cleanDate.contains("Z")) {
                cleanDate = cleanDate.replace("Z", "");
            }
            
            Date date = inputFormat.parse(cleanDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtUserName, txtComment, txtDate;
        LinearLayout layoutRating;
        RecyclerView rvReviewImages;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtDate = itemView.findViewById(R.id.txtDate);
            layoutRating = itemView.findViewById(R.id.layoutRating);
            rvReviewImages = itemView.findViewById(R.id.rvReviewImages);
        }
    }
}

