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

        // Initialize popular products title
        tvPopularProductsTitle = new TextView(getContext());
        tvPopularProductsTitle.setText("✨ Popular Products");
        tvPopularProductsTitle.setTextSize(18);
        tvPopularProductsTitle.setTextColor(getResources().getColor(R.color.text_primary));
        tvPopularProductsTitle.setPadding(16, 16, 16, 8);

        Log.d(TAG, "✅ SearchFragment views initialized");
    }

    private void setupRecyclerViews() {
        if (rvSearchResults != null) {
            productAdapter = new ProductAdapter(searchResults, this::navigateToProductDetail);

            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            rvSearchResults.setLayoutManager(layoutManager);
            rvSearchResults.setAdapter(productAdapter);

            // Add scroll listener for pagination
            rvSearchResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (!isSearching && !isShowingPopularProducts) {
                        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                        if (layoutManager != null) {
                            int totalItemCount = layoutManager.getItemCount();
                            int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                            if (totalItemCount <= (lastVisibleItem + 5)) {
                                // Load more items
                                loadMoreSearchResults();
                            }
                        }
                    }
                }
            });
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
            btnCategoryFilter.setOnClickListener(v -> showCategoryFilter());
        }

        if (btnPriceFilter != null) {
            btnPriceFilter.setOnClickListener(v -> showPriceFilter());
        }

        if (btnLocationFilter != null) {
            btnLocationFilter.setOnClickListener(v -> showLocationFilter());
        }

        if (btnConditionFilter != null) {
            btnConditionFilter.setOnClickListener(v -> showConditionFilter());
        }

        // Sort option
        if (tvSortOption != null) {
            tvSortOption.setOnClickListener(v -> showSortOptions());
        }
    }

    // ===== SEARCH FUNCTIONALITY =====

    private void scheduleSearch() {
        // Cancel previous search
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        // Schedule new search
        searchRunnable = () -> {
            if (TextUtils.isEmpty(currentQuery)) {
                showInitialState();
            } else {
                performSearch();
            }
        };

        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
    }

    private void performSearch() {
        if (isSearching) return;

        isSearching = true;
        isShowingPopularProducts = false;
        showLoadingState();

        Log.d(TAG, "🔍 Performing search: " + currentQuery);

        // Build search parameters
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("query", currentQuery);
        searchParams.put("page", 0);
        searchParams.put("size", 20);

        if (selectedCategoryId != null) {
            searchParams.put("categoryId", selectedCategoryId);
        }

        if (minPrice != null) {
            searchParams.put("minPrice", minPrice);
        }

        if (maxPrice != null) {
            searchParams.put("maxPrice", maxPrice);
        }

        if (selectedCondition != null) {
            searchParams.put("condition", selectedCondition);
        }

        if (searchLatitude != null && searchLongitude != null) {
            searchParams.put("latitude", searchLatitude);
            searchParams.put("longitude", searchLongitude);
            searchParams.put("radius", searchRadiusKm);
        }

        searchParams.put("sortBy", sortBy);

        // Call search API
        ApiClient.getApiService().searchProductsAdvanced(
                currentQuery,
                selectedCategoryId,
                minPrice,
                maxPrice,
                selectedCondition,
                searchLatitude,
                searchLongitude,
                searchRadiusKm,
                sortBy,
                0,
                20
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                isSearching = false;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                    if (productMaps != null) {
                        List<Product> products = convertMapsToProducts(productMaps);
                        displaySearchResults(products);

                        Log.d(TAG, "✅ Search completed: " + products.size() + " results");
                    } else {
                        displaySearchResults(new ArrayList<>());
                    }
                } else {
                    showError("Search failed: " + (response.body() != null ? response.body().getMessage() : "Unknown error"));
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                isSearching = false;
                Log.e(TAG, "❌ Search failed", t);
                showError("Search failed: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void loadMoreSearchResults() {
        // TODO: Implement pagination
        Log.d(TAG, "📄 Loading more search results...");
    }

    // ===== INITIAL STATE WITH POPULAR PRODUCTS =====

    private void showInitialState() {
        hideAllStates();
        loadPopularProducts();

        // Show search suggestions
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.VISIBLE);
        }
    }

    private void loadPopularProducts() {
        if (isSearching) return;

        isSearching = true;
        isShowingPopularProducts = true;
        showLoadingState();

        Log.d(TAG, "✨ Loading popular products...");

        // Try featured products first
        ApiClient.getApiService().getFeaturedProducts()
                .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                           @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<Map<String, Object>> productMaps = response.body().getData();

                            if (productMaps != null && !productMaps.isEmpty()) {
                                List<Product> products = convertMapsToProducts(productMaps);
                                displayPopularProducts(products);
                                Log.d(TAG, "✅ Loaded " + products.size() + " featured products");
                            } else {
                                loadRecentProducts(); // Fallback
                            }
                        } else {
                            loadRecentProducts(); // Fallback
                        }

                        isSearching = false;
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                        Log.e(TAG, "❌ Failed to load featured products", t);
                        loadRecentProducts(); // Fallback
                    }
                });
    }

    private void loadRecentProducts() {
        ApiClient.getApiService().getRecentProducts(20)
                .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                           @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {
                        isSearching = false;

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<Map<String, Object>> productMaps = response.body().getData();

                            if (productMaps != null && !productMaps.isEmpty()) {
                                List<Product> products = convertMapsToProducts(productMaps);
                                displayPopularProducts(products);
                                Log.d(TAG, "✅ Loaded " + products.size() + " recent products");
                            } else {
                                showEmptyState();
                            }
                        } else {
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                        isSearching = false;
                        Log.e(TAG, "❌ Failed to load recent products", t);
                        showEmptyState();
                    }
                });
    }

    // ===== DISPLAY METHODS =====

    private void displayPopularProducts(List<Product> products) {
        hideAllStates();

        searchResults.clear();
        searchResults.addAll(products);
        productAdapter.notifyDataSetChanged();

        rvSearchResults.setVisibility(View.VISIBLE);

        // Update results count
        if (tvResultsCount != null) {
            tvResultsCount.setText("✨ Popular Products (" + products.size() + ")");
        }

        // Show search suggestions alongside popular products
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "✅ Displayed " + products.size() + " popular products");
    }

    private void displaySearchResults(List<Product> products) {
        hideAllStates();

        searchResults.clear();
        searchResults.addAll(products);
        productAdapter.notifyDataSetChanged();

        if (products.isEmpty()) {
            showEmptyState();
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            updateResultsCount(products.size());
        }
    }

    // ===== FILTER METHODS =====

    private void showCategoryFilter() {
        // TODO: Implement category filter dialog
        Log.d(TAG, "🏷️ Show category filter");
        Toast.makeText(getContext(), "Category filter - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showPriceFilter() {
        // TODO: Implement price filter dialog
        Log.d(TAG, "💰 Show price filter");
        Toast.makeText(getContext(), "Price filter - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showLocationFilter() {
        // TODO: Implement location filter dialog
        Log.d(TAG, "📍 Show location filter");
        Toast.makeText(getContext(), "Location filter - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showConditionFilter() {
        if (getContext() == null) return;

        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};
        String[] conditionValues = {"NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};

        int selectedIndex = -1;
        if (selectedCondition != null) {
            for (int i = 0; i < conditionValues.length; i++) {
                if (conditionValues[i].equals(selectedCondition)) {
                    selectedIndex = i;
                    break;
                }
            }
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Select Condition")
                .setSingleChoiceItems(conditions, selectedIndex, (dialog, which) -> {
                    selectedCondition = conditionValues[which];
                    updateConditionFilterButton();
                    performSearch();
                    dialog.dismiss();
                })
                .setNegativeButton("Clear", (dialog, which) -> {
                    selectedCondition = null;
                    updateConditionFilterButton();
                    performSearch();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void showSortOptions() {
        if (getContext() == null) return;

        String[] sortOptions = {"Relevance", "Newest", "Oldest", "Price: Low to High", "Price: High to Low", "Distance"};
        String[] sortValues = {"relevance", "newest", "oldest", "price_asc", "price_desc", "distance"};

        int selectedIndex = 0;
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(sortBy)) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Sort by")
                .setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
                    sortBy = sortValues[which];
                    updateSortButton();
                    if (!TextUtils.isEmpty(currentQuery)) {
                        performSearch();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ===== FILTER UI UPDATES =====

    private void updateConditionFilterButton() {
        if (btnConditionFilter != null) {
            if (selectedCondition != null) {
                btnConditionFilter.setText("Condition: " + Constants.getProductConditionDisplay(selectedCondition));
                btnConditionFilter.setIconResource(R.drawable.ic_check);
            } else {
                btnConditionFilter.setText("Condition");
                btnConditionFilter.setIconResource(R.drawable.ic_arrow_drop_down);
            }
        }
    }

    private void updateSortButton() {
        if (tvSortOption != null) {
            String sortText = "Relevance";
            switch (sortBy) {
                case "newest": sortText = "Newest"; break;
                case "oldest": sortText = "Oldest"; break;
                case "price_asc": sortText = "Price ↑"; break;
                case "price_desc": sortText = "Price ↓"; break;
                case "distance": sortText = "Distance"; break;
            }
            tvSortOption.setText("Sort: " + sortText);
        }
    }

    // ===== CLEAR FILTERS =====

    private void clearAllFilters() {
        selectedCategoryId = null;
        selectedCategoryName = null;
        selectedCondition = null;
        minPrice = null;
        maxPrice = null;
        searchLatitude = null;
        searchLongitude = null;
        searchLocationName = "";
        sortBy = "relevance";

        updateFilterButtons();

        if (!TextUtils.isEmpty(currentQuery)) {
            performSearch();
        }
    }

    private void updateFilterButtons() {
        updateConditionFilterButton();
        updateSortButton();
        // TODO: Update other filter buttons
    }

    // ===== DATA CONVERSION =====

    private List<Product> convertMapsToProducts(List<Map<String, Object>> productMaps) {
        List<Product> products = new ArrayList<>();

        for (Map<String, Object> productMap : productMaps) {
            try {
                Product product = convertMapToProduct(productMap);
                if (product != null) {
                    products.add(product);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error converting product data", e);
            }
        }

        return products;
    }

    private Product convertMapToProduct(Map<String, Object> productMap) {
        try {
            Product product = new Product();

            // Basic info
            if (productMap.get("id") != null) {
                product.setId(((Number) productMap.get("id")).longValue());
            }

            product.setTitle((String) productMap.get("title"));
            product.setDescription((String) productMap.get("description"));

            // Price
            if (productMap.get("price") != null) {
                product.setPrice(((Number) productMap.get("price")).doubleValue());
            }

            // Images
            String imageUrl = (String) productMap.get("imageUrl");
            if (imageUrl != null) {
                product.setImageUrl(Constants.getImageUrl(imageUrl));
            }

            // Location
            product.setLocation((String) productMap.get("location"));

            // Condition
            product.setCondition((String) productMap.get("condition"));

            // Status
            product.setStatus((String) productMap.get("status"));

            // Timestamps
            product.setCreatedAt((String) productMap.get("createdAt"));

            return product;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error converting product map", e);
            return null;
        }
    }

    // ===== UI STATE MANAGEMENT =====

    private void showLoadingState() {
        hideAllStates();
        if (llLoadingState != null) {
            llLoadingState.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        hideAllStates();
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.VISIBLE);
        }

        // Show search suggestions even in empty state
        if (llRecentSearches != null && TextUtils.isEmpty(currentQuery)) {
            llRecentSearches.setVisibility(View.VISIBLE);
        }

        updateResultsCount(0);
    }

    private void hideAllStates() {
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
        if (llLoadingState != null) llLoadingState.setVisibility(View.GONE);
        if (llRecentSearches != null) llRecentSearches.setVisibility(View.GONE);
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

    // ===== NAVIGATION =====

    private void navigateToProductDetail(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    // ===== ERROR HANDLING =====

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: " + message);
    }

    // ===== CLEANUP =====

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}