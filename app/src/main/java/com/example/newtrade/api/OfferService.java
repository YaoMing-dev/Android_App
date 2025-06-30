// app/src/main/java/com/example/newtrade/api/OfferService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface OfferService {

    @POST("offers")
    Call<StandardResponse<Map<String, Object>>> createOffer(
            @Body Map<String, Object> offerData,
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

    @PUT("offers/{id}/accept")
    Call<StandardResponse<Map<String, Object>>> acceptOffer(
            @Path("id") Long offerId,
            @Header("User-ID") Long userId
    );

    @PUT("offers/{id}/reject")
    Call<StandardResponse<Void>> rejectOffer(
            @Path("id") Long offerId,
            @Header("User-ID") Long userId
    );
}