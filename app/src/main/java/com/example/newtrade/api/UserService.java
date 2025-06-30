// app/src/main/java/com/example/newtrade/api/UserService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface UserService {

    // ===== USER PROFILE =====

    @GET("users/{id}")
    Call<StandardResponse<User>> getUserById(@Path("id") Long id);

    @GET("users/profile")
    Call<StandardResponse<User>> getMyProfile(@Header("User-ID") Long userId);

    @PUT("users/profile")
    Call<StandardResponse<User>> updateProfile(
            @Body Map<String, Object> userData,
            @Header("User-ID") Long userId
    );

    @DELETE("users/profile")
    Call<StandardResponse<Void>> deleteAccount(@Header("User-ID") Long userId);

    // ===== USER SEARCH & DISCOVERY =====

    @GET("users/search")
    Call<StandardResponse<Map<String, Object>>> searchUsers(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("users/nearby")
    Call<StandardResponse<Map<String, Object>>> getNearbyUsers(
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Query("radius") Integer radius,
            @Query("page") int page,
            @Query("size") int size
    );

    // ===== USER VERIFICATION =====

    @POST("users/verify-phone")
    Call<StandardResponse<Map<String, String>>> initiatePhoneVerification(
            @Body Map<String, String> phoneData,
            @Header("User-ID") Long userId
    );

    @POST("users/verify-phone-otp")
    Call<StandardResponse<Void>> verifyPhoneOTP(
            @Body Map<String, String> otpData,
            @Header("User-ID") Long userId
    );

    // ===== USER SETTINGS =====

    @GET("users/settings")
    Call<StandardResponse<Map<String, Object>>> getUserSettings(
            @Header("User-ID") Long userId
    );

    @PUT("users/settings")
    Call<StandardResponse<Map<String, Object>>> updateUserSettings(
            @Body Map<String, Object> settings,
            @Header("User-ID") Long userId
    );

    // ===== USER STATS & ANALYTICS =====

    @GET("users/{id}/stats")
    Call<StandardResponse<Map<String, Object>>> getUserStats(@Path("id") Long id);

    @GET("users/dashboard")
    Call<StandardResponse<Map<String, Object>>> getUserDashboard(
            @Header("User-ID") Long userId
    );

    // ===== USER REVIEWS =====

    @GET("users/{id}/reviews")
    Call<StandardResponse<Map<String, Object>>> getUserReviews(
            @Path("id") Long id,
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("users/{id}/reviews")
    Call<StandardResponse<Map<String, Object>>> createUserReview(
            @Path("id") Long userId,
            @Body Map<String, Object> reviewData,
            @Header("User-ID") Long reviewerId
    );

    // ===== USER BLOCKING =====

    @POST("users/{id}/block")
    Call<StandardResponse<Void>> blockUser(
            @Path("id") Long userId,
            @Header("User-ID") Long blockerId
    );

    @DELETE("users/{id}/block")
    Call<StandardResponse<Void>> unblockUser(
            @Path("id") Long userId,
            @Header("User-ID") Long blockerId
    );

    @GET("users/blocked")
    Call<StandardResponse<List<User>>> getBlockedUsers(
            @Header("User-ID") Long userId
    );

    // ===== USER FOLLOWING =====

    @POST("users/{id}/follow")
    Call<StandardResponse<Void>> followUser(
            @Path("id") Long userId,
            @Header("User-ID") Long followerId
    );

    @DELETE("users/{id}/follow")
    Call<StandardResponse<Void>> unfollowUser(
            @Path("id") Long userId,
            @Header("User-ID") Long followerId
    );

    @GET("users/{id}/followers")
    Call<StandardResponse<Map<String, Object>>> getUserFollowers(
            @Path("id") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("users/{id}/following")
    Call<StandardResponse<Map<String, Object>>> getUserFollowing(
            @Path("id") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );
}