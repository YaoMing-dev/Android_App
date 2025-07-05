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
    Call<StandardResponse<Map<String, Object>>> getProductById(@Path("productId") Long productId);

    // ✅ THÊM getProduct method để tương thích
    @GET("/api/products/{productId}")
    Call<StandardResponse<Map<String, Object>>> getProduct(@Path("productId") Long productId);

    // ✅ THÊM getCategories method
    @GET("/api/categories")
    Call<StandardResponse<List<Map<String, Object>>>> getCategories();

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
    Call<StandardResponse<List<Map<String, Object>>>> getProductsByUser(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/products/my")
    Call<StandardResponse<Map<String, Object>>> getMyProducts(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/products/{productId}/offers")
    Call<StandardResponse<Map<String, Object>>> getProductOffers(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @POST("/api/products/{productId}/view")
    Call<StandardResponse<Map<String, Object>>> recordProductView(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @POST("/api/products/{productId}/save")
    Call<StandardResponse<Map<String, Object>>> saveProduct(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @DELETE("/api/products/{productId}/save")
    Call<StandardResponse<Map<String, Object>>> unsaveProduct(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @Multipart
    @POST("/api/products/{productId}/images")
    Call<StandardResponse<Map<String, Object>>> uploadProductImages(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId,
            @Part List<MultipartBody.Part> images);

    @DELETE("/api/products/{productId}/images/{imageId}")
    Call<StandardResponse<Map<String, Object>>> deleteProductImage(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId,
            @Path("imageId") Long imageId);
}