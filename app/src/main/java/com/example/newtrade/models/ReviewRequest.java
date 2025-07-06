// app/src/main/java/com/example/newtrade/models/ReviewRequest.java
package com.example.newtrade.models;

public class ReviewRequest {
    private Long transactionId;
    private Integer rating;
    private String comment;

    public ReviewRequest() {}

    public ReviewRequest(Long transactionId, Integer rating, String comment) {
        this.transactionId = transactionId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}