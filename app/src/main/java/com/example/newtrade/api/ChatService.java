// app/src/main/java/com/example/newtrade/api/ChatService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface ChatService {

    @GET("conversations")
    Call<StandardResponse<Map<String, Object>>> getConversations(
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    @POST("conversations/start")
    Call<StandardResponse<Map<String, Object>>> startConversation(
            @Query("productId") Long productId,
            @Header("User-ID") Long userId
    );

    @POST("conversations/start-product")
    Call<StandardResponse<Map<String, Object>>> startConversationForProduct(
            @Query("productId") Long productId,
            @Body Map<String, Object> messageData,
            @Header("User-ID") Long userId
    );

    @GET("conversations/{id}/messages")
    Call<StandardResponse<Map<String, Object>>> getMessages(
            @Path("id") Long conversationId,
            @Query("page") int page,
            @Query("size") int size,
            @Query("before") String beforeMessageId,
            @Header("User-ID") Long userId
    );

    @POST("conversations/{id}/messages")
    Call<StandardResponse<Map<String, Object>>> sendMessage(
            @Path("id") Long conversationId,
            @Body Map<String, Object> messageData,
            @Header("User-ID") Long userId
    );

    @PUT("conversations/{id}/read")
    Call<StandardResponse<Void>> markConversationAsRead(
            @Path("id") Long conversationId,
            @Header("User-ID") Long userId
    );

    @PUT("messages/{id}/read")
    Call<StandardResponse<Void>> markMessageAsRead(
            @Path("id") Long messageId,
            @Header("User-ID") Long userId
    );

    @DELETE("conversations/{id}")
    Call<StandardResponse<Void>> deleteConversation(
            @Path("id") Long conversationId,
            @Header("User-ID") Long userId
    );

    @POST("conversations/{id}/block")
    Call<StandardResponse<Void>> blockUser(
            @Path("id") Long conversationId,
            @Header("User-ID") Long userId
    );

    @POST("conversations/{id}/report")
    Call<StandardResponse<Void>> reportConversation(
            @Path("id") Long conversationId,
            @Body Map<String, String> reportData,
            @Header("User-ID") Long userId
    );
}