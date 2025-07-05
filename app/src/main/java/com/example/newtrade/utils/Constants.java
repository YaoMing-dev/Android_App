// app/src/main/java/com/example/newtrade/utils/Constants.java
package com.example.newtrade.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Constants {

    // =============================================
    // SERVER CONFIGURATION
    // =============================================
    public static final String BASE_URL = "http://10.0.2.2:8080"; // Emulator
    public static final String WS_BASE_URL = "ws://10.0.2.2:8080/ws";
    public static final String SOCKJS_URL = "ws://10.0.2.2:8080/sockjs";

    // =============================================
    // GOOGLE OAUTH CONFIGURATION
    // =============================================
    public static final String GOOGLE_CLIENT_ID = "638175281882-lqsdj0iur1i079l0vlqni71gelshrdgj.apps.googleusercontent.com";

    // =============================================
    // STRIPE CONFIGURATION
    // =============================================
    public static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51RcRCQRpMILatp67WGNGrZif5hI7ZrYMupx6rxrv5vpX3lrRpdtIQzck5V5yBfCHzTTwP7pIQfLb0NGvyQyiufG00o89zAvGq";

    // =============================================
    // API ENDPOINTS
    // =============================================
    public static final String API_AUTH = "/api/auth";
    public static final String API_USERS = "/api/users";
    public static final String API_PRODUCTS = "/api/products";
    public static final String API_PAYMENTS = "/api/payments";
    public static final String API_TRANSACTIONS = "/api/transactions";
    public static final String API_CONVERSATIONS = "/api/conversations";
    public static final String API_MESSAGES = "/api/messages";
    public static final String API_OFFERS = "/api/offers";
    public static final String API_REVIEWS = "/api/reviews";
    public static final String API_NOTIFICATIONS = "/api/notifications";
    public static final String API_SAVED_ITEMS = "/api/saved-items";
    public static final String API_ANALYTICS = "/api/analytics";
    public static final String API_FILES = "/api/files";

    // =============================================
    // HTTP HEADERS
    // =============================================
    public static final String HEADER_USER_ID = "User-ID";
    public static final String HEADER_STRIPE_SIGNATURE = "Stripe-Signature";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    // =============================================
    // PAGINATION
    // =============================================
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int CHAT_PAGE_SIZE = 50;
    public static final int PRODUCTS_PAGE_SIZE = 10;
    public static final int SEARCH_PAGE_SIZE = 15;

    // =============================================
    // FILE UPLOAD CONFIGURATION
    // =============================================
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    public static final int MAX_PRODUCT_IMAGES = 10;
    public static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    // =============================================
    // CURRENCY & PAYMENT
    // =============================================
    public static final String CURRENCY_VND = "VND";
    public static final String CURRENCY_USD = "USD";
    public static final String DEFAULT_CURRENCY = CURRENCY_USD;

    // =============================================
    // WEBSOCKET CONFIGURATION
    // =============================================
    public static final String WS_MESSAGE_TYPE_NEW_MESSAGE = "NEW_MESSAGE";
    public static final String WS_MESSAGE_TYPE_NOTIFICATION = "NEW_NOTIFICATION";
    public static final String WS_MESSAGE_TYPE_USER_JOINED = "USER_JOINED";
    public static final String WS_MESSAGE_TYPE_USER_LEFT = "USER_LEFT";
    public static final String WS_MESSAGE_TYPE_TYPING = "TYPING";
    public static final String WS_MESSAGE_TYPE_STOP_TYPING = "STOP_TYPING";

    // =============================================
    // OFFER CONFIGURATION
    // =============================================
    public static final int OFFER_EXPIRY_DAYS = 7;
    public static final double MIN_OFFER_PERCENTAGE = 0.1; // 10% of original price
    public static final double MAX_OFFER_PERCENTAGE = 0.9; // 90% of original price

    // =============================================
    // REVIEW CONFIGURATION
    // =============================================
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MIN_REVIEW_LENGTH = 10;
    public static final int MAX_REVIEW_LENGTH = 500;

    // =============================================
    // SEARCH CONFIGURATION
    // =============================================
    public static final int SEARCH_DELAY_MS = 200; // 200ms delay after typing
    public static final int MIN_SEARCH_QUERY_LENGTH = 2;
    public static final int MAX_RECENT_SEARCHES = 10;

    // =============================================
    // LOCATION CONFIGURATION
    // =============================================
    public static final double DEFAULT_SEARCH_RADIUS_KM = 10.0;
    public static final double MIN_SEARCH_RADIUS_KM = 1.0;
    public static final double MAX_SEARCH_RADIUS_KM = 100.0;

    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_OFFER_ID = "offer_id";
    public static final String EXTRA_REVIEW_ID = "review_id";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    // =============================================
    // VALIDATION CONSTANTS
    // =============================================
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MIN_DISPLAY_NAME_LENGTH = 2;
    public static final int MAX_DISPLAY_NAME_LENGTH = 50;
    public static final int MIN_PRODUCT_TITLE_LENGTH = 3;
    public static final int MAX_PRODUCT_TITLE_LENGTH = 100;
    public static final int MIN_PRODUCT_DESCRIPTION_LENGTH = 10;
    public static final int MAX_PRODUCT_DESCRIPTION_LENGTH = 1000;

    // =============================================
    // PRODUCT CONFIGURATION
    // =============================================
    public static final String PRODUCT_STATUS_AVAILABLE = "AVAILABLE";
    public static final String PRODUCT_STATUS_SOLD = "SOLD";
    public static final String PRODUCT_STATUS_PAUSED = "PAUSED";
    public static final String PRODUCT_STATUS_EXPIRED = "EXPIRED";

    public static final String PRODUCT_CONDITION_NEW = "NEW";
    public static final String PRODUCT_CONDITION_LIKE_NEW = "LIKE_NEW";
    public static final String PRODUCT_CONDITION_GOOD = "GOOD";
    public static final String PRODUCT_CONDITION_FAIR = "FAIR";
    public static final String PRODUCT_CONDITION_POOR = "POOR";

    // =============================================
    // NOTIFICATION CONFIGURATION
    // =============================================
    public static final String NOTIFICATION_TYPE_MESSAGE = "MESSAGE";
    public static final String NOTIFICATION_TYPE_OFFER = "OFFER";
    public static final String NOTIFICATION_TYPE_TRANSACTION = "TRANSACTION";
    public static final String NOTIFICATION_TYPE_REVIEW = "REVIEW";
    public static final String NOTIFICATION_TYPE_GENERAL = "GENERAL";

    // =============================================
    // SHARED PREFERENCES KEYS
    // =============================================
    public static final String PREF_NAME = "TradeUpPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_AVATAR = "user_avatar";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_FCM_TOKEN = "fcm_token";
    public static final String PREF_LAST_SYNC = "last_sync";

    // =============================================
    // INTENT EXTRAS
    // =============================================
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_PRODUCT_TITLE = "product_title";
    public static final String EXTRA_PRODUCT_PRICE = "product_price";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";

    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_IS_REGISTRATION = "is_registration";
    public static final String EXTRA_MESSAGE = "message";

    // Thêm vào Constants.java
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_CATEGORY_NAME = "category_name";

    // =============================================
    // REQUEST CODES
    // =============================================
    public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 9001;
    public static final int REQUEST_CODE_EDIT_PROFILE = 9002;
    public static final int REQUEST_CODE_ADD_PRODUCT = 9003;
    public static final int REQUEST_CODE_EDIT_PRODUCT = 9004;
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 9005;
    public static final int REQUEST_CODE_CAMERA_PERMISSION = 9006;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 9007;
    public static final int REQUEST_CODE_PICK_IMAGE = 9008;
    public static final int REQUEST_CODE_TAKE_PHOTO = 9009;
    public static final int REQUEST_CODE_LOCATION_PICKER = 9010;

    // =============================================
    // ERROR MESSAGES
    // =============================================
    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_GENERIC = "Something went wrong. Please try again.";
    public static final String ERROR_INVALID_EMAIL = "Please enter a valid email address.";
    public static final String ERROR_INVALID_PASSWORD = "Password must be at least 8 characters.";
    public static final String ERROR_PASSWORDS_NOT_MATCH = "Passwords do not match.";
    public static final String ERROR_REQUIRED_FIELD = "This field is required.";
    public static final String ERROR_FILE_TOO_LARGE = "File size too large. Maximum size is 10MB.";
    public static final String ERROR_INVALID_FILE_TYPE = "Invalid file type. Only images are allowed.";

    // =============================================
    // SUCCESS MESSAGES
    // =============================================
    public static final String SUCCESS_LOGIN = "Login successful!";
    public static final String SUCCESS_REGISTER = "Registration successful!";
    public static final String SUCCESS_PROFILE_UPDATE = "Profile updated successfully!";
    public static final String SUCCESS_PRODUCT_ADDED = "Product added successfully!";
    public static final String SUCCESS_PRODUCT_UPDATED = "Product updated successfully!";
    public static final String SUCCESS_PRODUCT_DELETED = "Product deleted successfully!";
    public static final String SUCCESS_MESSAGE_SENT = "Message sent successfully!";
    public static final String SUCCESS_OFFER_SENT = "Offer sent successfully!";
    public static final String SUCCESS_REVIEW_SUBMITTED = "Review submitted successfully!";

    // =============================================
    // FIREBASE CONFIGURATION
    // =============================================
    public static final String FCM_TOPIC_ALL_USERS = "all_users";
    public static final String FCM_TOPIC_NOTIFICATIONS = "notifications";
    public static final String FCM_TOPIC_OFFERS = "offers";
    public static final String FCM_TOPIC_MESSAGES = "messages";

    // =============================================
    // CACHE CONFIGURATION
    // =============================================
    public static final int CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final int CACHE_MAX_AGE = 60 * 60 * 24; // 1 day
    public static final int CACHE_MAX_STALE = 60 * 60 * 24 * 7; // 1 week

    // =============================================
    // ANIMATION DURATIONS
    // =============================================
    public static final int ANIMATION_DURATION_SHORT = 150;
    public static final int ANIMATION_DURATION_MEDIUM = 300;
    public static final int ANIMATION_DURATION_LONG = 500;

    // =============================================
    // TIMEOUTS
    // =============================================
    public static final int NETWORK_TIMEOUT = 30; // 30 seconds
    public static final int WEBSOCKET_TIMEOUT = 10; // 10 seconds
    public static final int IMAGE_LOAD_TIMEOUT = 15; // 15 seconds

    // =============================================
    // UTILITY METHODS
    // =============================================
    public static String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        if (imagePath.startsWith("http")) {
            return imagePath;
        }

        return BASE_URL + imagePath;
    }

    public static String formatPrice(double price) {
        return String.format("$%.2f", price);
    }

    public static String formatPriceVND(double price) {
        return String.format("₫%.0f", price);
    }

    public static String formatPrice(java.math.BigDecimal price) {
        if (price == null || price.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return "Free";
        }

        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
        return formatter.format(price) + " ₫";
    }

    // Compatibility method for double


    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }





    // =============================================
    // INTENT EXTRAS
    // =============================================

    // =============================================
    // REQUEST CODES
    // =============================================


    // =============================================
    // PRODUCT CATEGORIES - ✅ THÊM ARRAY BỊ THIẾU
    // =============================================
    public static final String[] PRODUCT_CATEGORIES = {
            "Electronics",
            "Clothing & Fashion",
            "Home & Garden",
            "Sports & Outdoors",
            "Books & Education",
            "Health & Beauty",
            "Automotive",
            "Toys & Games",
            "Music & Instruments",
            "Art & Crafts",
            "Baby & Kids",
            "Food & Beverages",
            "Services",
            "Other"
    };

    // =============================================
    // PRODUCT CONDITIONS
    // =============================================
    public static final String[] PRODUCT_CONDITIONS = {
            "NEW",
            "LIKE_NEW",
            "GOOD",
            "FAIR",
            "POOR"
    };

    // =============================================
    // ERROR MESSAGES
    // =============================================

    // =============================================
    // SUCCESS MESSAGES
    // =============================================


    // =============================================
    // UTILITY METHODS
    // =============================================

}