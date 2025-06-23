package com.example.newtrade.utils;

public class Constants {

    // ===== API CONFIGURATION =====
    public static final String BASE_URL = "http://10.0.2.2:8080/"; // Android Emulator
    // public static final String BASE_URL = "http://192.168.1.100:8080/"; // Real device - thay IP máy của bạn

    // API Endpoints
    public static final String API_AUTH = "api/auth/";
    public static final String API_USERS = "api/users/";
    public static final String API_PRODUCTS = "api/products/";
    public static final String API_CATEGORIES = "api/categories/";
    public static final String API_CONVERSATIONS = "api/conversations/";
    public static final String API_MESSAGES = "api/messages/";
    public static final String API_OFFERS = "api/offers/";
    public static final String API_FILES = "api/files/";

    // ===== SHARED PREFERENCES =====
    public static final String PREFS_NAME = "TradeUpPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_FCM_TOKEN = "fcm_token";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_IS_EMAIL_VERIFIED = "is_email_verified";

    // ===== GOOGLE OAUTH =====
    public static final String GOOGLE_CLIENT_ID = "197776863490-0j3eukit867ircvr4nbddjrurf7c2gr0.apps.googleusercontent.com";

    // ===== REQUEST CODES =====
    public static final int RC_GOOGLE_SIGN_IN = 1001;
    public static final int RC_PICK_IMAGE = 1002;
    public static final int RC_CAMERA = 1003;
    public static final int RC_LOCATION_PICKER = 1004;

    // ===== FIREBASE =====
    public static final String FCM_TOPIC_ALL_USERS = "all_users";
    public static final String FCM_TOPIC_OFFERS = "offers";
    public static final String FCM_TOPIC_MESSAGES = "messages";

    // ===== VALIDATION =====
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int OTP_LENGTH = 6;
    public static final int OTP_RESEND_TIME_SECONDS = 60;

    // ===== FILE UPLOAD =====
    public static final long MAX_IMAGE_SIZE_MB = 10;
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/jpg"};

    // ===== PAGINATION =====
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int SEARCH_DELAY_MS = 300; // Delay for search typing

    // ===== ERROR MESSAGES =====
    public static final String ERROR_NETWORK = "Kiểm tra kết nối mạng và thử lại";
    public static final String ERROR_SERVER = "Lỗi server, vui lòng thử lại sau";
    public static final String ERROR_UNAUTHORIZED = "Phiên đăng nhập hết hạn";
    public static final String ERROR_UNKNOWN = "Có lỗi xảy ra, vui lòng thử lại";

    // ===== PRODUCT CONDITIONS =====
    public static final String[] PRODUCT_CONDITIONS = {
            "NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"
    };

    public static final String[] PRODUCT_CONDITION_DISPLAY = {
            "Mới", "Như mới", "Tốt", "Khá", "Cũ"
    };

    // ===== OFFER STATUS =====
    public static final String OFFER_PENDING = "PENDING";
    public static final String OFFER_ACCEPTED = "ACCEPTED";
    public static final String OFFER_REJECTED = "REJECTED";
    public static final String OFFER_COUNTERED = "COUNTERED";
    public static final String OFFER_EXPIRED = "EXPIRED";

    // ===== CHAT =====
    public static final String MESSAGE_TYPE_TEXT = "TEXT";
    public static final String MESSAGE_TYPE_IMAGE = "IMAGE";
    public static final String MESSAGE_TYPE_OFFER = "OFFER";

    // ===== WEBSOCKET =====
    public static final String WEBSOCKET_URL = "ws://10.0.2.2:8080/ws";
    // public static final String WEBSOCKET_URL = "ws://192.168.1.100:8080/ws"; // Real device

    // Helper methods
    public static String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "";
        }
        if (imagePath.startsWith("http")) {
            return imagePath;
        }
        return BASE_URL + imagePath.substring(1); // Remove leading slash
    }

    public static String getProductConditionDisplay(String condition) {
        for (int i = 0; i < PRODUCT_CONDITIONS.length; i++) {
            if (PRODUCT_CONDITIONS[i].equals(condition)) {
                return PRODUCT_CONDITION_DISPLAY[i];
            }
        }
        return condition;
    }
}