// app/src/main/java/com/example/newtrade/models/PaymentMethod.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public enum PaymentMethod {
    @SerializedName("CARD")
    CARD("Credit/Debit Card"),

    @SerializedName("BANK_TRANSFER")
    BANK_TRANSFER("Bank Transfer"),

    @SerializedName("DIGITAL_WALLET")
    DIGITAL_WALLET("Digital Wallet"),

    @SerializedName("CASH")
    CASH("Cash");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}