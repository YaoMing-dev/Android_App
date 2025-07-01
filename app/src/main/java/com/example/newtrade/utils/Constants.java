// app/src/main/java/com/example/newtrade/utils/Constants.java
package com.example.newtrade.utils;

public class Constants {

    // =============================================
    // API CONFIGURATION
    // =============================================

    // Backend URLs
    public static final String BASE_URL = "http://10.0.2.2:8080/"; // For Android Emulator
    // public static final String BASE_URL = "http://192.168.1.100:8080/"; // For Real Device
    public static final String API_BASE_URL = BASE_URL + "api/";

    // WebSocket URLs
    public static final String WEBSOCKET_URL = "ws://10.0.2.2:8080/ws";
    // public static final String WEBSOCKET_URL = "ws://192.168.1.100:8080/ws";

    // File Upload URLs
    public static final String UPLOAD_URL = BASE_URL + "uploads/";
    public static final String PRODUCT_IMAGES_URL = UPLOAD_URL + "products/";
    public static final String AVATAR_IMAGES_URL = UPLOAD_URL + "avatars/";

    // =============================================
    // GOOGLE SERVICES
    // =============================================

    // ✅ UPDATED: Real Google Sign-In Client ID
    public static final String GOOGLE_CLIENT_ID = "638175281882-lqsdj0iur1i079l0vlqni71gelshrdgj.apps.googleusercontent.com";

    // =============================================
    // REQUEST CODES
    // =============================================

    public static final int RC_GOOGLE_SIGN_IN = 1001;
    public static final int RC_PICK_IMAGE = 1002;
    public static final int RC_TAKE_PHOTO = 1003;
    public static final int RC_LOCATION_PERMISSION = 1004;
    public static final int RC_CAMERA_PERMISSION = 1005;
    public static final int RC_STORAGE_PERMISSION = 1006;
    public static final int RC_NOTIFICATION_PERMISSION = 1007;

    // =============================================
    // NETWORK CONFIGURATION
    // =============================================

    public static final int NETWORK_TIMEOUT = 30; // seconds
    public static final int RETRY_COUNT = 3;
    public static final int UPLOAD_TIMEOUT = 60; // seconds for file uploads

    // =============================================
    // PAGINATION
    // =============================================

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int SEARCH_PAGE_SIZE = 15;
    public static final int CHAT_PAGE_SIZE = 50;

    // =============================================
    // VALIDATION CONSTANTS
    // =============================================

    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 50;
    public static final int MIN_PRODUCT_TITLE_LENGTH = 3;
    public static final int MAX_PRODUCT_TITLE_LENGTH = 100;
    public static final int MAX_PRODUCT_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_IMAGES_PER_PRODUCT = 10;
    public static final int MIN_DISPLAY_NAME_LENGTH = 2;
    public static final int MAX_DISPLAY_NAME_LENGTH = 50;
    public static final int MAX_BIO_LENGTH = 500;

