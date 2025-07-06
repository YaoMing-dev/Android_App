// app/src/main/java/com/example/newtrade/models/Review.java
package com.example.newtrade.models;

public class Review {
    private Long id;
    private Long transactionId;
    private Long reviewerId;
    private Long revieweeId;
    private String reviewerName;
    private String revieweeName;
    private String reviewerAvatarUrl;
    private Integer rating;
    private String comment;
    private String createdAt;
    private Transaction transaction; // Optional

    // Constructors
    public Review() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public Long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getRevieweeName() { return revieweeName; }
    public void setRevieweeName(String revieweeName) { this.revieweeName = revieweeName; }

    public String getReviewerAvatarUrl() { return reviewerAvatarUrl; }
    public void setReviewerAvatarUrl(String reviewerAvatarUrl) { this.reviewerAvatarUrl = reviewerAvatarUrl; }

    public Integer getRating() { return rating != null ? rating : 0; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    // Helper methods
    public String getRatingStars() {
        StringBuilder stars = new StringBuilder();
        int rating = getRating();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
}