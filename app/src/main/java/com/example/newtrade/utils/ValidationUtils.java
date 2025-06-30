// app/src/main/java/com/example/newtrade/utils/ValidationUtils.java
package com.example.newtrade.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Email validation
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Password validation
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    // Strong password validation
    public static boolean isStrongPassword(String password) {
        if (TextUtils.isEmpty(password) || password.length() < 8) {
            return false;
        }

        // Check for at least one digit, one lowercase, one uppercase, one special character
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$");
        return pattern.matcher(password).matches();
    }

    // Name validation
    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }

    // Phone number validation (basic)
    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }

        // Remove spaces and special characters
        String cleanPhone = phone.replaceAll("[^0-9+]", "");

        // Check if it's a valid phone number pattern
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 15;
    }

    // Vietnamese phone number validation
    public static boolean isValidVietnamesePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }

        String cleanPhone = phone.replaceAll("[^0-9]", "");

        // Vietnamese phone patterns
        return cleanPhone.matches("^(03|05|07|08|09)[0-9]{8}$") || // Mobile
                cleanPhone.matches("^(84)(3|5|7|8|9)[0-9]{8}$") ||    // International mobile
                cleanPhone.matches("^(02)[0-9]{9,10}$");              // Landline
    }

    // Price validation
    public static boolean isValidPrice(String priceStr) {
        if (TextUtils.isEmpty(priceStr)) {
            return false;
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);
            return price.compareTo(BigDecimal.ZERO) > 0 &&
                    price.compareTo(new BigDecimal("999999999")) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Product title validation
    public static boolean isValidProductTitle(String title) {
        return !TextUtils.isEmpty(title) &&
                title.trim().length() >= 3 &&
                title.trim().length() <= Constants.MAX_PRODUCT_TITLE_LENGTH;
    }

    // Product description validation
    public static boolean isValidProductDescription(String description) {
        return !TextUtils.isEmpty(description) &&
                description.trim().length() >= 10 &&
                description.trim().length() <= Constants.MAX_PRODUCT_DESCRIPTION_LENGTH;
    }

    // OTP validation
    public static boolean isValidOTP(String otp) {
        return !TextUtils.isEmpty(otp) &&
                otp.length() == 6 &&
                TextUtils.isDigitsOnly(otp);
    }

    // URL validation
    public static boolean isValidUrl(String url) {
        return !TextUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url).matches();
    }

    // Location validation
    public static boolean isValidLocation(String location) {
        return !TextUtils.isEmpty(location) && location.trim().length() >= 2;
    }

    // Coordinate validation
    public static boolean isValidLatitude(Double latitude) {
        return latitude != null && latitude >= -90.0 && latitude <= 90.0;
    }

    public static boolean isValidLongitude(Double longitude) {
        return longitude != null && longitude >= -180.0 && longitude <= 180.0;
    }

    // Search query validation
    public static boolean isValidSearchQuery(String query) {
        return !TextUtils.isEmpty(query) &&
                query.trim().length() >= 2 &&
                query.trim().length() <= 100;
    }

    // Message validation
    public static boolean isValidMessage(String message) {
        return !TextUtils.isEmpty(message) &&
                message.trim().length() > 0 &&
                message.trim().length() <= Constants.MAX_MESSAGE_LENGTH;
    }

    // Bio validation
    public static boolean isValidBio(String bio) {
        return TextUtils.isEmpty(bio) ||
                (bio.trim().length() >= 10 && bio.trim().length() <= 500);
    }

    // Display name validation
    public static boolean isValidDisplayName(String displayName) {
        return TextUtils.isEmpty(displayName) ||
                (displayName.trim().length() >= 2 && displayName.trim().length() <= 50);
    }

    // File size validation
    public static boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= Constants.MAX_FILE_SIZE;
    }

    // Image extension validation
    public static boolean isValidImageExtension(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }

        String extension = fileName.toLowerCase();
        for (String allowedType : Constants.ALLOWED_IMAGE_TYPES) {
            if (extension.endsWith("." + allowedType)) {
                return true;
            }
        }
        return false;
    }

    // Generic text length validation
    public static boolean isValidTextLength(String text, int minLength, int maxLength) {
        if (TextUtils.isEmpty(text)) {
            return minLength == 0;
        }

        int length = text.trim().length();
        return length >= minLength && length <= maxLength;
    }

    // Number range validation
    public static boolean isValidNumberRange(String numberStr, double min, double max) {
        if (TextUtils.isEmpty(numberStr)) {
            return false;
        }

        try {
            double number = Double.parseDouble(numberStr);
            return number >= min && number <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Password confirmation validation
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return !TextUtils.isEmpty(password) &&
                !TextUtils.isEmpty(confirmPassword) &&
                password.equals(confirmPassword);
    }

    // Credit card validation (basic Luhn algorithm)
    public static boolean isValidCreditCard(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return false;
        }

        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");

        if (cleanNumber.length() < 13 || cleanNumber.length() > 19) {
            return false;
        }

        return isValidLuhn(cleanNumber);
    }

    private static boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(cardNumber.substring(i, i + 1));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    // Get password strength
    public static PasswordStrength getPasswordStrength(String password) {
        if (TextUtils.isEmpty(password)) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;

        // Character type checks
        if (password.matches(".*[a-z].*")) score++; // lowercase
        if (password.matches(".*[A-Z].*")) score++; // uppercase
        if (password.matches(".*[0-9].*")) score++; // digits
        if (password.matches(".*[@#$%^&+=!].*")) score++; // special chars

        if (score <= 2) return PasswordStrength.WEAK;
        if (score <= 4) return PasswordStrength.MEDIUM;
        return PasswordStrength.STRONG;
    }

    public enum PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}