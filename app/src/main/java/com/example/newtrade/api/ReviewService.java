package com.example.newtrade.api;

import com.example.newtrade.models.ReviewRequest;
import com.example.newtrade.models.StandardResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ReviewService {

    // ✅ FIXED: POST /api/reviews với đúng header User-ID
    @POST("api/reviews")
    Call<StandardResponse<Map<String, Object>>> submitReview(
            @Header("User-ID") Long userId,
            @Body ReviewRequest request
    );

    // ✅ FIXED: GET /api/reviews/user/{userId} với pagination
    @GET("api/reviews/user/{userId}")
    Call<StandardResponse<Map<String, Object>>> getUserReviews(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    // ✅ FIXED: GET /api/reviews/transaction/{transactionId}/can-review với đúng header
    @GET("api/reviews/transaction/{transactionId}/can-review")
    Call<StandardResponse<Map<String, Boolean>>> canReviewTransaction(
            @Path("transactionId") Long transactionId,
            @Header("User-ID") Long userId  // ✅ ĐÚNG: Backend sử dụng @RequestHeader
    );
}