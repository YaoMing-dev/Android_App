// app/src/main/java/com/example/newtrade/api/ProductService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.Product;

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

public interface ProductService {

    // Get all products with filters
    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getAllProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice
    );

    // Search products
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

    // Get product by ID
    @GET("api/products/{id}")
    Call<StandardResponse<Product>> getProduct(@Path("id") Long productId);

    // Create new product
    @POST("api/products")
    Call<StandardResponse<Product>> createProduct(@Body Map<String, Object> request);

    // Update product
    @PUT("api/products/{id}")
    Call<StandardResponse<Product>> updateProduct(@Path("id") Long productId, @Body Map<String, Object> request);

    // Delete product
    @DELETE("api/products/{id}")
    Call<StandardResponse<String>> deleteProduct(@Path("id") Long productId);

    // Mark product as sold
    @PUT("api/products/{id}/mark-sold")
    Call<StandardResponse<String>> markProductAsSold(@Path("id") Long productId);

    // Get user's products
    @GET("api/products/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getUserProducts(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );
}