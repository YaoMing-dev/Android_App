// app/src/main/java/com/example/newtrade/utils/NotificationPreferencesHelper.java
package com.example.newtrade.utils;

import android.content.Context;
import android.util.Log;

import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.NotificationService;
import com.example.newtrade.models.StandardResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationPreferencesHelper {

    private static final String TAG = "NotificationPrefs";

    // ===== PREFERENCE KEYS =====
    public static final String PREF_PUSH_ENABLED = "pushEnabled";
    public static final String PREF_MESSAGE_NOTIFICATIONS = "messageNotifications";
    public static final String PREF_OFFER_NOTIFICATIONS = "offerNotifications";
    public static final String PREF_PROMOTION_NOTIFICATIONS = "promotionNotifications";
    public static final String PREF_LISTING_NOTIFICATIONS = "listingNotifications";
    public static final String PREF_TRANSACTION_NOTIFICATIONS = "transactionNotifications";

    public interface PreferencesCallback {
        void onSuccess(Map<String, Boolean> preferences);
        void onError(String error);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }

    // ===== GET PREFERENCES =====
    public static void getNotificationPreferences(Context context, PreferencesCallback callback) {
        try {
            NotificationService notificationService = ApiClient.getNotificationService();
            Call<StandardResponse<Map<String, Object>>> call = notificationService.getNotificationPreferences();

            call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                       Response<StandardResponse<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> standardResponse = response.body();
                        if (standardResponse.isSuccess()) {
                            Map<String, Object> data = standardResponse.getData();
                            Map<String, Boolean> preferences = parsePreferences(data);

                            // Save locally
                            savePreferencesLocally(context, preferences);

                            Log.d(TAG, "✅ Preferences retrieved successfully");
                            if (callback != null) {
                                callback.onSuccess(preferences);
                            }
                        } else {
                            Log.e(TAG, "❌ Server error: " + standardResponse.getMessage());
                            if (callback != null) {
                                callback.onError(standardResponse.getMessage());
                            }
                        }
                    } else {
                        Log.e(TAG, "❌ HTTP error: " + response.code());
                        if (callback != null) {
                            callback.onError("HTTP error: " + response.code());
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                    Log.e(TAG, "❌ Network error getting preferences", t);

                    // Fallback to local preferences
                    Map<String, Boolean> localPrefs = getLocalPreferences(context);
                    if (callback != null) {
                        callback.onSuccess(localPrefs);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception getting preferences", e);
            if (callback != null) {
                callback.onError("Exception: " + e.getMessage());
            }
        }
    }

    // ===== UPDATE PREFERENCES =====
    public static void updateNotificationPreferences(Context context, Map<String, Boolean> preferences,
                                                     UpdateCallback callback) {
        try {
            NotificationService notificationService = ApiClient.getNotificationService();

            // Convert to Map<String, Object> for API
            Map<String, Object> requestMap = new HashMap<>();
            for (Map.Entry<String, Boolean> entry : preferences.entrySet()) {
                requestMap.put(entry.getKey(), entry.getValue());
            }

            Call<StandardResponse<String>> call = notificationService.updateNotificationPreferences(requestMap);

            call.enqueue(new Callback<StandardResponse<String>>() {
                @Override
                public void onResponse(Call<StandardResponse<String>> call,
                                       Response<StandardResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<String> standardResponse = response.body();
                        if (standardResponse.isSuccess()) {
                            // Save locally
                            savePreferencesLocally(context, preferences);

                            Log.d(TAG, "✅ Preferences updated successfully");
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        } else {
                            Log.e(TAG, "❌ Server error: " + standardResponse.getMessage());
                            if (callback != null) {
                                callback.onError(standardResponse.getMessage());
                            }
                        }
                    } else {
                        Log.e(TAG, "❌ HTTP error: " + response.code());
                        if (callback != null) {
                            callback.onError("HTTP error: " + response.code());
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                    Log.e(TAG, "❌ Network error updating preferences", t);

                    // Save locally anyway
                    savePreferencesLocally(context, preferences);

                    if (callback != null) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception updating preferences", e);
            if (callback != null) {
                callback.onError("Exception: " + e.getMessage());
            }
        }
    }

    // ===== INDIVIDUAL PREFERENCE METHODS =====

    public static void enablePushNotifications(Context context, boolean enabled, UpdateCallback callback) {
        Map<String, Boolean> preferences = new HashMap<>();
        preferences.put(PREF_PUSH_ENABLED, enabled);
        updateNotificationPreferences(context, preferences, callback);
    }

    public static void enableMessageNotifications(Context context, boolean enabled, UpdateCallback callback) {
        Map<String, Boolean> preferences = new HashMap<>();
        preferences.put(PREF_MESSAGE_NOTIFICATIONS, enabled);
        updateNotificationPreferences(context, preferences, callback);
    }

    public static void enableOfferNotifications(Context context, boolean enabled, UpdateCallback callback) {
        Map<String, Boolean> preferences = new HashMap<>();
        preferences.put(PREF_OFFER_NOTIFICATIONS, enabled);
        updateNotificationPreferences(context, preferences, callback);
    }

    public static void enablePromotionNotifications(Context context, boolean enabled, UpdateCallback callback) {
        Map<String, Boolean> preferences = new HashMap<>();
        preferences.put(PREF_PROMOTION_NOTIFICATIONS, enabled);
        updateNotificationPreferences(context, preferences, callback);
    }

    public static void enableListingNotifications(Context context, boolean enabled, UpdateCallback callback) {
        Map<String, Boolean> preferences = new HashMap<>();
        preferences.put(PREF_LISTING_NOTIFICATIONS, enabled);
        updateNotificationPreferences(context, preferences, callback);
    }

    // ===== LOCAL STORAGE METHODS =====

    private static void savePreferencesLocally(Context context, Map<String, Boolean> preferences) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);

        for (Map.Entry<String, Boolean> entry : preferences.entrySet()) {
            prefsManager.saveBoolean(entry.getKey(), entry.getValue());
        }

        Log.d(TAG, "✅ Preferences saved locally");
    }

    public static Map<String, Boolean> getLocalPreferences(Context context) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);

        Map<String, Boolean> preferences = new HashMap<>();
        preferences.put(PREF_PUSH_ENABLED, prefsManager.getBoolean(PREF_PUSH_ENABLED, true));
        preferences.put(PREF_MESSAGE_NOTIFICATIONS, prefsManager.getBoolean(PREF_MESSAGE_NOTIFICATIONS, true));
        preferences.put(PREF_OFFER_NOTIFICATIONS, prefsManager.getBoolean(PREF_OFFER_NOTIFICATIONS, true));
        preferences.put(PREF_PROMOTION_NOTIFICATIONS, prefsManager.getBoolean(PREF_PROMOTION_NOTIFICATIONS, true));
        preferences.put(PREF_LISTING_NOTIFICATIONS, prefsManager.getBoolean(PREF_LISTING_NOTIFICATIONS, true));
        preferences.put(PREF_TRANSACTION_NOTIFICATIONS, prefsManager.getBoolean(PREF_TRANSACTION_NOTIFICATIONS, true));

        return preferences;
    }

    // ===== UTILITY METHODS =====

    private static Map<String, Boolean> parsePreferences(Map<String, Object> data) {
        Map<String, Boolean> preferences = new HashMap<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Boolean) {
                preferences.put(entry.getKey(), (Boolean) value);
            } else {
                // Default to true if parsing fails
                preferences.put(entry.getKey(), true);
            }
        }

        return preferences;
    }

    public static boolean isNotificationTypeEnabled(Context context, String notificationType) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);

        // Check if push notifications are globally enabled
        if (!prefsManager.getBoolean(PREF_PUSH_ENABLED, true)) {
            return false;
        }

        switch (notificationType.toUpperCase()) {
            case "MESSAGE":
                return prefsManager.getBoolean(PREF_MESSAGE_NOTIFICATIONS, true);
            case "OFFER":
                return prefsManager.getBoolean(PREF_OFFER_NOTIFICATIONS, true);
            case "PROMOTION":
                return prefsManager.getBoolean(PREF_PROMOTION_NOTIFICATIONS, true);
            case "LISTING_UPDATE":
                return prefsManager.getBoolean(PREF_LISTING_NOTIFICATIONS, true);
            case "TRANSACTION":
                return prefsManager.getBoolean(PREF_TRANSACTION_NOTIFICATIONS, true);
            default:
                return true;
        }
    }

    // ===== DEFAULT PREFERENCES =====
    public static Map<String, Boolean> getDefaultPreferences() {
        Map<String, Boolean> defaults = new HashMap<>();
        defaults.put(PREF_PUSH_ENABLED, true);
        defaults.put(PREF_MESSAGE_NOTIFICATIONS, true);
        defaults.put(PREF_OFFER_NOTIFICATIONS, true);
        defaults.put(PREF_PROMOTION_NOTIFICATIONS, true);
        defaults.put(PREF_LISTING_NOTIFICATIONS, true);
        defaults.put(PREF_TRANSACTION_NOTIFICATIONS, true);
        return defaults;
    }

    // ===== SYNC METHODS =====
    public static void syncPreferencesWithServer(Context context) {
        getNotificationPreferences(context, new PreferencesCallback() {
            @Override
            public void onSuccess(Map<String, Boolean> preferences) {
                Log.d(TAG, "✅ Preferences synced with server");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to sync preferences: " + error);
            }
        });
    }
}