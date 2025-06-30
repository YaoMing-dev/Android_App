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
    private ProductStatus status;
    private String location;
    private String contactInfo;
    private Integer viewCount;
    private Category category;
    private User seller;
    private List<String> imageUrls;
    private String createdAt;
    private String updatedAt;
    private Double latitude;
    private Double longitude;
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean isLiked;
    private Boolean isSaved;

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
            for (ProductCondition condition : ProductCondition.values()) {
                if (condition.name().equalsIgnoreCase(text) ||
                        condition.displayName.equalsIgnoreCase(text)) {
                    return condition;
                }
            }
            return NEW;
        }
    }

    public enum ProductStatus {
        AVAILABLE("Available"),
        SOLD("Sold"),
        ARCHIVED("Archived"),
        SUSPENDED("Suspended");

        private final String displayName;

        ProductStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ProductStatus fromString(String text) {
            for (ProductStatus status : ProductStatus.values()) {
                if (status.name().equalsIgnoreCase(text) ||
                        status.displayName.equalsIgnoreCase(text)) {
                    return status;
                }
            }
            return AVAILABLE;
        }
    }

    public Product() {}

    public String getFormattedPrice() {
        if (price == null) return "Price not set";
        return "₫" + String.format("%,.0f", price);
    }

    public String getMainImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }

    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    public int getImageCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    public String getConditionText() {
        return condition != null ? condition.getDisplayName() : "Not specified";
    }

    public String getStatusText() {
        return status != null ? status.getDisplayName() : "Unknown";
    }

    public boolean isAvailable() {
        return status == ProductStatus.AVAILABLE;
    }

    public boolean isSold() {
        return status == ProductStatus.SOLD;
    }

    public String getSellerName() {
        return seller != null ? seller.getDisplayNameOrFullName() : "Unknown Seller";
    }

    public String getCategoryName() {
        return category != null ? category.getName() : "Uncategorized";
    }

    // Full getters and setters
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

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }

    public Integer getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }

    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }

    public Boolean getIsSaved() { return isSaved; }
    public void setIsSaved(Boolean isSaved) { this.isSaved = isSaved; }
}