// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // User authentication
    public void saveUserLogin(Long userId, String email, String name, String avatar, String token) {
        editor.putLong(Constants.PREF_USER_ID, userId);
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.putString(Constants.PREF_USER_NAME, name);
        editor.putString(Constants.PREF_USER_AVATAR, avatar);
        editor.putString(Constants.PREF_AUTH_TOKEN, token);
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    public Long getUserId() {
        return sharedPreferences.getLong(Constants.PREF_USER_ID, -1L);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(Constants.PREF_USER_EMAIL, null);
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.PREF_USER_NAME, null);
    }

    public String getUserAvatar() {
        return sharedPreferences.getString(Constants.PREF_USER_AVATAR, null);
    }

    public String getAuthToken() {
        return sharedPreferences.getString(Constants.PREF_AUTH_TOKEN, null);
    }

    // Location
    public void saveLastLocation(double latitude, double longitude) {
        editor.putFloat(Constants.PREF_LAST_LOCATION_LAT, (float) latitude);
        editor.putFloat(Constants.PREF_LAST_LOCATION_LNG, (float) longitude);
        editor.apply();
    }

    public double getLastLatitude() {
        return sharedPreferences.getFloat(Constants.PREF_LAST_LOCATION_LAT, (float) Constants.DEFAULT_LATITUDE);
    }

    public double getLastLongitude() {
        return sharedPreferences.getFloat(Constants.PREF_LAST_LOCATION_LNG, (float) Constants.DEFAULT_LONGITUDE);
    }

    // Logout
    public void clearUserSession() {
        editor.clear();
        editor.apply();
    }
}