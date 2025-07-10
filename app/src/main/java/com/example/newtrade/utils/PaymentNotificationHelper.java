// app/src/main/java/com/example/newtrade/utils/PaymentNotificationHelper.java
package com.example.newtrade.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.newtrade.R;
import com.example.newtrade.ui.payment.PaymentDetailActivity;
import com.example.newtrade.ui.payment.PaymentHistoryActivity;

public class PaymentNotificationHelper {

    private static final String CHANNEL_ID = "payment_notifications";
    private static final String CHANNEL_NAME = "Payment Notifications";
    private static final int PAYMENT_SUCCESS_ID = 1001;
    private static final int PAYMENT_FAILED_ID = 1002;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for payment status updates");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showPaymentSuccessNotification(Context context, Long transactionId, String amount) {
        SharedPrefsManager prefsManager = new SharedPrefsManager(context);
        if (!prefsManager.isPaymentNotificationsEnabled()) {
            return;
        }

        Intent intent = new Intent(context, PaymentDetailActivity.class);
        intent.putExtra(PaymentDetailActivity.EXTRA_TRANSACTION_ID, transactionId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_payment)
                .setContentTitle("Payment Successful! 🎉")
                .setContentText("Your payment of " + amount + " has been processed successfully")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(PAYMENT_SUCCESS_ID, builder.build());
    }

    public static void showPaymentFailedNotification(Context context, String reason) {
        SharedPrefsManager prefsManager = new SharedPrefsManager(context);
        if (!prefsManager.isPaymentNotificationsEnabled()) {
            return;
        }

        Intent intent = new Intent(context, PaymentHistoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_error)
                .setContentTitle("Payment Failed ❌")
                .setContentText("Payment failed: " + reason)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(PAYMENT_FAILED_ID, builder.build());
    }
}