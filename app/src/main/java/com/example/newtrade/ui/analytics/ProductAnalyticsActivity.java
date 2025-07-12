// app/src/main/java/com/example/newtrade/ui/analytics/ProductAnalyticsActivity.java
package com.example.newtrade.ui.analytics;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.AnalyticsService;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.UserBehaviorTracker;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAnalyticsActivity extends AppCompatActivity {

    private static final String TAG = "ProductAnalyticsActivity";

    // ===== UI COMPONENTS =====
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;

    // Analytics Cards - ✅ CLEANED: Removed unused cardOverview reference
    private CardView cardInteractions;
    private CardView cardTrends;
    private CardView cardPerformance;

    // Overview Stats
    private TextView tvTotalViews;
    private TextView tvTotalSaves;
    private TextView tvTotalOffers;
    private TextView tvTotalChats;
    private TextView tvConversionRate;
    private TextView tvPerformanceScore;

    // Period Stats - ✅ FIXED: Added missing views
    private TextView tvViewsToday;
    private TextView tvViewsWeek;
    private TextView tvViewsMonth;
    private TextView tvSavesToday;
    private TextView tvSavesWeek;
    private TextView tvOffersToday;
    private TextView tvOffersWeek;

    // Charts
    private LineChart chartViewTrends;
    private PieChart chartInteractionBreakdown;

    // ===== DATA =====
    private Long productId;
    private String productTitle;
    private AnalyticsService analyticsService;
    private SharedPrefsManager prefsManager;
    private UserBehaviorTracker behaviorTracker;

    private Map<String, Object> analyticsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_analytics);

        // Initialize
        getIntentData();
        initViews();
        initServices();
        setupToolbar();
        setupListeners();

        // Track analytics view
        behaviorTracker.trackAnalyticsView(productId, "DETAILED", "OPENED");

        // Load analytics data
        loadAnalyticsData();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        productId = intent.getLongExtra("product_id", 0L);
        productTitle = intent.getStringExtra("product_title");

        if (productId <= 0) {
            Log.e(TAG, "❌ Invalid product ID");
            finish();
            return;
        }

        Log.d(TAG, "📊 Analytics for Product ID: " + productId + ", Title: " + productTitle);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);

        // Analytics Cards - ✅ CLEANED: Only cards that are actually used
        cardInteractions = findViewById(R.id.card_interactions);
        cardTrends = findViewById(R.id.card_trends);
        cardPerformance = findViewById(R.id.card_performance);

        // Overview Stats
        tvTotalViews = findViewById(R.id.tv_total_views);
        tvTotalSaves = findViewById(R.id.tv_total_saves);
        tvTotalOffers = findViewById(R.id.tv_total_offers);
        tvTotalChats = findViewById(R.id.tv_total_chats);
        tvConversionRate = findViewById(R.id.tv_conversion_rate);
        tvPerformanceScore = findViewById(R.id.tv_performance_score);

        // Period Stats - ✅ FIXED: All views now properly referenced
        tvViewsToday = findViewById(R.id.tv_views_today);
        tvViewsWeek = findViewById(R.id.tv_views_week);
        tvViewsMonth = findViewById(R.id.tv_views_month);
        tvSavesToday = findViewById(R.id.tv_saves_today);
        tvSavesWeek = findViewById(R.id.tv_saves_week);
        tvOffersToday = findViewById(R.id.tv_offers_today);
        tvOffersWeek = findViewById(R.id.tv_offers_week);

        // Charts
        chartViewTrends = findViewById(R.id.chart_view_trends);
        chartInteractionBreakdown = findViewById(R.id.chart_interaction_breakdown);

        Log.d(TAG, "✅ Views initialized");
    }

    private void initServices() {
        analyticsService = ApiClient.getAnalyticsService();
        prefsManager = SharedPrefsManager.getInstance(this);
        behaviorTracker = UserBehaviorTracker.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Analytics");

            if (productTitle != null && !productTitle.isEmpty()) {
                getSupportActionBar().setSubtitle(productTitle);
            }
        }
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadAnalyticsData);

        // Card click listeners for detailed views
        cardInteractions.setOnClickListener(v -> showInteractionDetails());
        cardTrends.setOnClickListener(v -> showTrendsDetails());
        cardPerformance.setOnClickListener(v -> showPerformanceDetails());
    }

    // ===== ANALYTICS DATA LOADING =====

    private void loadAnalyticsData() {
        showLoading(true);

        analyticsService.getProductStats(productId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                           @NonNull Response<StandardResponse<Map<String, Object>>> response) {

                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> standardResponse = response.body();
                            if (standardResponse.isSuccess()) {
                                analyticsData = standardResponse.getData();
                                updateAnalyticsUI();
                                setupCharts();
                                Log.d(TAG, "✅ Analytics data loaded successfully");
                            } else {
                                showError("Failed to load analytics: " + standardResponse.getMessage());
                            }
                        } else {
                            showError("Analytics service unavailable");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "❌ Failed to load analytics", t);
                        showError("Network error loading analytics");

                        // Show mock data for development
                        showMockAnalytics();
                    }
                });
    }

    // ===== UI UPDATES =====

    private void updateAnalyticsUI() {
        if (analyticsData == null) {
            Log.w(TAG, "⚠️ No analytics data available");
            return;
        }

        try {
            // Overview Stats
            updateTextView(tvTotalViews, analyticsData.get("viewCount"), "0");
            updateTextView(tvTotalSaves, analyticsData.get("saveCount"), "0");
            updateTextView(tvTotalOffers, analyticsData.get("offerCount"), "0");
            updateTextView(tvTotalChats, analyticsData.get("conversationCount"), "0");

            // Calculate conversion rate (offers/views * 100)
            int views = getIntValue(analyticsData.get("viewCount"));
            int offers = getIntValue(analyticsData.get("offerCount"));
            double conversionRate = views > 0 ? (double) offers / views * 100 : 0;
            tvConversionRate.setText(String.format("%.1f%%", conversionRate));

            // Calculate performance score (weighted average)
            int saves = getIntValue(analyticsData.get("saveCount"));
            int chats = getIntValue(analyticsData.get("conversationCount"));

            double performanceScore = calculatePerformanceScore(views, saves, offers, chats);
            tvPerformanceScore.setText(String.format("%.0f/100", performanceScore));

            // Period-specific stats (mock for now - implement with time-series data)
            updatePeriodStats();

            Log.d(TAG, "✅ Analytics UI updated");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating analytics UI", e);
            showError("Error displaying analytics data");
        }
    }

    private void updatePeriodStats() {
        // Mock period data - in real implementation, get from time-series analytics API
        int totalViews = getIntValue(analyticsData.get("viewCount"));
        int totalSaves = getIntValue(analyticsData.get("saveCount"));
        int totalOffers = getIntValue(analyticsData.get("offerCount"));

        // Simulate daily/weekly distribution
        tvViewsToday.setText(String.valueOf(Math.max(1, totalViews / 10)));
        tvViewsWeek.setText(String.valueOf(Math.max(1, totalViews / 3)));
        tvViewsMonth.setText(String.valueOf(totalViews));

        tvSavesToday.setText(String.valueOf(Math.max(0, totalSaves / 15)));
        tvSavesWeek.setText(String.valueOf(Math.max(0, totalSaves / 4)));

        tvOffersToday.setText(String.valueOf(Math.max(0, totalOffers / 20)));
        tvOffersWeek.setText(String.valueOf(Math.max(0, totalOffers / 5)));
    }

    // ===== CHARTS SETUP - ✅ FIXED: Compatible with MPAndroidChart v3.1.0 =====

    private void setupCharts() {
        setupViewTrendsChart();
        setupInteractionBreakdownChart();
    }

    private void setupViewTrendsChart() {
        // Mock trend data - replace with real time-series data
        List<Entry> entries = new ArrayList<>();
        int baseViews = getIntValue(analyticsData.get("viewCount"));

        // Generate mock 7-day trend
        for (int i = 0; i < 7; i++) {
            float value = (float) (baseViews * (0.1 + Math.random() * 0.3));
            entries.add(new Entry(i, value));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Daily Views");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E3F2FD"));

        LineData lineData = new LineData(dataSet);
        chartViewTrends.setData(lineData);

        // Customize chart
        Description desc = new Description();
        desc.setText("Views over the last 7 days");
        chartViewTrends.setDescription(desc);
        chartViewTrends.getXAxis().setGranularity(1f);
        chartViewTrends.animateX(1000);
        chartViewTrends.invalidate();
    }

    private void setupInteractionBreakdownChart() {
        List<PieEntry> entries = new ArrayList<>();

        int views = getIntValue(analyticsData.get("viewCount"));
        int saves = getIntValue(analyticsData.get("saveCount"));
        int offers = getIntValue(analyticsData.get("offerCount"));
        int chats = getIntValue(analyticsData.get("conversationCount"));

        if (views > 0) entries.add(new PieEntry(views, "Views"));
        if (saves > 0) entries.add(new PieEntry(saves, "Saves"));
        if (offers > 0) entries.add(new PieEntry(offers, "Offers"));
        if (chats > 0) entries.add(new PieEntry(chats, "Chats"));

        PieDataSet dataSet = new PieDataSet(entries, "Interactions");
        dataSet.setColors(new int[]{
                Color.parseColor("#2196F3"), // Views
                Color.parseColor("#4CAF50"), // Saves
                Color.parseColor("#FF9800"), // Offers
                Color.parseColor("#9C27B0")  // Chats
        });

        PieData pieData = new PieData(dataSet);
        chartInteractionBreakdown.setData(pieData);

        // ✅ FIXED: Compatible chart customization
        chartInteractionBreakdown.getDescription().setEnabled(false);

        // ✅ FIXED: Use proper Legend methods for v3.1.0
        Legend legend = chartInteractionBreakdown.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        chartInteractionBreakdown.animateY(1000);
        chartInteractionBreakdown.invalidate();
    }

    // ===== UTILITY METHODS =====

    private void updateTextView(TextView textView, Object value, String defaultValue) {
        if (textView != null) {
            String displayValue = value != null ? value.toString() : defaultValue;
            textView.setText(displayValue);
        }
    }

    private int getIntValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private double calculatePerformanceScore(int views, int saves, int offers, int chats) {
        if (views == 0) return 0;

        // Weighted scoring: saves(30%) + offers(40%) + chats(30%)
        double saveScore = Math.min(100, (double) saves / views * 100 * 10); // Max 10% save rate = 100 points
        double offerScore = Math.min(100, (double) offers / views * 100 * 20); // Max 5% offer rate = 100 points
        double chatScore = Math.min(100, (double) chats / views * 100 * 15); // Max 6.7% chat rate = 100 points

        return (saveScore * 0.3) + (offerScore * 0.4) + (chatScore * 0.3);
    }

    private void showMockAnalytics() {
        // Mock data for development/testing
        analyticsData = new java.util.HashMap<>();
        analyticsData.put("viewCount", 156);
        analyticsData.put("saveCount", 23);
        analyticsData.put("offerCount", 8);
        analyticsData.put("conversationCount", 12);

        updateAnalyticsUI();
        setupCharts();

        Toast.makeText(this, "📊 Showing mock analytics data", Toast.LENGTH_SHORT).show();
    }

    // ===== DETAILED VIEWS =====

    private void showInteractionDetails() {
        behaviorTracker.trackAnalyticsView(productId, "INTERACTIONS", "VIEWED");

        String details = String.format(
                "📊 Interaction Details\n\n" +
                        "👁️ Total Views: %s\n" +
                        "💾 Saves: %s (%.1f%% of views)\n" +
                        "💰 Offers: %s (%.1f%% of views)\n" +
                        "💬 Chats: %s (%.1f%% of views)\n\n" +
                        "💡 Tips:\n" +
                        "• Good save rate: >3%%\n" +
                        "• Good offer rate: >2%%\n" +
                        "• Good chat rate: >5%%",

                analyticsData.get("viewCount"),
                analyticsData.get("saveCount"),
                getPercentage(analyticsData.get("saveCount"), analyticsData.get("viewCount")),
                analyticsData.get("offerCount"),
                getPercentage(analyticsData.get("offerCount"), analyticsData.get("viewCount")),
                analyticsData.get("conversationCount"),
                getPercentage(analyticsData.get("conversationCount"), analyticsData.get("viewCount"))
        );

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Interaction Analysis")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showTrendsDetails() {
        behaviorTracker.trackAnalyticsView(productId, "TRENDS", "VIEWED");

        String trends = "📈 Trends Analysis\n\n" +
                "📅 This Week: Higher activity\n" +
                "📅 Last Week: Normal activity\n" +
                "🎯 Peak Time: Evenings\n" +
                "📱 Most Views: Mobile app\n\n" +
                "📊 Recommendations:\n" +
                "• Update photos for better engagement\n" +
                "• Consider adjusting price\n" +
                "• Post similar items in evening";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Trends Analysis")
                .setMessage(trends)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showPerformanceDetails() {
        behaviorTracker.trackAnalyticsView(productId, "PERFORMANCE", "VIEWED");

        double score = calculatePerformanceScore(
                getIntValue(analyticsData.get("viewCount")),
                getIntValue(analyticsData.get("saveCount")),
                getIntValue(analyticsData.get("offerCount")),
                getIntValue(analyticsData.get("conversationCount"))
        );

        String performance = String.format(
                "🎯 Performance Score: %.0f/100\n\n" +
                        "📊 Breakdown:\n" +
                        "• Save Rate: %.1f%% (%s)\n" +
                        "• Offer Rate: %.1f%% (%s)\n" +
                        "• Chat Rate: %.1f%% (%s)\n\n" +
                        "💡 Overall: %s",

                score,
                getPercentage(analyticsData.get("saveCount"), analyticsData.get("viewCount")),
                getScoreDescription(getPercentage(analyticsData.get("saveCount"), analyticsData.get("viewCount")), 3.0),
                getPercentage(analyticsData.get("offerCount"), analyticsData.get("viewCount")),
                getScoreDescription(getPercentage(analyticsData.get("offerCount"), analyticsData.get("viewCount")), 2.0),
                getPercentage(analyticsData.get("conversationCount"), analyticsData.get("viewCount")),
                getScoreDescription(getPercentage(analyticsData.get("conversationCount"), analyticsData.get("viewCount")), 5.0),
                getOverallPerformanceDescription(score)
        );

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Performance Analysis")
                .setMessage(performance)
                .setPositiveButton("OK", null)
                .show();
    }

    private double getPercentage(Object numerator, Object denominator) {
        int num = getIntValue(numerator);
        int den = getIntValue(denominator);
        return den > 0 ? (double) num / den * 100 : 0;
    }

    private String getScoreDescription(double percentage, double benchmark) {
        if (percentage >= benchmark) return "Excellent ✅";
        else if (percentage >= benchmark * 0.7) return "Good 👍";
        else if (percentage >= benchmark * 0.4) return "Average 📊";
        else return "Needs Improvement 📈";
    }

    private String getOverallPerformanceDescription(double score) {
        if (score >= 80) return "Excellent listing performance! 🌟";
        else if (score >= 60) return "Good performance with room for improvement 👍";
        else if (score >= 40) return "Average performance - consider optimizing 📊";
        else return "Low performance - needs significant improvement 📈";
    }

    // ===== UI STATE MANAGEMENT =====

    private void showLoading(boolean show) {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(show);
        }
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: " + message);
    }

    // ===== MENU HANDLING =====

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Track analytics session end
        if (behaviorTracker != null && productId != null) {
            behaviorTracker.trackAnalyticsView(productId, "DETAILED", "CLOSED");
        }
    }
}