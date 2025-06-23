package com.example.newtrade.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Constants {

    private static final String TAG = "Constants";

    // ===== API CONFIGURATION =====
    public static final String BASE_URL = "http://10.0.2.2:8080/"; // Android Emulator
    // public static final String BASE_URL = "http://192.168.1.100:8080/"; // Real device - change to your IP

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
    public static final int SEARCH_DELAY_MS = 300;

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
            "AVAILABLE", "SOLD", "RESERVED", "DELETED", "ARCHIVED"
    };

    public static final String[] PRODUCT_STATUS_DISPLAY = {
            "Có sẵn", "Đã bán", "Đã đặt", "Đã xóa", "Lưu trữ"
    };

    // ===== API RESPONSE CODES =====
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_INTERNAL_ERROR = 500;

    // ===== HELPER METHODS =====

    /**
     * Check network connectivity and log details
     */
    public static boolean checkNetworkAndLog(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        Log.d(TAG, "=== NETWORK DEBUG ===");
        Log.d(TAG, "Network connected: " + isConnected);
        Log.d(TAG, "Base URL: " + BASE_URL);

        if (activeNetwork != null) {
            Log.d(TAG, "Network type: " + activeNetwork.getTypeName());
            Log.d(TAG, "Network state: " + activeNetwork.getState());
            Log.d(TAG, "Network info: " + activeNetwork.toString());
        } else {
            Log.e(TAG, "No active network found!");
        }

        return isConnected;
    }

    /**
     * Get full image URL from image path
     */
    public static String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "";
        }

        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }

        if (imagePath.startsWith("/")) {
            return BASE_URL + imagePath.substring(1);
        } else {
            return BASE_URL + imagePath;
        }
    }

    /**
     * Get display text for product condition
     */
    public static String getProductConditionDisplay(String condition) {
        if (condition == null) {
            return "Không xác định";
        }

        for (int i = 0; i < PRODUCT_CONDITIONS.length; i++) {
            if (PRODUCT_CONDITIONS[i].equals(condition)) {
                return PRODUCT_CONDITION_DISPLAY[i];
            }
        }
        return condition;
    }

    /**
     * Get display text for product status
     */
    public static String getProductStatusDisplay(String status) {
        if (status == null) {
            return "Không xác định";
        }

        for (int i = 0; i < PRODUCT_STATUS.length; i++) {
            if (PRODUCT_STATUS[i].equals(status)) {
                return PRODUCT_STATUS_DISPLAY[i];
            }
        }
        return status;
    }

    /**
     * Format price to Vietnamese currency format
     */
    public static String formatPrice(double price) {
        if (price <= 0) {
            return "Miễn phí";
        }

        if (price >= 1000000000) {
            return String.format("%.1f tỷ", price / 1000000000);
        } else if (price >= 1000000) {
            return String.format("%.1f triệu", price / 1000000);
        } else if (price >= 1000) {
            return String.format("%.0f nghìn", price / 1000);
        } else {
            return String.format("%.0f ₫", price);
        }
    }

    /**
     * ✅ Get time ago string - FIX FOR PRODUCT.JAVA
     * @param timestamp Timestamp in milliseconds
     * @return Time ago string in Vietnamese
     */
    public static String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // < 1 minute
            return "Vừa xong";
        } else if (diff < 3600000) { // < 1 hour
            int minutes = (int) (diff / 60000);
            return minutes + " phút trước";
        } else if (diff < 86400000) { // < 1 day
            int hours = (int) (diff / 3600000);
            return hours + " giờ trước";
        } else if (diff < 2592000000L) { // < 30 days
            int days = (int) (diff / 86400000);
            return days + " ngày trước";
        } else if (diff < 31536000000L) { // < 1 year
            int months = (int) (diff / 2592000000L);
            return months + " tháng trước";
        } else {
            int years = (int) (diff / 31536000000L);
            return years + " năm trước";
        }
    }

    /**
     * Get user-friendly error message
     */
    public static String getUserFriendlyError(Throwable throwable) {
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

    /**
     * Test network connectivity to backend
     */
    public static void testBackendConnectivity(Context context) {
        Log.d(TAG, "=== TESTING BACKEND CONNECTIVITY ===");

        // Check network
        boolean hasNetwork = checkNetworkAndLog(context);
        if (!hasNetwork) {
            Log.e(TAG, "❌ No network connection available");
            return;
        }

        // Test in background thread
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
                Log.e(TAG, "❌ Backend connectivity test failed: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Validate phone number (Vietnamese format)
     */
    public static boolean isValidVietnamesePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return cleanPhone.matches("^(0[3|5|7|8|9])[0-9]{8}$") ||
                cleanPhone.matches("^(84[3|5|7|8|9])[0-9]{8}$") ||
                cleanPhone.matches("^(0[2|4])[0-9]{8}$");
    }

    /**
     * Clean phone number to standard format
     */
    public static String cleanPhoneNumber(String phone) {
        if (phone == null) return "";

        String cleaned = phone.replaceAll("[^0-9]", "");

        if (cleaned.startsWith("84")) {
            cleaned = "0" + cleaned.substring(2);
        }

        return cleaned;
    }

    /**
     * Check if URL is image
     */
    public static boolean isImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".jpg") ||
                lowerUrl.endsWith(".jpeg") ||
                lowerUrl.endsWith(".png") ||
                lowerUrl.endsWith(".gif") ||
                lowerUrl.endsWith(".webp");
    }

    /**
     * Get file extension from URL or filename
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }

        return "";
    }

    /**
     * Check if network error should show retry option
     */
    public static boolean shouldShowRetry(Throwable throwable) {
        return throwable instanceof java.net.ConnectException ||
                throwable instanceof java.net.SocketTimeoutException ||
                throwable instanceof java.net.UnknownHostException;
    }
}