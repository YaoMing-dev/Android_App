// app/src/main/java/com/example/newtrade/api/OfferService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OfferService {

    @POST("api/offers")
    Call<StandardResponse<Map<String, Object>>> createOffer(@Body Map<String, Object> offerData);

    @PUT("api/offers/{id}/respond")
    Call<StandardResponse<Map<String, Object>>> respondToOffer(
            @Path("id") Long offerId,
            @Query("status") String status,
            @Query("counterAmount") Double counterAmount,
            @Query("message") String message
    );

    @GET("api/offers/buyer")
    Call<StandardResponse<Map<String, Object>>> getBuyerOffers(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/offers/seller")
    Call<StandardResponse<Map<String, Object>>> getSellerOffers(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/offers/product/{productId}")
    Call<StandardResponse<Map<String, Object>>> getOffersForProduct(@Path("productId") Long productId);

    @PUT("api/offers/{id}/cancel")
    Call<StandardResponse<String>> cancelOffer(@Path("id") Long offerId);
}