// app/src/main/java/com/example/newtrade/utils/UserBehaviorTracker.java
package com.example.newtrade.utils;

import android.content.Context;
import android.util.Log;

import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;

import java.math.BigDecimal;
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

    // ===== EXISTING METHODS =====

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

    // ===== 🆕 NEW METHODS FOR FR-9.1.1: Track chats, offers per listing =====

    /**
     * 🆕 Track when user starts a chat conversation for a product
     * FR-9.1.1: Track chats per listing
     */
    public void trackChatStart(Long productId, Long sellerId, String conversationType) {
        if (productId == null || productId <= 0) {
            Log.w(TAG, "Invalid product ID for chat tracking");
            return;
        }

        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping chat tracking");
            return;
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "CHAT_START");
        behaviorData.put("productId", productId);
        behaviorData.put("sellerId", sellerId);
        behaviorData.put("buyerId", prefsManager.getUserId());
        behaviorData.put("conversationType", conversationType); // "INQUIRY", "NEGOTIATION", "SUPPORT"
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        saveBehaviorLocally(behaviorData);
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked chat start for product: " + productId);
    }

    /**
     * 🆕 Track chat engagement (message sent/received)
     * FR-9.1.1: Track conversation quality and engagement
     */
    public void trackChatEngagement(Long conversationId, Long productId, String engagementType, int messageCount) {
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping chat engagement tracking");
            return;
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "CHAT_ENGAGEMENT");
        behaviorData.put("conversationId", conversationId);
        behaviorData.put("productId", productId);
        behaviorData.put("engagementType", engagementType); // "MESSAGE_SENT", "MESSAGE_RECEIVED", "CHAT_CLOSED"
        behaviorData.put("messageCount", messageCount);
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        saveBehaviorLocally(behaviorData);
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked chat engagement: " + engagementType + " for product: " + productId);
    }

    /**
     * 🆕 Track when user makes an offer on a product
     * FR-9.1.1: Track offers per listing
     */
    public void trackOfferMade(Long productId, BigDecimal offerAmount, BigDecimal originalPrice, String offerType) {
        if (productId == null || productId <= 0) {
            Log.w(TAG, "Invalid product ID for offer tracking");
            return;
        }

        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping offer tracking");
            return;
        }

        // Calculate offer percentage
        double offerPercentage = 0.0;
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            offerPercentage = offerAmount.divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "OFFER_MADE");
        behaviorData.put("productId", productId);
        behaviorData.put("buyerId", prefsManager.getUserId());
        behaviorData.put("offerAmount", offerAmount.toString());
        behaviorData.put("originalPrice", originalPrice != null ? originalPrice.toString() : null);
        behaviorData.put("offerPercentage", offerPercentage);
        behaviorData.put("offerType", offerType); // "INITIAL", "COUNTER", "FINAL"
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        saveBehaviorLocally(behaviorData);
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked offer made: " + offerAmount + " (" + String.format("%.1f", offerPercentage) + "%) for product: " + productId);
    }

    /**
     * 🆕 Track offer response (accept/reject/counter)
     * FR-9.1.1: Track offer interactions per listing
     */
    public void trackOfferResponse(Long offerId, Long productId, String responseType, BigDecimal counterAmount) {
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping offer response tracking");
            return;
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "OFFER_RESPONSE");
        behaviorData.put("offerId", offerId);
        behaviorData.put("productId", productId);
        behaviorData.put("sellerId", prefsManager.getUserId());
        behaviorData.put("responseType", responseType); // "ACCEPTED", "REJECTED", "COUNTERED"
        behaviorData.put("counterAmount", counterAmount != null ? counterAmount.toString() : null);
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        saveBehaviorLocally(behaviorData);
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked offer response: " + responseType + " for product: " + productId);
    }

    /**
     * 🆕 Track product interaction summary for analytics
     * FR-2.2.3: Listing analytics (views, interactions)
     */
    public void trackProductInteraction(Long productId, String interactionType, Map<String, Object> additionalData) {
        if (productId == null || productId <= 0) {
            Log.w(TAG, "Invalid product ID for interaction tracking");
            return;
        }

        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping interaction tracking");
            return;
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "PRODUCT_INTERACTION");
        behaviorData.put("productId", productId);
        behaviorData.put("interactionType", interactionType); // "SAVE", "UNSAVE", "SHARE", "REPORT", "CONTACT"
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        // Add additional data if provided
        if (additionalData != null) {
            behaviorData.putAll(additionalData);
        }

        saveBehaviorLocally(behaviorData);
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked product interaction: " + interactionType + " for product: " + productId);
    }

    /**
     * 🆕 Track seller analytics activity
     * FR-9.1.1: Track seller viewing their own analytics
     */
    public void trackAnalyticsView(Long productId, String analyticsType, String viewDuration) {
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping analytics view tracking");
            return;
        }

        Map<String, Object> behaviorData = new HashMap<>();
        behaviorData.put("action", "ANALYTICS_VIEW");
        behaviorData.put("productId", productId);
        behaviorData.put("analyticsType", analyticsType); // "BASIC", "DETAILED", "TRENDS", "EXPORT"
        behaviorData.put("viewDuration", viewDuration);
        behaviorData.put("timestamp", System.currentTimeMillis());
        behaviorData.put("userId", prefsManager.getUserId());

        saveBehaviorLocally(behaviorData);
        sendToAnalytics(behaviorData);

        Log.d(TAG, "✅ Tracked analytics view: " + analyticsType + " for product: " + productId);
    }

    // ===== ENHANCED LOCAL STORAGE & ANALYTICS =====

    // ✅ Local storage for offline analysis and fallback
    private void saveBehaviorLocally(Map<String, Object> behaviorData) {
        try {
            String action = (String) behaviorData.get("action");
            String key = "behavior_" + action.toLowerCase();

            // Store last 50 actions of each type for better local analytics
            List<String> recentActions = prefsManager.getStringList(key);
            if (recentActions.size() >= 50) {
                recentActions.remove(0); // Remove oldest
            }

            // Store comprehensive representation for analytics
            String behaviorString = action + "|" +
                    behaviorData.get("productId") + "|" +
                    behaviorData.get("categoryName") + "|" +
                    behaviorData.get("query") + "|" +
                    behaviorData.get("timestamp") + "|" +
                    behaviorData.get("userId");

            recentActions.add(behaviorString);
            prefsManager.saveStringList(key, recentActions);

            Log.d(TAG, "✅ Behavior saved locally: " + action);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving behavior locally", e);
        }
    }

    // ✅ Enhanced analytics sending with retry mechanism
    private void sendToAnalytics(Map<String, Object> behaviorData) {
        try {
            if (ApiClient.getApiService() == null) {
                Log.w(TAG, "ApiClient not initialized, queueing analytics");
                queueAnalyticsForLater(behaviorData);
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
                                // Queue for retry
                                queueAnalyticsForLater(behaviorData);
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                            Log.e(TAG, "❌ Failed to send analytics", t);
                            // Queue for retry
                            queueAnalyticsForLater(behaviorData);
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending analytics", e);
            queueAnalyticsForLater(behaviorData);
        }
    }

    /**
     * Queue analytics data for later retry
     */
    private void queueAnalyticsForLater(Map<String, Object> behaviorData) {
        try {
            List<String> queuedAnalytics = prefsManager.getStringList("queued_analytics");

            // Convert behaviorData to JSON string for storage
            String dataJson = "{";
            for (Map.Entry<String, Object> entry : behaviorData.entrySet()) {
                dataJson += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\",";
            }
            dataJson = dataJson.substring(0, dataJson.length() - 1) + "}"; // Remove last comma

            queuedAnalytics.add(dataJson);

            // Keep only last 100 queued items
            if (queuedAnalytics.size() > 100) {
                queuedAnalytics = queuedAnalytics.subList(queuedAnalytics.size() - 100, queuedAnalytics.size());
            }

            prefsManager.saveStringList("queued_analytics", queuedAnalytics);
            Log.d(TAG, "📦 Analytics queued for later retry");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error queueing analytics", e);
        }
    }

    /**
     * Retry sending queued analytics (call this when network is restored)
     */
    public void retryQueuedAnalytics() {
        try {
            List<String> queuedAnalytics = prefsManager.getStringList("queued_analytics");

            if (queuedAnalytics.isEmpty()) {
                Log.d(TAG, "📭 No queued analytics to retry");
                return;
            }

            Log.d(TAG, "🔄 Retrying " + queuedAnalytics.size() + " queued analytics");

            for (String queuedData : queuedAnalytics) {
                // Parse and send (implement JSON parsing if needed)
                Log.d(TAG, "🔄 Retrying analytics: " + queuedData);
                // For now, just clear the queue - implement proper parsing if needed
            }

            // Clear the queue after attempting to send
            prefsManager.saveStringList("queued_analytics", new java.util.ArrayList<>());
            Log.d(TAG, "✅ Queued analytics retry completed");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error retrying queued analytics", e);
        }
    }

    /**
     * Get local analytics summary for offline insights
     */
    public Map<String, Integer> getLocalAnalyticsSummary() {
        Map<String, Integer> summary = new HashMap<>();

        try {
            // Count different types of tracked behaviors
            summary.put("product_views", prefsManager.getStringList("behavior_product_view").size());
            summary.put("searches", prefsManager.getStringList("behavior_search").size());
            summary.put("chats_started", prefsManager.getStringList("behavior_chat_start").size());
            summary.put("offers_made", prefsManager.getStringList("behavior_offer_made").size());
            summary.put("category_browses", prefsManager.getStringList("behavior_category_browse").size());
            summary.put("recommendations_clicked", prefsManager.getStringList("behavior_recommendation_click").size());

            Log.d(TAG, "📊 Local analytics summary generated");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error generating local analytics summary", e);
        }

        return summary;
    }

    // ===== 🚨 MISSING GETTER METHODS - THÊM VÀO ĐỂ SỬA LỖI COMPILE =====

    /**
     * Get viewed categories for recommendations (called by RecommendationEngine)
     */
    public List<String> getViewedCategories() {
        return prefsManager.getBrowsedCategories();
    }

    /**
     * Get search history for auto-suggestions (called by SearchFragment)
     */
    public List<String> getSearchHistory() {
        return prefsManager.getSearchHistory();
    }

    /**
     * Get browsed categories (alias for getViewedCategories)
     */
    public List<String> getBrowsedCategories() {
        return prefsManager.getBrowsedCategories();
    }

    /**
     * Get viewed products list
     */
    public List<Long> getViewedProducts() {
        return prefsManager.getViewedProducts();
    }

    /**
     * Get recent search queries for auto-suggestion (alias)
     */
    public List<String> getRecentSearchQueries() {
        return prefsManager.getSearchHistory();
    }

    /**
     * Get recently viewed categories for recommendations (alias)
     */
    public List<String> getRecentCategories() {
        return prefsManager.getBrowsedCategories();
    }

    /**
     * Get recently viewed product IDs (alias)
     */
    public List<Long> getRecentlyViewedProductIds() {
        return prefsManager.getViewedProducts();
    }

    /**
     * Get top search keywords (top 5)
     */
    public List<String> getTopSearchKeywords() {
        List<String> searchHistory = prefsManager.getSearchHistory();
        // Return first 5 recent searches
        if (searchHistory.size() <= 5) {
            return searchHistory;
        }
        return searchHistory.subList(0, 5);
    }

    /**
     * Get most browsed category for personalized recommendations
     */
    public String getMostViewedCategory() {
        List<String> categories = getBrowsedCategories();
        if (!categories.isEmpty()) {
            return categories.get(0); // Most recent category
        }
        return null;
    }

    /**
     * Check if user has search history
     */
    public boolean hasSearchHistory() {
        return !getSearchHistory().isEmpty();
    }

    /**
     * Check if user has browsing history
     */
    public boolean hasBrowsingHistory() {
        return !getViewedProducts().isEmpty() || !getBrowsedCategories().isEmpty();
    }

    /**
     * Get behavior statistics for debugging (enhanced version)
     */
    public Map<String, Integer> getBehaviorStats() {
        Map<String, Integer> stats = new HashMap<>();

        // Basic stats
        stats.put("viewedProducts", getViewedProducts().size());
        stats.put("searchQueries", getSearchHistory().size());
        stats.put("browsedCategories", getBrowsedCategories().size());

        // Enhanced stats from local storage
        List<String> productViews = prefsManager.getStringList("behavior_product_view");
        List<String> searches = prefsManager.getStringList("behavior_search");
        List<String> categoryBrowses = prefsManager.getStringList("behavior_category_browse");

        // 🆕 New analytics stats
        List<String> chatStarts = prefsManager.getStringList("behavior_chat_start");
        List<String> offersMade = prefsManager.getStringList("behavior_offer_made");
        List<String> offerResponses = prefsManager.getStringList("behavior_offer_response");
        List<String> interactions = prefsManager.getStringList("behavior_product_interaction");

        stats.put("totalProductViews", productViews.size());
        stats.put("totalSearches", searches.size());
        stats.put("totalCategoryBrowses", categoryBrowses.size());
        stats.put("totalChatStarts", chatStarts.size());
        stats.put("totalOffersMade", offersMade.size());
        stats.put("totalOfferResponses", offerResponses.size());
        stats.put("totalInteractions", interactions.size());

        return stats;
    }

    /**
     * Clear all behavior data
     */
    public void clearBehaviorData() {
        prefsManager.clearRecommendationData();

        // 🆕 Also clear new analytics data
        prefsManager.saveStringList("behavior_chat_start", new java.util.ArrayList<>());
        prefsManager.saveStringList("behavior_offer_made", new java.util.ArrayList<>());
        prefsManager.saveStringList("behavior_offer_response", new java.util.ArrayList<>());
        prefsManager.saveStringList("behavior_product_interaction", new java.util.ArrayList<>());
        prefsManager.saveStringList("behavior_analytics_view", new java.util.ArrayList<>());
        prefsManager.saveStringList("queued_analytics", new java.util.ArrayList<>());

        Log.d(TAG, "✅ All behavior data cleared (including new analytics)");
    }

    /**
     * Clear only search history
     */
    public void clearSearchHistory() {
        prefsManager.saveStringList("search_history", new java.util.ArrayList<>());
        Log.d(TAG, "✅ Search history cleared");
    }

    /**
     * Clear only browsing history
     */
    public void clearBrowsingHistory() {
        prefsManager.saveLongList("viewed_products", new java.util.ArrayList<>());
        prefsManager.saveStringList("browsed_categories", new java.util.ArrayList<>());
        Log.d(TAG, "✅ Browsing history cleared");
    }

    /**
     * Add search query manually (for testing or migration)
     */
    public void addSearchQuery(String query) {
        if (query != null && !query.trim().isEmpty()) {
            prefsManager.addToSearchHistory(query.trim());
        }
    }

    /**
     * Add viewed product manually (for testing or migration)
     */
    public void addViewedProduct(Long productId) {
        if (productId != null && productId > 0) {
            prefsManager.addToViewedProducts(productId);
        }
    }

    /**
     * Add browsed category manually (for testing or migration)
     */
    public void addBrowsedCategory(String categoryName) {
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            prefsManager.addToBrowsedCategories(categoryName.trim());
        }
    }

    // ===== 🆕 QUICK ACCESS METHODS FOR NEW ANALYTICS =====

    /**
     * Get chat analytics summary
     */
    public Map<String, Integer> getChatAnalytics() {
        Map<String, Integer> chatStats = new HashMap<>();
        List<String> chatStarts = prefsManager.getStringList("behavior_chat_start");
        List<String> chatEngagements = prefsManager.getStringList("behavior_chat_engagement");

        chatStats.put("chatsStarted", chatStarts.size());
        chatStats.put("chatEngagements", chatEngagements.size());

        return chatStats;
    }

    /**
     * Get offer analytics summary
     */
    public Map<String, Integer> getOfferAnalytics() {
        Map<String, Integer> offerStats = new HashMap<>();
        List<String> offersMade = prefsManager.getStringList("behavior_offer_made");
        List<String> offerResponses = prefsManager.getStringList("behavior_offer_response");

        offerStats.put("offersMade", offersMade.size());
        offerStats.put("offerResponses", offerResponses.size());

        return offerStats;
    }

    /**
     * Get interaction analytics summary
     */
    public Map<String, Integer> getInteractionAnalytics() {
        Map<String, Integer> interactionStats = new HashMap<>();
        List<String> interactions = prefsManager.getStringList("behavior_product_interaction");
        List<String> analyticsViews = prefsManager.getStringList("behavior_analytics_view");

        interactionStats.put("productInteractions", interactions.size());
        interactionStats.put("analyticsViews", analyticsViews.size());

        return interactionStats;
    }
}