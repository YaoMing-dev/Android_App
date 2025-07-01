// app/src/main/java/com/example/newtrade/api/ProductApiService.java
package com.example.newtrade.api;

import com.example.newtrade.models.Product;
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
     * Get all products with filters
     */
    @GET("products")
    Call<StandardResponse<PagedResponse<Product>>> getAllProducts(
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
    Call<StandardResponse<PagedResponse<Product>>> searchProducts(
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
     * Get product by ID
     */
    @GET("products/{id}")
    Call<StandardResponse<Product>> getProductById(@Path("id") Long id);

    /**
     * Create new product
     */
    @POST("products")
    Call<StandardResponse<Product>> createProduct(@Body ProductCreateRequest request);

    /**
     * Update product
     */
    @PUT("products/{id}")
    Call<StandardResponse<Product>> updateProduct(
            @Path("id") Long id,
            @Body ProductUpdateRequest request
    );

    /**
     * Delete product
     */
    @DELETE("products/{id}")
    Call<StandardResponse<Void>> deleteProduct(@Path("id") Long id);

    /**
     * Get products by user ID
     */
    @GET("products/user/{userId}")
    Call<StandardResponse<PagedResponse<Product>>> getUserProducts(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    /**
     * Get my products
     */
    @GET("products/my-products")
    Call<StandardResponse<PagedResponse<Product>>> getMyProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("status") String status
    );

    /**
     * Get products by category
     */
    @GET("products/category/{categoryId}")
    Call<StandardResponse<PagedResponse<Product>>> getProductsByCategory(
            @Path("categoryId") Long categoryId,
            @Query("page") int page,
            @Query("size") int size
    );

    /**
     * Mark product as sold
     */
    @PUT("products/{id}/mark-sold")
    Call<StandardResponse<Product>> markAsSold(@Path("id") Long id);

    /**
     * Archive product
     */
    @PUT("products/{id}/archive")
    Call<StandardResponse<Void>> archiveProduct(@Path("id") Long id);

    /**
     * Get product analytics
     */
    @GET("products/{id}/analytics")
    Call<StandardResponse<Map<String, Object>>> getProductAnalytics(@Path("id") Long id);

    /**
     * Increment view count
     */
    @POST("products/{id}/view")
    Call<StandardResponse<Void>> incrementViewCount(@Path("id") Long id);

    /**
     * Get recommended products
     */
    @GET("products/recommendations")
    Call<StandardResponse<List<Product>>> getRecommendations(
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Query("limit") int limit
    );

    /**
     * Get featured products
     */
    @GET("products/featured")
    Call<StandardResponse<List<Product>>> getFeaturedProducts(@Query("limit") int limit);

    /**
     * Get recent products
     */
    @GET("products/recent")
    Call<StandardResponse<List<Product>>> getRecentProducts(@Query("limit") int limit);

    // =============================================
    // REQUEST CLASSES
    // =============================================

    class ProductCreateRequest {
        private String title;
        private String description;
        private BigDecimal price;
        private String conditionType;
        private String location;
        private Double latitude;
        private Double longitude;
        private Long categoryId;
        private List<String> imageUrls;

        // Constructor
        public ProductCreateRequest(String title, String description, BigDecimal price,
                                    String conditionType, String location, Long categoryId) {
            this.title = title;
            this.description = description;
            this.price = price;
            this.conditionType = conditionType;
            this.location = location;
            this.categoryId = categoryId;
        }

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public String getConditionType() { return conditionType; }
        public void setConditionType(String conditionType) { this.conditionType = conditionType; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    }

    class ProductUpdateRequest {
        private String title;
        private String description;
        private BigDecimal price;
        private String conditionType;
        private String location;
        private Double latitude;
        private Double longitude;
        private Long categoryId;
        private List<String> imageUrls;

        // Same structure as ProductCreateRequest
        // Getters and setters...
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public String getConditionType() { return conditionType; }
        public void setConditionType(String conditionType) { this.conditionType = conditionType; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    }
}