// app/src/main/java/com/example/newtrade/api/NotificationService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationService {

    // ✅ UPDATED: Trả về Map<String, Object> như ApiService
    @GET("api/notifications")
    Call<StandardResponse<Map<String, Object>>> getNotifications(
            @Query("page") int page,
            @Query("size") int size
    );

    // ✅ UPDATED: Trả về Map<String, Object> như ApiService
    @GET("api/notifications/unread-count")
    Call<StandardResponse<Map<String, Object>>> getUnreadCount();

    // ✅ UPDATED: Trả về String như ApiService
    @PUT("api/notifications/{id}/mark-read")
    Call<StandardResponse<String>> markAsRead(@Path("id") Long notificationId);

    // ✅ UPDATED: Trả về String như ApiService
    @PUT("api/notifications/mark-all-read")
    Call<StandardResponse<String>> markAllAsRead();
}