// app/src/main/java/com/example/newtrade/models/Category.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import com.example.newtrade.utils.Constants;
import java.util.Date;

public class Category {

    private Long id;
    private String name;
    private String description;
    private String icon;
    @SerializedName("isActive")
    private Boolean isActive;
    @SerializedName("createdAt")
    private Date createdAt;
    @SerializedName("updatedAt")
    private Date updatedAt;

    // Constructors
    public Category() {}

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

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public String getIconUrl() {
        if (icon == null || icon.isEmpty()) {
            return null;
        }
        return Constants.getImageUrl(icon);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}