// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private static SharedPrefsManager instance;
    private SharedPreferences prefs;

    private SharedPrefsManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context.getApplicationContext());
        }
        return instance;
    }

    // ===== USER DATA =====

    public void setUserData(Long userId, String email, String name) {
        prefs.edit()
                .putLong(Constants.PREF_USER_ID, userId)
                .putString(Constants.PREF_USER_EMAIL, email)
                .putString(Constants.PREF_USER_NAME, name)
                .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                .apply();
    }

    public void saveUserSession(Long userId, String email, String name, boolean isEmailVerified) {
        prefs.edit()
                .putLong(Constants.PREF_USER_ID, userId)
                .putString(Constants.PREF_USER_EMAIL, email)
                .putString(Constants.PREF_USER_NAME, name)
                .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                .putBoolean(Constants.PREF_IS_EMAIL_VERIFIED, isEmailVerified)
                .apply();
    }

    public Long getUserId() {
        long id = prefs.getLong(Constants.PREF_USER_ID, -1L);
        return id == -1L ? null : id;
    }

    public String getUserEmail() {
        return prefs.getString(Constants.PREF_USER_EMAIL, "");
    }

    public String getUserName() {
        return prefs.getString(Constants.PREF_USER_NAME, "");
    }

    // ✅ FIX: Add missing methods for EditProfileActivity
    public void saveUserName(String name) {
        prefs.edit()
                .putString(Constants.PREF_USER_NAME, name)
                .apply();
    }

    public void saveUserEmail(String email) {
        prefs.edit()
                .putString(Constants.PREF_USER_EMAIL, email)
                .apply();
    }

    public void saveUserProfilePicture(String profilePictureUrl) {
        prefs.edit()
                .putString(Constants.PREF_USER_PROFILE_PICTURE, profilePictureUrl)
                .apply();
    }

    // ===== PROFILE PICTURE SUPPORT =====
    public void setUserProfilePicture(String profilePictureUrl) {
        prefs.edit()
                .putString(Constants.PREF_USER_PROFILE_PICTURE, profilePictureUrl)
                .apply();
    }

    public String getUserProfilePicture() {
        return prefs.getString(Constants.PREF_USER_PROFILE_PICTURE, "");
    }

    // ===== LOGIN STATE =====

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit()
                .putBoolean(Constants.PREF_IS_LOGGED_IN, loggedIn)
                .apply();
    }

    // ===== EMAIL VERIFICATION =====

    public void setEmailVerified(boolean verified) {
        prefs.edit()
                .putBoolean(Constants.PREF_IS_EMAIL_VERIFIED, verified)
                .apply();
    }

    public boolean isEmailVerified() {
        return prefs.getBoolean(Constants.PREF_IS_EMAIL_VERIFIED, false);
    }

    // ===== FCM TOKEN =====

    public void setFcmToken(String token) {
        prefs.edit()
                .putString(Constants.PREF_FCM_TOKEN, token)
                .apply();
    }

    public String getFcmToken() {
        return prefs.getString(Constants.PREF_FCM_TOKEN, "");
    }

    public void saveFcmToken(String token) {
        setFcmToken(token);
    }

    // ===== LOGOUT =====

    public void logout() {
        prefs.edit().clear().apply();
    }

    public void clearUserSession() {
        logout();
    }
}