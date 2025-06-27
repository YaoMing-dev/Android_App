// app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class Product {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String category;
    private String condition;
    private String location;
    private String tags;
    private boolean negotiable;
    private Long sellerId;
    private String sellerName;
    private String sellerProfilePicture;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;

    // ===== CONSTRUCTORS =====

    public Product() {}

    public Product(Long id, String title, String description, BigDecimal price, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // ✅ FIX: Add fromMap method
    public static Product fromMap(Map<String, Object> map) {
        Product product = new Product();

        product.id = getLongFromMap(map, "id");
        product.title = getStringFromMap(map, "title");
        product.description = getStringFromMap(map, "description");

        // Handle price conversion
        Object priceObj = map.get("price");
        if (priceObj instanceof Number) {
            product.price = new BigDecimal(priceObj.toString());
        } else if (priceObj instanceof String) {
            try {
                product.price = new BigDecimal((String) priceObj);
            } catch (NumberFormatException e) {
                product.price = BigDecimal.ZERO;
            }
        } else {
            product.price = BigDecimal.ZERO;
        }

        product.imageUrl = getStringFromMap(map, "imageUrl");
        product.category = getStringFromMap(map, "category");
        product.condition = getStringFromMap(map, "condition");
        product.location = getStringFromMap(map, "location");
        product.tags = getStringFromMap(map, "tags");
        product.negotiable = getBooleanFromMap(map, "negotiable");
        product.sellerId = getLongFromMap(map, "sellerId");
        product.sellerName = getStringFromMap(map, "sellerName");
        product.sellerProfilePicture = getStringFromMap(map, "sellerProfilePicture");
        product.isActive = getBooleanFromMap(map, "isActive");

        // Handle dates
        product.createdAt = getDateFromMap(map, "createdAt");
        product.updatedAt = getDateFromMap(map, "updatedAt");

        return product;
    }

    // ===== HELPER METHODS FOR MAP CONVERSION =====

    private static String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private static Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static boolean getBooleanFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }

    private static Date getDateFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Date) {
            return (Date) value;
        } else if (value instanceof Long) {
            return new Date((Long) value);
        } else if (value instanceof String) {
            try {
                return new Date(Long.parseLong((String) value));
            } catch (NumberFormatException e) {
                return new Date();
            }
        }
        return new Date();
    }

    // ===== GETTERS AND SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean isNegotiable() { return negotiable; }
    public void setNegotiable(boolean negotiable) { this.negotiable = negotiable; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerProfilePicture() { return sellerProfilePicture; }
    public void setSellerProfilePicture(String sellerProfilePicture) { this.sellerProfilePicture = sellerProfilePicture; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}