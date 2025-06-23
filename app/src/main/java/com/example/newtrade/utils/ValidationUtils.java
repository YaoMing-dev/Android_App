// app/src/main/java/com/example/newtrade/utils/ValidationUtils.java
package com.example.newtrade.utils;

import android.util.Patterns;

public class ValidationUtils {

    // Email validation
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static String getEmailError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email không được để trống";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return "Email không hợp lệ";
        }
        return null;
    }

    // Password validation
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= Constants.MIN_PASSWORD_LENGTH;
    }

    public static String getPasswordError(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Mật khẩu không được để trống";
        }
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            return "Mật khẩu phải có ít nhất " + Constants.MIN_PASSWORD_LENGTH + " ký tự";
        }
        return null;
    }

    // Confirm password validation
    public static boolean isPasswordMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    public static String getConfirmPasswordError(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return "Xác nhận mật khẩu không được để trống";
        }
        if (!password.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp";
        }
        return null;
    }

    // Display name validation
    public static boolean isValidDisplayName(String displayName) {
        return displayName != null && displayName.trim().length() >= 2;
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

    // OTP validation
    public static boolean isValidOtp(String otp) {
        return otp != null && otp.length() == Constants.OTP_LENGTH && otp.matches("\\d+");
    }

    public static String getOtpError(String otp) {
        if (otp == null || otp.trim().isEmpty()) {
            return "Mã OTP không được để trống";
        }
        if (otp.length() != Constants.OTP_LENGTH) {
            return "Mã OTP phải có " + Constants.OTP_LENGTH + " số";
        }
        if (!otp.matches("\\d+")) {
            return "Mã OTP chỉ được chứa số";
        }
        return null;
    }

    // Phone validation
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9]{10,11}$");
    }

    public static String getPhoneError(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // Phone is optional
        }
        if (!phone.matches("^[0-9]{10,11}$")) {
            return "Số điện thoại không hợp lệ";
        }
        return null;
    }
}