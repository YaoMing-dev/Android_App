// app/src/main/java/com/example/newtrade/api/UserService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

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

    @PUT("users/profile/deactivate")
    Call<StandardResponse<Void>> deactivateAccount(@Header("User-ID") Long userId);

    @PUT("users/profile/reactivate")
    Call<StandardResponse<Void>> reactivateAccount(@Header("User-ID") Long userId);

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

    // ===== SAVED ITEMS =====

    @GET("users/saved-items")
    Call<StandardResponse<Map<String, Object>>> getSavedItems(
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    @POST("users/saved-items/{productId}")
    Call<StandardResponse<Void>> saveProduct(
            @Path("productId") Long productId,
            @Header("User-ID") Long userId
    );

    @DELETE("users/saved-items/{productId}")
    Call<StandardResponse<Void>> unsaveProduct(
            @Path("productId") Long productId,
            @Header("User-ID") Long userId
    );

    // ===== TRANSACTIONS =====

    @GET("users/transactions")
    Call<StandardResponse<Map<String, Object>>> getUserTransactions(
            @Query("type") String type, // "PURCHASE", "SALE", "ALL"
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    // ===== FILE UPLOAD =====

    @Multipart
    @POST("users/upload-avatar")
    Call<StandardResponse<Map<String, String>>> uploadAvatar(
            @Part MultipartBody.Part avatar,
            @Header("User-ID") Long userId
    );

    // ===== BLOCKING & REPORTING =====

    @POST("users/{id}/block")
    Call<StandardResponse<Void>> blockUser(
            @Path("id") Long userIdToBlock,
            @Header("User-ID") Long userId
    );

    @DELETE("users/{id}/block")
    Call<StandardResponse<Void>> unblockUser(
            @Path("id") Long userIdToUnblock,
            @Header("User-ID") Long userId
    );

    @GET("users/blocked")
    Call<StandardResponse<Map<String, Object>>> getBlockedUsers(
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    @POST("users/{id}/report")
    Call<StandardResponse<Void>> reportUser(
            @Path("id") Long userIdToReport,
            @Body Map<String, String> reportData,
            @Header("User-ID") Long userId
    );
}