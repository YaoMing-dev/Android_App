// app/src/main/java/com/example/newtrade/api/ApiService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // ===== AUTHENTICATION =====
    @POST("api/auth/google-signin")
    Call<StandardResponse<Map<String, Object>>> googleSignIn(@Body Map<String, String> request);

    @POST("api/auth/logout")
    Call<StandardResponse<String>> logout();

    // ===== CATEGORIES =====
    @GET("api/categories")
    Call<StandardResponse<List<Map<String, Object>>>> getCategories();

    // ===== PRODUCTS =====
    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search,
            @Query("category") String category
    );

    @GET("api/products")
    Call<StandardResponse<Map<String, Object>>> getProducts();

    // ===== HEALTH CHECK =====
    @GET("/")
    Call<StandardResponse<String>> healthCheck();
}