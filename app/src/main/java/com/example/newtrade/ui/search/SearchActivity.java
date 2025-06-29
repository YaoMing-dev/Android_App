package com.example.newtrade.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.CategoryAdapter;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.location.LocationService;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.PagedResponse;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity implements LocationService.LocationCallback {

    private static final String TAG = "SearchActivity";
    private static final long SEARCH_DELAY = 200; // 200ms delay as per FR-3.1.2

    // UI Components
    private MaterialToolbar toolbar;
    private EditText etSearch;
    private ImageButton btnFilter;
    private ChipGroup chipCategories;
    private RecyclerView rvProducts;
    private LinearLayout llEmptyState;
    private TextView tvResultsCount;
    private MaterialButton btnSortRelevance, btnSortNewest, btnSortPriceLow, btnSortPriceHigh;

    // Filter components
    private LinearLayout llFilterSection;
    private EditText etMinPrice, etMaxPrice;
    private ChipGroup chipConditions;
    private TextView tvLocationFilter;
    private MaterialButton btnApplyFilters, btnClearFilters;

    // Data
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> products = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private SharedPrefsManager prefsManager;
    private LocationService locationService;

    // Search parameters
    private String currentQuery = "";
    private String selectedCategory = "";
    private Double minPrice = null;
    private Double maxPrice = null;
    private String selectedCondition = "";
    private String sortBy = "relevance";
    private Double userLatitude = null;
    private Double userLongitude = null;
    private int radiusKm = 50; // Default 50km radius

    // Handlers
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        prefsManager = SharedPrefsManager.getInstance(this);
        locationService = new LocationService(this);
        locationService.setLocationCallback(this);

        initViews();
        setupToolbar();
        setupRecyclerViews();
        setupListeners();
        loadCategories();
        getUserLocation();

        // Handle search query from intent
        handleSearchIntent();

        Log.d(TAG, "SearchActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.et_search);
        btnFilter = findViewById(R.id.btn_filter);
        chipCategories = findViewById(R.id.chip_categories);
        rvProducts = findViewById(R.id.rv_products);
        llEmptyState = findViewById(R.id.ll_empty_state);
        tvResultsCount = findViewById(R.id.tv_results_count);

        // Sort buttons
        btnSortRelevance = findViewById(R.id.btn_sort_relevance);
        btnSortNewest = findViewById(R.id.btn_sort_newest);
        btnSortPriceLow = findViewById(R.id.btn_sort_price_low);
        btnSortPriceHigh = findViewById(R.id.btn_sort_price_high);

        // Filter section
        llFilterSection = findViewById(R.id.ll_filter_section);
        etMinPrice = findViewById(R.id.et_min_price);
        etMaxPrice = findViewById(R.id.et_max_price);
        chipConditions = findViewById(R.id.chip_conditions);
        tvLocationFilter = findViewById(R.id.tv_location_filter);
        btnApplyFilters = findViewById(R.id.btn_apply_filters);
        btnClearFilters = findViewById(R.id.btn_clear_filters);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "Search");
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView
        LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        chipCategories.setLayoutManager(categoryLayoutManager);

        // Products RecyclerView
        GridLayoutManager productLayoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(productLayoutManager);

        productAdapter = new ProductAdapter(products, product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        rvProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        // Search input with 200ms delay
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();

                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Schedule new search with delay
                searchRunnable = () -> performSearch();
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter toggle
        btnFilter.setOnClickListener(v -> toggleFilterSection());

        // Sort buttons
        btnSortRelevance.setOnClickListener(v -> setSortBy("relevance"));
        btnSortNewest.setOnClickListener(v -> setSortBy("newest"));
        btnSortPriceLow.setOnClickListener(v -> setSortBy("price_asc"));
        btnSortPriceHigh.setOnClickListener(v -> setSortBy("price_desc"));

        // Filter actions
        btnApplyFilters.setOnClickListener(v -> applyFilters());
        btnClearFilters.setOnClickListener(v -> clearFilters());

        // Location filter
        tvLocationFilter.setOnClickListener(v -> getUserLocation());
    }

    private void handleSearchIntent() {
        Intent intent = getIntent();
        String query = intent.getStringExtra("search_query");
        String category = intent.getStringExtra("category");

        if (query != null && !query.isEmpty()) {
            etSearch.setText(query);
            currentQuery = query;
        }

        if (category != null && !category.isEmpty()) {
            selectedCategory = category;
        }

        // Perform initial search if we have query or category
        if (!currentQuery.isEmpty() || !selectedCategory.isEmpty()) {
            performSearch();
        }
    }

    private void loadCategories() {
        // This would typically load from API
        // For now, create sample categories
        categories.clear();
        categories.add(new Category(1L, "Electronics", "📱"));
        categories.add(new Category(2L, "Furniture", "🪑"));
        categories.add(new Category(3L, "Clothing", "👕"));
        categories.add(new Category(4L, "Books", "📚"));
        categories.add(new Category(5L, "Sports", "⚽"));
        categories.add(new Category(6L, "Vehicles", "🚗"));

        // Add category chips
        chipCategories.removeAllViews();
        for (Category category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category.getIcon() + " " + category.getName());
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategory = category.getName();
                    // Uncheck other chips
                    for (int i = 0; i < chipCategories.getChildCount(); i++) {
                        Chip otherChip = (Chip) chipCategories.getChildAt(i);
                        if (otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                } else {
                    selectedCategory = "";
                }
                performSearch();
            });
            chipCategories.addView(chip);
        }

        Log.d(TAG, "Categories loaded: " + categories.size());
    }

    private void getUserLocation() {
        tvLocationFilter.setText("Getting location...");
        locationService.getCurrentLocation();
    }

    @Override
    public void onLocationReceived(double latitude, double longitude, String address) {
        runOnUiThread(() -> {
            userLatitude = latitude;
            userLongitude = longitude;
            tvLocationFilter.setText("📍 " + address + " (50km)");

            // Refresh search with location
            if (!currentQuery.isEmpty() || !selectedCategory.isEmpty()) {
                performSearch();
            }
        });
    }

    @Override
    public void onLocationError(String error) {
        runOnUiThread(() -> {
            tvLocationFilter.setText("📍 Location not available");
            Log.w(TAG, "Location error: " + error);
        });
    }

    private void performSearch() {
        Log.d(TAG, "Performing search - Query: '" + currentQuery + "', Category: '" + selectedCategory + "'");

        // Prepare search request
        Map<String, Object> searchRequest = new HashMap<>();

        if (!currentQuery.isEmpty()) {
            searchRequest.put("search", currentQuery);
        }

        if (!selectedCategory.isEmpty()) {
            searchRequest.put("category", selectedCategory);
        }

        if (minPrice != null) {
            searchRequest.put("minPrice", minPrice);
        }

        if (maxPrice != null) {
            searchRequest.put("maxPrice", maxPrice);
        }

        if (!selectedCondition.isEmpty()) {
            searchRequest.put("condition", selectedCondition);
        }

        if (userLatitude != null && userLongitude != null) {
            searchRequest.put("latitude", userLatitude);
            searchRequest.put("longitude", userLongitude);
            searchRequest.put("radius", radiusKm);
        }

        searchRequest.put("sort", sortBy);
        searchRequest.put("page", 0);
        searchRequest.put("size", 20);

        // Call API
        ApiClient.getApiService().searchProductsAdvanced(searchRequest)
            .enqueue(new Callback<StandardResponse<PagedResponse<Product>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<PagedResponse<Product>>> call,
                                       @NonNull Response<StandardResponse<PagedResponse<Product>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<PagedResponse<Product>> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            PagedResponse<Product> pagedResponse = apiResponse.getData();
                            List<Product> searchResults = pagedResponse != null ? pagedResponse.getContent() : new ArrayList<>();

                            updateSearchResults(searchResults);

                            Log.d(TAG, "✅ Search completed: " + searchResults.size() + " results");
                        } else {
                            showError("Search failed");
                        }
                    } else {
                        showError("Search failed");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<PagedResponse<Product>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Search API error", t);
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void updateSearchResults(List<Product> searchResults) {
        products.clear();
        products.addAll(searchResults);
        productAdapter.notifyDataSetChanged();

        // Update results count
        tvResultsCount.setText(products.size() + " results found");

        // Show/hide empty state
        if (products.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private void toggleFilterSection() {
        if (llFilterSection.getVisibility() == View.VISIBLE) {
            llFilterSection.setVisibility(View.GONE);
            btnFilter.setImageResource(R.drawable.ic_filter_list);
        } else {
            llFilterSection.setVisibility(View.VISIBLE);
            btnFilter.setImageResource(R.drawable.ic_filter_list_off);
        }
    }

    private void setSortBy(String newSortBy) {
        sortBy = newSortBy;

        // Update button states
        resetSortButtons();
        switch (sortBy) {
            case "relevance":
                btnSortRelevance.setTextColor(getColor(R.color.primary));
                break;
            case "newest":
                btnSortNewest.setTextColor(getColor(R.color.primary));
                break;
            case "price_asc":
                btnSortPriceLow.setTextColor(getColor(R.color.primary));
                break;
            case "price_desc":
                btnSortPriceHigh.setTextColor(getColor(R.color.primary));
                break;
        }

        performSearch();
    }

    private void resetSortButtons() {
        int defaultColor = getColor(R.color.text_secondary);
        btnSortRelevance.setTextColor(defaultColor);
        btnSortNewest.setTextColor(defaultColor);
        btnSortPriceLow.setTextColor(defaultColor);
        btnSortPriceHigh.setTextColor(defaultColor);
    }

    private void applyFilters() {
        // Get price filters
        String minPriceStr = etMinPrice.getText().toString().trim();
        String maxPriceStr = etMaxPrice.getText().toString().trim();

        try {
            minPrice = minPriceStr.isEmpty() ? null : Double.parseDouble(minPriceStr);
            maxPrice = maxPriceStr.isEmpty() ? null : Double.parseDouble(maxPriceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get condition filter
        selectedCondition = "";
        for (int i = 0; i < chipConditions.getChildCount(); i++) {
            Chip chip = (Chip) chipConditions.getChildAt(i);
            if (chip.isChecked()) {
                selectedCondition = chip.getText().toString();
                break;
            }
        }

        // Apply filters
        performSearch();
        toggleFilterSection();
    }

    private void clearFilters() {
        etMinPrice.setText("");
        etMaxPrice.setText("");
        minPrice = null;
        maxPrice = null;
        selectedCondition = "";

        // Uncheck condition chips
        for (int i = 0; i < chipConditions.getChildCount(); i++) {
            Chip chip = (Chip) chipConditions.getChildAt(i);
            chip.setChecked(false);
        }

        performSearch();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}
