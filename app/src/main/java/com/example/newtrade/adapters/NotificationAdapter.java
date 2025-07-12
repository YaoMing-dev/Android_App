// app/src/main/java/com/example/newtrade/adapters/NotificationAdapter.java
package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.NotificationResponse;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationResponse> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationResponse notification);
        void onMarkAsReadClick(NotificationResponse notification);
        // ✅ NEW: Add promotion-specific click handler
        void onPromotionClick(NotificationResponse notification);
        void onPromoCodeCopy(String promoCode);
    }

    public NotificationAdapter(List<NotificationResponse> notifications,
                               OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationResponse notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconType;
        private TextView textTitle;
        private TextView textMessage;
        private TextView textTime;
        private View unreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            iconType = itemView.findViewById(R.id.icon_type);
            textTitle = itemView.findViewById(R.id.text_title);
            textMessage = itemView.findViewById(R.id.text_message);
            textTime = itemView.findViewById(R.id.text_time);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    NotificationResponse notification = notifications.get(getAdapterPosition());

                    // ✅ NEW: Handle promotion clicks differently
                    if (notification.isPromotion()) {
                        listener.onPromotionClick(notification);
                    } else {
                        listener.onNotificationClick(notification);
                    }
                }
            });

            // ✅ NEW: Add long click for promo code copy (using existing message TextView)
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    NotificationResponse notification = notifications.get(getAdapterPosition());
                    if (notification.hasPromoCode()) {
                        listener.onPromoCodeCopy(notification.getPromoCode());
                        return true;
                    }
                }
                return false;
            });
        }

        public void bind(NotificationResponse notification) {
            textTitle.setText(notification.getTitle());

            // ✅ NEW: Enhanced message display for promotions
            String displayMessage = notification.getMessage();
            if (notification.isPromotion() && notification.hasPromoCode()) {
                displayMessage += "\n🎟️ Code: " + notification.getPromoCode();
            }
            textMessage.setText(displayMessage);

            textTime.setText(notification.getFormattedTime());

            // Set icon based on type
            setNotificationIcon(notification.getType());

            // Show/hide unread indicator
            unreadIndicator.setVisibility(notification.isUnread() ?
                    View.VISIBLE : View.GONE);

            // ✅ NEW: Enhanced visual styling for different notification types
            if (notification.isUnread()) {
                itemView.setAlpha(1.0f);
                if (notification.isPromotion()) {
                    // Subtle promotion highlighting
                    itemView.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_orange_light));
                } else {
                    itemView.setBackgroundColor(itemView.getContext().getColor(android.R.color.white));
                }
            } else {
                itemView.setAlpha(0.7f);
                itemView.setBackgroundColor(itemView.getContext().getColor(android.R.color.transparent));
            }
        }

        // ✅ UPDATED: Enhanced notification icon setting with promotion support
        private void setNotificationIcon(NotificationResponse.NotificationType type) {
            switch (type) {
                case MESSAGE:
                    iconType.setImageResource(R.drawable.ic_message);
                    iconType.setColorFilter(itemView.getContext().getColor(android.R.color.holo_green_dark));
                    break;
                case OFFER:
                    iconType.setImageResource(R.drawable.ic_offer);
                    iconType.setColorFilter(itemView.getContext().getColor(android.R.color.holo_purple));
                    break;
                case TRANSACTION:
                    iconType.setImageResource(R.drawable.ic_notification);
                    iconType.setColorFilter(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                    break;
                case REVIEW:
                    iconType.setImageResource(R.drawable.ic_star);
                    iconType.setColorFilter(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                    break;
                case PROMOTION:         // ✅ NEW
                    iconType.setImageResource(R.drawable.ic_local_offer);
                    iconType.setColorFilter(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    break;
                case LISTING_UPDATE:    // ✅ NEW
                    iconType.setImageResource(R.drawable.ic_notification);
                    iconType.setColorFilter(itemView.getContext().getColor(android.R.color.holo_blue_light));
                    break;
                case GENERAL:
                default:
                    iconType.setImageResource(R.drawable.ic_notification);
                    iconType.setColorFilter(itemView.getContext().getColor(android.R.color.darker_gray));
                    break;
            }
        }
    }
}