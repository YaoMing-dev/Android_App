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

    // ===== PRODUCT CRUD OPERATIONS =====

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

    // ===== PRODUCT STATUS OPERATIONS =====

    @PUT("products/{id}/mark-sold")
    Call<StandardResponse<Void>> markProductAsSold(
            @Path("id") Long id,
            @Header("User-ID") Long userId
    );

    @PUT("products/{id}/archive")
    Call<StandardResponse<Void>> archiveProduct(
            @Path("id") Long id,
            @Header("User-ID") Long userId
    );

    @PUT("products/{id}/restore")
    Call<StandardResponse<Void>> restoreArchivedProduct(
            @Path("id") Long id,
            @Header("User-ID") Long userId
    );

    @GET("products/archived")
    Call<StandardResponse<Map<String, Object>>> getArchivedProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    @GET("products/status/{status}")
    Call<StandardResponse<Map<String, Object>>> getProductsByStatus(
            @Path("status") String status,
            @Query("page") int page,
            @Query("size") int size,
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
            @Query("status") String status,
            @Header("User-ID") Long userId
    );

    // ===== ANALYTICS & STATS =====

    @GET("products/{id}/analytics")
    Call<StandardResponse<Map<String, Object>>> getProductAnalytics(
            @Path("id") Long id,
            @Header("User-ID") Long userId
    );

    @GET("products/dashboard/stats")
    Call<StandardResponse<Map<String, Object>>> getUserDashboardStats(
            @Header("User-ID") Long userId
    );

    @GET("products/stats")
    Call<StandardResponse<Map<String, Object>>> getProductStats();

    // ===== CATEGORIES =====

    @GET("categories")
    Call<StandardResponse<List<Category>>> getCategories();

    @GET("categories/{id}")
    Call<StandardResponse<Category>> getCategoryById(@Path("id") Long id);

    @GET("categories/{id}/products")
    Call<StandardResponse<Map<String, Object>>> getCategoryProducts(
            @Path("id") Long id,
            @Query("page") int page,
            @Query("size") int size,
            @Query("condition") String condition,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir
    );

    // ===== FILE UPLOAD =====

    @Multipart
    @POST("files/upload/product")
    Call<StandardResponse<Map<String, String>>> uploadProductImage(
            @Part MultipartBody.Part file
    );

    @Multipart
    @POST("files/upload/avatar")
    Call<StandardResponse<Map<String, String>>> uploadAvatarImage(
            @Part MultipartBody.Part file
    );

    @GET("files/info")
    Call<StandardResponse<Map<String, Object>>> getUploadInfo();

    // ===== NEARBY PRODUCTS =====

    @GET("products/nearby")
    Call<StandardResponse<Map<String, Object>>> getNearbyProducts(
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Query("radius") Integer radius,
            @Query("page") int page,
            @Query("size") int size,
            @Query("categoryId") Long categoryId
    );

    // ===== FAVORITES/SAVED =====

    @POST("products/{id}/save")
    Call<StandardResponse<Void>> saveProduct(
            @Path("id") Long id,
            @Header("User-ID") Long userId
    );

    @DELETE("products/{id}/save")
    Call<StandardResponse<Void>> unsaveProduct(
            @Path("id") Long id,
            @Header("User-ID") Long userId
    );

    @GET("products/saved")
    Call<StandardResponse<Map<String, Object>>> getSavedProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    // ===== PRODUCT VIEWS =====

    @POST("products/{id}/view")
    Call<StandardResponse<Void>> incrementProductViews(
            @Path("id") Long id,
            @Header("User-ID") Long userId
    );
}