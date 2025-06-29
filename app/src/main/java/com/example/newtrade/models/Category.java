package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("icon")
    private String icon;

    @SerializedName("parent_id")
    private Long parentId;

    @SerializedName("is_active")
    private Boolean isActive;

    @SerializedName("sort_order")
    private Integer sortOrder;

    @SerializedName("product_count")
    private Integer productCount;

    // Constructors
    public Category() {}

    public Category(Long id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.isActive = true;
    }

    public Category(String name, String description, String icon) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }

    // Helper methods
    public String getDisplayName() {
        return (icon != null ? icon + " " : "") + name;
    }

    public boolean isParentCategory() {
        return parentId == null;
    }

    @Override
    public String toString() {
        return name;
    }
}
