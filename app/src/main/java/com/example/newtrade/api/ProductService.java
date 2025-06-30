// app/src/main/java/com/example/newtrade/api/ProductService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.Category;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface ProductService {

    // ===== CATEGORIES =====
    @GET("categories")
    Call<StandardResponse<List<Category>>> getCategories();

    @GET("categories/{id}")
    Call<StandardResponse<Category>> getCategoryById(@Path("id") Long id);

    // ===== PRODUCTS =====
    @GET("products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir
    );

    @GET("products/search")
    Call<StandardResponse<Map<String, Object>>> searchProducts(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice
    );

    @GET("products/{id}")
    Call<StandardResponse<Product>> getProductById(@Path("id") Long id);

    @POST("products")
    Call<StandardResponse<Product>> createProduct(
            @Body Map<String, Object> productData,
            @Header("User-ID") Long userId
    );

    @PUT("products/{id}")
    Call<StandardResponse<Product>> updateProduct(
            @Path("id") Long id,
            @Body Map<String, Object> productData,
            @Header("User-ID") Long userId
    );

    @DELETE("products/{id}")
    Call<StandardResponse<Void>> deleteProduct(
            @Path("id") Long id,
            @Header("User-ID") Long userId
    );

    // ===== USER PRODUCTS =====
    @GET("products/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getUserProducts(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("products/my-products")
    Call<StandardResponse<Map<String, Object>>> getMyProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    // ===== PRODUCT IMAGES =====
    @Multipart
    @POST("products/{id}/images")
    Call<StandardResponse<Map<String, Object>>> uploadProductImages(
            @Path("id") Long productId,
            @Part List<MultipartBody.Part> images,
            @Header("User-ID") Long userId
    );

    @DELETE("products/{productId}/images/{imageId}")
    Call<StandardResponse<Void>> deleteProductImage(
            @Path("productId") Long productId,
            @Path("imageId") Long imageId,
            @Header("User-ID") Long userId
    );
}