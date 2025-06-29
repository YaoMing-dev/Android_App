package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class Offer {
    @SerializedName("id")
    private Long id;

    @SerializedName("product_id")
    private Long productId;

    @SerializedName("buyer_id")
    private Long buyerId;

    @SerializedName("seller_id")
    private Long sellerId;

    @SerializedName("offer_amount")
    private Double offerAmount;

    @SerializedName("original_price")
    private Double originalPrice;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private String status; // pending, accepted, rejected, countered, expired

    @SerializedName("counter_amount")
    private Double counterAmount;

    @SerializedName("counter_message")
    private String counterMessage;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Buyer/Seller info
    @SerializedName("buyer_name")
    private String buyerName;

    @SerializedName("buyer_avatar")
    private String buyerAvatar;

    @SerializedName("seller_name")
    private String sellerName;

    @SerializedName("seller_avatar")
    private String sellerAvatar;

    // Product info
    @SerializedName("product_title")
    private String productTitle;

    @SerializedName("product_image")
    private String productImage;

    // Constructors
    public Offer() {}

    public Offer(Long productId, Long buyerId, Long sellerId, Double offerAmount, String message) {
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.offerAmount = offerAmount;
        this.message = message;
        this.status = "pending";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public Double getOfferAmount() { return offerAmount; }
    public void setOfferAmount(Double offerAmount) { this.offerAmount = offerAmount; }

    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getCounterAmount() { return counterAmount; }
    public void setCounterAmount(Double counterAmount) { this.counterAmount = counterAmount; }

    public String getCounterMessage() { return counterMessage; }
    public void setCounterMessage(String counterMessage) { this.counterMessage = counterMessage; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerAvatar() { return buyerAvatar; }
    public void setBuyerAvatar(String buyerAvatar) { this.buyerAvatar = buyerAvatar; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerAvatar() { return sellerAvatar; }
    public void setSellerAvatar(String sellerAvatar) { this.sellerAvatar = sellerAvatar; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    // Helper methods
    public boolean isPending() { return "pending".equals(status); }
    public boolean isAccepted() { return "accepted".equals(status); }
    public boolean isRejected() { return "rejected".equals(status); }
    public boolean isCountered() { return "countered".equals(status); }
    public boolean isExpired() { return "expired".equals(status); }

    public double getDiscountPercentage() {
        if (originalPrice != null && originalPrice > 0 && offerAmount != null) {
            return ((originalPrice - offerAmount) / originalPrice) * 100;
        }
        return 0;
    }

    public String getStatusDisplayText() {
        switch (status) {
            case "pending": return "Pending";
            case "accepted": return "Accepted";
            case "rejected": return "Rejected";
            case "countered": return "Counter Offer";
            case "expired": return "Expired";
            default: return "Unknown";
        }
    }
}
