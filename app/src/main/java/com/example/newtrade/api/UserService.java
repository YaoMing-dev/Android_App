// app/src/main/java/com/example/newtrade/api/UserService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserService {

    @GET("/api/users/profile")
    Call<StandardResponse<Map<String, Object>>> getUserProfile(@Header("User-ID") Long userId);

    @PUT("/api/users/profile")
    Call<StandardResponse<Map<String, Object>>> updateUserProfile(
            @Header("User-ID") Long userId,
            @Body Map<String, Object> profileUpdateRequest);

    @Multipart
    @POST("/api/users/profile/avatar/upload")
    Call<StandardResponse<Map<String, Object>>> uploadAvatar(
            @Header("User-ID") Long userId,
            @Part MultipartBody.Part file);

    @PUT("/api/users/profile/avatar")
    Call<StandardResponse<Map<String, Object>>> updateAvatar(
            @Header("User-ID") Long userId,
            @Query("avatarUrl") String avatarUrl);

    @DELETE("/api/users/profile/avatar")
    Call<StandardResponse<Map<String, Object>>> deleteAvatar(@Header("User-ID") Long userId);

    @GET("/api/users/{userId}/public-profile")
    Call<StandardResponse<Map<String, Object>>> getPublicProfile(@Path("userId") Long userId);

    @PUT("/api/users/deactivate")
    Call<StandardResponse<Map<String, Object>>> deactivateAccount(@Header("User-ID") Long userId);

    @DELETE("/api/users/delete")
    Call<StandardResponse<Map<String, Object>>> deleteAccount(@Header("User-ID") Long userId);

    @POST("/api/users/password-reset-request")
    Call<StandardResponse<Map<String, Object>>> requestPasswordReset(@Body Map<String, Object> request);

    @POST("/api/users/password-reset-confirm")
    Call<StandardResponse<Map<String, Object>>> confirmPasswordReset(@Body Map<String, Object> request);

    @PUT("/api/users/fcm-token")
    Call<StandardResponse<Map<String, Object>>> updateFcmToken(
            @Header("User-ID") Long userId,
            @Body Map<String, Object> fcmTokenRequest);

    @GET("/api/users/stats")
    Call<StandardResponse<Map<String, Object>>> getUserStats(@Header("User-ID") Long userId);
}