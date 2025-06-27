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
    public static final String WS_BASE_URL = "ws://10.0.2.2:8080";

    // ===== SHARED PREFERENCES =====
    public static final String PREFS_NAME = "TradeUpPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_PROFILE_PICTURE = "user_profile_picture"; // ✅ ADD THIS
    public static final String PREF_FCM_TOKEN = "fcm_token";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_IS_EMAIL_VERIFIED = "is_email_verified";

    // ===== GOOGLE OAUTH =====
    public static final String GOOGLE_CLIENT_ID = "638175281882-lqsdj0iur1i079l0vlqni71gelshrdgj.apps.googleusercontent.com";

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
            "AVAILABLE", "SOLD", "PENDING", "PAUSED", "EXPIRED"
    };

    public static final String[] PRODUCT_STATUS_DISPLAY = {
            "Có sẵn", "Đã bán", "Đang chờ", "Tạm dừng", "Hết hạn"
    };

    /**
     * ✅ FIXED: Get full image URL from image path
     */
    public static String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "";
        }

        // If already full URL, return as is
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }

        // Clean base URL (remove trailing slash if exists)
        String baseUrl = BASE_URL.endsWith("/") ? BASE_URL.substring(0, BASE_URL.length()-1) : BASE_URL;

        // Handle different path formats from backend
        if (imagePath.startsWith("/")) {
            // Path like "/uploads/avatars/image.jpg"
            return baseUrl + imagePath;
        } else if (imagePath.startsWith("uploads/")) {
            // Path like "uploads/avatars/image.jpg"
            return baseUrl + "/" + imagePath;
        } else if (imagePath.contains("avatars/") || imagePath.contains("products/")) {
            // Path like "avatars/image.jpg" or "products/image.jpg"
            return baseUrl + "/uploads/" + imagePath;
        } else {
            // Default case - assume it's just filename
            return baseUrl + "/uploads/" + imagePath;
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
     * Check network connectivity
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Test backend connectivity
     */
    public static void testBackendConnectivity() {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(BASE_URL + "api/auth/health");
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    Log.d(TAG, "✅ Backend connectivity test successful");
                } else {
                    Log.w(TAG, "⚠️ Backend responded with code: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Backend connectivity test failed: " + e.getMessage());
            }
        }).start();
    }
}