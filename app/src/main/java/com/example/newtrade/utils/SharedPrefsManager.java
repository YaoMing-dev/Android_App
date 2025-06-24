// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPrefsManager {

    private static final String TAG = "SharedPrefsManager";
    private static SharedPrefsManager instance;
    private static SharedPreferences sharedPreferences;

    private SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context.getApplicationContext());
        }
        return instance;
    }

    // ===== USER SESSION =====

    public void saveUserSession(Long userId, String email, String displayName, boolean isEmailVerified) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.PREF_USER_ID, userId);
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.putString(Constants.PREF_USER_NAME, displayName);
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.putBoolean(Constants.PREF_IS_EMAIL_VERIFIED, isEmailVerified);
        editor.apply();

        Log.d(TAG, "User session saved - ID: " + userId + ", Email: " + email);
    }

    public void clearUserSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_IS_LOGGED_IN);
        editor.remove(Constants.PREF_IS_EMAIL_VERIFIED);
        editor.apply();

        Log.d(TAG, "User session cleared");
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false)
                && getUserId() > 0;
    }

    public long getUserId() {
        return sharedPreferences.getLong(Constants.PREF_USER_ID, -1);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(Constants.PREF_USER_EMAIL, "");
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.PREF_USER_NAME, "");
    }

    public boolean isEmailVerified() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_EMAIL_VERIFIED, false);
    }

    public void setEmailVerified(boolean verified) {
        sharedPreferences.edit()
                .putBoolean(Constants.PREF_IS_EMAIL_VERIFIED, verified)
                .apply();
    }

    // ===== FCM TOKEN =====

    public void saveFcmToken(String fcmToken) {
        sharedPreferences.edit()
                .putString(Constants.PREF_FCM_TOKEN, fcmToken)
                .apply();
        Log.d(TAG, "FCM token saved");
    }

    public String getFcmToken() {
        return sharedPreferences.getString(Constants.PREF_FCM_TOKEN, "");
    }

    // ===== UTILITY METHODS =====

    public void saveString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void saveBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void saveLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public void removeKey(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
        Log.d(TAG, "All preferences cleared");
    }

    public void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Xóa tất cả data
        editor.apply();
        Log.d(TAG, "✅ All user data cleared");
    }

    // Hoặc nếu muốn chỉ xóa user session:

}