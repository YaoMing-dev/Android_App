// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.newtrade.models.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPrefsManager {
    private static final String TAG = "SharedPrefsManager";

    // =============================================
    // SHARED PREFERENCES FILES
    // =============================================
    private static final String PREF_NAME = "TradeUpPrefs";
    private static final String USER_PREF_NAME = "UserPrefs";
    private static final String SETTINGS_PREF_NAME = "SettingsPrefs";

    // =============================================
    // USER SESSION KEYS
    // =============================================
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_PROFILE_PICTURE = "profile_picture";
    private static final String KEY_BIO = "bio";
    private static final String KEY_CONTACT_INFO = "contact_info";
    private static final String KEY_RATING = "rating";
    private static final String KEY_TOTAL_TRANSACTIONS = "total_transactions";
    private static final String KEY_IS_EMAIL_VERIFIED = "is_email_verified";
    private static final String KEY_IS_ACTIVE = "is_active";
    private static final String KEY_LOGIN_TIMESTAMP = "login_timestamp";
    private static final String KEY_LAST_SYNC = "last_sync";

    // =============================================
    // APP SETTINGS KEYS
    // =============================================
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_PUSH_ENABLED = "push_enabled";
    private static final String KEY_LOCATION_ENABLED = "location_enabled";
    private static final String KEY_DARK_MODE_ENABLED = "dark_mode_enabled";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_CURRENCY = "currency";

    // =============================================
    // DATA STORAGE KEYS
    // =============================================
    private static final String KEY_SAVED_PRODUCT_IDS = "saved_product_ids";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final String KEY_RECENT_CATEGORIES = "recent_categories";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_DEVICE_ID = "device_id";

    // =============================================
    // CACHE KEYS
    // =============================================
    private static final String KEY_CACHE_CATEGORIES = "cache_categories";
    private static final String KEY_CACHE_CATEGORIES_TIMESTAMP = "cache_categories_timestamp";
    private static final String KEY_CACHE_USER_STATS = "cache_user_stats";
    private static final String KEY_CACHE_USER_STATS_TIMESTAMP = "cache_user_stats_timestamp";

    // =============================================
    // PERMISSIONS & PRIVACY
    // =============================================
    private static final String KEY_LOCATION_PERMISSION_ASKED = "location_permission_asked";
    private static final String KEY_NOTIFICATION_PERMISSION_ASKED = "notification_permission_asked";
    private static final String KEY_CAMERA_PERMISSION_ASKED = "camera_permission_asked";
    private static final String KEY_STORAGE_PERMISSION_ASKED = "storage_permission_asked";

    // =============================================
    // INSTANCE & INITIALIZATION
    // =============================================
    private static SharedPrefsManager instance;
    private final SharedPreferences mainPrefs;
    private final SharedPreferences userPrefs;
    private final SharedPreferences settingsPrefs;
    private final Gson gson;
    private final Context context;

    private SharedPrefsManager(Context context) {
        this.context = context.getApplicationContext();
        this.mainPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.userPrefs = context.getSharedPreferences(USER_PREF_NAME, Context.MODE_PRIVATE);
        this.settingsPrefs = context.getSharedPreferences(SETTINGS_PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();

        Log.d(TAG, "✅ SharedPrefsManager initialized");
    }

    /**
     * Get singleton instance
     */
    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context);
        }
        return instance;
    }

    /**
     * Constructor for direct use (maintains backward compatibility)
     */
    public SharedPrefsManager(Context context, boolean directUse) {
        this(context);
    }

    // =============================================
    // USER SESSION MANAGEMENT
    // =============================================

    /**
     * Save complete user session after login
     */
    public void saveUserSession(User user) {
        if (user == null) {
            Log.e(TAG, "Cannot save null user session");
            return;
        }

        try {
            SharedPreferences.Editor editor = userPrefs.edit();

            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putLong(KEY_USER_ID, user.getId() != null ? user.getId() : 0L);
            editor.putString(KEY_EMAIL, user.getEmail());
            editor.putString(KEY_DISPLAY_NAME, user.getDisplayName());
            editor.putString(KEY_PROFILE_PICTURE, user.getProfilePicture());
            editor.putString(KEY_BIO, user.getBio());
            editor.putString(KEY_CONTACT_INFO, user.getContactInfo());
            editor.putFloat(KEY_RATING, user.getRating() != null ? user.getRating().floatValue() : 0.0f);
            editor.putInt(KEY_TOTAL_TRANSACTIONS, user.getTotalTransactions() != null ? user.getTotalTransactions() : 0);
            editor.putBoolean(KEY_IS_EMAIL_VERIFIED, user.getIsEmailVerified() != null ? user.getIsEmailVerified() : false);
            editor.putBoolean(KEY_IS_ACTIVE, user.getIsActive() != null ? user.getIsActive() : true);
            editor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis());

            editor.apply();

            Log.d(TAG, "✅ User session saved for: " + user.getEmail());

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to save user session", e);
        }
    }

    /**
     * Save basic user session (for backward compatibility)
     */
    public void saveUserSession(Long userId, String email, String displayName, boolean isEmailVerified) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setIsEmailVerified(isEmailVerified);
        saveUserSession(user);
    }

    /**
     * Update user profile data
     */
    public void updateUserProfile(User user) {
        if (user == null || !isLoggedIn()) return;

        try {
            SharedPreferences.Editor editor = userPrefs.edit();

            if (user.getDisplayName() != null) {
                editor.putString(KEY_DISPLAY_NAME, user.getDisplayName());
            }
            if (user.getProfilePicture() != null) {
                editor.putString(KEY_PROFILE_PICTURE, user.getProfilePicture());
            }
            if (user.getBio() != null) {
                editor.putString(KEY_BIO, user.getBio());
            }
            if (user.getContactInfo() != null) {
                editor.putString(KEY_CONTACT_INFO, user.getContactInfo());
            }
            if (user.getRating() != null) {
                editor.putFloat(KEY_RATING, user.getRating().floatValue());
            }
            if (user.getTotalTransactions() != null) {
                editor.putInt(KEY_TOTAL_TRANSACTIONS, user.getTotalTransactions());
            }

            editor.putLong(KEY_LAST_SYNC, System.currentTimeMillis());
            editor.apply();

            Log.d(TAG, "✅ User profile updated");

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to update user profile", e);
        }
    }

    /**
     * Get current user as User object
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) return null;

        try {
            User user = new User();
            user.setId(getUserId());
            user.setEmail(getUserEmail());
            user.setDisplayName(getUserDisplayName());
            user.setProfilePicture(getUserProfilePicture());
            user.setBio(getUserBio());
            user.setContactInfo(getUserContactInfo());
            user.setRating((double) userPrefs.getFloat(KEY_RATING, 0.0f));
            user.setTotalTransactions(userPrefs.getInt(KEY_TOTAL_TRANSACTIONS, 0));
            user.setIsEmailVerified(isEmailVerified());
            user.setIsActive(userPrefs.getBoolean(KEY_IS_ACTIVE, true));

            return user;

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to get current user", e);
            return null;
        }
    }

    /**
     * Clear user session (logout)
     */
    public void clearUserSession() {
        try {
            // Save FCM token before clearing
            String fcmToken = getFcmToken();

            userPrefs.edit().clear().apply();

            // Restore FCM token
            if (fcmToken != null) {
                saveFcmToken(fcmToken);
            }

            Log.d(TAG, "✅ User session cleared");

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to clear user session", e);
        }
    }

    // =============================================
    // USER SESSION GETTERS
    // =============================================

    public boolean isLoggedIn() {
        return userPrefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public Long getUserId() {
        long id = userPrefs.getLong(KEY_USER_ID, 0L);
        return id > 0 ? id : null;
    }

    public String getUserEmail() {
        return userPrefs.getString(KEY_EMAIL, null);
    }

    public String getUserDisplayName() {
        return userPrefs.getString(KEY_DISPLAY_NAME, null);
    }

    public String getUserProfilePicture() {
        return userPrefs.getString(KEY_PROFILE_PICTURE, null);
    }

    public String getUserBio() {
        return userPrefs.getString(KEY_BIO, null);
    }

    public String getUserContactInfo() {
        return userPrefs.getString(KEY_CONTACT_INFO, null);
    }

    public float getUserRating() {
        return userPrefs.getFloat(KEY_RATING, 0.0f);
    }

    public int getTotalTransactions() {
        return userPrefs.getInt(KEY_TOTAL_TRANSACTIONS, 0);
    }

    public boolean isEmailVerified() {
        return userPrefs.getBoolean(KEY_IS_EMAIL_VERIFIED, false);
    }

    public long getLoginTimestamp() {
        return userPrefs.getLong(KEY_LOGIN_TIMESTAMP, 0L);
    }

    // =============================================
    // APP SETTINGS
    // =============================================

    public boolean isFirstLaunch() {
        return settingsPrefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean firstLaunch) {
        settingsPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, firstLaunch).apply();
    }

    public boolean isOnboardingCompleted() {
        return settingsPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        settingsPrefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
    }

    public boolean isNotificationEnabled() {
        return settingsPrefs.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    public void setNotificationEnabled(boolean enabled) {
        settingsPrefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }

    public boolean isPushEnabled() {
        return settingsPrefs.getBoolean(KEY_PUSH_ENABLED, true);
    }

    public void setPushEnabled(boolean enabled) {
        settingsPrefs.edit().putBoolean(KEY_PUSH_ENABLED, enabled).apply();
    }

    public boolean isLocationEnabled() {
        return settingsPrefs.getBoolean(KEY_LOCATION_ENABLED, false);
    }

    public void setLocationEnabled(boolean enabled) {
        settingsPrefs.edit().putBoolean(KEY_LOCATION_ENABLED, enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return settingsPrefs.getBoolean(KEY_DARK_MODE_ENABLED, false);
    }

    public void setDarkModeEnabled(boolean enabled) {
        settingsPrefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply();
    }

    public String getLanguage() {
        return settingsPrefs.getString(KEY_LANGUAGE, "en");
    }

    public void setLanguage(String language) {
        settingsPrefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public String getCurrency() {
        return settingsPrefs.getString(KEY_CURRENCY, "VND");
    }

    public void setCurrency(String currency) {
        settingsPrefs.edit().putString(KEY_CURRENCY, currency).apply();
    }

    // =============================================
    // SAVED PRODUCTS & FAVORITES
    // =============================================

    public void saveProduct(Long productId) {
        if (productId == null || productId <= 0) return;

        try {
            Set<String> savedIds = getSavedProductIds();
            savedIds.add(productId.toString());

            mainPrefs.edit()
                    .putStringSet(KEY_SAVED_PRODUCT_IDS, savedIds)
                    .apply();

            Log.d(TAG, "✅ Product saved: " + productId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to save product: " + productId, e);
        }
    }

    public void unsaveProduct(Long productId) {
        if (productId == null || productId <= 0) return;

        try {
            Set<String> savedIds = getSavedProductIds();
            savedIds.remove(productId.toString());

            mainPrefs.edit()
                    .putStringSet(KEY_SAVED_PRODUCT_IDS, savedIds)
                    .apply();

            Log.d(TAG, "✅ Product unsaved: " + productId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to unsave product: " + productId, e);
        }
    }

    public boolean isProductSaved(Long productId) {
        if (productId == null || productId <= 0) return false;

        Set<String> savedIds = getSavedProductIds();
        return savedIds.contains(productId.toString());
    }

    public Set<String> getSavedProductIds() {
        return new HashSet<>(mainPrefs.getStringSet(KEY_SAVED_PRODUCT_IDS, new HashSet<>()));
    }

    public List<Long> getSavedProductIdsAsLong() {
        List<Long> longIds = new ArrayList<>();
        Set<String> stringIds = getSavedProductIds();

        for (String id : stringIds) {
            try {
                longIds.add(Long.parseLong(id));
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid product ID in saved list: " + id);
            }
        }

        return longIds;
    }

    // =============================================
    // SEARCH HISTORY
    // =============================================

    public void addSearchHistory(String query) {
        if (query == null || query.trim().isEmpty()) return;

        try {
            List<String> history = getSearchHistory();
            String trimmedQuery = query.trim();

            // Remove if already exists (to move to top)
            history.remove(trimmedQuery);

            // Add to beginning
            history.add(0, trimmedQuery);

            // Keep only last 20 searches
            if (history.size() > Constants.MAX_SEARCH_HISTORY) {
                history = history.subList(0, Constants.MAX_SEARCH_HISTORY);
            }

            // Save back
            String json = gson.toJson(history);
            mainPrefs.edit().putString(KEY_SEARCH_HISTORY, json).apply();

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to add search history", e);
        }
    }

    public List<String> getSearchHistory() {
        try {
            String json = mainPrefs.getString(KEY_SEARCH_HISTORY, null);
            if (json != null) {
                Type type = new TypeToken<List<String>>(){}.getType();
                List<String> history = gson.fromJson(json, type);
                return history != null ? history : new ArrayList<>();
            }
        } catch (JsonSyntaxException e) {
            Log.w(TAG, "Failed to parse search history, clearing it", e);
            clearSearchHistory();
        }

        return new ArrayList<>();
    }

    public void clearSearchHistory() {
        mainPrefs.edit().remove(KEY_SEARCH_HISTORY).apply();
        Log.d(TAG, "✅ Search history cleared");
    }

    // =============================================
    // RECENT CATEGORIES
    // =============================================

    public void addRecentCategory(Long categoryId, String categoryName) {
        if (categoryId == null || categoryName == null) return;

        try {
            List<RecentCategory> recent = getRecentCategories();
            RecentCategory newCategory = new RecentCategory(categoryId, categoryName);

            // Remove if already exists
            recent.removeIf(cat -> cat.getId().equals(categoryId));

            // Add to beginning
            recent.add(0, newCategory);

            // Keep only last 10
            if (recent.size() > 10) {
                recent = recent.subList(0, 10);
            }

            // Save back
            String json = gson.toJson(recent);
            mainPrefs.edit().putString(KEY_RECENT_CATEGORIES, json).apply();

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to add recent category", e);
        }
    }

    public List<RecentCategory> getRecentCategories() {
        try {
            String json = mainPrefs.getString(KEY_RECENT_CATEGORIES, null);
            if (json != null) {
                Type type = new TypeToken<List<RecentCategory>>(){}.getType();
                List<RecentCategory> categories = gson.fromJson(json, type);
                return categories != null ? categories : new ArrayList<>();
            }
        } catch (JsonSyntaxException e) {
            Log.w(TAG, "Failed to parse recent categories, clearing them", e);
            mainPrefs.edit().remove(KEY_RECENT_CATEGORIES).apply();
        }

        return new ArrayList<>();
    }

    // =============================================
    // FCM & DEVICE
    // =============================================

    public void saveFcmToken(String token) {
        if (token == null || token.trim().isEmpty()) return;

        mainPrefs.edit().putString(KEY_FCM_TOKEN, token).apply();
        Log.d(TAG, "✅ FCM token saved");
    }

    public String getFcmToken() {
        return mainPrefs.getString(KEY_FCM_TOKEN, null);
    }

    public void saveDeviceId(String deviceId) {
        if (deviceId == null) return;

        mainPrefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
    }

    public String getDeviceId() {
        return mainPrefs.getString(KEY_DEVICE_ID, null);
    }

    // =============================================
    // PERMISSIONS TRACKING
    // =============================================

    public boolean isLocationPermissionAsked() {
        return settingsPrefs.getBoolean(KEY_LOCATION_PERMISSION_ASKED, false);
    }

    public void setLocationPermissionAsked(boolean asked) {
        settingsPrefs.edit().putBoolean(KEY_LOCATION_PERMISSION_ASKED, asked).apply();
    }

    public boolean isNotificationPermissionAsked() {
        return settingsPrefs.getBoolean(KEY_NOTIFICATION_PERMISSION_ASKED, false);
    }

    public void setNotificationPermissionAsked(boolean asked) {
        settingsPrefs.edit().putBoolean(KEY_NOTIFICATION_PERMISSION_ASKED, asked).apply();
    }

    public boolean isCameraPermissionAsked() {
        return settingsPrefs.getBoolean(KEY_CAMERA_PERMISSION_ASKED, false);
    }

    public void setCameraPermissionAsked(boolean asked) {
        settingsPrefs.edit().putBoolean(KEY_CAMERA_PERMISSION_ASKED, asked).apply();
    }

    public boolean isStoragePermissionAsked() {
        return settingsPrefs.getBoolean(KEY_STORAGE_PERMISSION_ASKED, false);
    }

    public void setStoragePermissionAsked(boolean asked) {
        settingsPrefs.edit().putBoolean(KEY_STORAGE_PERMISSION_ASKED, asked).apply();
    }

    // =============================================
    // CACHING SYSTEM
    // =============================================

    public void cacheCategories(String categoriesJson) {
        mainPrefs.edit()
                .putString(KEY_CACHE_CATEGORIES, categoriesJson)
                .putLong(KEY_CACHE_CATEGORIES_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public String getCachedCategories() {
        long timestamp = mainPrefs.getLong(KEY_CACHE_CATEGORIES_TIMESTAMP, 0);
        long age = System.currentTimeMillis() - timestamp;

        // Cache valid for 1 hour
        if (age < Constants.CACHE_CATEGORIES_DURATION) {
            return mainPrefs.getString(KEY_CACHE_CATEGORIES, null);
        }

        return null;
    }

    public void cacheUserStats(String statsJson) {
        userPrefs.edit()
                .putString(KEY_CACHE_USER_STATS, statsJson)
                .putLong(KEY_CACHE_USER_STATS_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public String getCachedUserStats() {
        long timestamp = userPrefs.getLong(KEY_CACHE_USER_STATS_TIMESTAMP, 0);
        long age = System.currentTimeMillis() - timestamp;

        // Cache valid for 30 minutes
        if (age < Constants.CACHE_USER_STATS_DURATION) {
            return userPrefs.getString(KEY_CACHE_USER_STATS, null);
        }

        return null;
    }

    // =============================================
    // UTILITY METHODS
    // =============================================

    /**
     * Check if user session is valid (not expired)
     */
    public boolean isSessionValid() {
        if (!isLoggedIn()) return false;

        long loginTime = getLoginTimestamp();
        long currentTime = System.currentTimeMillis();
        long sessionAge = currentTime - loginTime;

        // Session valid for 30 days
        return sessionAge < Constants.SESSION_TIMEOUT;
    }

    /**
     * Get session age in milliseconds
     */
    public long getSessionAge() {
        if (!isLoggedIn()) return 0;

        long loginTime = getLoginTimestamp();
        return System.currentTimeMillis() - loginTime;
    }

    /**
     * Clear all preferences (for debugging/reset)
     */
    public void clearAll() {
        mainPrefs.edit().clear().apply();
        userPrefs.edit().clear().apply();
        settingsPrefs.edit().clear().apply();
        Log.d(TAG, "✅ All preferences cleared");
    }

    /**
     * Get debug information
     */
    public String getDebugInfo() {
        return String.format(
                "User: %s (ID: %d)\n" +
                        "Logged in: %s\n" +
                        "Email verified: %s\n" +
                        "Session age: %d hours\n" +
                        "Saved products: %d\n" +
                        "Search history: %d\n" +
                        "Notifications: %s\n" +
                        "Location: %s\n" +
                        "FCM token: %s",
                getUserDisplayName(),
                getUserId(),
                isLoggedIn(),
                isEmailVerified(),
                getSessionAge() / (1000 * 60 * 60),
                getSavedProductIds().size(),
                getSearchHistory().size(),
                isNotificationEnabled(),
                isLocationEnabled(),
                getFcmToken() != null ? "Set" : "Not set"
        );
    }

    // =============================================
    // INNER CLASSES
    // =============================================

    public static class RecentCategory {
        private Long id;
        private String name;
        private long timestamp;

        public RecentCategory(Long id, String name) {
            this.id = id;
            this.name = name;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public long getTimestamp() { return timestamp; }
    }
}