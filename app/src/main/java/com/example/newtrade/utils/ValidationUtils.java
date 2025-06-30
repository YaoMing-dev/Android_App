// app/src/main/java/com/example/newtrade/utils/ValidationUtils.java
package com.example.newtrade.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.math.BigDecimal;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 8;
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }

    public static boolean isValidOTP(String otp) {
        return !TextUtils.isEmpty(otp) && otp.length() == 6 && otp.matches("\\d{6}");
    }

    public static boolean isValidPrice(String priceStr) {
        if (TextUtils.isEmpty(priceStr)) return false;
        try {
            BigDecimal price = new BigDecimal(priceStr);
            return price.compareTo(BigDecimal.ZERO) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidProductTitle(String title) {
        return !TextUtils.isEmpty(title) &&
                title.trim().length() >= 5 &&
                title.length() <= Constants.MAX_PRODUCT_TITLE_LENGTH;
    }

    public static boolean isValidProductDescription(String description) {
        return !TextUtils.isEmpty(description) &&
                description.trim().length() >= 10 &&
                description.length() <= Constants.MAX_PRODUCT_DESCRIPTION_LENGTH;
    }

    public static String getEmailValidationError(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email is required";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }
        return null;
    }

    public static String getPasswordValidationError(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }
        if (!isValidPassword(password)) {
            return "Password must be at least 8 characters";
        }
        return null;
    }

    public static String getNameValidationError(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Name is required";
        }
        if (!isValidName(name)) {
            return "Name must be at least 2 characters";
        }
        return null;
    }
}