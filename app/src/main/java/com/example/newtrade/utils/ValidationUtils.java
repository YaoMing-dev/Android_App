// app/src/main/java/com/example/newtrade/utils/ValidationUtils.java
package com.example.newtrade.utils;

import android.text.TextUtils;
import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {

    // =============================================
    // EMAIL VALIDATION
    // =============================================
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // =============================================
    // PASSWORD VALIDATION
    // =============================================
    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }

        // Check minimum length
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            return false;
        }

        // Check maximum length
        if (password.length() > Constants.MAX_PASSWORD_LENGTH) {
            return false;
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }

        return true;
    }

    public static String getPasswordStrength(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Very Weak";
        }

        int score = 0;

        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;

        // Character variety checks
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;

        switch (score) {
            case 0:
            case 1:
                return "Very Weak";
            case 2:
                return "Weak";
            case 3:
                return "Fair";
            case 4:
                return "Good";
            case 5:
            case 6:
                return "Strong";
            default:
                return "Very Strong";
        }
    }

    // =============================================
    // NAME VALIDATION
    // =============================================
    public static boolean isValidDisplayName(String displayName) {
        if (TextUtils.isEmpty(displayName)) {
            return false;
        }

        String trimmed = displayName.trim();

        // Check minimum length
        if (trimmed.length() < Constants.MIN_DISPLAY_NAME_LENGTH) {
            return false;
        }

        // Check maximum length
        if (trimmed.length() > Constants.MAX_DISPLAY_NAME_LENGTH) {
            return false;
        }

        // Check for valid characters (letters, numbers, spaces, hyphens, apostrophes)
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9\\s\\-']+$");
        return pattern.matcher(trimmed).matches();
    }

    // =============================================
    // PRODUCT VALIDATION
    // =============================================
    public static boolean isValidProductTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return false;
        }

        String trimmed = title.trim();
        return trimmed.length() >= Constants.MIN_PRODUCT_TITLE_LENGTH &&
                trimmed.length() <= Constants.MAX_PRODUCT_TITLE_LENGTH;
    }

    public static boolean isValidProductDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            return false;
        }

        String trimmed = description.trim();
        return trimmed.length() >= Constants.MIN_PRODUCT_DESCRIPTION_LENGTH &&
                trimmed.length() <= Constants.MAX_PRODUCT_DESCRIPTION_LENGTH;
    }

    public static boolean isValidPrice(String priceStr) {
        if (TextUtils.isEmpty(priceStr)) {
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            return price > 0 && price <= 1000000; // Maximum price $1M
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPrice(double price) {
        return price > 0 && price <= 1000000; // Maximum price $1M
    }

    // =============================================
    // PHONE NUMBER VALIDATION
    // =============================================
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }

        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("\\D", "");

        // Check if it's between 10-15 digits (international format)
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 15;
    }

    // =============================================
    // LOCATION VALIDATION
    // =============================================
    public static boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }

    public static boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
    }

    public static boolean isValidSearchRadius(double radius) {
        return radius >= Constants.MIN_SEARCH_RADIUS_KM &&
                radius <= Constants.MAX_SEARCH_RADIUS_KM;
    }

    // =============================================
    // OFFER VALIDATION
    // =============================================
    public static boolean isValidOffer(double offerAmount, double originalPrice) {
        if (offerAmount <= 0 || originalPrice <= 0) {
            return false;
        }

        double percentage = offerAmount / originalPrice;
        return percentage >= Constants.MIN_OFFER_PERCENTAGE &&
                percentage <= Constants.MAX_OFFER_PERCENTAGE;
    }

    // =============================================
    // REVIEW VALIDATION
    // =============================================
    public static boolean isValidRating(int rating) {
        return rating >= Constants.MIN_RATING && rating <= Constants.MAX_RATING;
    }

    public static boolean isValidReviewComment(String comment) {
        if (TextUtils.isEmpty(comment)) {
            return true; // Comment is optional
        }

        String trimmed = comment.trim();
        return trimmed.length() >= Constants.MIN_REVIEW_LENGTH &&
                trimmed.length() <= Constants.MAX_REVIEW_LENGTH;
    }

    // =============================================
    // SEARCH VALIDATION
    // =============================================
    public static boolean isValidSearchQuery(String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }

        String trimmed = query.trim();
        return trimmed.length() >= Constants.MIN_SEARCH_QUERY_LENGTH;
    }

    // =============================================
    // FILE VALIDATION
    // =============================================
    public static boolean isValidImageType(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }

        for (String allowedType : Constants.ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }

    // =============================================
    // UTILITY METHODS
    // =============================================
    public static String sanitizeInput(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }

        return input.trim().replaceAll("\\s+", " ");
    }

    public static boolean isEmptyOrWhitespace(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        return Patterns.WEB_URL.matcher(url).matches();
    }

    // =============================================
    // ERROR MESSAGE GENERATORS
    // =============================================
    public static String getEmailErrorMessage(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email is required";
        }

        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }

        return null;
    }

    public static String getPasswordErrorMessage(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }

        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters";
        }

        if (password.length() > Constants.MAX_PASSWORD_LENGTH) {
            return "Password must be less than " + Constants.MAX_PASSWORD_LENGTH + " characters";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }

        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one number";
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return "Password must contain at least one special character";
        }

        return null;
    }

    public static String getDisplayNameErrorMessage(String displayName) {
        if (TextUtils.isEmpty(displayName)) {
            return "Display name is required";
        }

        String trimmed = displayName.trim();

        if (trimmed.length() < Constants.MIN_DISPLAY_NAME_LENGTH) {
            return "Display name must be at least " + Constants.MIN_DISPLAY_NAME_LENGTH + " characters";
        }

        if (trimmed.length() > Constants.MAX_DISPLAY_NAME_LENGTH) {
            return "Display name must be less than " + Constants.MAX_DISPLAY_NAME_LENGTH + " characters";
        }

        Pattern pattern = Pattern.compile("^[a-zA-Z0-9\\s\\-']+$");
        if (!pattern.matcher(trimmed).matches()) {
            return "Display name can only contain letters, numbers, spaces, hyphens, and apostrophes";
        }

        return null;
    }

    public static String getProductTitleErrorMessage(String title) {
        if (TextUtils.isEmpty(title)) {
            return "Product title is required";
        }

        String trimmed = title.trim();

        if (trimmed.length() < Constants.MIN_PRODUCT_TITLE_LENGTH) {
            return "Product title must be at least " + Constants.MIN_PRODUCT_TITLE_LENGTH + " characters";
        }

        if (trimmed.length() > Constants.MAX_PRODUCT_TITLE_LENGTH) {
            return "Product title must be less than " + Constants.MAX_PRODUCT_TITLE_LENGTH + " characters";
        }

        return null;
    }

    public static String getProductDescriptionErrorMessage(String description) {
        if (TextUtils.isEmpty(description)) {
            return "Product description is required";
        }

        String trimmed = description.trim();

        if (trimmed.length() < Constants.MIN_PRODUCT_DESCRIPTION_LENGTH) {
            return "Product description must be at least " + Constants.MIN_PRODUCT_DESCRIPTION_LENGTH + " characters";
        }

        if (trimmed.length() > Constants.MAX_PRODUCT_DESCRIPTION_LENGTH) {
            return "Product description must be less than " + Constants.MAX_PRODUCT_DESCRIPTION_LENGTH + " characters";
        }

        return null;
    }

    public static String getPriceErrorMessage(String priceStr) {
        if (TextUtils.isEmpty(priceStr)) {
            return "Price is required";
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                return "Price must be greater than 0";
            }
            if (price > 1000000) {
                return "Price cannot exceed $1,000,000";
            }
        } catch (NumberFormatException e) {
            return "Please enter a valid price";
        }

        return null;
    }

    // Private constructor to prevent instantiation
    private ValidationUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }
}