// app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.models.Product;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    // Data & Adapters
    private ProductAdapter searchAdapter;
    private final List<Product> searchResults = new ArrayList<>();
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

        Log.d(TAG, "SearchFragment created successfully");
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

        Log.d(TAG, "✅ SearchFragment views initialized");
    }

    private void setupRecyclerViews() {
        if (rvSearchResults != null) {
            searchAdapter = new ProductAdapter(searchResults, this::navigateToProductDetail);
            rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
            rvSearchResults.setAdapter(searchAdapter);
        }
    }

    private void setupListeners() {
        // Search input with debounce
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (!query.equals(currentQuery)) {
                        currentQuery = query;
                        scheduleSearch();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Filter buttons
        if (btnCategoryFilter != null) {
            btnCategoryFilter.setOnClickListener(v -> showCategoryFilterDialog());
        }

        if (btnPriceFilter != null) {
            btnPriceFilter.setOnClickListener(v -> showPriceFilterDialog());
        }

        if (btnLocationFilter != null) {
            btnLocationFilter.setOnClickListener(v -> showLocationFilterDialog());
        }

        if (btnConditionFilter != null) {
            btnConditionFilter.setOnClickListener(v -> showConditionFilterDialog());
        }

        // Sort option
        if (tvSortOption != null) {
            View llSortOptions = requireView().findViewById(R.id.ll_sort_options);
            if (llSortOptions != null) {
                llSortOptions.setOnClickListener(v -> showSortDialog());
            }
        }

        Log.d(TAG, "✅ SearchFragment listeners setup completed");
    }

    private void scheduleSearch() {
        // Cancel previous search
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        // Schedule new search with delay
        searchRunnable = this::performSearch;
        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
    }

    private void performSearch() {
        String query = currentQuery.trim();

        if (query.isEmpty() && !hasActiveFilters()) {
            showInitialState();
            return;
        }

        showLoadingState();
        isSearching = true;

        // Search with location if available
        if (searchLatitude != null && searchLongitude != null) {
            searchWithLocation(query, searchLatitude, searchLongitude, searchRadiusKm);
        } else {
            searchWithoutLocation(query);
        }
    }

    private void searchWithLocation(String query, Double latitude, Double longitude, int radiusKm) {
        loadMockSearchResults(query, true);
        Log.d(TAG, "🔍 Searching with location: " + query + " near " + latitude + "," + longitude + " (" + radiusKm + "km)");
    }

    private void searchWithoutLocation(String query) {
        loadMockSearchResults(query, false);
        Log.d(TAG, "🔍 Searching without location: " + query);
    }

    private void loadMockSearchResults(String query, boolean hasLocation) {
        // Simulate API delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            searchResults.clear();

            // Mock search results based on query and location
            if (query.toLowerCase().contains("iphone")) {
                addMockProduct("iPhone 13 Pro Max", "25000000");
                addMockProduct("iPhone 12", "18000000");
                addMockProduct("iPhone 14", "28000000");
            } else if (query.toLowerCase().contains("samsung")) {
                addMockProduct("Samsung Galaxy S23", "19000000");
                addMockProduct("Samsung Galaxy Note 20", "15000000");
            } else if (query.toLowerCase().contains("macbook")) {
                addMockProduct("MacBook Air M2", "32000000");
                addMockProduct("MacBook Pro 14\"", "45000000");
            } else if (!query.isEmpty()) {
                // General search results
                addMockProduct("iPhone 13 Pro Max", "25000000");
                addMockProduct("Samsung Galaxy S23", "19000000");
                addMockProduct("MacBook Air M2", "32000000");
                addMockProduct("AirPods Pro", "6000000");
            } else if (hasActiveFilters()) {
                // Results based on filters only
                addMockProduct("Filtered Product 1", "15000000");
                addMockProduct("Filtered Product 2", "22000000");
            }

            // Apply additional filters
            applyFilters();

            if (searchAdapter != null) {
                searchAdapter.notifyDataSetChanged();
            }
            updateResultsCount(searchResults.size());
            showResults();
            isSearching = false;
        }, 800);
    }

    private void addMockProduct(String title, String price) {
        Product product = new Product();
        product.setId(System.currentTimeMillis() + searchResults.size());
        product.setTitle(title);
        product.setPrice(new BigDecimal(price));
        product.setCondition(Product.ProductCondition.GOOD);
        product.setImageUrl("https://example.com/product.jpg");
        product.setLocation("Ho Chi Minh City");

        searchResults.add(product);
    }

    private void applyFilters() {
        List<Product> filteredResults = new ArrayList<>();

        for (Product product : searchResults) {
            boolean passesFilters = true;

            // Price filter
            if (minPrice != null && product.getPrice().doubleValue() < minPrice) {
                passesFilters = false;
            }
            if (maxPrice != null && product.getPrice().doubleValue() > maxPrice) {
                passesFilters = false;
            }

            // Condition filter
            if (selectedCondition != null && !selectedCondition.equals(product.getCondition().name())) {
                passesFilters = false;
            }

            if (passesFilters) {
                filteredResults.add(product);
            }
        }

        searchResults.clear();
        searchResults.addAll(filteredResults);
    }

    private boolean hasActiveFilters() {
        return selectedCategoryId != null ||
                minPrice != null || maxPrice != null ||
                selectedCondition != null ||
                (searchLatitude != null && searchLongitude != null);
    }

    // ===== Filter Dialogs =====

    private void showCategoryFilterDialog() {
        String[] categoryNames = {"Electronics", "Fashion", "Home & Garden", "Sports", "Books", "Automotive"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Category");
        builder.setItems(categoryNames, (dialog, which) -> {
            selectedCategoryId = (long) (which + 1);
            selectedCategoryName = categoryNames[which];
            updateCategoryFilterButton();
            performSearch();
        });
        builder.setNegativeButton("Clear", (dialog, which) -> {
            selectedCategoryId = null;
            selectedCategoryName = null;
            updateCategoryFilterButton();
            performSearch();
        });
        builder.show();
    }

    private void showPriceFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_price_filter, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Price Range");
        builder.setView(dialogView);
        builder.setPositiveButton("Apply", (dialog, which) -> {
            // Mock price values for now
            minPrice = 5000000.0;
            maxPrice = 50000000.0;
            updatePriceFilterButton();
            performSearch();
        });
        builder.setNegativeButton("Clear", (dialog, which) -> {
            minPrice = null;
            maxPrice = null;
            updatePriceFilterButton();
            performSearch();
        });
        builder.show();
    }

    private void showLocationFilterDialog() {
        LocationFilterDialog dialog = LocationFilterDialog.newInstance();
        dialog.setLocationFilterListener(new LocationFilterDialog.LocationFilterListener() {
            @Override
            public void onLocationFilterApplied(Double latitude, Double longitude, String locationName, int radiusKm) {
                searchLatitude = latitude;
                searchLongitude = longitude;
                searchLocationName = locationName;
                searchRadiusKm = radiusKm;

                updateLocationFilterButton();
                performSearch();

                Log.d(TAG, "✅ Location filter applied: " + locationName + " (" + radiusKm + "km)");
            }

            @Override
            public void onLocationFilterCleared() {
                searchLatitude = null;
                searchLongitude = null;
                searchLocationName = "";
                searchRadiusKm = 10;

                updateLocationFilterButton();
                performSearch();

                Log.d(TAG, "✅ Location filter cleared");
            }
        });

        dialog.show(getParentFragmentManager(), "LocationFilterDialog");
    }

    private void showConditionFilterDialog() {
        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Condition");
        builder.setItems(conditions, (dialog, which) -> {
            selectedCondition = Product.ProductCondition.values()[which].name();
            updateConditionFilterButton();
            performSearch();
        });
        builder.setNegativeButton("Clear", (dialog, which) -> {
            selectedCondition = null;
            updateConditionFilterButton();
            performSearch();
        });
        builder.show();
    }

    private void showSortDialog() {
        String[] sortOptions = {"Relevance", "Newest", "Price: Low to High", "Price: High to Low", "Distance"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sort by");
        builder.setItems(sortOptions, (dialog, which) -> {
            sortBy = sortOptions[which].toLowerCase().replace(" ", "_");
            if (tvSortOption != null) {
                tvSortOption.setText(sortOptions[which]);
            }
            performSearch();
        });
        builder.show();
    }

    // ===== Filter Button Updates =====

    private void updateCategoryFilterButton() {
        if (btnCategoryFilter == null) return;

        if (selectedCategoryId != null) {
            btnCategoryFilter.setText(selectedCategoryName);
            addActiveFilterChip("Category: " + selectedCategoryName, "category");
        } else {
            btnCategoryFilter.setText("Category");
            removeActiveFilterChip("category");
        }
    }

    private void updatePriceFilterButton() {
        if (btnPriceFilter == null) return;

        if (minPrice != null || maxPrice != null) {
            String priceText = "Price Set";
            btnPriceFilter.setText(priceText);
            addActiveFilterChip("Price: " + priceText, "price");
        } else {
            btnPriceFilter.setText("Price");
            removeActiveFilterChip("price");
        }
    }

    private void updateLocationFilterButton() {
        if (btnLocationFilter == null) return;

        if (searchLatitude != null && searchLongitude != null) {
            btnLocationFilter.setText("📍 " + searchRadiusKm + "km");
            addActiveFilterChip("Location: " + searchLocationName + " (" + searchRadiusKm + "km)", "location");
        } else {
            btnLocationFilter.setText("Location");
            removeActiveFilterChip("location");
        }
    }

    private void updateConditionFilterButton() {
        if (btnConditionFilter == null) return;

        if (selectedCondition != null) {
            String conditionText = selectedCondition.replace("_", " ");
            btnConditionFilter.setText(conditionText);
            addActiveFilterChip("Condition: " + conditionText, "condition");
        } else {
            btnConditionFilter.setText("Condition");
            removeActiveFilterChip("condition");
        }
    }

    // ===== Active Filter Chips =====

    private void addActiveFilterChip(String text, String tag) {
        if (chipGroupFilters == null) return;

        // Remove existing chip with same tag
        removeActiveFilterChip(tag);

        // Add new chip
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setTag(tag);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            removeFilter(tag);
            removeActiveFilterChip(tag);
        });

        chipGroupFilters.addView(chip);
        chipGroupFilters.setVisibility(View.VISIBLE);
    }

    private void removeActiveFilterChip(String tag) {
        if (chipGroupFilters == null) return;

        for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
            View child = chipGroupFilters.getChildAt(i);
            if (child instanceof Chip && tag.equals(child.getTag())) {
                chipGroupFilters.removeView(child);
                break;
            }
        }

        if (chipGroupFilters.getChildCount() == 0) {
            chipGroupFilters.setVisibility(View.GONE);
        }
    }

    private void removeFilter(String tag) {
        switch (tag) {
            case "category":
                selectedCategoryId = null;
                selectedCategoryName = null;
                updateCategoryFilterButton();
                break;
            case "price":
                minPrice = null;
                maxPrice = null;
                updatePriceFilterButton();
                break;
            case "location":
                searchLatitude = null;
                searchLongitude = null;
                searchLocationName = "";
                updateLocationFilterButton();
                break;
            case "condition":
                selectedCondition = null;
                updateConditionFilterButton();
                break;
        }
        performSearch();
    }

    // ===== UI State Management =====

    private void showInitialState() {
        hideAllStates();
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.VISIBLE);
        }
        updateResultsCount(0);
    }

    private void showLoadingState() {
        hideAllStates();
        if (llLoadingState != null) {
            llLoadingState.setVisibility(View.VISIBLE);
        }
    }

    private void showResults() {        hideAllStates();
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.VISIBLE);
        }

        if (searchResults.isEmpty()) {
            showEmptyState();
        }
    }

    private void showEmptyState() {
        hideAllStates();
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.VISIBLE);
        }
    }

    private void hideAllStates() {
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
        if (llRecentSearches != null) llRecentSearches.setVisibility(View.GONE);
        if (llLoadingState != null) llLoadingState.setVisibility(View.GONE);
    }

    private void updateResultsCount(int count) {
        if (tvResultsCount != null) {
            if (count == 0) {
                tvResultsCount.setText("No products found");
            } else {
                tvResultsCount.setText("Found " + count + " products");
            }
        }
    }

    private void navigateToProductDetail(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}