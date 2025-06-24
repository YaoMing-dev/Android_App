package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ===== AUTHENTICATION =====
    @POST("api/auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body Map<String, String> request);

    @POST("api/auth/logout")
    Call<StandardResponse<String>> logout();

    // ===== CATEGORIES =====
    @GET("api/categories")
    Call<StandardResponse<List<Map<String, Object>>>> getCategories();

    // ===== PRODUCTS =====
    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search,
            @Query("category") String category
    );

    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts();

    @POST("api/products")
    Call<StandardResponse<Map<String, Object>>> createProduct(@Body Map<String, Object> productData);

    @GET("api/products/{id}")
    Call<StandardResponse<Map<String, Object>>> getProductDetail(@Path("id") Long productId);

    // ===== SEARCH =====
    @GET("api/products/search")
    Call<StandardResponse<Map<String, Object>>> searchProducts(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice
    );

    // ===== USER MANAGEMENT =====
    @GET("api/users/{id}/profile")
    Call<StandardResponse<Map<String, Object>>> getUserProfile(@Path("id") Long userId);

    @PUT("api/users/{id}/profile")
    Call<StandardResponse<Map<String, Object>>> updateUserProfile(
            @Path("id") Long userId,
            @Body Map<String, Object> profileData
    );

    @GET("api/users/{id}/stats")
    Call<StandardResponse<Map<String, Object>>> getUserStats(@Path("id") Long userId);

    // ===== CONVERSATIONS & MESSAGES =====
    @GET("api/conversations/user/{userId}")
    Call<StandardResponse<List<Map<String, Object>>>> getUserConversations(@Path("userId") Long userId);

    @GET("api/conversations/{id}")
    Call<StandardResponse<Map<String, Object>>> getConversation(@Path("id") Long conversationId);

    @POST("api/conversations")
    Call<StandardResponse<Map<String, Object>>> createConversation(@Body Map<String, Object> conversationData);

    // ===== FILE UPLOAD =====
    @Multipart
    @POST("api/files/upload/product")
    Call<StandardResponse<Map<String, String>>> uploadProductImage(
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("api/files/upload/avatar")
    Call<StandardResponse<Map<String, String>>> uploadAvatar(
            @Part MultipartBody.Part image
    );

    // ===== HEALTH CHECK =====
    @GET("/")
    Call<StandardResponse<String>> healthCheck();
}