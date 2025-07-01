// app/src/main/java/com/example/newtrade/models/GoogleSignInRequest.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class GoogleSignInRequest {
    @SerializedName("idToken")
    private String idToken;

    public GoogleSignInRequest() {}

    public GoogleSignInRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}