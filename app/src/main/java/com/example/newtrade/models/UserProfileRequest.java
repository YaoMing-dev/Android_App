// app/src/main/java/com/example/newtrade/models/UserProfileRequest.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class UserProfileRequest {

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("bio")
    private String bio;

    @SerializedName("contactInfo")
    private String contactInfo;

    @SerializedName("profilePicture")
    private String profilePicture;

    // Constructors
    public UserProfileRequest() {}

    // Getters and Setters
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}