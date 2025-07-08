// app/src/main/java/com/example/newtrade/models/OfferRequest.java
package com.example.newtrade.models;

import java.math.BigDecimal;

public class OfferRequest {
    private Long productId;
    private BigDecimal offerAmount;
    private String message;

    public OfferRequest() {}

    public OfferRequest(Long productId, BigDecimal offerAmount, String message) {
        this.productId = productId;
        this.offerAmount = offerAmount;
        this.message = message;
    }

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getOfferAmount() { return offerAmount; }
    public void setOfferAmount(BigDecimal offerAmount) { this.offerAmount = offerAmount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}