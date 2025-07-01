// app/src/main/java/com/example/newtrade/api/AuthApiService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthApiService {

    /**
     * Login with email and password
     */
    @POST("auth/login")
    Call<StandardResponse<Map<String, Object>>> login(@Body LoginRequest request);

    /**
     * Register new user
     */
    @POST("auth/register")
    Call<StandardResponse<Map<String, Object>>> register(@Body RegisterRequest request);

    /**
     * Google Sign-In with ID token
     */
    @POST("auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body GoogleSignInRequest request);

    /**
     * Send OTP for email verification
     */
    @POST("auth/send-otp")
    Call<StandardResponse<Map<String, String>>> sendOtp(@Body OtpRequest request);

    /**
     * Verify OTP code
     */
    @POST("auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOtp(@Body OtpVerificationRequest request);

    /**
     * Resend OTP
     */
    @POST("auth/resend-otp")
    Call<StandardResponse<Map<String, String>>> resendOtp(@Body OtpRequest request);

    /**
     * Forgot password - send reset email
     */
    @POST("auth/forgot-password")
    Call<StandardResponse<Map<String, String>>> forgotPassword(@Body ForgotPasswordRequest request);

    /**
     * Reset password with token
     */
    @PUT("auth/reset-password")
    Call<StandardResponse<Map<String, String>>> resetPassword(@Body ResetPasswordRequest request);

    /**
     * Change password (logged in user)
     */
    @PUT("auth/change-password")
    Call<StandardResponse<Map<String, String>>> changePassword(@Body ChangePasswordRequest request);

    /**
     * Logout user
     */
    @POST("auth/logout")
    Call<StandardResponse<Map<String, String>>> logout();

    /**
     * Validate session
     */
    @POST("auth/validate-session")
    Call<StandardResponse<Map<String, Boolean>>> validateSession();

    // =============================================
    // REQUEST CLASSES
    // =============================================

    class LoginRequest {
        private String email;
        private String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        // Getters
        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }

    class RegisterRequest {
        private String displayName;
        private String email;
        private String password;

        public RegisterRequest(String displayName, String email, String password) {
            this.displayName = displayName;
            this.email = email;
            this.password = password;
        }

        // Getters
        public String getDisplayName() { return displayName; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }

    class GoogleSignInRequest {
        private String idToken;

        public GoogleSignInRequest(String idToken) {
            this.idToken = idToken;
        }

        public String getIdToken() { return idToken; }
    }

    class OtpRequest {
        private String email;

        public OtpRequest(String email) {
            this.email = email;
        }

        public String getEmail() { return email; }
    }

    class OtpVerificationRequest {
        private String email;
        private String otpCode;

        public OtpVerificationRequest(String email, String otpCode) {
            this.email = email;
            this.otpCode = otpCode;
        }

        public String getEmail() { return email; }
        public String getOtpCode() { return otpCode; }
    }

    class ForgotPasswordRequest {
        private String email;

        public ForgotPasswordRequest(String email) {
            this.email = email;
        }

        public String getEmail() { return email; }
    }

    class ResetPasswordRequest {
        private String token;
        private String newPassword;

        public ResetPasswordRequest(String token, String newPassword) {
            this.token = token;
            this.newPassword = newPassword;
        }

        public String getToken() { return token; }
        public String getNewPassword() { return newPassword; }
    }

    class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public ChangePasswordRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }

        public String getCurrentPassword() { return currentPassword; }
        public String getNewPassword() { return newPassword; }
    }
}