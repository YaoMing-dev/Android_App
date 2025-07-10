package com.example.newtrade.models;

public class Review {
    private Long id;
    private Long transactionId;
    private UserSummary reviewer;  // ✅ Dùng UserSummary
    private UserSummary reviewee;  // ✅ Dùng UserSummary
    private Integer rating;
    private String comment;
    private String createdAt;

    // Constructors
    public Review() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public UserSummary getReviewer() { return reviewer; }
    public void setReviewer(UserSummary reviewer) { this.reviewer = reviewer; }

    public UserSummary getReviewee() { return reviewee; }
    public void setReviewee(UserSummary reviewee) { this.reviewee = reviewee; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // ✅ Helper methods cho existing code compatibility
    public String getReviewerName() {
        return reviewer != null ? reviewer.getDisplayName() : "Anonymous";
    }

    public String getReviewerAvatarUrl() {
        return reviewer != null ? reviewer.getProfilePicture() : null;
    }

    public Long getReviewerId() {
        return reviewer != null ? reviewer.getId() : null;
    }

    public Long getRevieweeId() {
        return reviewee != null ? reviewee.getId() : null;
    }

    public String getRevieweeName() {
        return reviewee != null ? reviewee.getDisplayName() : "Anonymous";
    }

    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    public String getRatingText() {
        if (rating == null) return "";
        switch (rating) {
            case 1: return "Poor";
            case 2: return "Fair";
            case 3: return "Good";
            case 4: return "Very Good";
            case 5: return "Excellent";
            default: return "";
        }
    }

    public String getProductTitle() {
        // Backend chưa return field này, có thể thêm sau
        return null;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", rating=" + rating +
                ", reviewer=" + getReviewerName() +
                ", reviewee=" + getRevieweeName() +
                ", comment='" + comment + '\'' +
                '}';
    }
}