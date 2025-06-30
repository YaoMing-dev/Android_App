// app/src/main/java/com/example/newtrade/models/Category.java
package com.example.newtrade.models;

public class Category {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String color;
    private Boolean isActive;
    private Integer productCount;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public Category() {}

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Helper methods
    public String getDisplayName() {
        return name != null ? name : "Unknown Category";
    }

    public boolean hasIcon() {
        return iconUrl != null && !iconUrl.trim().isEmpty();
    }

    public String getProductCountText() {
        if (productCount == null || productCount == 0) {
            return "No items";
        } else if (productCount == 1) {
            return "1 item";
        } else {
            return productCount + " items";
        }
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

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                ", productCount=" + productCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id != null && id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}