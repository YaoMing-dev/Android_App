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

    // Get paginated notifications
    @GET("api/notifications")
    Call<StandardResponse<Map<String, Object>>> getNotifications(
            @Query("page") int page,
            @Query("size") int size
    );

    // Get unread notifications count
    @GET("api/notifications/unread-count")
    Call<StandardResponse<Map<String, Object>>> getUnreadCount();

    // Mark single notification as read
    @PUT("api/notifications/{id}/mark-read")
    Call<StandardResponse<String>> markAsRead(@Path("id") Long notificationId);

    // Mark all notifications as read
    @PUT("api/notifications/mark-all-read")
    Call<StandardResponse<String>> markAllAsRead();

    // ===== EXISTING SEND METHODS =====

    // Send message notification - Chat notifications
    @POST("api/notifications/send-message")
    Call<StandardResponse<String>> sendMessageNotification(@Body Map<String, Object> request);

    // Send offer notification - Price offer notifications
    @POST("api/notifications/send-offer")
    Call<StandardResponse<String>> sendOfferNotification(@Body Map<String, Object> request);

    // Send general notification - General app notifications
    @POST("api/notifications/send-general")
    Call<StandardResponse<String>> sendGeneralNotification(@Body Map<String, Object> request);

    // Send product upload notification - Product listing updates
    @POST("api/notifications/send-product-upload")
    Call<StandardResponse<String>> sendProductUploadNotification(@Body Map<String, Object> request);

    // Send transaction notification - Payment/transaction notifications
    @POST("api/notifications/send-transaction")
    Call<StandardResponse<String>> sendTransactionNotification(@Body Map<String, Object> request);

    // ===== ✅ NEW: PROMOTION METHODS - FR-4.2.1 =====

    // Send single promotion notification
    @POST("api/notifications/send-promotion")
    Call<StandardResponse<String>> sendPromotionNotification(@Body Map<String, Object> request);

    // Send bulk promotion to multiple users
    @POST("api/notifications/send-bulk-promotion")
    Call<StandardResponse<String>> sendBulkPromotionNotification(@Body Map<String, Object> request);

    // Send location-based promotion
    @POST("api/notifications/send-location-promotion")
    Call<StandardResponse<String>> sendLocationBasedPromotion(@Body Map<String, Object> request);

    // ===== ✅ NEW: LISTING UPDATE METHODS - FR-4.2.1 =====

    // Send listing update notification
    @POST("api/notifications/send-listing-update")
    Call<StandardResponse<String>> sendListingUpdateNotification(@Body Map<String, Object> request);

    // ===== ✅ NEW: PREFERENCE MANAGEMENT METHODS =====

    // Get notification preferences
    @GET("api/notifications/preferences")
    Call<StandardResponse<Map<String, Object>>> getNotificationPreferences();

    // Update notification preferences
    @PUT("api/notifications/preferences")
    Call<StandardResponse<String>> updateNotificationPreferences(@Body Map<String, Object> preferences);

    // ===== UTILITY METHODS =====

    // Get notification by ID
    @GET("api/notifications/{id}")
    Call<StandardResponse<Map<String, Object>>> getNotificationById(@Path("id") Long notificationId);

    // Delete notification (if needed)
    @PUT("api/notifications/{id}/delete")
    Call<StandardResponse<String>> deleteNotification(@Path("id") Long notificationId);
}