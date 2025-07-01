// app/src/main/java/com/example/newtrade/api/UserApiService.java
package com.example.newtrade.api;

import com.example.newtrade.models.User;
import com.example.newtrade.models.UserProfileRequest;
import com.example.newtrade.models.UserProfileResponse;
import com.example.newtrade.models.StandardResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApiService {

    /**
     * Get current user profile
     */
    @GET("users/profile")
    Call<StandardResponse<UserProfileResponse>> getCurrentUserProfile();

    /**
     * Get user profile by ID
     */
    @GET("users/{id}/profile")
    Call<StandardResponse<UserProfileResponse>> getUserProfile(@Path("id") Long id);

    /**
     * Update user profile
     */
    @PUT("users/profile")
    Call<StandardResponse<UserProfileResponse>> updateProfile(@Body UserProfileRequest request);

    /**
     * Get user statistics
     */
    @GET("users/stats")
    Call<StandardResponse<Map<String, Object>>> getUserStats();
}