// app/src/main/java/com/example/newtrade/api/FileUploadApiService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadApiService {

    /**
     * Upload product image
     */
    @Multipart
    @POST("upload/product-image")
    Call<StandardResponse<Map<String, String>>> uploadProductImage(
            @Part MultipartBody.Part file,
            @Part("description") RequestBody description
    );

    /**
     * Upload avatar image
     */
    @Multipart
    @POST("upload/avatar")
    Call<StandardResponse<Map<String, String>>> uploadAvatar(
            @Part MultipartBody.Part file,
            @Part("description") RequestBody description
    );

    /**
     * Upload multiple product images
     */
    @Multipart
    @POST("upload/product-images")
    Call<StandardResponse<Map<String, Object>>> uploadProductImages(
            @Part MultipartBody.Part[] files,
            @Part("description") RequestBody description
    );

    /**
     * Upload chat image
     */
    @Multipart
    @POST("upload/chat-image")
    Call<StandardResponse<Map<String, String>>> uploadChatImage(
            @Part MultipartBody.Part file,
            @Part("conversationId") RequestBody conversationId
    );
}