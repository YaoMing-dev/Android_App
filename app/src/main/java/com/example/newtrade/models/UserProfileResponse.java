// app/src/main/java/com/example/newtrade/models/UserProfileResponse.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {

    @SerializedName("user")
    private User user;

    @SerializedName("stats")
    private UserStats stats;

    // Constructors
    public UserProfileResponse() {}

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public UserStats getStats() { return stats; }
    public void setStats(UserStats stats) { this.stats = stats; }

    // Inner class for user stats
    public static class UserStats {
        @SerializedName("totalListings")
        private int totalListings;

        @SerializedName("activeLisitings")
        private int activeListings;

        @SerializedName("soldItems")
        private int soldItems;

        @SerializedName("rating")
        private double rating;

        @SerializedName("reviewCount")
        private int reviewCount;

        // Getters and Setters
        public int getTotalListings() { return totalListings; }
        public void setTotalListings(int totalListings) { this.totalListings = totalListings; }

        public int getActiveListings() { return activeListings; }
        public void setActiveListings(int activeListings) { this.activeListings = activeListings; }

        public int getSoldItems() { return soldItems; }
        public void setSoldItems(int soldItems) { this.soldItems = soldItems; }

        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }

        public int getReviewCount() { return reviewCount; }
        public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    }
}