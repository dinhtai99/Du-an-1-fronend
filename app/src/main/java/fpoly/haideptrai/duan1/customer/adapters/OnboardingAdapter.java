package fpoly.haideptrai.duan1.customer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.customer.models.OnboardingItem;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = onboardingItems.get(position);
        
        // Set ảnh
        int imageResId = item.getImageResId();
        Log.d("OnboardingAdapter", "Binding position " + position + ", image resource ID: " + imageResId);
        
        try {
            if (holder.imgIllustration != null) {
                holder.imgIllustration.setImageResource(imageResId);
                holder.imgIllustration.setVisibility(View.VISIBLE);
                holder.imgIllustration.setAlpha(1.0f);
                Log.d("OnboardingAdapter", "Image set successfully for position " + position + ", resource ID: " + imageResId);
            } else {
                Log.e("OnboardingAdapter", "ImageView is null for position " + position);
            }
        } catch (Exception e) {
            Log.e("OnboardingAdapter", "Error setting image for position " + position + ": " + e.getMessage(), e);
            if (holder.imgIllustration != null) {
                holder.imgIllustration.setImageResource(R.drawable.img_1);
            }
        }
        
        // Set text
        try {
            holder.txtTitle.setText(item.getTitleResId());
            holder.txtDescription.setText(item.getDescriptionResId());
            Log.d("OnboardingAdapter", "Text set successfully for position " + position);
        } catch (Exception e) {
            Log.e("OnboardingAdapter", "Error setting text for position " + position + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return onboardingItems != null ? onboardingItems.size() : 0;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgIllustration;
        private final TextView txtTitle;
        private final TextView txtDescription;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIllustration = itemView.findViewById(R.id.imgIllustration);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            
            if (imgIllustration == null) {
                Log.e("OnboardingAdapter", "ImageView imgIllustration is NULL!");
            } else {
                Log.d("OnboardingAdapter", "ImageView found successfully");
            }
        }
    }
}

