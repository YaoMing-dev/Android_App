// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SharedPrefsManager {

    private static final String TAG = "SharedPrefsManager";
    private static SharedPrefsManager instance;
    private SharedPreferences sharedPreferences;

    private SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context.getApplicationContext());
        }
        return instance;
    }

    // =============================================
    // USER AUTHENTICATION
    // =============================================
    public void saveUserData(Long userId, String displayName, String email, String profilePicture) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.PREF_USER_ID, userId != null ? userId : -1L);
        editor.putString(Constants.PREF_USER_NAME, displayName);
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.putString(Constants.PREF_USER_AVATAR, profilePicture);
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.putLong(Constants.PREF_LAST_SYNC, System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "✅ User data saved - ID: " + userId + ", Name: " + displayName + ", Email: " + email);
    }

    public void updateUserProfile(String displayName, String profilePicture) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (displayName != null) {
            editor.putString(Constants.PREF_USER_NAME, displayName);
        }
        if (profilePicture != null) {
            editor.putString(Constants.PREF_USER_AVATAR, profilePicture);
        }
        editor.putLong(Constants.PREF_LAST_SYNC, System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "✅ User profile updated");
    }

    // ✅ THÊM METHOD BỊ THIẾU: getUserProfilePicture()
    public String getUserProfilePicture() {
        return sharedPreferences.getString(Constants.PREF_USER_AVATAR, "");
    }

    // ✅ THÊM METHOD BỊ THIẾU: updateProfilePicture()
    public void updateProfilePicture(String profilePicture) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_USER_AVATAR, profilePicture);
        editor.putLong(Constants.PREF_LAST_SYNC, System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "✅ Profile picture updated: " + profilePicture);
    }

    // ✅ THÊM METHOD BỊ THIẾU: clearUserSession()
    public void clearUserSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_USER_AVATAR);
        editor.remove(Constants.PREF_IS_LOGGED_IN);
        editor.remove(Constants.PREF_FCM_TOKEN);
        editor.apply();

        Log.d(TAG, "✅ User session cleared");
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    public Long getUserId() {
        long userId = sharedPreferences.getLong(Constants.PREF_USER_ID, -1L);
        return userId != -1L ? userId : null;
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.PREF_USER_NAME, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(Constants.PREF_USER_EMAIL, "");
    }

    // =============================================
    // FCM TOKEN
    // =============================================
    public void saveFcmToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_FCM_TOKEN, token);
        editor.apply();

        Log.d(TAG, "✅ FCM token saved");
    }

    public String getFcmToken() {
        return sharedPreferences.getString(Constants.PREF_FCM_TOKEN, "");
    }

    // =============================================
    // APP SETTINGS
    // =============================================
    public void saveAppSettings(boolean darkMode, boolean notifications, boolean location) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("dark_mode", darkMode);
        editor.putBoolean("notifications_enabled", notifications);
        editor.putBoolean("location_enabled", location);
        editor.apply();

        Log.d(TAG, "✅ App settings saved");
    }

    public boolean getDarkMode() {
        return sharedPreferences.getBoolean("dark_mode", false);
    }

    public boolean getNotificationsEnabled() {
        return sharedPreferences.getBoolean("notifications_enabled", true);
    }

    public boolean getLocationEnabled() {
        return sharedPreferences.getBoolean("location_enabled", true);
    }

    // =============================================
    // ONBOARDING
    // =============================================
    public void setOnboardingCompleted(boolean completed) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("onboarding_completed", completed);
        editor.apply();

        Log.d(TAG, "✅ Onboarding status updated: " + completed);
    }

    public boolean isOnboardingCompleted() {
        return sharedPreferences.getBoolean("onboarding_completed", false);
    }

    // =============================================
    // SEARCH HISTORY
    // =============================================
    public void addRecentSearch(String query) {
        // Implementation for recent searches
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("recent_search_" + System.currentTimeMillis(), query);
        editor.apply();

        Log.d(TAG, "✅ Recent search added: " + query);
    }

    public String[] getRecentSearches() {
        // Implementation to get recent searches
        return new String[0]; // Simplified
    }

    // =============================================
    // SYNC AND TRACKING
    // =============================================
    public void updateLastSyncTime() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.PREF_LAST_SYNC, System.currentTimeMillis());
        editor.apply();
    }

    public long getLastSyncTime() {
        return sharedPreferences.getLong(Constants.PREF_LAST_SYNC, 0L);
    }

    public void incrementAppLaunchCount() {
        int currentCount = getAppLaunchCount();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("app_launch_count", currentCount + 1);
        editor.apply();

        Log.d(TAG, "✅ App launch count incremented to: " + (currentCount + 1));
    }

    public int getAppLaunchCount() {
        return sharedPreferences.getInt("app_launch_count", 0);
    }

    // =============================================
    // UTILITY METHODS
    // =============================================
    public void clearAllData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Log.d(TAG, "✅ All shared preferences data cleared");
    }

    public boolean isFirstLaunch() {
        return getAppLaunchCount() == 0;
    }

    public void logCurrentState() {
        Log.d(TAG, "=== SharedPrefsManager Current State ===");
        Log.d(TAG, "Logged In: " + isLoggedIn());
        Log.d(TAG, "User ID: " + getUserId());
        Log.d(TAG, "User Name: " + getUserName());
        Log.d(TAG, "User Email: " + getUserEmail());
        Log.d(TAG, "Profile Picture: " + getUserProfilePicture());
        Log.d(TAG, "FCM Token: " + (getFcmToken().isEmpty() ? "Not set" : "Set"));
        Log.d(TAG, "Last Sync: " + getLastSyncTime());
        Log.d(TAG, "App Launch Count: " + getAppLaunchCount());
        Log.d(TAG, "Dark Mode: " + getDarkMode());
        Log.d(TAG, "Onboarding Completed: " + isOnboardingCompleted());
        Log.d(TAG, "=======================================");
    }


    public void saveLastLocation(double latitude, double longitude, String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("last_latitude", (float) latitude);
        editor.putFloat("last_longitude", (float) longitude);
        editor.putString("last_address", address);
        editor.apply();

        Log.d(TAG, "✅ Last location saved: " + latitude + ", " + longitude + " - " + address);
    }

    public double getLastLatitude() {
        return sharedPreferences.getFloat("last_latitude", 0.0f);
    }

    public double getLastLongitude() {
        return sharedPreferences.getFloat("last_longitude", 0.0f);
    }

    public String getLastAddress() {
        return sharedPreferences.getString("last_address", "");
    }

    // =============================================
