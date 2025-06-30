// app/src/main/java/com/example/newtrade/api/UserService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface UserService {

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

    @GET("users/search")
    Call<StandardResponse<Map<String, Object>>> searchUsers(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("users/{id}/stats")
    Call<StandardResponse<Map<String, Object>>> getUserStats(@Path("id") Long id);

    @GET("users/dashboard")
    Call<StandardResponse<Map<String, Object>>> getUserDashboard(
            @Header("User-ID") Long userId
    );
}