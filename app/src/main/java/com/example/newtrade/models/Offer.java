// app/src/main/java/com/example/newtrade/models/Offer.java
package com.example.newtrade.models;

import android.util.Log;

import java.math.BigDecimal;
import java.util.Map;

public class Offer {

    public enum OfferStatus {
        PENDING, ACCEPTED, REJECTED, COUNTERED, CANCELLED, EXPIRED
    }

    private Long id;
    private Long productId;
    private String productTitle;
    private String productImage; // ✅ THÊM cho hiển thị ảnh
    private BigDecimal offerAmount;
    private BigDecimal originalPrice;
    private OfferStatus status;
    private String message;
    private String buyerName;
    private String sellerName;
    private String createdAt;
    private String expiresAt; // ✅ THÊM để hiển thị expire time

    // Constructors
    public Offer() {}

    // ✅ THÊM STATIC METHOD để parse từ backend response
    public static Offer fromBackendResponse(Map<String, Object> data) {
        Offer offer = new Offer();

        try {
            // Basic fields
            if (data.get("id") != null) {
                offer.setId(((Number) data.get("id")).longValue());
            }

            if (data.get("offerAmount") != null) {
                offer.setOfferAmount(new BigDecimal(data.get("offerAmount").toString()));
            }

            if (data.get("originalPrice") != null) {
                offer.setOriginalPrice(new BigDecimal(data.get("originalPrice").toString()));
            }

            if (data.get("status") != null) {
                try {
                    offer.setStatus(OfferStatus.valueOf(data.get("status").toString()));
                } catch (IllegalArgumentException e) {
                    offer.setStatus(OfferStatus.PENDING);
                }
            }

            offer.setMessage((String) data.get("message"));
            offer.setCreatedAt((String) data.get("createdAt"));
            offer.setExpiresAt((String) data.get("expiresAt"));

            // ✅ PRODUCT INFO
            Map<String, Object> product = (Map<String, Object>) data.get("product");
            if (product != null) {
                if (product.get("id") != null) {
                    offer.setProductId(((Number) product.get("id")).longValue());
                }
                offer.setProductTitle((String) product.get("title"));

                // Get first image
                Object images = product.get("images");
                if (images instanceof java.util.List && !((java.util.List) images).isEmpty()) {
                    offer.setProductImage(((java.util.List<String>) images).get(0));
                } else {
                    Object singleImage = product.get("imageUrl");
                    if (singleImage != null) {
                        offer.setProductImage(singleImage.toString());
                    }
                }
            }

            // ✅ USER INFO
            Map<String, Object> buyer = (Map<String, Object>) data.get("buyer");
            if (buyer != null) {
                offer.setBuyerName((String) buyer.get("displayName"));
            }

            Map<String, Object> seller = (Map<String, Object>) data.get("seller");
            if (seller != null) {
                offer.setSellerName((String) seller.get("displayName"));
            }

        } catch (Exception e) {
            Log.e("Offer", "Error parsing offer data: " + data, e);
        }

        return offer;
    }

    // Getters and Setters (đã có đầy đủ theo code bạn cung cấp)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    // ✅ THÊM getter/setter cho productImage
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

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

    // ✅ THÊM getter/setter cho expiresAt
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    // ✅ HELPER METHODS
    public boolean isPending() {
        return status == OfferStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == OfferStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return status == OfferStatus.REJECTED;
    }

    public boolean isCountered() {
        return status == OfferStatus.COUNTERED;
    }

    public String getStatusDisplayText() {
        switch (status) {
            case PENDING: return "Pending";
            case ACCEPTED: return "Accepted";
            case REJECTED: return "Rejected";
            case COUNTERED: return "Countered";
            case CANCELLED: return "Cancelled";
            case EXPIRED: return "Expired";
            default: return "Unknown";
        }
    }
}