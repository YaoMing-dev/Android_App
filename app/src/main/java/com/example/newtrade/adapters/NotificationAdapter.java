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
                    listener.onNotificationClick(notifications.get(getAdapterPosition()));
                }
            });
        }

        public void bind(NotificationResponse notification) {
            textTitle.setText(notification.getTitle());
            textMessage.setText(notification.getMessage());
            textTime.setText(notification.getFormattedTime());

            // Set icon based on type
            setNotificationIcon(notification.getType());

            // Show/hide unread indicator
            unreadIndicator.setVisibility(notification.isUnread() ?
                    View.VISIBLE : View.GONE);

            // Set background tint for unread notifications
            itemView.setAlpha(notification.isUnread() ? 1.0f : 0.7f);
        }

        private void setNotificationIcon(NotificationResponse.NotificationType type) {
            switch (type) {
                case MESSAGE:
                    iconType.setImageResource(R.drawable.ic_message);
                    break;
                case OFFER:
                    iconType.setImageResource(R.drawable.ic_offer);
                    break;
                case TRANSACTION:
                    iconType.setImageResource(R.drawable.ic_transaction);
                    break;
                case REVIEW:
                    iconType.setImageResource(R.drawable.ic_star);
                    break;
                case GENERAL:
                default:
                    iconType.setImageResource(R.drawable.ic_notification);
                    break;
            }
        }
    }
}