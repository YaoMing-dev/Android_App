// app/src/main/java/com/example/newtrade/api/ProductService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.Category;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface ProductService {

    @GET("products")
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

    @PUT("products/{id}/status")
    Call<StandardResponse<Product>> updateProductStatus(
            @Path("id") Long id,
            @Body Map<String, String> statusData,
            @Header("User-ID") Long userId
    );

    @PUT("products/{id}/sold")
    Call<StandardResponse<Product>> markAsSold(
            @Path("id") Long id,
            @Header("User-ID") Long userId
    );

    @GET("products/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getUserProducts(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("categories")
    Call<StandardResponse<Map<String, Object>>> getCategories();

    @Multipart
    @POST("products/upload-image")
    Call<StandardResponse<Map<String, String>>> uploadProductImage(
            @Part MultipartBody.Part image,
            @Header("User-ID") Long userId
    );
}