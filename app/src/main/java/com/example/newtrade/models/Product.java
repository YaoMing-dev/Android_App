package com.example.newtrade.models;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Product {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String location;
    private ProductCondition condition;
    private ProductStatus status;
    private Long categoryId;
    private String categoryName;
    private String userDisplayName;
    private Integer viewCount;
    private List<String> imageUrls;
    private String createdAt;

    // Enums
    public enum ProductCondition {
        NEW("Mới"),
        LIKE_NEW("Như mới"),
        GOOD("Tốt"),
        FAIR("Khá"),
        POOR("Cũ");

        private final String displayName;

        ProductCondition(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ProductStatus {
        AVAILABLE("Có sẵn"),
        SOLD("Đã bán"),
        RESERVED("Đã đặt"),
        DELETED("Đã xóa"),
        ARCHIVED("Lưu trữ");

        private final String displayName;

        ProductStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public Product() {}

    public Product(String title, String description, BigDecimal price, String location) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.location = location;
        this.status = ProductStatus.AVAILABLE;
        this.imageUrls = new ArrayList<>();
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

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public ProductCondition getCondition() { return condition; }
    public void setCondition(ProductCondition condition) { this.condition = condition; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    // Thêm method setImageUrl cho compatibility
    public void setImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            this.imageUrls = List.of(imageUrl);
        }
    }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // ✅ THÊM METHOD setPrimaryImageUrl()
    public void setPrimaryImageUrl(String primaryImageUrl) {
        if (primaryImageUrl != null && !primaryImageUrl.isEmpty()) {
            if (this.imageUrls == null) {
                this.imageUrls = new ArrayList<>();
            }
            // Nếu chưa có image nào, thêm vào đầu list
            if (this.imageUrls.isEmpty()) {
                this.imageUrls.add(primaryImageUrl);
            } else {
                // Nếu đã có images, thay thế image đầu tiên
                this.imageUrls.set(0, primaryImageUrl);
            }
        }
    }

    // Helper methods
    public String getFormattedPrice() {
        if (price == null) return "Free";
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " VNĐ";
    }

    public String getPrimaryImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }

    public String getConditionDisplayName() {
        return condition != null ? condition.getDisplayName() : "";
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }
}