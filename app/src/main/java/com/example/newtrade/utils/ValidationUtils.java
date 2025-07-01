// app/src/main/java/com/example/newtrade/utils/ValidationUtils.java
package com.example.newtrade.utils;

import android.text.TextUtils;
import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;

    // Password validation pattern (at least 6 chars)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^.{6,}$"
    );

    // Strong password pattern (at least 8 chars, contains letters and numbers)
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$"
    );

    // Phone number pattern (Vietnamese format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+84|0)[3|5|7|8|9][0-9]{8}$"
    );

    // Product title pattern (3-100 characters)
    private static final Pattern PRODUCT_TITLE_PATTERN = Pattern.compile(
            "^.{3,100}$"
    );

    // Price pattern (numbers with optional decimal)
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "^[0-9]+(\\.[0-9]{1,2})?$"
    );

    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate password (minimum 6 characters)
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate strong password (8+ chars with letters and numbers)
     */
    public static boolean isValidStrongPassword(String password) {
        return !TextUtils.isEmpty(password) && STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate phone number (Vietnamese format)
     */
    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate product title
     */
    public static boolean isValidProductTitle(String title) {
        return !TextUtils.isEmpty(title) && PRODUCT_TITLE_PATTERN.matcher(title).matches();
    }

    /**
     * Validate price format
     */
    public static boolean isValidPrice(String price) {
        return !TextUtils.isEmpty(price) && PRICE_PATTERN.matcher(price).matches();
    }

    /**
     * Validate display name (2-50 characters)
     */
    public static boolean isValidDisplayName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2 && name.trim().length() <= 50;
    }

    /**
     * Validate product description (max 1000 characters)
     */
    public static boolean isValidProductDescription(String description) {
        return !TextUtils.isEmpty(description) && description.trim().length() <= 1000;
    }

    /**
     * Check if string is not empty
     */
    public static boolean isNotEmpty(String text) {
        return !TextUtils.isEmpty(text) && !text.trim().isEmpty();
    }

    /**
     * Get email validation error message
     */
    public static String getEmailError(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email is required";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Please enter a valid email address";
        }
        return null;
    }

    /**
     * Get password validation error message
     */
    public static String getPasswordError(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters";
        }
        if (password.length() > Constants.MAX_PASSWORD_LENGTH) {
            return "Password must be less than " + Constants.MAX_PASSWORD_LENGTH + " characters";
        }
        return null;
    }

    /**
     * Get product title validation error message
     */
    public static String getProductTitleError(String title) {
        if (TextUtils.isEmpty(title)) {
            return "Product title is required";
        }
        if (title.trim().length() < Constants.MIN_PRODUCT_TITLE_LENGTH) {
            return "Title must be at least " + Constants.MIN_PRODUCT_TITLE_LENGTH + " characters";
        }
        if (title.trim().length() > Constants.MAX_PRODUCT_TITLE_LENGTH) {
            return "Title must be less than " + Constants.MAX_PRODUCT_TITLE_LENGTH + " characters";
        }
        return null;
    }

    /**
     * Get display name validation error message
     */
    public static String getDisplayNameError(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Display name is required";
        }
        if (name.trim().length() < 2) {
            return "Display name must be at least 2 characters";
        }
        if (name.trim().length() > 50) {
            return "Display name must be less than 50 characters";
        }
        return null;
    }

    /**
     * Sanitize input text
     */
    public static String sanitizeInput(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        return input.trim();
    }

    /**
     * Check if passwords match
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        return !TextUtils.isEmpty(password) && password.equals(confirmPassword);
    }

    /**
     * Get password match error message
     */
    public static String getPasswordMatchError(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            return "Please confirm your password";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        return null;
    }
}