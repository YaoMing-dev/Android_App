// app/src/main/java/com/example/newtrade/adapters/MessageAdapter.java
// ✅ FIXED - Replace getCreatedAt() with getTimestamp()
package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messages;
    private final Long currentUserId;

    public MessageAdapter(List<Message> messages, Long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message, currentUserId);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final TextView tvTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        public void bind(Message message, Long currentUserId) {
            tvMessage.setText(message.getContent());

            // ✅ FIX: Use getTimestamp() instead of getCreatedAt()
            tvTime.setText(message.getTimestamp() != null ? message.getTimestamp() : "");

            // Style message based on sender
            boolean isOwnMessage = message.getSenderId() != null &&
                    currentUserId != null &&
                    message.getSenderId().equals(currentUserId);

            // Apply different background based on message ownership
            if (isOwnMessage) {
                itemView.setBackgroundResource(R.drawable.bg_message_sent);
                tvMessage.setTextColor(itemView.getContext().getColor(android.R.color.white));
            } else {
                itemView.setBackgroundResource(R.drawable.bg_message_received);
                tvMessage.setTextColor(itemView.getContext().getColor(android.R.color.black));
            }
        }
    }
}