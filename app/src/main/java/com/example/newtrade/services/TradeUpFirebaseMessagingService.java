// Cập nhật app/src/main/java/com/example/newtrade/services/TradeUpFirebaseMessagingService.java
package com.example.newtrade.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.activities.NotificationActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class TradeUpFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    // Multiple notification channels for different types
    private static final String CHANNEL_MESSAGES = "tradeup_messages";
    private static final String CHANNEL_OFFERS = "tradeup_offers";
    private static final String CHANNEL_TRANSACTIONS = "tradeup_transactions";
    private static final String CHANNEL_GENERAL = "tradeup_general";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "🔔 FCM Message received from: " + remoteMessage.getFrom());
        Log.d(TAG, "🔔 FCM Message ID: " + remoteMessage.getMessageId());
        Log.d(TAG, "🔔 FCM Message data: " + remoteMessage.getData());

        // Check if message contains a data payload
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "🔔 FCM Data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage);
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "🔔 FCM Notification title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "🔔 FCM Notification body: " + remoteMessage.getNotification().getBody());

            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    remoteMessage.getData()
            );
        }

        // ✅ ALWAYS SHOW NOTIFICATION FOR DEBUGGING
        if (remoteMessage.getData().isEmpty() && remoteMessage.getNotification() == null) {
            Log.d(TAG, "🔔 Empty FCM message received - showing debug notification");
            showNotification("FCM Test", "Empty FCM message received", remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "🎯 FCM New token received: " + token);

        // Save token locally
        SharedPrefsManager.getInstance(this).saveFcmToken(token);

        // TODO: Send token to server
        // You should send this token to your server for sending notifications
        Log.d(TAG, "🎯 FCM Token should be sent to server: " + token);
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {
        String type = remoteMessage.getData().get("type");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String referenceId = remoteMessage.getData().get("referenceId");

        Log.d(TAG, "🔔 Handling data message - Type: " + type + ", Title: " + title);

        if (title != null && body != null) {
            showNotification(title, body, remoteMessage.getData());
        } else {
            // Default notification if no title/body
            showNotification("New Notification", "You have a new notification", remoteMessage.getData());
        }
    }

    private void showNotification(String title, String body, java.util.Map<String, String> data) {
        String notificationType = data.get("type");
        String referenceId = data.get("referenceId");

        Log.d(TAG, "🔔 Showing notification - Title: " + title + ", Body: " + body + ", Type: " + notificationType);

        // Create appropriate notification channel
        String channelId = getChannelForType(notificationType);
        createNotificationChannel(channelId, notificationType);

        // Create appropriate intent based on notification type
        Intent intent = createIntentForNotification(notificationType, referenceId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                generateNotificationId(notificationType, referenceId),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification with appropriate styling
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification) // Make sure this exists
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(getPriorityForType(notificationType))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis());

        // Add action buttons based on type
        addNotificationActions(notificationBuilder, notificationType, referenceId);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = generateNotificationId(notificationType, referenceId);
            notificationManager.notify(notificationId, notificationBuilder.build());
            Log.d(TAG, "✅ Notification displayed with ID: " + notificationId);
        } else {
            Log.e(TAG, "❌ NotificationManager is null");
        }
    }

    private String getChannelForType(String type) {
        if (type == null) return CHANNEL_GENERAL;

        switch (type.toUpperCase()) {
            case "MESSAGE":
                return CHANNEL_MESSAGES;
            case "OFFER":
                return CHANNEL_OFFERS;
            case "TRANSACTION":
            case "LISTING_UPDATE":
                return CHANNEL_TRANSACTIONS;
            default:
                return CHANNEL_GENERAL;
        }
    }

    private void createNotificationChannel(String channelId, String type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name;
            String description;
            int importance;

            switch (channelId) {
                case CHANNEL_MESSAGES:
                    name = "New Messages";
                    description = "Notifications for new messages";
                    importance = NotificationManager.IMPORTANCE_HIGH;
                    break;
                case CHANNEL_OFFERS:
                    name = "Price Offers";
                    description = "Notifications for price offers";
                    importance = NotificationManager.IMPORTANCE_DEFAULT;
                    break;
                case CHANNEL_TRANSACTIONS:
                    name = "Listing Updates";
                    description = "Notifications for transaction and listing updates";
                    importance = NotificationManager.IMPORTANCE_DEFAULT;
                    break;
                default:
                    name = "General Notifications";
                    description = "General app notifications";
                    importance = NotificationManager.IMPORTANCE_DEFAULT;
                    break;
            }

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Notification channel created: " + channelId);
            }
        }
    }

    private Intent createIntentForNotification(String type, String referenceId) {
        Intent intent;

        if (type != null) {
            switch (type.toUpperCase()) {
                case "MESSAGE":
                    // Navigate to chat activity
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("navigate_to", "chat");
                    intent.putExtra("conversation_id", referenceId);
                    break;
                case "OFFER":
                    // Navigate to offer details
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("navigate_to", "offers");
                    intent.putExtra("offer_id", referenceId);
                    break;
                case "TRANSACTION":
                case "LISTING_UPDATE":
                    // Navigate to transaction details
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("navigate_to", "transactions");
                    intent.putExtra("transaction_id", referenceId);
                    break;
                default:
                    intent = new Intent(this, NotificationActivity.class);
                    break;
            }
        } else {
            intent = new Intent(this, NotificationActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private int getPriorityForType(String type) {
        if (type == null) return NotificationCompat.PRIORITY_DEFAULT;

        switch (type.toUpperCase()) {
            case "MESSAGE":
                return NotificationCompat.PRIORITY_HIGH;
            case "OFFER":
                return NotificationCompat.PRIORITY_DEFAULT;
            case "TRANSACTION":
            case "LISTING_UPDATE":
                return NotificationCompat.PRIORITY_DEFAULT;
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
        }
    }

    private void addNotificationActions(NotificationCompat.Builder builder, String type, String referenceId) {
        if (type == null) return;

        switch (type.toUpperCase()) {
            case "MESSAGE":
                // Add quick reply action
                Intent replyIntent = new Intent(this, MainActivity.class);
                replyIntent.putExtra("action", "quick_reply");
                replyIntent.putExtra("conversation_id", referenceId);

                PendingIntent replyPendingIntent = PendingIntent.getActivity(
                        this, 1, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                builder.addAction(R.drawable.ic_notification, "Reply", replyPendingIntent);
                break;

            case "OFFER":
                // Add view action for offers
                Intent viewIntent = new Intent(this, MainActivity.class);
                viewIntent.putExtra("action", "view_offer");
                viewIntent.putExtra("offer_id", referenceId);

                PendingIntent viewPendingIntent = PendingIntent.getActivity(
                        this, 2, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                builder.addAction(R.drawable.ic_notification, "View Offer", viewPendingIntent);
                break;
        }
    }

    private int generateNotificationId(String type, String referenceId) {
        // Generate unique notification ID based on type and reference
        if (referenceId != null && type != null) {
            return (type + referenceId).hashCode();
        } else if (type != null) {
            return type.hashCode();
        }
        return (int) System.currentTimeMillis();
    }
}