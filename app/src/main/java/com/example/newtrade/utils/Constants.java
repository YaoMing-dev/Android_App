// app/src/main/java/com/example/newtrade/utils/Constants.java
package com.example.newtrade.utils;

public class Constants {
    // Backend URLs
    public static final String BASE_URL = "http://10.0.2.2:8080/api/"; // Android Emulator
    // public static final String BASE_URL = "http://192.168.1.100:8080/api/"; // Real device

    // Google OAuth
    public static final String GOOGLE_CLIENT_ID = "638175281882-lqsdj0iur1i079l0vlqni71gelshrdgj.apps.googleusercontent.com";

    // WebSocket
    public static final String WEBSOCKET_URL = "ws://10.0.2.2:8080/ws";

    // Shared Preferences Keys
    public static final String PREFS_NAME = "TradeUpPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_AVATAR = "user_avatar";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_AUTH_TOKEN = "auth_token";
    public static final String PREF_LAST_LOCATION_LAT = "last_location_lat";
    public static final String PREF_LAST_LOCATION_LNG = "last_location_lng";

    // Request Codes
    public static final int REQUEST_LOCATION_PERMISSION = 100;
    public static final int REQUEST_CAMERA_PERMISSION = 101;
    public static final int REQUEST_STORAGE_PERMISSION = 102;
    public static final int REQUEST_PICK_IMAGE = 200;
    public static final int REQUEST_CAPTURE_IMAGE = 201;
    public static final int GOOGLE_SIGN_IN_REQUEST = 9001;

    // Product Constants
    public static final int MAX_PRODUCT_IMAGES = 10;
    public static final int MAX_PRODUCT_TITLE_LENGTH = 200;
    public static final int MAX_PRODUCT_DESCRIPTION_LENGTH = 2000;

    // Message Constants
    public static final int MAX_MESSAGE_LENGTH = 1000;

    // Location Constants
    public static final double DEFAULT_LATITUDE = 10.7769; // Ho Chi Minh City
    public static final double DEFAULT_LONGITUDE = 106.7009;
    public static final int DEFAULT_LOCATION_RADIUS = 10; // km
}