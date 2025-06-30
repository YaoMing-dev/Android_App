// app/src/main/java/com/example/newtrade/models/Category.java
package com.example.newtrade.models;

public class Category {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;

    public Category() {}

    public Category(Long id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}