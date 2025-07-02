// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPrefsManager {

    private static final String TAG = "SharedPrefsManager";
    private static SharedPrefsManager instance;
    private SharedPreferences prefs;

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context);
        }
        return instance;
    }

    private SharedPrefsManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
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
}