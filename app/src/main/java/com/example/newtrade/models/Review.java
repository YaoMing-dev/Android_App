// app/src/main/java/com/example/newtrade/models/Review.java
package com.example.newtrade.models;

public class Review {
    private Long id;
    private Long transactionId;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerAvatarUrl;
    private Long revieweeId;
    private String revieweeName;
    private String revieweeAvatarUrl;
    private Integer rating;
    private String comment;
    private String createdAt;
    private String productTitle;

    // Constructors
    public Review() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getReviewerAvatarUrl() { return reviewerAvatarUrl; }
    public void setReviewerAvatarUrl(String reviewerAvatarUrl) { this.reviewerAvatarUrl = reviewerAvatarUrl; }

    public Long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }

    public String getRevieweeName() { return revieweeName; }
    public void setRevieweeName(String revieweeName) { this.revieweeName = revieweeName; }

    public String getRevieweeAvatarUrl() { return revieweeAvatarUrl; }
    public void setRevieweeAvatarUrl(String revieweeAvatarUrl) { this.revieweeAvatarUrl = revieweeAvatarUrl; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    // Helper methods
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    public String getRatingText() {
        if (rating == null) return "No rating";
        switch (rating) {
            case 1: return "Poor";
            case 2: return "Fair";
            case 3: return "Good";
            case 4: return "Very Good";
            case 5: return "Excellent";
            default: return "No rating";
        }
    }
}