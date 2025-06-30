// app/src/main/java/com/example/newtrade/ui/chat/adapter/ChatMessagesAdapter.java
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

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_MESSAGE_SYSTEM = 3;

    private List<ChatActivity.ChatMessage> messages;
    private Long currentUserId;
    private OnMessageActionListener listener;

    public interface OnMessageActionListener {
        void onMessageClick(ChatActivity.ChatMessage message);
        void onMessageLongClick(ChatActivity.ChatMessage message);
        void onAttachmentClick(ChatActivity.ChatMessage message);
    }

    public ChatMessagesAdapter(List<ChatActivity.ChatMessage> messages, Long currentUserId, OnMessageActionListener listener) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatActivity.ChatMessage message = messages.get(position);

        if (message.type == ChatActivity.ChatMessage.MessageType.SYSTEM) {
            return VIEW_TYPE_MESSAGE_SYSTEM;
        } else if (message.isSentByCurrentUser(currentUserId)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_MESSAGE_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageViewHolder(view);
            case VIEW_TYPE_MESSAGE_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(view);
            case VIEW_TYPE_MESSAGE_SYSTEM:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_system, parent, false);
                return new SystemMessageViewHolder(view);
            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatActivity.ChatMessage message = messages.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                bindSentMessage((SentMessageViewHolder) holder, message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                bindReceivedMessage((ReceivedMessageViewHolder) holder, message);
                break;
            case VIEW_TYPE_MESSAGE_SYSTEM:
                bindSystemMessage((SystemMessageViewHolder) holder, message);
                break;
        }
    }

    private void bindSentMessage(SentMessageViewHolder holder, ChatActivity.ChatMessage message) {
        holder.tvMessage.setText(message.content);

        if (message.timestamp != null) {
            holder.tvTime.setText(DateTimeUtils.formatMessageTime(message.timestamp));
        }

        // Read status
        if (message.isRead) {
            holder.ivReadStatus.setImageResource(R.drawable.ic_message_read);
        } else {
            holder.ivReadStatus.setImageResource(R.drawable.ic_message_sent);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMessageClick(message);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onMessageLongClick(message);
            }
            return true;
        });
    }

    private void bindReceivedMessage(ReceivedMessageViewHolder holder, ChatActivity.ChatMessage message) {
        holder.tvMessage.setText(message.content);

        if (message.timestamp != null) {
            holder.tvTime.setText(DateTimeUtils.formatMessageTime(message.timestamp));
        }

        // Sender info
        if (message.senderName != null) {
            holder.tvSenderName.setText(message.senderName);
        }

        // Avatar
        if (message.senderAvatar != null && !message.senderAvatar.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(message.senderAvatar)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMessageClick(message);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onMessageLongClick(message);
            }
            return true;
        });
    }

    private void bindSystemMessage(SystemMessageViewHolder holder, ChatActivity.ChatMessage message) {
        holder.tvMessage.setText(message.content);

        if (message.timestamp != null) {
            holder.tvTime.setText(DateTimeUtils.formatMessageTime(message.timestamp));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder classes
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ImageView ivReadStatus;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivReadStatus = itemView.findViewById(R.id.iv_read_status);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvSenderName;
        ImageView ivAvatar;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }

    static class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}