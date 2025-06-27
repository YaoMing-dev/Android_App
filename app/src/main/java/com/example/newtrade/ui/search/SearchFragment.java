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
    private static final int SEARCH_DELAY_MS = 300;

    // UI Components
    private TextInputEditText etSearch;
    private MaterialButton btnCategoryFilter, btnPriceFilter, btnLocationFilter, btnConditionFilter;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmptyState, llRecentSearches, llLoadingState;
    private TextView tvResultsCount, tvSortOption;
    private TextView tvPopularProductsTitle;

    // Data & Adapters
    private ProductAdapter productAdapter;
    private final List<Product> searchResults = new ArrayList<>();
    private final List<Product> popularProducts = new ArrayList<>(); // ✅ FIX: Add popular products
    private String currentQuery = "";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Filters
    private Long selectedCategoryId = null;
    private String selectedCategoryName = null;
    private String selectedCondition = null;
    private Double minPrice = null;
    private Double maxPrice = null;
    private String sortBy = "relevance";
    private boolean isSearching = false;

    // Location Filter
    private Double searchLatitude = null;
    private Double searchLongitude = null;
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
        // ✅ FIX: Setup RecyclerView with proper layout
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvSearchResults.setLayoutManager(layoutManager);

        productAdapter = new ProductAdapter(searchResults, product -> {
            // Handle product click
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        rvSearchResults.setAdapter(productAdapter);
    }

    private void setupListeners() {
        // ✅ FIX: Search text watcher with debouncing
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

        // Sort option
        tvSortOption.setOnClickListener(v -> showSortOptions());
    }

    // ✅ FIX: Show initial state with popular products
    private void showInitialState() {
        if (TextUtils.isEmpty(currentQuery)) {
            loadPopularProducts();
        }
    }

    // ✅ FIX: Load popular products when search is empty
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

                        for (Map<String, Object> productMap : productMaps) {
                            Product product = Product.fromMap(productMap);
                            popularProducts.add(product);
                            searchResults.add(product); // Add to search results for display
                        }

                        productAdapter.notifyDataSetChanged();

                        // Show results or empty state
                        if (searchResults.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
                            Log.d(TAG, "✅ Loaded " + searchResults.size() + " popular products");
                        }

                    } else {
                        showError("Failed to load popular products: " + standardResponse.getMessage());
                        showEmptyState(true);
                    }
                } else {
                    showError("Failed to load popular products");
                    showEmptyState(true);
                    Log.e(TAG, "Popular products response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                showLoadingState(false);
                showEmptyState(true);
                Log.e(TAG, "❌ Failed to load popular products", t);
                showError("Network error while loading popular products");
            }
        });
    }

    private void performSearch(String query) {
        Log.d(TAG, "Performing search: " + query);

        if (TextUtils.isEmpty(query)) {
            // ✅ FIX: Show popular products when search is empty
            loadPopularProducts();
            return;
        }

        showLoadingState(true);
        isSearching = true;
        isShowingPopularProducts = false;

        // Update UI for search results
        if (tvPopularProductsTitle != null) {
            tvPopularProductsTitle.setVisibility(View.GONE);
        }

        if (tvResultsCount != null) {
            tvResultsCount.setVisibility(View.VISIBLE);
        }

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.searchProductsAdvanced(
                query, selectedCategoryId, minPrice, maxPrice, selectedCondition,
                searchLatitude, searchLongitude, searchRadiusKm, sortBy, 0, 20
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
                                Product product = Product.fromMap(productMap);
                                searchResults.add(product);
                            }
                        }

                        productAdapter.notifyDataSetChanged();

                        // Update results count
                        if (tvResultsCount != null) {
                            tvResultsCount.setText(totalCount != null ?
                                    totalCount + " results found" :
                                    searchResults.size() + " results found");
                        }

                        // Show results or empty state
                        if (searchResults.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
                            Log.d(TAG, "✅ Search completed: " + searchResults.size() + " results");
                        }

                    } else {
                        showError("Search failed: " + standardResponse.getMessage());
                        showEmptyState(true);
                    }
                } else {
                    showError("Search failed");
                    showEmptyState(true);
                    Log.e(TAG, "Search response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                showLoadingState(false);
                showEmptyState(true);
                isSearching = false;
                Log.e(TAG, "❌ Search failed", t);
                showError("Network error during search");
            }
        });
    }

    // ===== FILTER METHODS =====

    private void showCategoryFilter() {
        // TODO: Implement category filter dialog
        Toast.makeText(requireContext(), "Category filter coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showPriceFilter() {
        // TODO: Implement price filter dialog
        Toast.makeText(requireContext(), "Price filter coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showLocationFilter() {
        // TODO: Implement location filter dialog
        Toast.makeText(requireContext(), "Location filter coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showConditionFilter() {
        // TODO: Implement condition filter dialog
        Toast.makeText(requireContext(), "Condition filter coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showSortOptions() {
        String[] sortOptions = {"Relevance", "Price: Low to High", "Price: High to Low", "Newest First", "Oldest First"};
        String[] sortValues = {"relevance", "price_asc", "price_desc", "newest", "oldest"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sort by");
        builder.setItems(sortOptions, (dialog, which) -> {
            sortBy = sortValues[which];
            tvSortOption.setText(sortOptions[which]);

            // Re-perform search with new sort
            if (!TextUtils.isEmpty(currentQuery)) {
                performSearch(currentQuery);
            } else if (isShowingPopularProducts) {
                loadPopularProducts();
            }
        });
        builder.show();
    }

    // ===== UI STATE METHODS =====

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