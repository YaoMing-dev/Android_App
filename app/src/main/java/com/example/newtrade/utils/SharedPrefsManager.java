// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedPrefsManager {

    private static final String TAG = "SharedPrefsManager";
    private static SharedPrefsManager instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context);
        }
        return instance;
    }

    public SharedPrefsManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ===== USER DATA =====

    public void saveUserData(Long userId, String userName, String userEmail, String profilePicture) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Constants.PREF_USER_ID, userId != null ? userId : 0L);
        editor.putString(Constants.PREF_USER_NAME, userName);
        editor.putString(Constants.PREF_USER_EMAIL, userEmail);
        editor.putString(Constants.PREF_USER_PROFILE_PICTURE, profilePicture);
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.apply();

        Log.d(TAG, "✅ User data saved: " + userName + " (ID: " + userId + ")");
    }

    // ✅ THÊM METHOD saveUserSession
    public void saveUserSession(Long userId, String email, String displayName, boolean isEmailVerified) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Constants.PREF_USER_ID, userId != null ? userId : 0L);
        editor.putString(Constants.PREF_USER_NAME, displayName);
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.putBoolean(Constants.PREF_IS_EMAIL_VERIFIED, isEmailVerified);
        editor.apply();

        Log.d(TAG, "✅ User session saved: " + displayName + " (ID: " + userId + ")");
    }

    // ✅ THÊM METHOD clearUserSession
    public void clearUserSession() {
        clearUserData();
        Log.d(TAG, "✅ User session cleared");
    }

    // ✅ THÊM METHOD updateProfilePicture
    public void updateProfilePicture(String profilePictureUrl) {
        prefs.edit().putString(Constants.PREF_USER_PROFILE_PICTURE, profilePictureUrl).apply();
        Log.d(TAG, "✅ Profile picture updated: " + profilePictureUrl);
    }

    public Long getUserId() {
        long id = prefs.getLong(Constants.PREF_USER_ID, 0L);
        return id > 0 ? id : null;
    }

    public String getUserName() {
        return prefs.getString(Constants.PREF_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(Constants.PREF_USER_EMAIL, "");
    }

    public String getUserProfilePicture() {
        return prefs.getString(Constants.PREF_USER_PROFILE_PICTURE, "");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false) && getUserId() != null;
    }

    public boolean isEmailVerified() {
        return prefs.getBoolean(Constants.PREF_IS_EMAIL_VERIFIED, false);
    }

    public void setEmailVerified(boolean verified) {
        prefs.edit().putBoolean(Constants.PREF_IS_EMAIL_VERIFIED, verified).apply();
    }

    // ===== FCM TOKEN =====

    public void saveFcmToken(String token) {
        prefs.edit().putString(Constants.PREF_FCM_TOKEN, token).apply();
        Log.d(TAG, "✅ FCM token saved");
    }

    public String getFcmToken() {
        return prefs.getString(Constants.PREF_FCM_TOKEN, "");
    }

    // ===== CHAT SETTINGS =====

    public void saveLastConversationId(Long conversationId) {
        prefs.edit().putLong("last_conversation_id", conversationId != null ? conversationId : 0L).apply();
    }

    public Long getLastConversationId() {
        long id = prefs.getLong("last_conversation_id", 0L);
        return id > 0 ? id : null;
    }

    public void setChatNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("chat_notifications_enabled", enabled).apply();
    }

    public boolean isChatNotificationsEnabled() {
        return prefs.getBoolean("chat_notifications_enabled", true);
    }

    // ===== APP SETTINGS =====

    public void setFirstLaunch(boolean isFirstLaunch) {
        prefs.edit().putBoolean("is_first_launch", isFirstLaunch).apply();
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean("is_first_launch", true);
    }

    public void saveAppVersion(String version) {
        prefs.edit().putString("app_version", version).apply();
    }

    public String getAppVersion() {
        return prefs.getString("app_version", "1.0.0");
    }

    // ===== CLEAR DATA =====

    public void clearUserData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_USER_PROFILE_PICTURE);
        editor.remove(Constants.PREF_IS_LOGGED_IN);
        editor.remove(Constants.PREF_IS_EMAIL_VERIFIED);
        editor.remove("last_conversation_id");
        editor.apply();

        Log.d(TAG, "✅ User data cleared");
    }

    public void clearAllData() {
        prefs.edit().clear().apply();
        Log.d(TAG, "✅ All preferences cleared");
    }

    // ===== DEBUG =====

    public void logAllPreferences() {
        Log.d(TAG, "=== SHARED PREFERENCES DEBUG ===");
        Log.d(TAG, "User ID: " + getUserId());
        Log.d(TAG, "User Name: " + getUserName());
        Log.d(TAG, "User Email: " + getUserEmail());
        Log.d(TAG, "Is Logged In: " + isLoggedIn());
        Log.d(TAG, "Is Email Verified: " + isEmailVerified());
        Log.d(TAG, "FCM Token: " + (getFcmToken().isEmpty() ? "Not set" : "Set"));
        Log.d(TAG, "===========================");
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void saveTestProductId(Long productId) {
        prefs.edit().putLong("test_product_id", productId != null ? productId : 0L).apply();
        Log.d(TAG, "✅ Test product ID saved: " + productId);
    }

    public Long getTestProductId() {
        long id = prefs.getLong("test_product_id", 0L);
        return id > 0 ? id : null;
    }

    public void saveString(String key, String value) {
        prefs.edit().putString(key, value).apply();
        Log.d(TAG, "✅ String saved - " + key + ": " + value);
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void saveInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
        Log.d(TAG, "✅ Int saved - " + key + ": " + value);
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public void saveBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
        Log.d(TAG, "✅ Boolean saved - " + key + ": " + value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    // ===== PAYMENT PREFERENCES =====

    public void savePreferredPaymentMethod(String paymentMethod) {
        saveString("preferred_payment_method", paymentMethod);
    }

    public String getPreferredPaymentMethod() {
        return getString("preferred_payment_method", "CARD");
    }

    public void savePaymentNotificationsEnabled(boolean enabled) {
        saveBoolean("payment_notifications_enabled", enabled);
    }

    public boolean isPaymentNotificationsEnabled() {
        return getBoolean("payment_notifications_enabled", true);
    }




    public void setLocationEnabled(boolean enabled) {
        prefs.edit().putBoolean("location_enabled", enabled).apply();
    }

    public boolean isLocationEnabled() {
        return prefs.getBoolean("location_enabled", true);
    }

    // ===== ✅ NEW: LIST SUPPORT FOR RECOMMENDATIONS =====

    public void saveStringList(String key, List<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        prefs.edit().putString(key, json).apply();
        Log.d(TAG, "✅ String list saved - " + key + ": " + list.size() + " items");
    }

    public List<String> getStringList(String key) {
        String json = prefs.getString(key, null);
        if (json == null) {
            return new ArrayList<>();
        }

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>(){}.getType();
            List<String> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing string list for key: " + key, e);
            return new ArrayList<>();
        }
    }

    public void saveLongList(String key, List<Long> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        prefs.edit().putString(key, json).apply();
        Log.d(TAG, "✅ Long list saved - " + key + ": " + list.size() + " items");
    }

    public List<Long> getLongList(String key) {
        String json = prefs.getString(key, null);
        if (json == null) {
            return new ArrayList<>();
        }

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Long>>(){}.getType();
            List<Long> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing long list for key: " + key, e);
            return new ArrayList<>();
        }
    }

    // ✅ SPECIFIC METHODS FOR RECOMMENDATIONS

    public void addToViewedProducts(Long productId) {
        List<Long> viewedProducts = getLongList("viewed_products");
        if (!viewedProducts.contains(productId)) {
            viewedProducts.add(0, productId); // Add to beginning
            if (viewedProducts.size() > 50) { // Keep only last 50
                viewedProducts = viewedProducts.subList(0, 50);
            }
            saveLongList("viewed_products", viewedProducts);
        }
    }

    public List<Long> getViewedProducts() {
        return getLongList("viewed_products");
    }

    public void addToSearchHistory(String query) {
        if (query == null || query.trim().isEmpty()) return;

        List<String> searchHistory = getStringList("search_history");
        searchHistory.remove(query); // Remove if exists
        searchHistory.add(0, query); // Add to beginning
        if (searchHistory.size() > 20) { // Keep only last 20
            searchHistory = searchHistory.subList(0, 20);
        }
        saveStringList("search_history", searchHistory);
    }

    public List<String> getSearchHistory() {
        return getStringList("search_history");
    }

    public void addToBrowsedCategories(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) return;

        List<String> browsedCategories = getStringList("browsed_categories");
        browsedCategories.remove(categoryName);
        browsedCategories.add(0, categoryName);
        if (browsedCategories.size() > 10) {
            browsedCategories = browsedCategories.subList(0, 10);
        }
        saveStringList("browsed_categories", browsedCategories);
    }

    public List<String> getBrowsedCategories() {
        return getStringList("browsed_categories");
    }

    public void clearRecommendationData() {
        prefs.edit()
                .remove("viewed_products")
                .remove("search_history")
                .remove("browsed_categories")
                .remove("behavior_product_view")
                .remove("behavior_search")
                .remove("behavior_category_browse")
                .apply();
        Log.d(TAG, "✅ Recommendation data cleared");
    }


    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply();
        Log.d(TAG, "✅ Notifications enabled: " + enabled);
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean("notifications_enabled", true);
    }

    public void setMessageNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("message_notifications_enabled", enabled).apply();
        Log.d(TAG, "✅ Message notifications enabled: " + enabled);
    }

    public boolean isMessageNotificationsEnabled() {
        return prefs.getBoolean("message_notifications_enabled", true);
    }

    public void setOfferNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("offer_notifications_enabled", enabled).apply();
        Log.d(TAG, "✅ Offer notifications enabled: " + enabled);
    }

    public boolean isOfferNotificationsEnabled() {
        return prefs.getBoolean("offer_notifications_enabled", true);
    }

    public void setPromotionNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("promotion_notifications_enabled", enabled).apply();
        Log.d(TAG, "✅ Promotion notifications enabled: " + enabled);
    }

    public boolean isPromotionNotificationsEnabled() {
        return prefs.getBoolean("promotion_notifications_enabled", true);
    }

    public void setListingNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("listing_notifications_enabled", enabled).apply();
        Log.d(TAG, "✅ Listing notifications enabled: " + enabled);
    }

    public boolean isListingNotificationsEnabled() {
        return prefs.getBoolean("listing_notifications_enabled", true);
    }

    public void setTransactionNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("transaction_notifications_enabled", enabled).apply();
        Log.d(TAG, "✅ Transaction notifications enabled: " + enabled);
    }

    public boolean isTransactionNotificationsEnabled() {
        return prefs.getBoolean("transaction_notifications_enabled", true);
    }

