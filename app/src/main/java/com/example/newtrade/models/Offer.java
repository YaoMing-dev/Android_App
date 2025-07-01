// app/src/main/java/com/example/newtrade/models/Offer.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.Date;

public class Offer {

    @SerializedName("id")
    private Long id;

    @SerializedName("product")
    private Product product;

    @SerializedName("buyer")
    private User buyer;

    @SerializedName("seller")
    private User seller;

    // ✅ MATCH DATABASE: offered_price (not offerAmount)
    @SerializedName("offeredPrice")
    private BigDecimal offeredPrice;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private String status; // "PENDING", "ACCEPTED", "REJECTED", "COUNTERED", "EXPIRED", "CANCELLED"

    @SerializedName("expiresAt")
    private Date expiresAt;

    @SerializedName("respondedAt")
    private Date respondedAt;

    @SerializedName("createdAt")
    private Date createdAt;

    // Constructors
    public Offer() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    // ✅ OFFERED_PRICE (match database)
    public BigDecimal getOfferedPrice() { return offeredPrice; }
    public void setOfferedPrice(BigDecimal offeredPrice) { this.offeredPrice = offeredPrice; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getExpiresAt() { return expiresAt; }
    public Date getRespondedAt() { return respondedAt; }
    public Date getCreatedAt() { return createdAt; }

    // Utility methods
    public String getFormattedOfferedPrice() {
        return offeredPrice != null ? String.format("%,.0f VNĐ", offeredPrice.doubleValue()) : "N/A";
    }

    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isAccepted() { return "ACCEPTED".equals(status); }
    public boolean isRejected() { return "REJECTED".equals(status); }
}