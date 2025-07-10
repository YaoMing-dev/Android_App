// app/src/main/java/com/example/newtrade/models/PaymentStatus.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public enum PaymentStatus {
    @SerializedName("PENDING")
    PENDING,

    @SerializedName("PROCESSING")
    PROCESSING,

    @SerializedName("SUCCEEDED")
    SUCCEEDED,

    @SerializedName("FAILED")
    FAILED,

    @SerializedName("CANCELED")
    CANCELED,

    @SerializedName("REFUNDED")
    REFUNDED,

    @SerializedName("PARTIALLY_REFUNDED")
    PARTIALLY_REFUNDED
}