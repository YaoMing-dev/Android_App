// app/src/main/java/com/example/newtrade/ui/profile/MyListingsActivity.java
package com.example.newtrade.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ProductService;
import com.example.newtrade.api.AnalyticsService;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.analytics.ProductAnalyticsActivity;
import com.example.newtrade.ui.product.AddProductActivity;
import com.example.newtrade.ui.product.EditProductActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.UserBehaviorTracker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyListingsActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "MyListingsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayoutStatus;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvMyListings;
    private LinearLayout llEmptyStateContainer;
    private TextView tvEmptyState;
    private TextView tvEmptySubtitle;
    private MaterialButton btnAddFirstProduct;
    private FloatingActionButton fabAddProduct;

    // ✅ Stats Summary Views
    private CardView cardStatsSummary;
    private TextView tvTotalListings;
    private TextView tvAvailableCount;
    private TextView tvSoldCount;
    private TextView tvPausedCount;

    // Data & Adapters
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> filteredProducts = new ArrayList<>();
    private ProductAdapter productAdapter;
    private SharedPrefsManager prefsManager;

    // Services
    private ProductService productService;
    private AnalyticsService analyticsService;
    private UserBehaviorTracker behaviorTracker;

    // ✅ Status Filtering
    private String currentStatusFilter = "ALL";
    private final Map<String, Integer> statusCounts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        initViews();
        initServices();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupListeners();
        setupStatsCard();

        // Load data
        loadMyProducts();
        loadDashboardAnalytics();
    }

    // ===== INITIALIZATION =====

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayoutStatus = findViewById(R.id.tab_layout_status);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvMyListings = findViewById(R.id.rv_my_listings);
        llEmptyStateContainer = findViewById(R.id.ll_empty_state_container);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        tvEmptySubtitle = findViewById(R.id.tv_empty_subtitle);
        btnAddFirstProduct = findViewById(R.id.btn_add_first_product);
        fabAddProduct = findViewById(R.id.fab_add_product);

        // ✅ Stats Summary Views (safe findViewById)
        cardStatsSummary = findViewById(R.id.card_stats_summary);
        tvTotalListings = findViewById(R.id.tv_total_listings);
        tvAvailableCount = findViewById(R.id.tv_available_count);
        tvSoldCount = findViewById(R.id.tv_sold_count);
        tvPausedCount = findViewById(R.id.tv_paused_count);

        Log.d(TAG, "✅ Views initialized");
    }

    private void initServices() {
        productService = ApiClient.getProductService();
        analyticsService = ApiClient.getAnalyticsService();
        prefsManager = SharedPrefsManager.getInstance(this);
        behaviorTracker = UserBehaviorTracker.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            String currentUserName = prefsManager.getUserName();
            String title = (currentUserName != null && !currentUserName.isEmpty())
                    ? currentUserName + "'s Listings"
                    : "My Listings";

            getSupportActionBar().setTitle(title);
        }
    }

    // ===== ✅ TAB SETUP =====

    private void setupTabs() {
        if (tabLayoutStatus == null) return;

        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("All"));
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("Available"));
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("Sold"));
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("Paused"));

        tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentStatusFilter = "ALL";
                        break;
                    case 1:
                        currentStatusFilter = "AVAILABLE";
                        break;
                    case 2:
                        currentStatusFilter = "SOLD";
                        break;
                    case 3:
                        currentStatusFilter = "PAUSED";
                        break;
                }

                Log.d(TAG, "🔍 Status filter changed to: " + currentStatusFilter);
                filterProductsByStatus();
                updateEmptyStateForFilter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(filteredProducts, this, true);
        rvMyListings.setLayoutManager(new LinearLayoutManager(this));
        rvMyListings.setAdapter(productAdapter);
        rvMyListings.setHasFixedSize(true);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
        fabAddProduct.setOnClickListener(v -> openAddProduct());

        if (btnAddFirstProduct != null) {
            btnAddFirstProduct.setOnClickListener(v -> openAddProduct());
        }
    }

    private void setupStatsCard() {
        if (cardStatsSummary != null) {
            cardStatsSummary.setOnClickListener(v -> showDetailedStats());
        }
    }

    // ===== DATA LOADING =====

    private void refreshData() {
        loadMyProducts();
        loadDashboardAnalytics();
    }

    // ✅ FIXED: Using existing ProductService methods
    private void loadMyProducts() {
        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            showError("User not logged in");
            return;
        }

        allProducts.clear();
        filteredProducts.clear();
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        // ✅ Use getProductsByUser method từ ProductService hiện có
        productService.getProductsByUser(userId).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {

                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        parseProductsResponse(standardResponse.getData());
                        Log.d(TAG, "✅ Loaded " + allProducts.size() + " products");
                    } else {
                        showError("Failed to load products: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Service unavailable");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                Log.e(TAG, "❌ Failed to load products", t);
                showError("Network error");
            }
        });
    }

    // ===== ✅ PRODUCT PARSING =====

    private void parseProductsResponse(Map<String, Object> data) {
        allProducts.clear();
        statusCounts.clear();

        // Initialize status counts
        statusCounts.put("ALL", 0);
        statusCounts.put("AVAILABLE", 0);
        statusCounts.put("SOLD", 0);
        statusCounts.put("PAUSED", 0);

        try {
            // ✅ Flexible parsing - handle both direct list and paginated response
            List<Map<String, Object>> productList = extractProductListFromResponse(data);

            for (Map<String, Object> productData : productList) {
                try {
                    Product product = parseProductFromMap(productData);
                    if (product != null) {
                        allProducts.add(product);

                        statusCounts.put("ALL", statusCounts.get("ALL") + 1);

                        Product.ProductStatus status = product.getStatus();
                        if (status != null) {
                            String statusKey = status.name();
                            statusCounts.put(statusKey, statusCounts.getOrDefault(statusKey, 0) + 1);
                        } else {
                            statusCounts.put("AVAILABLE", statusCounts.get("AVAILABLE") + 1);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing individual product", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing products response", e);
        }

        updateStatsCard();
        updateTabTitlesWithCounts();
        filterProductsByStatus();
        updateUI();
    }

    // ✅ Flexible parsing để handle different response formats
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractProductListFromResponse(Map<String, Object> data) {
        if (data == null) {
            return new ArrayList<>();
        }

        // Try different possible keys for product list
        String[] possibleKeys = {"products", "content", "data", "items", "results"};

        for (String key : possibleKeys) {
            Object value = data.get(key);
            if (value instanceof List) {
                try {
                    return (List<Map<String, Object>>) value;
                } catch (ClassCastException e) {
                    Log.w(TAG, "Found list under key '" + key + "' but wrong type: " + e.getMessage());
                }
            }
        }

        // If no list found, check if data itself is a list
        if (data instanceof List) {
            try {
                return (List<Map<String, Object>>) data;
            } catch (ClassCastException e) {
                Log.w(TAG, "Data is list but wrong type: " + e.getMessage());
            }
        }

        Log.w(TAG, "❌ Could not find product list in response. Available keys: " + data.keySet());
        return new ArrayList<>();
    }

    private Product parseProductFromMap(Map<String, Object> productData) {
        try {
            Product product = new Product();

            // Parse ID
            Object idObj = productData.get("id");
            if (idObj instanceof Number) {
                product.setId(((Number) idObj).longValue());
            }

            // Basic fields
            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));
            product.setLocation((String) productData.get("location"));

            // Parse price
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(new BigDecimal(priceObj.toString()));
            }

            // Parse status
            Object statusObj = productData.get("status");
            if (statusObj != null) {
                try {
                    Product.ProductStatus status = Product.ProductStatus.valueOf(statusObj.toString());
                    product.setStatus(status);
                } catch (IllegalArgumentException e) {
                    product.setStatus(Product.ProductStatus.AVAILABLE);
                }
            } else {
                product.setStatus(Product.ProductStatus.AVAILABLE);
            }

            // Parse condition
            Object conditionObj = productData.get("condition");
            if (conditionObj != null) {
                try {
                    Product.ProductCondition condition = Product.ProductCondition.valueOf(conditionObj.toString());
                    product.setCondition(condition);
                } catch (IllegalArgumentException e) {
                    product.setCondition(Product.ProductCondition.GOOD);
                }
            }

            // Parse image URLs
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) imageUrlsObj;
                product.setImageUrls(imageUrls);
            } else if (imageUrlsObj instanceof String) {
                List<String> imageUrls = new ArrayList<>();
                imageUrls.add((String) imageUrlsObj);
                product.setImageUrls(imageUrls);
            }

            // Additional fields
            Object viewCountObj = productData.get("viewCount");
            if (viewCountObj instanceof Number) {
                product.setViewCount(((Number) viewCountObj).intValue());
            }

            product.setCreatedAt((String) productData.get("createdAt"));
            product.setCategoryName((String) productData.get("categoryName"));

            return product;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing product data", e);
            return null;
        }
    }

    // ===== ✅ STATUS FILTERING =====

    private void filterProductsByStatus() {
        filteredProducts.clear();

        for (Product product : allProducts) {
            if (shouldIncludeProduct(product)) {
                filteredProducts.add(product);
            }
        }

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        Log.d(TAG, "🔍 Filtered " + filteredProducts.size() + " products for status: " + currentStatusFilter);
    }

    private boolean shouldIncludeProduct(Product product) {
        if ("ALL".equals(currentStatusFilter)) {
            return true;
        }

        Product.ProductStatus status = product.getStatus();
        if (status == null) {
            status = Product.ProductStatus.AVAILABLE;
        }

        return currentStatusFilter.equals(status.name());
    }

    // ===== ✅ STATS CARD UPDATES =====

    private void updateStatsCard() {
        if (tvTotalListings != null) {
            tvTotalListings.setText(String.valueOf(statusCounts.get("ALL")));
        }
        if (tvAvailableCount != null) {
            tvAvailableCount.setText(String.valueOf(statusCounts.get("AVAILABLE")));
        }
        if (tvSoldCount != null) {
            tvSoldCount.setText(String.valueOf(statusCounts.get("SOLD")));
        }
        if (tvPausedCount != null) {
            tvPausedCount.setText(String.valueOf(statusCounts.get("PAUSED")));
        }
    }

    private void updateTabTitlesWithCounts() {
        if (tabLayoutStatus != null && tabLayoutStatus.getTabCount() >= 4) {
            TabLayout.Tab allTab = tabLayoutStatus.getTabAt(0);
            if (allTab != null) {
                allTab.setText("All (" + statusCounts.get("ALL") + ")");
            }

            TabLayout.Tab availableTab = tabLayoutStatus.getTabAt(1);
            if (availableTab != null) {
                availableTab.setText("Available (" + statusCounts.get("AVAILABLE") + ")");
            }

            TabLayout.Tab soldTab = tabLayoutStatus.getTabAt(2);
            if (soldTab != null) {
                soldTab.setText("Sold (" + statusCounts.get("SOLD") + ")");
            }

            TabLayout.Tab pausedTab = tabLayoutStatus.getTabAt(3);
            if (pausedTab != null) {
                pausedTab.setText("Paused (" + statusCounts.get("PAUSED") + ")");
            }
        }
    }

    // ===== ✅ EMPTY STATE MANAGEMENT =====

    private void updateEmptyStateForFilter() {
        if (filteredProducts.isEmpty()) {
            updateEmptyStateMessage();
        }
    }

    private void updateEmptyStateMessage() {
        if (tvEmptyState != null && tvEmptySubtitle != null) {
            switch (currentStatusFilter) {
                case "ALL":
                    tvEmptyState.setText("No listings found");
                    tvEmptySubtitle.setText("Start by adding your first product");
                    break;
                case "AVAILABLE":
                    tvEmptyState.setText("No available listings");
                    tvEmptySubtitle.setText("All your listings are sold or paused");
                    break;
                case "SOLD":
                    tvEmptyState.setText("No sold listings");
                    tvEmptySubtitle.setText("Complete some sales to see them here");
                    break;
                case "PAUSED":
                    tvEmptyState.setText("No paused listings");
                    tvEmptySubtitle.setText("Pause listings to manage inventory");
                    break;
            }
        }
    }

    private void updateUI() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        boolean isEmpty = filteredProducts.isEmpty();
        if (rvMyListings != null) {
            rvMyListings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (llEmptyStateContainer != null) {
            llEmptyStateContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        if (cardStatsSummary != null) {
            cardStatsSummary.setVisibility(allProducts.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    // ===== ✅ PRODUCT ADAPTER CALLBACKS =====

    @Override
    public void onProductClick(Product product) {
        if (product.getStatus() == Product.ProductStatus.AVAILABLE) {
            navigateToProductDetail(product);
        } else {
            showProductOptionsMenu(product);
        }
    }

    @Override
    public void onProductLongClick(Product product) {
        showProductOptionsMenu(product);
    }

    @Override
    public void onAnalyticsClick(Product product) {
        navigateToProductAnalytics(product);
    }

    @Override
    public void onStatusChangeClick(Product product) {
        showStatusChangeDialog(product);
    }

    // ===== NAVIGATION & DIALOGS =====

    private void navigateToProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getFormattedPrice());
        startActivity(intent);
    }

    private void navigateToProductAnalytics(Product product) {
        Intent intent = new Intent(this, ProductAnalyticsActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        startActivity(intent);

        Log.d(TAG, "📊 Opening analytics for: " + product.getTitle());
    }

    private void showProductOptionsMenu(Product product) {
        String[] options = {
                "📊 View Analytics",
                "✏️ Edit Product",
                "🔄 Change Status",
                "🗑️ Delete Product"
        };

        new AlertDialog.Builder(this)
                .setTitle(product.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            navigateToProductAnalytics(product);
                            break;
                        case 1:
                            editProduct(product);
                            break;
                        case 2:
                            showStatusChangeDialog(product);
                            break;
                        case 3:
                            confirmDeleteProduct(product);
                            break;
                    }
                })
                .show();
    }

    // ===== STATUS CHANGE DIALOG =====

    private void showStatusChangeDialog(Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_status, null);
        RadioGroup rgStatus = dialogView.findViewById(R.id.rg_status);

        // Pre-select current status
        switch (product.getStatus()) {
            case AVAILABLE:
                rgStatus.check(R.id.rb_available);
                break;
            case SOLD:
                rgStatus.check(R.id.rb_sold);
                break;
            case PAUSED:
                rgStatus.check(R.id.rb_paused);
                break;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_update).setOnClickListener(v -> {
            Product.ProductStatus newStatus = getSelectedStatus(rgStatus);
            if (newStatus != product.getStatus()) {
                updateProductStatus(product, newStatus);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private Product.ProductStatus getSelectedStatus(RadioGroup rgStatus) {
        int selectedId = rgStatus.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_available) {
            return Product.ProductStatus.AVAILABLE;
        } else if (selectedId == R.id.rb_sold) {
            return Product.ProductStatus.SOLD;
        } else if (selectedId == R.id.rb_paused) {
            return Product.ProductStatus.PAUSED;
        }
        return Product.ProductStatus.AVAILABLE;
    }

    // ✅ FIXED: Using existing ProductService methods
    private void updateProductStatus(Product product, Product.ProductStatus newStatus) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Updating status...");
        progressDialog.show();

        // Track status change
        behaviorTracker.trackProductInteraction(product.getId(), "STATUS_CHANGE",
                Map.of("oldStatus", product.getStatus().name(), "newStatus", newStatus.name()));

        if (newStatus == Product.ProductStatus.SOLD) {
            // ✅ Use markProductAsSold from ProductService
            productService.markProductAsSold(product.getId())
                    .enqueue(new Callback<StandardResponse<Void>>() {
                        @Override
                        public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                               @NonNull Response<StandardResponse<Void>> response) {
                            progressDialog.dismiss();

                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                product.setStatus(newStatus);
                                refreshData();
                                showSuccess("Product marked as sold!");
                                Log.d(TAG, "✅ Product marked as sold: " + product.getTitle());
                            } else {
                                showError("Failed to mark as sold");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                            progressDialog.dismiss();
                            Log.e(TAG, "❌ Failed to mark as sold", t);
                            showError("Network error");
                        }
                    });
        } else {
            // ✅ Use updateProduct for other status changes
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("status", newStatus.name());

            productService.updateProduct(product.getId(), updateData)
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                               @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                            progressDialog.dismiss();

                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                product.setStatus(newStatus);
                                refreshData();
                                showSuccess("Status updated successfully!");
                                Log.d(TAG, "✅ Status updated: " + product.getTitle() + " -> " + newStatus);
                            } else {
                                showError("Failed to update status");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                            progressDialog.dismiss();
                            Log.e(TAG, "❌ Failed to update status", t);
                            showError("Network error");
                        }
                    });
        }
    }

    // ===== DASHBOARD ANALYTICS =====

    private void loadDashboardAnalytics() {
        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) return;

        analyticsService.getDashboard().enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        Map<String, Object> dashboard = standardResponse.getData();
                        updateDashboardUI(dashboard);
                        Log.d(TAG, "✅ Dashboard analytics loaded");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Failed to load dashboard analytics", t);
            }
        });
    }

    private void updateDashboardUI(Map<String, Object> dashboard) {
        if (getSupportActionBar() != null && dashboard != null) {
            Object productsListed = dashboard.get("productsListed");
            Object productsSold = dashboard.get("productsSold");

            String subtitle = String.format("Listed: %s • Sold: %s",
                    productsListed != null ? productsListed.toString() : "0",
                    productsSold != null ? productsSold.toString() : "0");

            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    // ===== UTILITY METHODS =====

    private void editProduct(Product product) {
        Intent intent = new Intent(this, EditProductActivity.class);

        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_description", product.getDescription());
        intent.putExtra("product_price", product.getPrice() != null ?
                product.getPrice().toString() : "");
        intent.putExtra("product_location", product.getLocation());
        intent.putExtra("product_condition", product.getCondition() != null ?
                product.getCondition().name() : "");
        intent.putExtra("product_category", product.getCategoryName());

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            intent.putExtra("product_image_url", product.getImageUrls().get(0));
        }

        startActivityForResult(intent, 100);
        Log.d(TAG, "✏️ Opening edit for: " + product.getTitle());
    }

    private void confirmDeleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete '" + product.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ✅ Using existing deleteProduct method
    private void deleteProduct(Product product) {
        behaviorTracker.trackProductInteraction(product.getId(), "DELETE", null);

        productService.deleteProduct(product.getId())
                .enqueue(new Callback<StandardResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                           @NonNull Response<StandardResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            allProducts.remove(product);
                            filteredProducts.remove(product);

                            refreshData();
                            showSuccess("Product deleted successfully");
                            Log.d(TAG, "🗑️ Product deleted: " + product.getTitle());
                        } else {
                            showError("Failed to delete product");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                        Log.e(TAG, "❌ Failed to delete product", t);
                        showError("Network error");
                    }
                });
    }

    private void openAddProduct() {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivityForResult(intent, 200);
        Log.d(TAG, "➕ Opening add product");
    }

    private void showDetailedStats() {
        String message = String.format(
                "📊 Your Listing Statistics\n\n" +
                        "📋 Total Listings: %d\n" +
                        "🟢 Available: %d\n" +
                        "🔴 Sold: %d\n" +
                        "⏸️ Paused: %d\n\n" +
                        "💡 Keep your listings active for better visibility!",
                statusCounts.get("ALL"),
                statusCounts.get("AVAILABLE"),
                statusCounts.get("SOLD"),
                statusCounts.get("PAUSED")
        );

        new AlertDialog.Builder(this)
                .setTitle("Listing Statistics")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: " + message);
    }

    // ===== ACTIVITY LIFECYCLE =====

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 100 || requestCode == 200) {
                refreshData();
                Log.d(TAG, "🔄 Refreshing after add/edit product");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Optional: refresh on resume
    }
}