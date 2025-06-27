// app/src/main/java/com/example/newtrade/api/UserService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService {

    // ✅ Get current user profile - trả về UserProfileResponse từ backend
    @GET("api/users/profile")
    Call<StandardResponse<User>> getCurrentUserProfile();

    // ✅ Get user profile by ID - trả về UserProfileResponse từ backend
    @GET("api/users/{id}/profile")
    Call<StandardResponse<User>> getUserProfile(@Path("id") Long userId);

    // ✅ Update user profile - match với UserProfileRequest backend
    @PUT("api/users/profile")
    Call<StandardResponse<User>> updateProfile(@Body Map<String, Object> request);

    // ✅ Get user statistics
    @GET("api/users/stats")
    Call<StandardResponse<Map<String, Object>>> getUserStats();

    // ✅ Update FCM token cho push notifications
    @PUT("api/users/fcm-token")
    Call<StandardResponse<String>> updateFcmToken(@Body Map<String, String> request);
}