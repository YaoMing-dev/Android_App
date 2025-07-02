// app/src/main/java/com/example/newtrade/api/ApiService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ===== AUTHENTICATION - UPDATED =====

    @POST("api/auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body Map<String, String> request);

    @POST("api/auth/register") // ✅ NEW - Email/Password Registration
    Call<StandardResponse<Map<String, Object>>> register(@Body Map<String, String> request);

    @POST("api/auth/login") // ✅ NEW - Email/Password Login
    Call<StandardResponse<Map<String, Object>>> login(@Body Map<String, String> request);

    @POST("api/auth/send-otp")
    Call<StandardResponse<String>> sendOtp(@Body Map<String, String> request);

    @POST("api/auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOtp(@Body Map<String, String> request);

    @GET("api/auth/health")
    Call<StandardResponse<String>> healthCheck();

    @POST("api/auth/logout")
    Call<StandardResponse<String>> logout();

    // ===== USER MANAGEMENT - UPDATED =====

    @GET("api/users/profile") // ✅ FIXED - No {id} needed
    Call<StandardResponse<Map<String, Object>>> getCurrentUserProfile();

    @GET("api/users/{id}/profile")
    Call<StandardResponse<Map<String, Object>>> getUserProfile(@Path("id") Long userId);

    @PUT("api/users/profile") // ✅ FIXED - No {id} needed
    Call<StandardResponse<Map<String, Object>>> updateUserProfile(@Body Map<String, Object> profileData);

    @GET("api/users/{id}/stats")
    Call<StandardResponse<Map<String, Object>>> getUserStats(@Path("id") Long userId);

    // ===== AVATAR UPLOAD - NEW =====

    @Multipart
    @POST("api/files/upload/avatar")
    Call<StandardResponse<Map<String, String>>> uploadAvatar(@Part MultipartBody.Part image);

    @Multipart
    @POST("api/users/profile/avatar/upload") // ✅ NEW - Upload + Update in one step
    Call<StandardResponse<Map<String, String>>> uploadAndUpdateAvatar(@Part MultipartBody.Part image);

    @PUT("api/users/profile/avatar")
    Call<StandardResponse<Map<String, String>>> updateAvatarUrl(@Body Map<String, String> avatarData);

    @DELETE("api/users/profile/avatar")
    Call<StandardResponse<String>> deleteAvatar();

    // ===== PRODUCTS =====

    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir
    );

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

    @GET("api/products/{id}")
    Call<StandardResponse<Map<String, Object>>> getProductDetail(@Path("id") Long productId);

    @POST("api/products")
    Call<StandardResponse<Map<String, Object>>> createProduct(@Body Map<String, Object> productData);

    @PUT("api/products/{id}")
    Call<StandardResponse<Map<String, Object>>> updateProduct(
            @Path("id") Long productId,
            @Body Map<String, Object> productData
    );

    @DELETE("api/products/{id}")
    Call<StandardResponse<String>> deleteProduct(@Path("id") Long productId);

    @PUT("api/products/{id}/mark-sold")
    Call<StandardResponse<Map<String, Object>>> markProductAsSold(@Path("id") Long productId);

    @GET("api/products/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getUserProducts(@Path("userId") Long userId);

    // ===== CATEGORIES =====

    @GET("api/categories")
    Call<StandardResponse<List<Map<String, Object>>>> getCategories();

    @GET("api/categories/all")
    Call<StandardResponse<List<Map<String, Object>>>> getAllCategories();

    @GET("api/categories/{id}")
    Call<StandardResponse<Map<String, Object>>> getCategory(@Path("id") Long categoryId);

    // ===== MESSAGING - NEW =====

    @POST("api/messages/send")
    Call<StandardResponse<Map<String, Object>>> sendMessage(@Body Map<String, Object> messageData);

    @GET("api/messages/conversation/{conversationId}")
    Call<StandardResponse<Map<String, Object>>> getConversationMessages(
            @Path("conversationId") Long conversationId,
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("api/messages/conversation/{conversationId}/mark-read")
    Call<StandardResponse<String>> markMessagesAsRead(@Path("conversationId") Long conversationId);

    @GET("api/messages/unread-count")
    Call<StandardResponse<Map<String, Object>>> getUnreadMessageCount();

    // ===== CONVERSATIONS - NEW =====

    @POST("api/conversations/start")
    Call<StandardResponse<Map<String, Object>>> startConversation(@Body Map<String, Object> conversationData);

    @POST("api/conversations/find-or-create")
    Call<StandardResponse<Map<String, Object>>> findOrCreateConversation(@Body Map<String, Object> conversationData);

    @GET("api/conversations")
    Call<StandardResponse<Map<String, Object>>> getConversations(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/conversations/{id}")
    Call<StandardResponse<Map<String, Object>>> getConversation(@Path("id") Long conversationId);

    // ===== NOTIFICATIONS - NEW =====

    @GET("api/notifications")
    Call<StandardResponse<Map<String, Object>>> getNotifications(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/notifications/unread-count")
    Call<StandardResponse<Map<String, Object>>> getUnreadNotificationCount();

    @PUT("api/notifications/{id}/mark-read")
    Call<StandardResponse<String>> markNotificationAsRead(@Path("id") Long notificationId);

    @PUT("api/notifications/mark-all-read")
    Call<StandardResponse<String>> markAllNotificationsAsRead();

    // ===== FILE UPLOAD =====

    @Multipart
    @POST("api/files/upload/product")
    Call<StandardResponse<Map<String, String>>> uploadProductImage(@Part MultipartBody.Part image);

    @GET("api/files/info")
    Call<StandardResponse<Map<String, Object>>> getFileUploadInfo();

    // ===== OFFERS - NEW =====

    @POST("api/offers")
    Call<StandardResponse<Map<String, Object>>> makeOffer(@Body Map<String, Object> offerData);

    @PUT("api/offers/{id}/respond")
    Call<StandardResponse<Map<String, Object>>> respondToOffer(
            @Path("id") Long offerId,
            @Body Map<String, Object> responseData
    );

    @GET("api/offers/buyer")
    Call<StandardResponse<Map<String, Object>>> getBuyerOffers(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/offers/seller")
    Call<StandardResponse<Map<String, Object>>> getSellerOffers(
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("api/offers/{id}/cancel")
    Call<StandardResponse<String>> cancelOffer(@Path("id") Long offerId);

    // ===== SAVED ITEMS - NEW =====

    @POST("api/saved-items/{productId}")
    Call<StandardResponse<String>> saveItem(@Path("productId") Long productId);

    @DELETE("api/saved-items/{productId}")
    Call<StandardResponse<String>> unsaveItem(@Path("productId") Long productId);

    @GET("api/saved-items")
    Call<StandardResponse<Map<String, Object>>> getSavedItems(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/saved-items/{productId}/is-saved")
    Call<StandardResponse<Map<String, Object>>> isItemSaved(@Path("productId") Long productId);

    // ===== ANALYTICS =====

    @GET("api/analytics/dashboard")
    Call<StandardResponse<Map<String, Object>>> getDashboardAnalytics();

    @GET("api/analytics/product/{productId}/stats")
    Call<StandardResponse<Map<String, Object>>> getProductAnalytics(@Path("productId") Long productId);
}