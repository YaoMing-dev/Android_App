// app/src/main/java/com/example/newtrade/models/Category.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Category {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("iconUrl")
    private String iconUrl;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("productCount")
    private Integer productCount;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Constructors
    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Boolean getIsActive() {
        return isActive != null ? isActive : true;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getProductCount() {
        return productCount != null ? productCount : 0;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    public String getIconImageUrl() {
        if (iconUrl != null && !iconUrl.isEmpty()) {
            if (iconUrl.startsWith("http")) {
                return iconUrl;
            } else {
                return com.example.newtrade.utils.Constants.UPLOAD_URL + iconUrl;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", productCount=" + productCount +
                ", isActive=" + isActive +
                '}';
    }
}