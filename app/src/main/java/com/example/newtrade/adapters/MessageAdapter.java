package com.example.newtrade.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_LOCATION_SENT = 3;
    private static final int VIEW_TYPE_LOCATION_RECEIVED = 4;

    private List<Message> messages;
    private Long currentUserId;
    private Context context;

    public MessageAdapter(List<Message> messages, Long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        boolean isCurrentUser = message.getSenderId().equals(currentUserId);

        if (message.isLocationMessage()) {
            return isCurrentUser ? VIEW_TYPE_LOCATION_SENT : VIEW_TYPE_LOCATION_RECEIVED;
        } else {
            return isCurrentUser ? VIEW_TYPE_MESSAGE_SENT : VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case VIEW_TYPE_MESSAGE_SENT:
                return new SentMessageViewHolder(inflater.inflate(R.layout.item_message_sent, parent, false));
            case VIEW_TYPE_MESSAGE_RECEIVED:
                return new ReceivedMessageViewHolder(inflater.inflate(R.layout.item_message_received, parent, false));
            case VIEW_TYPE_LOCATION_SENT:
                return new SentLocationViewHolder(inflater.inflate(R.layout.item_location_sent, parent, false));
            case VIEW_TYPE_LOCATION_RECEIVED:
                return new ReceivedLocationViewHolder(inflater.inflate(R.layout.item_location_received, parent, false));
            default:
                return new SentMessageViewHolder(inflater.inflate(R.layout.item_message_sent, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_LOCATION_SENT:
                ((SentLocationViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_LOCATION_RECEIVED:
                ((ReceivedLocationViewHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Sent Message ViewHolder
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTime;
        TextView tvStatus;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }

        void bind(Message message) {
            tvMessage.setText(message.getContent());
            tvTime.setText(formatTime(message.getCreatedAt()));

            // Show message status
            if (message.getStatus() != null) {
                switch (message.getStatus()) {
                    case "sent":
                        tvStatus.setText("✓");
                        break;
                    case "delivered":
                        tvStatus.setText("✓✓");
                        break;
                    case "read":
                        tvStatus.setText("✓✓");
                        tvStatus.setTextColor(itemView.getContext().getColor(R.color.primary));
                        break;
                    default:
                        tvStatus.setText("");
                }
            }
        }
    }

    // Received Message ViewHolder
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTime;
        TextView tvSenderName;
        CircleImageView ivSenderAvatar;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            ivSenderAvatar = itemView.findViewById(R.id.iv_sender_avatar);
        }

        void bind(Message message) {
            tvMessage.setText(message.getContent());
            tvTime.setText(formatTime(message.getCreatedAt()));

            if (message.getSenderName() != null) {
                tvSenderName.setText(message.getSenderName());
            }

            if (message.getSenderAvatar() != null && !message.getSenderAvatar().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(message.getSenderAvatar())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(ivSenderAvatar);
            }
        }
    }

    // Sent Location ViewHolder
    static class SentLocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocationName;
        TextView tvTime;
        TextView tvStatus;
        ImageView ivMapPreview;

        SentLocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocationName = itemView.findViewById(R.id.tv_location_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivMapPreview = itemView.findViewById(R.id.iv_map_preview);
        }

        void bind(Message message) {
            tvLocationName.setText(message.getLocationName() != null ?
                message.getLocationName() : "📍 Location");
            tvTime.setText(formatTime(message.getCreatedAt()));

            // Set click listener to open maps
            itemView.setOnClickListener(v -> openLocation(message));

            // Show status
            if (message.getStatus() != null) {
                switch (message.getStatus()) {
                    case "sent":
                        tvStatus.setText("✓");
                        break;
                    case "delivered":
                        tvStatus.setText("✓✓");
                        break;
                    case "read":
                        tvStatus.setText("✓✓");
                        tvStatus.setTextColor(itemView.getContext().getColor(R.color.primary));
                        break;
                }
            }
        }

        private void openLocation(Message message) {
            if (message.getLatitude() != null && message.getLongitude() != null) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                    message.getLatitude(), message.getLongitude(),
                    message.getLatitude(), message.getLongitude(),
                    message.getLocationName() != null ? message.getLocationName() : "Location");

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");

                if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                    itemView.getContext().startActivity(intent);
                } else {
                    // Fallback to browser
                    String webUri = String.format(Locale.ENGLISH,
                        "https://www.google.com/maps?q=%f,%f",
                        message.getLatitude(), message.getLongitude());
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                    itemView.getContext().startActivity(webIntent);
                }
            }
        }
    }

    // Received Location ViewHolder
    static class ReceivedLocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocationName;
        TextView tvTime;
        TextView tvSenderName;
        CircleImageView ivSenderAvatar;
        ImageView ivMapPreview;

        ReceivedLocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocationName = itemView.findViewById(R.id.tv_location_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            ivSenderAvatar = itemView.findViewById(R.id.iv_sender_avatar);
            ivMapPreview = itemView.findViewById(R.id.iv_map_preview);
        }

        void bind(Message message) {
            tvLocationName.setText(message.getLocationName() != null ?
                message.getLocationName() : "📍 Location");
            tvTime.setText(formatTime(message.getCreatedAt()));

            if (message.getSenderName() != null) {
                tvSenderName.setText(message.getSenderName());
            }

            if (message.getSenderAvatar() != null && !message.getSenderAvatar().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(message.getSenderAvatar())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(ivSenderAvatar);
            }

            // Set click listener to open location
            itemView.setOnClickListener(v -> openLocation(message));
        }

        private void openLocation(Message message) {
            if (message.getLatitude() != null && message.getLongitude() != null) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                    message.getLatitude(), message.getLongitude(),
                    message.getLatitude(), message.getLongitude(),
                    message.getLocationName() != null ? message.getLocationName() : "Location");

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");

                if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                    itemView.getContext().startActivity(intent);
                } else {
                    // Fallback to browser
                    String webUri = String.format(Locale.ENGLISH,
                        "https://www.google.com/maps?q=%f,%f",
                        message.getLatitude(), message.getLongitude());
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                    itemView.getContext().startActivity(webIntent);
                }
            }
        }
    }

    private static String formatTime(Long timestamp) {
        if (timestamp == null) return "";

        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}
