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
    private static OkHttpClient okHttpClient = null; // ✅ ADD: Store OkHttpClient

    // Service instances
    private static AuthService authService;
    private static UserService userService;
    private static ProductService productService;

    // ✅ FIXED: Remove static context - pass context as parameter
    public static void init(Context context) {
        if (retrofit == null) {
            retrofit = createRetrofit(context);

            // Initialize services
            authService = retrofit.create(AuthService.class);
            userService = retrofit.create(UserService.class);
            productService = retrofit.create(ProductService.class);

            Log.d(TAG, "ApiClient initialized with base URL: " + Constants.BASE_URL);
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

        // Auth interceptor - adds User-ID header
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();

                // Skip adding User-ID for auth endpoints
                String url = originalRequest.url().toString();
                if (url.contains("/api/auth/")) {
                    return chain.proceed(originalRequest);
                }

                // Add User-ID header for authenticated requests
                long userId = SharedPrefsManager.getInstance(context).getUserId();
                if (userId > 0) {
                    Request newRequest = originalRequest.newBuilder()
                            .addHeader("User-ID", String.valueOf(userId))
                            .addHeader("Content-Type", "application/json")
                            .build();

                    Log.d(TAG, "Adding User-ID header: " + userId);
                    return chain.proceed(newRequest);
                }

                return chain.proceed(originalRequest);
            }
        };

        // ✅ FIXED: Store OkHttpClient reference
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    // Service getters
    public static AuthService getAuthService() {
        if (authService == null) {
            authService = retrofit.create(AuthService.class);
        }
        return authService;
    }

    public static UserService getUserService() {
        if (userService == null) {
            userService = retrofit.create(UserService.class);
        }
        return userService;
    }

    public static ProductService getProductService() {
        if (productService == null) {
            productService = retrofit.create(ProductService.class);
        }
        return productService;
    }

    // Utility methods
    public static boolean isNetworkAvailable() {
        // Simple network check - you can enhance this
        return true; // For now, assume network is always available
    }

    // ✅ FIXED: Remove problematic updateBaseUrl method for now
    // We can add this later if needed

    // Error handling utility
    public static String getErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return Constants.ERROR_UNKNOWN;
        }

        String message = throwable.getMessage();
        if (message == null || message.isEmpty()) {
            return Constants.ERROR_UNKNOWN;
        }

        // Handle common HTTP errors
        if (message.contains("timeout")) {
            return "Kết nối timeout, vui lòng thử lại";
        } else if (message.contains("Unable to resolve host")) {
            return Constants.ERROR_NETWORK;
        } else if (message.contains("401")) {
            return Constants.ERROR_UNAUTHORIZED;
        } else if (message.contains("500")) {
            return Constants.ERROR_SERVER;
        }

        return message;
    }
}