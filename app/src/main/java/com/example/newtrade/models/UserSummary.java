package com.example.newtrade.models;

public class UserSummary {
    private Long id;
    private String displayName;
    private String email;
    private String profilePicture;

    // Constructors
    public UserSummary() {}

    public UserSummary(Long id, String displayName, String email, String profilePicture) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.profilePicture = profilePicture;
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

    @Override
    public String toString() {
        return "UserSummary{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}