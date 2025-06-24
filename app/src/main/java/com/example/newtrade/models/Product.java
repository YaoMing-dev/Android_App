// app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Product {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String condition;
    private String location;
    private List<String> imageUrls;
    private Long categoryId;
    private String categoryName;
    private Long userId;
    private String userName;
    private String status;
    private Long viewCount;

    // Constructor from Map
    public static Product fromMap(Map<String, Object> map) {
        Product product = new Product();
        product.setId(((Double) map.get("id")).longValue());
        product.setTitle((String) map.get("title"));
        product.setDescription((String) map.get("description"));
        product.setPrice(new BigDecimal(String.valueOf(map.get("price"))));
        product.setCondition((String) map.get("condition"));
        product.setLocation((String) map.get("location"));
        product.setImageUrls((List<String>) map.get("imageUrls"));
        product.setCategoryId(((Double) map.get("categoryId")).longValue());
        product.setCategoryName((String) map.get("categoryName"));
        product.setStatus((String) map.get("status"));
        return product;
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

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
}