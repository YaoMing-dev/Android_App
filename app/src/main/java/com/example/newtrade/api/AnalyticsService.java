// app/src/main/java/com/example/newtrade/api/AnalyticsService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface AnalyticsService {

    @GET("/api/analytics/dashboard")
    Call<StandardResponse<Map<String, Object>>> getDashboardAnalytics(
            @Header("User-ID") Long userId);

    @GET("/api/analytics/product/{productId}/stats")
    Call<StandardResponse<Map<String, Object>>> getProductStats(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @GET("/api/analytics/user/{userId}/stats")
    Call<StandardResponse<Map<String, Object>>> getUserStats(
            @Path("userId") Long userId);

    @GET("/api/analytics/platform/stats")
    Call<StandardResponse<Map<String, Object>>> getPlatformStats();
}