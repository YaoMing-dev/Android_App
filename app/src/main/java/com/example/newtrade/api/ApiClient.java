// app/src/main/java/com/example/newtrade/api/ApiClient.java
package com.example.newtrade.api;

import android.content.Context;
import android.util.Log;

import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;

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

    private static Retrofit retrofit;
    private static Context context;
    private static SharedPrefsManager prefsManager;

    // API Service instances
    private static AuthApiService authApiService;
    private static ProductApiService productApiService;
    private static UserApiService userApiService;
    private static ChatApiService chatApiService;
    private static OfferApiService offerApiService;
    private static FileUploadApiService fileUploadApiService;

    /**
     * Initialize ApiClient with context
     */
    public static void init(Context ctx) {
        context = ctx.getApplicationContext();
        prefsManager = new SharedPrefsManager(context);

        // Create OkHttpClient with interceptors
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.UPLOAD_TIMEOUT, TimeUnit.SECONDS);

        // Add logging interceptor for debug
        if (Constants.DEBUG_MODE) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(loggingInterceptor);
        }

        // Add User-ID header interceptor
        httpClient.addInterceptor(new UserIdInterceptor());

        // Add retry interceptor
        httpClient.addInterceptor(new RetryInterceptor());

        // Build Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Log.d(TAG, "✅ ApiClient initialized with base URL: " + Constants.API_BASE_URL);
    }

    /**
     * Get Retrofit instance
     */
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            throw new RuntimeException("ApiClient not initialized. Call ApiClient.init() first.");
        }
        return retrofit;
    }




    // =============================================
    // API SERVICE GETTERS
    // =============================================

    public static AuthApiService getAuthService() {
        if (authApiService == null) {
            authApiService = getRetrofit().create(AuthApiService.class);
        }
        return authApiService;
    }

    public static ProductApiService getProductService() {
        if (productApiService == null) {
            productApiService = getRetrofit().create(ProductApiService.class);
        }
        return productApiService;
    }

    public static UserApiService getUserService() {
        if (userApiService == null) {
            userApiService = getRetrofit().create(UserApiService.class);
        }
        return userApiService;
    }

    public static ChatApiService getChatService() {
        if (chatApiService == null) {
            chatApiService = getRetrofit().create(ChatApiService.class);
        }
        return chatApiService;
    }

    public static OfferApiService getOfferService() {
        if (offerApiService == null) {
            offerApiService = getRetrofit().create(OfferApiService.class);
        }
        return offerApiService;
    }

    public static FileUploadApiService getFileUploadService() {
        if (fileUploadApiService == null) {
            fileUploadApiService = getRetrofit().create(FileUploadApiService.class);
        }
        return fileUploadApiService;
    }

    // =============================================
    // INTERCEPTORS
    // =============================================

    /**
     * Interceptor to add User-ID header to requests
     */
    private static class UserIdInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws java.io.IOException {
            Request originalRequest = chain.request();

            // Skip adding User-ID for auth endpoints
            String url = originalRequest.url().toString();
            if (url.contains("/auth/login") ||
                    url.contains("/auth/register") ||
                    url.contains("/auth/google-signin") ||
                    url.contains("/auth/send-otp") ||
                    url.contains("/auth/verify-otp")) {
                return chain.proceed(originalRequest);
            }

            // Add User-ID header if user is logged in
            if (prefsManager != null && prefsManager.isLoggedIn()) {
                Long userId = prefsManager.getUserId();
                if (userId != null) {
                    Request newRequest = originalRequest.newBuilder()
                            .header(Constants.HEADER_USER_ID, userId.toString())
                            .build();
                    return chain.proceed(newRequest);
                }
            }

            return chain.proceed(originalRequest);
        }
    }

    /**
     * Interceptor to retry failed requests
     */
    private static class RetryInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws java.io.IOException {
            Request request = chain.request();
            Response response = null;
            java.io.IOException exception = null;

            for (int i = 0; i < Constants.RETRY_COUNT; i++) {
                try {
                    response = chain.proceed(request);
                    if (response.isSuccessful()) {
                        return response;
                    }
                } catch (java.io.IOException e) {
                    exception = e;
                    Log.w(TAG, "Request failed, attempt " + (i + 1) + "/" + Constants.RETRY_COUNT, e);

                    if (i == Constants.RETRY_COUNT - 1) {
                        throw exception;
                    }

                    // Wait before retry
                    try {
                        Thread.sleep(1000 * (i + 1)); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw exception;
                    }
                }
            }

            if (exception != null) {
                throw exception;
            }

            return response;
        }
    }

    /**
     * Check if network is available
     */
    public static boolean isNetworkAvailable() {
        if (context == null) return false;

        android.net.ConnectivityManager connectivityManager =
                (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        return false;
    }

    /**
     * Get current user ID from preferences
     */
    public static Long getCurrentUserId() {
        return prefsManager != null ? prefsManager.getUserId() : null;
    }

    /**
     * Clear all API service instances (for logout)
     */


    private static CategoryApiService categoryApiService;

    public static CategoryApiService getCategoryService() {
        if (categoryApiService == null) {
            categoryApiService = getRetrofit().create(CategoryApiService.class);
        }
        return categoryApiService;
    }

    // Cập nhật clearInstances method:
    public static void clearInstances() {
        authApiService = null;
        productApiService = null;
        userApiService = null;
        chatApiService = null;
        offerApiService = null;
        fileUploadApiService = null;
        categoryApiService = null; // Add this line
        Log.d(TAG, "✅ API service instances cleared");
    }

}