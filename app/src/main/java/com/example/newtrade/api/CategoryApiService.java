// app/src/main/java/com/example/newtrade/api/CategoryApiService.java
package com.example.newtrade.api;

import com.example.newtrade.models.Category;
import com.example.newtrade.models.StandardResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CategoryApiService {

    /**
     * Get all active categories
     */
    @GET("categories")
    Call<StandardResponse<List<Category>>> getAllActiveCategories();

    /**
     * Get category by ID
     */
    @GET("categories/{id}")
    Call<StandardResponse<Category>> getCategory(@Path("id") Long id);

    /**
     * Get all categories (including inactive)
     */
    @GET("categories/all")
    Call<StandardResponse<List<Category>>> getAllCategories();
}