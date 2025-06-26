// app/src/main/java/com/example/newtrade/models/Offer.java
package com.example.newtrade.models;

import java.math.BigDecimal;

public class Offer {

    public enum OfferStatus {
        PENDING, ACCEPTED, REJECTED, COUNTERED, CANCELLED
    }

    private Long id;
    private Long productId;
    private String productTitle;
    private BigDecimal offerAmount;
    private BigDecimal originalPrice;
    private OfferStatus status;
    private String message;
    private String buyerName;
    private String sellerName;
    private String createdAt;

    // Constructors
    public Offer() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public BigDecimal getOfferAmount() { return offerAmount; }
    public void setOfferAmount(BigDecimal offerAmount) { this.offerAmount = offerAmount; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}