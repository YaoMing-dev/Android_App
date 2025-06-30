// app/src/main/java/com/example/newtrade/models/ProductImage.java
package com.example.newtrade.models;

public class ProductImage {
    private Long id;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isPrimary;
    private String createdAt;

    public ProductImage() {}

    public ProductImage(String imageUrl, Integer displayOrder, Boolean isPrimary) {
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.isPrimary = isPrimary;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}