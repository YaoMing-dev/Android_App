// app/src/main/java/com/example/newtrade/api/ProductService.java
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

public interface ProductService {

    // ===== PRODUCT CRUD OPERATIONS =====

    /**
     * Get all products with pagination and filtering
     */
    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search,
            @Query("category") String category,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice
    );

    /**
     * Get all products with default parameters
     */
    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts();

    /**
     * Get product by ID
     */
    @GET("api/products/{id}")
    Call<StandardResponse<Map<String, Object>>> getProductById(@Path("id") Long productId);

    /**
     * Create new product
     */
    @POST("api/products")
    Call<StandardResponse<Map<String, Object>>> createProduct(@Body Map<String, Object> productData);

    /**
     * Update existing product
     */
    @PUT("api/products/{id}")
    Call<StandardResponse<Map<String, Object>>> updateProduct(
            @Path("id") Long productId,
            @Body Map<String, Object> productData
    );

    /**
     * Delete product
     */
    @DELETE("api/products/{id}")
    Call<StandardResponse<Void>> deleteProduct(@Path("id") Long productId);

    /**
     * Mark product as sold
     */
    @PUT("api/products/{id}/mark-sold")
    Call<StandardResponse<Void>> markProductAsSold(@Path("id") Long productId);

    /**
     * Archive product
     */
    @PUT("api/products/{id}/archive")
    Call<StandardResponse<Void>> archiveProduct(@Path("id") Long productId);

    // ===== USER SPECIFIC OPERATIONS =====

    /**
     * Get products by user ID
     */
    @GET("api/products/user/{userId}")
    Call<StandardResponse<List<Map<String, Object>>>> getProductsByUser(@Path("userId") Long userId);

    /**
     * Get current user's products
     */
    @GET("api/products/my-products")
    Call<StandardResponse<List<Map<String, Object>>>> getMyProducts(
            @Query("page") int page,
            @Query("size") int size
    );

    // ===== SEARCH OPERATIONS =====

    /**
     * Search products with filters
     */
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

    /**
     * Simple search products
     */
    @GET("api/products/search")
    Call<StandardResponse<Map<String, Object>>> searchProducts(@Query("query") String query);

    // ===== FEATURED & SPECIAL PRODUCTS =====

    /**
     * Get featured products
     */
    @GET("api/products/featured")
    Call<StandardResponse<List<Map<String, Object>>>> getFeaturedProducts();

    /**
     * Get recent products
     */
    @GET("api/products/recent")
    Call<StandardResponse<List<Map<String, Object>>>> getRecentProducts(@Query("limit") int limit);

    /**
     * Get nearby products
     */
    @GET("api/products/nearby")
    Call<StandardResponse<List<Map<String, Object>>>> getNearbyProducts(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radius") int radius
    );

    /**
     * Get products by category
     */
    @GET("api/products/category/{categoryId}")
    Call<StandardResponse<List<Map<String, Object>>>> getProductsByCategory(@Path("categoryId") Long categoryId);

    // ===== STATISTICS =====

    /**
     * Get product statistics
     */
    @GET("api/products/stats")
    Call<StandardResponse<Map<String, Object>>> getProductStats();

    /**
     * Get user product statistics
     */
    @GET("api/products/user/{userId}/stats")
    Call<StandardResponse<Map<String, Object>>> getUserProductStats(@Path("userId") Long userId);

    // ===== FILE UPLOAD =====

    /**
     * Upload product image
     */
    @Multipart
    @POST("api/files/upload/product")
    Call<StandardResponse<Map<String, String>>> uploadProductImage(
            @Part MultipartBody.Part image
    );

    /**
     * Upload multiple product images
     */
    @Multipart
    @POST("api/files/upload/product/multiple")
    Call<StandardResponse<Map<String, Object>>> uploadProductImages(
            @Part List<MultipartBody.Part> images
    );

    // ===== PRODUCT INTERACTION =====

    /**
     * Increment product view count
     */
    @POST("api/products/{id}/view")
    Call<StandardResponse<Void>> incrementViewCount(@Path("id") Long productId);

    /**
     * Add product to favorites
     */
    @POST("api/products/{id}/favorite")
    Call<StandardResponse<Void>> addToFavorites(@Path("id") Long productId);

    /**
     * Remove product from favorites
     */
    @DELETE("api/products/{id}/favorite")
    Call<StandardResponse<Void>> removeFromFavorites(@Path("id") Long productId);

    /**
     * Get user's favorite products
     */
    @GET("api/products/favorites")
    Call<StandardResponse<List<Map<String, Object>>>> getFavoriteProducts();

    // ===== PRODUCT OFFERS =====

    /**
     * Make offer on product
     */
    @POST("api/products/{id}/offers")
    Call<StandardResponse<Map<String, Object>>> makeOffer(
            @Path("id") Long productId,
            @Body Map<String, Object> offerData
    );

    /**
     * Get offers for product
     */
    @GET("api/products/{id}/offers")
    Call<StandardResponse<List<Map<String, Object>>>> getProductOffers(@Path("id") Long productId);

    /**
     * Accept/reject offer
     */
    @PUT("api/products/offers/{offerId}")
    Call<StandardResponse<Map<String, Object>>> respondToOffer(
            @Path("offerId") Long offerId,
            @Body Map<String, Object> response
    );
}