// app/src/main/java/com/example/newtrade/models/PaymentConfig.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class PaymentConfig {
    @SerializedName("publishableKey")
    private String publishableKey;

    @SerializedName("currency")
    private String currency;

    @SerializedName("country")
    private String country;

    @SerializedName("merchantName")
    private String merchantName;

    // Constructors
    public PaymentConfig() {}

    // Getters and Setters
    public String getPublishableKey() { return publishableKey; }
    public void setPublishableKey(String publishableKey) { this.publishableKey = publishableKey; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
}