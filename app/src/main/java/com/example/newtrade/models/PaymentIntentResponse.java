// app/src/main/java/com/example/newtrade/models/PaymentIntentResponse.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class PaymentIntentResponse {
    @SerializedName("clientSecret")
    private String clientSecret;

    @SerializedName("paymentIntentId")
    private String paymentIntentId;

    @SerializedName("status")
    private String status;

    @SerializedName("currency")
    private String currency;

    @SerializedName("amount")
    private Long amount; // In cents

    // Constructors
    public PaymentIntentResponse() {}

    // Getters and Setters
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    // Helper methods
    public BigDecimal getAmountDecimal() {
        if (amount == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100));
    }

    public String getDisplayAmount() {
        BigDecimal amountDecimal = getAmountDecimal();
        if (currency != null && currency.equalsIgnoreCase("VND")) {
            return String.format("₫%,.0f", amountDecimal);
        }
        return String.format("$%.2f", amountDecimal);
    }
}