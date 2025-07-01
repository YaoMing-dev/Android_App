// app/src/main/java/com/example/newtrade/utils/ApiCallback.java
package com.example.newtrade.utils;

import android.util.Log;
import com.example.newtrade.models.StandardResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ApiCallback<T> implements Callback<StandardResponse<T>> {
    private static final String TAG = "ApiCallback";

    public abstract void onSuccess(T result);
    public abstract void onError(String error);

    @Override
    public void onResponse(Call<StandardResponse<T>> call, Response<StandardResponse<T>> response) {
        try {
            if (response.isSuccessful() && response.body() != null) {
                StandardResponse<T> apiResponse = response.body();
                if (apiResponse.isSuccess()) {
                    onSuccess(apiResponse.getData());
                } else {
                    onError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error");
                }
            } else {
                String errorMessage = "Request failed";
                if (response.code() == 401) {
                    errorMessage = "Unauthorized - Please login again";
                } else if (response.code() == 403) {
                    errorMessage = "Forbidden - Access denied";
                } else if (response.code() == 404) {
                    errorMessage = "Not found";
                } else if (response.code() == 500) {
                    errorMessage = "Server error - Please try again later";
                } else {
                    errorMessage = "Error " + response.code() + ": " + response.message();
                }
                onError(errorMessage);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing response", e);
            onError("Error processing response: " + e.getMessage());
        }
    }

    @Override
    public void onFailure(Call<StandardResponse<T>> call, Throwable t) {
        Log.e(TAG, "Network call failed", t);

        String errorMessage;
        if (t instanceof java.net.SocketTimeoutException) {
            errorMessage = "Connection timeout - Please try again";
        } else if (t instanceof java.net.UnknownHostException) {
            errorMessage = "No internet connection - Please check your network";
        } else if (t instanceof java.net.ConnectException) {
            errorMessage = "Unable to connect to server";
        } else {
            errorMessage = "Network error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error");
        }

        onError(errorMessage);
    }
}