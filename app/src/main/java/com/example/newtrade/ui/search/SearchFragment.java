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
import com.example.newtrade.ui.product.adapter.ProductGridAdapter;
import com.example.newtrade.ui.search.filter.SearchFilterBottomSheet;
import com.example.newtrade.ui.search.sort.SearchSortBottomSheet;
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
        ProductGridAdapter.OnProductClickListener,
        SearchFilterBottomSheet.OnFilterAppliedListener,
        SearchSortBottomSheet.OnSortAppliedListener,
        LocationManager.LocationCallback {

    private static final String TAG = "SearchFragment";
    private static final int SEARCH_DELAY_MS = 500;

    // UI Components
    private EditText etSearch;
    private ImageView ivFilter, ivSort;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvResultsCount, tvEmpty;

    // Data and Adapter
    private ProductGridAdapter adapter;
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
                    minPrice != null || maxPrice != null || radius != null;
        }
    }

    public static class SearchSort {
        public String sortBy = "createdAt";
        public String sortDirection = "desc";

        public SearchSort() {}

        public SearchSort(String sortBy, String sortDirection) {
            this.sortBy = sortBy;
            this.sortDirection = sortDirection;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = new SharedPrefsManager(requireContext());
        locationManager = new LocationManager(requireContext(), this);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadCategories();

        // Get user location for distance-based search
        getCurrentLocation();

        // Handle search query from bundle
        handleSearchFromBundle();
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

    private void setupRecyclerView() {
        adapter = new ProductGridAdapter(products, this);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(adapter);

        // Pagination scroll listener
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreProducts();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        // Search input
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.equals(currentQuery)) {
                    currentQuery = query;
                    scheduleSearch();
                }
            }
        });

        // Filter button
        ivFilter.setOnClickListener(v -> showFilterBottomSheet());

        // Sort button
        ivSort.setOnClickListener(v -> showSortBottomSheet());

        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(this::refreshSearch);
    }

    private void handleSearchFromBundle() {
        Bundle args = getArguments();
        if (args != null) {
            String query = args.getString(Constants.BUNDLE_SEARCH_QUERY);
            if (query != null && !query.isEmpty()) {
                etSearch.setText(query);
                currentQuery = query;
                performSearch();
            }

            // Handle other bundle parameters
            String sortBy = args.getString("sortBy");
            if (sortBy != null) {
                currentSort.sortBy = sortBy;
                currentSort.sortDirection = "desc";
            }

            boolean nearbyOnly = args.getBoolean("nearbyOnly", false);
            if (nearbyOnly && currentFilter.latitude != null && currentFilter.longitude != null) {
                currentFilter.radius = (int) Constants.DEFAULT_LOCATION_RADIUS;
            }
        }
    }

    private void scheduleSearch() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        searchRunnable = this::performSearch;
        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
    }

    private void performSearch() {
        currentPage = 0;
        isLastPage = false;
        products.clear();
        adapter.notifyDataSetChanged();

        if (currentQuery.isEmpty()) {
            showEmptyState();
            return;
        }

        searchProducts();
    }

    private void searchProducts() {
        if (isLoading) return;

        isLoading = true;
        showLoading(currentPage == 0);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .searchProducts(
                        currentQuery,
                        currentPage,
                        Constants.DEFAULT_PAGE_SIZE,
                        currentFilter.categoryId,
                        currentFilter.condition != null ? currentFilter.condition.name() : null,
                        currentFilter.minPrice != null ? currentFilter.minPrice.doubleValue() : null,
                        currentFilter.maxPrice != null ? currentFilter.maxPrice.doubleValue() : null
                );

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;
                hideLoading();

                handleSearchResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                isLoading = false;
                hideLoading();

                Log.e(TAG, "Search failed", t);
                if (products.isEmpty()) {
                    showEmptyState();
                }
                Toast.makeText(getContext(), "Search failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleSearchResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null) {
                    List<Product> newProducts = new ArrayList<>();
                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            newProducts.add(product);
                        }
                    }

                    int oldSize = products.size();
                    products.addAll(newProducts);
                    adapter.notifyItemRangeInserted(oldSize, newProducts.size());

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : newProducts.size() < Constants.DEFAULT_PAGE_SIZE;

                    // Update results count
                    Object totalElements = data.get("totalElements");
                    if (totalElements instanceof Number) {
                        int total = ((Number) totalElements).intValue();
                        updateResultsCount(total);
                    }
                }

                if (products.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                }

            } else {
                Log.e(TAG, "Search failed: " + response.message());
                if (products.isEmpty()) {
                    showEmptyState();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing search response", e);
            if (products.isEmpty()) {
                showEmptyState();
            }
        }
    }

    private Product parseProductFromMap(Map<String, Object> productMap) {
        try {
            Product product = new Product();
            product.setId(getLongFromMap(productMap, "id"));
            product.setTitle((String) productMap.get("title"));
            product.setDescription((String) productMap.get("description"));

            // Parse price
            Object priceObj = productMap.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }

            product.setLocation((String) productMap.get("location"));
            product.setCreatedAt((String) productMap.get("createdAt"));

            // Parse view count
            Object viewCountObj = productMap.get("viewCount");
            if (viewCountObj instanceof Number) {
                product.setViewCount(((Number) viewCountObj).intValue());
            }

            // Parse condition
            String conditionStr = (String) productMap.get("condition");
            if (conditionStr != null) {
                try {
                    product.setCondition(Product.ProductCondition.valueOf(conditionStr));
                } catch (IllegalArgumentException e) {
                    product.setCondition(Product.ProductCondition.GOOD);
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

            return product;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing product from map", e);
            return null;
        }
    }

    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private void loadMoreProducts() {
        if (!isLoading && !isLastPage) {
            currentPage++;
            searchProducts();
        }
    }

    private void refreshSearch() {
        currentPage = 0;
        isLastPage = false;
        products.clear();
        adapter.notifyDataSetChanged();

        if (!currentQuery.isEmpty()) {
            searchProducts();
        } else {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void loadCategories() {
        Call<StandardResponse<List<Category>>> call = ApiClient.getProductService().getCategories();
        call.enqueue(new Callback<StandardResponse<List<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Category>>> call,
                                   @NonNull Response<StandardResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    categories.clear();
                    categories.addAll(response.body().getData());
                    Log.d(TAG, "Categories loaded: " + categories.size());
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Category>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load categories", t);
            }
        });
    }

    private void getCurrentLocation() {
        locationManager.requestLocation();
    }

    private void showFilterBottomSheet() {
        SearchFilterBottomSheet bottomSheet = SearchFilterBottomSheet.newInstance(currentFilter, categories);
        bottomSheet.setOnFilterAppliedListener(this);
        bottomSheet.show(getChildFragmentManager(), "filter_bottom_sheet");
    }

    private void showSortBottomSheet() {
        SearchSortBottomSheet bottomSheet = SearchSortBottomSheet.newInstance(currentSort);
        bottomSheet.setOnSortAppliedListener(this);
        bottomSheet.show(getChildFragmentManager(), "sort_bottom_sheet");
    }

    private void updateFilterChips() {
        chipGroupFilters.removeAllViews();

        if (currentFilter.categoryId != null) {
            Category category = findCategoryById(currentFilter.categoryId);
            if (category != null) {
                addFilterChip(category.getName(), () -> {
                    currentFilter.categoryId = null;
                    updateFilterChips();
                    performSearch();
                });
            }
        }

        if (currentFilter.condition != null) {
            addFilterChip(currentFilter.condition.getDisplayName(), () -> {
                currentFilter.condition = null;
                updateFilterChips();
                performSearch();
            });
        }

        if (currentFilter.minPrice != null || currentFilter.maxPrice != null) {
            String priceRange = "Price: ";
            if (currentFilter.minPrice != null) {
                priceRange += Constants.CURRENCY_SYMBOL + currentFilter.minPrice.intValue();
            } else {
                priceRange += "0";
            }
            priceRange += " - ";
            if (currentFilter.maxPrice != null) {
                priceRange += Constants.CURRENCY_SYMBOL + currentFilter.maxPrice.intValue();
            } else {
                priceRange += "∞";
            }

            addFilterChip(priceRange, () -> {
                currentFilter.minPrice = null;
                currentFilter.maxPrice = null;
                updateFilterChips();
                performSearch();
            });
        }

        if (currentFilter.radius != null) {
            addFilterChip("Within " + currentFilter.radius + "km", () -> {
                currentFilter.radius = null;
                updateFilterChips();
                performSearch();
            });
        }
    }

    private void addFilterChip(String text, Runnable removeAction) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> removeAction.run());
        chipGroupFilters.addView(chip);
    }

    private Category findCategoryById(Long categoryId) {
        for (Category category : categories) {
            if (category.getId().equals(categoryId)) {
                return category;
            }
        }
        return null;
    }

    private void showLoading(boolean isInitialLoad) {
        if (isInitialLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
        swipeRefresh.setRefreshing(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void showEmptyState() {
        tvEmpty.setVisibility(View.VISIBLE);
        tvResultsCount.setVisibility(View.GONE);

        if (currentQuery.isEmpty()) {
            tvEmpty.setText("Search for products, brands, or categories");
        } else {
            tvEmpty.setText("No products found for \"" + currentQuery + "\"");
        }
    }

    private void hideEmptyState() {
        tvEmpty.setVisibility(View.GONE);
    }

    private void updateResultsCount(int count) {
        tvResultsCount.setVisibility(View.VISIBLE);
        tvResultsCount.setText(getString(R.string.search_results_count, count));
    }

    // Interface implementations
    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public void onFilterApplied(SearchFilter filter) {
        currentFilter = filter;
        updateFilterChips();
        performSearch();
    }

    @Override
    public void onSortApplied(SearchSort sort) {
        currentSort = sort;
        performSearch();
    }

    @Override
    public void onLocationReceived(Location location) {
        currentFilter.latitude = location.getLatitude();
        currentFilter.longitude = location.getLongitude();
        prefsManager.saveLocation(location.getLatitude(), location.getLongitude(), null);
        Log.d(TAG, "Location received for search");
    }

    @Override
    public void onLocationError(String error) {
        Log.e(TAG, "Location error: " + error);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.cleanup();
        }
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
    }
}