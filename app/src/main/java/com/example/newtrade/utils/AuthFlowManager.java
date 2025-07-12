// app/src/main/java/com/example/newtrade/utils/AuthFlowManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.newtrade.ui.auth.OtpVerificationActivity;

/**
 * ✅ AuthFlowManager - Quản lý các flow xác thực
 * Giúp tách biệt rõ ràng các flow: Login, Register, Forgot Password
 * FR-1.1.2: Email verification required for account activation
 */
public class AuthFlowManager {

    private static final String TAG = "AuthFlowManager";

    /**
     * Enum định nghĩa các loại flow xác thực
     */
    public enum AuthFlow {
        LOGIN,           // Đăng nhập bình thường - KIỂM TRA account activation
        REGISTER,        // Đăng ký mới - CẦN email verification theo FR-1.1.2
        FORGOT_PASSWORD  // Quên mật khẩu - CẦN OTP verification
    }

    /**
     * Kiểm tra xem flow có cần email verification không
     *
     * @param flow Loại flow
     * @param isEmailVerified Trạng thái email đã verify chưa
     * @return true nếu cần verify, false nếu không
     */
    public static boolean requiresEmailVerification(AuthFlow flow, boolean isEmailVerified) {
        switch (flow) {
            case LOGIN:
                // ✅ LOGIN: CẦN KIỂM TRA account activation
                boolean needsActivation = !isEmailVerified;
                Log.d(TAG, "LOGIN flow - account activation needed: " + needsActivation);
                return needsActivation;

            case REGISTER:
                // ✅ FR-1.1.2: REGISTER cần email verification nếu chưa verify
                boolean needsVerify = !isEmailVerified;
                Log.d(TAG, "REGISTER flow - email verification required: " + needsVerify);
                return needsVerify;

            case FORGOT_PASSWORD:
                // ✅ FORGOT PASSWORD: LUÔN cần OTP verification
                Log.d(TAG, "FORGOT_PASSWORD flow - OTP verification required");
                return true;

            default:
                Log.w(TAG, "Unknown flow type, defaulting to no verification");
                return false;
        }
    }

    /**
     * Check if user needs account activation (email verification)
     * Used for both register and login flows according to FR-1.1.2
     */
    public static boolean needsAccountActivation(boolean isEmailVerified) {
        boolean needsActivation = !isEmailVerified;
        Log.d(TAG, "Account activation needed: " + needsActivation + " (EmailVerified: " + isEmailVerified + ")");
        return needsActivation;
    }

    /**
     * Tạo Intent cho OTP verification với flow context
     *
     * @param context Context
     * @param email Email cần verify
     * @param flow Loại flow
     * @return Intent cho OtpVerificationActivity
     */
    public static Intent createOtpIntent(Context context, String email, AuthFlow flow) {
        Intent intent = new Intent(context, OtpVerificationActivity.class);
        intent.putExtra("email", email);

        // fromRegister: true = register flow, false = forgot password flow
        intent.putExtra("fromRegister", flow == AuthFlow.REGISTER);

        Log.d(TAG, "Created OTP intent for flow: " + flow + ", email: " + email);
        return intent;
    }

    /**
     * Create account activation intent (used from login when account not activated)
     * FR-1.1.2: Email verification required for account activation
     */
    public static Intent createAccountActivationIntent(Context context, String email) {
        Intent intent = new Intent(context, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("fromRegister", true); // Use register flow for activation
        intent.putExtra("isAccountActivation", true);
        intent.putExtra("fromLogin", true);

        Log.d(TAG, "Created account activation intent for email: " + email);
        return intent;
    }

    /**
     * Log flow information for debugging
     */
    public static void logFlowInfo(AuthFlow flow, String email, boolean isEmailVerified) {
        Log.d(TAG, "=== AUTH FLOW INFO ===");
        Log.d(TAG, "Flow: " + flow);
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Email Verified: " + isEmailVerified);
        Log.d(TAG, "Requires Verification: " + requiresEmailVerification(flow, isEmailVerified));
        Log.d(TAG, "Needs Account Activation: " + needsAccountActivation(isEmailVerified));
        Log.d(TAG, "====================");
    }

    /**
     * Validate flow parameters
     */
    public static boolean isValidFlowData(String email, AuthFlow flow) {
        if (email == null || email.trim().isEmpty()) {
            Log.e(TAG, "Invalid email for flow: " + flow);
            return false;
        }

        if (flow == null) {
            Log.e(TAG, "Flow cannot be null");
            return false;
        }

        return true;
    }

    /**
     * Get user-friendly flow description
     */
    public static String getFlowDescription(AuthFlow flow) {
        switch (flow) {
            case LOGIN:
                return "Đăng nhập";
            case REGISTER:
                return "Đăng ký tài khoản";
            case FORGOT_PASSWORD:
                return "Quên mật khẩu";
            default:
                return "Unknown";
        }
    }

    /**
     * Get activation message based on context
     */
    public static String getActivationMessage(AuthFlow flow, boolean fromLogin) {
        if (flow == AuthFlow.REGISTER && fromLogin) {
            return "Tài khoản chưa được kích hoạt!\n" +
                    "Vui lòng xác thực email để kích hoạt tài khoản và tiếp tục sử dụng app.";
        } else if (flow == AuthFlow.REGISTER) {
            return "Vui lòng xác thực email để kích hoạt tài khoản của bạn.";
        } else {
            return "Vui lòng xác thực để tiếp tục.";
        }
    }

    /**
     * Check if current flow is account activation from login
     */
    public static boolean isAccountActivationFromLogin(boolean isAccountActivation, boolean fromLogin) {
        return isAccountActivation && fromLogin;
    }
}