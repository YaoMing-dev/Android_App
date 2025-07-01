// app/src/main/java/com/example/newtrade/ui/chat/adapter/ConversationsAdapter.java
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
import com.example.newtrade.ui.chat.MessagesFragment;
import com.example.newtrade.utils.DateTimeUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.card.MaterialCardView;

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
        holder.bind(conversation, listener);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardConversation;
        private ImageView ivOtherUserAvatar, ivProductImage;
        private TextView tvOtherUserName, tvLastMessage, tvTimeAgo, tvProductTitle;
        private View badgeUnread;
        private TextView tvUnreadCount;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardConversation = itemView.findViewById(R.id.card_conversation);
            ivOtherUserAvatar = itemView.findViewById(R.id.iv_avatar); // Fixed ID
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvOtherUserName = itemView.findViewById(R.id.tv_user_name); // Fixed ID
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTimeAgo = itemView.findViewById(R.id.tv_time); // Fixed ID
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            badgeUnread = itemView.findViewById(R.id.v_unread_indicator); // Fixed ID
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
        }

        public void bind(MessagesFragment.ConversationItem conversation, OnConversationClickListener listener) {
            // Other user name
            tvOtherUserName.setText(conversation.otherUserName != null ?
                    conversation.otherUserName : "Unknown User");

            // Other user avatar
            if (conversation.otherUserAvatar != null && !conversation.otherUserAvatar.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(conversation.otherUserAvatar)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .error(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(ivOtherUserAvatar);
            } else {
                ivOtherUserAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            // Last message
            if (conversation.lastMessage != null && !conversation.lastMessage.isEmpty()) {
                String messagePrefix = conversation.isLastMessageFromMe ? "You: " : "";
                tvLastMessage.setText(messagePrefix + conversation.lastMessage);
            } else {
                tvLastMessage.setText("No messages yet");
            }

            // Time ago
            if (conversation.lastMessageTime != null) {
                tvTimeAgo.setText(DateTimeUtils.formatMessageTime(conversation.lastMessageTime));
            } else {
                tvTimeAgo.setText("");
            }

            // Product info
            if (conversation.productTitle != null) {
                tvProductTitle.setText(conversation.productTitle);
                tvProductTitle.setVisibility(View.VISIBLE);

                // Product image
                if (conversation.productImage != null && !conversation.productImage.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(conversation.productImage)
                            .placeholder(R.drawable.placeholder_product)
                            .error(R.drawable.placeholder_product)
                            .centerCrop()
                            .into(ivProductImage);
                    ivProductImage.setVisibility(View.VISIBLE);
                } else {
                    ivProductImage.setVisibility(View.GONE);
                }
            } else {
                tvProductTitle.setVisibility(View.GONE);
                ivProductImage.setVisibility(View.GONE);
            }

            // Unread badge
            if (conversation.unreadCount > 0) {
                badgeUnread.setVisibility(View.VISIBLE);
                tvUnreadCount.setText(String.valueOf(conversation.unreadCount));

                // Style for unread conversations
                cardConversation.setCardBackgroundColor(
                        itemView.getContext().getColor(R.color.conversation_unread_background));
                tvOtherUserName.setTypeface(null, android.graphics.Typeface.BOLD);
                tvLastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                badgeUnread.setVisibility(View.GONE);

                // Style for read conversations
                cardConversation.setCardBackgroundColor(
                        itemView.getContext().getColor(R.color.conversation_read_background));
                tvOtherUserName.setTypeface(null, android.graphics.Typeface.NORMAL);
                tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            // Click listener
            cardConversation.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });
        }
    }

    public void updateConversations(List<MessagesFragment.ConversationItem> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    public void addConversations(List<MessagesFragment.ConversationItem> newConversations) {
        int oldSize = conversations.size();
        conversations.addAll(newConversations);
        notifyItemRangeInserted(oldSize, newConversations.size());
    }

    public void markConversationAsRead(Long conversationId) {
        for (int i = 0; i < conversations.size(); i++) {
            MessagesFragment.ConversationItem conversation = conversations.get(i);
            if (conversation.id.equals(conversationId)) {
                conversation.unreadCount = 0;
                notifyItemChanged(i);
                break;
            }
        }
    }
}