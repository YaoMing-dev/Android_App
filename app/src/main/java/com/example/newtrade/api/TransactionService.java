// app/src/main/java/com/example/newtrade/api/TransactionService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.Transaction;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TransactionService {

    // ✅ THÊM CREATE TRANSACTION METHOD
    // Create transaction for payment
    @POST("api/transactions")
    Call<StandardResponse<Transaction>> createTransaction(
            @Header("User-ID") String userId,
            @Body Map<String, Object> transactionRequest
    );

    // Overloaded methods with User-ID headers for existing methods
    @GET("api/transactions/{id}")
    Call<StandardResponse<Transaction>> getTransactionWithAuth(
            @Header("User-ID") String userId,
            @Path("id") Long transactionId
    );

    @GET("api/transactions/purchases")
    Call<StandardResponse<Map<String, Object>>> getPurchasesWithAuth(
            @Header("User-ID") String userId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/transactions/sales")
    Call<StandardResponse<Map<String, Object>>> getSalesWithAuth(
            @Header("User-ID") String userId,
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("api/transactions/{id}/complete")
    Call<StandardResponse<Transaction>> completeTransactionWithAuth(
            @Header("User-ID") String userId,
            @Path("id") Long transactionId
    );
}