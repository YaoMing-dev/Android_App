// app/src/main/java/com/example/newtrade/ui/product/CategoryProductsActivity.java
package com.example.newtrade.ui.product;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.LocationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.UserBehaviorTracker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryProductsActivity extends AppCompatActivity {

    private static final String TAG = "CategoryProductsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // ===== UI COMPONENTS =====
    private MaterialToolbar toolbar;
    private TextView tvEmptyState;
    private TextView tvLocationStatus;
    private TextView tvProductCount;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvProducts;
    private Chip chipNearby;
    private Chip chipAll;

    // ===== DATA =====
    private ProductAdapter productAdapter;
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> displayedProducts = new ArrayList<>();
    private Long categoryId;
    private String categoryName;

    // ===== LOCATION TRACKING =====
    private Double userLatitude = null;
    private Double userLongitude = null;
    private boolean isLocationLoaded = false;
    private boolean isLocationRequested = false;

    // ===== FILTERING =====
    private boolean showNearbyOnly = false;
    private static final double NEARBY_RADIUS_KM = 25.0; // 25km radius for "nearby"

    // ===== SERVICES =====
    private SharedPrefsManager prefsManager;
    private UserBehaviorTracker behaviorTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        // Initialize services
        initServices();

        // Get intent data
        getIntentData();

        // Initialize UI
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        // Start location detection and load products
        getCurrentLocationAndLoadProducts();
    }

    // ===== INITIALIZATION =====
    private void initServices() {
        prefsManager = SharedPrefsManager.getInstance(this);
        behaviorTracker = UserBehaviorTracker.getInstance(this);

        Log.d(TAG, "✅ Services initialized");
    }

    private void getIntentData() {
        categoryId = getIntent().getLongExtra("category_id", -1);
        categoryName = getIntent().getStringExtra("category_name");

        if (categoryName == null) {
            categoryName = "Category Products";
        }

        Log.d(TAG, "📱 Category ID: " + categoryId + ", Name: " + categoryName);

        // Track category browsing behavior
        if (behaviorTracker != null && categoryId > 0) {
            behaviorTracker.trackCategoryBrowse(categoryId, categoryName);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvProducts = findViewById(R.id.rv_products);

        // Optional UI components (may not exist in layout)
        tvLocationStatus = findViewById(R.id.tv_location_status);
        tvProductCount = findViewById(R.id.tv_product_count);
        chipNearby = findViewById(R.id.chip_nearby);
        chipAll = findViewById(R.id.chip_all);

        // Set initial location status
        updateLocationStatus("Detecting your location...");

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(displayedProducts, product -> {
            Log.d(TAG, "📱 Product clicked: " + product.getTitle() + " (ID: " + product.getId() +
                    (product.getDistanceFromUser() != null ? ", Distance: " + product.getFormattedDistance() : "") + ")");

            // Track product view behavior
            if (behaviorTracker != null) {
                behaviorTracker.trackProductView(product.getId(), categoryName, null);
            }

            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_title", product.getTitle());
            intent.putExtra("product_price", product.getFormattedPrice());
            intent.putExtra("category_name", categoryName);
            startActivity(intent);
        });

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(productAdapter);

        Log.d(TAG, "✅ RecyclerView setup with location-aware adapter");
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadProducts);

        // Filter chips listeners (if they exist)
        if (chipNearby != null) {
            chipNearby.setOnClickListener(v -> {
                showNearbyOnly = true;
                updateFilterChips();
                filterAndDisplayProducts();
                Log.d(TAG, "🔍 Filter: Nearby products only");
            });
        }

        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                showNearbyOnly = false;
                updateFilterChips();
                filterAndDisplayProducts();
                Log.d(TAG, "🔍 Filter: All products");
            });
        }

        Log.d(TAG, "✅ Listeners setup");
    }

    // ===== LOCATION HANDLING =====
    private void getCurrentLocationAndLoadProducts() {
        if (isLocationRequested) {
            Log.d(TAG, "Location already requested, loading products without location");
            loadProducts();
            return;
        }

        isLocationRequested = true;
        updateLocationStatus("Getting your location...");

        LocationUtils.getCurrentLocation(this, new LocationUtils.LocationCallback() {
            @Override
            public void onLocationSuccess(double latitude, double longitude) {
                userLatitude = latitude;
                userLongitude = longitude;
                isLocationLoaded = true;

                Log.d(TAG, "📍 User location detected: " + latitude + ", " + longitude);
                updateLocationStatus("📍 Location detected - showing nearby products first");

                // Enable nearby filter if we have location
                enableLocationFeatures();

                // Load products with location prioritization
                loadProducts();

                // Hide location status after 3 seconds
                findViewById(android.R.id.content).postDelayed(() -> hideLocationStatus(), 3000);
            }

            @Override
            public void onLocationError(String error) {
                Log.w(TAG, "Location error: " + error);
                updateLocationStatus("📍 Location unavailable - showing all " + categoryName.toLowerCase() + " products");

                // Disable location features
                disableLocationFeatures();

                // Load products anyway without location prioritization
                loadProducts();

                // Hide location status after 5 seconds
                findViewById(android.R.id.content).postDelayed(() -> hideLocationStatus(), 5000);
            }
        });
    }

    private void enableLocationFeatures() {
        // Show nearby filter option
        if (chipNearby != null) {
            chipNearby.setVisibility(View.VISIBLE);
        }
        if (chipAll != null) {
            chipAll.setVisibility(View.VISIBLE);
            chipAll.setChecked(true); // Default to showing all
        }
    }

    private void disableLocationFeatures() {
        // Hide nearby filter options since we don't have location
        if (chipNearby != null) {
            chipNearby.setVisibility(View.GONE);
        }
        if (chipAll != null) {
            chipAll.setVisibility(View.GONE);
        }
        showNearbyOnly = false;
    }

    private void updateLocationStatus(String message) {
        if (tvLocationStatus != null) {
            tvLocationStatus.setText(message);
            tvLocationStatus.setVisibility(View.VISIBLE);
        }
    }

    private void hideLocationStatus() {
        if (tvLocationStatus != null) {
            tvLocationStatus.setVisibility(View.GONE);
        }
    }

    // ===== PRODUCT LOADING =====
    private void loadProducts() {
        swipeRefresh.setRefreshing(true);
        hideEmptyState();
        showProductsView();

        Log.d(TAG, "🔄 Loading products for category: " + categoryId +
                (isLocationLoaded ? " with location prioritization" : " without location"));

        // Load products by category filter
        ApiClient.getApiService().getProducts(
                0,           // page
                100,         // size - get more products for better location filtering
                null,        // search
                categoryId,  // categoryId filter
                null,        // minPrice
                null,        // maxPrice
                null,        // condition
                null,        // location
                null         // radius
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                swipeRefresh.setRefreshing(false);

                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Map<String, Object> data = response.body().getData();
                        List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                        processProductList(productList);
                        Log.d(TAG, "✅ Loaded " + (productList != null ? productList.size() : 0) + " products");
                    } else {
                        Log.e(TAG, "❌ API response not successful or null body");
                        showEmptyState();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing products response", e);
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "❌ Products API call failed", t);
                showEmptyState();
            }
        });
    }

    private void processProductList(List<Map<String, Object>> productList) {
        allProducts.clear();

        if (productList != null && !productList.isEmpty()) {
            // Parse all products
            for (Map<String, Object> productData : productList) {
                try {
                    Product product = parseProductFromData(productData);
                    if (product != null) {
                        allProducts.add(product);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "❌ Error parsing product: " + e.getMessage());
                }
            }

            if (!allProducts.isEmpty()) {
                // Apply location-based prioritization
                prioritizeNearbyProducts();

                // Filter and display products based on current filter
                filterAndDisplayProducts();

                Log.d(TAG, "✅ Processed " + allProducts.size() + " products with location prioritization");
            } else {
                showEmptyState();
            }
        } else {
            showEmptyState();
        }
    }

    private Product parseProductFromData(Map<String, Object> productData) {
        try {
            Product product = new Product();

            // Basic fields
            product.setId(((Number) productData.get("id")).longValue());
            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));
            product.setLocation((String) productData.get("location"));

            // Location coordinates
            if (productData.get("latitude") instanceof Number) {
                product.setLatitude(((Number) productData.get("latitude")).doubleValue());
            }
            if (productData.get("longitude") instanceof Number) {
                product.setLongitude(((Number) productData.get("longitude")).doubleValue());
            }

            // Price handling
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }

            // Image URLs handling - Support multiple formats
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                List<String> imageUrls = (List<String>) imageUrlsObj;
                product.setImageUrls(imageUrls);
                if (!imageUrls.isEmpty()) {
                    product.setImageUrl(imageUrls.get(0)); // Set primary image
                }
            } else if (imageUrlsObj instanceof String && !((String) imageUrlsObj).isEmpty()) {
                product.setImageUrl((String) imageUrlsObj);
            }

            // Condition handling
            String condition = (String) productData.get("condition");
            if (condition != null) {
                try {
                    product.setCondition(Product.ProductCondition.valueOf(condition));
                } catch (IllegalArgumentException e) {
                    product.setCondition(Product.ProductCondition.GOOD);
                }
            }

            // Category name
            product.setCategoryName(categoryName);

            // Calculate distance immediately if user location is available
            if (userLatitude != null && userLongitude != null && product.hasLocation()) {
                double distance = LocationUtils.calculateDistance(
                        userLatitude, userLongitude,
                        product.getLatitude(), product.getLongitude()
                );
                product.setDistanceFromUser(distance);
            }

            Log.d(TAG, "✅ Parsed product: " + product.getTitle() + " (ID: " + product.getId() +
                    (product.getDistanceFromUser() != null ? ", " + product.getFormattedDistance() : ", no location") + ")");
            return product;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating product from data", e);
            return null;
        }
    }

    private void prioritizeNearbyProducts() {
        if (userLatitude != null && userLongitude != null && !allProducts.isEmpty()) {
            // Calculate distances for all products if not already done
            LocationUtils.calculateDistancesForProducts(userLatitude, userLongitude, allProducts);

            // Sort by distance (nearest first)
            LocationUtils.sortProductsByDistance(allProducts);

            // Log location statistics
            LocationUtils.logLocationStats(allProducts);

            // Update location groups for filtering
            LocationUtils.LocationGroups groups = LocationUtils.groupProductsByDistance(allProducts);

            Log.d(TAG, "✅ Products prioritized by distance: " +
                    groups.getTotalNearbyCount() + " within " + NEARBY_RADIUS_KM + "km");
        } else {
            Log.d(TAG, "⚠️ Cannot prioritize products - missing location or products");
        }
    }

    // ===== FILTERING =====
    private void filterAndDisplayProducts() {
        displayedProducts.clear();

        if (showNearbyOnly && userLatitude != null && userLongitude != null) {
            // Show only nearby products
            List<Product> nearbyProducts = LocationUtils.filterProductsByRadius(allProducts, NEARBY_RADIUS_KM);
            displayedProducts.addAll(nearbyProducts);

            Log.d(TAG, "🔍 Filtered to " + nearbyProducts.size() + " nearby products within " + NEARBY_RADIUS_KM + "km");
        } else {
            // Show all products (already sorted by distance if location available)
            displayedProducts.addAll(allProducts);

            Log.d(TAG, "🔍 Showing all " + allProducts.size() + " products");
        }

        // Update UI
        updateProductCount();
        updateFilterChips();

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        // Show appropriate view
        if (displayedProducts.isEmpty()) {
            showEmptyStateForFilter();
        } else {
            showProductsView();
        }
    }

    private void updateFilterChips() {
        if (chipNearby != null && chipAll != null) {
            chipNearby.setChecked(showNearbyOnly);
            chipAll.setChecked(!showNearbyOnly);
        }
    }

    private void updateProductCount() {
        if (tvProductCount != null) {
            String countText;
            if (showNearbyOnly) {
                countText = displayedProducts.size() + " nearby products";
            } else {
                countText = displayedProducts.size() + " products";
                if (userLatitude != null && userLongitude != null) {
                    int nearbyCount = LocationUtils.filterProductsByRadius(allProducts, NEARBY_RADIUS_KM).size();
                    if (nearbyCount > 0) {
                        countText += " (" + nearbyCount + " nearby)";
                    }
                }
            }
            tvProductCount.setText(countText);
            tvProductCount.setVisibility(View.VISIBLE);
        }
    }

    // ===== UI STATE MANAGEMENT =====
    private void showProductsView() {
        if (rvProducts != null) {
            rvProducts.setVisibility(View.VISIBLE);
        }
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        if (rvProducts != null) {
            rvProducts.setVisibility(View.GONE);
        }
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No products found in " + categoryName + "\n\nCheck back later or try a different category.");
        }
        Log.d(TAG, "📭 Showing empty state for category: " + categoryName);
    }

    private void showEmptyStateForFilter() {
        if (rvProducts != null) {
            rvProducts.setVisibility(View.GONE);
        }
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.VISIBLE);

            if (showNearbyOnly) {
                tvEmptyState.setText("No " + categoryName.toLowerCase() + " products found nearby\n\n" +
                        "Try expanding your search to see all products in this category.");
            } else {
                tvEmptyState.setText("No products found in " + categoryName + "\n\nCheck back later or try a different category.");
            }
        }
        Log.d(TAG, "📭 Showing filtered empty state for category: " + categoryName);
    }

    // ===== PERMISSION HANDLING =====
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Location permission granted");
                isLocationRequested = false; // Allow re-requesting
                getCurrentLocationAndLoadProducts();
            } else {
                Log.d(TAG, "❌ Location permission denied");
                updateLocationStatus("📍 Location permission denied - showing all products");
                disableLocationFeatures();
                loadProducts();
            }
        }
    }

    // ===== LIFECYCLE =====
    @Override
    protected void onResume() {
        super.onResume();

        // Refresh data when returning to this activity
        if (allProducts.isEmpty()) {
            getCurrentLocationAndLoadProducts();
        } else {
            // Re-apply filtering in case location status changed
            filterAndDisplayProducts();
        }

        Log.d(TAG, "🔄 CategoryProductsActivity resumed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🧹 CategoryProductsActivity destroyed");
    }

    // ===== DEBUG & UTILITY =====
    private void logCurrentState() {
        Log.d(TAG, "=== CATEGORY PRODUCTS STATE ===");
        Log.d(TAG, "Category: " + categoryName + " (ID: " + categoryId + ")");
        Log.d(TAG, "User location: " + (isLocationLoaded ?
                userLatitude + ", " + userLongitude : "unavailable"));
        Log.d(TAG, "All products: " + allProducts.size());
        Log.d(TAG, "Displayed products: " + displayedProducts.size());
        Log.d(TAG, "Show nearby only: " + showNearbyOnly);

        if (isLocationLoaded && !allProducts.isEmpty()) {
            LocationUtils.LocationGroups groups = LocationUtils.groupProductsByDistance(allProducts);
            Log.d(TAG, "Nearby products (<" + NEARBY_RADIUS_KM + "km): " + groups.getTotalNearbyCount());
        }
        Log.d(TAG, "===============================");
    }
}