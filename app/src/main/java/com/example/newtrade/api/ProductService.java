// app/src/main/java/com/example/newtrade/api/ProductService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import java.util.Map;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductService {

    @GET("/api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("category") String category,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("condition") String condition,
            @Query("location") String location,
            @Query("radius") Double radius,
            @Query("sort") String sort);

    @GET("/api/products/search")
    Call<StandardResponse<Map<String, Object>>> searchProducts(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size,
            @Query("category") String category,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("condition") String condition,
            @Query("location") String location,
            @Query("radius") Double radius,
            @Query("sort") String sort);

    @GET("/api/products/{productId}")
    Call<StandardResponse<Map<String, Object>>> getProduct(@Path("productId") Long productId);

    @POST("/api/products")
    Call<StandardResponse<Map<String, Object>>> createProduct(
            @Header("User-ID") Long userId,
            @Body Map<String, Object> productRequest);

    @PUT("/api/products/{productId}")
    Call<StandardResponse<Map<String, Object>>> updateProduct(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId,
            @Body Map<String, Object> productUpdateRequest);

    @DELETE("/api/products/{productId}")
    Call<StandardResponse<Map<String, Object>>> deleteProduct(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @PUT("/api/products/{productId}/status")
    Call<StandardResponse<Map<String, Object>>> updateProductStatus(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId,
            @Query("status") String status);

    @GET("/api/products/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getUserProducts(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/products/my-listings")
    Call<StandardResponse<Map<String, Object>>> getMyListings(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/products/categories")
    Call<StandardResponse<Map<String, Object>>> getCategories();

    @GET("/api/products/featured")
    Call<StandardResponse<Map<String, Object>>> getFeaturedProducts(
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/products/nearby")
    Call<StandardResponse<Map<String, Object>>> getNearbyProducts(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radius") double radius,
            @Query("page") int page,
            @Query("size") int size);

    @POST("/api/products/{productId}/view")
    Call<StandardResponse<Map<String, Object>>> recordProductView(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @Multipart
    @POST("/api/files/upload/product")
    Call<StandardResponse<Map<String, Object>>> uploadProductImage(
            @Header("User-ID") Long userId,
            @Part MultipartBody.Part file);

    @GET("/api/products/{productId}/stats")
    Call<StandardResponse<Map<String, Object>>> getProductStats(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);
}