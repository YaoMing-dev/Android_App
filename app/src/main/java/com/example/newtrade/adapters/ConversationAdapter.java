// app/src/main/java/com/example/newtrade/adapters/ConversationAdapter.java
package com.example.newtrade.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.models.Conversation;
import com.example.newtrade.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private static final String TAG = "ConversationAdapter";

    private Context context;
    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(Context context, List<Conversation> conversations, OnConversationClickListener listener) {
        this.context = context;
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivAvatar;
        private ImageView ivProductImage;
        private TextView tvUserName;
        private TextView tvProductTitle;
        private TextView tvLastMessage;
        private TextView tvMessageTime;
        private TextView tvUnreadBadge;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvMessageTime = itemView.findViewById(R.id.tv_message_time);
            tvUnreadBadge = itemView.findViewById(R.id.tv_unread_badge);
        }

        public void bind(Conversation conversation) {
            // User name
            if (tvUserName != null) {
                tvUserName.setText(conversation.getOtherUserName() != null ?
                        conversation.getOtherUserName() : "Unknown User");
            }

            // Product title
            if (tvProductTitle != null) {
                tvProductTitle.setText(conversation.getProductTitle() != null ?
                        conversation.getProductTitle() : "Product");
            }

            // Last message
            if (tvLastMessage != null) {
                String lastMessage = conversation.getLastMessage();
                if (!TextUtils.isEmpty(lastMessage)) {
                    tvLastMessage.setText(lastMessage);
                } else {
                    tvLastMessage.setText("No messages yet");
                }
            }

            // Message time
            if (tvMessageTime != null) {
                String messageTime = conversation.getLastMessageTime();
                if (!TextUtils.isEmpty(messageTime)) {
                    tvMessageTime.setText(formatMessageTime(messageTime));
                } else {
                    tvMessageTime.setText("");
                }
            }

            // Unread badge
            if (tvUnreadBadge != null) {
                int unreadCount = conversation.getUnreadCount();
                if (unreadCount > 0) {
                    tvUnreadBadge.setVisibility(View.VISIBLE);
                    if (unreadCount > 99) {
                        tvUnreadBadge.setText("99+");
                    } else {
                        tvUnreadBadge.setText(String.valueOf(unreadCount));
                    }
                } else {
                    tvUnreadBadge.setVisibility(View.GONE);
                }
            }

            // User avatar
            if (ivAvatar != null) {
                String avatarUrl = conversation.getOtherUserAvatar();
                if (!TextUtils.isEmpty(avatarUrl)) {
                    Glide.with(context)
                            .load(Constants.BASE_URL + avatarUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_person);
                }
            }

            // Product image
            if (ivProductImage != null) {
                String productImageUrl = conversation.getProductImageUrl();
                if (!TextUtils.isEmpty(productImageUrl)) {
                    Glide.with(context)
                            .load(Constants.BASE_URL + productImageUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_placeholder)
                            .into(ivProductImage);
                } else {
                    ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
                }
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });

            // Style based on unread status
            styleConversationItem(conversation.hasUnreadMessages());
        }

        private void styleConversationItem(boolean hasUnread) {
            if (hasUnread) {
                // Bold text for unread conversations
                if (tvUserName != null) {
                    tvUserName.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                if (tvLastMessage != null) {
                    tvLastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
                }
            } else {
                // Normal text for read conversations
                if (tvUserName != null) {
                    tvUserName.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                if (tvLastMessage != null) {
                    tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
            }
        }

        // ✅ FIXED: Use java.util.Date instead of java.time (API level compatible)
        private String formatMessageTime(String messageTime) {
            try {
                // Simple time formatting - can be enhanced
                if (messageTime.contains("trước")) {
                    return messageTime; // Already formatted like "10 phút trước"
                }

                // If it's a timestamp, format it
                if (messageTime.contains("T")) {
                    // Parse ISO timestamp using SimpleDateFormat
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date messageDate = inputFormat.parse(messageTime);

                    if (messageDate != null) {
                        Date now = new Date();
                        long diffInMillis = now.getTime() - messageDate.getTime();

                        long minutes = diffInMillis / (60 * 1000);
                        long hours = diffInMillis / (60 * 60 * 1000);
                        long days = diffInMillis / (24 * 60 * 60 * 1000);

                        if (minutes < 1) {
                            return "Vừa xong";
                        } else if (minutes < 60) {
                            return minutes + " phút trước";
                        } else if (hours < 24) {
                            return hours + " giờ trước";
                        } else if (days < 7) {
                            return days + " ngày trước";
                        } else {
                            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                            return outputFormat.format(messageDate);
                        }
                    }
                }

                return messageTime;

            } catch (ParseException e) {
                return messageTime; // Return as-is if parsing fails
            }
        }
    }

    // ===== PUBLIC METHODS =====

    public void updateConversations(List<Conversation> newConversations) {
        this.conversations.clear();
        this.conversations.addAll(newConversations);
        notifyDataSetChanged();
    }

    public void addConversation(Conversation conversation) {
        this.conversations.add(0, conversation); // Add to top
        notifyItemInserted(0);
    }

    public void updateConversation(Conversation updatedConversation) {
        for (int i = 0; i < conversations.size(); i++) {
            Conversation conversation = conversations.get(i);
            if (conversation.getId().equals(updatedConversation.getId())) {
                conversations.set(i, updatedConversation);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeConversation(Long conversationId) {
        for (int i = 0; i < conversations.size(); i++) {
            Conversation conversation = conversations.get(i);
            if (conversation.getId().equals(conversationId)) {
                conversations.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }
}