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
import com.example.newtrade.models.HomeSection;
import com.example.newtrade.ui.home.adapter.CategoriesAdapter;
import com.example.newtrade.ui.home.adapter.HomeSectionsAdapter;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.search.SearchActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.LocationManager;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements
        LocationManager.LocationCallback,
        CategoriesAdapter.OnCategoryClickListener {

    private static final String TAG = "HomeFragment";

    // UI Components
    private TextView tvWelcome;
    private TextInputEditText etSearch;
    private RecyclerView rvCategories, rvContent;
    private SwipeRefreshLayout swipeRefresh;

    // Adapters
    private CategoriesAdapter categoriesAdapter;
    private HomeSectionsAdapter sectionsAdapter;

    // Data
    private List<Category> categories = new ArrayList<>();
    private List<HomeSection> homeSections = new ArrayList<>();

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

        prefsManager = new SharedPrefsManager(requireContext());
        locationManager = new LocationManager(requireContext(), this);

        initViews(view);
        setupRecyclerViews();
        setupListeners();

        loadHomeData();
        getCurrentLocation();
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_welcome);
        etSearch = view.findViewById(R.id.et_search);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvContent = view.findViewById(R.id.rv_content);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        // Set welcome message
        String userName = prefsManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText(getString(R.string.welcome_user, userName));
        } else {
            tvWelcome.setText("Welcome to NewTrade!");
        }
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        categoriesAdapter = new CategoriesAdapter(categories, this);
        rvCategories.setAdapter(categoriesAdapter);

        // Content RecyclerView
        rvContent.setLayoutManager(new LinearLayoutManager(requireContext()));
        sectionsAdapter = new HomeSectionsAdapter(homeSections);
        rvContent.setAdapter(sectionsAdapter);
    }

    private void setupListeners() {
        // Search input click
        etSearch.setOnClickListener(v -> openSearchActivity());
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    openSearchActivity(s.toString());
                }
            }
        });

        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    private void openSearchActivity() {
        openSearchActivity(null);
    }

    private void openSearchActivity(String query) {
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        if (query != null && !query.trim().isEmpty()) {
            intent.putExtra(Constants.BUNDLE_SEARCH_QUERY, query);
        }
        startActivity(intent);

        // Clear search text
        etSearch.setText("");
    }

    // ✅ ADD: Missing refreshData method
    public void refreshData() {
        Log.d(TAG, "Refreshing home data");
        loadHomeData();
        getCurrentLocation();
    }

    // ✅ ADD: Missing onBackPressed method
    public boolean onBackPressed() {
        // Handle back press in home fragment
        // Return true if consumed, false to pass to activity

        // If search is focused, clear it
        if (etSearch.hasFocus()) {
            etSearch.clearFocus();
            etSearch.setText("");
            return true;
        }

        // If user is not on the main home view, reset to main view
        // For now, just return false to let activity handle
        return false;
    }

    private void loadHomeData() {
        if (isLoadingData) return;

        isLoadingData = true;
        swipeRefresh.setRefreshing(true);

        loadCategories();
        loadProducts();
    }

    private void loadCategories() {
        Call<StandardResponse<List<Category>>> call = ApiClient.getProductService().getCategories();
        call.enqueue(new Callback<StandardResponse<List<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Category>>> call,
                                   @NonNull Response<StandardResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {

                    categories.clear();
                    categories.addAll(response.body().getData());
                    categoriesAdapter.notifyDataSetChanged();

                    Log.d(TAG, "Categories loaded: " + categories.size());
                } else {
                    Log.e(TAG, "Failed to load categories: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Category>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Error loading categories", t);
            }
        });
    }

    private void loadProducts() {
        // Load recent products
        loadRecentProducts();

        // Load nearby products if location is available
        if (currentLatitude != null && currentLongitude != null) {
            loadNearbyProducts();
        }

        // Load popular products
        loadPopularProducts();
    }

    private void loadRecentProducts() {
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getProducts(0, 10, null, null, null, null,
                        Constants.SORT_NEWEST, null);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleProductsResponse(response, HomeSection.SectionType.RECENT_PRODUCTS);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Error loading recent products", t);
                checkLoadingComplete();
            }
        });
    }

    private void loadNearbyProducts() {
        // TODO: Implement nearby products loading
        // For now, skip
        Log.d(TAG, "Nearby products loading not implemented yet");
    }

    private void loadPopularProducts() {
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getProducts(0, 10, null, null, null, null,
                        Constants.SORT_POPULAR, null);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleProductsResponse(response, HomeSection.SectionType.POPULAR_PRODUCTS);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Error loading popular products", t);
                checkLoadingComplete();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleProductsResponse(Response<StandardResponse<Map<String, Object>>> response,
                                        HomeSection.SectionType sectionType) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null && !productMaps.isEmpty()) {
                    List<Product> products = new ArrayList<>();

                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            products.add(product);
                        }
                    }

                    if (!products.isEmpty()) {
                        HomeSection section = new HomeSection(sectionType, products);
                        addOrUpdateSection(section);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling products response for " + sectionType, e);
        } finally {
            checkLoadingComplete();
        }
    }

    private Product parseProductFromMap(Map<String, Object> productMap) {
        try {
            Product product = new Product();

            if (productMap.get("id") != null) {
                product.setId(((Number) productMap.get("id")).longValue());
            }

            product.setTitle((String) productMap.get("title"));
            product.setDescription((String) productMap.get("description"));

            if (productMap.get("price") != null) {
                product.setPrice(new java.math.BigDecimal(productMap.get("price").toString()));
            }

            product.setLocation((String) productMap.get("location"));

            // Parse image URLs
            Object imageUrlsObj = productMap.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                product.setImageUrls((List<String>) imageUrlsObj);
            }

            // Parse condition
            String conditionStr = (String) productMap.get("condition");
            if (conditionStr != null) {
                try {
                    product.setCondition(Product.ProductCondition.valueOf(conditionStr));
                } catch (IllegalArgumentException e) {
                    product.setCondition(Product.ProductCondition.NEW);
                }
            }

            // Parse status
            String statusStr = (String) productMap.get("status");
            if (statusStr != null) {
                try {
                    product.setStatus(Product.ProductStatus.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    product.setStatus(Product.ProductStatus.AVAILABLE);
                }
            }

            product.setCreatedAt((String) productMap.get("createdAt"));
            product.setUpdatedAt((String) productMap.get("updatedAt"));

            return product;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing product from map", e);
            return null;
        }
    }

    private void addOrUpdateSection(HomeSection section) {
        // Remove existing section of same type
        homeSections.removeIf(existingSection ->
                existingSection.getType() == section.getType());

        // Add new section
        homeSections.add(section);

        // Update adapter
        if (sectionsAdapter != null) {
            sectionsAdapter.notifyDataSetChanged();
        }
    }

    private void checkLoadingComplete() {
        isLoadingData = false;
        swipeRefresh.setRefreshing(false);
    }

    private void getCurrentLocation() {
        locationManager.getCurrentLocation();
    }

    // LocationManager.LocationCallback implementation
    @Override
    public void onLocationReceived(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Log.d(TAG, "Location received: " + currentLatitude + ", " + currentLongitude);

        // Load nearby products if we just got location
        if (!isLoadingData) {
            loadNearbyProducts();
        }
    }

    @Override
    public void onLocationError(String error) {
        Log.e(TAG, "Location error: " + error);
        // Continue without location-based features
    }

    // CategoriesAdapter.OnCategoryClickListener implementation
    @Override
    public void onCategoryClick(Category category) {
        // Navigate to category products
        Intent intent = new Intent(requireContext(), com.example.newtrade.ui.search.CategoryProductsActivity.class);
        intent.putExtra(Constants.BUNDLE_CATEGORY_ID, category.getId());
        intent.putExtra(Constants.BUNDLE_CATEGORY_NAME, category.getName());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationManager != null) {
            locationManager.cleanup();
        }
    }
}