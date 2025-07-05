// app/src/main/java/com/example/newtrade/api/TransactionService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
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

    @POST("/api/transactions")
    Call<StandardResponse<Map<String, Object>>> createTransaction(
            @Header("User-ID") Long userId,
            @Query("productId") Long productId,
            @Query("offerId") Long offerId,
            @Query("paymentMethod") String paymentMethod,
            @Query("deliveryMethod") String deliveryMethod,
            @Query("deliveryAddress") String deliveryAddress,
            @Query("notes") String notes);

    @PUT("/api/transactions/{id}/complete")
    Call<StandardResponse<Map<String, Object>>> completeTransaction(
            @Header("User-ID") Long userId,
            @Path("id") Long transactionId);

    @GET("/api/transactions/{id}")
    Call<StandardResponse<Map<String, Object>>> getTransaction(
            @Header("User-ID") Long userId,
            @Path("id") Long transactionId);

    @GET("/api/transactions/purchases")
    Call<StandardResponse<Map<String, Object>>> getPurchases(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/transactions/sales")
    Call<StandardResponse<Map<String, Object>>> getSales(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @PUT("/api/transactions/{id}/cancel")
    Call<StandardResponse<Map<String, Object>>> cancelTransaction(
            @Header("User-ID") Long userId,
            @Path("id") Long transactionId);

    @GET("/api/transactions/{id}/status")
    Call<StandardResponse<Map<String, Object>>> getTransactionStatus(
            @Header("User-ID") Long userId,
            @Path("id") Long transactionId);
}