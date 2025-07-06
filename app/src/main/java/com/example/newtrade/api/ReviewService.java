// app/src/main/java/com/example/newtrade/api/ReviewService.java
package com.example.newtrade.api;

import com.example.newtrade.models.Review;
import com.example.newtrade.models.ReviewRequest;
import com.example.newtrade.models.StandardResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReviewService {

    // Submit a review
    @POST("api/reviews")
    Call<StandardResponse<Review>> submitReview(@Body ReviewRequest request);

    // Get reviews for a user
    @GET("api/reviews/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getUserReviews(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    // Check if user can review a transaction
    @GET("api/reviews/transaction/{transactionId}/can-review")
    Call<StandardResponse<Map<String, Object>>> canReviewTransaction(@Path("transactionId") Long transactionId);
}