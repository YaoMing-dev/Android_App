package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthService {

    // ===== AUTHENTICATION ENDPOINTS =====

    // Google Sign-In
    @POST("api/auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body Map<String, String> request);

    // Regular email login (if backend has this endpoint)
    @POST("api/auth/login")
    Call<StandardResponse<Map<String, Object>>> login(@Body Map<String, String> request);

    // Regular email registration (if backend has this endpoint)
    @POST("api/auth/register")
    Call<StandardResponse<User>> register(@Body Map<String, String> request);

    // ===== OTP ENDPOINTS =====

    // Send OTP for email verification
    @POST("api/auth/send-otp")
    Call<StandardResponse<Map<String, String>>> sendOtp(@Body Map<String, String> request);

    // Verify OTP code
    @POST("api/auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOtp(@Body Map<String, String> request);

    // Resend OTP verification
    @POST("api/auth/resend-otp")
    Call<StandardResponse<Map<String, String>>> resendOtp(@Body Map<String, String> request);

    // ===== PASSWORD RECOVERY ENDPOINTS =====

    // Send password reset email
    @POST("api/auth/forgot-password")
    Call<StandardResponse<Map<String, String>>> forgotPassword(@Body Map<String, String> request);

    // Reset password with token
    @POST("api/auth/reset-password")
    Call<StandardResponse<Map<String, String>>> resetPassword(@Body Map<String, String> request);

    // ===== SESSION MANAGEMENT =====

    // Validate session (requires User-ID header)
    @GET("api/auth/validate-session")
    Call<StandardResponse<Map<String, Boolean>>> validateSession();

    // Logout (requires User-ID header)
    @POST("api/auth/logout")
    Call<StandardResponse<Map<String, String>>> logout();

    // ===== UTILITY ENDPOINTS =====

    // Health check - ✅ Fixed to GET
    @GET("api/auth/health")
    Call<StandardResponse<String>> healthCheck();
}