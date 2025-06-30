// app/src/main/java/com/example/newtrade/models/Category.java
package com.example.newtrade.models;

public class Category {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String colorHex;
    private Integer displayOrder;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;

    // Product count
    private Integer productCount;

    // Constructors
    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.displayOrder = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }

    // Helper methods
    public String getFormattedProductCount() {
        if (productCount == null || productCount == 0) return "No items";
        return productCount + (productCount == 1 ? " item" : " items");
    }

    @Override
    public String toString() {
        return name;
    }
}