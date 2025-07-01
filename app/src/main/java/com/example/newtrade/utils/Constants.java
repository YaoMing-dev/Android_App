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
    public static final String WS_BASE_URL = "ws://10.0.2.2:8080";
    public static final String WEBSOCKET_URL = WS_BASE_URL + "/ws";

    // File Upload URLs
    public static final String UPLOAD_URL = BASE_URL + "uploads/";
    public static final String PRODUCT_IMAGES_URL = UPLOAD_URL + "products/";
    public static final String AVATAR_IMAGES_URL = UPLOAD_URL + "avatars/";
    public static final String CHAT_IMAGES_URL = UPLOAD_URL + "chat/";

    // =============================================
    // AUTHENTICATION & SECURITY
    // =============================================

    // Google Services
    public static final String GOOGLE_CLIENT_ID = "638175281882-lqsdj0iur1i079l0vlqni71gelshrdgj.apps.googleusercontent.com";

    // Session Management
    public static final long SESSION_TIMEOUT = 30L * 24 * 60 * 60 * 1000; // 30 days in milliseconds
    public static final long AUTH_TOKEN_REFRESH_THRESHOLD = 24 * 60 * 60 * 1000; // 24 hours
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOGIN_LOCKOUT_DURATION = 15 * 60 * 1000; // 15 minutes

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
    public static final int RC_PHONE_PERMISSION = 1008;
    public static final int RC_MICROPHONE_PERMISSION = 1009;

    // Activity Request Codes
    public static final int RC_PRODUCT_DETAIL = 2001;
    public static final int RC_EDIT_PRODUCT = 2002;
    public static final int RC_EDIT_PROFILE = 2003;
    public static final int RC_SETTINGS = 2004;
    public static final int RC_LOCATION_PICKER = 2005;

    // =============================================
    // NETWORK CONFIGURATION
    // =============================================

    public static final int NETWORK_TIMEOUT = 30; // seconds
    public static final int UPLOAD_TIMEOUT = 120; // seconds for file uploads
    public static final int DOWNLOAD_TIMEOUT = 60; // seconds for downloads
    public static final int RETRY_COUNT = 3;
    public static final long RETRY_DELAY = 2000; // milliseconds
    public static final int MAX_CONCURRENT_REQUESTS = 5;

    // =============================================
    // PAGINATION & LIMITS
    // =============================================

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_SIZE = 5;

    // Home Screen Limits
    public static final int HOME_RECENT_LIMIT = 10;
    public static final int HOME_FEATURED_LIMIT = 8;
    public static final int HOME_CATEGORIES_LIMIT = 12;

    // Other Limits
    public static final int SEARCH_PAGE_SIZE = 15;
    public static final int CHAT_PAGE_SIZE = 50;
    public static final int OFFERS_PAGE_SIZE = 20;
    public static final int TRANSACTIONS_PAGE_SIZE = 15;

    // =============================================
    // VALIDATION CONSTANTS
    // =============================================

    // Password Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 50;
    public static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,}$";

    // Product Validation
    public static final int MIN_PRODUCT_TITLE_LENGTH = 3;
    public static final int MAX_PRODUCT_TITLE_LENGTH = 100;
    public static final int MIN_PRODUCT_DESCRIPTION_LENGTH = 10;
    public static final int MAX_PRODUCT_DESCRIPTION_LENGTH = 2000;
    public static final double MIN_PRODUCT_PRICE = 1000.0; // VND
    public static final double MAX_PRODUCT_PRICE = 999999999.0; // VND

    // User Validation
    public static final int MIN_DISPLAY_NAME_LENGTH = 2;
    public static final int MAX_DISPLAY_NAME_LENGTH = 50;
    public static final int MAX_BIO_LENGTH = 500;
    public static final int MAX_CONTACT_INFO_LENGTH = 200;

    // Search & History
    public static final int MAX_SEARCH_HISTORY = 20;
    public static final int MIN_SEARCH_QUERY_LENGTH = 2;
    public static final int MAX_SEARCH_QUERY_LENGTH = 100;

    // =============================================
    // FILE UPLOAD CONFIGURATION
    // =============================================

    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    public static final long MAX_CHAT_IMAGE_SIZE = 8 * 1024 * 1024; // 8MB
    public static final int MAX_IMAGES_PER_PRODUCT = 10;
    public static final int MAX_IMAGES_PER_MESSAGE = 5;

    // Image Processing
    public static final int IMAGE_COMPRESSION_QUALITY = 85;
    public static final int MAX_IMAGE_WIDTH = 1200;
    public static final int MAX_IMAGE_HEIGHT = 1200;
    public static final int THUMBNAIL_SIZE = 300;
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "webp", "gif"};

    // =============================================
    // LOCATION CONFIGURATION
    // =============================================

    public static final double DEFAULT_LATITUDE = 10.8231; // Ho Chi Minh City
    public static final double DEFAULT_LONGITUDE = 106.6297;
    public static final float DEFAULT_SEARCH_RADIUS = 25.0f; // km
    public static final float MIN_SEARCH_RADIUS = 1.0f; // km
    public static final float MAX_SEARCH_RADIUS = 100.0f; // km
    public static final long LOCATION_UPDATE_INTERVAL = 30000; // 30 seconds
    public static final long LOCATION_FASTEST_INTERVAL = 10000; // 10 seconds
    public static final float MIN_LOCATION_ACCURACY = 100.0f; // meters

    // =============================================
    // CHAT & MESSAGING
    // =============================================

    public static final int MAX_MESSAGE_LENGTH = 1000;
    public static final long TYPING_TIMEOUT = 3000; // milliseconds
    public static final long MESSAGE_RETRY_DELAY = 2000; // milliseconds
    public static final long WEBSOCKET_RECONNECT_DELAY = 5000; // milliseconds
    public static final int MAX_RECONNECT_ATTEMPTS = 5;
    public static final long CHAT_SYNC_INTERVAL = 30000; // 30 seconds
    public static final int MAX_UNREAD_MESSAGES = 999;

    // =============================================
    // OFFERS & TRANSACTIONS
    // =============================================

    public static final long OFFER_EXPIRY_DAYS = 7; // days
    public static final double MIN_OFFER_PERCENTAGE = 0.1; // 10% of original price
    public static final double MAX_OFFER_PERCENTAGE = 0.95; // 95% of original price
    public static final long TRANSACTION_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours
    public static final int MAX_OFFERS_PER_PRODUCT = 50;

    // =============================================
    // RATING & REVIEWS
    // =============================================

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MAX_REVIEW_LENGTH = 500;
    public static final int MIN_REVIEW_LENGTH = 10;

    // =============================================
    // CACHING CONFIGURATION
    // =============================================

    public static final long CACHE_CATEGORIES_DURATION = 60 * 60 * 1000; // 1 hour
    public static final long CACHE_USER_STATS_DURATION = 30 * 60 * 1000; // 30 minutes
    public static final long CACHE_PRODUCT_DURATION = 15 * 60 * 1000; // 15 minutes
    public static final long CACHE_SEARCH_DURATION = 10 * 60 * 1000; // 10 minutes
    public static final int MAX_CACHE_SIZE = 50; // number of items

    // =============================================
    // UI CONFIGURATION
    // =============================================

    // Animation Durations
    public static final int ANIMATION_DURATION_SHORT = 150;
    public static final int ANIMATION_DURATION_MEDIUM = 300;
    public static final int ANIMATION_DURATION_LONG = 500;

    // Loading & Refresh
    public static final long MIN_LOADING_TIME = 500; // milliseconds
    public static final long REFRESH_THROTTLE = 2000; // milliseconds
    public static final int SHIMMER_ITEM_COUNT = 6;

    // =============================================
    // API HEADERS
    // =============================================

    public static final String HEADER_USER_ID = "User-ID";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_DEVICE_ID = "Device-ID";
    public static final String HEADER_APP_VERSION = "App-Version";
    public static final String HEADER_PLATFORM = "Platform";

    // Content Types
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

    // =============================================
    // WEBSOCKET CONFIGURATION
    // =============================================

    public static final String WS_TOPIC_CHAT_PREFIX = "/topic/chat/";
    public static final String WS_TOPIC_USER_PREFIX = "/topic/user/";
    public static final String WS_TOPIC_OFFERS_PREFIX = "/topic/offers/";
    public static final String WS_TOPIC_NOTIFICATIONS_PREFIX = "/topic/notifications/";

    public static final String WS_APP_CHAT = "/app/chat";
    public static final String WS_APP_TYPING = "/app/typing";
    public static final String WS_APP_LOCATION = "/app/location";

    // =============================================
    // NOTIFICATION CONFIGURATION
    // =============================================

    // Notification Channels
    public static final String NOTIFICATION_CHANNEL_MESSAGES = "messages";
    public static final String NOTIFICATION_CHANNEL_OFFERS = "offers";
    public static final String NOTIFICATION_CHANNEL_GENERAL = "general";
    public static final String NOTIFICATION_CHANNEL_LISTINGS = "listings";
    public static final String NOTIFICATION_CHANNEL_SYSTEM = "system";

    // Notification IDs
    public static final int NOTIFICATION_ID_MESSAGE = 1001;
    public static final int NOTIFICATION_ID_OFFER = 1002;
    public static final int NOTIFICATION_ID_GENERAL = 1003;
    public static final int NOTIFICATION_ID_SYNC = 1004;

    // =============================================
    // SHARED PREFERENCES KEYS
    // =============================================

    public static final String PREF_FIRST_LAUNCH = "first_launch";
    public static final String PREF_ONBOARDING_COMPLETED = "onboarding_completed";
    public static final String PREF_LOCATION_PERMISSION_ASKED = "location_permission_asked";
    public static final String PREF_NOTIFICATION_PERMISSION_ASKED = "notification_permission_asked";
    public static final String PREF_TUTORIAL_SHOWN = "tutorial_shown";

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
    public static final String EXTRA_MESSAGE_ID = "message_id";
    public static final String EXTRA_NOTIFICATION_TYPE = "notification_type";

    // =============================================
    // ACTIVITY CLASSES (for navigation)
    // =============================================

    public static final String ACTIVITY_MAIN = "com.example.newtrade.MainActivity";
    public static final String ACTIVITY_LOGIN = "com.example.newtrade.ui.auth.LoginActivity";
    public static final String ACTIVITY_REGISTER = "com.example.newtrade.ui.auth.RegisterActivity";
    public static final String ACTIVITY_SEARCH = "com.example.newtrade.ui.search.SearchActivity";
    public static final String ACTIVITY_PRODUCT_DETAIL = "com.example.newtrade.ui.product.ProductDetailActivity";
    public static final String ACTIVITY_CATEGORY_PRODUCTS = "com.example.newtrade.ui.product.CategoryProductsActivity";
    public static final String ACTIVITY_ALL_PRODUCTS = "com.example.newtrade.ui.search.AllProductsActivity";
    public static final String ACTIVITY_ALL_CATEGORIES = "com.example.newtrade.ui.home.AllCategoriesActivity";
    public static final String ACTIVITY_EDIT_PROFILE = "com.example.newtrade.ui.profile.EditProfileActivity";
    public static final String ACTIVITY_SETTINGS = "com.example.newtrade.ui.profile.SettingsActivity";
    public static final String ACTIVITY_CHAT = "com.example.newtrade.ui.chat.ChatActivity";

    // =============================================
    // ERROR & SUCCESS MESSAGES
    // =============================================

    // Error Messages
    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_SERVER = "Server error. Please try again later.";
    public static final String ERROR_UNAUTHORIZED = "Session expired. Please login again.";
    public static final String ERROR_VALIDATION = "Please check your input and try again.";
    public static final String ERROR_UNKNOWN = "An unexpected error occurred.";
    public static final String ERROR_IMAGE_UPLOAD = "Failed to upload image. Please try again.";
    public static final String ERROR_LOCATION = "Unable to get your location. Please try again.";
    public static final String ERROR_PERMISSION_DENIED = "Permission denied. Please allow access in settings.";
    public static final String ERROR_FILE_TOO_LARGE = "File too large. Maximum size is %.1f MB.";
    public static final String ERROR_INVALID_FILE_TYPE = "Invalid file type. Only images are allowed.";

    // Success Messages
    public static final String SUCCESS_LOGIN = "Login successful!";
    public static final String SUCCESS_REGISTER = "Registration successful!";
    public static final String SUCCESS_LOGOUT = "Logged out successfully.";
    public static final String SUCCESS_PRODUCT_ADDED = "Product listed successfully!";
    public static final String SUCCESS_PRODUCT_UPDATED = "Product updated successfully!";
    public static final String SUCCESS_PROFILE_UPDATED = "Profile updated successfully!";
    public static final String SUCCESS_IMAGE_UPLOADED = "Image uploaded successfully!";
    public static final String SUCCESS_MESSAGE_SENT = "Message sent successfully!";
    public static final String SUCCESS_OFFER_SENT = "Offer sent successfully!";

    // =============================================
    // DEBUG FLAGS
    // =============================================

    public static final boolean DEBUG_MODE = true; // Set to false for production
    public static final boolean ENABLE_LOGGING = true;
    public static final boolean ENABLE_CRASH_REPORTING = false; // Set to true for production
    public static final boolean ENABLE_ANALYTICS = false; // Set to true for production

    // =============================================
    // UTILITY METHODS
    // =============================================

    /**
     * Get condition display name
     */
    public static String getConditionDisplayName(String condition) {
        if (condition == null) return "Unknown";

        switch (condition.toUpperCase()) {
            case "NEW": return "New";
            case "LIKE_NEW": return "Like New";
            case "GOOD": return "Good";
            case "FAIR": return "Fair";
            case "POOR": return "Poor";
            default: return condition;
        }
    }

    /**
     * Get status display name
     */
    public static String getStatusDisplayName(String status) {
        if (status == null) return "Unknown";

        switch (status.toUpperCase()) {
            case "AVAILABLE": return "Available";
            case "SOLD": return "Sold";
            case "RESERVED": return "Reserved";
            case "ARCHIVED": return "Archived";
            case "DELETED": return "Deleted";
            default: return status;
        }
    }

    /**
     * Format file size for display
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    /**
     * Check if string is a valid email
     */
    public static boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Check if string is a valid phone number (Vietnamese format)
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        return phone.matches("^(\\+84|84|0)(3|5|7|8|9)[0-9]{8}$");
    }
}