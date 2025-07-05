// app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.adapters.RecentSearchAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final int SEARCH_DELAY_MS = 300;

    // UI Components
    private TextInputLayout tilSearch;
    private EditText etSearch;
    private Button btnCategoryFilter, btnPriceFilter, btnLocationFilter, btnConditionFilter;
    private ChipGroup chipGroupFilters;
    private TextView tvResultsCount, tvSortOption;
    private RecyclerView rvSearchResults, rvRecentSearches;
    private LinearLayout llEmptyState, llLoadingState, llRecentSearches;

    // Adapters
    private ProductAdapter productAdapter;
    private RecentSearchAdapter recentSearchAdapter;

    // Data
    private List<Product> searchResults = new ArrayList<>();
    private List<String> recentSearches = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    // Search state
    private String currentQuery = "";
    private String selectedCategory = "";
    private double minPrice = 0.0;
    private double maxPrice = 0.0;
    private String selectedCondition = "";
    private String selectedLocation = "";
    private String currentSort = "relevance";
    private boolean isLoading = false;
    private int currentPage = 0;

    // Handler for search delay
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initUtils();
        setupAdapters();
        setupListeners();
        loadRecentSearches();
        showRecentSearches();

        Log.d(TAG, "✅ SearchFragment initialized");
    }

    private void initViews(View view) {
        tilSearch = view.findViewById(R.id.til_search);
        etSearch = view.findViewById(R.id.et_search);
        btnCategoryFilter = view.findViewById(R.id.btn_category_filter);
        btnPriceFilter = view.findViewById(R.id.btn_price_filter);
        btnLocationFilter = view.findViewById(R.id.btn_location_filter);
        btnConditionFilter = view.findViewById(R.id.btn_condition_filter);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        tvResultsCount = view.findViewById(R.id.tv_results_count);
        tvSortOption = view.findViewById(R.id.tv_sort_option);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        rvRecentSearches = view.findViewById(R.id.rv_recent_searches);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        llLoadingState = view.findViewById(R.id.ll_loading_state);
        llRecentSearches = view.findViewById(R.id.ll_recent_searches);

        Log.d(TAG, "✅ Views initialized");
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void setupAdapters() {
        // Product adapter
        productAdapter = new ProductAdapter(searchResults, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                navigateToProductDetail(product);
            }

            @Override
            public void onProductSave(Product product) {
                toggleSaveProduct(product);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvSearchResults.setLayoutManager(gridLayoutManager);
        rvSearchResults.setAdapter(productAdapter);

        // Recent search adapter
        recentSearchAdapter = new RecentSearchAdapter(recentSearches, new RecentSearchAdapter.OnRecentSearchClickListener() {
            @Override
            public void onRecentSearchClick(String query) {
                performSearch(query);
            }

            @Override
            public void onRecentSearchDelete(String query) {
                deleteRecentSearch(query);
            }
        });

        LinearLayoutManager recentSearchLayoutManager = new LinearLayoutManager(requireContext());
        rvRecentSearches.setLayoutManager(recentSearchLayoutManager);
        rvRecentSearches.setAdapter(recentSearchAdapter);

        Log.d(TAG, "✅ Adapters set up");
    }

    private void setupListeners() {
        // Search input text watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    showRecentSearches();
                    return;
                }

                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Schedule new search
                searchRunnable = () -> performSearch(query);
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

        Log.d(TAG, "✅ Listeners set up");
    }

    private void loadRecentSearches() {
        String[] searches = prefsManager.getRecentSearches();
        recentSearches.clear();
        for (String search : searches) {
            recentSearches.add(search);
        }
        recentSearchAdapter.notifyDataSetChanged();
    }

    private void showRecentSearches() {
        rvSearchResults.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);
        llLoadingState.setVisibility(View.GONE);

        if (recentSearches.isEmpty()) {
            llRecentSearches.setVisibility(View.GONE);
        } else {
            llRecentSearches.setVisibility(View.VISIBLE);
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            showRecentSearches();
            return;
        }

        // Update search input if different
        if (!query.equals(etSearch.getText().toString().trim())) {
            etSearch.setText(query);
        }

        currentQuery = query;
        currentPage = 0;

        // Save to recent searches
        prefsManager.saveRecentSearch(query);
        loadRecentSearches();

        // Show loading state
        showLoadingState();

        // Perform search
        searchProducts();
    }

    private void searchProducts() {
        if (isLoading) return;

        isLoading = true;

        Log.d(TAG, "🔍 Searching products: " + currentQuery);

        ApiClient.getProductService().searchProducts(
                currentQuery,
                currentPage,
                Constants.SEARCH_PAGE_SIZE,
                selectedCategory.isEmpty() ? null : selectedCategory,
                minPrice > 0 ? minPrice : null,
                maxPrice > 0 ? maxPrice : null,
                selectedCondition.isEmpty() ? null : selectedCondition,
                selectedLocation.isEmpty() ? null : selectedLocation,
                null, // radius
                currentSort
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        handleSearchResults(apiResponse.getData());
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Search failed");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                isLoading = false;
                Log.e(TAG, "❌ Search failed", t);
                showError("Network error. Please try again.");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleSearchResults(Map<String, Object> data) {
        try {
            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");
            Number totalElements = (Number) data.get("totalElements");

            if (productList != null) {
                searchResults.clear();
                for (Map<String, Object> productData : productList) {
                    Product product = Product.fromMap(productData);
                    searchResults.add(product);
                }

                productAdapter.notifyDataSetChanged();

                // Update results count
                if (totalElements != null) {
                    tvResultsCount.setText(totalElements.intValue() + " results found");
                } else {
                    tvResultsCount.setText(searchResults.size() + " results found");
                }

                // Show results
                showSearchResults();

                Log.d(TAG, "✅ Search results loaded: " + searchResults.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing search results", e);
            showError("Error processing search results");
        }
    }

    private void showLoadingState() {
        llRecentSearches.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);
        llLoadingState.setVisibility(View.VISIBLE);
    }

    private void showSearchResults() {
        llRecentSearches.setVisibility(View.GONE);
        llLoadingState.setVisibility(View.GONE);

        if (searchResults.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToProductDetail(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId());
        intent.putExtra(Constants.EXTRA_PRODUCT_TITLE, product.getTitle());
        intent.putExtra(Constants.EXTRA_PRODUCT_PRICE, product.getFormattedPrice());
        startActivity(intent);
    }

    private void toggleSaveProduct(Product product) {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "Please log in to save products", Toast.LENGTH_SHORT).show();
            return;
        }

        if (product.isSaved()) {
            // Remove from saved
            ApiClient.getSavedItemService().removeSavedItem(userId, product.getId())
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                               Response<StandardResponse<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();
                                if (apiResponse.isSuccess()) {
                                    product.setSaved(false);
                                    productAdapter.notifyDataSetChanged();
                                    Toast.makeText(requireContext(), "Removed from saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                            Log.e(TAG, "❌ Failed to remove from saved", t);
                        }
                    });
        } else {
            // Add to saved
            ApiClient.getSavedItemService().saveItem(userId, product.getId())
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                               Response<StandardResponse<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();
                                if (apiResponse.isSuccess()) {
                                    product.setSaved(true);
                                    productAdapter.notifyDataSetChanged();
                                    Toast.makeText(requireContext(), "Added to saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                            Log.e(TAG, "❌ Failed to save item", t);
                        }
                    });
        }
    }

    private void deleteRecentSearch(String query) {
        // Remove from recent searches
        recentSearches.remove(query);
        recentSearchAdapter.notifyDataSetChanged();

        // Update preferences
        // In a real app, you would implement this in SharedPrefsManager
        Toast.makeText(requireContext(), "Search removed", Toast.LENGTH_SHORT).show();
    }

    private void showCategoryFilter() {
        // Show category filter dialog
        Toast.makeText(requireContext(), "Category filter", Toast.LENGTH_SHORT).show();
    }

    private void showPriceFilter() {
        // Show price filter dialog
        Toast.makeText(requireContext(), "Price filter", Toast.LENGTH_SHORT).show();
    }

    private void showLocationFilter() {
        // Show location filter dialog
        Toast.makeText(requireContext(), "Location filter", Toast.LENGTH_SHORT).show();
    }

    private void showConditionFilter() {
        // Show condition filter dialog
        Toast.makeText(requireContext(), "Condition filter", Toast.LENGTH_SHORT).show();
    }

    private void showSortOptions() {
        // Show sort options dialog
        Toast.makeText(requireContext(), "Sort options", Toast.LENGTH_SHORT).show();
    }

    private void updateFilterChips() {
        chipGroupFilters.removeAllViews();

        // Add active filter chips
        if (!selectedCategory.isEmpty()) {
            addFilterChip("Category: " + selectedCategory, () -> {
                selectedCategory = "";
                updateFilterChips();
                performSearch(currentQuery);
            });
        }

        if (minPrice > 0 || maxPrice > 0) {
            String priceText = "Price: ";
            if (minPrice > 0 && maxPrice > 0) {
                priceText += Constants.formatPrice(minPrice) + " - " + Constants.formatPrice(maxPrice);
            } else if (minPrice > 0) {
                priceText += "From " + Constants.formatPrice(minPrice);
            } else {
                priceText += "Up to " + Constants.formatPrice(maxPrice);
            }

            addFilterChip(priceText, () -> {
                minPrice = 0.0;
                maxPrice = 0.0;
                updateFilterChips();
                performSearch(currentQuery);
            });
        }

        if (!selectedCondition.isEmpty()) {
            addFilterChip("Condition: " + selectedCondition, () -> {
                selectedCondition = "";
                updateFilterChips();
                performSearch(currentQuery);
            });
        }

        if (!selectedLocation.isEmpty()) {
            addFilterChip("Location: " + selectedLocation, () -> {
                selectedLocation = "";
                updateFilterChips();
                performSearch(currentQuery);
            });
        }
    }

    private void addFilterChip(String text, Runnable onRemove) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> onRemove.run());
        chipGroupFilters.addView(chip);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        Log.w(TAG, "Error: " + message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        Log.d(TAG, "SearchFragment destroyed");
    }
}