// app/src/main/java/com/example/newtrade/utils/UserBehaviorTracker.java
package com.example.newtrade.utils;

import android.content.Context;
import android.util.Log;

import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserBehaviorTracker {

    private static final String TAG = "UserBehaviorTracker";
    private static UserBehaviorTracker instance;

    private final Context context;
    private final SharedPrefsManager prefsManager;

    private UserBehaviorTracker(Context context) {
        this.context = context.getApplicationContext();
        this.prefsManager = SharedPrefsManager.getInstance(context);
    }

    public static synchronized UserBehaviorTracker getInstance(Context context) {
        if (instance == null) {
            instance = new UserBehaviorTracker(context);
        }
        return instance;
    }

    // ✅ FR-3.2.2: Track browsing history for personalized recommendations
    public void trackProductView(Long productId, String categoryName, String searchQuery) {
        if (productId == null || productId <= 0) {
            Log.w(TAG, "Invalid product ID, skipping tracking");
            return;
        }

        // Add to local viewed products
        prefsManager.addToViewedProducts(productId);

        if (categoryName != null) {
            prefsManager.addToBrowsedCategories(categoryName);
        }

        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, only local tracking");
            return;
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "PRODUCT_VIEW");
        behaviorData.put("productId", productId);
        behaviorData.put("categoryName", categoryName);
        behaviorData.put("searchQuery", searchQuery);
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        // Save locally for offline analysis
        saveBehaviorLocally(behaviorData);

        // Send to backend analytics
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked product view: " + productId + " in category: " + categoryName);
    }

    // ✅ Track search patterns for popular search recommendations
    public void trackSearch(String query, String categoryFilter, String locationFilter) {
        if (query != null && !query.trim().isEmpty()) {
            prefsManager.addToSearchHistory(query.trim());
        }

        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, only local search tracking");
            return;
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "SEARCH");
        behaviorData.put("query", query);
        behaviorData.put("categoryFilter", categoryFilter);
        behaviorData.put("locationFilter", locationFilter);
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        saveBehaviorLocally(behaviorData);
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked search: '" + query + "' with filters");
    }

    // ✅ FR-3.2.2: Track location activity for nearby recommendations
    public void trackLocationActivity(String location, Double latitude, Double longitude) {
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping location tracking");
            return;
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "LOCATION_BROWSE");
        behaviorData.put("location", location);
        behaviorData.put("latitude", latitude);
        behaviorData.put("longitude", longitude);
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        saveBehaviorLocally(behaviorData);
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked location activity: " + location);
    }

    // ✅ Track category browsing for category-based recommendations
    public void trackCategoryBrowse(Long categoryId, String categoryName) {
        if (categoryName != null) {
            prefsManager.addToBrowsedCategories(categoryName);
        }

        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, only local category tracking");
            return;
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "CATEGORY_BROWSE");
        behaviorData.put("categoryId", categoryId);
        behaviorData.put("categoryName", categoryName);
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        saveBehaviorLocally(behaviorData);
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked category browse: " + categoryName);
    }

    // ✅ Track recommendation clicks to improve algorithm
    public void trackRecommendationClick(Long productId, String recommendationType) {
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping recommendation click tracking");
            return;
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "RECOMMENDATION_CLICK");
        behaviorData.put("productId", productId);
        behaviorData.put("recommendationType", recommendationType); // "browsing_history", "popular", "nearby"
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        saveBehaviorLocally(behaviorData);
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked recommendation click: " + recommendationType);
    }

    // ✅ Track app launch and session
    public void trackAppLaunch() {
        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "APP_LAUNCH");
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        saveBehaviorLocally(behaviorData);
        if (prefsManager.isLoggedIn()) {
            sendToAnalytics(behaviorData);
        }

        Log.d(TAG, "✅ Tracked app launch");
    }

    // ✅ Local storage for offline analysis and fallback
    private void saveBehaviorLocally(Map<String, Object> behaviorData) {
        try {
            String action = (String) behaviorData.get("action");
            String key = "behavior_" + action.toLowerCase();

            // Store last 20 actions of each type for local recommendations
            List<String> recentActions = prefsManager.getStringList(key);
            if (recentActions.size() >= 20) {
                recentActions.remove(0); // Remove oldest
            }

            // Store compact representation
            String behaviorString = action + "|" +
                    behaviorData.get("productId") + "|" +
                    behaviorData.get("categoryName") + "|" +
                    behaviorData.get("query") + "|" +
                    System.currentTimeMillis();

            recentActions.add(behaviorString);
            prefsManager.saveStringList(key, recentActions);

            Log.d(TAG, "✅ Behavior saved locally: " + action);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving behavior locally", e);
        }
    }

    // ✅ Send to backend analytics using existing API
    private void sendToAnalytics(Map<String, Object> behaviorData) {
        try {
            if (ApiClient.getApiService() == null) {
                Log.w(TAG, "ApiClient not initialized, skipping analytics");
                return;
            }

            ApiClient.getApiService().trackUserBehavior(behaviorData)
                    .enqueue(new Callback<StandardResponse<String>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<String>> call,
                                               Response<StandardResponse<String>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Log.d(TAG, "✅ Behavior sent to analytics successfully");
                            } else {
                                Log.w(TAG, "⚠️ Analytics API response: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                            Log.w(TAG, "⚠️ Analytics API failed, behavior saved locally", t);
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending to analytics", e);
        }
    }

    // ✅ Get local browsing history for recommendations
    public List<String> getViewedCategories() {
        return prefsManager.getBrowsedCategories();
    }

    public List<String> getSearchHistory() {
        return prefsManager.getSearchHistory();
    }

    public List<String> getBrowsedCategories() {
        return prefsManager.getBrowsedCategories();
    }

    public List<Long> getViewedProducts() {
        return prefsManager.getViewedProducts();
    }

    // ✅ Get most viewed category for personalized recommendations
    public String getMostViewedCategory() {
        List<String> categories = getViewedCategories();
        if (!categories.isEmpty()) {
            return categories.get(0); // Most recent category
        }
        return null;
    }

    // ✅ Clear all behavior data
    public void clearBehaviorData() {
        prefsManager.clearRecommendationData();
        Log.d(TAG, "✅ All behavior data cleared");
    }

    // ✅ Get behavior statistics
    public Map<String, Integer> getBehaviorStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("viewedProducts", getViewedProducts().size());
        stats.put("searchQueries", getSearchHistory().size());
        stats.put("browsedCategories", getBrowsedCategories().size());

        List<String> productViews = prefsManager.getStringList("behavior_product_view");
        List<String> searches = prefsManager.getStringList("behavior_search");
        List<String> categoryBrowses = prefsManager.getStringList("behavior_category_browse");

        stats.put("totalProductViews", productViews.size());
        stats.put("totalSearches", searches.size());
        stats.put("totalCategoryBrowses", categoryBrowses.size());

        return stats;
    }
}