package fpoly.haideptrai.duan1.customer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.customer.models.TimelineItem;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private final List<TimelineItem> items = new ArrayList<>();

    public void setItems(List<TimelineItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimelineItem item = items.get(position);
        holder.txtStatus.setText(item.getStatus());
        holder.txtDateTime.setText(item.getDateTime());

        // Show line if not last item
        if (position < items.size() - 1) {
            holder.viewLine.setVisibility(View.VISIBLE);
        } else {
            holder.viewLine.setVisibility(View.GONE);
        }

        // Set dot color based on completion
        if (item.isCompleted()) {
            holder.viewDot.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.primary_blue));
        } else {
            holder.viewDot.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.text_secondary));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewDot, viewLine;
        TextView txtStatus, txtDateTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewDot = itemView.findViewById(R.id.viewDot);
            viewLine = itemView.findViewById(R.id.viewLine);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
        }
    }
}

