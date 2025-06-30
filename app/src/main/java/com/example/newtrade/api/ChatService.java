// app/src/main/java/com/example/newtrade/api/ChatService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface ChatService {

    // ===== CONVERSATIONS =====

    @GET("conversations")
    Call<StandardResponse<Map<String, Object>>> getConversations(
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    @GET("conversations/{id}")
    Call<StandardResponse<Map<String, Object>>> getConversation(
            @Path("id") Long conversationId,
            @Header("User-ID") Long userId
    );

    @POST("conversations")
    Call<StandardResponse<Map<String, Object>>> createConversation(
            @Body Map<String, Object> conversationData,
            @Header("User-ID") Long userId
    );

    @POST("conversations/product/{productId}")
    Call<StandardResponse<Map<String, Object>>> createConversationForProduct(
            @Path("productId") Long productId,
            @Body Map<String, Object> messageData,
            @Header("User-ID") Long userId
    );

    @DELETE("conversations/{id}")
    Call<StandardResponse<Void>> deleteConversation(
            @Path("id") Long conversationId,
            @Header("User-ID") Long userId
    );

    // ===== MESSAGES =====

    @GET("conversations/{conversationId}/messages")
    Call<StandardResponse<Map<String, Object>>> getMessages(
            @Path("conversationId") Long conversationId,
            @Query("page") int page,
            @Query("size") int size,
            @Query("before") String before,
            @Header("User-ID") Long userId
    );

    @POST("conversations/{conversationId}/messages")
    Call<StandardResponse<Map<String, Object>>> sendMessage(
            @Path("conversationId") Long conversationId,
            @Body Map<String, Object> messageData,
            @Header("User-ID") Long userId
    );

    @PUT("messages/{id}/read")
    Call<StandardResponse<Void>> markMessageAsRead(
            @Path("id") Long messageId,
            @Header("User-ID") Long userId
    );

    @PUT("conversations/{id}/read")
    Call<StandardResponse<Void>> markConversationAsRead(
            @Path("id") Long conversationId,
            @Header("User-ID") Long userId
    );

    @DELETE("messages/{id}")
    Call<StandardResponse<Void>> deleteMessage(
            @Path("id") Long messageId,
            @Header("User-ID") Long userId
    );

    // ===== NOTIFICATIONS =====

    @GET("chat/notifications")
    Call<StandardResponse<List<Map<String, Object>>>> getChatNotifications(
            @Header("User-ID") Long userId
    );

    @PUT("chat/notifications/{id}/read")
    Call<StandardResponse<Void>> markNotificationAsRead(
            @Path("id") Long notificationId,
            @Header("User-ID") Long userId
    );

    @PUT("chat/notifications/read-all")
    Call<StandardResponse<Void>> markAllNotificationsAsRead(
            @Header("User-ID") Long userId
    );

    // ===== CHAT STATS =====

    @GET("chat/stats")
    Call<StandardResponse<Map<String, Object>>> getChatStats(
            @Header("User-ID") Long userId
    );

    @GET("conversations/{id}/stats")
    Call<StandardResponse<Map<String, Object>>> getConversationStats(
            @Path("id") Long conversationId,
            @Header("User-ID") Long userId
    );

    // ===== FILE UPLOAD FOR CHAT =====

    @Multipart
    @POST("chat/upload")
    Call<StandardResponse<Map<String, String>>> uploadChatFile(
            @Part okhttp3.MultipartBody.Part file,
            @Header("User-ID") Long userId
    );
}