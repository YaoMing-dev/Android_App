// app/src/main/java/com/example/newtrade/utils/Constants.java
package com.example.newtrade.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Constants {

    private static final String TAG = "Constants";

    // ===== API CONFIGURATION =====
    public static final String BASE_URL = "http://10.0.2.2:8080/";
    public static final String WS_BASE_URL = "ws://10.0.2.2:8080"; // ✅ Fixed for WebSocket

    // ===== GOOGLE OAUTH - UPDATED =====
    public static final String GOOGLE_CLIENT_ID =
            "197776863490-0j3eukit867ircvr4nbddjrurf7c2gr0.apps.googleusercontent.com";

    // ===== API ENDPOINTS - UPDATED =====
    public static final String API_AUTH = "/api/auth";
    public static final String API_USERS = "/api/users";
    public static final String API_PRODUCTS = "/api/products";
    public static final String API_CATEGORIES = "/api/categories";
    public static final String API_FILES = "/api/files";
    public static final String API_MESSAGES = "/api/messages"; // ✅ NEW
    public static final String API_CONVERSATIONS = "/api/conversations"; // ✅ NEW
    public static final String API_NOTIFICATIONS = "/api/notifications"; // ✅ NEW
    public static final String API_OFFERS = "/api/offers";
    public static final String API_TRANSACTIONS = "/api/transactions";
    public static final String API_REVIEWS = "/api/reviews";
    public static final String API_SAVED_ITEMS = "/api/saved-items";
    public static final String API_ANALYTICS = "/api/analytics";

    // ===== WEBSOCKET TOPICS - NEW =====
    public static final String TOPIC_NOTIFICATIONS = "/user/queue/notifications";
    public static final String TOPIC_CONVERSATION = "/topic/conversation/";
    public static final String TOPIC_MESSAGES = "/topic/messages";

    // ===== WEBSOCKET SEND DESTINATIONS - NEW =====
    public static final String SEND_MESSAGE = "/app/send/message";
    public static final String SEND_NOTIFICATION = "/app/send/notification";
    public static final String JOIN_CONVERSATION = "/app/join/conversation/";
    public static final String LEAVE_CONVERSATION = "/app/leave/conversation/";

    // ===== SHARED PREFERENCES =====
    public static final String PREFS_NAME = "TradeUpPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_PROFILE_PICTURE = "user_profile_picture";
    public static final String PREF_FCM_TOKEN = "fcm_token";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_IS_EMAIL_VERIFIED = "is_email_verified";

    // ===== REQUEST CODES =====
    public static final int RC_GOOGLE_SIGN_IN = 1001;
    public static final int RC_PICK_IMAGE = 1002;
    public static final int RC_CAMERA = 1003;
    public static final int RC_LOCATION_PICKER = 1004;

    // ===== HEADERS =====
    public static final String HEADER_USER_ID = "User-ID";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    // ===== VALIDATION =====
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int OTP_LENGTH = 6;
    public static final int OTP_RESEND_TIME_SECONDS = 60;

    // ===== FILE UPLOAD - UPDATED =====
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB for products
    public static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB for avatars
    public static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    // ===== PAGINATION =====
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";
    public static final int SEARCH_DELAY_MS = 200;
    public static final int MIN_SEARCH_LENGTH = 2;

    // ===== LOCATION =====
    public static final int DEFAULT_LOCATION_RADIUS = 10; // km
    public static final int MAX_LOCATION_RADIUS = 100; // km

    // ===== MESSAGING =====
    public static final int MESSAGE_POLLING_INTERVAL = 3000; // 3 seconds
    public static final int MAX_MESSAGE_LENGTH = 1000;

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

    // ===== PRODUCT STATUS =====
    public static final String[] PRODUCT_STATUS = {
            "AVAILABLE", "SOLD", "PAUSED", "DELETED"
    };

    // ===== UTILITY METHODS =====

    public static boolean checkNetworkAndLog(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Log.d(TAG, "✅ Network connected: " + activeNetwork.getTypeName());
        } else {
            Log.e(TAG, "❌ No network connection");
        }

        return isConnected;
    }

    public static String getNetworkErrorMessage(Throwable throwable) {
        if (throwable instanceof java.net.ConnectException) {
            return "Không thể kết nối đến server. Kiểm tra kết nối mạng.";
        } else if (throwable instanceof java.net.SocketTimeoutException) {
            return "Kết nối quá chậm, vui lòng thử lại";
        } else if (throwable instanceof java.net.UnknownHostException) {
            return "Không thể kết nối đến server";
        } else {
            return ERROR_UNKNOWN + ": " + throwable.getMessage();
        }
    }

    public static void testBackendConnectivity(Context context) {
        Log.d(TAG, "=== TESTING BACKEND CONNECTIVITY ===");

        boolean hasNetwork = checkNetworkAndLog(context);
        if (!hasNetwork) {
            Log.e(TAG, "❌ No network connection available");
            return;
        }

        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(BASE_URL + "api/auth/health");
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "✅ Backend connectivity test: " + responseCode);

                if (responseCode == 200) {
                    Log.d(TAG, "✅ Backend is reachable and healthy");
                } else {
                    Log.e(TAG, "❌ Backend returned error code: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Backend connectivity test failed", e);
            }
        }).start();
    }
}