// app/src/main/java/com/example/newtrade/services/TradeUpFirebaseMessagingService.java
package com.example.newtrade.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.activities.NotificationActivity;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.UserService;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TradeUpFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    // ✅ UPDATED: Multiple notification channels for different types - FR-4.2.1
    private static final String CHANNEL_MESSAGES = "tradeup_messages";
    private static final String CHANNEL_OFFERS = "tradeup_offers";
    private static final String CHANNEL_TRANSACTIONS = "tradeup_transactions";
    private static final String CHANNEL_PROMOTIONS = "tradeup_promotions";    // ✅ NEW
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

        // ALWAYS SHOW NOTIFICATION FOR DEBUGGING
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

        // Send token to server
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        try {
            UserService userService = ApiClient.getUserService();

            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("fcmToken", token);

            Call<StandardResponse<String>> call = userService.updateFcmToken(tokenRequest);

            call.enqueue(new Callback<StandardResponse<String>>() {
                @Override
                public void onResponse(Call<StandardResponse<String>> call,
                                       Response<StandardResponse<String>> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ FCM token sent to server from service");
                    } else {
                        Log.e(TAG, "❌ Failed to send FCM token from service: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                    Log.e(TAG, "❌ Error sending FCM token from service", t);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error in sendTokenToServer from service", e);
        }
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

        // ✅ Check user notification preferences
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(this);
        if (!shouldShowNotification(notificationType, prefsManager)) {
            Log.d(TAG, "🔕 Notification blocked by user preferences: " + notificationType);
            return;
        }

        // Create appropriate notification channel
        String channelId = getChannelForType(notificationType);
        createNotificationChannel(channelId, notificationType);

        // Create appropriate intent based on notification type
        Intent intent = createIntentForNotification(notificationType, referenceId, data);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                generateNotificationId(notificationType, referenceId),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // ✅ Build notification with enhanced styling for different types
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(getIconForType(notificationType))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(getPriorityForType(notificationType))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setColor(getColorForType(notificationType));

        // ✅ Add promotion-specific features
        if ("PROMOTION".equals(notificationType)) {
            addPromotionFeatures(notificationBuilder, data);
        }

        // Add action buttons based on type
        addNotificationActions(notificationBuilder, notificationType, referenceId, data);

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

    // ✅ NEW: Check if notification should be shown based on user preferences
    private boolean shouldShowNotification(String type, SharedPrefsManager prefsManager) {
        if (!prefsManager.isNotificationsEnabled()) {
            return false;
        }

        if (type == null) return true;

        switch (type.toUpperCase()) {
            case "MESSAGE":
                return prefsManager.isChatNotificationsEnabled();
            case "OFFER":
                return prefsManager.getBoolean("offer_notifications_enabled", true);
            case "PROMOTION":
                return prefsManager.getBoolean("promotion_notifications_enabled", true);
            case "LISTING_UPDATE":
            case "TRANSACTION":
                return prefsManager.getBoolean("listing_notifications_enabled", true);
            default:
                return true;
        }
    }

    // ✅ UPDATED: Get channel for notification type including promotions
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
            case "PROMOTION":               // ✅ NEW
                return CHANNEL_PROMOTIONS;  // ✅ NEW
            default:
                return CHANNEL_GENERAL;
        }
    }

    // ✅ UPDATED: Create notification channels including promotions
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
                case CHANNEL_PROMOTIONS:    // ✅ NEW
                    name = "Promotions & Deals";    // ✅ NEW
                    description = "Promotional offers and discounts";  // ✅ NEW
                    importance = NotificationManager.IMPORTANCE_DEFAULT; // ✅ NEW
                    break;  // ✅ NEW
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

            // ✅ NEW: Special settings for promotions
            if (channelId.equals(CHANNEL_PROMOTIONS)) {
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 250, 250, 250});
            }

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Notification channel created: " + channelId);
            }
        }
    }

    // ✅ NEW: Get icon for notification type
    private int getIconForType(String type) {
        if (type == null) return R.drawable.ic_notification;

        switch (type.toUpperCase()) {
            case "MESSAGE":
                return R.drawable.ic_message;
            case "OFFER":
                return R.drawable.ic_local_offer;
            case "TRANSACTION":
            case "LISTING_UPDATE":
                return R.drawable.ic_receipt;
            case "PROMOTION":
                return R.drawable.ic_local_offer; // Use offer icon or create new one
            default:
                return R.drawable.ic_notification;
        }
    }

    // ✅ NEW: Get color for notification type
    private int getColorForType(String type) {
        if (type == null) return Color.BLUE;

        switch (type.toUpperCase()) {
            case "MESSAGE":
                return Color.GREEN;
            case "OFFER":
                return Color.MAGENTA;
            case "TRANSACTION":
            case "LISTING_UPDATE":
                return Color.BLUE;
            case "PROMOTION":
                return Color.RED; // Eye-catching color for promotions
            default:
                return Color.BLUE;
        }
    }

    // ✅ NEW: Add promotion-specific features
    private void addPromotionFeatures(NotificationCompat.Builder builder, Map<String, String> data) {
        String promoCode = data.get("promoCode");
        String promotionType = data.get("promotionType");

        // ✅ FIX: Remove setLargeIcon to avoid ambiguous method call
        // builder.setLargeIcon(null); // Remove this line

        // Add promotional styling
        if (promoCode != null && !promoCode.isEmpty()) {
            builder.setSubText("Promo Code: " + promoCode);
        }

        // Set high priority for limited-time promotions
        if ("FLASH_SALE".equals(promotionType) || "LIMITED_TIME".equals(promotionType)) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
    }

    // ✅ UPDATED: Create intent for notification with enhanced routing
    private Intent createIntentForNotification(String type, String referenceId, Map<String, String> data) {
        Intent intent;

        if (type != null) {
            switch (type.toUpperCase()) {
                case "MESSAGE":
                    // Navigate to chat activity
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("navigate_to", "chat");
                    intent.putExtra("conversation_id", referenceId);
                    intent.putExtra("sender_name", data.get("senderName"));
                    break;
                case "OFFER":
                    // Navigate to offer details
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("navigate_to", "offers");
                    intent.putExtra("offer_id", referenceId);
                    intent.putExtra("buyer_name", data.get("buyerName"));
                    break;
                case "TRANSACTION":
                case "LISTING_UPDATE":
                    // Navigate to transaction details
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("navigate_to", "listings");
                    intent.putExtra("product_id", referenceId);
                    break;
                case "PROMOTION":   // ✅ NEW
                    // Navigate to promotions/deals section
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("navigate_to", "promotions");
                    intent.putExtra("promotion_type", data.get("promotionType"));
                    intent.putExtra("promo_code", data.get("promoCode"));
                    break;
                default:
                    intent = new Intent(this, NotificationActivity.class);
                    break;
            }
        } else {
            intent = new Intent(this, NotificationActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("notification_type", type);
        intent.putExtra("reference_id", referenceId);

        return intent;
    }

    private int getPriorityForType(String type) {
        if (type == null) return NotificationCompat.PRIORITY_DEFAULT;

        switch (type.toUpperCase()) {
            case "MESSAGE":
                return NotificationCompat.PRIORITY_HIGH;
            case "OFFER":
                return NotificationCompat.PRIORITY_DEFAULT;
            case "PROMOTION":
                return NotificationCompat.PRIORITY_DEFAULT;
            case "TRANSACTION":
            case "LISTING_UPDATE":
                return NotificationCompat.PRIORITY_DEFAULT;
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
        }
    }

    // ✅ UPDATED: Add notification actions including promotion actions
    private void addNotificationActions(NotificationCompat.Builder builder, String type,
                                        String referenceId, Map<String, String> data) {
        if (type == null) return;

        switch (type.toUpperCase()) {
            case "MESSAGE":
                // Add quick reply action
                Intent replyIntent = new Intent(this, MainActivity.class);
                replyIntent.putExtra("action", "quick_reply");
                replyIntent.putExtra("conversation_id", referenceId);

                PendingIntent replyPendingIntent = PendingIntent.getActivity(
                        this, 1, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                builder.addAction(R.drawable.ic_reply, "Reply", replyPendingIntent);
                break;

            case "OFFER":
                // Add view action for offers
                Intent viewIntent = new Intent(this, MainActivity.class);
                viewIntent.putExtra("action", "view_offer");
                viewIntent.putExtra("offer_id", referenceId);

                PendingIntent viewPendingIntent = PendingIntent.getActivity(
                        this, 2, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                builder.addAction(R.drawable.ic_visibility, "View Offer", viewPendingIntent);
                break;

            case "PROMOTION":   // ✅ NEW
                // Add "View Deal" action for promotions
                Intent promoIntent = new Intent(this, MainActivity.class);
                promoIntent.putExtra("action", "view_promotion");
                promoIntent.putExtra("promotion_type", data.get("promotionType"));
                promoIntent.putExtra("promo_code", data.get("promoCode"));

                PendingIntent promoPendingIntent = PendingIntent.getActivity(
                        this, 3, promoIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                builder.addAction(R.drawable.ic_local_offer, "View Deal", promoPendingIntent);

                // ✅ Add "Copy Code" action if promo code exists
                String promoCode = data.get("promoCode");
                if (promoCode != null && !promoCode.isEmpty()) {
                    Intent copyIntent = new Intent(this, MainActivity.class);
                    copyIntent.putExtra("action", "copy_promo_code");
                    copyIntent.putExtra("promo_code", promoCode);

                    PendingIntent copyPendingIntent = PendingIntent.getActivity(
                            this, 4, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    builder.addAction(R.drawable.ic_content_copy, "Copy Code", copyPendingIntent);
                }
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