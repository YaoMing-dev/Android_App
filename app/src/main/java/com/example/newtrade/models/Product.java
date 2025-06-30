// app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

import java.math.BigDecimal;
import java.util.List;

public class Product {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private ProductCondition condition;
    private String location;
    private Double latitude;
    private Double longitude;
    private Integer locationRadius;
    private ProductStatus status;
    private Integer viewCount;
    private String createdAt;
    private String updatedAt;

    // Relations
    private User seller;
    private Category category;
    private List<ProductImage> images;

    // Constructors
    public Product() {}

    // Enums
    public enum ProductCondition {
        NEW("New"),
        LIKE_NEW("Like New"),
        GOOD("Good"),
        FAIR("Fair"),
        POOR("Poor");

        private final String displayName;

        ProductCondition(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ProductStatus {
        AVAILABLE("Available"),
        SOLD("Sold"),
        RESERVED("Reserved"),
        PAUSED("Paused"),
        DELETED("Deleted"),
        ARCHIVED("Archived");

        private final String displayName;

        ProductStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public ProductCondition getCondition() { return condition; }
    public void setCondition(ProductCondition condition) { this.condition = condition; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getLocationRadius() { return locationRadius; }
    public void setLocationRadius(Integer locationRadius) { this.locationRadius = locationRadius; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }

    // Helper methods
    public String getFirstImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageUrl();
        }
        return null;
    }

    public String getFormattedPrice() {
        if (price == null) return "Free";
        return "₫" + String.format("%,.0f", price);
    }

    public boolean isAvailable() {
        return status == ProductStatus.AVAILABLE;
    }
}