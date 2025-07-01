// app/src/main/java/com/example/newtrade/ui/chat/adapter/MessagesAdapter.java
package com.example.newtrade.ui.chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.utils.DateTimeUtils;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_DATE_SEPARATOR = 3;

    private List<ChatActivity.MessageItem> messages;
    private Long currentUserId;

    public MessagesAdapter(List<ChatActivity.MessageItem> messages, Long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatActivity.MessageItem message = messages.get(position);

        // Check if we need a date separator
        if (position == 0 || !DateTimeUtils.isToday(message.timestamp)) {
            // For simplicity, we'll handle date separators in a different way
        }

        return message.isFromMe ? VIEW_TYPE_MESSAGE_SENT : VIEW_TYPE_MESSAGE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_MESSAGE_SENT:
                View sentView = inflater.inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageViewHolder(sentView);
            case VIEW_TYPE_MESSAGE_RECEIVED:
                View receivedView = inflater.inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(receivedView);
            default:
                View defaultView = inflater.inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageViewHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatActivity.MessageItem message = messages.get(position);

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

    // Sent message view holder
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage, tvTimestamp;
        private ImageView ivMessageStatus;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            ivMessageStatus = itemView.findViewById(R.id.iv_message_status);
        }

        public void bind(ChatActivity.MessageItem message) {
            tvMessage.setText(message.content);

            if (message.timestamp != null) {
                tvTimestamp.setText(DateTimeUtils.formatMessageTime(message.timestamp));
            }

            // Message status (sent, delivered, read)
            if (message.isRead) {
                ivMessageStatus.setImageResource(R.drawable.ic_message_read);
            } else {
                ivMessageStatus.setImageResource(R.drawable.ic_message_sent);
            }
        }
    }

    // Received message view holder
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage, tvTimestamp, tvSenderName;
        private ImageView ivSenderAvatar;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            ivSenderAvatar = itemView.findViewById(R.id.iv_sender_avatar);
        }

        public void bind(ChatActivity.MessageItem message) {
            tvMessage.setText(message.content);

            if (message.timestamp != null) {
                tvTimestamp.setText(DateTimeUtils.formatMessageTime(message.timestamp));
            }

            // Sender info
            if (message.senderName != null) {
                tvSenderName.setText(message.senderName);
                tvSenderName.setVisibility(View.VISIBLE);
            } else {
                tvSenderName.setVisibility(View.GONE);
            }

            // Sender avatar
            if (message.senderAvatar != null && !message.senderAvatar.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(message.senderAvatar)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .error(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(ivSenderAvatar);
            } else {
                ivSenderAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }
        }
    }

    public void updateMessages(List<ChatActivity.MessageItem> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    public void addMessage(ChatActivity.MessageItem message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void addMessagesAtStart(List<ChatActivity.MessageItem> newMessages) {
        messages.addAll(0, newMessages);
        notifyItemRangeInserted(0, newMessages.size());
    }
}