// app/src/main/java/com/example/newtrade/utils/PromotionNotificationHelper.java
package com.example.newtrade.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.NotificationService;
import com.example.newtrade.models.StandardResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromotionNotificationHelper {

    private static final String TAG = "PromotionHelper";

    // ===== ✅ SCHEDULING VARIABLES =====
    private static Handler promotionHandler;
    private static Runnable promotionRunnable;
    private static boolean isSchedulingActive = false;

    // ✅ Thời gian interval: 5 phút = 5 * 60 * 1000 ms
    private static final long PROMOTION_INTERVAL_MS = 5 * 60 * 1000; // 5 phút

    // ✅ Danh sách test user IDs
    private static final List<Long> TEST_USER_IDS = Arrays.asList(1L, 2L, 3L, 4L, 5L);

    // ✅ Random promotion data
    private static final String[] PROMO_CODES = {
            "FLASH20", "SUMMER15", "WELCOME10", "DEAL25", "SAVE30", "HOT10", "MEGA50", "COOL40"
    };

    private static final String[] DISCOUNTS = {
            "10%", "15%", "20%", "25%", "30%", "35%", "40%"
    };

    private static final String[] SEASONS = {
            "Summer", "Winter", "Spring", "Holiday", "Weekend", "Flash", "Mega", "Super"
    };

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

    // ===== ✅ NEW: AUTOMATIC SCHEDULING - MỖI 5 PHÚT =====

    /**
     * ✅ BẮT ĐẦU SCHEDULE - Mỗi 5 phút gửi 1 promotion ngẫu nhiên
     */
    public static void startRandomPromotionScheduling(Context context) {
        Log.d(TAG, "🎯 Starting random promotion scheduling every 5 minutes...");

        if (isSchedulingActive) {
            Log.w(TAG, "⚠️ Promotion scheduling is already active");
            return;
        }

        // ✅ Tạo Handler cho main thread
        promotionHandler = new Handler(Looper.getMainLooper());

        // ✅ Tạo Runnable để chạy promotion
        promotionRunnable = new Runnable() {
            @Override
            public void run() {
                // Gửi random promotion
                sendRandomPromotion(context);

                // Schedule lần tiếp theo sau 5 phút
                if (isSchedulingActive && promotionHandler != null) {
                    promotionHandler.postDelayed(this, PROMOTION_INTERVAL_MS);
                }
            }
        };

        // ✅ Bắt đầu schedule
        isSchedulingActive = true;

        // Gửi promotion đầu tiên ngay lập tức
        sendRandomPromotion(context);

        // Schedule lần tiếp theo sau 5 phút
        promotionHandler.postDelayed(promotionRunnable, PROMOTION_INTERVAL_MS);

        Log.d(TAG, "✅ Random promotion scheduling started successfully");
    }

    /**
     * ✅ DỪNG SCHEDULE
     */
    public static void stopRandomPromotionScheduling() {
        Log.d(TAG, "🛑 Stopping random promotion scheduling...");

        isSchedulingActive = false;

        if (promotionHandler != null && promotionRunnable != null) {
            promotionHandler.removeCallbacks(promotionRunnable);
        }

        promotionHandler = null;
        promotionRunnable = null;

        Log.d(TAG, "✅ Random promotion scheduling stopped");
    }

    /**
     * ✅ KIỂM TRA TRẠNG THÁI SCHEDULING
     */
    public static boolean isPromotionSchedulingActive() {
        return isSchedulingActive;
    }

    /**
     * ✅ GỬI RANDOM PROMOTION - Core logic
     */
    private static void sendRandomPromotion(Context context) {
        try {
            Random random = new Random();

            // ✅ Random chọn 1 trong 4 loại promotion
            int promotionType = random.nextInt(4);

            Log.d(TAG, "🎲 Sending random promotion type: " + promotionType);

            switch (promotionType) {
                case 0:
                    sendRandomFlashSale(context, random);
                    break;
                case 1:
                    sendRandomWelcome(context, random);
                    break;
                case 2:
                    sendRandomSeasonal(context, random);
                    break;
                case 3:
                    sendRandomLocation(context, random);
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending random promotion", e);
        }
    }

    /**
     * ✅ RANDOM FLASH SALE
     */
    private static void sendRandomFlashSale(Context context, Random random) {
        String discount = DISCOUNTS[random.nextInt(DISCOUNTS.length)];
        String promoCode = PROMO_CODES[random.nextInt(PROMO_CODES.length)];
        String duration = random.nextBoolean() ? "2 hours" : "3 hours";

        Log.d(TAG, "⚡ Sending Flash Sale: " + discount + " off, Code: " + promoCode);

        sendFlashSalePromotion(context, TEST_USER_IDS, discount, duration, promoCode);
    }

    /**
     * ✅ RANDOM WELCOME PROMOTION
     */
    private static void sendRandomWelcome(Context context, Random random) {
        Long randomUserId = TEST_USER_IDS.get(random.nextInt(TEST_USER_IDS.size()));
        String promoCode = PROMO_CODES[random.nextInt(PROMO_CODES.length)];

        Log.d(TAG, "🎉 Sending Welcome promotion to user: " + randomUserId + ", Code: " + promoCode);

        sendWelcomePromotion(context, randomUserId, promoCode);
    }

    /**
     * ✅ RANDOM SEASONAL PROMOTION
     */
    private static void sendRandomSeasonal(Context context, Random random) {
        String season = SEASONS[random.nextInt(SEASONS.length)];
        String discount = DISCOUNTS[random.nextInt(DISCOUNTS.length)];
        String promoCode = PROMO_CODES[random.nextInt(PROMO_CODES.length)];

        Log.d(TAG, "🌟 Sending " + season + " promotion: " + discount + " off, Code: " + promoCode);

        sendSeasonalPromotion(context, TEST_USER_IDS, season, discount, promoCode);
    }

    /**
     * ✅ RANDOM LOCATION-BASED PROMOTION
     */
    private static void sendRandomLocation(Context context, Random random) {
        String promoCode = PROMO_CODES[random.nextInt(PROMO_CODES.length)];
        String discount = DISCOUNTS[random.nextInt(DISCOUNTS.length)];

        // ✅ Random tọa độ xung quanh Hồ Chí Minh
        double latitude = 10.8231 + (random.nextDouble() - 0.5) * 0.1;
        double longitude = 106.6297 + (random.nextDouble() - 0.5) * 0.1;

        String[] storeNames = {"TradeUp Store", "Mega Mall", "Tech Center", "Shopping Plaza"};
        String storeName = storeNames[random.nextInt(storeNames.length)];

        Log.d(TAG, "📍 Sending Location promotion at " + storeName + ": " + discount + " off, Code: " + promoCode);

        sendNearbyStorePromotion(context, latitude, longitude, storeName, discount, promoCode);
    }

    /**
     * ✅ MANUAL TRIGGER - Gửi promotion ngay lập tức (để test)
     */
    public static void triggerRandomPromotionNow(Context context) {
        Log.d(TAG, "🚀 Manual trigger - sending random promotion now...");
        sendRandomPromotion(context);
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