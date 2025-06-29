// app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final int SEARCH_DELAY_MS = 200; // FR-3.1.2: 200ms debounce

    // UI Components
    private TextInputEditText etSearch;
    private MaterialButton btnCategoryFilter, btnPriceFilter, btnLocationFilter, btnConditionFilter, btnSortFilter;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmptyState, llRecentSearches, llLoadingState;
    private TextView tvResultsCount, tvSortOption;
    private TextView tvPopularProductsTitle;

    // Data & Adapters
    private ProductAdapter productAdapter;
    private final List<Product> searchResults = new ArrayList<>();
    private final List<Product> popularProducts = new ArrayList<>();
    private String currentQuery = "";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // FR-3.1.1: Advanced Filters
    private Long selectedCategoryId = null;
    private String selectedCategoryName = null;
    private String selectedCondition = null;
    private Double minPrice = null;
    private Double maxPrice = null;
    private String sortBy = "relevance"; // FR-3.1.3: Default sort
    private boolean isSearching = false;

    // FR-6.1: Location Filter for radius search
    private Double searchLatitude = null;
    private Double searchLongitude = null;
    private Integer searchRadius = 50; // Default 50km radius

    // FR-3.1.3: Sort options
    private static final String[] SORT_OPTIONS = {
        "relevance", "newest", "price_low", "price_high", "distance"
    };
    private static final String[] SORT_LABELS = {
        "Relevance", "Newest First", "Price: Low to High", "Price: High to Low", "Distance"
    };

    // FR-3.1.1: Condition options
    private static final String[] CONDITION_OPTIONS = {
        "NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"
    };
    private static final String[] CONDITION_LABELS = {
        "New", "Like New", "Good", "Fair", "Poor"
    };

    // Location Filter
    private String searchLocationName = "";
    private int searchRadiusKm = 10;

    // State management
    private boolean isShowingPopularProducts = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupListeners();
        showInitialState();

        Log.d(TAG, "✅ SearchFragment created successfully");
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        btnCategoryFilter = view.findViewById(R.id.btn_category_filter);
        btnPriceFilter = view.findViewById(R.id.btn_price_filter);
        btnLocationFilter = view.findViewById(R.id.btn_location_filter);
        btnConditionFilter = view.findViewById(R.id.btn_condition_filter);
        btnSortFilter = view.findViewById(R.id.btn_sort_filter);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        llRecentSearches = view.findViewById(R.id.ll_recent_searches);
        llLoadingState = view.findViewById(R.id.ll_loading_state);
        tvResultsCount = view.findViewById(R.id.tv_results_count);
        tvSortOption = view.findViewById(R.id.tv_sort_option);
        tvPopularProductsTitle = view.findViewById(R.id.tv_popular_products_title);
    }

    private void setupRecyclerViews() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvSearchResults.setLayoutManager(layoutManager);

        productAdapter = new ProductAdapter(searchResults, product -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        rvSearchResults.setAdapter(productAdapter);
    }

    private void setupListeners() {
        // Search text watcher with debouncing
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (!query.equals(currentQuery)) {
                        currentQuery = query;
                        performSearch(query);
                    }
                };

                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter buttons
        btnCategoryFilter.setOnClickListener(v -> showCategoryFilter());
        btnPriceFilter.setOnClickListener(v -> showPriceFilter());
        btnLocationFilter.setOnClickListener(v -> showLocationFilter());
        btnConditionFilter.setOnClickListener(v -> showConditionFilter());
        btnSortFilter.setOnClickListener(v -> showSortOptions());

        // Sort option
        tvSortOption.setOnClickListener(v -> showSortOptions());
    }

    private void showInitialState() {
        if (TextUtils.isEmpty(currentQuery)) {
            loadPopularProducts();
        }
    }

    private void loadPopularProducts() {
        Log.d(TAG, "Loading popular products...");

        showLoadingState(true);
        isShowingPopularProducts = true;

        // Update UI to show popular products
        if (tvPopularProductsTitle != null) {
            tvPopularProductsTitle.setVisibility(View.VISIBLE);
            tvPopularProductsTitle.setText("Popular Products");
        }

        if (tvResultsCount != null) {
            tvResultsCount.setVisibility(View.GONE);
        }

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<List<Map<String, Object>>>> call = apiService.getFeaturedProducts();

        call.enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                   Response<StandardResponse<List<Map<String, Object>>>> response) {

                showLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<List<Map<String, Object>>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        List<Map<String, Object>> productMaps = standardResponse.getData();

                        popularProducts.clear();
                        searchResults.clear();

                        if (productMaps != null) {
                            for (Map<String, Object> productMap : productMaps) {
                                try {
                                    // ✅ FIX: Use Product.fromMap method
                                    Product product = Product.fromMap(productMap);
                                    popularProducts.add(product);
                                    searchResults.add(product); // Also add to search results for display
                                } catch (Exception e) {
                                    Log.w(TAG, "Error parsing popular product", e);
                                }
                            }
                        }

                        productAdapter.notifyDataSetChanged();

                        if (searchResults.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
                        }

                        Log.d(TAG, "✅ Popular products loaded: " + popularProducts.size());
                    }
                } else {
                    Log.e(TAG, "Failed to load popular products");
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                showLoadingState(false);
                showEmptyState(true);
                Log.e(TAG, "Error loading popular products", t);
            }
        });
    }

    private void performSearch(String query) {
        if (TextUtils.isEmpty(query)) {
            loadPopularProducts();
            return;
        }

        if (isSearching) return;

        Log.d(TAG, "Performing search: " + query);

        isSearching = true;
        isShowingPopularProducts = false;
        showLoadingState(true);

        // Update UI for search results
        if (tvPopularProductsTitle != null) {
            tvPopularProductsTitle.setVisibility(View.GONE);
        }

        if (tvResultsCount != null) {
            tvResultsCount.setVisibility(View.VISIBLE);
        }

        ApiService apiService = ApiClient.getApiService();
        // ✅ FIX: Use searchProductsAdvanced method
        Call<StandardResponse<Map<String, Object>>> call = apiService.searchProductsAdvanced(
                query, selectedCategoryId, minPrice, maxPrice, selectedCondition,
                searchLatitude, searchLongitude, searchRadius, sortBy, 0, 20
        );

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                showLoadingState(false);
                isSearching = false;

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        Map<String, Object> data = standardResponse.getData();
                        List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("products");
                        Integer totalCount = (Integer) data.get("totalCount");

                        searchResults.clear();

                        if (productMaps != null) {
                            for (Map<String, Object> productMap : productMaps) {
                                try {
                                    // ✅ FIX: Use Product.fromMap method
                                    Product product = Product.fromMap(productMap);
                                    searchResults.add(product);
                                } catch (Exception e) {
                                    Log.w(TAG, "Error parsing search result", e);
                                }
                            }
                        }

                        productAdapter.notifyDataSetChanged();

                        // Update results count
                        if (tvResultsCount != null) {
                            tvResultsCount.setText(totalCount != null ?
                                    totalCount + " results found" : searchResults.size() + " results found");
                        }

                        if (searchResults.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
                        }

                        Log.d(TAG, "✅ Search completed: " + searchResults.size() + " results");
                    }
                } else {
                    Log.e(TAG, "Search failed: " + response.code());
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                showLoadingState(false);
                isSearching = false;
                showEmptyState(true);
                showError("Search failed: " + t.getMessage());
                Log.e(TAG, "Search request failed", t);
            }
        });
    }

    // Filter methods (placeholder implementations)
    private void showCategoryFilter() {
        Toast.makeText(requireContext(), "Category filter coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showPriceFilter() {
        Toast.makeText(requireContext(), "Price filter coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showLocationFilter() {
        Toast.makeText(requireContext(), "Location filter coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showConditionFilter() {
        Toast.makeText(requireContext(), "Condition filter coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showSortOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sort by");
        builder.setItems(SORT_LABELS, (dialog, which) -> {
            sortBy = SORT_OPTIONS[which];
            tvSortOption.setText(SORT_LABELS[which]);

            // Re-perform search with new sort
            if (!TextUtils.isEmpty(currentQuery)) {
                performSearch(currentQuery);
            } else if (isShowingPopularProducts) {
                loadPopularProducts();
            }
        });
        builder.show();
    }

    // UI State methods
    private void showLoadingState(boolean show) {
        if (llLoadingState != null) {
            llLoadingState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyState(boolean show) {
        if (llEmptyState != null) {
            llEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clean up search handler
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}