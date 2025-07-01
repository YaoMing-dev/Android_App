// ✅ FIXED: app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Product {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private ProductCondition condition;
    private ProductStatus status;
    private String location;

    @SerializedName("viewCount")
    private Integer viewCount;

    private User seller;
    private Category category;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    @SerializedName("images")
    private List<String> images;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // GPS Location fields
    private Double latitude;
    private Double longitude;

    // Enums matching backend exactly
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

        public static ProductCondition fromString(String text) {
            if (text == null) return null;
            for (ProductCondition condition : ProductCondition.values()) {
                if (condition.name().equalsIgnoreCase(text)) {
                    return condition;
                }
            }
            return null;
        }
    }

    // ✅ FIXED: Match backend exactly - RESERVED and ARCHIVED added
    public enum ProductStatus {
        AVAILABLE("Available"),
        SOLD("Sold"),
        RESERVED("Reserved"),      // ✅ Added
        DELETED("Deleted"),
        ARCHIVED("Archived");      // ✅ Added

        private final String displayName;

        ProductStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ProductStatus fromString(String text) {
            if (text == null) return null;
            for (ProductStatus status : ProductStatus.values()) {
                if (status.name().equalsIgnoreCase(text)) {
                    return status;
                }
            }
            return null;
        }
    }

    // Constructors
    public Product() {}

    // Helper methods
    public String getFirstImage() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }

    public List<String> getAllImages() {
        return imageUrls != null ? imageUrls : images;
    }

    public String getPriceString() {
        return price != null ? "$" + price.toString() : "Price not set";
    }

    public String getConditionDisplay() {
        return condition != null ? condition.getDisplayName() : "Unknown";
    }

    public String getStatusDisplay() {
        return status != null ? status.getDisplayName() : "Unknown";
    }

    // Status helper methods
    public boolean isAvailable() {
        return status == ProductStatus.AVAILABLE;
    }

    public boolean isSold() {
        return status == ProductStatus.SOLD;
    }

    public boolean isReserved() {
        return status == ProductStatus.RESERVED;
    }
    public String getFormattedPrice() {
        if (price == null) return "Price not set";
        return String.format("$%.2f", price);
    }
    public String getFirstImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }

    // Alternative name (some adapters might use this)


    public boolean isArchived() {
        return status == ProductStatus.ARCHIVED;
    }

    public boolean isDeleted() {
        return status == ProductStatus.DELETED;
    }

    public boolean isActive() {
        return status == ProductStatus.AVAILABLE || status == ProductStatus.RESERVED;
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

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}