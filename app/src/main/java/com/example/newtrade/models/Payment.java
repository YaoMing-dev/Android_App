// app/src/main/java/com/example/newtrade/models/Payment.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.Date;

public class Payment {
    @SerializedName("id")
    private Long id;

    @SerializedName("transactionId")
    private Long transactionId;

    @SerializedName("stripePaymentIntentId")
    private String stripePaymentIntentId;

    @SerializedName("stripeChargeId")
    private String stripeChargeId;

    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("currency")
    private String currency;

    @SerializedName("status")
    private PaymentStatus status;

    @SerializedName("paymentMethod")
    private PaymentMethod paymentMethod;

    @SerializedName("failureReason")
    private String failureReason;

    @SerializedName("stripeFee")
    private BigDecimal stripeFee;

    @SerializedName("netAmount")
    private BigDecimal netAmount;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("completedAt")
    private Date completedAt;

    @SerializedName("transaction")
    private Transaction transaction;

    // Constructors
    public Payment() {}

    public Payment(Long transactionId, BigDecimal amount, String currency, PaymentMethod paymentMethod) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public void setStripePaymentIntentId(String stripePaymentIntentId) { this.stripePaymentIntentId = stripePaymentIntentId; }

    public String getStripeChargeId() { return stripeChargeId; }
    public void setStripeChargeId(String stripeChargeId) { this.stripeChargeId = stripeChargeId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public BigDecimal getStripeFee() { return stripeFee; }
    public void setStripeFee(BigDecimal stripeFee) { this.stripeFee = stripeFee; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    // Helper methods
    public boolean isCompleted() {
        return status == PaymentStatus.SUCCEEDED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED || status == PaymentStatus.CANCELED;
    }

    public String getDisplayAmount() {
        if (currency != null && currency.equalsIgnoreCase("VND")) {
            return String.format("₫%,.0f", amount);
        }
        return String.format("$%.2f", amount);
    }

    public String getStatusDisplay() {
        if (status == null) return "Unknown";

        switch (status) {
            case PENDING:
                return "Pending";
            case PROCESSING:
                return "Processing";
            case SUCCEEDED:
                return "Completed";
            case FAILED:
                return "Failed";
            case CANCELED:
                return "Cancelled";
            case REFUNDED:
                return "Refunded";
            case PARTIALLY_REFUNDED:
                return "Partially Refunded";
            default:
                return status.toString();
        }
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", transactionId=" + transactionId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", paymentMethod=" + paymentMethod +
                '}';
    }
}