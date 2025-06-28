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

    // Service instances
    private static AuthService authService;
    private static UserService userService;
    private static ProductService productService;
    private static ApiService apiService; // ✅ FIX: Add ApiService

    public static void init(Context context) {
        if (retrofit == null) {
            appContext = context.getApplicationContext();

            // Test network connectivity
            Constants.checkNetworkAndLog(context);
            Constants.testBackendConnectivity(context);

            retrofit = createRetrofit(appContext);

            // Initialize services
            authService = retrofit.create(AuthService.class);
            userService = retrofit.create(UserService.class);
            productService = retrofit.create(ProductService.class);
            apiService = retrofit.create(ApiService.class); // ✅ FIX: Initialize ApiService

            Log.d(TAG, "✅ ApiClient initialized with base URL: " + Constants.BASE_URL);
        }
    }

    private static Retrofit createRetrofit(Context context) {
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

                    return chain.proceed(newRequest);
                }

                // Add User-ID header for protected endpoints
                SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
                Long userId = prefsManager.getUserId();

                Request.Builder requestBuilder = originalRequest.newBuilder()
                        .addHeader("Content-Type", "application/json");

                if (userId != null && userId > 0) {
                    requestBuilder.addHeader("User-ID", userId.toString());
                    Log.d(TAG, "🔑 Added User-ID header: " + userId);
                }

                return chain.proceed(requestBuilder.build());
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
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    // Service getters
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

    // ✅ FIX: Add getApiService method
    public static ApiService getApiService() {
        if (apiService == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init() first.");
        }
        return apiService;
    }

    // Utility methods
    public static boolean isInitialized() {
        return retrofit != null;
    }

    public static void resetClient() {
        retrofit = null;
        authService = null;
        userService = null;
        productService = null;
        apiService = null; // ✅ FIX: Reset ApiService
        Log.d(TAG, "ApiClient reset");
    }
}