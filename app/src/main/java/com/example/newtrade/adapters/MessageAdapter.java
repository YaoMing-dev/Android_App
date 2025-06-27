// app/src/main/java/com/example/newtrade/adapters/MessageAdapter.java
package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<Message> messages;
    private final Long currentUserId;

    public MessageAdapter(List<Message> messages, Long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder for sent messages
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessageText;
        private final TextView tvTimestamp;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tv_message_text);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }

        public void bind(Message message) {
            tvMessageText.setText(message.getContent());
            if (tvTimestamp != null) {
                tvTimestamp.setText(message.getTimestamp() != null ? message.getTimestamp() : "");
            }
        }
    }

    // ViewHolder for received messages
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessageText;
        private final TextView tvTimestamp;
        private final ImageView ivSenderAvatar;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tv_message_text);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            ivSenderAvatar = itemView.findViewById(R.id.iv_sender_avatar);
        }

        public void bind(Message message) {
            tvMessageText.setText(message.getContent());
            if (tvTimestamp != null) {
                tvTimestamp.setText(message.getTimestamp() != null ? message.getTimestamp() : "");
            }

            // Set sender avatar if available
            if (ivSenderAvatar != null) {
                // Use placeholder for now
                ivSenderAvatar.setImageResource(R.drawable.placeholder_avatar);
            }
        }
    }
}