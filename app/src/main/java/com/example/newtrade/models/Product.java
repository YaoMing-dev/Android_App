// app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Product {
    private Long id;
    private String title;
    private String description;
    private Double price; // ✅ KEEP as Double for compatibility
    private String imageUrl;
    private String location;
    private String condition;
    private String status;
    private String createdAt;
    private String updatedAt;
    private Long userId;
    private Long categoryId;

    // ✅ FIX: Add missing fields causing "cannot find symbol" errors
    private List<String> imageUrls;
    private String categoryName;
    private Integer viewCount = 0;

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

    // ===== EXISTING GETTERS & SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

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

    // ✅ FIX: Add NEW methods that were causing "cannot find symbol" errors

    /**
     * getFormattedPrice() - was causing "cannot find symbol method getFormattedPrice()" error
     */
    public String getFormattedPrice() {
        if (price == null || price <= 0) {
            return "Free";
        }

        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(price).replace("₫", "đ");
        } catch (Exception e) {
            return String.format("%,.0f đ", price);
        }
    }

    /**
     * getPrimaryImageUrl() - was causing "cannot find symbol method getPrimaryImageUrl()" error
     */
    public String getPrimaryImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return imageUrl; // fallback to single imageUrl
    }

    /**
     * setImageUrls() - was causing "cannot find symbol method setImageUrls(List<String>)" error
     */
    public List<String> getImageUrls() {
        return imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    /**
     * setCategoryName() - was causing "cannot find symbol method setCategoryName(String)" error
     */
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    /**
     * setViewCount() - was causing "cannot find symbol method setViewCount(int)" error
     */
    public Integer getViewCount() { return viewCount != null ? viewCount : 0; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    // ✅ FIX: Add setPrimaryImageUrl() - was causing error in CategoryProductsActivity
    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.imageUrl = primaryImageUrl; // set to main imageUrl field
    }

    // ===== HELPER METHODS =====

    public boolean hasImages() {
        return (imageUrls != null && !imageUrls.isEmpty()) || (imageUrl != null && !imageUrl.isEmpty());
    }

    public void addImageUrl(String imageUrl) {
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        imageUrls.add(imageUrl);
    }
}