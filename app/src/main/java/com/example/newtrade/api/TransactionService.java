// app/src/main/java/com/example/newtrade/api/TransactionService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.Transaction;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface TransactionService {

    // Get transaction by ID
    @GET("api/transactions/{id}")
    Call<StandardResponse<Transaction>> getTransaction(@Path("id") Long transactionId);

    // Get user's purchases
    @GET("api/transactions/purchases")
    Call<StandardResponse<Map<String, Object>>> getPurchases(
            @Query("page") int page,
            @Query("size") int size
    );

    // Get user's sales
    @GET("api/transactions/sales")
    Call<StandardResponse<Map<String, Object>>> getSales(
            @Query("page") int page,
            @Query("size") int size
    );

    // Mark transaction as complete
    @PUT("api/transactions/{id}/complete")
    Call<StandardResponse<Transaction>> completeTransaction(@Path("id") Long transactionId);
}