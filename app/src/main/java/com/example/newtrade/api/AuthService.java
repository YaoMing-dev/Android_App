// app/src/main/java/com/example/newtrade/api/AuthService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthService {

    @POST("/api/auth/login")
    Call<StandardResponse<Map<String, Object>>> login(@Body Map<String, Object> loginRequest);

    @POST("/api/auth/register")
    Call<StandardResponse<Map<String, Object>>> register(@Body Map<String, Object> registerRequest);

    @POST("/api/auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body Map<String, Object> googleSignInRequest);

    @POST("/api/auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOtp(@Body Map<String, Object> otpRequest);

    @POST("/api/auth/send-otp")
    Call<StandardResponse<Map<String, Object>>> sendOtp(@Body Map<String, Object> otpRequest);

    @POST("/api/auth/resend-otp")
    Call<StandardResponse<Map<String, Object>>> resendOtp(@Body Map<String, Object> resendOtpRequest);

    @POST("/api/auth/logout")
    Call<StandardResponse<Map<String, Object>>> logout();

    @GET("/api/auth/health")
    Call<StandardResponse<Map<String, Object>>> healthCheck();

    @GET("/api/auth/validate-session")
    Call<StandardResponse<Map<String, Object>>> validateSession();

    @POST("/api/auth/change-password")
    Call<StandardResponse<Map<String, Object>>> changePassword(@Body Map<String, Object> changePasswordRequest);
}