    // =============================================
    // FILE UPLOAD LIMITS
    // =============================================

    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "webp"};
    public static final int IMAGE_COMPRESSION_QUALITY = 85;
    public static final int MAX_IMAGE_WIDTH = 1024;
    public static final int MAX_IMAGE_HEIGHT = 1024;

    // =============================================
    // LOCATION CONSTANTS
    // =============================================

    public static final double DEFAULT_LATITUDE = 10.8231; // Ho Chi Minh City
    public static final double DEFAULT_LONGITUDE = 106.6297;
    public static final float DEFAULT_SEARCH_RADIUS = 50.0f; // km
    public static final float MIN_SEARCH_RADIUS = 1.0f; // km
    public static final float MAX_SEARCH_RADIUS = 100.0f; // km
    public static final long LOCATION_UPDATE_INTERVAL = 30000; // 30 seconds
    public static final long LOCATION_FASTEST_INTERVAL = 10000; // 10 seconds

    // =============================================
    // CHAT & MESSAGING
    // =============================================

    public static final int MAX_MESSAGE_LENGTH = 1000;
    public static final long TYPING_TIMEOUT = 3000; // milliseconds
    public static final long MESSAGE_RETRY_DELAY = 2000; // milliseconds
    public static final long WEBSOCKET_RECONNECT_DELAY = 5000; // milliseconds
    public static final int MAX_RECONNECT_ATTEMPTS = 5;

    // =============================================
    // NOTIFICATION CHANNELS
    // =============================================

    public static final String NOTIFICATION_CHANNEL_MESSAGES = "messages";
    public static final String NOTIFICATION_CHANNEL_OFFERS = "offers";
    public static final String NOTIFICATION_CHANNEL_GENERAL = "general";
    public static final String NOTIFICATION_CHANNEL_LISTINGS = "listings";

    // =============================================
    // SHARED PREFERENCES KEYS
    // =============================================

    public static final String PREF_FIRST_LAUNCH = "first_launch";
    public static final String PREF_ONBOARDING_COMPLETED = "onboarding_completed";
    public static final String PREF_LOCATION_PERMISSION_ASKED = "location_permission_asked";
    public static final String PREF_NOTIFICATION_PERMISSION_ASKED = "notification_permission_asked";

    // =============================================
    // INTENT EXTRAS
    // =============================================

    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_CATEGORY_NAME = "category_name";
    public static final String EXTRA_SEARCH_QUERY = "search_query";
    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_FROM_REGISTER = "from_register";
    public static final String EXTRA_PRODUCT_TITLE = "product_title";
    public static final String EXTRA_SELLER_NAME = "seller_name";
    public static final String EXTRA_OFFER_ID = "offer_id";

    // =============================================
    // ERROR MESSAGES
    // =============================================

    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_SERVER = "Server error. Please try again later.";
    public static final String ERROR_UNAUTHORIZED = "Session expired. Please login again.";
    public static final String ERROR_VALIDATION = "Please check your input and try again.";
    public static final String ERROR_UNKNOWN = "An unexpected error occurred.";
    public static final String ERROR_IMAGE_UPLOAD = "Failed to upload image. Please try again.";
    public static final String ERROR_LOCATION = "Unable to get your location. Please try again.";
    public static final String ERROR_PERMISSION_DENIED = "Permission denied. Please allow access in settings.";

    // =============================================
    // SUCCESS MESSAGES
    // =============================================

    public static final String SUCCESS_LOGIN = "Login successful!";
    public static final String SUCCESS_REGISTER = "Registration successful!";
    public static final String SUCCESS_LOGOUT = "Logged out successfully.";
    public static final String SUCCESS_PRODUCT_ADDED = "Product listed successfully!";
    public static final String SUCCESS_PRODUCT_UPDATED = "Product updated successfully!";
    public static final String SUCCESS_PROFILE_UPDATED = "Profile updated successfully!";
    public static final String SUCCESS_OFFER_SENT = "Offer sent successfully!";
    public static final String SUCCESS_OFFER_ACCEPTED = "Offer accepted!";
    public static final String SUCCESS_OFFER_REJECTED = "Offer rejected.";
    public static final String SUCCESS_MESSAGE_SENT = "Message sent!";

    // =============================================
    // PRODUCT RELATED
    // =============================================

    public static final String[] PRODUCT_CONDITIONS = {
            "NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"
    };

    public static final String[] PRODUCT_CONDITIONS_DISPLAY = {
            "New", "Like New", "Good", "Fair", "Poor"
    };

    public static final String[] SORT_OPTIONS = {
            "Recent", "Price: Low to High", "Price: High to Low", "Most Popular", "Nearest"
    };

    public static final String[] SORT_VALUES = {
            "createdAt,desc", "price,asc", "price,desc", "viewCount,desc", "distance,asc"
    };

    // =============================================
    // PRICE FILTERS (VND)
    // =============================================

    public static final long[] PRICE_RANGES = {
            0, 100000, 500000, 1000000, 5000000, 10000000, 50000000, Long.MAX_VALUE
    };

    public static final String[] PRICE_RANGE_LABELS = {
            "Under 100K", "100K - 500K", "500K - 1M", "1M - 5M",
            "5M - 10M", "10M - 50M", "Over 50M"
    };

    // =============================================
    // ANIMATION DURATIONS
    // =============================================

    public static final int ANIMATION_DURATION_SHORT = 200;
    public static final int ANIMATION_DURATION_MEDIUM = 300;
    public static final int ANIMATION_DURATION_LONG = 500;
    public static final int SPLASH_DURATION = 2000;

    // =============================================
    // SEARCH & FILTER
    // =============================================

    public static final long SEARCH_DEBOUNCE_DELAY = 500; // milliseconds
    public static final int MIN_SEARCH_QUERY_LENGTH = 2;
    public static final int MAX_SEARCH_HISTORY = 10;

    // =============================================
    // OFFERS & TRANSACTIONS
    // =============================================

    public static final long OFFER_EXPIRY_DAYS = 7; // days
    public static final double MIN_OFFER_PERCENTAGE = 0.1; // 10% of original price
    public static final double MAX_OFFER_PERCENTAGE = 0.95; // 95% of original price

    // =============================================
    // RATING & REVIEWS
    // =============================================

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MAX_REVIEW_LENGTH = 500;

    // =============================================
    // API HEADERS
    // =============================================

    public static final String HEADER_USER_ID = "User-ID";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    // =============================================
    // WEBSOCKET TOPICS
    // =============================================

    public static final String WS_TOPIC_CHAT_PREFIX = "/topic/chat/";
    public static final String WS_TOPIC_USER_PREFIX = "/topic/user/";
    public static final String WS_TOPIC_OFFERS_PREFIX = "/topic/offers/";
    public static final String WS_APP_CHAT = "/app/chat";
    public static final String WS_APP_TYPING = "/app/typing";

    // =============================================
    // DEBUG FLAGS
    // =============================================

    public static final boolean DEBUG_MODE = true; // Set to false for production
    public static final boolean ENABLE_LOGGING = true;
    public static final boolean ENABLE_CRASH_REPORTING = false; // Enable in production

    // =============================================
    // APP VERSION & BUILD INFO
    // =============================================

    public static final String APP_NAME = "TradeUp";
    public static final String APP_VERSION = "1.0.0";
    public static final String SUPPORT_EMAIL = "support@tradeup.com";
    public static final String PRIVACY_POLICY_URL = "https://tradeup.com/privacy";
    public static final String TERMS_OF_SERVICE_URL = "https://tradeup.com/terms";

    // =============================================
    // CACHE SETTINGS
    // =============================================

    public static final long CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long CACHE_MAX_AGE = 24 * 60 * 60; // 24 hours
    public static final long CACHE_MAX_STALE = 7 * 24 * 60 * 60; // 7 days

    // =============================================
    // BUNDLE KEYS
    // =============================================

    public static final String BUNDLE_PRODUCT = "bundle_product";
    public static final String BUNDLE_USER = "bundle_user";
    public static final String BUNDLE_CONVERSATION = "bundle_conversation";
    public static final String BUNDLE_CATEGORY = "bundle_category";
    public static final String BUNDLE_FILTER = "bundle_filter";

    // =============================================
    // PRIVATE CONSTRUCTOR
    // =============================================

    private Constants() {
        // Prevent instantiation
    }

    // =============================================
    // UTILITY METHODS
    // =============================================

    /**
     * Get product condition display name
     */
    public static String getConditionDisplayName(String condition) {
        if (condition == null) return "Unknown";

        for (int i = 0; i < PRODUCT_CONDITIONS.length; i++) {
            if (PRODUCT_CONDITIONS[i].equals(condition)) {
                return PRODUCT_CONDITIONS_DISPLAY[i];
            }
        }
        return condition;
    }

    /**
     * Get sort value from display name
     */
    public static String getSortValue(String displayName) {
        for (int i = 0; i < SORT_OPTIONS.length; i++) {
            if (SORT_OPTIONS[i].equals(displayName)) {
                return SORT_VALUES[i];
            }
        }
        return SORT_VALUES[0]; // Default to recent
    }

    /**
     * Format price for display
     */
    public static String formatPrice(double price) {
        if (price < 1000) {
            return String.format("%.0f VNĐ", price);
        } else if (price < 1000000) {
            return String.format("%.0fK VNĐ", price / 1000);
        } else {
            return String.format("%.1fM VNĐ", price / 1000000);
        }
    }

    /**
     * Get price range label
     */
    public static String getPriceRangeLabel(long minPrice, long maxPrice) {
        for (int i = 0; i < PRICE_RANGES.length - 1; i++) {
            if (minPrice >= PRICE_RANGES[i] && maxPrice <= PRICE_RANGES[i + 1]) {
                return PRICE_RANGE_LABELS[i];
            }
        }
        return "Custom Range";
    }

    /**
     * Check if debug mode is enabled
     */
    public static boolean isDebugMode() {
        return DEBUG_MODE;
    }

    /**
     * Get full image URL
     */
    public static String getFullImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        if (imagePath.startsWith("http")) {
            return imagePath;
        }

        return UPLOAD_URL + imagePath;
    }
}