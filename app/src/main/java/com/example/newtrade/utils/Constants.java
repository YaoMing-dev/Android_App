// app/src/main/java/com/example/newtrade/utils/Constants.java
package com.example.newtrade.utils;

public class Constants {

    // ===== API CONFIGURATION =====
    public static final String BASE_URL = "http://10.0.2.2:8080/api/"; // For emulator
    // public static final String BASE_URL = "http://192.168.1.100:8080/api/"; // For physical device

    // ===== SHARED PREFERENCES =====
    public static final String PREF_NAME = "NewTradePref";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_AVATAR = "user_avatar";
    public static final String PREF_ACCESS_TOKEN = "access_token";
    public static final String PREF_REFRESH_TOKEN = "refresh_token";
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
    public static final int REQUEST_CODE_EDIT_PROFILE = 1009;

    // ===== PERMISSIONS =====
    public static final int PERMISSION_REQUEST_LOCATION = 2001;
    public static final int PERMISSION_REQUEST_CAMERA = 2002;
    public static final int PERMISSION_REQUEST_STORAGE = 2003;
    public static final int PERMISSION_REQUEST_NOTIFICATIONS = 2004;

    // ===== PAGINATION =====
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int SEARCH_PAGE_SIZE = 15;
    public static final int CHAT_PAGE_SIZE = 50;
    public static final int NOTIFICATION_PAGE_SIZE = 25;

    // ===== PRODUCT CONSTANTS =====
    public static final int MAX_PRODUCT_IMAGES = 10;
    public static final int MAX_PRODUCT_TITLE_LENGTH = 100;
    public static final int MAX_PRODUCT_DESCRIPTION_LENGTH = 2000;
    public static final double DEFAULT_LOCATION_RADIUS = 50.0; // km
    public static final double MAX_LOCATION_RADIUS = 200.0; // km

    // ===== FILE UPLOAD =====
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "webp"};

    // ===== CHAT CONSTANTS =====
    public static final int MAX_MESSAGE_LENGTH = 1000;
    public static final long TYPING_INDICATOR_TIMEOUT = 3000; // 3 seconds
    public static final long MESSAGE_RETRY_TIMEOUT = 30000; // 30 seconds

    // ===== UI CONSTANTS =====
    public static final long SPLASH_DISPLAY_LENGTH = 2000; // 2 seconds
    public static final long TOAST_SHORT_DURATION = 2000;
    public static final long TOAST_LONG_DURATION = 3500;

    // ===== NOTIFICATION TYPES =====
    public static final String NOTIFICATION_TYPE_NEW_MESSAGE = "new_message";
    public static final String NOTIFICATION_TYPE_NEW_OFFER = "new_offer";
    public static final String NOTIFICATION_TYPE_OFFER_ACCEPTED = "offer_accepted";
    public static final String NOTIFICATION_TYPE_OFFER_REJECTED = "offer_rejected";
    public static final String NOTIFICATION_TYPE_PRODUCT_SOLD = "product_sold";
    public static final String NOTIFICATION_TYPE_PRODUCT_LIKED = "product_liked";

    // ===== WEBSOCKET =====
    public static final String WEBSOCKET_URL = "ws://10.0.2.2:8080/ws";
    public static final long WEBSOCKET_RECONNECT_INTERVAL = 5000; // 5 seconds
    public static final int WEBSOCKET_MAX_RECONNECT_ATTEMPTS = 10;

    // ===== ERROR CODES =====
    public static final int ERROR_CODE_NETWORK = 1000;
    public static final int ERROR_CODE_SERVER = 1001;
    public static final int ERROR_CODE_UNAUTHORIZED = 1002;
    public static final int ERROR_CODE_VALIDATION = 1003;
    public static final int ERROR_CODE_FILE_UPLOAD = 1004;
    public static final int ERROR_CODE_LOCATION = 1005;

    // ===== MAP CONSTANTS =====
    public static final float DEFAULT_MAP_ZOOM = 15.0f;
    public static final float CLOSE_MAP_ZOOM = 18.0f;
    public static final int MAP_ANIMATION_DURATION = 1000;

    // ===== PRICE FORMATTING =====
    public static final String CURRENCY_SYMBOL = "₫";
    public static final String PRICE_FREE = "Free";

    // ===== DATE FORMATS =====
    public static final String DATE_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String TIME_FORMAT_DISPLAY = "HH:mm";
    public static final String DATETIME_FORMAT_DISPLAY = "MMM dd, yyyy HH:mm";

    // ===== BUNDLE KEYS =====
    public static final String BUNDLE_PRODUCT_ID = "product_id";
    public static final String BUNDLE_USER_ID = "user_id";
    public static final String BUNDLE_CATEGORY_ID = "category_id";
    public static final String BUNDLE_CONVERSATION_ID = "conversation_id";
    public static final String BUNDLE_OFFER_ID = "offer_id";
    public static final String BUNDLE_PRODUCT_TITLE = "product_title";
    public static final String BUNDLE_PRODUCT_PRICE = "product_price";
    public static final String BUNDLE_FROM_REGISTRATION = "from_registration";
    public static final String BUNDLE_EMAIL = "email";
    public static final String BUNDLE_LOCATION_LAT = "location_lat";
    public static final String BUNDLE_LOCATION_LNG = "location_lng";
    public static final String BUNDLE_LOCATION_NAME = "location_name";

    // ===== INTENT ACTIONS =====
    public static final String ACTION_REFRESH_PRODUCTS = "com.example.newtrade.REFRESH_PRODUCTS";
    public static final String ACTION_REFRESH_MESSAGES = "com.example.newtrade.REFRESH_MESSAGES";
    public static final String ACTION_REFRESH_PROFILE = "com.example.newtrade.REFRESH_PROFILE";
    public static final String ACTION_NEW_MESSAGE = "com.example.newtrade.NEW_MESSAGE";
    public static final String ACTION_NEW_OFFER = "com.example.newtrade.NEW_OFFER";

    // ===== SEARCH FILTERS =====
    public static final String SORT_NEWEST = "createdAt_desc";
    public static final String SORT_OLDEST = "createdAt_asc";
    public static final String SORT_PRICE_LOW = "price_asc";
    public static final String SORT_PRICE_HIGH = "price_desc";
    public static final String SORT_DISTANCE = "distance_asc";
    public static final String SORT_POPULARITY = "viewCount_desc";

    // Private constructor to prevent instantiation
    private Constants() {}
}