// DRAFT PRODUCT MANAGEMENT
// =============================================
    public void saveDraftProduct(String title, String description, String price,
                                 String category, String condition, String location) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("draft_title", title);
        editor.putString("draft_description", description);
        editor.putString("draft_price", price);
        editor.putString("draft_category", category);
        editor.putString("draft_condition", condition);
        editor.putString("draft_location", location);
        editor.putLong("draft_timestamp", System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "✅ Product draft saved");
    }

    public String getDraftTitle() {
        return sharedPreferences.getString("draft_title", "");
    }

    public String getDraftDescription() {
        return sharedPreferences.getString("draft_description", "");
    }

    public String getDraftPrice() {
        return sharedPreferences.getString("draft_price", "");
    }

    public String getDraftCategory() {
        return sharedPreferences.getString("draft_category", "");
    }

    public String getDraftCondition() {
        return sharedPreferences.getString("draft_condition", "");
    }

    public String getDraftLocation() {
        return sharedPreferences.getString("draft_location", "");
    }

    public long getDraftTimestamp() {
        return sharedPreferences.getLong("draft_timestamp", 0L);
    }

    public boolean hasDraft() {
        return getDraftTimestamp() > 0 && !getDraftTitle().isEmpty();
    }

    public void clearDraft() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("draft_title");
        editor.remove("draft_description");
        editor.remove("draft_price");
        editor.remove("draft_category");
        editor.remove("draft_condition");
        editor.remove("draft_location");
        editor.remove("draft_timestamp");
        editor.apply();

        Log.d(TAG, "✅ Product draft cleared");
    }

    // =============================================
// SEARCH HISTORY
// =============================================
    public void saveRecentSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save with timestamp to maintain order
        String key = "search_" + System.currentTimeMillis();
        editor.putString(key, query.trim());
        editor.apply();

        Log.d(TAG, "✅ Recent search saved: " + query);
    }



    public void clearRecentSearches() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Map<String, ?> all = sharedPreferences.getAll();

        for (String key : all.keySet()) {
            if (key.startsWith("search_")) {
                editor.remove(key);
            }
        }

        editor.apply();
        Log.d(TAG, "✅ Recent searches cleared");
    }
}