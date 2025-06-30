// app/src/main/java/com/example/newtrade/models/User.java
package com.example.newtrade.models;

public class User {
    private Long id;
    private String fullName;
    private String displayName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String bio;
    private String location;
    private Double latitude;
    private Double longitude;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
    private String lastSeen;

    // Rating info
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalProducts;

    // Constructors
    public User() {}

    public User(String fullName, String displayName, String email) {
        this.fullName = fullName;
        this.displayName = displayName;
        this.email = email;
        this.isEmailVerified = false;
        this.isPhoneVerified = false;
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

    public Boolean getIsPhoneVerified() { return isPhoneVerified; }
    public void setIsPhoneVerified(Boolean isPhoneVerified) { this.isPhoneVerified = isPhoneVerified; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getLastSeen() { return lastSeen; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public Integer getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }

    // Helper methods
    public String getDisplayNameOrFullName() {
        return displayName != null && !displayName.trim().isEmpty() ? displayName : fullName;
    }

    public String getFormattedRating() {
        if (averageRating == null || averageRating == 0) return "No rating";
        return String.format("%.1f ★ (%d reviews)", averageRating, totalReviews != null ? totalReviews : 0);
    }

    public boolean isVerified() {
        return Boolean.TRUE.equals(isEmailVerified) || Boolean.TRUE.equals(isPhoneVerified);
    }
}