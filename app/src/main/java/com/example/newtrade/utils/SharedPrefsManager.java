// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

    public String getUserAvatar() {
        return sharedPreferences.getString(Constants.PREF_USER_AVATAR, "");
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, false);
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_USER_AVATAR);
        editor.apply();

        Log.d(TAG, "✅ User logged out successfully");
    }

    // =============================================
    // FCM TOKEN MANAGEMENT
    // =============================================
    public void saveFcmToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_FCM_TOKEN, token);
        editor.apply();

        Log.d(TAG, "✅ FCM token saved: " + token.substring(0, Math.min(token.length(), 20)) + "...");
    }

    public String getFcmToken() {
        return sharedPreferences.getString(Constants.PREF_FCM_TOKEN, "");
    }

    // =============================================
    // SEARCH PREFERENCES
    // =============================================
    public void saveRecentSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        String trimmedQuery = query.trim();
        String recentSearches = sharedPreferences.getString("recent_searches", "");

        // Remove if already exists
        String[] searches = recentSearches.split("\\|");
        StringBuilder newSearches = new StringBuilder();

        // Add new search at the beginning
        newSearches.append(trimmedQuery);

        // Add existing searches (up to MAX_RECENT_SEARCHES)
        int count = 1;
        for (String search : searches) {
            if (count >= Constants.MAX_RECENT_SEARCHES) {
                break;
            }
            if (!search.trim().isEmpty() && !search.equals(trimmedQuery)) {
                newSearches.append("|").append(search);
                count++;
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("recent_searches", newSearches.toString());
        editor.apply();

        Log.d(TAG, "✅ Recent search saved: " + trimmedQuery);
    }

    public String[] getRecentSearches() {
        String recentSearches = sharedPreferences.getString("recent_searches", "");
        if (recentSearches.isEmpty()) {
            return new String[0];
        }

        String[] searches = recentSearches.split("\\|");
        return searches.length > 0 ? searches : new String[0];
    }

    public void clearRecentSearches() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("recent_searches");
        editor.apply();

        Log.d(TAG, "✅ Recent searches cleared");
    }

    // =============================================
    // LOCATION PREFERENCES
    // =============================================
    public void saveLastLocation(double latitude, double longitude, String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("last_latitude", String.valueOf(latitude));
        editor.putString("last_longitude", String.valueOf(longitude));
        editor.putString("last_address", address);
        editor.putLong("last_location_time", System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "✅ Last location saved: " + address);
    }

    public double getLastLatitude() {
        String latStr = sharedPreferences.getString("last_latitude", "0.0");
        try {
            return Double.parseDouble(latStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public double getLastLongitude() {
        String lonStr = sharedPreferences.getString("last_longitude", "0.0");
        try {
            return Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public String getLastAddress() {
        return sharedPreferences.getString("last_address", "");
    }

    public long getLastLocationTime() {
        return sharedPreferences.getLong("last_location_time", 0L);
    }

    // =============================================
    // SEARCH FILTERS
    // =============================================
    public void saveSearchFilters(String category, double minPrice, double maxPrice,
                                  String condition, double searchRadius) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("filter_category", category);
        editor.putString("filter_min_price", String.valueOf(minPrice));
        editor.putString("filter_max_price", String.valueOf(maxPrice));
        editor.putString("filter_condition", condition);
        editor.putString("filter_search_radius", String.valueOf(searchRadius));
        editor.apply();

        Log.d(TAG, "✅ Search filters saved");
    }

    public String getFilterCategory() {
        return sharedPreferences.getString("filter_category", "");
    }

    public double getFilterMinPrice() {
        String priceStr = sharedPreferences.getString("filter_min_price", "0.0");
        try {
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public double getFilterMaxPrice() {
        String priceStr = sharedPreferences.getString("filter_max_price", "0.0");
        try {
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public String getFilterCondition() {
        return sharedPreferences.getString("filter_condition", "");
    }

    public double getFilterSearchRadius() {
        String radiusStr = sharedPreferences.getString("filter_search_radius", "10.0");
        try {
            return Double.parseDouble(radiusStr);
        } catch (NumberFormatException e) {
            return Constants.DEFAULT_SEARCH_RADIUS_KM;
        }
    }

    public void clearSearchFilters() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("filter_category");
        editor.remove("filter_min_price");
        editor.remove("filter_max_price");
        editor.remove("filter_condition");
        editor.remove("filter_search_radius");
        editor.apply();

        Log.d(TAG, "✅ Search filters cleared");
    }

    // =============================================
    // NOTIFICATION PREFERENCES
    // =============================================
    public void saveNotificationPreferences(boolean newMessages, boolean offers,
                                            boolean transactions, boolean general) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notif_new_messages", newMessages);
        editor.putBoolean("notif_offers", offers);
        editor.putBoolean("notif_transactions", transactions);
        editor.putBoolean("notif_general", general);
        editor.apply();

        Log.d(TAG, "✅ Notification preferences saved");
    }

    public boolean getNotificationNewMessages() {
        return sharedPreferences.getBoolean("notif_new_messages", true);
    }

    public boolean getNotificationOffers() {
        return sharedPreferences.getBoolean("notif_offers", true);
    }

    public boolean getNotificationTransactions() {
        return sharedPreferences.getBoolean("notif_transactions", true);
    }

    public boolean getNotificationGeneral() {
        return sharedPreferences.getBoolean("notif_general", true);
    }

    // =============================================
    // APP SETTINGS
    // =============================================
    public void saveAppSettings(boolean darkMode, String language, boolean locationEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("dark_mode", darkMode);
        editor.putString("language", language);
        editor.putBoolean("location_enabled", locationEnabled);
        editor.apply();

        Log.d(TAG, "✅ App settings saved");
    }

    public boolean getDarkMode() {
        return sharedPreferences.getBoolean("dark_mode", false);
    }

    public String getLanguage() {
        return sharedPreferences.getString("language", "en");
    }

    public boolean getLocationEnabled() {
        return sharedPreferences.getBoolean("location_enabled", true);
    }

    // =============================================
    // ONBOARDING & TUTORIALS
    // =============================================
    public void setOnboardingCompleted(boolean completed) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("onboarding_completed", completed);
        editor.apply();

        Log.d(TAG, "✅ Onboarding status set to: " + completed);
    }

    public boolean isOnboardingCompleted() {
        return sharedPreferences.getBoolean("onboarding_completed", false);
    }

    public void setTutorialShown(String tutorialName, boolean shown) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("tutorial_" + tutorialName, shown);
        editor.apply();

        Log.d(TAG, "✅ Tutorial " + tutorialName + " set to: " + shown);
    }

    public boolean isTutorialShown(String tutorialName) {
        return sharedPreferences.getBoolean("tutorial_" + tutorialName, false);
    }

    // =============================================
    // ANALYTICS & TRACKING
    // =============================================
    public void incrementAppLaunchCount() {
        int currentCount = sharedPreferences.getInt("app_launch_count", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("app_launch_count", currentCount + 1);
        editor.putLong("last_app_launch", System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "✅ App launch count incremented to: " + (currentCount + 1));
    }

    public int getAppLaunchCount() {
        return sharedPreferences.getInt("app_launch_count", 0);
    }

    public long getLastAppLaunch() {
        return sharedPreferences.getLong("last_app_launch", 0L);
    }

    public void saveLastSyncTime() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.PREF_LAST_SYNC, System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "✅ Last sync time updated");
    }

    public long getLastSyncTime() {
        return sharedPreferences.getLong(Constants.PREF_LAST_SYNC, 0L);
    }

    // =============================================
    // CACHE MANAGEMENT
    // =============================================
    public void saveCacheData(String key, String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cache_" + key, data);
        editor.putLong("cache_time_" + key, System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "✅ Cache data saved for key: " + key);
    }

    public String getCacheData(String key) {
        return sharedPreferences.getString("cache_" + key, "");
    }

    public long getCacheTime(String key) {
        return sharedPreferences.getLong("cache_time_" + key, 0L);
    }

    public boolean isCacheValid(String key, long maxAge) {
        long cacheTime = getCacheTime(key);
        return cacheTime > 0 && (System.currentTimeMillis() - cacheTime) < maxAge;
    }

    public void clearCache(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("cache_" + key);
        editor.remove("cache_time_" + key);
        editor.apply();

        Log.d(TAG, "✅ Cache cleared for key: " + key);
    }

    public void clearAllCache() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Get all keys and remove cache entries
        for (String key : sharedPreferences.getAll().keySet()) {
            if (key.startsWith("cache_")) {
                editor.remove(key);
            }
        }

        editor.apply();
        Log.d(TAG, "✅ All cache cleared");
    }

    // =============================================
    // DRAFT MANAGEMENT
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
    // UTILITY METHODS
    // =============================================
    public void clearAllData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Log.d(TAG, "✅ All shared preferences data cleared");
    }

    public void exportUserData() {
        // Export user data for backup purposes
        StringBuilder data = new StringBuilder();
        data.append("User Data Export\n");
        data.append("================\n");
        data.append("User ID: ").append(getUserId()).append("\n");
        data.append("Name: ").append(getUserName()).append("\n");
        data.append("Email: ").append(getUserEmail()).append("\n");
        data.append("Last Sync: ").append(getLastSyncTime()).append("\n");
        data.append("App Launch Count: ").append(getAppLaunchCount()).append("\n");

        Log.d(TAG, "✅ User data exported");
        Log.d(TAG, data.toString());
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
        Log.d(TAG, "FCM Token: " + (getFcmToken().isEmpty() ? "Not set" : "Set"));
        Log.d(TAG, "Last Sync: " + getLastSyncTime());
        Log.d(TAG, "App Launch Count: " + getAppLaunchCount());
        Log.d(TAG, "Recent Searches: " + getRecentSearches().length);
        Log.d(TAG, "Location Enabled: " + getLocationEnabled());
        Log.d(TAG, "Dark Mode: " + getDarkMode());
        Log.d(TAG, "Onboarding Completed: " + isOnboardingCompleted());
        Log.d(TAG, "Has Draft: " + hasDraft());
        Log.d(TAG, "=======================================");
    }
}