package fpoly.haideptrai.duan1.customer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.customer.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final List<Message> items;

    public MessageAdapter(List<Message> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = items.get(position);
        holder.txtMessage.setText(message.getText());
        holder.txtTime.setText(message.getTime());

        // Set alignment and background based on message type
        if (message.isUser()) {
            holder.layoutMessage.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            ((LinearLayout.LayoutParams) holder.layoutMessage.getLayoutParams()).gravity = android.view.Gravity.END;
            holder.layoutMessage.setBackgroundColor(holder.itemView.getContext().getColor(R.color.primary_blue_light));
            holder.txtMessage.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
        } else {
            holder.layoutMessage.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            ((LinearLayout.LayoutParams) holder.layoutMessage.getLayoutParams()).gravity = android.view.Gravity.START;
            holder.layoutMessage.setBackgroundColor(holder.itemView.getContext().getColor(R.color.grey_light));
            holder.txtMessage.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMessage;
        TextView txtMessage, txtTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutMessage = itemView.findViewById(R.id.layoutMessage);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}

