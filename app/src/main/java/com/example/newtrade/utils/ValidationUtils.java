// app/src/main/java/com/example/newtrade/utils/ValidationUtils.java
package com.example.newtrade.utils;

import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= Constants.MIN_PASSWORD_LENGTH &&
                password.length() <= Constants.MAX_PASSWORD_LENGTH;
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= Constants.MIN_NAME_LENGTH &&
                name.trim().length() <= Constants.MAX_NAME_LENGTH;
    }

    public static boolean isValidDisplayName(String displayName) {
        return isValidName(displayName);
    }

    public static boolean isValidPrice(String priceText) {
        try {
            double price = Double.parseDouble(priceText);
            return price > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.trim().length() >= 10 &&
                phone.trim().matches("^[+]?[0-9\\s\\-\\(\\)]*$");
    }

    public static boolean isValidBio(String bio) {
        return bio == null || bio.trim().length() <= 500;
    }

    public static boolean isValidLocation(String location) {
        return location != null && location.trim().length() >= 2 && location.trim().length() <= 200;
    }

    public static boolean isValidProductTitle(String title) {
        return title != null && title.trim().length() >= Constants.MIN_PRODUCT_TITLE_LENGTH &&
                title.trim().length() <= Constants.MAX_PRODUCT_TITLE_LENGTH;
    }

    public static boolean isValidProductDescription(String description) {
        return description != null && description.trim().length() >= Constants.MIN_PRODUCT_DESCRIPTION_LENGTH &&
                description.trim().length() <= Constants.MAX_PRODUCT_DESCRIPTION_LENGTH;
    }

    public static boolean isValidOtp(String otp) {
        return otp != null && otp.trim().length() == Constants.OTP_LENGTH &&
                otp.trim().matches("^[0-9]+$");
    }
}