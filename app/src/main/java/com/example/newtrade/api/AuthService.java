// app/src/main/java/com/example/newtrade/api/AuthService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    // Google Sign-In
    @POST("api/auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body Map<String, String> request);

    // Send OTP for email verification
    @POST("api/auth/send-otp")
    Call<StandardResponse<Map<String, String>>> sendOtp(@Body Map<String, String> request);

    // Verify OTP code
    @POST("api/auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOtp(@Body Map<String, String> request);

    // Regular email registration
    @POST("api/auth/register")
    Call<StandardResponse<User>> register(@Body Map<String, String> request);

    // Regular email login
    @POST("api/auth/login")
    Call<StandardResponse<Map<String, Object>>> login(@Body Map<String, String> request);

    // Send password reset email
    @POST("api/auth/password-reset")
    Call<StandardResponse<String>> sendPasswordReset(@Body Map<String, String> request);

    // Verify password reset token and set new password
    @POST("api/auth/password-reset-confirm")
    Call<StandardResponse<String>> confirmPasswordReset(@Body Map<String, String> request);

    // Resend OTP verification
    @POST("api/auth/resend-otp")
    Call<StandardResponse<String>> resendOtp(@Body Map<String, String> request);
}