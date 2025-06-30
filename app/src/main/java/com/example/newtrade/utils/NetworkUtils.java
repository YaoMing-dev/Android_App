// app/src/main/java/com/example/newtrade/utils/NetworkUtils.java
package com.example.newtrade.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        return false;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
        }

        return false;
    }

    public static String getNetworkErrorMessage(Throwable throwable) {
        if (throwable.getMessage() != null) {
            String message = throwable.getMessage().toLowerCase();
            if (message.contains("timeout")) {
                return "Connection timeout. Please try again.";
            } else if (message.contains("connection")) {
                return "Connection failed. Please check your internet.";
            } else if (message.contains("host")) {
                return "Server unavailable. Please try again later.";
            }
        }
        return "Network error. Please check your connection.";
    }
}