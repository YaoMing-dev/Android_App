// app/src/main/java/com/example/newtrade/models/User.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    private Long id;

    @SerializedName("email")
    private String email;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("profilePicture")  // ✅ Sửa để match backend
    private String profilePicture;

    @SerializedName("bio")  // ✅ Thêm missing field
    private String bio;

    @SerializedName("contactInfo")  // ✅ Thêm missing field
    private String contactInfo;

    @SerializedName("isEmailVerified")
    private Boolean isEmailVerified;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("totalTransactions")
    private Integer totalTransactions;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("lastSeen")
    private String lastSeen;

    // Constructors
    public User() {}

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    // ✅ Sửa method name để match backend
    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    // ✅ Legacy method để backward compatibility
    public String getProfileImageUrl() {
        return profilePicture;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profilePicture = profileImageUrl;
    }

    // ✅ Thêm Bio methods
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    // ✅ Thêm ContactInfo methods
    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified != null ? isEmailVerified : false;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    public Boolean getIsActive() {
        return isActive != null ? isActive : false;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }
}