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

    // ===== PRODUCT METHODS =====

    /**
     * ✅ FIX: Get products without parameters (fixes HomeFragment error)
     */
    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts();

    /**
     * ✅ FIX: Get products with pagination (fixes existing usage)
     */
    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size
    );

    /**
     * ✅ FIX: Get products with full filters (fixes AllProductsActivity error)
     */
    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search,
            @Query("categoryId") Long categoryId
    );

    @GET("api/products/{id}")
    Call<StandardResponse<Map<String, Object>>> getProductById(@Path("id") Long productId);

    @POST("api/products")
    Call<StandardResponse<Map<String, Object>>> createProduct(@Body Map<String, Object> productData);

    @PUT("api/products/{id}")
    Call<StandardResponse<Map<String, Object>>> updateProduct(
            @Path("id") Long productId,
            @Body Map<String, Object> productData
    );

    @DELETE("api/products/{id}")
    Call<StandardResponse<Void>> deleteProduct(@Path("id") Long productId);

    // ===== CATEGORY METHODS =====

    /**
     * ✅ FIX: Add getCategories method (fixes HomeFragment error "cannot find symbol method getCategories()")
     */
    @GET("api/categories")
    Call<StandardResponse<List<Map<String, Object>>>> getCategories();

    @GET("api/categories/{id}")
    Call<StandardResponse<Map<String, Object>>> getCategoryById(@Path("id") Long categoryId);

    // ===== SEARCH METHODS =====

    /**
     * ✅ FIX: Search products method (fixes CategoryProductsActivity error "cannot find symbol method searchProducts")
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

    // ===== FILE UPLOAD =====

    /**
     * Upload avatar image
     */
    @Multipart
    @POST("api/files/upload/avatar")
    Call<StandardResponse<Map<String, String>>> uploadAvatar(@Part MultipartBody.Part image);

    /**
     * Upload product image
     */
    @Multipart
    @POST("api/files/upload/product")
    Call<StandardResponse<Map<String, String>>> uploadProductImage(@Part MultipartBody.Part image);

    // ===== HEALTH CHECK =====

    /**
     * Health check endpoint
     */
    @GET("api/health")
    Call<StandardResponse<String>> healthCheck();
}