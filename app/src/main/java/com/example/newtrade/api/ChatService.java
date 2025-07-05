// app/src/main/java/com/example/newtrade/api/ChatService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatService {

    @POST("/api/conversations/start")
    Call<StandardResponse<Map<String, Object>>> startConversation(
            @Header("User-ID") Long userId,
            @Query("productId") Long productId);

    @POST("/api/conversations/find-or-create")
    Call<StandardResponse<Map<String, Object>>> findOrCreateConversation(
            @Header("User-ID") Long userId,
            @Query("productId") Long productId);

    @GET("/api/conversations")
    Call<StandardResponse<Map<String, Object>>> getConversations(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/conversations/{id}")
    Call<StandardResponse<Map<String, Object>>> getConversation(
            @Header("User-ID") Long userId,
            @Path("id") Long conversationId);

    @PUT("/api/conversations/{id}/deactivate")
    Call<StandardResponse<Map<String, Object>>> deactivateConversation(
            @Header("User-ID") Long userId,
            @Path("id") Long conversationId);

    @POST("/api/messages/send")
    Call<StandardResponse<Map<String, Object>>> sendMessage(
            @Header("User-ID") Long userId,
            @Body Map<String, Object> messageRequest);

    @GET("/api/messages/conversation/{conversationId}")
    Call<StandardResponse<Map<String, Object>>> getMessages(
            @Header("User-ID") Long userId,
            @Path("conversationId") Long conversationId,
            @Query("page") int page,
            @Query("size") int size);

    @PUT("/api/messages/conversation/{conversationId}/mark-read")
    Call<StandardResponse<Map<String, Object>>> markMessagesAsRead(
            @Header("User-ID") Long userId,
            @Path("conversationId") Long conversationId);

    @GET("/api/messages/unread-count")
    Call<StandardResponse<Map<String, Object>>> getUnreadMessageCount(
            @Header("User-ID") Long userId);
}