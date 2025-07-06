// app/src/main/java/com/example/newtrade/api/ApiClient.java
package com.example.newtrade.api;

import android.content.Context;
import android.util.Log;

import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient = null;
    private static Context appContext = null;
    private static String currentBaseUrl = null;

    // Service instances
    private static AuthService authService;
    private static UserService userService;
    private static ProductService productService;
    private static ApiService apiService;

    // ===== DYNAMIC INITIALIZATION =====

    public static void init(Context context) {
        init(context, null);
    }

    public static void init(Context context, InitCallback callback) {
        if (retrofit == null || shouldReinit(context)) {
            appContext = context.getApplicationContext();

            // Log device info for debugging
            Constants.logDeviceInfo();

            // Check network connectivity
            if (!Constants.checkNetworkAndLog(context)) {
                Log.w(TAG, "⚠️ No network connectivity detected");
                if (callback != null) callback.onFailure("No network connection");
                return;
            }

            // Get dynamic base URL
            String baseUrl = Constants.getBaseURL(context);
            Log.d(TAG, "🌐 Initializing ApiClient with URL: " + baseUrl);

            // Test connection and initialize
            testAndInitialize(context, baseUrl, callback);

        } else {
            Log.d(TAG, "✅ ApiClient already initialized");
            if (callback != null) callback.onSuccess(currentBaseUrl);
        }
    }

    private static boolean shouldReinit(Context context) {
        String newBaseUrl = Constants.getBaseURL(context);
        boolean urlChanged = !newBaseUrl.equals(currentBaseUrl);
        if (urlChanged) {
            Log.d(TAG, "🔄 Base URL changed from " + currentBaseUrl + " to " + newBaseUrl);
        }
        return urlChanged;
    }

    private static void testAndInitialize(Context context, String baseUrl, InitCallback callback) {
        // Test connection first
        Constants.testConnection(context, new Constants.ConnectionTestCallback() {
            @Override
            public void onSuccess(String workingUrl) {
                // Connection successful, proceed with initialization
                finalizeInit(context, baseUrl, callback);
            }

            @Override
            public void onFailure(String error) {
                Log.w(TAG, "⚠️ Primary URL failed, scanning for alternatives...");

                // Auto-scan for working IP
                Constants.findWorkingIP(context, new Constants.IPScanCallback() {
                    @Override
                    public void onFound(String workingIP) {
                        // Save the working IP for future use
                        Constants.setCustomHostIP(context, workingIP);
                        String newBaseUrl = "http://" + workingIP + ":8080/";
                        Log.d(TAG, "🎯 Found working IP, using: " + newBaseUrl);
                        finalizeInit(context, newBaseUrl, callback);
                    }

                    @Override
                    public void onNotFound() {
                        Log.e(TAG, "😞 No working backend found");
                        if (callback != null) {
                            callback.onFailure("No accessible backend server found. Please check if backend is running.");
                        }
                    }
                });
            }
        });
    }

    private static void finalizeInit(Context context, String baseUrl, InitCallback callback) {
        try {
            currentBaseUrl = baseUrl;
            retrofit = createRetrofit(context, baseUrl);

            // Initialize services
            authService = retrofit.create(AuthService.class);
            userService = retrofit.create(UserService.class);
            productService = retrofit.create(ProductService.class);
            apiService = retrofit.create(ApiService.class);

            Log.d(TAG, "✅ ApiClient initialized successfully with base URL: " + baseUrl);

            if (callback != null) {
                callback.onSuccess(baseUrl);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize ApiClient", e);
            if (callback != null) {
                callback.onFailure("Initialization failed: " + e.getMessage());
            }
        }
    }

    private static Retrofit createRetrofit(Context context, String baseUrl) {
        // Gson configuration
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setLenient()
                .create();

        // HTTP logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                Log.d("HTTP", message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Auth interceptor
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                String url = originalRequest.url().toString();

                Log.d(TAG, "🔍 API Request: " + originalRequest.method() + " " + url);

                // Skip User-ID header for auth endpoints (except session validation and logout)
                if (url.contains("/api/auth/") &&
                        !url.contains("/validate-session") &&
                        !url.contains("/logout")) {

                    Request newRequest = originalRequest.newBuilder()
                            .addHeader("Content-Type", "application/json")
                            .build();

                    Log.d(TAG, "🔓 Auth endpoint - no User-ID header");
                    return chain.proceed(newRequest);

                } else {
                    // Add User-ID header for authenticated endpoints
                    SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
                    Long userId = prefsManager.getUserId();

                    if (userId != null && userId > 0) {
                        Request newRequest = originalRequest.newBuilder()
                                .addHeader("User-ID", userId.toString())
                                .addHeader("Content-Type", "application/json")
                                .build();

                        Log.d(TAG, "🔐 Added User-ID header: " + userId);
                        return chain.proceed(newRequest);
                    } else {
                        Log.w(TAG, "⚠️ No valid User-ID for authenticated endpoint: " + url);
                        return chain.proceed(originalRequest);
                    }
                }
            }
        };

        // Network error interceptor
        Interceptor networkErrorInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                try {
                    Response response = chain.proceed(request);

                    // Log response details
                    Log.d(TAG, "🔍 Response: " + response.code() + " " + response.message());

                    if (!response.isSuccessful()) {
                        Log.e(TAG, "❌ HTTP Error: " + response.code() + " for " + request.url());
                    }

                    return response;

                } catch (IOException e) {
                    Log.e(TAG, "❌ Network error for " + request.url() + ": " + e.getMessage());
                    throw e;
                }
            }
        };

        // Build OkHttpClient
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(networkErrorInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        // Build Retrofit
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    // ===== SERVICE GETTERS =====

    public static AuthService getAuthService() {
        if (authService == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init() first.");
        }
        return authService;
    }

    public static UserService getUserService() {
        if (userService == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init() first.");
        }
        return userService;
    }

    public static ProductService getProductService() {
        if (productService == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init() first.");
        }
        return productService;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init() first.");
        }
        return apiService;
    }

    // ===== UTILITY METHODS =====

    public static boolean isInitialized() {
        return retrofit != null;
    }

    public static String getCurrentBaseUrl() {
        return currentBaseUrl;
    }

    public static void resetClient() {
        retrofit = null;
        authService = null;
        userService = null;
        productService = null;
        apiService = null;
        currentBaseUrl = null;
        Log.d(TAG, "🧹 ApiClient reset");
    }

    public static void reinitialize(Context context, InitCallback callback) {
        resetClient();
        init(context, callback);
    }

    // ===== IP MANAGEMENT UTILITIES =====

    public static void setCustomIP(Context context, String ip, InitCallback callback) {
        Log.d(TAG, "🔧 Setting custom IP: " + ip);
        Constants.setCustomHostIP(context, ip);
        reinitialize(context, callback);
    }

    public static void autoDetectIP(Context context, InitCallback callback) {
        Log.d(TAG, "🔍 Auto-detecting best IP...");
        Constants.clearCustomHostIP(context);
        reinitialize(context, callback);
    }

    // ===== CALLBACK INTERFACE =====

    public interface InitCallback {
        void onSuccess(String baseUrl);
        void onFailure(String error);
    }
}