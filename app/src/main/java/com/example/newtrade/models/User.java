// app/src/main/java/com/example/newtrade/models/User.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class User {
    private Long id;
    private String email;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("profilePicture")
    private String profilePicture;

    @SerializedName("contactInfo")
    private String contactInfo;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    private String bio;
    private String location;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("isEmailVerified")
    private Boolean isEmailVerified;

    @SerializedName("isPhoneVerified")
    private Boolean isPhoneVerified;

    private Double rating;

    @SerializedName("totalTransactions")
    private Integer totalTransactions;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Constructors
    public User() {}

    // Helper methods
    public String getDisplayOrFullName() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            return firstName;
        }
        return "Unknown User";
    }

    public String getProfileImageUrl() {
        return avatarUrl != null ? avatarUrl : profilePicture;
    }

    public boolean isVerified() {
        return Boolean.TRUE.equals(isEmailVerified);
    }

    public boolean isActiveUser() {
        return Boolean.TRUE.equals(isActive);
    }

    public String getRatingString() {
        if (rating == null || rating == 0.0) {
            return "No rating";
        }
        return String.format("%.1f", rating);
    }

    public String getTransactionCountText() {
        if (totalTransactions == null || totalTransactions == 0) {
            return "No transactions";
        } else if (totalTransactions == 1) {
            return "1 transaction";
        } else {
            return totalTransactions + " transactions";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

    public Boolean getIsPhoneVerified() { return isPhoneVerified; }
    public void setIsPhoneVerified(Boolean isPhoneVerified) { this.isPhoneVerified = isPhoneVerified; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}