// app/src/main/java/com/example/newtrade/utils/NetworkUtils.java
package com.example.newtrade.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network availability", e);
        }

        return false;
    }

    public static String getNetworkErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error occurred";
        }

        String message = throwable.getMessage();

        if (throwable instanceof UnknownHostException) {
            return "No internet connection";
        } else if (throwable instanceof SocketTimeoutException) {
            return "Connection timeout. Please try again";
        } else if (throwable instanceof IOException) {
            return "Network error. Please check your connection";
        } else if (message != null) {
            if (message.contains("timeout")) {
                return "Request timeout. Please try again";
            } else if (message.contains("connection")) {
                return "Connection error. Please try again";
            } else if (message.contains("401")) {
                return "Authentication failed. Please login again";
            } else if (message.contains("403")) {
                return "Access denied";
            } else if (message.contains("404")) {
                return "Resource not found";
            } else if (message.contains("500")) {
                return "Server error. Please try again later";
            }
        }

        return "Network error. Please try again";
    }
}