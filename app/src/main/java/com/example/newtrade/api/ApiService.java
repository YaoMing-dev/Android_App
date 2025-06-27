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

    // ===== SEARCH METHODS - ✅ ADD THESE =====

    /**
     * ✅ FIX: Advanced search products
     */
    @GET("api/products/search")
    Call<StandardResponse<Map<String, Object>>> searchProductsAdvanced(
            @Query("query") String query,
            @Query("categoryId") Long categoryId,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("condition") String condition,
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Query("radius") Integer radius,
            @Query("sortBy") String sortBy,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    /**
     * ✅ FIX: Get featured products
     */
    @GET("api/products/featured")
    Call<StandardResponse<List<Map<String, Object>>>> getFeaturedProducts();

    /**
     * ✅ FIX: Get recent products
     */
    @GET("api/products/recent")
    Call<StandardResponse<List<Map<String, Object>>>> getRecentProducts(@Query("limit") int limit);

    // ===== EXISTING METHODS =====

    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size
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

    // ===== FILE UPLOAD =====

    /**
     * ✅ FIX: Upload avatar image
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
}