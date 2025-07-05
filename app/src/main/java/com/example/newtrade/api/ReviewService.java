// app/src/main/java/com/example/newtrade/api/ReviewService.java
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

public interface ReviewService {

    @POST("/api/reviews")
    Call<StandardResponse<Map<String, Object>>> createReview(
            @Header("User-ID") Long userId,
            @Body Map<String, Object> reviewRequest);

    @GET("/api/reviews/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getUserReviews(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/reviews/transaction/{transactionId}/can-review")
    Call<StandardResponse<Map<String, Object>>> canReviewTransaction(
            @Header("User-ID") Long userId,
            @Path("transactionId") Long transactionId);

    @GET("/api/reviews/received")
    Call<StandardResponse<Map<String, Object>>> getReceivedReviews(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/reviews/given")
    Call<StandardResponse<Map<String, Object>>> getGivenReviews(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/reviews/{id}")
    Call<StandardResponse<Map<String, Object>>> getReview(
            @Path("id") Long reviewId);
}