// app/src/main/java/com/example/newtrade/models/Transaction.java
package com.example.newtrade.models;

import java.math.BigDecimal;

public class Transaction {

    public enum TransactionStatus {
        PENDING, PAID, COMPLETED, CANCELLED, REFUNDED
    }

    public enum DeliveryMethod {
        PICKUP, DELIVERY, SHIPPING
    }

    private Long id;
    private Long productId;
    private String productTitle;
    private String productImageUrl;
    private Long sellerId;
    private String sellerName;
    private String sellerAvatarUrl;
    private Long buyerId;
    private String buyerName;
    private String buyerAvatarUrl;
    private Long offerId;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private TransactionStatus paymentStatus;
    private DeliveryMethod deliveryMethod;
    private String deliveryAddress;
    private String completionDate;
    private String createdAt;
    private String updatedAt;
    private Product product; // Optional
    private boolean canReview;
    private boolean hasReviewed;

    // Constructors
    public Transaction() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerAvatarUrl() { return sellerAvatarUrl; }
    public void setSellerAvatarUrl(String sellerAvatarUrl) { this.sellerAvatarUrl = sellerAvatarUrl; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerAvatarUrl() { return buyerAvatarUrl; }
    public void setBuyerAvatarUrl(String buyerAvatarUrl) { this.buyerAvatarUrl = buyerAvatarUrl; }

    public Long getOfferId() { return offerId; }
    public void setOfferId(Long offerId) { this.offerId = offerId; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public TransactionStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(TransactionStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public DeliveryMethod getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(DeliveryMethod deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getCompletionDate() { return completionDate; }
    public void setCompletionDate(String completionDate) { this.completionDate = completionDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public boolean isCanReview() { return canReview; }
    public void setCanReview(boolean canReview) { this.canReview = canReview; }

    public boolean isHasReviewed() { return hasReviewed; }
    public void setHasReviewed(boolean hasReviewed) { this.hasReviewed = hasReviewed; }

    // Helper methods
    public String getFormattedPrice() {
        if (finalAmount == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", finalAmount);
    }

    public boolean isCompleted() {
        return paymentStatus == TransactionStatus.COMPLETED;
    }

    public boolean isPending() {
        return paymentStatus == TransactionStatus.PENDING;
    }

    public boolean isPaid() {
        return paymentStatus == TransactionStatus.PAID;
    }

    public String getStatusDisplayText() {
        if (paymentStatus == null) return "Unknown";

        switch (paymentStatus) {
            case PENDING:
                return "Pending Payment";
            case PAID:
                return "Paid";
            case COMPLETED:
                return "Completed";
            case CANCELLED:
                return "Cancelled";
            case REFUNDED:
                return "Refunded";
            default:
                return paymentStatus.toString();
        }
    }

    public int getStatusColor() {
        if (paymentStatus == null) return android.R.color.darker_gray;

        switch (paymentStatus) {
            case PENDING:
                return android.R.color.holo_orange_dark;
            case PAID:
                return android.R.color.holo_blue_dark;
            case COMPLETED:
                return android.R.color.holo_green_dark;
            case CANCELLED:
            case REFUNDED:
                return android.R.color.holo_red_dark;
            default:
                return android.R.color.darker_gray;
        }
    }

    public String getDeliveryMethodText() {
        if (deliveryMethod == null) return "Not specified";

        switch (deliveryMethod) {
            case PICKUP:
                return "Pickup";
            case DELIVERY:
                return "Delivery";
            case SHIPPING:
                return "Shipping";
            default:
                return deliveryMethod.toString();
        }
    }

    // Check if current user is buyer or seller
    public boolean isBuyer(Long currentUserId) {
        return currentUserId != null && currentUserId.equals(buyerId);
    }

    public boolean isSeller(Long currentUserId) {
        return currentUserId != null && currentUserId.equals(sellerId);
    }

    public String getOtherPartyName(Long currentUserId) {
        if (isBuyer(currentUserId)) {
            return sellerName;
        } else if (isSeller(currentUserId)) {
            return buyerName;
        }
        return "Unknown";
    }

    public String getOtherPartyAvatarUrl(Long currentUserId) {
        if (isBuyer(currentUserId)) {
            return sellerAvatarUrl;
        } else if (isSeller(currentUserId)) {
            return buyerAvatarUrl;
        }
        return null;
    }

    public String getTransactionRole(Long currentUserId) {
        if (isBuyer(currentUserId)) {
            return "Purchase";
        } else if (isSeller(currentUserId)) {
            return "Sale";
        }
        return "Transaction";
    }
}