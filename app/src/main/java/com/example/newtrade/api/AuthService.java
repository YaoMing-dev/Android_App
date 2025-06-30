// app/src/main/java/com/example/newtrade/api/AuthService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface AuthService {

    @POST("auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body Map<String, String> request);

    @POST("auth/register")
    Call<StandardResponse<Map<String, Object>>> register(@Body Map<String, Object> request);

    @POST("auth/login")
    Call<StandardResponse<Map<String, Object>>> login(@Body Map<String, String> request);

    @POST("auth/send-otp")
    Call<StandardResponse<Map<String, String>>> sendOtp(@Body Map<String, String> request);

    @POST("auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOtp(@Body Map<String, String> request);

    @POST("auth/forgot-password")
    Call<StandardResponse<Map<String, String>>> forgotPassword(@Body Map<String, String> request);

    @POST("auth/reset-password")
    Call<StandardResponse<Map<String, String>>> resetPassword(@Body Map<String, String> request);

    @POST("auth/logout")
    Call<StandardResponse<Void>> logout(@Header("User-ID") Long userId);
}