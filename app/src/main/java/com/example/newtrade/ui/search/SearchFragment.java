// File: app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

import android.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
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
    private TextInputEditText etSearch;
    private MaterialButton btnCategoryFilter, btnPriceFilter, btnLocationFilter, btnConditionFilter;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmptyState, llRecentSearches;
    private TextView tvResultsCount, tvSortOption;

    // Data & Adapters
    private ProductAdapter searchAdapter;
    private final List<Product> searchResults = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
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
        loadCategories();
        showInitialState();

        Log.d(TAG, "SearchFragment created successfully");
    }

    private void initViews(View view) {
        try {
            etSearch = view.findViewById(R.id.et_search);
            btnCategoryFilter = view.findViewById(R.id.btn_category_filter);
            btnPriceFilter = view.findViewById(R.id.btn_price_filter);
            btnLocationFilter = view.findViewById(R.id.btn_location_filter);
            btnConditionFilter = view.findViewById(R.id.btn_condition_filter);
            chipGroupFilters = view.findViewById(R.id.chip_group_filters);
            rvSearchResults = view.findViewById(R.id.rv_search_results);
            llEmptyState = view.findViewById(R.id.ll_empty_state);
            llRecentSearches = view.findViewById(R.id.ll_recent_searches);
            tvResultsCount = view.findViewById(R.id.tv_results_count);
            tvSortOption = view.findViewById(R.id.tv_sort_option);

            Log.d(TAG, "✅ SearchFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some SearchFragment views not found: " + e.getMessage());
        }
    }

    private void setupRecyclerViews() {
        if (rvSearchResults != null) {
            searchAdapter = new ProductAdapter(searchResults, this::navigateToProductDetail);
            rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
            rvSearchResults.setAdapter(searchAdapter);
        }
    }

    private void setupListeners() {
        try {
            // 🔥 SEARCH INPUT WITH DELAY
            if (etSearch != null) {
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        currentQuery = s.toString().trim();

                        // Remove previous search callback
                        if (searchRunnable != null) {
                            searchHandler.removeCallbacks(searchRunnable);
                        }

                        // Schedule new search
                        searchRunnable = () -> performSearch();
                        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }

            // 🔥 FILTER BUTTONS
            if (btnCategoryFilter != null) {
                btnCategoryFilter.setOnClickListener(v -> showCategoryFilterDialog());
            }

            if (btnPriceFilter != null) {
                btnPriceFilter.setOnClickListener(v -> showPriceFilterDialog());
            }

            if (btnConditionFilter != null) {
                btnConditionFilter.setOnClickListener(v -> showConditionFilterDialog());
            }

            if (btnLocationFilter != null) {
                btnLocationFilter.setOnClickListener(v -> showLocationFilterDialog());
            }

            // 🔥 SORT OPTIONS
            if (tvSortOption != null) {
                tvSortOption.setOnClickListener(v -> showSortDialog());
            }

            Log.d(TAG, "✅ SearchFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }

    private void loadCategories() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                   Response<StandardResponse<List<Map<String, Object>>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Map<String, Object>> categoryData = response.body().getData();
                        categories.clear();

                        for (Map<String, Object> data : categoryData) {
                            Category category = new Category();
                            category.setId(((Number) data.get("id")).longValue());
                            category.setName((String) data.get("name"));
                            categories.add(category);
                        }

                        Log.d(TAG, "✅ Loaded " + categories.size() + " categories for search");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing categories", e);
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                Log.e(TAG, "❌ Categories API call failed", t);
            }
        });
    }

    private void showInitialState() {
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.VISIBLE);
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.GONE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
        if (tvResultsCount != null) {
            tvResultsCount.setVisibility(View.GONE);
        }
    }

    private void performSearch() {
        if (isSearching) return;

        Log.d(TAG, "🔍 Performing search: '" + currentQuery + "'");

        if (currentQuery.isEmpty()) {
            showInitialState();
            searchResults.clear();
            if (searchAdapter != null) {
                searchAdapter.notifyDataSetChanged();
            }
            return;
        }

        isSearching = true;
        showLoadingState();

        // 🔥 CALL SEARCH API
        ApiClient.getApiService().searchProducts(
                currentQuery,
                0, // page
                20, // size
                selectedCategoryId,
                selectedCondition,
                minPrice,
                maxPrice
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                isSearching = false;

                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Map<String, Object> data = response.body().getData();
                        List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                        searchResults.clear();

                        if (productList != null && !productList.isEmpty()) {
                            for (Map<String, Object> productData : productList) {
                                Product product = new Product();
                                product.setId(((Number) productData.get("id")).longValue());
                                product.setTitle((String) productData.get("title"));
                                product.setDescription((String) productData.get("description"));

                                // Handle price conversion
                                Object priceObj = productData.get("price");
                                if (priceObj instanceof Number) {
                                    product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                                }

                                product.setLocation((String) productData.get("location"));

                                // Handle imageUrls
                                Object imageUrlsObj = productData.get("imageUrls");
                                if (imageUrlsObj instanceof List) {
                                    product.setImageUrls((List<String>) imageUrlsObj);
                                } else if (imageUrlsObj instanceof String) {
                                    product.setImageUrl((String) imageUrlsObj);
                                }

                                // Handle condition
                                String condition = (String) productData.get("condition");
                                if (condition != null) {
                                    try {
                                        product.setCondition(Product.ProductCondition.valueOf(condition));
                                    } catch (IllegalArgumentException e) {
                                        product.setCondition(Product.ProductCondition.GOOD);
                                    }
                                }

                                searchResults.add(product);
                            }

                            showSearchResults();
                        } else {
                            showEmptyResults();
                        }

                        if (searchAdapter != null) {
                            searchAdapter.notifyDataSetChanged();
                        }

                        Log.d(TAG, "✅ Search completed: " + searchResults.size() + " results");
                    } else {
                        Log.w(TAG, "❌ Search response unsuccessful");
                        showEmptyResults();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing search results", e);
                    showEmptyResults();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                isSearching = false;
                Log.e(TAG, "❌ Search API call failed", t);
                showEmptyResults();
                Toast.makeText(getContext(), "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadingState() {
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.GONE);
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.VISIBLE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
        if (tvResultsCount != null) {
            tvResultsCount.setVisibility(View.VISIBLE);
            tvResultsCount.setText("Searching...");
        }
    }

    private void showSearchResults() {
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.GONE);
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.VISIBLE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
        if (tvResultsCount != null) {
            tvResultsCount.setVisibility(View.VISIBLE);
            tvResultsCount.setText("Found " + searchResults.size() + " products");
        }
    }

    private void showEmptyResults() {
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.GONE);
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.GONE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.VISIBLE);
        }
        if (tvResultsCount != null) {
            tvResultsCount.setVisibility(View.VISIBLE);
            tvResultsCount.setText("No products found");
        }
    }

    // 🔥 FILTER DIALOGS
    private void showCategoryFilterDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(getContext(), "Loading categories...", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Category");

        // Create category list
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All Categories");
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        String[] categoryArray = categoryNames.toArray(new String[0]);
        int selectedIndex = 0;
        if (selectedCategoryName != null) {
            for (int i = 0; i < categoryArray.length; i++) {
                if (categoryArray[i].equals(selectedCategoryName)) {
                    selectedIndex = i;
                    break;
                }
            }
        }

        builder.setSingleChoiceItems(categoryArray, selectedIndex, (dialog, which) -> {
            if (which == 0) {
                // All Categories
                selectedCategoryId = null;
                selectedCategoryName = null;
                btnCategoryFilter.setText("Category");
            } else {
                Category selectedCategory = categories.get(which - 1);
                selectedCategoryId = selectedCategory.getId();
                selectedCategoryName = selectedCategory.getName();
                btnCategoryFilter.setText(selectedCategoryName);
            }

            updateFilterChips();
            performSearch();
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showConditionFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Condition");

        String[] conditions = {"All Conditions", "NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};
        int selectedIndex = 0;
        if (selectedCondition != null) {
            for (int i = 0; i < conditions.length; i++) {
                if (conditions[i].equals(selectedCondition)) {
                    selectedIndex = i;
                    break;
                }
            }
        }

        builder.setSingleChoiceItems(conditions, selectedIndex, (dialog, which) -> {
            if (which == 0) {
                selectedCondition = null;
                btnConditionFilter.setText("Condition");
            } else {
                selectedCondition = conditions[which];
                btnConditionFilter.setText(selectedCondition.replace("_", " "));
            }

            updateFilterChips();
            performSearch();
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showPriceFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Price Range");

        // Simple price ranges
        String[] priceRanges = {
                "All Prices",
                "Under 100,000 VNĐ",
                "100,000 - 500,000 VNĐ",
                "500,000 - 1,000,000 VNĐ",
                "1,000,000 - 5,000,000 VNĐ",
                "Above 5,000,000 VNĐ"
        };

        builder.setSingleChoiceItems(priceRanges, 0, (dialog, which) -> {
            switch (which) {
                case 0:
                    minPrice = null;
                    maxPrice = null;
                    btnPriceFilter.setText("Price");
                    break;
                case 1:
                    minPrice = null;
                    maxPrice = 100000.0;
                    btnPriceFilter.setText("< 100K");
                    break;
                case 2:
                    minPrice = 100000.0;
                    maxPrice = 500000.0;
                    btnPriceFilter.setText("100K-500K");
                    break;
                case 3:
                    minPrice = 500000.0;
                    maxPrice = 1000000.0;
                    btnPriceFilter.setText("500K-1M");
                    break;
                case 4:
                    minPrice = 1000000.0;
                    maxPrice = 5000000.0;
                    btnPriceFilter.setText("1M-5M");
                    break;
                case 5:
                    minPrice = 5000000.0;
                    maxPrice = null;
                    btnPriceFilter.setText("> 5M");
                    break;
            }

            updateFilterChips();
            performSearch();
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sort By");

        String[] sortOptions = {"Relevance", "Newest", "Price: Low to High", "Price: High to Low"};
        int selectedIndex = 0;
        switch (sortBy) {
            case "newest": selectedIndex = 1; break;
            case "price_asc": selectedIndex = 2; break;
            case "price_desc": selectedIndex = 3; break;
            default: selectedIndex = 0; break;
        }

        builder.setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
            switch (which) {
                case 0:
                    sortBy = "relevance";
                    if (tvSortOption != null) tvSortOption.setText("Sort: Relevance");
                    break;
                case 1:
                    sortBy = "newest";
                    if (tvSortOption != null) tvSortOption.setText("Sort: Newest");
                    break;
                case 2:
                    sortBy = "price_asc";
                    if (tvSortOption != null) tvSortOption.setText("Sort: Price ↑");
                    break;
                case 3:
                    sortBy = "price_desc";
                    if (tvSortOption != null) tvSortOption.setText("Sort: Price ↓");
                    break;
            }

            performSearch();
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showLocationFilterDialog() {
        Toast.makeText(getContext(), "Location filter coming soon! 📍", Toast.LENGTH_SHORT).show();
    }

    private void updateFilterChips() {
        if (chipGroupFilters != null) {
            chipGroupFilters.removeAllViews();

            // Add category chip
            if (selectedCategoryName != null) {
                addFilterChip(selectedCategoryName, () -> {
                    selectedCategoryId = null;
                    selectedCategoryName = null;
                    btnCategoryFilter.setText("Category");
                    updateFilterChips();
                    performSearch();
                });
            }

            // Add condition chip
            if (selectedCondition != null) {
                addFilterChip(selectedCondition.replace("_", " "), () -> {
                    selectedCondition = null;
                    btnConditionFilter.setText("Condition");
                    updateFilterChips();
                    performSearch();
                });
            }

            // Add price chip
            if (minPrice != null || maxPrice != null) {
                String priceText = "";
                if (minPrice != null && maxPrice != null) {
                    priceText = String.format("%.0f - %.0f VNĐ", minPrice, maxPrice);
                } else if (minPrice != null) {
                    priceText = String.format("> %.0f VNĐ", minPrice);
                } else if (maxPrice != null) {
                    priceText = String.format("< %.0f VNĐ", maxPrice);
                }

                addFilterChip(priceText, () -> {
                    minPrice = null;
                    maxPrice = null;
                    btnPriceFilter.setText("Price");
                    updateFilterChips();
                    performSearch();
                });
            }

            // Show/hide chip group
            chipGroupFilters.setVisibility(chipGroupFilters.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void addFilterChip(String text, Runnable onCloseClick) {
        if (chipGroupFilters != null) {
            Chip chip = new Chip(requireContext());
            chip.setText(text);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> onCloseClick.run());
            chipGroupFilters.addView(chip);
        }
    }

    private void navigateToProductDetail(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getFormattedPrice());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}