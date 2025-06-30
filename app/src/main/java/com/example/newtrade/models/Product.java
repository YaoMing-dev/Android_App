// app/src/main/java/com/example/newtrade/models/Product.java
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

    // Enums matching backend
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
                if (condition.name().equalsIgnoreCase(text)) {
                    return condition;
                }
            }
            return null;
        }
    }

    public enum ProductStatus {
        AVAILABLE("Available"),
        SOLD("Sold"),
        PAUSED("Paused"),
        DELETED("Deleted");

        private final String displayName;

        ProductStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ProductStatus fromString(String text) {
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

    public boolean isAvailable() {
        return ProductStatus.AVAILABLE.equals(status);
    }

    public boolean isSold() {
        return ProductStatus.SOLD.equals(status);
    }

    public String getSellerName() {
        return seller != null ? seller.getDisplayOrFullName() : "Unknown Seller";
    }

    public String getCategoryName() {
        return category != null ? category.getName() : "Uncategorized";
    }

    public String getViewCountText() {
        if (viewCount == null || viewCount == 0) {
            return "No views";
        } else if (viewCount == 1) {
            return "1 view";
        } else {
            return viewCount + " views";
        }
    }

    public String getSellerContactInfo() {
        return seller != null ? seller.getContactInfo() : null;
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
}