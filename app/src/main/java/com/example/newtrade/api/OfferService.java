// app/src/main/java/com/example/newtrade/api/OfferService.java
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

public interface OfferService {

    @POST("/api/offers")
    Call<StandardResponse<Map<String, Object>>> createOffer(
            @Header("User-ID") Long userId,
            @Body Map<String, Object> offerRequest);

    @PUT("/api/offers/{id}/respond")
    Call<StandardResponse<Map<String, Object>>> respondToOffer(
            @Header("User-ID") Long userId,
            @Path("id") Long offerId,
            @Query("status") String status,
            @Query("counterAmount") Double counterAmount,
            @Query("message") String message);

    @GET("/api/offers/buyer")
    Call<StandardResponse<Map<String, Object>>> getBuyerOffers(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/offers/seller")
    Call<StandardResponse<Map<String, Object>>> getSellerOffers(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/offers/product/{productId}")
    Call<StandardResponse<Map<String, Object>>> getProductOffers(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @PUT("/api/offers/{id}/cancel")
    Call<StandardResponse<Map<String, Object>>> cancelOffer(
            @Header("User-ID") Long userId,
            @Path("id") Long offerId);

    // ✅ SỬA getOfferById method để có User-ID header
    @GET("/api/offers/{id}")
    Call<StandardResponse<Map<String, Object>>> getOfferById(
            @Header("User-ID") Long userId,
            @Path("id") Long offerId);

    // Compatibility method
    @GET("/api/offers/{id}")
    Call<StandardResponse<Map<String, Object>>> getOffer(
            @Header("User-ID") Long userId,
            @Path("id") Long offerId);
}