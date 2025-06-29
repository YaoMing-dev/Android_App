// app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class Product implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private Double price;

    @SerializedName("category")
    private String category;

    @SerializedName("condition")
    private String condition;

    @SerializedName("location")
    private String location;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("seller_id")
    private Long sellerId;

    @SerializedName("seller_name")
    private String sellerName;

    @SerializedName("seller_avatar")
    private String sellerAvatar;

    @SerializedName("seller_rating")
    private Double sellerRating;

    @SerializedName("images")
    private List<String> images;

    @SerializedName("main_image")
    private String mainImage;

    @SerializedName("status")
    private String status; // available, sold, paused

    @SerializedName("views_count")
    private Integer viewsCount;

    @SerializedName("favorites_count")
    private Integer favoritesCount;

    @SerializedName("is_favorited")
    private Boolean isFavorited;

    @SerializedName("is_negotiable")
    private Boolean isNegotiable;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public Product() {}

    public Product(String title, String description, Double price, String category) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = "available";
        this.isNegotiable = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerAvatar() { return sellerAvatar; }
    public void setSellerAvatar(String sellerAvatar) { this.sellerAvatar = sellerAvatar; }

    public Double getSellerRating() { return sellerRating; }
    public void setSellerRating(Double sellerRating) { this.sellerRating = sellerRating; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getViewsCount() { return viewsCount; }
    public void setViewsCount(Integer viewsCount) { this.viewsCount = viewsCount; }

    public Integer getFavoritesCount() { return favoritesCount; }
    public void setFavoritesCount(Integer favoritesCount) { this.favoritesCount = favoritesCount; }

    public Boolean getIsFavorited() { return isFavorited; }
    public void setIsFavorited(Boolean isFavorited) { this.isFavorited = isFavorited; }

    public Boolean getIsNegotiable() { return isNegotiable; }
    public void setIsNegotiable(Boolean isNegotiable) { this.isNegotiable = isNegotiable; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isAvailable() {
        return "available".equals(status);
    }

    public boolean isSold() {
        return "sold".equals(status);
    }

    public boolean isPaused() {
        return "paused".equals(status);
    }

    public String getFormattedPrice() {
        return price != null ? "$" + String.format("%.2f", price) : "Price not set";
    }

    public String getDisplayImage() {
        if (mainImage != null && !mainImage.isEmpty()) {
            return mainImage;
        }
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }
}