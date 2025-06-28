// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {

    private static final String PREF_NAME = "TradeUpPrefs";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Constructor
    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Static getInstance method
    public static SharedPrefsManager getInstance(Context context) {
        return new SharedPrefsManager(context);
    }

    // ✅ FIX: User ID methods
    public void setUserId(Long userId) {
        if (userId != null) {
            editor.putLong(Constants.PREF_USER_ID, userId);
        } else {
            editor.remove(Constants.PREF_USER_ID);
        }
        editor.apply();
    }

    public Long getUserId() {
        long id = sharedPreferences.getLong(Constants.PREF_USER_ID, -1);
        return id != -1 ? id : null;
    }

    // ✅ FIX: User name methods
    public void setUserName(String userName) {
        editor.putString(Constants.PREF_USER_NAME, userName);
        editor.apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.PREF_USER_NAME, "");
    }

    // ✅ FIX: saveUserName method for EditProfileActivity
    public void saveUserName(String userName) {
        setUserName(userName);
    }

    // ✅ FIX: User email methods
    public void setUserEmail(String email) {
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(Constants.PREF_USER_EMAIL, "");
    }

    // ✅ FIX: saveUserEmail method for EditProfileActivity
    public void saveUserEmail(String email) {
        setUserEmail(email);
    }

    // ✅ FIX: Profile picture methods
    public void setUserProfilePicture(String profilePicture) {
        editor.putString(Constants.PREF_USER_PROFILE_PICTURE, profilePicture);
        editor.apply();
    }

    public String getUserProfilePicture() {
        return sharedPreferences.getString(Constants.PREF_USER_PROFILE_PICTURE, "");
    }

    // ✅ FIX: saveUserProfilePicture method for EditProfileActivity
    public void saveUserProfilePicture(String profilePicture) {
        setUserProfilePicture(profilePicture);
    }

    // ✅ FIX: Login status methods
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    // ✅ FIX: FCM Token methods
    public void setFcmToken(String token) {
        editor.putString(Constants.PREF_FCM_TOKEN, token);
        editor.apply();
    }

    public String getFcmToken() {
        return sharedPreferences.getString(Constants.PREF_FCM_TOKEN, "");
    }

    // ✅ FIX: saveFcmToken method for FirebaseMessagingService
    public void saveFcmToken(String token) {
        setFcmToken(token);
    }

    // ✅ FIX: Email verification status
    public void setEmailVerified(boolean isVerified) {
        editor.putBoolean(Constants.PREF_IS_EMAIL_VERIFIED, isVerified);
        editor.apply();
    }

    public boolean isEmailVerified() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_EMAIL_VERIFIED, false);
    }

    // ✅ FIX: saveUserSession method for LoginActivity and OtpVerificationActivity
    public void saveUserSession(Long userId, String email, String name, boolean isEmailVerified) {
        setUserId(userId);
        setUserEmail(email);
        setUserName(name);
        setEmailVerified(isEmailVerified);
        setLoggedIn(true);
    }

    // ✅ FIX: clearUserSession method for SettingsActivity
    public void clearUserSession() {
        setLoggedIn(false);
        setUserId(null);
        setUserEmail("");
        setUserName("");
        setUserProfilePicture("");
        setFcmToken("");
        setEmailVerified(false);
    }

    // ✅ FIX: clearUserData method for ProfileFragment
    public void clearUserData() {
        clearUserSession();
    }
}