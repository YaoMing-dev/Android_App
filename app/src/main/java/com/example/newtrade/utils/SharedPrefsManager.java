// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Login/Logout
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    // User data
    public void saveUserData(Long userId, String email, String name, String avatar) {
        editor.putLong(Constants.PREF_USER_ID, userId)
                .putString(Constants.PREF_USER_EMAIL, email)
                .putString(Constants.PREF_USER_NAME, name)
                .putString(Constants.PREF_USER_AVATAR, avatar)
                .apply();
    }

    public Long getUserId() {
        long id = sharedPreferences.getLong(Constants.PREF_USER_ID, -1);
        return id == -1 ? null : id;
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

    // Location
    public void saveLocation(Double latitude, Double longitude, String locationName) {
        if (latitude != null && longitude != null) {
            editor.putFloat(Constants.PREF_LAST_LOCATION_LAT, latitude.floatValue())
                    .putFloat(Constants.PREF_LAST_LOCATION_LNG, longitude.floatValue());
        }
        if (locationName != null) {
            editor.putString(Constants.PREF_LOCATION_NAME, locationName);
        }
        editor.apply();
    }

    public Double getLastLatitude() {
        float lat = sharedPreferences.getFloat(Constants.PREF_LAST_LOCATION_LAT, Float.MIN_VALUE);
        return lat == Float.MIN_VALUE ? null : (double) lat;
    }

    public Double getLastLongitude() {
        float lng = sharedPreferences.getFloat(Constants.PREF_LAST_LOCATION_LNG, Float.MIN_VALUE);
        return lng == Float.MIN_VALUE ? null : (double) lng;
    }

    public String getLocationName() {
        return sharedPreferences.getString(Constants.PREF_LOCATION_NAME, null);
    }

    // Email verification
    public void setEmailVerified(boolean isVerified) {
        editor.putBoolean(Constants.PREF_IS_EMAIL_VERIFIED, isVerified).apply();
    }

    public boolean isEmailVerified() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_EMAIL_VERIFIED, false);
    }

    // Clear data
    public void clearUserData() {
        editor.clear().apply();
    }
}