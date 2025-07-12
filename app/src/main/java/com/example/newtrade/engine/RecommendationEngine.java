// app/src/main/java/com/example/newtrade/engine/RecommendationEngine.java
package com.example.newtrade.engine;

import android.content.Context;
import android.util.Log;

import com.example.newtrade.api.ApiClient;
import com.example.newtrade.interfaces.RecommendationCallback;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.LocationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.UserBehaviorTracker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationEngine {

    private static final String TAG = "RecommendationEngine";
    private static RecommendationEngine instance;

    private final Context context;
    private final SharedPrefsManager prefsManager;
    private final UserBehaviorTracker behaviorTracker;

    private RecommendationEngine(Context context) {
        this.context = context.getApplicationContext();
        this.prefsManager = SharedPrefsManager.getInstance(context);
        this.behaviorTracker = UserBehaviorTracker.getInstance(context);
    }

    public static synchronized RecommendationEngine getInstance(Context context) {
        if (instance == null) {
            instance = new RecommendationEngine(context);
        }
        return instance;
    }

    // ✅ FR-3.2.2: Browsing history based recommendations
    public void getPersonalizedRecommendations(RecommendationCallback callback) {
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, using popular recommendations");
            getPopularRecommendations(callback);
            return;
        }

        Long userId = prefsManager.getUserId();

        // ✅ Try backend personalized recommendations first
        ApiClient.getApiService().getPersonalizedRecommendations(userId, 10)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            if (productList != null && !productList.isEmpty()) {
                                List<Product> products = parseProductList(productList);
                                callback.onSuccess(products, "✨ For You");
                                Log.d(TAG, "✅ Backend personalized recommendations: " + products.size());
                                return;
                            }
                        }

                        // Fallback to local behavior-based recommendations
                        generateLocalPersonalizedRecommendations(callback);
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.w(TAG, "Backend personalized recommendations failed, using local", t);
                        generateLocalPersonalizedRecommendations(callback);
                    }
                });
    }

    // ✅ Generate personalized recommendations from local behavior data
    private void generateLocalPersonalizedRecommendations(RecommendationCallback callback) {
        // Get user's most viewed categories
        List<String> viewedCategories = behaviorTracker.getViewedCategories();

        if (!viewedCategories.isEmpty()) {
            // Get recommendations based on most viewed category
            String mostViewedCategory = viewedCategories.get(0);
            Long categoryId = getCategoryIdFromName(mostViewedCategory);

            if (categoryId != null) {
                loadProductsByCategory(categoryId, callback, "✨ Based on your interests");
                return;
            }
        }

        // Final fallback to popular recommendations
        getPopularRecommendations(callback);
    }

    // ✅ FR-3.2.2: Popular/trending recommendations
    public void getPopularRecommendations(RecommendationCallback callback) {
        // ✅ Try trending products API first
        ApiClient.getApiService().getTrendingProducts(10)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            if (productList != null && !productList.isEmpty()) {
                                List<Product> products = parseProductList(productList);
                                callback.onSuccess(products, "🔥 Popular right now");
                                Log.d(TAG, "✅ Trending products: " + products.size());
                                return;
                            }
                        }

                        // Fallback to recent products
                        loadRecentProducts(callback, "📅 Recently added");
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.w(TAG, "Trending API failed, loading recent products", t);
                        loadRecentProducts(callback, "📅 Recently added");
                    }
                });
    }

    // ✅ FR-3.2.2: Nearby recommendations
    public void getNearbyRecommendations(double latitude, double longitude, RecommendationCallback callback) {
        // ✅ Try backend nearby API first
        ApiClient.getApiService().getNearbyRecommendations(latitude, longitude, 10.0, 10)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            if (productList != null && !productList.isEmpty()) {
                                List<Product> products = parseProductList(productList);
                                callback.onSuccess(products, "📍 Near you");
                                Log.d(TAG, "✅ Backend nearby recommendations: " + products.size());
                                return;
                            }
                        }

                        // Fallback to client-side filtering
                        filterNearbyProductsLocally(latitude, longitude, callback);
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.w(TAG, "Backend nearby API failed, using local filtering", t);
                        filterNearbyProductsLocally(latitude, longitude, callback);
                    }
                });
    }

    // ✅ Client-side nearby filtering as fallback
    private void filterNearbyProductsLocally(double latitude, double longitude, RecommendationCallback callback) {
        ApiClient.getApiService().getProducts(0, 50, null, null, null, null, null, null, null)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            if (productList != null) {
                                List<Product> nearbyProducts = new ArrayList<>();

                                for (Map<String, Object> productData : productList) {
                                    Product product = parseProductFromData(productData);
                                    if (product != null && isProductNearby(product, latitude, longitude, 10.0)) {
                                        nearbyProducts.add(product);
                                    }
                                }

                                if (!nearbyProducts.isEmpty()) {
                                    callback.onSuccess(nearbyProducts, "📍 Near you");
                                    Log.d(TAG, "✅ Local nearby filtering: " + nearbyProducts.size() + " products");
                                } else {
                                    callback.onError("No products found nearby");
                                }
                            } else {
                                callback.onError("No product data available");
                            }
                        } else {
                            callback.onError("Failed to load nearby products");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "Failed to load products for nearby filtering", t);
                        callback.onError("Network error loading nearby products");
                    }
                });
    }

    // ✅ Category-based recommendations for home sections (FR-3.2.1)
    public void getCategoryRecommendations(Long categoryId, String categoryName, RecommendationCallback callback) {
        // Track category browsing
        behaviorTracker.trackCategoryBrowse(categoryId, categoryName);

        // ✅ Try backend category recommendations first
        ApiClient.getApiService().getCategoryRecommendations(categoryId, 10)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            if (productList != null && !productList.isEmpty()) {
                                List<Product> products = parseProductList(productList);
                                callback.onSuccess(products, categoryName);
                                Log.d(TAG, "✅ Backend category recommendations: " + products.size());
                                return;
                            }
                        }

                        // Fallback to regular category filtering
                        loadProductsByCategory(categoryId, callback, categoryName);
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.w(TAG, "Backend category recommendations failed, using regular filter", t);
                        loadProductsByCategory(categoryId, callback, categoryName);
                    }
                });
    }

    // ✅ Get popular search suggestions
    public void getPopularSearchSuggestions(int limit, PopularSearchCallback callback) {
        ApiClient.getApiService().getPopularSearches(limit)
                .enqueue(new Callback<StandardResponse<List<String>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<List<String>>> call,
                                           Response<StandardResponse<List<String>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<String> suggestions = response.body().getData();
                            if (suggestions != null && !suggestions.isEmpty()) {
                                callback.onSuccess(suggestions);
                                Log.d(TAG, "✅ Popular search suggestions: " + suggestions.size());
                                return;
                            }
                        }

                        // Fallback to local search history
                        List<String> localHistory = behaviorTracker.getSearchHistory();
                        callback.onSuccess(localHistory.subList(0, Math.min(limit, localHistory.size())));
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<List<String>>> call, Throwable t) {
                        Log.w(TAG, "Popular searches API failed, using local history", t);
                        List<String> localHistory = behaviorTracker.getSearchHistory();
                        callback.onSuccess(localHistory.subList(0, Math.min(limit, localHistory.size())));
                    }
                });
    }

    // ===== HELPER METHODS =====

    private void loadProductsByCategory(Long categoryId, RecommendationCallback callback, String title) {
        ApiClient.getApiService().getProducts(0, 10, null, categoryId, null, null, null, null, null)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            List<Product> products = parseProductList(productList);

                            if (!products.isEmpty()) {
                                callback.onSuccess(products, title);
                                Log.d(TAG, "✅ Category products loaded: " + products.size() + " for " + title);
                            } else {
                                callback.onError("No products found in this category");
                            }
                        } else {
                            callback.onError("Failed to load category products");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "Failed to load category products", t);
                        callback.onError("Network error loading category products");
                    }
                });
    }

    private void loadRecentProducts(RecommendationCallback callback, String title) {
        ApiClient.getApiService().getProducts(0, 10, null, null, null, null, null, "createdAt", "desc")
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            List<Product> products = parseProductList(productList);

                            if (!products.isEmpty()) {
                                callback.onSuccess(products, title);
                                Log.d(TAG, "✅ Recent products loaded: " + products.size());
                            } else {
                                callback.onError("No recent products found");
                            }
                        } else {
                            callback.onError("Failed to load recent products");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "Failed to load recent products", t);
                        callback.onError("Network error loading recent products");
                    }
                });
    }

    private List<Product> parseProductList(List<Map<String, Object>> productList) {
        List<Product> products = new ArrayList<>();

        if (productList != null) {
            for (Map<String, Object> productData : productList) {
                Product product = parseProductFromData(productData);
                if (product != null) {
                    products.add(product);
                }
            }
        }

        return products;
    }

    private Product parseProductFromData(Map<String, Object> productData) {
        try {
            Product product = new Product();

            // Basic fields
            if (productData.get("id") instanceof Number) {
                product.setId(((Number) productData.get("id")).longValue());
            }
            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));
            product.setLocation((String) productData.get("location"));

            // Coordinates
            if (productData.get("latitude") instanceof Number) {
                product.setLatitude(((Number) productData.get("latitude")).doubleValue());
            }
            if (productData.get("longitude") instanceof Number) {
                product.setLongitude(((Number) productData.get("longitude")).doubleValue());
            }

            // Price
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }

            // Category info
            if (productData.get("categoryId") instanceof Number) {
                product.setCategoryId(((Number) productData.get("categoryId")).longValue());
            }
            product.setCategoryName((String) productData.get("categoryName"));

            // Images
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) imageUrlsObj;
                product.setImageUrls(imageUrls);
                if (!imageUrls.isEmpty()) {
                    product.setImageUrl(imageUrls.get(0));
                }
            } else if (imageUrlsObj instanceof String) {
                product.setImageUrl((String) imageUrlsObj);
            }

            // Condition
            String condition = (String) productData.get("condition");
            if (condition != null) {
                try {
                    product.setCondition(Product.ProductCondition.valueOf(condition));
                } catch (IllegalArgumentException e) {
                    product.setCondition(Product.ProductCondition.GOOD);
                }
            }

            // Created date
            product.setCreatedAt((String) productData.get("createdAt"));

            return product;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing product from data", e);
            return null;
        }
    }

    private Long getCategoryIdFromName(String categoryName) {
        if (categoryName == null) return null;

        // Map category names to IDs (same as in backend)
        Map<String, Long> categoryMap = new HashMap<>();
        categoryMap.put("Electronics", 1L);
        categoryMap.put("Fashion", 2L);
        categoryMap.put("Home & Garden", 3L);
        categoryMap.put("Sports", 4L);
        categoryMap.put("Books", 5L);
        categoryMap.put("Automotive", 6L);
        categoryMap.put("Beauty", 7L);
        categoryMap.put("Toys", 8L);
        categoryMap.put("Other", 9L);

        return categoryMap.get(categoryName);
    }

    private boolean isProductNearby(Product product, double userLat, double userLng, double radiusKm) {
        if (product.getLatitude() == null || product.getLongitude() == null) {
            return false;
        }

        double distance = LocationUtils.calculateDistance(userLat, userLng, product.getLatitude(), product.getLongitude());
        return distance <= radiusKm;
    }

    // ✅ Interface for popular search suggestions
    public interface PopularSearchCallback {
        void onSuccess(List<String> suggestions);
        void onError(String error);
    }
}