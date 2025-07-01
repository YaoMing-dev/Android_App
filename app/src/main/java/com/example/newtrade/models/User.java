// app/src/main/java/com/example/newtrade/models/User.java
package com.example.newtrade.models;
import com.example.newtrade.utils.Constants;
import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class User {

    @SerializedName("id")
    private Long id;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("email")
    private String email;

    @SerializedName("profilePicture")
    private String profilePicture;

    @SerializedName("bio")
    private String bio;

    @SerializedName("contactInfo")
    private String contactInfo;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("totalTransactions")
    private Integer totalTransactions;

    @SerializedName("isEmailVerified")
    private Boolean isEmailVerified;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("createdAt")
    private Date createdAt;

    // Constructors
    public User() {}

    public User(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Double getRating() {
        return rating != null ? rating : 0.0;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalTransactions() {
        return totalTransactions != null ? totalTransactions : 0;
    }

    public void setTotalTransactions(Integer totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified != null ? isEmailVerified : false;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    public Boolean getIsActive() {
        return isActive != null ? isActive : true;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public String getProfilePictureUrl() {
        if (profilePicture != null && !profilePicture.isEmpty()) {
            if (profilePicture.startsWith("http")) {
                return profilePicture;
            } else {
                return Constants.AVATAR_IMAGES_URL + profilePicture;
            }
        }
        return null;
    }

    public String getDisplayNameOrEmail() {
        return displayName != null && !displayName.isEmpty() ? displayName : email;
    }

    public boolean isVerified() {
        return Boolean.TRUE.equals(isEmailVerified);
    }

    public String getRatingText() {
        double r = getRating();
        if (r == 0.0) return "No rating";
        return String.format("%.1f ⭐ (%d reviews)", r, getTotalTransactions());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", rating=" + rating +
                ", verified=" + isEmailVerified +
                '}';
    }
}