package com.example.newtrade.api;

import com.example.newtrade.models.ReviewRequest;
import com.example.newtrade.models.StandardResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ReviewService {

    // ✅ POST /api/reviews - Submit review với User-ID header
    @POST("reviews")
    Call<StandardResponse<Map<String, Object>>> submitReview(
            @Header("User-ID") Long userId,
            @Body ReviewRequest request
    );

    // ✅ GET /api/reviews/user/{userId} - Get user reviews (paginated)
    @GET("reviews/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getUserReviews(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    // ✅ GET /api/reviews/transaction/{transactionId}/can-review - Check review eligibility
    @GET("reviews/transaction/{transactionId}/can-review")
    Call<StandardResponse<Map<String, Boolean>>> canReviewTransaction(
            @Path("transactionId") Long transactionId,
            @Header("User-ID") Long userId
    );
}