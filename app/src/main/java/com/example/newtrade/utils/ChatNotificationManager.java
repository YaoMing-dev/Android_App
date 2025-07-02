// app/src/main/java/com/example/newtrade/utils/ChatNotificationManager.java
package com.example.newtrade.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.newtrade.R;
import com.example.newtrade.ui.chat.ChatActivity;

public class ChatNotificationManager {

    private static final String TAG = "ChatNotificationManager";
    private static final String CHANNEL_ID = "chat_messages";
    private static final String CHANNEL_NAME = "Chat Messages";
    private static final int NOTIFICATION_ID_BASE = 1000;

    private Context context;
    private NotificationManager notificationManager;

    public ChatNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new chat messages");
            channel.enableVibration(true);
            channel.setShowBadge(true);

            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "✅ Notification channel created");
        }
    }

    public void showMessageNotification(Long conversationId, String senderName, String message, String productTitle) {
        try {
            // Create intent to open chat
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("conversation_id", conversationId);
            chatIntent.putExtra("other_user_name", senderName);
            chatIntent.putExtra("product_title", productTitle);
            chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    conversationId.intValue(),
                    chatIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_message)
                    .setContentTitle(senderName)
                    .setContentText(message)
                    .setSubText(productTitle)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            // Show notification
            int notificationId = NOTIFICATION_ID_BASE + conversationId.intValue();
            notificationManager.notify(notificationId, builder.build());

            Log.d(TAG, "✅ Message notification shown for conversation: " + conversationId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing message notification", e);
        }
    }

    public void clearNotifications(Long conversationId) {
        try {
            int notificationId = NOTIFICATION_ID_BASE + conversationId.intValue();
            notificationManager.cancel(notificationId);
            Log.d(TAG, "✅ Cleared notifications for conversation: " + conversationId);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error clearing notifications", e);
        }
    }

    public void clearAllChatNotifications() {
        try {
            // Clear all chat notifications (you may need to track active conversations)
            notificationManager.cancelAll();
            Log.d(TAG, "✅ Cleared all chat notifications");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error clearing all notifications", e);
        }
    }
}