// ===== ✅ NEW: PROMOTION TRACKING METHODS =====

    public void savePromotionClick(String promoCode, String promotionType) {
        String key = "promotion_click_" + System.currentTimeMillis();
        String value = promoCode + "|" + promotionType + "|" + System.currentTimeMillis();
        saveString(key, value);
        Log.d(TAG, "✅ Promotion click saved: " + promoCode);
    }

    public void savePromotionUsage(String promoCode, String orderId) {
        String key = "promotion_usage_" + System.currentTimeMillis();
        String value = promoCode + "|" + orderId + "|" + System.currentTimeMillis();
        saveString(key, value);
        Log.d(TAG, "✅ Promotion usage saved: " + promoCode);
    }

// ===== ✅ NEW: NOTIFICATION SETTINGS BULK OPERATIONS =====

    public void saveAllNotificationSettings(Map<String, Boolean> settings) {
        SharedPreferences.Editor editor = prefs.edit();

        for (Map.Entry<String, Boolean> entry : settings.entrySet()) {
            editor.putBoolean(entry.getKey(), entry.getValue());
        }

        editor.apply();
        Log.d(TAG, "✅ All notification settings saved");
    }

    public Map<String, Boolean> getAllNotificationSettings() {
        Map<String, Boolean> settings = new HashMap<>();

        settings.put("notifications_enabled", isNotificationsEnabled());
        settings.put("message_notifications_enabled", isMessageNotificationsEnabled());
        settings.put("offer_notifications_enabled", isOfferNotificationsEnabled());
        settings.put("promotion_notifications_enabled", isPromotionNotificationsEnabled());
        settings.put("listing_notifications_enabled", isListingNotificationsEnabled());
        settings.put("transaction_notifications_enabled", isTransactionNotificationsEnabled());

        return settings;
    }

    public void resetNotificationSettingsToDefault() {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("notifications_enabled", true);
        editor.putBoolean("message_notifications_enabled", true);
        editor.putBoolean("offer_notifications_enabled", true);
        editor.putBoolean("promotion_notifications_enabled", true);
        editor.putBoolean("listing_notifications_enabled", true);
        editor.putBoolean("transaction_notifications_enabled", true);

        editor.apply();
        Log.d(TAG, "✅ Notification settings reset to default");
    }
}