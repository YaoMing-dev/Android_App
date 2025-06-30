// app/src/main/java/com/example/newtrade/ui/product/ProductAnalyticsActivity.java
package com.example.newtrade.ui.product;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAnalyticsActivity extends AppCompatActivity {
    private static final String TAG = "ProductAnalyticsActivity";

    // UI Components
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private MaterialCardView cardViews, cardContacts, cardSaves, cardShares;
    private TextView tvProductTitle, tvTotalViews, tvViewsToday, tvViewsWeek, tvViewsMonth;
    private TextView tvTotalContacts, tvContactsToday, tvContactsWeek, tvContactsMonth;
    private TextView tvTotalSaves, tvSavesToday, tvSavesWeek, tvSavesMonth;
    private TextView tvTotalShares, tvSharesToday, tvSharesWeek, tvSharesMonth;
    private TextView tvCreatedDate, tvLastViewed, tvAverageDaily;

    // Data
    private Long productId;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_analytics);

        productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadAnalytics();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);

        cardViews = findViewById(R.id.card_views);
        cardContacts = findViewById(R.id.card_contacts);
        cardSaves = findViewById(R.id.card_saves);
        cardShares = findViewById(R.id.card_shares);

        tvProductTitle = findViewById(R.id.tv_product_title);

        // Views
        tvTotalViews = findViewById(R.id.tv_total_views);
        tvViewsToday = findViewById(R.id.tv_views_today);
        tvViewsWeek = findViewById(R.id.tv_views_week);
        tvViewsMonth = findViewById(R.id.tv_views_month);

        // Contacts
        tvTotalContacts = findViewById(R.id.tv_total_contacts);
        tvContactsToday = findViewById(R.id.tv_contacts_today);
        tvContactsWeek = findViewById(R.id.tv_contacts_week);
        tvContactsMonth = findViewById(R.id.tv_contacts_month);

        // Saves
        tvTotalSaves = findViewById(R.id.tv_total_saves);
        tvSavesToday = findViewById(R.id.tv_saves_today);
        tvSavesWeek = findViewById(R.id.tv_saves_week);
        tvSavesMonth = findViewById(R.id.tv_saves_month);

        // Shares
        tvTotalShares = findViewById(R.id.tv_total_shares);
        tvSharesToday = findViewById(R.id.tv_shares_today);
        tvSharesWeek = findViewById(R.id.tv_shares_week);
        tvSharesMonth = findViewById(R.id.tv_shares_month);

        // Additional info
        tvCreatedDate = findViewById(R.id.tv_created_date);
        tvLastViewed = findViewById(R.id.tv_last_viewed);
        tvAverageDaily = findViewById(R.id.tv_average_daily);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Product Analytics");
        }
    }

    private void loadAnalytics() {
        setLoading(true);

        // First load product details
        ApiClient.getProductService().getProductById(productId)
                .enqueue(new Callback<StandardResponse<Product>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Product>> call,
                                           Response<StandardResponse<Product>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Product> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                product = apiResponse.getData();
                                displayProductInfo();
                                loadAnalyticsData();
                            } else {
                                setLoading(false);
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            setLoading(false);
                            showError("Failed to load product data");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Product>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Failed to load product", t);
                        showError("Network error");
                    }
                });
    }

    private void displayProductInfo() {
        if (product != null) {
            tvProductTitle.setText(product.getTitle());
            tvCreatedDate.setText("Created: " + formatDate(product.getCreatedAt()));
        }
    }

    private void loadAnalyticsData() {
        // For now, display basic analytics from product data
        // In a real app, you would call a separate analytics API
        displayBasicAnalytics();
        setLoading(false);
    }

    private void displayBasicAnalytics() {
        if (product == null) return;

        // Views
        int totalViews = product.getViewCount() != null ? product.getViewCount() : 0;
        tvTotalViews.setText(String.valueOf(totalViews));

        // Simulate some analytics data (in real app, this would come from backend)
        tvViewsToday.setText(String.valueOf(Math.max(0, totalViews / 30))); // Rough estimate
        tvViewsWeek.setText(String.valueOf(Math.max(0, totalViews / 7)));
        tvViewsMonth.setText(String.valueOf(totalViews));

        // Contacts (simulate based on views)
        int estimatedContacts = Math.max(0, totalViews / 10);
        tvTotalContacts.setText(String.valueOf(estimatedContacts));
        tvContactsToday.setText(String.valueOf(Math.max(0, estimatedContacts / 30)));
        tvContactsWeek.setText(String.valueOf(Math.max(0, estimatedContacts / 7)));
        tvContactsMonth.setText(String.valueOf(estimatedContacts));

        // Saves (simulate)
        int estimatedSaves = Math.max(0, totalViews / 20);
        tvTotalSaves.setText(String.valueOf(estimatedSaves));
        tvSavesToday.setText(String.valueOf(Math.max(0, estimatedSaves / 30)));
        tvSavesWeek.setText(String.valueOf(Math.max(0, estimatedSaves / 7)));
        tvSavesMonth.setText(String.valueOf(estimatedSaves));

        // Shares (simulate)
        int estimatedShares = Math.max(0, totalViews / 50);
        tvTotalShares.setText(String.valueOf(estimatedShares));
        tvSharesToday.setText(String.valueOf(Math.max(0, estimatedShares / 30)));
        tvSharesWeek.setText(String.valueOf(Math.max(0, estimatedShares / 7)));
        tvSharesMonth.setText(String.valueOf(estimatedShares));

        // Additional info
        tvLastViewed.setText("Last viewed: " + formatDate(product.getUpdatedAt()));
        tvAverageDaily.setText("Avg daily views: " + Math.max(1, totalViews / 30));
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        cardViews.setVisibility(loading ? View.GONE : View.VISIBLE);
        cardContacts.setVisibility(loading ? View.GONE : View.VISIBLE);
        cardSaves.setVisibility(loading ? View.GONE : View.VISIBLE);
        cardShares.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "Unknown";
        try {
            return dateString.substring(0, 10); // Simple format: yyyy-MM-dd
        } catch (Exception e) {
            return dateString;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}