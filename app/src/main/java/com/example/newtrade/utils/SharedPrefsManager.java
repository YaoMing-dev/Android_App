package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private static final String PREF_NAME = "NewTradePrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PROFILE_PICTURE = "user_profile_picture";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    // FCM and Notifications
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_MESSAGE_NOTIFICATIONS = "message_notifications";
    private static final String KEY_OFFER_NOTIFICATIONS = "offer_notifications";
    private static final String KEY_LISTING_NOTIFICATIONS = "listing_notifications";
    private static final String KEY_PROMOTION_NOTIFICATIONS = "promotion_notifications";
    private static SharedPrefsManager instance;
    private final SharedPreferences prefs;

    // ✅ FIX: Make constructor public
    public SharedPrefsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context);
        }
        return instance;
    }

    public static SharedPrefsManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Call getInstance(Context) first");
        }
        return instance;
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Add getAuthToken method for compatibility
    public String getAuthToken() {
        return getToken();
    }

    public void saveUserId(Long userId) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public Long getUserId() {
        if (!prefs.contains(KEY_USER_ID)) return null;
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public void saveUserName(String userName) {
        prefs.edit().putString(KEY_USER_NAME, userName).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public void saveUserEmail(String email) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null && getUserId() != null;
    }

    // ✅ FIX: Add missing methods
    public void saveUserSession(Long userId, String email, String name, boolean isEmailVerified) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public void setUserId(Long userId) {
        if (userId == null) {
            prefs.edit().remove(KEY_USER_ID).apply();
        } else {
            prefs.edit().putLong(KEY_USER_ID, userId).apply();
        }
    }

    public void setUserEmail(String email) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public void saveUserProfilePicture(String profilePictureUrl) {
        prefs.edit().putString(KEY_USER_PROFILE_PICTURE, profilePictureUrl).apply();
    }

    public String getUserProfilePicture() {
        return prefs.getString(KEY_USER_PROFILE_PICTURE, null);
    }

    public void setUserProfilePicture(String profilePictureUrl) {
        saveUserProfilePicture(profilePictureUrl);
    }

    // FCM Token methods
    public void saveFcmToken(String token) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
    }

    public String getFcmToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }

    // Notification settings
    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setMessageNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MESSAGE_NOTIFICATIONS, enabled).apply();
    }

    public boolean areMessageNotificationsEnabled() {
        return prefs.getBoolean(KEY_MESSAGE_NOTIFICATIONS, true);
    }

    public void setOfferNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_OFFER_NOTIFICATIONS, enabled).apply();
    }

    public boolean areOfferNotificationsEnabled() {
        return prefs.getBoolean(KEY_OFFER_NOTIFICATIONS, true);
    }

    public void setListingNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_LISTING_NOTIFICATIONS, enabled).apply();
    }

    public boolean areListingNotificationsEnabled() {
        return prefs.getBoolean(KEY_LISTING_NOTIFICATIONS, true);
    }

    public void setPromotionNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PROMOTION_NOTIFICATIONS, enabled).apply();
    }

    public boolean arePromotionNotificationsEnabled() {
        return prefs.getBoolean(KEY_PROMOTION_NOTIFICATIONS, false); // Default false for promotions
    }

    public void clearUserSession() {
        clearSession();
    }
}
