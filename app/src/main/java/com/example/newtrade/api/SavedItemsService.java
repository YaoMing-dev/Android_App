// app/src/main/java/com/example/newtrade/api/SavedItemsService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SavedItemsService {

    // Save an item
    @POST("api/saved-items/{productId}")
    Call<StandardResponse<String>> saveItem(@Path("productId") Long productId);

    // Remove saved item
    @DELETE("api/saved-items/{productId}")
    Call<StandardResponse<String>> removeSavedItem(@Path("productId") Long productId);

    // Get saved items with pagination
    @GET("api/saved-items")
    Call<StandardResponse<Map<String, Object>>> getSavedItems(
            @Query("page") int page,
            @Query("size") int size
    );

    // Check if item is saved
    @GET("api/saved-items/{productId}/is-saved")
    Call<StandardResponse<Map<String, Object>>> isItemSaved(@Path("productId") Long productId);

    // Get saved items count
    @GET("api/saved-items/count")
    Call<StandardResponse<Map<String, Object>>> getSavedItemsCount();
}