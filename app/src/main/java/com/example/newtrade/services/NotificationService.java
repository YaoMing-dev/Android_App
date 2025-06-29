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
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.ui.offers.OffersActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class NotificationService extends FirebaseMessagingService {

    private static final String TAG = "NotificationService";

    // Notification channels
    private static final String CHANNEL_MESSAGES = "messages";
    private static final String CHANNEL_OFFERS = "offers";
    private static final String CHANNEL_LISTINGS = "listings";
    private static final String CHANNEL_PROMOTIONS = "promotions";

    // Notification types
    private static final String TYPE_MESSAGE = "message";
    private static final String TYPE_OFFER = "offer";
    private static final String TYPE_LISTING_UPDATE = "listing_update";
    private static final String TYPE_PROMOTION = "promotion";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Firebase token refreshed: " + token);

        // Save token to preferences and send to server
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(this);
        prefsManager.saveFcmToken(token);

        // Send token to backend for this user
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Get notification data
        Map<String, String> data = remoteMessage.getData();
        String notificationType = data.get("type");

        if (notificationType == null) {
            Log.w(TAG, "Notification type is null, ignoring");
            return;
        }

        // Handle different notification types
        switch (notificationType) {
            case TYPE_MESSAGE:
                handleMessageNotification(data, remoteMessage);
                break;
            case TYPE_OFFER:
                handleOfferNotification(data, remoteMessage);
                break;
            case TYPE_LISTING_UPDATE:
                handleListingUpdateNotification(data, remoteMessage);
                break;
            case TYPE_PROMOTION:
                handlePromotionNotification(data, remoteMessage);
                break;
            default:
                Log.w(TAG, "Unknown notification type: " + notificationType);
        }
    }

    private void handleMessageNotification(Map<String, String> data, RemoteMessage remoteMessage) {
        String conversationId = data.get("conversation_id");
        String senderName = data.get("sender_name");
        String messageContent = data.get("message_content");
        String otherUserId = data.get("other_user_id");

        String title = senderName != null ? senderName : "New Message";
        String body = messageContent != null ? messageContent : "You have a new message";

        // Create intent to open chat
        Intent intent = new Intent(this, ChatActivity.class);
        if (conversationId != null) {
            intent.putExtra("conversation_id", Long.parseLong(conversationId));
        }
        if (otherUserId != null) {
            intent.putExtra("other_user_id", Long.parseLong(otherUserId));
        }
        intent.putExtra("other_user_name", senderName);

        showNotification(CHANNEL_MESSAGES, title, body, intent, R.drawable.ic_message);

        Log.d(TAG, "✅ Message notification displayed");
    }

    private void handleOfferNotification(Map<String, String> data, RemoteMessage remoteMessage) {
        String offerType = data.get("offer_type"); // new_offer, offer_accepted, offer_rejected, counter_offer
        String productTitle = data.get("product_title");
        String offerAmount = data.get("offer_amount");
        String buyerName = data.get("buyer_name");
        String sellerName = data.get("seller_name");

        String title, body;

        switch (offerType != null ? offerType : "") {
            case "new_offer":
                title = "New Offer Received";
                body = buyerName + " made an offer of $" + offerAmount + " for " + productTitle;
                break;
            case "offer_accepted":
                title = "Offer Accepted! 🎉";
                body = sellerName + " accepted your offer of $" + offerAmount + " for " + productTitle;
                break;
            case "offer_rejected":
                title = "Offer Declined";
                body = sellerName + " declined your offer for " + productTitle;
                break;
            case "counter_offer":
                title = "Counter Offer Received";
                body = sellerName + " sent a counter offer of $" + offerAmount + " for " + productTitle;
                break;
            default:
                title = "Offer Update";
                body = "You have an offer update for " + productTitle;
        }

        // Create intent to open offers
        Intent intent = new Intent(this, OffersActivity.class);

        showNotification(CHANNEL_OFFERS, title, body, intent, R.drawable.ic_offer);

        Log.d(TAG, "✅ Offer notification displayed: " + offerType);
    }

    private void handleListingUpdateNotification(Map<String, String> data, RemoteMessage remoteMessage) {
        String updateType = data.get("update_type"); // item_sold, price_drop, back_in_stock
        String productId = data.get("product_id");
        String productTitle = data.get("product_title");
        String newPrice = data.get("new_price");

        String title, body;

        switch (updateType != null ? updateType : "") {
            case "item_sold":
                title = "Item Sold! 🎉";
                body = "Your listing '" + productTitle + "' has been sold";
                break;
            case "price_drop":
                title = "Price Drop Alert";
                body = productTitle + " is now $" + newPrice;
                break;
            case "back_in_stock":
                title = "Back in Stock";
                body = productTitle + " is available again";
                break;
            default:
                title = "Listing Update";
                body = "Your listing has been updated";
        }

        // Create intent to open product detail
        Intent intent = new Intent(this, ProductDetailActivity.class);
        if (productId != null) {
            intent.putExtra("product_id", Long.parseLong(productId));
        }

        showNotification(CHANNEL_LISTINGS, title, body, intent, R.drawable.ic_notification);

        Log.d(TAG, "✅ Listing update notification displayed: " + updateType);
    }

    private void handlePromotionNotification(Map<String, String> data, RemoteMessage remoteMessage) {
        String title = data.get("title");
        String body = data.get("body");
        String actionUrl = data.get("action_url");

        if (title == null) title = "Special Offer";
        if (body == null) body = "Check out our latest deals!";

        // Create intent to open main activity
        Intent intent = new Intent(this, MainActivity.class);
        if (actionUrl != null) {
            intent.putExtra("action_url", actionUrl);
        }

        showNotification(CHANNEL_PROMOTIONS, title, body, intent, R.drawable.ic_promotion);

        Log.d(TAG, "✅ Promotion notification displayed");
    }

    private void showNotification(String channelId, String title, String body, Intent intent, int iconRes) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create pending intent
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            (int) System.currentTimeMillis(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show notification
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Messages channel
            NotificationChannel messagesChannel = new NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            );
            messagesChannel.setDescription("New chat messages");
            messagesChannel.enableVibration(true);
            notificationManager.createNotificationChannel(messagesChannel);

            // Offers channel
            NotificationChannel offersChannel = new NotificationChannel(
                CHANNEL_OFFERS,
                "Offers",
                NotificationManager.IMPORTANCE_HIGH
            );
            offersChannel.setDescription("Price offers and negotiations");
            offersChannel.enableVibration(true);
            notificationManager.createNotificationChannel(offersChannel);

            // Listings channel
            NotificationChannel listingsChannel = new NotificationChannel(
                CHANNEL_LISTINGS,
                "Listing Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            listingsChannel.setDescription("Updates about your listings");
            notificationManager.createNotificationChannel(listingsChannel);

            // Promotions channel
            NotificationChannel promotionsChannel = new NotificationChannel(
                CHANNEL_PROMOTIONS,
                "Promotions",
                NotificationManager.IMPORTANCE_LOW
            );
            promotionsChannel.setDescription("Special offers and promotions");
            notificationManager.createNotificationChannel(promotionsChannel);

            Log.d(TAG, "✅ Notification channels created");
        }
    }

    private void sendTokenToServer(String token) {
        // TODO: Implement API call to send FCM token to backend
        // This would typically be done via ApiClient
        Log.d(TAG, "TODO: Send FCM token to server: " + token);
    }
}
