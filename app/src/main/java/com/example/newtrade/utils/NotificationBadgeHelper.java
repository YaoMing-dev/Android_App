// app/src/main/java/com/example/newtrade/utils/NotificationBadgeHelper.java
package com.example.newtrade.utils;

import android.content.Context;
import android.util.Log;

import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.NotificationService;
import com.example.newtrade.models.StandardResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationBadgeHelper {

    private static final String TAG = "NotificationBadge";

    public interface BadgeUpdateListener {
        void onBadgeUpdated(long count);
    }

    public static void updateBadgeCount(Context context, BadgeUpdateListener listener) {
        try {
            NotificationService notificationService = ApiClient.getNotificationService();

            // ✅ UPDATED: Sử dụng Map<String, Object> response
            Call<StandardResponse<Map<String, Object>>> call = notificationService.getUnreadCount();

            call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                       Response<StandardResponse<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> standardResponse = response.body();
                        if (standardResponse.isSuccess()) {
                            Map<String, Object> data = standardResponse.getData();

                            // ✅ UPDATED: Parse unread count from Map
                            long unreadCount = NotificationResponseHelper.parseUnreadCount(data);

                            if (listener != null) {
                                listener.onBadgeUpdated(unreadCount);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                    Log.e(TAG, "Error getting unread count", t);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error updating badge count", e);
        }
    }
}