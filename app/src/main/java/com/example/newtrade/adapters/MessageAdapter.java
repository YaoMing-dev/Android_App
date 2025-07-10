// ✅ COMPLETE: MessageAdapter.java with Image Viewer
package com.example.newtrade.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.models.Message;
import com.example.newtrade.ui.image.ImageViewerActivity;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "MessageAdapter";
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messages;
    private Long currentUserId;

    public MessageAdapter(Context context, List<Message> messages, Long currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        Long senderId = message.getSenderId();

        if (senderId != null && currentUserId != null && senderId.equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message, context);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message, context);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ✅ COMPLETE: SentMessageViewHolder với image click support
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ImageView ivImage;
        View llImageContainer;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivImage = itemView.findViewById(R.id.iv_image);
            llImageContainer = itemView.findViewById(R.id.ll_image_container);

            Log.d("MessageAdapter", "SentMessageViewHolder - tvMessage: " + tvMessage + ", tvTime: " + tvTime + ", ivImage: " + ivImage);
        }

        void bind(Message message, Context context) {
            // Check if it's an image message
            if (message.isImageMessage()) {
                // Show image, hide text
                if (tvMessage != null) tvMessage.setVisibility(View.GONE);
                if (llImageContainer != null) llImageContainer.setVisibility(View.VISIBLE);

                if (ivImage != null) {
                    String imageUrl = message.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {

                        // Load image based on URL type
                        if (imageUrl.startsWith("content://")) {
                            // Local URI - load directly
                            ivImage.setImageURI(Uri.parse(imageUrl));
                            Log.d(TAG, "📷 Loading local image: " + imageUrl);
                        } else {
                            // Server URL - use Glide
                            String fullUrl = imageUrl.startsWith("/") ?
                                    "http://10.0.2.2:8080" + imageUrl : imageUrl;

                            Glide.with(itemView.getContext())
                                    .load(fullUrl)
                                    .placeholder(R.drawable.ic_image_placeholder)
                                    .error(R.drawable.ic_image_error)
                                    .centerCrop()
                                    .into(ivImage);

                            Log.d(TAG, "📷 Loading server image: " + fullUrl);
                        }

                        // ✅ NEW: Add click listener to open ImageViewerActivity
                        ivImage.setOnClickListener(v -> {
                            try {
                                Intent intent = new Intent(context, ImageViewerActivity.class);
                                intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, imageUrl);
                                intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_TITLE, "Sent Image");
                                context.startActivity(intent);
                                Log.d(TAG, "🖼️ Opening image viewer for: " + imageUrl);
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error opening image viewer", e);
                            }
                        });

                        // ✅ NEW: Add visual feedback on touch
                        ivImage.setOnTouchListener((v, event) -> {
                            switch (event.getAction()) {
                                case android.view.MotionEvent.ACTION_DOWN:
                                    v.setAlpha(0.7f);
                                    break;
                                case android.view.MotionEvent.ACTION_UP:
                                case android.view.MotionEvent.ACTION_CANCEL:
                                    v.setAlpha(1.0f);
                                    break;
                            }
                            return false; // Allow click to proceed
                        });

                    } else {
                        ivImage.setImageResource(R.drawable.ic_image_placeholder);
                        ivImage.setOnClickListener(null); // Remove click listener
                    }
                }
            } else {
                // Show text, hide image
                if (llImageContainer != null) llImageContainer.setVisibility(View.GONE);
                if (tvMessage != null) {
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText(message.getContent() != null ? message.getContent() : "");
                }
            }

            // Always set time
            if (tvTime != null) {
                tvTime.setText(message.getCreatedAt() != null ? message.getCreatedAt() : "");
            }
        }
    }

    // ✅ COMPLETE: ReceivedMessageViewHolder với image click support
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ImageView ivImage;
        View llImageContainer;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivImage = itemView.findViewById(R.id.iv_image);
            llImageContainer = itemView.findViewById(R.id.ll_image_container);

            Log.d("MessageAdapter", "ReceivedMessageViewHolder - tvMessage: " + tvMessage + ", tvTime: " + tvTime + ", ivImage: " + ivImage);
        }

        void bind(Message message, Context context) {
            // Check if it's an image message
            if (message.isImageMessage()) {
                // Show image, hide text
                if (tvMessage != null) tvMessage.setVisibility(View.GONE);
                if (llImageContainer != null) llImageContainer.setVisibility(View.VISIBLE);

                if (ivImage != null) {
                    String imageUrl = message.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {

                        // Load image based on URL type
                        if (imageUrl.startsWith("content://")) {
                            // Local URI - load directly
                            ivImage.setImageURI(Uri.parse(imageUrl));
                            Log.d(TAG, "📷 Loading local image: " + imageUrl);
                        } else {
                            // Server URL - use Glide
                            String fullUrl = imageUrl.startsWith("/") ?
                                    "http://10.0.2.2:8080" + imageUrl : imageUrl;

                            Glide.with(itemView.getContext())
                                    .load(fullUrl)
                                    .placeholder(R.drawable.ic_image_placeholder)
                                    .error(R.drawable.ic_image_error)
                                    .centerCrop()
                                    .into(ivImage);

                            Log.d(TAG, "📷 Loading server image: " + fullUrl);
                        }

                        // ✅ NEW: Add click listener to open ImageViewerActivity
                        ivImage.setOnClickListener(v -> {
                            try {
                                Intent intent = new Intent(context, ImageViewerActivity.class);
                                intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, imageUrl);
                                intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_TITLE, "Received Image");
                                context.startActivity(intent);
                                Log.d(TAG, "🖼️ Opening image viewer for: " + imageUrl);
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error opening image viewer", e);
                            }
                        });

                        // ✅ NEW: Add visual feedback on touch
                        ivImage.setOnTouchListener((v, event) -> {
                            switch (event.getAction()) {
                                case android.view.MotionEvent.ACTION_DOWN:
                                    v.setAlpha(0.7f);
                                    break;
                                case android.view.MotionEvent.ACTION_UP:
                                case android.view.MotionEvent.ACTION_CANCEL:
                                    v.setAlpha(1.0f);
                                    break;
                            }
                            return false; // Allow click to proceed
                        });

                    } else {
                        ivImage.setImageResource(R.drawable.ic_image_placeholder);
                        ivImage.setOnClickListener(null); // Remove click listener
                    }
                }
            } else {
                // Show text, hide image
                if (llImageContainer != null) llImageContainer.setVisibility(View.GONE);
                if (tvMessage != null) {
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText(message.getContent() != null ? message.getContent() : "");
                }
            }

            // Always set time
            if (tvTime != null) {
                tvTime.setText(message.getCreatedAt() != null ? message.getCreatedAt() : "");
            }
        }
    }

    // ✅ NEW: Helper method để update single message
    public void updateMessage(int position, Message updatedMessage) {
        if (position >= 0 && position < messages.size()) {
            messages.set(position, updatedMessage);
            notifyItemChanged(position);
        }
    }

    // ✅ NEW: Helper method để add message
    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    // ✅ NEW: Helper method để clear all messages
    public void clearMessages() {
        int size = messages.size();
        messages.clear();
        notifyItemRangeRemoved(0, size);
    }

    // ✅ NEW: Helper method để get message at position
    public Message getMessageAt(int position) {
        if (position >= 0 && position < messages.size()) {
            return messages.get(position);
        }
        return null;
    }

    // ✅ NEW: Helper method để find message by ID
    public int findMessagePosition(Long messageId) {
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message.getId() != null && message.getId().equals(messageId)) {
                return i;
            }
        }
        return -1;
    }

    // ✅ NEW: Helper method để update message status by ID
    public void updateMessageStatus(Long messageId, String status) {
        int position = findMessagePosition(messageId);
        if (position != -1) {
            Message message = messages.get(position);
            message.setCreatedAt(status);
            notifyItemChanged(position);
        }
    }
}