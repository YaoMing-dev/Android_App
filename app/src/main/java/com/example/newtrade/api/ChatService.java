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

    @GET("conversations/{id}/messages")
    Call<StandardResponse<Map<String, Object>>> getMessages(
            @Path("id") Long conversationId,
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    @POST("conversations/{id}/messages")
    Call<StandardResponse<Map<String, Object>>> sendMessage(
            @Path("id") Long conversationId,
            @Body Map<String, Object> messageData,
            @Header("User-ID") Long userId
    );
}