// app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.LocationManager;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment implements
        LocationManager.LocationCallback {

    private static final String TAG = "SearchFragment";

    // UI Components
    private EditText etSearch;
    private ImageView ivFilter, ivSort;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvResultsCount, tvEmpty;

    // Data and Adapter
    private List<Product> products = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    // Search parameters
    private String currentQuery = "";
    private SearchFilter currentFilter = new SearchFilter();
    private SearchSort currentSort = new SearchSort();

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Utils
    private SharedPrefsManager prefsManager;
    private LocationManager locationManager;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Data classes
    public static class SearchFilter {
        public Long categoryId;
        public Product.ProductCondition condition;
        public BigDecimal minPrice;
        public BigDecimal maxPrice;
        public Double latitude;
        public Double longitude;
        public Integer radius;

        public SearchFilter() {}

        public SearchFilter(SearchFilter other) {
            this.categoryId = other.categoryId;
            this.condition = other.condition;
            this.minPrice = other.minPrice;
            this.maxPrice = other.maxPrice;
            this.latitude = other.latitude;
            this.longitude = other.longitude;
            this.radius = other.radius;
        }

        public boolean hasActiveFilters() {
            return categoryId != null || condition != null ||
                    minPrice != null || maxPrice != null ||
                    (latitude != null && longitude != null);
        }

        public int getActiveFilterCount() {
            int count = 0;
            if (categoryId != null) count++;
            if (condition != null) count++;
            if (minPrice != null || maxPrice != null) count++;
            if (latitude != null && longitude != null) count++;
            return count;
        }
    }

    public static class SearchSort {
        public enum SortBy {
            RELEVANCE("relevance", "Relevance"),
            NEWEST("createdAt", "Newest"),
            PRICE_LOW_HIGH("price", "Price: Low to High"),
            PRICE_HIGH_LOW("price", "Price: High to Low");

            private final String apiValue;
            private final String displayName;

            SortBy(String apiValue, String displayName) {
                this.apiValue = apiValue;
                this.displayName = displayName;
            }

            public String getApiValue() { return apiValue; }
            public String getDisplayName() { return displayName; }
        }

        public SortBy sortBy = SortBy.RELEVANCE;
        public String sortDirection = "desc";

        public void setSortBy(SortBy sortBy) {
            this.sortBy = sortBy;
            // Set default direction based on sort type
            switch (sortBy) {
                case PRICE_LOW_HIGH:
                    sortDirection = "asc";
                    break;
                case PRICE_HIGH_LOW:
                case NEWEST:
                default:
                    sortDirection = "desc";
                    break;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initUtils();
        setupListeners();
        setupRecyclerView();

        // Handle arguments from intent
        handleArguments();

        loadCategories();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        ivFilter = view.findViewById(R.id.iv_filter);
        ivSort = view.findViewById(R.id.iv_sort);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        rvProducts = view.findViewById(R.id.rv_products);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        tvResultsCount = view.findViewById(R.id.tv_results_count);
        tvEmpty = view.findViewById(R.id.tv_empty);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(requireContext());
        locationManager = new LocationManager(requireContext(), this);
    }

    private void setupListeners() {
        // FR-3.1.2: Search triggers 200ms after typing starts
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Schedule new search
                searchRunnable = () -> performSearch(query);
                searchHandler.postDelayed(searchRunnable, Constants.SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ivFilter.setOnClickListener(v -> showFilterDialog());
        ivSort.setOnClickListener(v -> showSortDialog());
        swipeRefresh.setOnRefreshListener(this::refreshSearch);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvProducts.setLayoutManager(layoutManager);

        // TODO: Create ProductGridAdapter
        // ProductGridAdapter adapter = new ProductGridAdapter(products, this);
        // rvProducts.setAdapter(adapter);

        // Pagination scroll listener
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && !isLastPage && dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        loadMoreProducts();
                    }
                }
            }
        });
    }

    private void handleArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String query = args.getString("query");
            if (query != null) {
                etSearch.setText(query);
                currentQuery = query;
                performSearch(query);
            }

            Long categoryId = args.getLong("categoryId", -1);
            if (categoryId != -1) {
                currentFilter.categoryId = categoryId;
                updateFilterChips();
            }
        }
    }

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
            }
        });
    }

    // FR-3.1.1: Search via Keywords, Category, Price range, Condition, Distance
    private void performSearch(String query) {
        currentQuery = query;
        currentPage = 0;
        isLastPage = false;
        products.clear();
        updateResultsCount();

        if (query.isEmpty() && !currentFilter.hasActiveFilters()) {
            showEmptyState("Enter a search term or apply filters");
            return;
        }

        searchProducts();
    }

    private void searchProducts() {
        if (isLoading) return;

        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        // Prepare search parameters
        String conditionStr = currentFilter.condition != null ? currentFilter.condition.name() : null;
        Double minPrice = currentFilter.minPrice != null ? currentFilter.minPrice.doubleValue() : null;
        Double maxPrice = currentFilter.maxPrice != null ? currentFilter.maxPrice.doubleValue() : null;

        Call<StandardResponse<Map<String, Object>>> call;

        if (!currentQuery.isEmpty()) {
            // Search with query
            call = ApiClient.getProductService().searchProducts(
                    currentQuery, currentPage, Constants.DEFAULT_PAGE_SIZE,
                    currentFilter.categoryId, conditionStr, minPrice, maxPrice
            );
        } else {
            // Filter without query
            call = ApiClient.getProductService().getAllProducts(
                    currentPage, Constants.DEFAULT_PAGE_SIZE,
                    currentSort.sortBy.getApiValue(), currentSort.sortDirection,
                    currentFilter.categoryId, conditionStr, minPrice, maxPrice
            );
        }

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleSearchResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleSearchError(t);
            }
        });
    }

    private void loadMoreProducts() {
        if (isLoading || isLastPage) return;

        currentPage++;
        searchProducts();
    }

    private void refreshSearch() {
        currentPage = 0;
        isLastPage = false;
        products.clear();
        searchProducts();
    }

    @SuppressWarnings("unchecked")
    private void handleSearchResponse(Response<StandardResponse<Map<String, Object>>> response) {
        isLoading = false;
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);

        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null) {
                    int oldSize = products.size();

                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            products.add(product);
                        }
                    }

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : true;

                    // Update UI
                    updateResultsCount();

                    if (products.isEmpty()) {
                        showEmptyState("No products found");
                    } else {
                        showProductsState();
                        // TODO: Notify adapter of changes
                        // adapter.notifyItemRangeInserted(oldSize, productMaps.size());
                    }
                }
            } else {
                showEmptyState("Search failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing search response", e);
            showEmptyState("Error loading results");
        }
    }

    private void handleSearchError(Throwable t) {
        isLoading = false;
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);

        Log.e(TAG, "Search failed", t);
        Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show();

        if (products.isEmpty()) {
            showEmptyState("Search failed. Please try again.");
        }
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
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing categories response", e);
        }
    }

    private Product parseProductFromMap(Map<String, Object> productMap) {
        // Same implementation as in HomeFragment
        try {
            Product product = new Product();
            product.setId(((Number) productMap.get("id")).longValue());
            product.setTitle((String) productMap.get("title"));
            product.setDescription((String) productMap.get("description"));
            product.setLocation((String) productMap.get("location"));

            Object price = productMap.get("price");
            if (price instanceof Number) {
                product.setPrice(new BigDecimal(price.toString()));
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

            return product;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing product", e);
            return null;
        }
    }

    private Category parseCategoryFromMap(Map<String, Object> categoryMap) {
        // Same implementation as in HomeFragment
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

    private void showFilterDialog() {
        // TODO: Implement SearchFilterBottomSheet
        Toast.makeText(requireContext(), "Filter dialog - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showSortDialog() {
        // TODO: Implement SearchSortBottomSheet
        Toast.makeText(requireContext(), "Sort dialog - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void updateFilterChips() {
        chipGroupFilters.removeAllViews();

        if (currentFilter.hasActiveFilters()) {
            if (currentFilter.categoryId != null) {
                addFilterChip("Category", findCategoryName(currentFilter.categoryId));
            }

            if (currentFilter.condition != null) {
                addFilterChip("Condition", currentFilter.condition.getDisplayName());
            }

            if (currentFilter.minPrice != null || currentFilter.maxPrice != null) {
                String priceRange = "";
                if (currentFilter.minPrice != null && currentFilter.maxPrice != null) {
                    priceRange = "$" + currentFilter.minPrice + " - $" + currentFilter.maxPrice;
                } else if (currentFilter.minPrice != null) {
                    priceRange = "From $" + currentFilter.minPrice;
                } else {
                    priceRange = "Up to $" + currentFilter.maxPrice;
                }
                addFilterChip("Price", priceRange);
            }

            if (currentFilter.latitude != null && currentFilter.longitude != null) {
                String radius = currentFilter.radius != null ? currentFilter.radius + "km" : "10km";
                addFilterChip("Location", "Within " + radius);
            }
        }
    }

    private void addFilterChip(String label, String value) {
        Chip chip = new Chip(requireContext());
        chip.setText(label + ": " + value);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            removeFilter(label);
            chipGroupFilters.removeView(chip);
            performSearch(currentQuery);
        });
        chipGroupFilters.addView(chip);
    }

    private void removeFilter(String label) {
        switch (label) {
            case "Category":
                currentFilter.categoryId = null;
                break;
            case "Condition":
                currentFilter.condition = null;
                break;
            case "Price":
                currentFilter.minPrice = null;
                currentFilter.maxPrice = null;
                break;
            case "Location":
                currentFilter.latitude = null;
                currentFilter.longitude = null;
                currentFilter.radius = null;
                break;
        }
    }

    private String findCategoryName(Long categoryId) {
        for (Category category : categories) {
            if (category.getId().equals(categoryId)) {
                return category.getName();
            }
        }
        return "Unknown";
    }

    private void updateResultsCount() {
        if (products.isEmpty()) {
            tvResultsCount.setVisibility(View.GONE);
        } else {
            tvResultsCount.setVisibility(View.VISIBLE);
            tvResultsCount.setText(products.size() + " result" + (products.size() == 1 ? "" : "s"));
        }
    }

    private void showEmptyState(String message) {
        rvProducts.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText(message);
    }

    private void showProductsState() {
        rvProducts.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    // LocationManager.LocationCallback implementation
    @Override
    public void onLocationReceived(Location location) {
        currentFilter.latitude = location.getLatitude();
        currentFilter.longitude = location.getLongitude();
        if (currentFilter.radius == null) {
            currentFilter.radius = (int) Constants.DEFAULT_SEARCH_RADIUS_KM;
        }

        updateFilterChips();
        performSearch(currentQuery);
    }

    @Override
    public void onLocationError(String error) {
        Log.e(TAG, "Location error: " + error);
        Toast.makeText(requireContext(), "Location error: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.cleanup();
        }
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}