// app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Product {
    private Long id;
    private String title;
    private String description;
    private Double price; // Keep as Double for Android compatibility
    private String imageUrl;
    private String location;
    private String condition; // String version for compatibility
    private String status;    // String version for compatibility
    private String createdAt;
    private String updatedAt;
    private Long userId;
    private Long categoryId;

    // Additional fields
    private List<String> imageUrls;
    private String categoryName;
    private Integer viewCount = 0;
    private String primaryImageUrl;

    // ===== ENUMS =====
    public enum ProductCondition {
        NEW("New"),
        LIKE_NEW("Like New"),
        GOOD("Good"),
        FAIR("Fair"),
        POOR("Poor");

        private final String displayName;
        ProductCondition(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override
        public String toString() { return displayName; }

        // ✅ FIX: Add conversion from String
        public static ProductCondition fromString(String value) {
            if (value == null) return null;
            for (ProductCondition condition : values()) {
                if (condition.displayName.equalsIgnoreCase(value) ||
                        condition.name().equalsIgnoreCase(value)) {
                    return condition;
                }
            }
            return NEW; // default
        }
    }

    public enum ProductStatus {
        AVAILABLE("Available"),
        SOLD("Sold"),
        RESERVED("Reserved"),
        DELETED("Deleted"),
        ARCHIVED("Archived");

        private final String displayName;
        ProductStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override
        public String toString() { return displayName; }

        // ✅ FIX: Add conversion from String
        public static ProductStatus fromString(String value) {
            if (value == null) return null;
            for (ProductStatus status : values()) {
                if (status.displayName.equalsIgnoreCase(value) ||
                        status.name().equalsIgnoreCase(value)) {
                    return status;
                }
            }
            return AVAILABLE; // default
        }
    }

    // ===== CONSTRUCTORS =====
    public Product() {
        this.imageUrls = new ArrayList<>();
    }

    public Product(Long id, String title, String description, Double price) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
    }

    // ✅ FIX: Add fromMap method for SearchFragment
    public static Product fromMap(Map<String, Object> productData) {
        Product product = new Product();

        try {
            if (productData.get("id") instanceof Number) {
                product.setId(((Number) productData.get("id")).longValue());
            }

            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));

            // ✅ FIX: Handle BigDecimal to Double conversion
            if (productData.get("price") instanceof Number) {
                product.setPrice(((Number) productData.get("price")).doubleValue());
            }

            product.setLocation((String) productData.get("location"));
            product.setImageUrl((String) productData.get("primaryImageUrl"));
            product.setPrimaryImageUrl((String) productData.get("primaryImageUrl"));

            // ✅ FIX: Handle condition conversion
            Object conditionObj = productData.get("condition");
            if (conditionObj != null) {
                product.setCondition(conditionObj.toString());
            }

            // ✅ FIX: Handle status conversion
            Object statusObj = productData.get("status");
            if (statusObj != null) {
                product.setStatus(statusObj.toString());
            }

            product.setCreatedAt((String) productData.get("createdAt"));
            product.setUpdatedAt((String) productData.get("updatedAt"));

            if (productData.get("userId") instanceof Number) {
                product.setUserId(((Number) productData.get("userId")).longValue());
            }

            if (productData.get("categoryId") instanceof Number) {
                product.setCategoryId(((Number) productData.get("categoryId")).longValue());
            }

            product.setCategoryName((String) productData.get("categoryName"));

            if (productData.get("viewCount") instanceof Number) {
                product.setViewCount(((Number) productData.get("viewCount")).intValue());
            }

        } catch (Exception e) {
            android.util.Log.e("Product", "Error parsing product from map", e);
        }

        return product;
    }

    // ===== HELPER METHODS =====

    public String getFormattedPrice() {
        if (price == null) return "0 VND";
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " VND";
    }

    // ✅ FIX: Add enum getters that return proper enum types
    public ProductCondition getConditionEnum() {
        return ProductCondition.fromString(this.condition);
    }

    public ProductStatus getStatusEnum() {
        return ProductStatus.fromString(this.status);
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    // ✅ FIX: Add method to set price from BigDecimal (for backend compatibility)
    public void setPriceFromBigDecimal(BigDecimal price) {
        this.price = price != null ? price.doubleValue() : null;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public void setPrimaryImageUrl(String primaryImageUrl) { this.primaryImageUrl = primaryImageUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
}