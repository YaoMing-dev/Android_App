// app/src/main/java/com/example/newtrade/utils/ValidationUtils.java
package com.example.newtrade.utils;

import android.text.TextUtils;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Phone validation pattern (Vietnamese)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+84|84|0)(3|5|7|8|9)[0-9]{8}$"
    );

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number
     */
    public static boolean isValidPhoneNumber(String phone) {
        return !TextUtils.isEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate product title
     */
    public static boolean isValidProductTitle(String title) {
        return !TextUtils.isEmpty(title) &&
                title.trim().length() >= Constants.MIN_PRODUCT_TITLE_LENGTH &&
                title.trim().length() <= Constants.MAX_PRODUCT_TITLE_LENGTH;
    }

    /**
     * Validate price
     */
    public static boolean isValidPrice(String priceStr) {
        if (TextUtils.isEmpty(priceStr)) return false;

        try {
            double price = Double.parseDouble(priceStr);
            return price > 0 && price <= 999999999;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Format price for display
     */
    public static String formatPrice(double price) {
        return String.format("%,.0f VNĐ", price);
    }

    /**
     * Validate display name
     */
    public static boolean isValidDisplayName(String name) {
        return !TextUtils.isEmpty(name) &&
                name.trim().length() >= Constants.MIN_DISPLAY_NAME_LENGTH &&
                name.trim().length() <= Constants.MAX_DISPLAY_NAME_LENGTH;
    }
}