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

    @GET("products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Query("radius") Integer radius,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir
    );

    @GET("products/{id}")
    Call<StandardResponse<Product>> getProductById(@Path("id") Long id);

    @GET("products/search")
    Call<StandardResponse<Map<String, Object>>> searchProducts(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Query("radius") Integer radius,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir
    );

    @POST("products")
    Call<StandardResponse<Product>> createProduct(
            @Body Map<String, Object> productRequest,
            @Header("User-ID") Long userId
    );

    @PUT("products/{id}")
    Call<StandardResponse<Product>> updateProduct(
            @Path("id") Long id,
            @Body Map<String, Object> productRequest,
            @Header("User-ID") Long userId
    );

    @DELETE("products/{id}")
    Call<StandardResponse<Void>> deleteProduct(
            @Path("id") Long id,
            @Header("User-ID") Long userId
    );

    @GET("products/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getUserProducts(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size,
            @Query("status") String status
    );

    @PUT("products/{id}/status")
    Call<StandardResponse<Product>> updateProductStatus(
            @Path("id") Long id,
            @Body Map<String, String> statusRequest,
            @Header("User-ID") Long userId
    );

    @POST("products/{id}/view")
    Call<StandardResponse<Void>> incrementViewCount(@Path("id") Long id);

    @GET("categories")
    Call<StandardResponse<List<Category>>> getCategories();

    @GET("categories/{id}")
    Call<StandardResponse<Category>> getCategoryById(@Path("id") Long id);

    @Multipart
    @POST("files/upload/product")
    Call<StandardResponse<Map<String, String>>> uploadProductImage(
            @Part MultipartBody.Part file
    );
}