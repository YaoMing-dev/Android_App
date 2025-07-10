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
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductService {

    // ===== PRODUCT CRUD OPERATIONS =====

    /**
     * Get all products with pagination and filtering
     */
    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search,
            @Query("category") String category,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir,
            @Query("categoryId") Long categoryId,
            @Query("condition") String condition,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice
    );

    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts();

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

    @PUT("api/products/{id}/mark-sold")
    Call<StandardResponse<Void>> markProductAsSold(@Path("id") Long productId);

    @PUT("api/products/{id}/archive")
    Call<StandardResponse<Void>> archiveProduct(@Path("id") Long productId);

    // ===== USER SPECIFIC OPERATIONS =====



    // ===== SEARCH OPERATIONS =====

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

    @GET("api/products/search")
    Call<StandardResponse<Map<String, Object>>> searchProducts(@Query("query") String query);

    @GET("api/products/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getProductsByUser(@Path("userId") Long userId);

    // ✅ SỬA: Backend trả về paginated response
    @GET("api/products/my-products")
    Call<StandardResponse<Map<String, Object>>> getMyProducts(
            @Query("page") int page,
            @Query("size") int size
    );
    @GET("api/products/user/{userId}")
    Call<StandardResponse<Object>> getUserProducts(@Path("userId") Long userId);
    // ===== FEATURED & SPECIAL PRODUCTS =====

    @GET("api/products/featured")
    Call<StandardResponse<List<Map<String, Object>>>> getFeaturedProducts();

    @GET("api/products/recent")
    Call<StandardResponse<List<Map<String, Object>>>> getRecentProducts(@Query("limit") int limit);

    @GET("api/products/nearby")
    Call<StandardResponse<List<Map<String, Object>>>> getNearbyProducts(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radius") int radius
    );

    @GET("api/products/category/{categoryId}")
    Call<StandardResponse<List<Map<String, Object>>>> getProductsByCategory(@Path("categoryId") Long categoryId);

    // ===== STATISTICS =====

    @GET("api/products/stats")
    Call<StandardResponse<Map<String, Object>>> getProductStats();

    @GET("api/products/user/{userId}/stats")
    Call<StandardResponse<Map<String, Object>>> getUserProductStats(@Path("userId") Long userId);

    // ===== FILE UPLOAD =====

    @Multipart
    @POST("api/files/upload/product")
    Call<StandardResponse<Map<String, String>>> uploadProductImage(
            @Part MultipartBody.Part image
    );


    // ✅ GIỮ: Backup endpoint nếu cần


    // ===== PRODUCT INTERACTION =====

    @POST("api/products/{id}/view")
    Call<StandardResponse<Void>> incrementViewCount(@Path("id") Long productId);

    @POST("api/products/{id}/favorite")
    Call<StandardResponse<Void>> addToFavorites(@Path("id") Long productId);

    @DELETE("api/products/{id}/favorite")
    Call<StandardResponse<Void>> removeFromFavorites(@Path("id") Long productId);

    @GET("api/products/favorites")
    Call<StandardResponse<List<Map<String, Object>>>> getFavoriteProducts();

    // ✅ SỬA: Đúng endpoint backend cho saved items
    /**
     * Get user's saved items with pagination
     */
    @GET("api/saved-items")
    Call<StandardResponse<Map<String, Object>>> getSavedItems(
            @Query("page") int page,
            @Query("size") int size
    );

    /**
     * Save a product
     */
    @POST("api/saved-items/{productId}")
    Call<StandardResponse<Void>> saveProduct(@Path("productId") Long productId);



    /**
     * Unsave a product
     */
    @DELETE("api/saved-items/{productId}")
    Call<StandardResponse<Void>> unsaveProduct(@Path("productId") Long productId);

    /**
     * Check if product is saved
     */
    @GET("api/saved-items/{productId}/is-saved")  // ✅ Đúng backend endpoint
    Call<StandardResponse<Map<String, Object>>> isProductSaved(@Path("productId") Long productId);


    // ===== PRODUCT OFFERS =====

    @POST("api/products/{id}/offers")
    Call<StandardResponse<Map<String, Object>>> makeOffer(
            @Path("id") Long productId,
            @Body Map<String, Object> offerData
    );

    @GET("api/products/{id}/offers")
    Call<StandardResponse<List<Map<String, Object>>>> getProductOffers(@Path("id") Long productId);

    @PUT("api/products/offers/{offerId}")
    Call<StandardResponse<Map<String, Object>>> respondToOffer(
            @Path("offerId") Long offerId,
            @Body Map<String, Object> response
    );


}