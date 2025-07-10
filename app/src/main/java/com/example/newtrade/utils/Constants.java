// app/src/main/java/com/example/newtrade/utils/Constants.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class Constants {

    private static final String TAG = "Constants";

    // ===== IP CONFIGURATION - NEW =====
    private static final String PREFS_IP_CONFIG = "ip_config_prefs";
    private static final String PREF_CUSTOM_HOST_IP = "custom_host_ip";
    private static final String PREF_USE_CUSTOM_IP = "use_custom_ip";

    // Default IPs for different scenarios
    private static final String EMULATOR_HOST_IP = "10.0.2.2";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String DEFAULT_PORT = "8080";

    // Backup IPs - common router ranges
    private static final String[] COMMON_HOST_IPS = {
            "192.168.1.1", "192.168.1.100", "192.168.1.101", "192.168.1.102",
            "192.168.0.1", "192.168.0.100", "192.168.0.101", "192.168.0.102",
            "10.0.0.1", "10.0.0.100", "10.0.0.101", "10.0.0.102"
    };

    // ===== DYNAMIC API CONFIGURATION =====
    public static String getBaseURL(Context context) {
        String hostIP = getOptimalHostIP(context);
        String url = "http://" + hostIP + ":" + DEFAULT_PORT + "/";
        Log.d(TAG, "🌐 Generated BASE_URL: " + url);
        return url;
    }

    public static String getWebSocketURL(Context context) {
        String hostIP = getOptimalHostIP(context);
        String url = "ws://" + hostIP + ":" + DEFAULT_PORT;
        Log.d(TAG, "🔌 Generated WS_URL: " + url);
        return url;
    }

    // ===== LEGACY CONSTANTS (KEPT FOR COMPATIBILITY) =====
    public static final String BASE_URL = "http://10.0.2.2:8080/"; // ← GIỮ NGUYÊN CHO COMPATIBILITY
    public static final String WS_BASE_URL = "ws://10.0.2.2:8080"; // ← GIỮ NGUYÊN CHO COMPATIBILITY

    // ===== GOOGLE OAUTH - CỦA BẠN =====
    public static final String GOOGLE_CLIENT_ID =
            "638175281882-lqsdj0iur1i079l0vlqni71gelshrdgj.apps.googleusercontent.com"; // ← SỬA THÀNH CỦA BẠN

    // ===== API ENDPOINTS - UPDATED WITH PAYMENTS =====
    public static final String API_AUTH = "/api/auth";
    public static final String API_USERS = "/api/users";
    public static final String API_PRODUCTS = "/api/products";
    public static final String API_CATEGORIES = "/api/categories";
    public static final String API_FILES = "/api/files";
    public static final String API_MESSAGES = "/api/messages";
    public static final String API_CONVERSATIONS = "/api/conversations";
    public static final String API_NOTIFICATIONS = "/api/notifications";
    public static final String API_OFFERS = "/api/offers";
    public static final String API_TRANSACTIONS = "/api/transactions";
    public static final String API_REVIEWS = "/api/reviews";
    public static final String API_SAVED_ITEMS = "/api/saved-items";
    public static final String API_ANALYTICS = "/api/analytics";

    // ===== 🆕 PAYMENT API ENDPOINTS =====
    public static final String API_PAYMENTS = "/api/payments";
    public static final String PAYMENT_CONFIG = "/config";
    public static final String CREATE_PAYMENT_INTENT = "/create-payment-intent";
    public static final String CONFIRM_PAYMENT = "/confirm-payment";
    public static final String PAYMENT_STATUS = "/status";
    public static final String MY_PAYMENTS = "/my-payments";
    public static final String PROCESS_REFUND = "/refund";

    // ===== WEBSOCKET TOPICS - UNCHANGED =====
    public static final String TOPIC_NOTIFICATIONS = "/user/queue/notifications";
    public static final String TOPIC_CONVERSATION = "/topic/conversation/";
    public static final String TOPIC_MESSAGES = "/topic/messages";

    // ===== WEBSOCKET SEND DESTINATIONS - UNCHANGED =====
    public static final String SEND_MESSAGE = "/app/send/message";
    public static final String SEND_NOTIFICATION = "/app/send/notification";
    public static final String JOIN_CONVERSATION = "/app/join/conversation/";
    public static final String LEAVE_CONVERSATION = "/app/leave/conversation/";

    // ===== SHARED PREFERENCES - UNCHANGED =====
    public static final String PREFS_NAME = "TradeUpPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final int REQUEST_CODE_WRITE_REVIEW = 1003;
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_PROFILE_PICTURE = "user_profile_picture";
    public static final String PREF_FCM_TOKEN = "fcm_token";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_IS_EMAIL_VERIFIED = "is_email_verified";

    // ===== REQUEST CODES - UPDATED WITH PAYMENT =====
    public static final int RC_GOOGLE_SIGN_IN = 1001;
    public static final int RC_PICK_IMAGE = 1002;
    public static final int RC_CAMERA = 1003;
    public static final int RC_LOCATION_PICKER = 1004;
    public static final int RC_PAYMENT = 1005; // 🆕 Payment request code

    // ===== HEADERS - UNCHANGED =====
    public static final String HEADER_USER_ID = "User-ID";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    // ===== VALIDATION - UNCHANGED =====
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int OTP_LENGTH = 6;
    public static final int OTP_RESEND_TIME_SECONDS = 60;

    // ===== FILE UPLOAD - UNCHANGED =====
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB for products
    public static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB for avatars
    public static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    // ===== PAGINATION - UNCHANGED =====
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";
    public static final int SEARCH_DELAY_MS = 200;
    public static final int MIN_SEARCH_LENGTH = 2;

    // ===== LOCATION - UNCHANGED =====
    public static final int DEFAULT_LOCATION_RADIUS = 10; // km
    public static final int MAX_LOCATION_RADIUS = 100; // km

    // ===== MESSAGING - UNCHANGED =====
    public static final int MESSAGE_POLLING_INTERVAL = 3000; // 3 seconds
    public static final int MAX_MESSAGE_LENGTH = 1000;

    // ===== 🆕 PAYMENT CONFIGURATION =====
    // Stripe Configuration
    public static final String STRIPE_PUBLISHABLE_KEY_TEST = "pk_test_51RcRCQRpMILatp67WGNGrZif5hI7ZrYMupx6rxrv5vpX3lrRpdtIQzck5V5yBfCHzTTwP7pIQfLb0NGvyQyiufG00o89zAvGq";

    // Payment Methods
    public static final String PAYMENT_METHOD_CARD = "CARD";
    public static final String PAYMENT_METHOD_BANK_TRANSFER = "BANK_TRANSFER";
    public static final String PAYMENT_METHOD_DIGITAL_WALLET = "DIGITAL_WALLET";
    public static final String PAYMENT_METHOD_CASH = "CASH";

    // Currency
    public static final String CURRENCY_VND = "VND";
    public static final String CURRENCY_USD = "USD";

    // Payment Status
    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_PROCESSING = "PROCESSING";
    public static final String PAYMENT_STATUS_SUCCEEDED = "SUCCEEDED";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    public static final String PAYMENT_STATUS_CANCELED = "CANCELED";
    public static final String PAYMENT_STATUS_REFUNDED = "REFUNDED";
    public static final String PAYMENT_STATUS_PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED";

    // Payment Intent Status
    public static final String PAYMENT_INTENT_STATUS_REQUIRES_PAYMENT_METHOD = "requires_payment_method";
    public static final String PAYMENT_INTENT_STATUS_REQUIRES_CONFIRMATION = "requires_confirmation";
    public static final String PAYMENT_INTENT_STATUS_REQUIRES_ACTION = "requires_action";
    public static final String PAYMENT_INTENT_STATUS_PROCESSING = "processing";
    public static final String PAYMENT_INTENT_STATUS_SUCCEEDED = "succeeded";
    public static final String PAYMENT_INTENT_STATUS_CANCELED = "canceled";

    // Payment Validation
    public static final double MIN_PAYMENT_AMOUNT = 1.0; // $1 or ₫1
    public static final double MAX_PAYMENT_AMOUNT = 10000.0; // $10,000 or ₫10,000
    public static final int PAYMENT_TIMEOUT_MINUTES = 15; // Payment expires in 15 minutes

    // ===== ERROR MESSAGES - UPDATED WITH PAYMENT =====
    public static final String ERROR_NETWORK = "Lỗi kết nối mạng";
    public static final String ERROR_TIMEOUT = "Kết nối quá chậm";
    public static final String ERROR_UNKNOWN = "Lỗi không xác định";

    // 🆕 Payment Error Messages
    public static final String ERROR_PAYMENT_FAILED = "Thanh toán thất bại";
    public static final String ERROR_PAYMENT_CANCELED = "Thanh toán bị hủy";
    public static final String ERROR_PAYMENT_TIMEOUT = "Thanh toán hết hạn";
    public static final String ERROR_PAYMENT_INSUFFICIENT_FUNDS = "Số dư không đủ";
    public static final String ERROR_PAYMENT_CARD_DECLINED = "Thẻ bị từ chối";
    public static final String ERROR_PAYMENT_NETWORK = "Lỗi kết nối thanh toán";
    public static final String ERROR_STRIPE_NOT_INITIALIZED = "Stripe chưa được khởi tạo";

    // ===== NEW METHODS FOR IP MANAGEMENT - UNCHANGED =====

    private static String getOptimalHostIP(Context context) {
        // 1. Check if user set custom IP
        String customIP = getCustomHostIP(context);
        if (customIP != null && !customIP.isEmpty()) {
            Log.d(TAG, "✅ Using custom IP: " + customIP);
            return customIP;
        }

        // 2. Detect if emulator
        if (isEmulator()) {
            Log.d(TAG, "📱 Detected emulator, using: " + EMULATOR_HOST_IP);
            return EMULATOR_HOST_IP;
        }

        // 3. Try to get gateway IP (router)
        String gatewayIP = getGatewayIP(context);
        if (gatewayIP != null && !gatewayIP.equals("0.0.0.0")) {
            Log.d(TAG, "🏠 Using gateway IP: " + gatewayIP);
            return gatewayIP;
        }

        // 4. Try to detect local IP and guess host
        String localIP = getLocalIPAddress();
        if (localIP != null) {
            String guessedHost = guessHostFromLocalIP(localIP);
            Log.d(TAG, "🔍 Guessed host from local IP " + localIP + ": " + guessedHost);
            return guessedHost;
        }

        // 5. Fallback to localhost
        Log.w(TAG, "⚠️ Using fallback IP: " + LOCALHOST_IP);
        return LOCALHOST_IP;
    }

    private static boolean isEmulator() {
        return android.os.Build.FINGERPRINT.startsWith("generic") ||
                android.os.Build.FINGERPRINT.startsWith("unknown") ||
                android.os.Build.MODEL.contains("google_sdk") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK built for x86") ||
                android.os.Build.MANUFACTURER.contains("Genymotion") ||
                (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic")) ||
                android.os.Build.PRODUCT.contains("sdk") ||
                android.os.Build.PRODUCT.contains("vbox");
    }

    private static String getGatewayIP(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                int gatewayAddress = wifiManager.getDhcpInfo().gateway;
                if (gatewayAddress != 0) {
                    return Formatter.formatIpAddress(gatewayAddress);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting gateway IP", e);
        }
        return null;
    }

    private static String getLocalIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        Log.d(TAG, "Found local IP: " + ip);
                        return ip;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting local IP", e);
        }
        return null;
    }

    private static String guessHostFromLocalIP(String localIP) {
        if (localIP == null) return LOCALHOST_IP;

        // Extract network prefix and guess host
        String[] parts = localIP.split("\\.");
        if (parts.length == 4) {
            // Try common host IPs in same network
            String network = parts[0] + "." + parts[1] + "." + parts[2] + ".";
            String[] commonHosts = {"1", "100", "101", "102", "10", "20"};

            for (String host : commonHosts) {
                if (!host.equals(parts[3])) { // Don't return self
                    return network + host;
                }
            }
        }

        return LOCALHOST_IP;
    }

    // ===== CUSTOM IP MANAGEMENT - UNCHANGED =====

    public static void setCustomHostIP(Context context, String ip) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_IP_CONFIG, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(PREF_CUSTOM_HOST_IP, ip)
                .putBoolean(PREF_USE_CUSTOM_IP, true)
                .apply();
        Log.d(TAG, "💾 Saved custom IP: " + ip);
    }

    public static String getCustomHostIP(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_IP_CONFIG, Context.MODE_PRIVATE);
        boolean useCustom = prefs.getBoolean(PREF_USE_CUSTOM_IP, false);
        if (useCustom) {
            return prefs.getString(PREF_CUSTOM_HOST_IP, null);
        }
        return null;
    }

    public static void clearCustomHostIP(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_IP_CONFIG, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(PREF_CUSTOM_HOST_IP)
                .putBoolean(PREF_USE_CUSTOM_IP, false)
                .apply();
        Log.d(TAG, "🗑️ Cleared custom IP");
    }

    // ===== CONNECTIVITY TESTING - UNCHANGED =====

    public static void testConnection(Context context, ConnectionTestCallback callback) {
        String baseUrl = getBaseURL(context);
        String testUrl = baseUrl + "api/auth/health";

        new Thread(() -> {
            try {
                Log.d(TAG, "🧪 Testing connection to: " + testUrl);

                java.net.URL url = new java.net.URL(testUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                boolean success = responseCode == 200;

                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    if (success) {
                        Log.d(TAG, "✅ Connection test successful: " + responseCode);
                        callback.onSuccess(baseUrl);
                    } else {
                        Log.e(TAG, "❌ Connection test failed: " + responseCode);
                        callback.onFailure("HTTP Error: " + responseCode);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Connection test exception", e);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onFailure("Connection failed: " + e.getMessage()));
            }
        }).start();
    }

    public interface ConnectionTestCallback {
        void onSuccess(String workingUrl);
        void onFailure(String error);
    }

    // ===== AUTO-SCAN MULTIPLE IPS - UNCHANGED =====

    public static void findWorkingIP(Context context, IPScanCallback callback) {
        new Thread(() -> {
            String workingIP = null;

            // Test current optimal IP first
            String currentIP = getOptimalHostIP(context);
            if (testSingleIP(currentIP)) {
                workingIP = currentIP;
            } else {
                // Scan common IPs
                for (String ip : COMMON_HOST_IPS) {
                    if (testSingleIP(ip)) {
                        workingIP = ip;
                        break;
                    }
                }
            }

            final String finalIP = workingIP;
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> {
                if (finalIP != null) {
                    Log.d(TAG, "🎯 Found working IP: " + finalIP);
                    callback.onFound(finalIP);
                } else {
                    Log.e(TAG, "😞 No working IP found");
                    callback.onNotFound();
                }
            });
        }).start();
    }

    private static boolean testSingleIP(String ip) {
        try {
            String testUrl = "http://" + ip + ":" + DEFAULT_PORT + "/api/auth/health";
            java.net.URL url = new java.net.URL(testUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Testing " + ip + ": " + responseCode);
            return responseCode == 200;

        } catch (Exception e) {
            Log.d(TAG, "Testing " + ip + ": failed - " + e.getMessage());
            return false;
        }
    }

    public interface IPScanCallback {
        void onFound(String workingIP);
        void onNotFound();
    }

    // ===== 🆕 PAYMENT UTILITY METHODS =====

    /**
     * Format currency amount for display
     */
    public static String formatCurrency(double amount, String currency) {
        if (CURRENCY_VND.equalsIgnoreCase(currency)) {
            return String.format("₫%,.0f", amount);
        } else {
            return String.format("$%.2f", amount);
        }
    }

    /**
     * Validate payment amount
     */
    public static boolean isValidPaymentAmount(double amount) {
        return amount >= MIN_PAYMENT_AMOUNT && amount <= MAX_PAYMENT_AMOUNT;
    }

    /**
     * Get payment method display name
     */
    public static String getPaymentMethodDisplayName(String paymentMethod) {
        switch (paymentMethod) {
            case PAYMENT_METHOD_CARD:
                return "Credit/Debit Card";
            case PAYMENT_METHOD_BANK_TRANSFER:
                return "Bank Transfer";
            case PAYMENT_METHOD_DIGITAL_WALLET:
                return "Digital Wallet";
            case PAYMENT_METHOD_CASH:
                return "Cash";
            default:
                return paymentMethod;
        }
    }

    /**
     * Get payment status display name
     */
    public static String getPaymentStatusDisplayName(String status) {
        switch (status) {
            case PAYMENT_STATUS_PENDING:
                return "Pending";
            case PAYMENT_STATUS_PROCESSING:
                return "Processing";
            case PAYMENT_STATUS_SUCCEEDED:
                return "Completed";
            case PAYMENT_STATUS_FAILED:
                return "Failed";
            case PAYMENT_STATUS_CANCELED:
                return "Cancelled";
            case PAYMENT_STATUS_REFUNDED:
                return "Refunded";
            case PAYMENT_STATUS_PARTIALLY_REFUNDED:
                return "Partially Refunded";
            default:
                return status;
        }
    }

    // ===== EXISTING UTILITY METHODS - UNCHANGED =====

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

    public static boolean checkNetworkAndLog(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
                Log.d(TAG, "🌐 Network status: " + (isConnected ? "Connected" : "Disconnected"));
                return isConnected;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network", e);
        }
        return false;
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
                java.net.URL url = new java.net.URL(getBaseURL(context) + "api/auth/health");
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

    public static void logDeviceInfo() {
        Log.d(TAG, "=== DEVICE INFO ===");
        Log.d(TAG, "📱 Device: " + android.os.Build.DEVICE);
        Log.d(TAG, "🏭 Manufacturer: " + android.os.Build.MANUFACTURER);
        Log.d(TAG, "📟 Model: " + android.os.Build.MODEL);
        Log.d(TAG, "🆔 Product: " + android.os.Build.PRODUCT);
        Log.d(TAG, "👆 Fingerprint: " + android.os.Build.FINGERPRINT);
        Log.d(TAG, "🤖 Is Emulator: " + isEmulator());
        Log.d(TAG, "===================");
    }
}