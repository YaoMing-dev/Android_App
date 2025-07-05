// app/src/main/java/com/example/newtrade/services/FirebaseMessagingService.java
package com.example.newtrade.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingService";
    private static final String CHANNEL_ID = "tradeup_notifications";
    private static final String CHANNEL_NAME = "TradeUp Notifications";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "🔔 FCM Message received from: " + remoteMessage.getFrom());

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message notification body: " + remoteMessage.getNotification().getBody());
            handleNotificationMessage(remoteMessage.getNotification());
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "✅ FCM Token refreshed: " + token);

        // Save token locally
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(this);
        prefsManager.saveFcmToken(token);

        // Send token to server if user is logged in
        if (prefsManager.isLoggedIn()) {
            sendTokenToServer(token);
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String notificationType = data.get("type");
        String title = data.get("title");
        String body = data.get("body");

        if (notificationType == null || title == null || body == null) {
            Log.w(TAG, "❌ Invalid notification data");
            return;
        }

        // Create notification based on type
        switch (notificationType) {
            case Constants.NOTIFICATION_TYPE_MESSAGE:
                handleNewMessageNotification(data, title, body);
                break;
            case Constants.NOTIFICATION_TYPE_OFFER:
                handleOfferNotification(data, title, body);
                break;
            case Constants.NOTIFICATION_TYPE_TRANSACTION:
                handleTransactionNotification(data, title, body);
                break;
            case Constants.NOTIFICATION_TYPE_REVIEW:
                handleReviewNotification(data, title, body);
                break;
            case Constants.NOTIFICATION_TYPE_GENERAL:
                handleGeneralNotification(data, title, body);
                break;
            default:
                Log.w(TAG, "Unknown notification type: " + notificationType);
        }
    }

    private void handleNotificationMessage(RemoteMessage.Notification notification) {
        String title = notification.getTitle();
        String body = notification.getBody();

        if (title != null && body != null) {
            showNotification(title, body, MainActivity.class, null);
        }
    }

    private void handleNewMessageNotification(Map<String, String> data, String title, String body) {
        String conversationId = data.get("conversationId");

        Intent intent = new Intent(this, ChatActivity.class);
        if (conversationId != null) {
            intent.putExtra(Constants.EXTRA_CONVERSATION_ID, Long.parseLong(conversationId));
        }

        showNotification(title, body, ChatActivity.class, intent);
    }

    private void handleOfferNotification(Map<String, String> data, String title, String body) {
        String productId = data.get("productId");

        Intent intent = new Intent(this, ProductDetailActivity.class);
        if (productId != null) {
            intent.putExtra(Constants.EXTRA_PRODUCT_ID, Long.parseLong(productId));
        }

        showNotification(title, body, ProductDetailActivity.class, intent);
    }

    private void handleTransactionNotification(Map<String, String> data, String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigate_to", "profile");

        showNotification(title, body, MainActivity.class, intent);
    }

    private void handleReviewNotification(Map<String, String> data, String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigate_to", "profile");

        showNotification(title, body, MainActivity.class, intent);
    }

    private void handleGeneralNotification(Map<String, String> data, String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        showNotification(title, body, MainActivity.class, intent);
    }

    private void showNotification(String title, String body, Class<?> activityClass, Intent customIntent) {
        Intent intent = customIntent != null ? customIntent : new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
        Log.d(TAG, "✅ Notification displayed: " + title);
    }

    private void sendTokenToServer(String token) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(this);
        Long userId = prefsManager.getUserId();

        if (userId == null) {
            Log.w(TAG, "❌ Cannot send token: User not logged in");
            return;
        }

        Map<String, Object> tokenRequest = new HashMap<>();
        tokenRequest.put("fcmToken", token);

        ApiClient.getUserService().updateFcmToken(userId, tokenRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Log.d(TAG, "✅ FCM token sent to server successfully");
                            } else {
                                Log.w(TAG, "❌ Failed to send FCM token: " + apiResponse.getMessage());
                            }
                        } else {
                            Log.w(TAG, "❌ Failed to send FCM token to server");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Network error sending FCM token", t);
                    }
                });
    }
}