// app/src/main/java/com/example/newtrade/utils/SharedPrefsManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class SharedPrefsManager {
    private SharedPreferences prefs;

    public SharedPrefsManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    // ===== LOGIN STATE =====
    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(Constants.PREF_IS_LOGGED_IN, loggedIn).apply();
    }

    // ===== USER DATA =====
    public Long getUserId() {
        long id = prefs.getLong(Constants.PREF_USER_ID, -1);
        return id == -1 ? null : id;
    }

    public void setUserId(Long userId) {
        if (userId != null) {
            prefs.edit().putLong(Constants.PREF_USER_ID, userId).apply();
        } else {
            prefs.edit().remove(Constants.PREF_USER_ID).apply();
        }
    }

    public String getUserEmail() {
        return prefs.getString(Constants.PREF_USER_EMAIL, null);
    }

    public void setUserEmail(String email) {
        prefs.edit().putString(Constants.PREF_USER_EMAIL, email).apply();
    }

    public String getUserName() {
        return prefs.getString(Constants.PREF_USER_NAME, null);
    }

    public void setUserName(String name) {
        prefs.edit().putString(Constants.PREF_USER_NAME, name).apply();
    }

    public String getUserAvatar() {
        return prefs.getString(Constants.PREF_USER_AVATAR, null);
    }

    public void setUserAvatar(String avatarUrl) {
        prefs.edit().putString(Constants.PREF_USER_AVATAR, avatarUrl).apply();
    }

    public boolean isEmailVerified() {
        return prefs.getBoolean(Constants.PREF_IS_EMAIL_VERIFIED, false);
    }

    public void setEmailVerified(boolean verified) {
        prefs.edit().putBoolean(Constants.PREF_IS_EMAIL_VERIFIED, verified).apply();
    }

    // ===== LOCATION =====
    public double getLastLocationLat() {
        return (double) prefs.getFloat(Constants.PREF_LAST_LOCATION_LAT, 0f);
    }

    public double getLastLocationLng() {
        return (double) prefs.getFloat(Constants.PREF_LAST_LOCATION_LNG, 0f);
    }

    public void saveLastLocation(double lat, double lng, String locationName) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(Constants.PREF_LAST_LOCATION_LAT, (float) lat);
        editor.putFloat(Constants.PREF_LAST_LOCATION_LNG, (float) lng);
        if (locationName != null) {
            editor.putString(Constants.PREF_LOCATION_NAME, locationName);
        }
        editor.apply();
    }

    public String getLocationName() {
        return prefs.getString(Constants.PREF_LOCATION_NAME, null);
    }

    // ===== SAVE LOGIN DATA FROM API RESPONSE =====
    @SuppressWarnings("unchecked")
    public void saveLoginData(Map<String, Object> userData) {
        SharedPreferences.Editor editor = prefs.edit();

        // Mark as logged in
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);

        // Save user ID
        if (userData.get("id") != null) {
            long userId = ((Number) userData.get("id")).longValue();
            editor.putLong(Constants.PREF_USER_ID, userId);
        }

        // Save user email
        if (userData.get("email") != null) {
            editor.putString(Constants.PREF_USER_EMAIL, (String) userData.get("email"));
        }

        // Save user name (try different fields)
        String userName = null;
        if (userData.get("displayName") != null) {
            userName = (String) userData.get("displayName");
        } else if (userData.get("fullName") != null) {
            userName = (String) userData.get("fullName");
        } else if (userData.get("firstName") != null) {
            userName = (String) userData.get("firstName");
        }
        if (userName != null) {
            editor.putString(Constants.PREF_USER_NAME, userName);
        }

        // Save avatar URL
        String avatarUrl = null;
        if (userData.get("avatarUrl") != null) {
            avatarUrl = (String) userData.get("avatarUrl");
        } else if (userData.get("profilePicture") != null) {
            avatarUrl = (String) userData.get("profilePicture");
        }
        if (avatarUrl != null) {
            editor.putString(Constants.PREF_USER_AVATAR, avatarUrl);
        }

        // Save email verification status
        if (userData.get("isEmailVerified") != null) {
            boolean isVerified = (Boolean) userData.get("isEmailVerified");
            editor.putBoolean(Constants.PREF_IS_EMAIL_VERIFIED, isVerified);
        }

        editor.apply();
    }

    // ===== LOGOUT =====
    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.PREF_IS_LOGGED_IN);
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_USER_AVATAR);
        editor.remove(Constants.PREF_IS_EMAIL_VERIFIED);
        // Keep location data
        editor.apply();
    }

    // ===== CLEAR ALL =====
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}