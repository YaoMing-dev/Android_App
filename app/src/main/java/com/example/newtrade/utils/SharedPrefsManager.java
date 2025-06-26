// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
// ✅ FIXED - Add all missing methods
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private static final String PREF_NAME = "TradeUpPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PROFILE_PICTURE = "user_profile_picture";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_FCM_TOKEN = "fcm_token"; // ✅ ADD
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static SharedPrefsManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context.getApplicationContext());
        }
        return instance;
    }

    // ✅ FIX: Add all missing saveUserSession methods
    public void saveUserSession(Long userId, String userName, String userEmail, boolean isEmailVerified) {
        editor.putLong(KEY_USER_ID, userId != null ? userId : 0);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        // isEmailVerified is stored but we don't have a key for it in this simple version
        editor.apply();
    }

    // Overloaded method
    public void saveUserSession(Long userId, String userName, String userEmail) {
        saveUserSession(userId, userName, userEmail, true);
    }

    // ✅ FIX: Add missing saveUserData method
    public void saveUserData(Long userId, String userName, String userEmail, String profilePicture) {
        editor.putLong(KEY_USER_ID, userId != null ? userId : 0);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_PROFILE_PICTURE, profilePicture);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // Original saveUserData method (keep for compatibility)
    public void saveUserData(Long userId, String userName, String userEmail) {
        saveUserData(userId, userName, userEmail, null);
    }

    // ✅ FIX: Add missing saveFcmToken method
    public void saveFcmToken(String fcmToken) {
        editor.putString(KEY_FCM_TOKEN, fcmToken);
        editor.apply();
    }

    public void saveAccessToken(String accessToken) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.apply();
    }

    // Getters
    public Long getUserId() {
        long id = sharedPreferences.getLong(KEY_USER_ID, 0);
        return id > 0 ? id : null;
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserProfilePicture() {
        return sharedPreferences.getString(KEY_USER_PROFILE_PICTURE, "");
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    // ✅ FIX: Add missing getFcmToken method
    public String getFcmToken() {
        return sharedPreferences.getString(KEY_FCM_TOKEN, "");
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void clearUserSession() {
        editor.clear();
        editor.apply();
    }

    // Additional helper methods
    public void updateUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public void updateUserEmail(String userEmail) {
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.apply();
    }

    public void updateUserProfilePicture(String profilePicture) {
        editor.putString(KEY_USER_PROFILE_PICTURE, profilePicture);
        editor.apply();
    }
}