// app/src/main/java/com/example/newtrade/utils/PromotionNotificationHelper.java
package com.example.newtrade.utils;

import android.content.Context;
import android.util.Log;

import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.NotificationService;
import com.example.newtrade.models.StandardResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromotionNotificationHelper {

    private static final String TAG = "PromotionHelper";

    // ===== SINGLE USER PROMOTION =====
    public static void sendPromotionToUser(Context context, Long userId, String title,
                                           String message, String promoCode) {
        sendPromotionToUser(context, userId, title, message, "GENERAL", promoCode, null, null);
    }

    public static void sendPromotionToUser(Context context, Long userId, String title,
                                           String message, String promotionType, String promoCode,
                                           String imageUrl, PromotionCallback callback) {
        try {
            NotificationService notificationService = ApiClient.getNotificationService();

            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("title", title);
            request.put("message", message);
            request.put("promotionType", promotionType);
            request.put("promoCode", promoCode);
            request.put("imageUrl", imageUrl);

            Call<StandardResponse<String>> call = notificationService.sendPromotionNotification(request);

            call.enqueue(new Callback<StandardResponse<String>>() {
                @Override
                public void onResponse(Call<StandardResponse<String>> call,
                                       Response<StandardResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Promotion sent successfully");
                        if (callback != null) {
                            callback.onSuccess("Promotion sent successfully");
                        }
                    } else {
                        Log.e(TAG, "❌ Failed to send promotion: " + response.code());
                        if (callback != null) {
                            callback.onError("Failed to send promotion: " + response.code());
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                    Log.e(TAG, "❌ Error sending promotion", t);
                    if (callback != null) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception sending promotion", e);
            if (callback != null) {
                callback.onError("Exception: " + e.getMessage());
            }
        }
    }

    // ===== BULK PROMOTION =====
    public static void sendBulkPromotion(Context context, List<Long> userIds, String title,
                                         String message, String promoCode) {
        sendBulkPromotion(context, userIds, title, message, "BULK", promoCode, null);
    }

    public static void sendBulkPromotion(Context context, List<Long> userIds, String title,
                                         String message, String promotionType, String promoCode,
                                         PromotionCallback callback) {
        try {
            NotificationService notificationService = ApiClient.getNotificationService();

            Map<String, Object> request = new HashMap<>();
            request.put("userIds", userIds);
            request.put("title", title);
            request.put("message", message);
            request.put("promotionType", promotionType);
            request.put("promoCode", promoCode);

            Call<StandardResponse<String>> call = notificationService.sendBulkPromotionNotification(request);

            call.enqueue(new Callback<StandardResponse<String>>() {
                @Override
                public void onResponse(Call<StandardResponse<String>> call,
                                       Response<StandardResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Bulk promotion sent successfully to " + userIds.size() + " users");
                        if (callback != null) {
                            callback.onSuccess("Bulk promotion sent to " + userIds.size() + " users");
                        }
                    } else {
                        Log.e(TAG, "❌ Failed to send bulk promotion: " + response.code());
                        if (callback != null) {
                            callback.onError("Failed to send bulk promotion: " + response.code());
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                    Log.e(TAG, "❌ Error sending bulk promotion", t);
                    if (callback != null) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception sending bulk promotion", e);
            if (callback != null) {
                callback.onError("Exception: " + e.getMessage());
            }
        }
    }

    // ===== LOCATION-BASED PROMOTION =====
    public static void sendLocationBasedPromotion(Context context, Double latitude, Double longitude,
                                                  Double radiusKm, String title, String message,
                                                  String promoCode) {
        sendLocationBasedPromotion(context, latitude, longitude, radiusKm, title, message, promoCode, null);
    }

    public static void sendLocationBasedPromotion(Context context, Double latitude, Double longitude,
                                                  Double radiusKm, String title, String message,
                                                  String promoCode, PromotionCallback callback) {
        try {
            NotificationService notificationService = ApiClient.getNotificationService();

            Map<String, Object> request = new HashMap<>();
            request.put("latitude", latitude);
            request.put("longitude", longitude);
            request.put("radiusKm", radiusKm);
            request.put("title", title);
            request.put("message", message);
            request.put("promoCode", promoCode);

            Call<StandardResponse<String>> call = notificationService.sendLocationBasedPromotion(request);

            call.enqueue(new Callback<StandardResponse<String>>() {
                @Override
                public void onResponse(Call<StandardResponse<String>> call,
                                       Response<StandardResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Location-based promotion sent successfully");
                        if (callback != null) {
                            callback.onSuccess("Location-based promotion sent successfully");
                        }
                    } else {
                        Log.e(TAG, "❌ Failed to send location promotion: " + response.code());
                        if (callback != null) {
                            callback.onError("Failed to send location promotion: " + response.code());
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                    Log.e(TAG, "❌ Error sending location promotion", t);
                    if (callback != null) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception sending location promotion", e);
            if (callback != null) {
                callback.onError("Exception: " + e.getMessage());
            }
        }
    }

    // ===== PREDEFINED PROMOTION TEMPLATES =====

    /**
     * Send flash sale promotion
     */
    public static void sendFlashSalePromotion(Context context, List<Long> userIds,
                                              String discount, String duration, String promoCode) {
        String title = "⚡ Flash Sale Alert!";
        String message = "Limited time offer - " + discount + " off! Valid for " + duration + " only.";

        sendBulkPromotion(context, userIds, title, message, "FLASH_SALE", promoCode, null);
    }

    /**
     * Send welcome promotion for new users
     */
    public static void sendWelcomePromotion(Context context, Long userId, String promoCode) {
        String title = "🎉 Welcome to TradeUp!";
        String message = "Get started with a special discount on your first purchase!";

        sendPromotionToUser(context, userId, title, message, "WELCOME", promoCode, null, null);
    }

    /**
     * Send seasonal promotion
     */
    public static void sendSeasonalPromotion(Context context, List<Long> userIds,
                                             String season, String discount, String promoCode) {
        String title = "🌟 " + season + " Special Deals!";
        String message = "Celebrate " + season + " with " + discount + " off selected items!";

        sendBulkPromotion(context, userIds, title, message, "SEASONAL", promoCode, null);
    }

    /**
     * Send nearby store promotion
     */
    public static void sendNearbyStorePromotion(Context context, Double latitude, Double longitude,
                                                String storeName, String discount, String promoCode) {
        String title = "📍 " + storeName + " - Special Offer!";
        String message = "You're near " + storeName + "! Get " + discount + " off today only.";

        sendLocationBasedPromotion(context, latitude, longitude, 2.0, title, message, promoCode);
    }

    // ===== CALLBACK INTERFACE =====
    public interface PromotionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // ===== PROMOTION TRACKING =====

    /**
     * Track promotion engagement
     */
    public static void trackPromotionEngagement(Context context, String promoCode, String action) {
        Log.d(TAG, "📊 Promotion engagement - Code: " + promoCode + ", Action: " + action);

        // Save locally for analytics
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        String key = "promotion_engagement_" + System.currentTimeMillis();
        String value = promoCode + "|" + action + "|" + System.currentTimeMillis();
        prefsManager.saveString(key, value);

        // Send to analytics service (implement if needed)
        // AnalyticsHelper.trackEvent("promotion_engagement", params);
    }

    /**
     * Get promotion statistics
     */
    public static Map<String, Integer> getPromotionStats(Context context) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_sent", 0);
        stats.put("total_opened", 0);
        stats.put("total_used", 0);

        // Implement based on your analytics requirements
        return stats;
    }
}