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

    // ✅ SỬA: Thay ChatActivity.ChatMessage thành ChatActivity.MessageItem
    private List<ChatActivity.MessageItem> messages;
    private Long currentUserId;
    private OnMessageActionListener listener;

    public interface OnMessageActionListener {
        void onMessageClick(ChatActivity.MessageItem message);
        void onMessageLongClick(ChatActivity.MessageItem message);
    }

    // ✅ SỬA: Constructor
    public ChatMessagesAdapter(List<ChatActivity.MessageItem> messages, Long currentUserId, OnMessageActionListener listener) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatActivity.MessageItem message = messages.get(position);
        // ✅ SỬA: Sử dụng MessageItem thay vì ChatMessage
        return message.isFromMe ? VIEW_TYPE_MESSAGE_SENT : VIEW_TYPE_MESSAGE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatActivity.MessageItem message = messages.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            bindSentMessage((SentMessageViewHolder) holder, message);
        } else {
            bindReceivedMessage((ReceivedMessageViewHolder) holder, message);
        }
    }

    private void bindSentMessage(SentMessageViewHolder holder, ChatActivity.MessageItem message) {
        holder.tvMessage.setText(message.content);
        if (message.timestamp != null) {
            holder.tvTime.setText(DateTimeUtils.formatMessageTime(message.timestamp));
        }
        // Set read status text
        holder.tvStatus.setText(message.isRead ? "✓✓" : "✓");
    }

    private void bindReceivedMessage(ReceivedMessageViewHolder holder, ChatActivity.MessageItem message) {
        holder.tvMessage.setText(message.content);
        if (message.timestamp != null) {
            holder.tvTime.setText(DateTimeUtils.formatMessageTime(message.timestamp));
        }
        if (message.senderName != null) {
            holder.tvSenderName.setText(message.senderName);
        }

        // Load sender avatar
        if (message.senderAvatar != null && !message.senderAvatar.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(message.senderAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder classes
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvStatus;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
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
}