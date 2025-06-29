package com.example.newtrade.utils;

public class Constants {
    // API Configuration
    public static final String BASE_URL = "http://192.168.1.100:8080/api/v1/";

    // Google Services
    public static final String GOOGLE_CLIENT_ID = "your-google-client-id.apps.googleusercontent.com";

    // OTP Configuration
    public static final int OTP_RESEND_TIME_SECONDS = 60;

    // Network Configuration
    public static final int CONNECTION_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;

    // Image Configuration
    public static final int MAX_IMAGE_SIZE_MB = 5;
    public static final int COMPRESSION_QUALITY = 80;

    // Location Configuration
    public static final int DEFAULT_LOCATION_RADIUS_KM = 50;
    public static final long LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;

    // Helper methods
    public static String getFullImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }

        if (relativePath.startsWith("http")) {
            return relativePath;
        }

        if (BASE_URL.endsWith("/")) {
            return BASE_URL.substring(0, BASE_URL.length() - 1) + relativePath;
        } else {
            return BASE_URL + relativePath;
        }
    }
}
