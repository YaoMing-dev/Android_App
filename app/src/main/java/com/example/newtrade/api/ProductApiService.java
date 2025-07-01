// app/src/main/java/com/example/newtrade/api/ProductApiService.java
package com.example.newtrade.api;

import com.example.newtrade.models.ProductRequest;
import com.example.newtrade.models.ProductResponse;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.PagedResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductApiService {

    /**
     * Create new product
     */
    @POST("products")
    Call<StandardResponse<ProductResponse>> createProduct(@Body ProductRequest request);

    /**
     * Update product
     */
    @PUT("products/{id}")
    Call<StandardResponse<ProductResponse>> updateProduct(
            @Path("id") Long id,
            @Body ProductRequest request
    );

    /**
     * Get product by ID
     */
    @GET("products/{id}")
    Call<StandardResponse<ProductResponse>> getProductById(@Path("id") Long id);

    /**
     * Get all products with filters
     */
    @GET("products")
    Call<StandardResponse<PagedResponse<ProductResponse>>> getAllProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") BigDecimal minPrice,
            @Query("maxPrice") BigDecimal maxPrice
    );

    /**
     * Search products
     */
    @GET("products/search")
    Call<StandardResponse<PagedResponse<ProductResponse>>> searchProducts(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") BigDecimal minPrice,
            @Query("maxPrice") BigDecimal maxPrice,
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Query("radius") Float radius
    );

    /**
     * Get my products
     */
    @GET("products/my-products")
    Call<StandardResponse<PagedResponse<ProductResponse>>> getMyProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("status") String status
    );

    /**
     * Get products by user ID
     */
    @GET("products/user/{userId}")
    Call<StandardResponse<PagedResponse<ProductResponse>>> getUserProducts(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    /**
     * Get products by category
     */
    @GET("products/category/{categoryId}")
    Call<StandardResponse<PagedResponse<ProductResponse>>> getProductsByCategory(
            @Path("categoryId") Long categoryId,
            @Query("page") int page,
            @Query("size") int size
    );

    /**
     * Delete product
     */
    @DELETE("products/{id}")
    Call<StandardResponse<Void>> deleteProduct(@Path("id") Long id);

    /**
     * Mark product as sold
     */
    @PUT("products/{id}/mark-sold")
    Call<StandardResponse<ProductResponse>> markAsSold(@Path("id") Long id);

    /**
     * Archive product
     */
    @PUT("products/{id}/archive")
    Call<StandardResponse<Void>> archiveProduct(@Path("id") Long id);

    /**
     * Increment view count
     */
    @POST("products/{id}/view")
    Call<StandardResponse<Void>> incrementViewCount(@Path("id") Long id);

    /**
     * Get product analytics
     */
    @GET("products/{id}/analytics")
    Call<StandardResponse<Map<String, Object>>> getProductAnalytics(@Path("id") Long id);

    /**
     * Get recommended products
     */
    @GET("products/recommendations")
    Call<StandardResponse<List<ProductResponse>>> getRecommendations(
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Query("limit") int limit
    );

    /**
     * Get featured products
     */
    @GET("products/featured")
    Call<StandardResponse<List<ProductResponse>>> getFeaturedProducts(@Query("limit") int limit);

    /**
     * Get recent products
     */
    @GET("products/recent")
    Call<StandardResponse<List<ProductResponse>>> getRecentProducts(@Query("limit") int limit);
}