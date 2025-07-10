// app/src/main/java/com/example/newtrade/api/PaymentService.java
package com.example.newtrade.api;

import com.example.newtrade.models.PagedResponse;
import com.example.newtrade.models.Payment;
import com.example.newtrade.models.PaymentConfig;
import com.example.newtrade.models.PaymentIntentRequest;
import com.example.newtrade.models.PaymentIntentResponse;
import com.example.newtrade.models.StandardResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaymentService {

    // Get payment configuration (Stripe publishable key, etc.)
    @GET("payments/config")
    Call<StandardResponse<PaymentConfig>> getPaymentConfig();

    // Create payment intent for transaction
    @POST("payments/create-payment-intent")
    Call<StandardResponse<PaymentIntentResponse>> createPaymentIntent(
            @Header("User-ID") String userId,
            @Body PaymentIntentRequest request
    );

    // Confirm payment after Stripe confirmation
    @POST("payments/confirm-payment")
    Call<StandardResponse<Payment>> confirmPayment(
            @Query("paymentIntentId") String paymentIntentId
    );

    // Get payment details by transaction ID
    @GET("payments/transaction/{transactionId}")
    Call<StandardResponse<Payment>> getPaymentByTransaction(
            @Header("User-ID") String userId,
            @Path("transactionId") Long transactionId
    );

    // Get payment status by payment intent ID
    @GET("payments/status/{paymentIntentId}")
    Call<StandardResponse<Payment>> getPaymentStatus(
            @Path("paymentIntentId") String paymentIntentId
    );

    // Get user's payment history
    @GET("payments/my-payments")
    Call<StandardResponse<PagedResponse<Payment>>> getMyPayments(
            @Header("User-ID") String userId,
            @Query("page") int page,
            @Query("size") int size
    );

    // Process refund (for sellers)
    @POST("payments/{paymentId}/refund")
    Call<StandardResponse<Payment>> processRefund(
            @Header("User-ID") String userId,
            @Path("paymentId") Long paymentId,
            @Body RefundRequest refundRequest
    );

    // Refund request model
    class RefundRequest {
        private Double amount;
        private String reason;

        public RefundRequest(Double amount, String reason) {
            this.amount = amount;
            this.reason = reason;
        }

        // Getters and setters
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}