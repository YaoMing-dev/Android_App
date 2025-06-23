// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {

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
    }

    public void clearUserSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_IS_LOGGED_IN);
        editor.remove(Constants.PREF_IS_EMAIL_VERIFIED);
        editor.apply();
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

    public void saveInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void saveLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    // ===== DEBUG METHODS =====

    public void logUserSession() {
        android.util.Log.d("SharedPrefs", "User ID: " + getUserId());
        android.util.Log.d("SharedPrefs", "Email: " + getUserEmail());
        android.util.Log.d("SharedPrefs", "Name: " + getUserName());
        android.util.Log.d("SharedPrefs", "Logged In: " + isLoggedIn());
        android.util.Log.d("SharedPrefs", "Email Verified: " + isEmailVerified());
    }
}