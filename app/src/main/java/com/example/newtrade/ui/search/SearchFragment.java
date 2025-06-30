// app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

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
import com.example.newtrade.ui.product.adapter.ProductGridAdapter;
import com.example.newtrade.ui.search.filter.SearchFilterBottomSheet;
import com.example.newtrade.ui.search.sort.SearchSortBottomSheet;
import com.example.newtrade.utils.LocationManager;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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
    private static final int SEARCH_DELAY_MS = 200; // FR-3.1.2: 200ms delay

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
                if (layoutManager != null && !isLoading && !isLastPage && !currentQuery.isEmpty()) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 4) {
                        loadMoreProducts();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        // Search text watcher with 200ms delay
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                String query = s.toString().trim();

                // Create new search runnable
                searchRunnable = () -> {
                    if (!query.equals(currentQuery)) {
                        currentQuery = query;
                        performSearch();
                    }
                };

                // Post with delay
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter button
        ivFilter.setOnClickListener(v -> showFilterDialog());

        // Sort button
        ivSort.setOnClickListener(v -> showSortDialog());

        // Swipe refresh
        swipeRefresh.setOnRefreshListener(this::refreshSearch);
    }

    private void loadCategories() {
        ApiClient.getProductService().getCategories()
                .enqueue(new Callback<StandardResponse<List<Category>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<List<Category>>> call,
                                           Response<StandardResponse<List<Category>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<List<Category>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                categories.clear();
                                categories.addAll(apiResponse.getData());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<List<Category>>> call, Throwable t) {
                        Log.e(TAG, "Failed to load categories", t);
                    }
                });
    }

    private void getCurrentLocation() {
        if (locationManager.hasLocationPermission()) {
            locationManager.getCurrentLocation();
        }
    }

    @Override
    public void onLocationReceived(android.location.Location location) {
        currentFilter.latitude = location.getLatitude();
        currentFilter.longitude = location.getLongitude();
        prefsManager.saveLastLocation(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onLocationError(String error) {
        // Use last known location
        currentFilter.latitude = prefsManager.getLastLatitude();
        currentFilter.longitude = prefsManager.getLastLongitude();
    }

    private void performSearch() {
        if (currentQuery.isEmpty()) {
            clearResults();
            return;
        }

        currentPage = 0;
        isLastPage = false;
        setLoading(true);

        searchProducts(false);
    }

    private void loadMoreProducts() {
        if (isLoading || isLastPage || currentQuery.isEmpty()) return;

        currentPage++;
        setLoading(true);
        searchProducts(true);
    }

    private void searchProducts(boolean isLoadMore) {
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService().searchProducts(
                currentQuery,
                currentPage,
                20, // page size
                currentFilter.categoryId,
                currentFilter.condition,
                currentFilter.minPrice,
                currentFilter.maxPrice,
                currentFilter.latitude,
                currentFilter.longitude,
                currentFilter.radius,
                currentSort.sortBy,
                currentSort.sortDirection
        );

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        handleSearchResponse(apiResponse.getData(), isLoadMore);
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Search failed");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                setLoading(false);
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Search failed", t);
                showError("Network error");
            }
        });
    }

    private void handleSearchResponse(Map<String, Object> data, boolean isLoadMore) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productsData = (List<Map<String, Object>>) data.get("content");

            List<Product> newProducts = new ArrayList<>();
            if (productsData != null) {
                for (Map<String, Object> productData : productsData) {
                    Product product = parseProductFromMap(productData);
                    newProducts.add(product);
                }
            }

            if (isLoadMore) {
                int startPosition = products.size();
                products.addAll(newProducts);
                adapter.notifyItemRangeInserted(startPosition, newProducts.size());
            } else {
                products.clear();
                products.addAll(newProducts);
                adapter.notifyDataSetChanged();
            }

            // Update pagination info
            Boolean isLast = (Boolean) data.get("last");
            isLastPage = isLast != null && isLast;

            // Update results count
            Long totalElements = (Long) data.get("totalElements");
            if (totalElements != null) {
                tvResultsCount.setText(getString(R.string.results_count, totalElements.intValue()));
                tvResultsCount.setVisibility(View.VISIBLE);
            }

            updateEmptyState();

        } catch (Exception e) {
            Log.e(TAG, "Error parsing search response", e);
            showError("Error loading search results");
        }
    }

    private Product parseProductFromMap(Map<String, Object> data) {
        // Same parsing logic as in MyProductsFragment
        Product product = new Product();

        if (data.get("id") != null) {
            product.setId(Long.valueOf(data.get("id").toString()));
        }
        product.setTitle((String) data.get("title"));
        product.setDescription((String) data.get("description"));

        if (data.get("price") != null) {
            product.setPrice(new java.math.BigDecimal(data.get("price").toString()));
        }

        String conditionStr = (String) data.get("condition");
        if (conditionStr != null) {
            try {
                product.setCondition(Product.ProductCondition.valueOf(conditionStr));
            } catch (IllegalArgumentException e) {
                product.setCondition(Product.ProductCondition.GOOD);
            }
        }

        product.setLocation((String) data.get("location"));

        if (data.get("viewCount") != null) {
            product.setViewCount(Integer.valueOf(data.get("viewCount").toString()));
        }

        // Parse images
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> imagesData = (List<Map<String, Object>>) data.get("images");
        if (imagesData != null) {
            List<com.example.newtrade.models.ProductImage> images = new ArrayList<>();
            for (Map<String, Object> imageData : imagesData) {
                com.example.newtrade.models.ProductImage image = new com.example.newtrade.models.ProductImage();
                image.setImageUrl((String) imageData.get("imageUrl"));
                images.add(image);
            }
            product.setImages(images);
        }

        return product;
    }

    private void showFilterDialog() {
        SearchFilterBottomSheet filterSheet = SearchFilterBottomSheet.newInstance(currentFilter, categories);
        filterSheet.setOnFilterAppliedListener(this);
        filterSheet.show(getChildFragmentManager(), "filter");
    }

    private void showSortDialog() {
        SearchSortBottomSheet sortSheet = SearchSortBottomSheet.newInstance(currentSort);
        sortSheet.setOnSortAppliedListener(this);
        sortSheet.show(getChildFragmentManager(), "sort");
    }

    @Override
    public void onFilterApplied(SearchFilter filter) {
        currentFilter = filter;
        updateFilterChips();
        if (!currentQuery.isEmpty()) {
            performSearch();
        }
    }

    @Override
    public void onSortApplied(SearchSort sort) {
        currentSort = sort;
        if (!currentQuery.isEmpty()) {
            performSearch();
        }
    }

    private void updateFilterChips() {
        chipGroupFilters.removeAllViews();

        if (currentFilter.categoryId != null) {
            Category category = findCategoryById(currentFilter.categoryId);
            if (category != null) {
                addFilterChip("Category: " + category.getName(), () -> {
                    currentFilter.categoryId = null;
                    updateFilterChips();
                    performSearch();
                });
            }
        }

        if (currentFilter.condition != null) {
            addFilterChip("Condition: " + currentFilter.condition, () -> {
                currentFilter.condition = null;
                updateFilterChips();
                performSearch();
            });
        }

        if (currentFilter.minPrice != null || currentFilter.maxPrice != null) {
            String priceText = "Price: ";
            if (currentFilter.minPrice != null && currentFilter.maxPrice != null) {
                priceText += "₫" + String.format("%,.0f", currentFilter.minPrice) +
                        " - ₫" + String.format("%,.0f", currentFilter.maxPrice);
            } else if (currentFilter.minPrice != null) {
                priceText += "From ₫" + String.format("%,.0f", currentFilter.minPrice);
            } else {
                priceText += "Up to ₫" + String.format("%,.0f", currentFilter.maxPrice);
            }

            addFilterChip(priceText, () -> {
                currentFilter.minPrice = null;
                currentFilter.maxPrice = null;
                updateFilterChips();
                performSearch();
            });
        }

        if (currentFilter.radius != null && currentFilter.radius < 100) {
            addFilterChip("Within " + currentFilter.radius + "km", () -> {
                currentFilter.radius = null;
                updateFilterChips();
                performSearch();
            });
        }
    }

    private void addFilterChip(String text, Runnable onClose) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> onClose.run());
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

    private void clearResults() {
        products.clear();
        adapter.notifyDataSetChanged();
        tvResultsCount.setVisibility(View.GONE);
        updateEmptyState();
    }

    private void refreshSearch() {
        if (!currentQuery.isEmpty()) {
            performSearch();
        } else {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void updateEmptyState() {
        if (products.isEmpty() && !currentQuery.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No products found for \"" + currentQuery + "\"");
            rvProducts.setVisibility(View.GONE);
        } else if (products.isEmpty() && currentQuery.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Search for products, brands, or categories");
            rvProducts.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        if (currentPage == 0) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProductClick(Product product) {
        // Navigate to product detail
        android.content.Intent intent = new android.content.Intent(getContext(),
                com.example.newtrade.ui.product.ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        if (locationManager != null) {
            locationManager.cleanup();
        }
    }

    // Search filter and sort classes
    public static class SearchFilter {
        public Long categoryId;
        public String condition;
        public Double minPrice;
        public Double maxPrice;
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
    }

    public static class SearchSort {
        public String sortBy = "createdAt"; // Default sort
        public String sortDirection = "desc";

        public SearchSort() {}

        public SearchSort(String sortBy, String sortDirection) {
            this.sortBy = sortBy;
            this.sortDirection = sortDirection;
        }
    }
}