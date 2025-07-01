// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class SharedPrefsManager {
    private static final String TAG = "SharedPrefsManager";

    // SharedPreferences file names
    private static final String PREF_NAME = "TradeUpPrefs";
    private static final String USER_PREF_NAME = "UserPrefs";

    // User session keys
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_PROFILE_PICTURE = "profile_picture";
    private static final String KEY_IS_EMAIL_VERIFIED = "is_email_verified";
    private static final String KEY_LOGIN_TIMESTAMP = "login_timestamp";

    // App settings keys
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_PUSH_ENABLED = "push_enabled";
    private static final String KEY_LOCATION_ENABLED = "location_enabled";
    private static final String KEY_SAVED_PRODUCT_IDS = "saved_product_ids";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences userPreferences;
    private final SharedPreferences.Editor editor;
    private final SharedPreferences.Editor userEditor;
    private final Gson gson;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        userPreferences = context.getSharedPreferences(USER_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        userEditor = userPreferences.edit();
        gson = new Gson();

        Log.d(TAG, "SharedPrefsManager initialized");
    }

    // =============================================
    // USER SESSION MANAGEMENT
    // =============================================

    /**
     * Save user session after successful login
     */
    public void saveUserSession(Long userId, String email, String displayName, boolean isEmailVerified) {
        try {
            userEditor.putBoolean(KEY_IS_LOGGED_IN, true);
            userEditor.putLong(KEY_USER_ID, userId);
            userEditor.putString(KEY_EMAIL, email);
            userEditor.putString(KEY_DISPLAY_NAME, displayName);
            userEditor.putBoolean(KEY_IS_EMAIL_VERIFIED, isEmailVerified);
            userEditor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis());
            userEditor.apply();

            Log.d(TAG, "✅ User session saved - ID: " + userId + ", Email: " + email);
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to save user session", e);
        }
    }

    /**
     * Update user profile info
     */
    public void updateUserProfile(String displayName, String profilePicture) {
        try {
            userEditor.putString(KEY_DISPLAY_NAME, displayName);
            if (profilePicture != null) {
                userEditor.putString(KEY_PROFILE_PICTURE, profilePicture);
            }
            userEditor.apply();

            Log.d(TAG, "✅ User profile updated");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to update user profile", e);
        }
    }

    /**
     * Update email verification status
     */
    public void updateEmailVerification(boolean isVerified) {
        userEditor.putBoolean(KEY_IS_EMAIL_VERIFIED, isVerified);
        userEditor.apply();
        Log.d(TAG, "✅ Email verification status updated: " + isVerified);
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        boolean loggedIn = userPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        Log.d(TAG, "User login status: " + loggedIn);
        return loggedIn;
    }

    /**
     * Get current user ID
     */
    public Long getUserId() {
        long userId = userPreferences.getLong(KEY_USER_ID, 0);
        return userId > 0 ? userId : null;
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return userPreferences.getString(KEY_EMAIL, null);
    }

    /**
     * Get user display name
     */
    public String getUserDisplayName() {
        return userPreferences.getString(KEY_DISPLAY_NAME, "User");
    }

    /**
     * Get user profile picture URL
     */
    public String getUserProfilePicture() {
        return userPreferences.getString(KEY_PROFILE_PICTURE, null);
    }

    /**
     * Check if email is verified
     */
    public boolean isEmailVerified() {
        return userPreferences.getBoolean(KEY_IS_EMAIL_VERIFIED, false);
    }

    /**
     * Get login timestamp
     */
    public long getLoginTimestamp() {
        return userPreferences.getLong(KEY_LOGIN_TIMESTAMP, 0);
    }

    /**
     * Clear user session (logout)
     */
    public void clearUserSession() {
        try {
            userEditor.clear();
            userEditor.apply();
            Log.d(TAG, "✅ User session cleared");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to clear user session", e);
        }
    }

    // =============================================
    // APP SETTINGS
    // =============================================

    /**
     * Check if this is first app launch
     */
    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    /**
     * Set first launch completed
     */
    public void setFirstLaunchCompleted() {
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.apply();
    }

    /**
     * Notification settings
     */
    public boolean isNotificationEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    public void setNotificationEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATION_ENABLED, enabled);
        editor.apply();
    }

    public boolean isPushEnabled() {
        return sharedPreferences.getBoolean(KEY_PUSH_ENABLED, true);
    }

    public void setPushEnabled(boolean enabled) {
        editor.putBoolean(KEY_PUSH_ENABLED, enabled);
        editor.apply();
    }

    /**
     * Location settings
     */
    public boolean isLocationEnabled() {
        return sharedPreferences.getBoolean(KEY_LOCATION_ENABLED, false);
    }

    public void setLocationEnabled(boolean enabled) {
        editor.putBoolean(KEY_LOCATION_ENABLED, enabled);
        editor.apply();
    }

    // =============================================
    // SAVED PRODUCTS & FAVORITES
    // =============================================

    /**
     * Save product to favorites
     */
    public void saveProduct(Long productId) {
        Set<String> savedIds = getSavedProductIds();
        savedIds.add(productId.toString());
        saveSavedProductIds(savedIds);
        Log.d(TAG, "✅ Product saved: " + productId);
    }

    /**
     * Remove product from favorites
     */
    public void unsaveProduct(Long productId) {
        Set<String> savedIds = getSavedProductIds();
        savedIds.remove(productId.toString());
        saveSavedProductIds(savedIds);
        Log.d(TAG, "✅ Product unsaved: " + productId);
    }

    /**
     * Check if product is saved
     */
    public boolean isProductSaved(Long productId) {
        Set<String> savedIds = getSavedProductIds();
        return savedIds.contains(productId.toString());
    }

    /**
     * Get all saved product IDs
     */
    public Set<String> getSavedProductIds() {
        return sharedPreferences.getStringSet(KEY_SAVED_PRODUCT_IDS, new HashSet<>());
    }

    private void saveSavedProductIds(Set<String> productIds) {
        editor.putStringSet(KEY_SAVED_PRODUCT_IDS, productIds);
        editor.apply();
    }

    // =============================================
    // SEARCH HISTORY
    // =============================================

    /**
     * Add search query to history
     */
    public void addSearchHistory(String query) {
        if (query == null || query.trim().isEmpty()) return;

        Set<String> history = getSearchHistory();
        history.add(query.trim());

        // Keep only last 20 searches
        if (history.size() > 20) {
            // Convert to sorted list and keep recent ones
            // This is a simple implementation, you might want to use a more sophisticated approach
        }

        editor.putStringSet(KEY_SEARCH_HISTORY, history);
        editor.apply();
    }

    /**
     * Get search history
     */
    public Set<String> getSearchHistory() {
        return sharedPreferences.getStringSet(KEY_SEARCH_HISTORY, new HashSet<>());
    }

    /**
     * Clear search history
     */
    public void clearSearchHistory() {
        editor.remove(KEY_SEARCH_HISTORY);
        editor.apply();
    }

    // =============================================
    // FIREBASE CLOUD MESSAGING
    // =============================================

    /**
     * Save FCM token
     */
    public void saveFcmToken(String token) {
        editor.putString(KEY_FCM_TOKEN, token);
        editor.apply();
        Log.d(TAG, "✅ FCM token saved");
    }

    /**
     * Get FCM token
     */
    public String getFcmToken() {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null);
    }

    // =============================================
    // UTILITY METHODS
    // =============================================

    /**
     * Save any custom string data
     */
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Get custom string data
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Save any custom boolean data
     */
    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Get custom boolean data
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Save any custom long data
     */
    public void saveLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * Get custom long data
     */
    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    /**
     * Clear all preferences (for debugging)
     */
    public void clearAll() {
        editor.clear();
        userEditor.clear();
        editor.apply();
        userEditor.apply();
        Log.d(TAG, "✅ All preferences cleared");
    }

    /**
     * Get debug info
     */
    public String getDebugInfo() {
        return "User: " + getUserDisplayName() +
                " (ID: " + getUserId() + ")" +
                ", Logged in: " + isLoggedIn() +
                ", Email verified: " + isEmailVerified() +
                ", Saved products: " + getSavedProductIds().size();
    }
}