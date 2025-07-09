// app/src/main/java/com/example/newtrade/api/NotificationService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationService {

    // ===== EXISTING METHODS (GET NOTIFICATIONS) =====

    // ✅ Get paginated notifications
    @GET("api/notifications")
    Call<StandardResponse<Map<String, Object>>> getNotifications(
            @Query("page") int page,
            @Query("size") int size
    );

    // ✅ Get unread notifications count
    @GET("api/notifications/unread-count")
    Call<StandardResponse<Map<String, Object>>> getUnreadCount();

    // ✅ Mark single notification as read
    @PUT("api/notifications/{id}/mark-read")
    Call<StandardResponse<String>> markAsRead(@Path("id") Long notificationId);

    // ✅ Mark all notifications as read
    @PUT("api/notifications/mark-all-read")
    Call<StandardResponse<String>> markAllAsRead();

    // ===== NEW METHODS (SEND NOTIFICATIONS) =====

    // ✅ THÊM: Send message notification - Chat notifications
    @POST("api/notifications/send-message")
    Call<StandardResponse<String>> sendMessageNotification(@Body Map<String, Object> request);

    // ✅ THÊM: Send offer notification - Price offer notifications
    @POST("api/notifications/send-offer")
    Call<StandardResponse<String>> sendOfferNotification(@Body Map<String, Object> request);

    // ✅ THÊM: Send general notification - General app notifications
    @POST("api/notifications/send-general")
    Call<StandardResponse<String>> sendGeneralNotification(@Body Map<String, Object> request);

    // ✅ THÊM: Send product upload notification - Product listing updates
    @POST("api/notifications/send-product-upload")
    Call<StandardResponse<String>> sendProductUploadNotification(@Body Map<String, Object> request);

    // ✅ THÊM: Send listing update notification - When product is updated
    @POST("api/notifications/send-listing-update")
    Call<StandardResponse<String>> sendListingUpdateNotification(@Body Map<String, Object> request);

    // ✅ THÊM: Send transaction notification - Payment/transaction notifications
    @POST("api/notifications/send-transaction")
    Call<StandardResponse<String>> sendTransactionNotification(@Body Map<String, Object> request);

    // ✅ THÊM: Send bulk notification - Send to multiple users
    @POST("api/notifications/send-bulk")
    Call<StandardResponse<String>> sendBulkNotification(@Body Map<String, Object> request);

    // ===== UTILITY METHODS =====

    // ✅ THÊM: Get notification by ID
    @GET("api/notifications/{id}")
    Call<StandardResponse<Map<String, Object>>> getNotificationById(@Path("id") Long notificationId);

    // ✅ THÊM: Delete notification
    @PUT("api/notifications/{id}/delete")
    Call<StandardResponse<String>> deleteNotification(@Path("id") Long notificationId);

    // ✅ THÊM: Get notification preferences
    @GET("api/notifications/preferences")
    Call<StandardResponse<Map<String, Object>>> getNotificationPreferences();

    // ✅ THÊM: Update notification preferences
    @PUT("api/notifications/preferences")
    Call<StandardResponse<String>> updateNotificationPreferences(@Body Map<String, Object> preferences);
}