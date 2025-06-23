package com.example.newtrade.models;
import com.example.newtrade.utils.Constants;
import java.math.BigDecimal;
import java.util.Date;

public class User {

    private Long id;
    private String displayName;
    private String email;
    private String profilePicture;
    private String bio;
    private String contactInfo;
    private BigDecimal rating;
    private Integer totalTransactions;
    private Boolean isEmailVerified;
    private Boolean isActive;
    private String googleId;
    private String fcmToken;
    private Date createdAt;
    private Date updatedAt;

    // Constructors
    public User() {}

    public User(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
        this.rating = BigDecimal.ZERO;
        this.totalTransactions = 0;
        this.isEmailVerified = false;
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public String getDisplayRating() {
        if (rating == null || rating.compareTo(BigDecimal.ZERO) == 0) {
            return "Chưa có đánh giá";
        }
        return String.format("%.1f", rating);
    }

    public String getTransactionText() {
        if (totalTransactions == null || totalTransactions == 0) {
            return "Chưa có giao dịch";
        }
        return totalTransactions + " giao dịch";
    }

    public String getProfileImageUrl() {
        if (profilePicture == null || profilePicture.isEmpty()) {
            return null;
        }
        return Constants.getImageUrl(profilePicture);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", rating=" + rating +
                ", totalTransactions=" + totalTransactions +
                ", isEmailVerified=" + isEmailVerified +
                ", isActive=" + isActive +
                '}';
    }
}