// app/src/main/java/com/example/newtrade/models/PaymentIntentRequest.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class PaymentIntentRequest {
    @SerializedName("transactionId")
    private Long transactionId;

    @SerializedName("description")
    private String description;

    // Constructors
    public PaymentIntentRequest() {}

    public PaymentIntentRequest(Long transactionId, String description) {
        this.transactionId = transactionId;
        this.description = description;
    }

    // Getters and Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}