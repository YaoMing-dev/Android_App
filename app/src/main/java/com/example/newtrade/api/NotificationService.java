// app/src/main/java/com/example/newtrade/api/NotificationService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationService {

    @GET("/api/notifications")
    Call<StandardResponse<Map<String, Object>>> getNotifications(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/notifications/unread-count")
    Call<StandardResponse<Map<String, Object>>> getUnreadCount(
            @Header("User-ID") Long userId);

    @PUT("/api/notifications/{id}/mark-read")
    Call<StandardResponse<Map<String, Object>>> markAsRead(
            @Header("User-ID") Long userId,
            @Path("id") Long notificationId);

    @PUT("/api/notifications/mark-all-read")
    Call<StandardResponse<Map<String, Object>>> markAllAsRead(
            @Header("User-ID") Long userId);
}