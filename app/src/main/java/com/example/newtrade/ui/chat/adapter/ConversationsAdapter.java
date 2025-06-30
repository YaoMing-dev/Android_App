// app/src/main/java/com/example/newtrade/ui/chat/adapter/ConversationsAdapter.java
package com.example.newtrade.ui.chat.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.ui.chat.MessagesFragment;
import com.example.newtrade.utils.DateTimeUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {

    private List<MessagesFragment.ConversationItem> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(MessagesFragment.ConversationItem conversation);
    }

    public ConversationsAdapter(List<MessagesFragment.ConversationItem> conversations,
                                OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        MessagesFragment.ConversationItem conversation = conversations.get(position);

        // Other user name
        holder.tvUserName.setText(conversation.otherUserName != null ?
                conversation.otherUserName : "Unknown User");

        // Product title
        if (conversation.productTitle != null && !conversation.productTitle.isEmpty()) {
            holder.tvProductTitle.setText(conversation.productTitle);
            holder.tvProductTitle.setVisibility(View.VISIBLE);
        } else {
            holder.tvProductTitle.setVisibility(View.GONE);
        }

        // Last message
        if (conversation.lastMessage != null && !conversation.lastMessage.isEmpty()) {
            holder.tvLastMessage.setText(conversation.lastMessage);
        } else {
            holder.tvLastMessage.setText("No messages yet");
        }

        // Time
        if (conversation.lastMessageTime != null) {
            String formattedTime = DateTimeUtils.formatMessageTime(conversation.lastMessageTime);
            holder.tvTime.setText(formattedTime);
        } else {
            holder.tvTime.setText("");
        }

        // Read status styling
        if (conversation.isRead) {
            holder.tvUserName.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.text_secondary));
            holder.tvLastMessage.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.text_secondary));
            holder.itemView.setBackgroundResource(R.color.transparent);
        } else {
            holder.tvUserName.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.text_primary));
            holder.tvLastMessage.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.text_primary));
            holder.itemView.setBackgroundResource(R.color.unread_message_bg);
        }

        // Unread count badge
        if (conversation.unreadCount > 0) {
            holder.tvUnreadCount.setText(String.valueOf(conversation.unreadCount));
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        // User avatar
        if (conversation.otherUserAvatar != null && !conversation.otherUserAvatar.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(conversation.otherUserAvatar)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }

        // Product image
        if (conversation.productImage != null && !conversation.productImage.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(conversation.productImage)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(holder.ivProductImage);
            holder.ivProductImage.setVisibility(View.VISIBLE);
        } else {
            holder.ivProductImage.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivProductImage;
        TextView tvUserName, tvProductTitle, tvLastMessage, tvTime, tvUnreadCount;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
        }
    }
}