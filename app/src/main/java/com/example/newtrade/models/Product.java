// app/src/main/java/com/example/newtrade/models/Product.java
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
    private User user;
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

    // ✅ ENHANCED: Location coordinates
    private Double latitude;
    private Double longitude;

    // ✅ NEW: Distance tracking from user location
    private Double distanceFromUser; // in km
    private String distanceText; // formatted display text

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
        PAUSED("Tạm dừng"),
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
    public Product() {
        this.imageUrls = new ArrayList<>();
        this.status = ProductStatus.AVAILABLE;
    }

    public Product(String title, String description, BigDecimal price, String location) {
        this();
        this.title = title;
        this.description = description;
        this.price = price;
        this.location = location;
    }

    // ===== BASIC GETTERS & SETTERS =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // ===== LOCATION COORDINATES =====
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLocationCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    public String getLocationCoordinates() {
        if (hasLocation()) {
            return String.format("%.6f, %.6f", latitude, longitude);
        }
        return null;
    }

    // ===== DISTANCE FROM USER =====
    public Double getDistanceFromUser() {
        return distanceFromUser;
    }

    public void setDistanceFromUser(Double distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
        // Auto-generate formatted distance text
        updateDistanceText();
    }

    private void updateDistanceText() {
        if (distanceFromUser != null) {
            if (distanceFromUser < 1.0) {
                this.distanceText = String.format("%.0f m away", distanceFromUser * 1000);
            } else {
                this.distanceText = String.format("%.1f km away", distanceFromUser);
            }
        } else {
            this.distanceText = null;
        }
    }

    public String getDistanceText() {
        return distanceText;
    }

    public String getFormattedDistance() {
        if (distanceFromUser == null) return "";

        if (distanceFromUser < 1.0) {
            return String.format("%.0f m", distanceFromUser * 1000);
        } else {
            return String.format("%.1f km", distanceFromUser);
        }
    }

    public boolean isNearby(double radiusKm) {
        return distanceFromUser != null && distanceFromUser <= radiusKm;
    }

    public boolean isWithinRadius(double radiusKm) {
        return isNearby(radiusKm);
    }

    // ===== IMAGE HANDLING =====
    public List<String> getImageUrls() {
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public String getImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0); // Return first image as primary
        }
        return null;
    }

    public void setImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Clear existing and add new primary image
            this.imageUrls.clear();
            this.imageUrls.add(imageUrl);
        }
    }

    public String getPrimaryImageUrl() {
        return getImageUrl();
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }

        if (primaryImageUrl != null && !primaryImageUrl.isEmpty()) {
            if (this.imageUrls.isEmpty()) {
                // Add as first image
                this.imageUrls.add(primaryImageUrl);
            } else {
                // Replace first image
                this.imageUrls.set(0, primaryImageUrl);
            }
        }
    }

    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    public int getImageCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    public void addImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (this.imageUrls == null) {
                this.imageUrls = new ArrayList<>();
            }
            this.imageUrls.add(imageUrl);
        }
    }

    public void removeImageUrl(String imageUrl) {
        if (this.imageUrls != null) {
            this.imageUrls.remove(imageUrl);
        }
    }

    public void clearImages() {
        if (this.imageUrls != null) {
            this.imageUrls.clear();
        }
    }

    // ===== DISPLAY METHODS =====
    public String getFormattedPrice() {
        if (price == null) return "Free";
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " VNĐ";
    }

    public String getDisplayPrice() {
        return getFormattedPrice();
    }

    public String getConditionDisplayName() {
        return condition != null ? condition.getDisplayName() : "";
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    // ===== LOCATION DISPLAY WITH DISTANCE =====
    public String getLocationWithDistance() {
        StringBuilder locationText = new StringBuilder();

        if (location != null && !location.isEmpty()) {
            locationText.append(location);
        }

        if (distanceFromUser != null) {
            if (locationText.length() > 0) {
                locationText.append(" • ");
            }
            locationText.append(getFormattedDistance());
        }

        return locationText.toString();
    }

    public String getFullLocationInfo() {
        StringBuilder info = new StringBuilder();

        if (location != null && !location.isEmpty()) {
            info.append(location);
        }

        if (hasLocation()) {
            if (info.length() > 0) {
                info.append("\n");
            }
            info.append("Coordinates: ").append(getLocationCoordinates());
        }

        if (distanceFromUser != null) {
            if (info.length() > 0) {
                info.append("\n");
            }
            info.append("Distance: ").append(getFormattedDistance());
        }

        return info.toString();
    }

    // ===== UTILITY METHODS =====
    public boolean isAvailable() {
        return status == ProductStatus.AVAILABLE;
    }

    public boolean isSold() {
        return status == ProductStatus.SOLD;
    }

    public boolean hasPrice() {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }

    // ===== COMPARISON & SORTING =====
    public int compareDistanceTo(Product other) {
        if (this.distanceFromUser == null && other.distanceFromUser == null) return 0;
        if (this.distanceFromUser == null) return 1; // null distance goes to end
        if (other.distanceFromUser == null) return -1;
        return this.distanceFromUser.compareTo(other.distanceFromUser);
    }

    // ===== DEBUG =====
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", location='" + location + '\'' +
                ", coordinates=(" + latitude + ", " + longitude + ")" +
                ", distance=" + (distanceFromUser != null ? distanceFromUser + "km" : "unknown") +
                ", condition=" + condition +
                ", status=" + status +
                ", imageCount=" + getImageCount() +
                '}';
    }
}