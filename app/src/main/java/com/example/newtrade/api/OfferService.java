// app/src/main/java/com/example/newtrade/api/OfferService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.math.BigDecimal;
import java.util.Map;

public interface OfferService {

    @POST("offers")
    Call<StandardResponse<Map<String, Object>>> makeOffer(
            @Body Map<String, Object> offerData,
            @Header("User-ID") Long userId
    );

    @PUT("offers/{id}/respond")
    Call<StandardResponse<Map<String, Object>>> respondToOffer(
            @Path("id") Long offerId,
            @Query("status") String status,
            @Query("counterAmount") BigDecimal counterAmount,
            @Query("message") String message,
            @Header("User-ID") Long userId
    );

    @GET("offers/received")
    Call<StandardResponse<Map<String, Object>>> getReceivedOffers(
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    @GET("offers/sent")
    Call<StandardResponse<Map<String, Object>>> getSentOffers(
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );

    @GET("offers/product/{productId}")
    Call<StandardResponse<Map<String, Object>>> getProductOffers(
            @Path("productId") Long productId,
            @Query("page") int page,
            @Query("size") int size,
            @Header("User-ID") Long userId
    );
}