// app/src/main/java/com/example/newtrade/api/AuthService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.Map;

public interface AuthService {

    @POST("auth/login")
    Call<StandardResponse<Map<String, Object>>> login(@Body Map<String, Object> loginRequest);

    @POST("auth/register")
    Call<StandardResponse<Map<String, Object>>> register(@Body Map<String, Object> registerRequest);

    @POST("auth/google-login")
    Call<StandardResponse<Map<String, Object>>> googleLogin(@Body Map<String, String> googleRequest);

    @POST("auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOTP(@Body Map<String, String> otpRequest);

    @POST("auth/resend-otp")
    Call<StandardResponse<Map<String, String>>> resendOTP(@Body Map<String, String> resendRequest);

    @POST("auth/forgot-password")
    Call<StandardResponse<Map<String, String>>> forgotPassword(@Body Map<String, String> forgotRequest);

    @POST("auth/reset-password")
    Call<StandardResponse<Map<String, String>>> resetPassword(@Body Map<String, String> resetRequest);

    @GET("auth/health")
    Call<StandardResponse<String>> healthCheck();
}