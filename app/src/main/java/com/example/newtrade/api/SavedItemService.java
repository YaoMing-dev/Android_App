// app/src/main/java/com/example/newtrade/api/SavedItemService.java
package com.example.newtrade.api;

import com.example.newtrade.models.StandardResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SavedItemService {

    @POST("/api/saved-items/{productId}")
    Call<StandardResponse<Map<String, Object>>> saveItem(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @DELETE("/api/saved-items/{productId}")
    Call<StandardResponse<Map<String, Object>>> removeSavedItem(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @GET("/api/saved-items")
    Call<StandardResponse<Map<String, Object>>> getSavedItems(
            @Header("User-ID") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/saved-items/{productId}/is-saved")
    Call<StandardResponse<Map<String, Object>>> isItemSaved(
            @Header("User-ID") Long userId,
            @Path("productId") Long productId);

    @GET("/api/saved-items/count")
    Call<StandardResponse<Map<String, Object>>> getSavedItemsCount(
            @Header("User-ID") Long userId);
}