// app/src/main/java/com/example/newtrade/models/ProductResponse.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ProductResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("condition")
    private String condition;

    @SerializedName("location")
    private String location;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("status")
    private String status;

    @SerializedName("viewCount")
    private Integer viewCount;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    @SerializedName("seller")
    private User seller;

    @SerializedName("category")
    private Category category;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Constructors
    public ProductResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getViewCount() { return viewCount != null ? viewCount : 0; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Utility methods
    public String getFirstImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String firstImage = imageUrls.get(0);
            return firstImage.startsWith("http") ? firstImage :
                    com.example.newtrade.utils.Constants.PRODUCT_IMAGES_URL + firstImage;
        }
        return null;
    }

    public String getFormattedPrice() {
        return price != null ? String.format("%,.0f VNĐ", price.doubleValue()) : "N/A";
    }

    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    public boolean isSold() {
        return "SOLD".equals(status);
    }

    public String getSellerName() {
        return seller != null ? seller.getDisplayName() : "Unknown";
    }

    public String getCategoryName() {
        return category != null ? category.getName() : "Uncategorized";
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    @Override
    public String toString() {
        return "ProductResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                ", seller=" + (seller != null ? seller.getDisplayName() : "null") +
                ", category=" + (category != null ? category.getName() : "null") +
                '}';
    }
}