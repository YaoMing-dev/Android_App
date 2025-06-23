// app/src/main/java/com/example/newtrade/utils/ValidationUtils.java
package com.example.newtrade.utils;

import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= Constants.MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidDisplayName(String displayName) {
        return displayName != null && displayName.trim().length() >= 2;
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && Patterns.PHONE.matcher(phoneNumber).matches();
    }

    public static String getEmailError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email không được để trống";
        }
        if (!isValidEmail(email)) {
            return "Email không hợp lệ";
        }
        return null;
    }

    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Mật khẩu không được để trống";
        }
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            return "Mật khẩu phải có ít nhất " + Constants.MIN_PASSWORD_LENGTH + " ký tự";
        }
        return null;
    }

    public static String getDisplayNameError(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return "Tên hiển thị không được để trống";
        }
        if (displayName.trim().length() < 2) {
            return "Tên hiển thị phải có ít nhất 2 ký tự";
        }
        return null;
    }
}