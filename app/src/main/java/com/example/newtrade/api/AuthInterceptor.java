// app/src/main/java/com/example/newtrade/api/AuthInterceptor.java
package com.example.newtrade.api;

import android.content.Context;
import android.util.Log;

import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";

    private SharedPrefsManager prefsManager;

    public AuthInterceptor(Context context) {
        this.prefsManager = SharedPrefsManager.getInstance(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Check if this is an authentication endpoint
        String url = originalRequest.url().toString();
        boolean isAuthEndpoint = url.contains("/api/auth/login") ||
                url.contains("/api/auth/register") ||
                url.contains("/api/auth/google-signin") ||
                url.contains("/api/auth/health") ||
                url.contains("/api/users/password-reset");

        // If it's an auth endpoint, don't add User-ID header
        if (isAuthEndpoint) {
            Log.d(TAG, "⚡ Auth endpoint detected, skipping User-ID header: " + url);
            return chain.proceed(originalRequest);
        }

        // For other endpoints, add User-ID header if user is logged in
        if (prefsManager.isLoggedIn()) {
            Long userId = prefsManager.getUserId();
            if (userId != null) {
                Request modifiedRequest = originalRequest.newBuilder()
                        .addHeader(Constants.HEADER_USER_ID, String.valueOf(userId))
                        .build();

                Log.d(TAG, "⚡ Added User-ID header: " + userId + " for URL: " + url);
                return chain.proceed(modifiedRequest);
            }
        }

        // If no user ID available, proceed with original request
        Log.d(TAG, "⚡ No User-ID available, proceeding with original request: " + url);
        return chain.proceed(originalRequest);
    }
}