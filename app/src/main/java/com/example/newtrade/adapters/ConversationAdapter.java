// File: app/src/main/java/com/example/newtrade/adapters/ConversationAdapter.java
package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.models.Conversation;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private final List<Conversation> conversations;
    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
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
        Conversation conversation = conversations.get(position);
        holder.bind(conversation, listener);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView ivAvatar;
        private final TextView tvUserName;
        private final TextView tvLastMessage;
        private final TextView tvTime;
        private final View vUnreadIndicator;
        private final View vOnlineIndicator;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            vUnreadIndicator = itemView.findViewById(R.id.v_unread_indicator);
            vOnlineIndicator = itemView.findViewById(R.id.v_online_indicator);
        }

        void bind(Conversation conversation, OnConversationClickListener listener) {
            // Set user name
            if (tvUserName != null) {
                tvUserName.setText(conversation.getOtherUserName());
            }

            // Set last message
            if (tvLastMessage != null) {
                tvLastMessage.setText(conversation.getLastMessage());
            }

            // Set time
            if (tvTime != null) {
                tvTime.setText(conversation.getLastMessageTime());
            }

            // Load avatar
            if (ivAvatar != null) {
                Glide.with(itemView.getContext())
                        .load(conversation.getOtherUserAvatar())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivAvatar);
            }

            // Show/hide unread indicator
            if (vUnreadIndicator != null) {
                vUnreadIndicator.setVisibility(conversation.isHasUnreadMessages() ? View.VISIBLE : View.GONE);
            }

            // Show/hide online indicator
            if (vOnlineIndicator != null) {
                vOnlineIndicator.setVisibility(conversation.isOnline() ? View.VISIBLE : View.GONE);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });
        }
    }
}