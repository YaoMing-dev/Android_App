// app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

public class Product {
    private Long id;
    private String title;
    private String description;
    private Double price; // ✅ KEEP as Double for compatibility
    private String imageUrl;
    private String location;
    private String condition;
    private String status;
    private String createdAt;
    private String updatedAt;
    private Long userId;
    private Long categoryId;

    // ===== CONSTRUCTORS =====

    public Product() {}

    public Product(Long id, String title, String description, Double price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; } // ✅ FIX: Double not BigDecimal

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; } // ✅ FIX: String not enum

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; } // ✅ FIX: String not enum

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}