// app/src/main/java/com/example/newtrade/utils/Constants.java
package com.example.newtrade.utils;

public class Constants {

    // ===== API CONFIGURATION =====
    public static final String BASE_URL = "http://10.0.2.2:8080/api/"; // For emulator
    // public static final String BASE_URL = "http://192.168.1.100:8080/api/"; // For physical device

    // ===== GOOGLE SIGN IN =====
    public static final String GOOGLE_CLIENT_ID = "your_google_client_id_here";

    // ===== SHARED PREFERENCES =====
    public static final String PREF_NAME = "NewTradePref";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_AVATAR = "user_avatar";
    public static final String PREF_IS_EMAIL_VERIFIED = "is_email_verified";
    public static final String PREF_LAST_LOCATION_LAT = "last_location_lat";
    public static final String PREF_LAST_LOCATION_LNG = "last_location_lng";
    public static final String PREF_LOCATION_NAME = "location_name";

    // ===== REQUEST CODES =====
    public static final int REQUEST_CODE_LOGIN = 1001;
    public static final int REQUEST_CODE_REGISTER = 1002;
    public static final int REQUEST_CODE_ADD_PRODUCT = 1003;
    public static final int REQUEST_CODE_EDIT_PRODUCT = 1004;
    public static final int REQUEST_CODE_LOCATION_PICKER = 1005;
    public static final int REQUEST_CODE_CAMERA = 1006;
    public static final int REQUEST_CODE_GALLERY = 1007;
    public static final int REQUEST_CODE_PERMISSIONS = 1008;

    // ===== PERMISSIONS =====
    public static final int PERMISSION_REQUEST_CAMERA = 2001;
    public static final int PERMISSION_REQUEST_STORAGE = 2002;
    public static final int PERMISSION_REQUEST_LOCATION = 2003;

    // ===== BUNDLE KEYS =====
    public static final String BUNDLE_PRODUCT_ID = "product_id";
    public static final String BUNDLE_USER_ID = "user_id";
    public static final String BUNDLE_CATEGORY_ID = "category_id";
    public static final String BUNDLE_CATEGORY_NAME = "category_name";
    public static final String BUNDLE_SEARCH_QUERY = "search_query";
    public static final String BUNDLE_LOCATION_LAT = "location_lat";
    public static final String BUNDLE_LOCATION_LNG = "location_lng";
    public static final String BUNDLE_LOCATION_NAME = "location_name";

    // ===== PRODUCT CONSTANTS =====
    public static final int MAX_PRODUCT_IMAGES = 5;
    public static final int MAX_PRODUCT_TITLE_LENGTH = 100;
    public static final int MAX_PRODUCT_DESCRIPTION_LENGTH = 1000;
    public static final double MIN_PRODUCT_PRICE = 1000; // VND
    public static final double MAX_PRODUCT_PRICE = 10000000000.0; // VND

    // ===== SORT OPTIONS =====
    public static final String SORT_NEWEST = "createdAt";
    public static final String SORT_OLDEST = "createdAt";
    public static final String SORT_PRICE_LOW = "price";
    public static final String SORT_PRICE_HIGH = "price";
    public static final String SORT_POPULAR = "viewCount";
    public static final String SORT_RELEVANCE = "relevance";

    // ===== PAGINATION =====
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int SMALL_PAGE_SIZE = 10;
    public static final int LARGE_PAGE_SIZE = 50;

    // ===== LOCATION =====
    public static final double DEFAULT_SEARCH_RADIUS_KM = 10.0;
    public static final double MAX_SEARCH_RADIUS_KM = 100.0;

    // ===== FILE PATHS =====
    public static final String IMAGE_CACHE_DIR = "images";
    public static final String TEMP_IMAGE_DIR = "temp";

    // ===== NETWORK =====
    public static final int NETWORK_TIMEOUT_SECONDS = 30;
    public static final int MAX_RETRY_ATTEMPTS = 3;

    // ===== VALIDATION =====
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 50;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;

    // ===== OTP =====
    public static final int OTP_LENGTH = 6;
    public static final long OTP_EXPIRE_TIME_MINUTES = 5;
    public static final int MAX_OTP_ATTEMPTS = 3;

    // ===== CHAT =====
    public static final int MAX_MESSAGE_LENGTH = 1000;
    public static final int CHAT_PAGE_SIZE = 50;

    // ===== NOTIFICATIONS =====
    public static final String NOTIFICATION_CHANNEL_ID = "newtrade_notifications";
    public static final String NOTIFICATION_CHANNEL_NAME = "NewTrade Notifications";

    // ===== WEBSOCKET =====
    public static final String WEBSOCKET_URL = "ws://10.0.2.2:8080/ws";
    public static final int WEBSOCKET_RECONNECT_INTERVAL = 5000; // 5 seconds

    private Constants() {
        // Prevent instantiation
    }
}