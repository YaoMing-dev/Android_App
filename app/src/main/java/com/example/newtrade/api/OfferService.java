// app/src/main/java/com/example/newtrade/api/OfferService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface OfferService {

    // ===== OFFER MANAGEMENT =====

    @POST("offers")
    Call<StandardResponse<Map<String, Object>>> createOffer(
            @Body Map<String, Object> offerData,
            @Header("User-ID") Long userId
    );

    @GET("offers/{id}")
    Call<StandardResponse<Map<String, Object>>> getOfferById(
            @Path("id") Long offerId,
            @Header("User-ID") Long userId
    );

    @PUT("offers/{id}")
    Call<StandardResponse<Map<String, Object>>> updateOffer(
            @Path("id") Long offerId,
            @Body Map<String, Object> offerData,
            @Header("User-ID") Long userId
    );

    @DELETE("offers/{id}")
    Call<StandardResponse<Void>> deleteOffer(
            @Path("id") Long offerId,
            @Header("User-ID") Long userId
    );

    // ===== OFFER RESPONSES =====

    @PUT("offers/{id}/accept")
    Call<StandardResponse<Map<String, Object>>> acceptOffer(
            @Path("id") Long offerId,
            @Header("User-ID") Long userId
    );

    @PUT("offers/{id}/reject")
    Call<StandardResponse<Void>> rejectOffer(
            @Path("id") Long offerId,
            @Body Map<String, String> rejectionData,
            @Header("User-ID") Long userId
    );

    @PUT("offers/{id}/counter")
    Call<StandardResponse<Map<String, Object>>> counterOffer(
            @Path("id") Long offerId,
            @Body Map<String, Object> counterOfferData,
            @Header("User-ID") Long userId
    );

    // ===== USER OFFERS =====

    @GET("offers/received")
    Call<StandardResponse<Map<String, Object>>> getReceivedOffers(
            @Query("page") int page,
            @Query("size") int size,
            @Query("status") String status,
            @Header("User-ID") Long userId
    );

    @GET("offers/sent")
    Call<StandardResponse<Map<String, Object>>> getSentOffers(
            @Query("page") int page,
            @Query("size") int size,
            @Query("status") String status,
            @Header("User-ID") Long userId
    );

    // ===== PRODUCT OFFERS =====

    @GET("products/{productId}/offers")
    Call<StandardResponse<Map<String, Object>>> getProductOffers(
            @Path("productId") Long productId,
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    // ===== OFFER STATS =====

    @GET("offers/stats")
    Call<StandardResponse<Map<String, Object>>> getOfferStats(
            @Header("User-ID") Long userId
    );
}