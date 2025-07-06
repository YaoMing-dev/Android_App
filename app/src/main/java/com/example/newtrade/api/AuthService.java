// app/src/main/java/com/example/newtrade/api/AuthService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthService {

    // ===== AUTHENTICATION ENDPOINTS =====

    @POST("api/auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body Map<String, String> request);

    @POST("api/auth/login")
    Call<StandardResponse<Map<String, Object>>> login(@Body Map<String, String> request);

    @POST("api/auth/register")
    Call<StandardResponse<Map<String, Object>>> register(@Body Map<String, String> request);

    // ===== OTP ENDPOINTS (CHỈ cho email verification và forgot password) =====

    @POST("api/auth/send-otp")
    Call<StandardResponse<Map<String, String>>> sendOtp(@Body Map<String, String> request);

    @POST("api/auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOtp(@Body Map<String, String> request);

    @POST("api/auth/resend-otp")
    Call<StandardResponse<Map<String, String>>> resendOtp(@Body Map<String, String> request);

    // ===== FORGOT/RESET PASSWORD =====

    @POST("api/auth/forgot-password")
    Call<StandardResponse<Map<String, String>>> forgotPassword(@Body Map<String, String> request);

    @POST("api/auth/reset-password")
    Call<StandardResponse<Map<String, String>>> resetPassword(@Body Map<String, String> request);

    @GET("api/auth/validate-reset-token")
    Call<StandardResponse<Map<String, Object>>> validateResetToken(@Query("token") String token);

    // ✅ THÊM: Reset password with email (sau khi verify OTP)
    @POST("api/auth/reset-password-with-email")
    Call<StandardResponse<Map<String, String>>> resetPasswordWithEmail(@Body Map<String, String> request);

    // ===== SESSION & PASSWORD MANAGEMENT =====

    @POST("api/auth/change-password")
    Call<StandardResponse<Map<String, String>>> changePassword(
            @Body Map<String, String> request,
            @Header("User-ID") Long userId
    );

    @GET("api/auth/validate-session")
    Call<StandardResponse<Map<String, Object>>> validateSession(@Header("User-ID") Long userId);

    @POST("api/auth/logout")
    Call<StandardResponse<Map<String, String>>> logout(@Header("User-ID") Long userId);

    // ===== UTILITY ENDPOINTS =====

    @GET("api/auth/health")
    Call<StandardResponse<String>> healthCheck();
}