// app/src/main/java/com/example/newtrade/models/ProductRequest.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class ProductRequest {

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("categoryId")
    private Long categoryId;

    @SerializedName("condition")
    private String condition;

    @SerializedName("location")
    private String location;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    // Constructors
    public ProductRequest() {}

    public ProductRequest(String title, String description, BigDecimal price,
                          Long categoryId, String condition, String location) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.condition = condition;
        this.location = location;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    @Override
    public String toString() {
        return "ProductRequest{" +
                "title='" + title + '\'' +
                ", price=" + price +
                ", categoryId=" + categoryId +
                ", condition='" + condition + '\'' +
                ", location='" + location + '\'' +
                ", imageCount=" + (imageUrls != null ? imageUrls.size() : 0) +
                '}';
    }
}