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
    public static final String PREF_USER_PROFILE_PICTURE = "user_profile_picture";
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
    public static final String ERROR_UNKNOWN = "Đã có lỗi xảy ra";

    // ===== HELPER METHODS - ✅ ADD: getImageUrl method =====

    /**
     * ✅ FIX: getImageUrl method (used in EditProfileActivity)
     */
    public static String getImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }

        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath; // Already full URL
        }

        // Convert relative path to full URL
        if (relativePath.startsWith("/")) {
            return BASE_URL.substring(0, BASE_URL.length() - 1) + relativePath;
        } else {
            return BASE_URL + relativePath;
        }
    }

    // ===== NETWORK UTILITY METHODS =====

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        return false;
    }

    public static void checkNetworkAndLog(Context context) {
        boolean networkAvailable = isNetworkAvailable(context);
        Log.d(TAG, networkAvailable ? "✅ Network available" : "❌ No network connection");
    }

    public static void testBackendConnectivity(Context context) {
        Log.d(TAG, "🔗 Backend URL: " + BASE_URL);
        Log.d(TAG, "📡 WebSocket URL: " + WS_BASE_URL);
    }
}