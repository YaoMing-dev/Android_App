// app/src/main/java/com/example/newtrade/api/PaymentService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaymentService {

    @GET("/api/payments/config")
    Call<StandardResponse<Map<String, Object>>> getPaymentConfig();

    @POST("/api/payments/create-payment-intent")
    Call<StandardResponse<Map<String, Object>>> createPaymentIntent(
            @Header("User-ID") Long userId,
            @Body Map<String, Object> paymentIntentRequest);

    @POST("/api/payments/confirm-payment")
    Call<StandardResponse<Map<String, Object>>> confirmPayment(
            @Query("paymentIntentId") String paymentIntentId);

    @GET("/api/payments/transaction/{transactionId}")
    Call<StandardResponse<Map<String, Object>>> getTransactionPayment(
            @Header("User-ID") Long userId,
            @Path("transactionId") Long transactionId);

    @POST("/api/payments/{paymentId}/refund")
    Call<StandardResponse<Map<String, Object>>> processRefund(
            @Header("User-ID") Long userId,
            @Path("paymentId") Long paymentId,
            @Body Map<String, Object> refundRequest);

    @GET("/api/payments/history")
    Call<StandardResponse<Map<String, Object>>> getPaymentHistory(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);
}