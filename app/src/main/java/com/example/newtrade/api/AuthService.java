// app/src/main/java/com/example/newtrade/api/AuthService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface AuthService {

    @POST("auth/login")
    Call<StandardResponse<Map<String, Object>>> login(@Body Map<String, String> loginData);

    @POST("auth/register")
    Call<StandardResponse<Map<String, Object>>> register(@Body Map<String, Object> registerData);

    @POST("auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body Map<String, String> googleData);

    @POST("auth/send-otp")
    Call<StandardResponse<Map<String, String>>> sendOtp(@Body Map<String, String> otpRequest);

    @POST("auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOtp(@Body Map<String, String> otpData);

    @POST("auth/forgot-password")
    Call<StandardResponse<Map<String, String>>> forgotPassword(@Body Map<String, String> forgotData);

    @POST("auth/reset-password")
    Call<StandardResponse<Map<String, String>>> resetPassword(@Body Map<String, String> resetData);

    @GET("auth/health")
    Call<StandardResponse<String>> health();
}