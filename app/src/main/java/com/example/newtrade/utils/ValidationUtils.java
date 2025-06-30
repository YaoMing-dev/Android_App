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
        return !TextUtils.isEmpty(password) && password.length() >= Constants.MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= Constants.MIN_NAME_LENGTH
                && name.trim().length() <= Constants.MAX_NAME_LENGTH;
    }

    public static boolean isValidPrice(String priceString) {
        if (TextUtils.isEmpty(priceString)) return false;

        try {
            BigDecimal price = new BigDecimal(priceString);
            return price.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // FR-2.1.1: Required fields validation
    public static boolean isValidProductTitle(String title) {
        return !TextUtils.isEmpty(title) &&
                title.trim().length() >= Constants.MIN_PRODUCT_TITLE_LENGTH &&
                title.trim().length() <= Constants.MAX_PRODUCT_TITLE_LENGTH;
    }

    public static boolean isValidProductDescription(String description) {
        return !TextUtils.isEmpty(description) &&
                description.trim().length() >= Constants.MIN_PRODUCT_DESCRIPTION_LENGTH &&
                description.trim().length() <= Constants.MAX_PRODUCT_DESCRIPTION_LENGTH;
    }

    public static boolean isValidPhoneNumber(String phone) {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isValidOTP(String otp) {
        return !TextUtils.isEmpty(otp) && otp.length() == Constants.OTP_LENGTH && otp.matches("\\d+");
    }

    public static boolean isValidLocation(String location) {
        return !TextUtils.isEmpty(location) && location.trim().length() >= 3;
    }

    public static boolean isValidBio(String bio) {
        return bio == null || bio.trim().length() <= 500; // Optional field
    }

    public static boolean isValidDisplayName(String displayName) {
        return !TextUtils.isEmpty(displayName) &&
                displayName.trim().length() >= 2 &&
                displayName.trim().length() <= 50;
    }
}