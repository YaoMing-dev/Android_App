// app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
package com.example.newtrade.ui.home;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.CategoryProductsActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.search.SearchActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.LocationManager;
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements
        LocationManager.LocationCallback {

    private static final String TAG = "HomeFragment";

    // UI Components
    private TextView tvWelcome;
    private TextInputEditText etSearch;
    private RecyclerView rvCategories, rvRecentProducts, rvNearbyProducts;
    private SwipeRefreshLayout swipeRefresh;
    private View loadingView, errorView, contentView;

    // Data
    private List<Category> categories = new ArrayList<>();
    private List<Product> recentProducts = new ArrayList<>();
    private List<Product> nearbyProducts = new ArrayList<>();

    // Utils
    private SharedPrefsManager prefsManager;
    private LocationManager locationManager;
    private Double currentLatitude;
    private Double currentLongitude;

    // State
    private boolean isLoadingData = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initUtils();
        setupListeners();
        setupRecyclerViews();

        loadData();
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_welcome);
        etSearch = view.findViewById(R.id.et_search);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvRecentProducts = view.findViewById(R.id.rv_recent_products);
        rvNearbyProducts = view.findViewById(R.id.rv_nearby_products);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        loadingView = view.findViewById(R.id.view_loading);
        errorView = view.findViewById(R.id.view_error);
        contentView = view.findViewById(R.id.view_content);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(requireContext());
        locationManager = new LocationManager(requireContext(), this);

        // FR-3.2.2: Personalized recommendations - welcome user
        String userName = prefsManager.getUserName();
        if (userName != null) {
            tvWelcome.setText("Welcome back, " + userName + "!");
        } else {
            tvWelcome.setText("Welcome to TradeUp!");
        }
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);

        // Search functionality
        etSearch.setOnClickListener(v -> openSearchActivity());
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    openSearchActivity(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Recent Products RecyclerView
        rvRecentProducts.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Nearby Products RecyclerView
        rvNearbyProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadData() {
        if (isLoadingData) return;

        isLoadingData = true;
        showLoadingState();

        // Get user location for nearby products (FR-6.3: Listings near user prioritized)
        if (locationManager.hasLocationPermission()) {
            locationManager.getCurrentLocation();
        }

        loadCategories();
        loadRecentProducts();
    }

    public void refreshData() {
        Log.d(TAG, "Refreshing home data");
        loadData();
    }

    // FR-3.2.1: Items organized under categories
    private void loadCategories() {
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService().getCategories();
        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleCategoriesResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load categories", t);
                handleLoadingError("Failed to load categories");
            }
        });
    }

    private void loadRecentProducts() {
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getAllProducts(0, 10, "createdAt", "desc", null, null, null, null);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleRecentProductsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load recent products", t);
                handleLoadingError("Failed to load recent products");
            }
        });
    }

    private void loadNearbyProducts() {
        if (currentLatitude == null || currentLongitude == null) {
            finishLoading();
            return;
        }

        // FR-6.1: Search by location radius
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getAllProducts(0, 10, "createdAt", "desc", null, null, null, null);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleNearbyProductsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load nearby products", t);
                finishLoading();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleCategoriesResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> categoryMaps = (List<Map<String, Object>>) data.get("content");

                if (categoryMaps != null) {
                    categories.clear();
                    for (Map<String, Object> categoryMap : categoryMaps) {
                        Category category = parseCategoryFromMap(categoryMap);
                        if (category != null) {
                            categories.add(category);
                        }
                    }
                    updateCategoriesUI();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing categories response", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleRecentProductsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null) {
                    recentProducts.clear();
                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            recentProducts.add(product);
                        }
                    }
                    updateRecentProductsUI();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing recent products response", e);
        }

        finishLoading();
    }

    @SuppressWarnings("unchecked")
    private void handleNearbyProductsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null) {
                    nearbyProducts.clear();
                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            nearbyProducts.add(product);
                        }
                    }
                    updateNearbyProductsUI();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing nearby products response", e);
        }

        finishLoading();
    }

    private Category parseCategoryFromMap(Map<String, Object> categoryMap) {
        try {
            Category category = new Category();
            category.setId(((Number) categoryMap.get("id")).longValue());
            category.setName((String) categoryMap.get("name"));
            category.setDescription((String) categoryMap.get("description"));
            category.setIconUrl((String) categoryMap.get("iconUrl"));
            category.setColor((String) categoryMap.get("color"));

            Object productCount = categoryMap.get("productCount");
            if (productCount instanceof Number) {
                category.setProductCount(((Number) productCount).intValue());
            }

            return category;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing category", e);
            return null;
        }
    }

    private Product parseProductFromMap(Map<String, Object> productMap) {
        try {
            Product product = new Product();
            product.setId(((Number) productMap.get("id")).longValue());
            product.setTitle((String) productMap.get("title"));
            product.setDescription((String) productMap.get("description"));
            product.setLocation((String) productMap.get("location"));

            Object price = productMap.get("price");
            if (price instanceof Number) {
                product.setPrice(new java.math.BigDecimal(price.toString()));
            }

            Object viewCount = productMap.get("viewCount");
            if (viewCount instanceof Number) {
                product.setViewCount(((Number) viewCount).intValue());
            }

            String conditionStr = (String) productMap.get("condition");
            if (conditionStr != null) {
                product.setCondition(Product.ProductCondition.fromString(conditionStr));
            }

            String statusStr = (String) productMap.get("status");
            if (statusStr != null) {
                product.setStatus(Product.ProductStatus.fromString(statusStr));
            }

            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) productMap.get("imageUrls");
            if (imageUrls != null) {
                product.setImageUrls(imageUrls);
            }

            // Parse seller
            @SuppressWarnings("unchecked")
            Map<String, Object> sellerMap = (Map<String, Object>) productMap.get("seller");
            if (sellerMap != null) {
                // Create basic User object
                // Full User parsing would be more complex, but for display purposes this is sufficient
            }

            return product;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing product", e);
            return null;
        }
    }

    private void updateCategoriesUI() {
        // TODO: Implement categories adapter
        Log.d(TAG, "Categories loaded: " + categories.size());
    }

    private void updateRecentProductsUI() {
        // TODO: Implement recent products adapter
        Log.d(TAG, "Recent products loaded: " + recentProducts.size());
    }

    private void updateNearbyProductsUI() {
        // TODO: Implement nearby products adapter
        Log.d(TAG, "Nearby products loaded: " + nearbyProducts.size());
    }

    private void openSearchActivity() {
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        startActivity(intent);
    }

    private void openSearchActivity(String query) {
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        intent.putExtra("query", query);
        startActivity(intent);
    }

    private void showLoadingState() {
        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }

    private void showContentState() {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    private void showErrorState() {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    private void handleLoadingError(String error) {
        Log.e(TAG, error);

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showErrorState();
        } else {
            // Show content even with partial data
            showContentState();
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        }

        finishLoading();
    }

    private void finishLoading() {
        isLoadingData = false;
        swipeRefresh.setRefreshing(false);
        showContentState();
    }

    // LocationManager.LocationCallback implementation
    @Override
    public void onLocationReceived(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        // Save location for future use
        prefsManager.saveLastLocation(currentLatitude, currentLongitude, null);

        Log.d(TAG, "Location received: " + currentLatitude + ", " + currentLongitude);

        // Load nearby products with location
        loadNearbyProducts();
    }

    @Override
    public void onLocationError(String error) {
        Log.e(TAG, "Location error: " + error);
        finishLoading();
    }

    // Method called by MainActivity for back press handling
    public boolean onBackPressed() {
        // Return false to allow default back press behavior
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.cleanup();
        }
    }
}