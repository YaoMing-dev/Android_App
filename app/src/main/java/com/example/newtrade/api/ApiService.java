package com.example.newtrade.api;

import com.example.newtrade.models.Product;
import com.example.newtrade.models.User;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.PagedResponse;
import com.example.newtrade.models.Review;
import com.example.newtrade.models.Offer;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    // Auth APIs - ✅ FIX: All return StandardResponse
    @POST("auth/login")
    Call<StandardResponse<Map<String, Object>>> login(@Body Map<String, String> credentials);

    @POST("auth/register")
    Call<StandardResponse<User>> register(@Body Map<String, String> userData);

    @POST("auth/forgot-password")
    Call<StandardResponse<Map<String, String>>> forgotPassword(@Body Map<String, String> email);

    @POST("auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOTP(@Body Map<String, String> otpData);

    @POST("auth/send-otp")
    Call<StandardResponse<Map<String, String>>> sendOtp(@Body Map<String, String> request);

    @POST("auth/verify-otp")
    Call<StandardResponse<Map<String, Object>>> verifyOtp(@Body Map<String, String> request);

    @POST("auth/resend-otp")
    Call<StandardResponse<Map<String, String>>> resendOtp(@Body Map<String, String> request);

    @POST("auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body Map<String, String> request);

    @GET("auth/health")
    Call<StandardResponse<String>> healthCheck();

    // Product APIs
    @GET("products")
    Call<StandardResponse<PagedResponse<Product>>> getAllProducts(
        @Query("page") int page,
        @Query("size") int size,
        @Query("category") String category,
        @Query("search") String search,
        @Query("minPrice") Double minPrice,
        @Query("maxPrice") Double maxPrice,
        @Query("condition") String condition,
        @Query("sort") String sort
    );

    // ✅ FIX: Updated to return StandardResponse
    @GET("products")
    Call<StandardResponse<List<Product>>> getProducts(
        @Query("page") int page,
        @Query("size") int size,
        @Query("category") String category,
        @Query("search") String search
    );

    @GET("products/featured")
    Call<StandardResponse<List<Map<String, Object>>>> getFeaturedProducts();

    // Location/Search APIs
    @POST("products/search")
    Call<StandardResponse<PagedResponse<Product>>> searchProductsAdvanced(
        @Body Map<String, Object> searchRequest
    );

    @GET("products/search")
    Call<List<Product>> searchProducts(
        @Query("search") String search,
        @Query("page") int page,
        @Query("size") int size,
        @Query("categoryId") Long categoryId,
        @Query("minPrice") Double minPrice,
        @Query("maxPrice") Double maxPrice,
        @Query("condition") String condition
    );

    @GET("products/{id}")
    Call<StandardResponse<Product>> getProductById(@Path("id") Long id);

    // ✅ FIX: Updated createProduct to accept Map<String,Object>
    @Multipart
    @POST("products")
    Call<StandardResponse<Product>> createProduct(
        @Part("product") Map<String, Object> productRequest,
        @Part List<MultipartBody.Part> images
    );

    @PUT("products/{id}")
    Call<StandardResponse<Product>> updateProduct(@Path("id") Long id, @Body Product product);

    @DELETE("products/{id}")
    Call<StandardResponse<Void>> deleteProduct(@Path("id") Long id);

    // User APIs
    @GET("users/profile")
    Call<StandardResponse<User>> getCurrentUser();

    @GET("users/{userId}")
    Call<StandardResponse<User>> getUserById(@Path("userId") Long userId);

    @GET("users/{userId}/products")
    Call<StandardResponse<List<Product>>> getUserProducts(
        @Path("userId") Long userId,
        @Query("page") int page,
        @Query("size") int size
    );

    @GET("users/{userId}/reviews")
    Call<StandardResponse<List<Review>>> getUserReviews(@Path("userId") Long userId);

    @POST("users/{userId}/reviews")
    Call<StandardResponse<Review>> createUserReview(
        @Path("userId") Long userId,
        @Body Map<String, Object> reviewData
    );

    @Multipart
    @PUT("users/profile")
    Call<StandardResponse<User>> updateProfile(
        @Part("user") User user,
        @Part MultipartBody.Part avatar
    );

    @GET("users/products")
    Call<StandardResponse<List<Product>>> getUserProducts(
        @Query("page") int page,
        @Query("size") int size
    );

    // Favorites APIs
    @GET("favorites")
    Call<StandardResponse<List<Product>>> getFavorites();

    @POST("favorites/{productId}")
    Call<StandardResponse<Void>> addToFavorites(@Path("productId") Long productId);

    @DELETE("favorites/{productId}")
    Call<StandardResponse<Void>> removeFromFavorites(@Path("productId") Long productId);

    // Chat APIs
    @GET("chats")
    Call<StandardResponse<List<Map<String, Object>>>> getChats();

    @GET("chats/{chatId}/messages")
    Call<StandardResponse<List<Map<String, Object>>>> getChatMessages(
        @Path("chatId") Long chatId,
        @Query("page") int page,
        @Query("size") int size
    );

    @POST("chats/{chatId}/messages")
    Call<StandardResponse<Map<String, Object>>> sendMessage(
        @Path("chatId") Long chatId,
        @Body Map<String, String> message
    );

    @POST("chats")
    Call<StandardResponse<Map<String, Object>>> createChat(@Body Map<String, Object> chatData);

    // ✅ FIX: Upload APIs return StandardResponse
    @Multipart
    @POST("upload/product-image")
    Call<StandardResponse<Map<String, String>>> uploadProductImage(@Part okhttp3.MultipartBody.Part image);

    @Multipart
    @POST("upload/avatar")
    Call<StandardResponse<Map<String, String>>> uploadAvatar(@Part okhttp3.MultipartBody.Part image);

    // Offer APIs
    @GET("offers/received")
    Call<StandardResponse<List<Offer>>> getReceivedOffers();

    @GET("offers/sent")
    Call<StandardResponse<List<Offer>>> getSentOffers();

    @POST("offers")
    Call<StandardResponse<Offer>> createOffer(@Body Map<String, Object> offerData);

    @PUT("offers/{offerId}/status")
    Call<StandardResponse<Offer>> updateOfferStatus(
        @Path("offerId") Long offerId,
        @Body Map<String, Object> statusData
    );

    @GET("offers/{offerId}")
    Call<StandardResponse<Offer>> getOfferById(@Path("offerId") Long offerId);

    // Transaction APIs
    @POST("transactions")
    Call<StandardResponse<Map<String, Object>>> createTransaction(@Body Map<String, Object> transactionData);

    @GET("transactions")
    Call<StandardResponse<List<Map<String, Object>>>> getUserTransactions();

    @PUT("transactions/{transactionId}/status")
    Call<StandardResponse<Map<String, Object>>> updateTransactionStatus(
        @Path("transactionId") Long transactionId,
        @Body Map<String, Object> statusData
    );
}
