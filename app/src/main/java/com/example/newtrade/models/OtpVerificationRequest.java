// app/src/main/java/com/example/newtrade/models/OtpVerificationRequest.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class OtpVerificationRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("otpCode")
    private String otpCode;

    public OtpVerificationRequest() {}

    public OtpVerificationRequest(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}