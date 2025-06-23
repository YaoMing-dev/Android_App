// app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import com.example.newtrade.utils.Constants;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Product {

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

    @SerializedName("status")
    private String status;

    @SerializedName("location")
    private String location;

    @SerializedName("contactInfo")
    private String contactInfo;

    @SerializedName("viewCount")
    private Integer viewCount;

    @SerializedName("category")
    private Category category;

    @SerializedName("seller")
    private User seller;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Product Conditions
    public static final String CONDITION_NEW = "NEW";
    public static final String CONDITION_LIKE_NEW = "LIKE_NEW";
    public static final String CONDITION_GOOD = "GOOD";
    public static final String CONDITION_FAIR = "FAIR";
    public static final String CONDITION_POOR = "POOR";

    // Product Status
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_SOLD = "SOLD";
    public static final String STATUS_RESERVED = "RESERVED";
    public static final String STATUS_DELETED = "DELETED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    // Constructors
    public Product() {}

    public Product(String title, String description, BigDecimal price, String condition, String location) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.condition = condition;
        this.location = location;
        this.status = STATUS_AVAILABLE;
        this.viewCount = 0;
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

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public Integer getViewCount() {
        return viewCount != null ? viewCount : 0;
    }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public String getFormattedPrice() {
        if (price == null) return "Miễn phí";
        return Constants.formatPrice(price.doubleValue());
    }

    public String getConditionDisplay() {
        return Constants.getProductConditionDisplay(condition);
    }

    public String getStatusDisplay() {
        return Constants.getProductStatusDisplay(status);
    }

    public String getMainImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return Constants.getImageUrl(imageUrls.get(0));
        }
        return null;
    }

    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    public int getImageCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    public boolean isAvailable() {
        return STATUS_AVAILABLE.equals(status);
    }

    public boolean isSold() {
        return STATUS_SOLD.equals(status);
    }

    public boolean isReserved() {
        return STATUS_RESERVED.equals(status);
    }

    public double getPriceAsDouble() {
        return price != null ? price.doubleValue() : 0.0;
    }

    public String getCategoryName() {
        return category != null ? category.getName() : "Khác";
    }

    public String getSellerName() {
        return seller != null ? seller.getDisplayName() : "Người bán";
    }

    public String getTimeAgo() {
        if (createdAt != null) {
            return Constants.getTimeAgo(createdAt.getTime());
        }
        return "";
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", condition='" + condition + '\'' +
                ", status='" + status + '\'' +
                ", location='" + location + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id != null && id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}