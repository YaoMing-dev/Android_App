// app/src/main/java/com/example/newtrade/models/OtpRequest.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class OtpRequest {
    @SerializedName("email")
    private String email;

    public OtpRequest() {}

    public OtpRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}