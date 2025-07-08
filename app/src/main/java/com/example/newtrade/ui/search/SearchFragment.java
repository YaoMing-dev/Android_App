// app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ProductService;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.DateUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final int SEARCH_DELAY_MS = 300;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

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
    private final List<Category> categories = new ArrayList<>();
    private String currentQuery = "";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Services
    private ProductService productService;
    private ApiService apiService;
    private SharedPrefsManager prefsManager;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;

    // Filters
    private Long selectedCategoryId = null;
    private String selectedCategoryName = null;
    private String selectedCondition = null;
    private Double minPrice = null;
    private Double maxPrice = null;
    private String currentSortOption = "relevance";
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

        initServices();
        initViews(view);
        setupRecyclerViews();
        setupListeners();
        loadCategories();

        // ✅ Load tất cả sản phẩm ban đầu
        loadAllProductsInitially();

        Log.d(TAG, "SearchFragment created successfully");
    }

    private void initServices() {
        productService = ApiClient.getProductService();
        apiService = ApiClient.getApiService();
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        geocoder = new Geocoder(requireContext(), Locale.getDefault());
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

        if (tvSortOption != null) {
            View llSortOptions = requireView().findViewById(R.id.ll_sort_options);
            if (llSortOptions != null) {
                llSortOptions.setOnClickListener(v -> showSortDialog());
            }
        }

        Log.d(TAG, "✅ SearchFragment listeners setup completed");
    }

    // ===============================
    // ✅ COPY EXACT HomeFragment METHOD
    // ===============================

    private void loadAllProductsInitially() {
        Log.d(TAG, "🔄 Loading all products initially...");
        showLoadingState();

        // ✅ COPY EXACT từ HomeFragment
        ApiClient.getApiService().getProducts().enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                try {
                    Log.d(TAG, "✅ Initial products API response: " + response.code());
                    Log.d(TAG, "✅ Response URL: " + call.request().url());

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Map<String, Object> data = apiResponse.getData();

                            // ✅ COPY EXACT parse logic từ HomeFragment
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            if (productList != null) {
                                searchResults.clear();

                                for (Map<String, Object> productData : productList) {
                                    // ✅ COPY EXACT parseProductFromMap từ HomeFragment
                                    Product product = parseProductFromMapHomeStyle(productData);
                                    if (product != null) {
                                        searchResults.add(product);
                                    }
                                }

                                if (searchAdapter != null) {
                                    searchAdapter.notifyDataSetChanged();
                                }

                                showResults();
                                Log.d(TAG, "✅ Initially loaded " + searchResults.size() + " products");
                            }
                        } else {
                            Log.w(TAG, "❌ Products response unsuccessful");
                            showEmptyResults();
                        }
                    } else {
                        Log.w(TAG, "❌ Products response failed: HTTP " + response.code());
                        showEmptyResults();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing products", e);
                    showEmptyResults();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Products API call failed", t);
                showEmptyResults();
            }
        });
    }

    // ✅ COPY EXACT parseProductFromMap từ HomeFragment
    private Product parseProductFromMapHomeStyle(Map<String, Object> productData) {
        try {
            Product product = new Product();

            // Basic fields
            Object idObj = productData.get("id");
            if (idObj instanceof Number) {
                product.setId(((Number) idObj).longValue());
            }

            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));
            product.setLocation((String) productData.get("location"));

            // Price
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }

            // Category
            Object categoryIdObj = productData.get("categoryId");
            if (categoryIdObj instanceof Number) {
                product.setCategoryId(((Number) categoryIdObj).longValue());
            }

            // Condition
            String conditionStr = (String) productData.get("condition");
            if (conditionStr != null) {
                try {
                    Product.ProductCondition condition = Product.ProductCondition.valueOf(conditionStr);
                    product.setCondition(condition);
                } catch (IllegalArgumentException e) {
                    product.setCondition(Product.ProductCondition.GOOD);
                }
            }

            // Status
            String statusStr = (String) productData.get("status");
            if (statusStr != null) {
                try {
                    Product.ProductStatus status = Product.ProductStatus.valueOf(statusStr);
                    product.setStatus(status);
                } catch (IllegalArgumentException e) {
                    product.setStatus(Product.ProductStatus.AVAILABLE);
                }
            }

            // ✅ IMAGE PARSING - COPY EXACT từ HomeFragment
            String imageUrl = (String) productData.get("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // ✅ Set imageUrl directly - ProductAdapter sẽ handle base URL
                product.setImageUrl(imageUrl);
                Log.d(TAG, "✅ Product " + product.getId() + " imageUrl: " + imageUrl);
            } else {
                Log.w(TAG, "❌ No imageUrl for product " + product.getId());
            }

            // Created date
            String createdAtStr = (String) productData.get("createdAt");
            if (createdAtStr != null && DateUtils.isValidDate(createdAtStr)) {
                product.setCreatedAt(createdAtStr);
            } else {
                product.setCreatedAt(DateUtils.getCurrentTimestamp());
            }

            return product;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing product from map", e);
            return null;
        }
    }

    // ===============================
    // LOAD CATEGORIES FROM BACKEND
    // ===============================

    private void loadCategories() {
        Log.d(TAG, "🔄 Loading categories from backend...");

        apiService.getCategories().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                   @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        categories.clear();

                        for (Map<String, Object> categoryData : apiResponse.getData()) {
                            Category category = new Category();

                            Object idObj = categoryData.get("id");
                            if (idObj instanceof Number) {
                                category.setId(((Number) idObj).longValue());
                            }

                            category.setName((String) categoryData.get("name"));
                            category.setDescription((String) categoryData.get("description"));
                            category.setIcon((String) categoryData.get("icon"));

                            categories.add(category);
                        }

                        Log.d(TAG, "✅ Loaded " + categories.size() + " categories from backend:");
                        for (Category cat : categories) {
                            Log.d(TAG, "  - Category: " + cat.getId() + " = " + cat.getName());
                        }
                    } else {
                        Log.w(TAG, "❌ Categories response unsuccessful: " + apiResponse.getMessage());
                        loadSampleCategories();
                    }
                } else {
                    Log.w(TAG, "❌ Categories response failed: HTTP " + response.code());
                    loadSampleCategories();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Categories API call failed", t);
                loadSampleCategories();
            }
        });
    }

    private void loadSampleCategories() {
        categories.clear();
        categories.add(new Category(1L, "Electronics", "Electronics devices", "electronics", true));
        categories.add(new Category(2L, "Fashion", "Clothing and accessories", "fashion", true));
        categories.add(new Category(3L, "Home & Garden", "Home decor and garden", "home", true));
        categories.add(new Category(4L, "Books", "Books and educational materials", "books", true));
        categories.add(new Category(5L, "Sports", "Sports and outdoor equipment", "sports", true));
        categories.add(new Category(6L, "Beauty", "Beauty and health products", "beauty", true));
        categories.add(new Category(7L, "Vehicles", "Cars and motorbikes", "vehicles", true));
        categories.add(new Category(8L, "Toys", "Toys and kids items", "toys", true));

        Log.d(TAG, "✅ Loaded sample categories");
    }

    // ===============================
    // ✅ FIXED SEARCH API METHODS
    // ===============================

    private void scheduleSearch() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        searchRunnable = this::performSearch;
        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
    }

    private void performSearch() {
        String query = currentQuery.trim();

        // ✅ Nếu không có query và filter, load tất cả sản phẩm
        if (query.isEmpty() && !hasActiveFilters()) {
            loadAllProductsInitially();
            return;
        }

        showLoadingState();

        // ✅ Fallback: Nếu có filters, load all products rồi filter client-side
        if (hasActiveFilters() || !query.isEmpty()) {
            loadAllProductsAndFilter();
        }
    }

    // ✅ NEW METHOD: Load all products và filter client-side để tránh lỗi search API
    private void loadAllProductsAndFilter() {
        Log.d(TAG, "🔄 Loading all products and applying filters...");

        ApiClient.getApiService().getProducts().enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Map<String, Object> data = apiResponse.getData();

                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            if (productList != null) {
                                searchResults.clear();

                                for (Map<String, Object> productData : productList) {
                                    Product product = parseProductFromMapHomeStyle(productData);
                                    if (product != null) {
                                        searchResults.add(product);
                                    }
                                }

                                // ✅ Apply client-side filtering
                                applyClientSideFilters();
                                applySorting();

                                if (searchAdapter != null) {
                                    searchAdapter.notifyDataSetChanged();
                                }

                                showResults();
                                Log.d(TAG, "✅ Filtered results: " + searchResults.size() + " products");
                            }
                        } else {
                            Log.w(TAG, "❌ Products response unsuccessful");
                            showEmptyResults();
                        }
                    } else {
                        Log.w(TAG, "❌ Products response failed: HTTP " + response.code());
                        showEmptyResults();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing products", e);
                    showEmptyResults();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Products API call failed", t);
                showEmptyResults();
            }
        });
    }

    // ✅ CLIENT-SIDE FILTERING
    private void applyClientSideFilters() {
        if (searchResults.isEmpty()) return;

        List<Product> originalResults = new ArrayList<>(searchResults);
        searchResults.clear();

        for (Product product : originalResults) {
            boolean passesFilters = true;

            // Query filter
            if (!currentQuery.trim().isEmpty() && product.getTitle() != null) {
                String query = currentQuery.trim().toLowerCase();
                String title = product.getTitle().toLowerCase();
                if (!title.contains(query)) {
                    passesFilters = false;
                }
            }

            // ✅ Price filter - FIXED LOGIC
            if (minPrice != null && product.getPrice() != null) {
                if (product.getPrice().doubleValue() < minPrice) {
                    passesFilters = false;
                }
            }
            if (maxPrice != null && product.getPrice() != null) {
                if (product.getPrice().doubleValue() > maxPrice) {
                    passesFilters = false;
                }
            }

            // Condition filter
            if (selectedCondition != null && product.getCondition() != null) {
                if (!selectedCondition.equals(product.getCondition().name())) {
                    passesFilters = false;
                }
            }

            // Category filter
            if (selectedCategoryId != null && product.getCategoryId() != null) {
                if (!selectedCategoryId.equals(product.getCategoryId())) {
                    passesFilters = false;
                }
            }

            if (passesFilters) {
                searchResults.add(product);
            }
        }

        Log.d(TAG, "✅ Client-side filtering applied:");
        Log.d(TAG, "  - Query: '" + currentQuery + "'");
        Log.d(TAG, "  - Price range: " + minPrice + " - " + maxPrice);
        Log.d(TAG, "  - Category: " + selectedCategoryId);
        Log.d(TAG, "  - Condition: " + selectedCondition);
        Log.d(TAG, "  - Results: " + searchResults.size() + " / " + originalResults.size());
    }

    private void applySorting() {
        if (searchResults.isEmpty()) return;

        switch (currentSortOption) {
            case "newest":
                searchResults.sort((p1, p2) -> {
                    String date1 = p1.getCreatedAt();
                    String date2 = p2.getCreatedAt();

                    if (!DateUtils.isValidDate(date1) && !DateUtils.isValidDate(date2)) {
                        return 0;
                    }
                    if (!DateUtils.isValidDate(date1)) {
                        return 1;
                    }
                    if (!DateUtils.isValidDate(date2)) {
                        return -1;
                    }

                    Date d1 = DateUtils.parseBackendDate(date1);
                    Date d2 = DateUtils.parseBackendDate(date2);

                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1;
                    if (d2 == null) return -1;

                    return d2.compareTo(d1);
                });
                break;

            case "price_low":
                searchResults.sort((p1, p2) -> {
                    if (p1.getPrice() != null && p2.getPrice() != null) {
                        return p1.getPrice().compareTo(p2.getPrice());
                    }
                    return 0;
                });
                break;

            case "price_high":
                searchResults.sort((p1, p2) -> {
                    if (p1.getPrice() != null && p2.getPrice() != null) {
                        return p2.getPrice().compareTo(p1.getPrice());
                    }
                    return 0;
                });
                break;

            case "relevance":
            default:
                break;
        }

        if (searchAdapter != null) {
            searchAdapter.notifyItemRangeChanged(0, searchResults.size());
        }

        Log.d(TAG, "✅ Applied sorting: " + currentSortOption);
    }

    private boolean hasActiveFilters() {
        return selectedCategoryId != null ||
                selectedCondition != null ||
                minPrice != null ||
                maxPrice != null ||
                (searchLatitude != null && searchLongitude != null);
    }

    // ===============================
    // FILTER DIALOG IMPLEMENTATIONS
    // ===============================

    private void showCategoryFilterDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(getContext(), "Loading categories...", Toast.LENGTH_SHORT).show();
            loadCategories();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Category");

        String[] categoryNames = new String[categories.size() + 1];
        categoryNames[0] = "All Categories";

        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i + 1] = categories.get(i).getName();
        }

        int selectedIndex = 0;
        if (selectedCategoryId != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId().equals(selectedCategoryId)) {
                    selectedIndex = i + 1;
                    break;
                }
            }
        }

        builder.setSingleChoiceItems(categoryNames, selectedIndex, (dialog, which) -> {
            if (which == 0) {
                selectedCategoryId = null;
                selectedCategoryName = null;
            } else {
                Category category = categories.get(which - 1);
                selectedCategoryId = category.getId();
                selectedCategoryName = category.getName();

                Log.d(TAG, "✅ Selected category: " + selectedCategoryId + " = " + selectedCategoryName);
            }

            updateFilterChips();
            scheduleSearch();
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showPriceFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Price Range");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_price_filter, null);

        TextInputEditText etMinPrice = dialogView.findViewById(R.id.et_min_price);
        TextInputEditText etMaxPrice = dialogView.findViewById(R.id.et_max_price);

        MaterialButton btnUnder10M = dialogView.findViewById(R.id.btn_under_10m);
        MaterialButton btn10MTo50M = dialogView.findViewById(R.id.btn_10m_to_50m);
        MaterialButton btnOver50M = dialogView.findViewById(R.id.btn_over_50m);

        // Set current values
        if (minPrice != null) {
            etMinPrice.setText(String.valueOf(minPrice.longValue()));
        }
        if (maxPrice != null) {
            etMaxPrice.setText(String.valueOf(maxPrice.longValue()));
        }

        // Quick price option listeners
        btnUnder10M.setOnClickListener(v -> {
            etMinPrice.setText("");
            etMaxPrice.setText("10000000");
        });

        btn10MTo50M.setOnClickListener(v -> {
            etMinPrice.setText("10000000");
            etMaxPrice.setText("50000000");
        });

        btnOver50M.setOnClickListener(v -> {
            etMinPrice.setText("50000000");
            etMaxPrice.setText("");
        });

        builder.setView(dialogView);

        builder.setPositiveButton("Apply", (dialog, which) -> {
            try {
                String minPriceStr = etMinPrice.getText().toString().trim();
                String maxPriceStr = etMaxPrice.getText().toString().trim();

                minPrice = minPriceStr.isEmpty() ? null : Double.valueOf(minPriceStr);
                maxPrice = maxPriceStr.isEmpty() ? null : Double.valueOf(maxPriceStr);

                Log.d(TAG, "✅ Price filter applied: " + minPrice + " - " + maxPrice);

                updateFilterChips();
                scheduleSearch();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.setNeutralButton("Clear", (dialog, which) -> {
            minPrice = null;
            maxPrice = null;
            Log.d(TAG, "✅ Price filter cleared");
            updateFilterChips();
            scheduleSearch();
        });

        builder.show();
    }

    private void showLocationFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_filter, null);

        TextInputEditText etCustomLocation = dialogView.findViewById(R.id.et_custom_location);
        SeekBar seekBarRadius = dialogView.findViewById(R.id.seek_bar_radius);
        TextView tvRadiusValue = dialogView.findViewById(R.id.tv_radius_value);
        TextView tvCurrentLocationStatus = dialogView.findViewById(R.id.tv_current_location_status);

        MaterialButton btnUseCurrentLocation = dialogView.findViewById(R.id.btn_use_current_location);
        MaterialButton btnUseCustomLocation = dialogView.findViewById(R.id.btn_use_custom_location);
        MaterialButton btnClear = dialogView.findViewById(R.id.btn_clear);
        MaterialButton btnApply = dialogView.findViewById(R.id.btn_apply);

        etCustomLocation.setText(searchLocationName);
        seekBarRadius.setProgress(Math.max(0, searchRadiusKm - 5));
        tvRadiusValue.setText(searchRadiusKm + " km");

        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int radius = progress + 5;
                tvRadiusValue.setText(radius + " km");
                searchRadiusKm = radius;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnUseCurrentLocation.setOnClickListener(v -> {
            getCurrentLocationWithGeocoding(tvCurrentLocationStatus, btnApply);
        });

        btnUseCustomLocation.setOnClickListener(v -> {
            String customLocation = etCustomLocation.getText().toString().trim();
            if (!customLocation.isEmpty()) {
                searchLocationName = customLocation;
                btnApply.setEnabled(true);
                Toast.makeText(getContext(), "Custom location set: " + customLocation, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter a location", Toast.LENGTH_SHORT).show();
            }
        });

        btnClear.setOnClickListener(v -> {
            searchLocationName = "";
            searchLatitude = null;
            searchLongitude = null;
            searchRadiusKm = 10;
            etCustomLocation.setText("");
            seekBarRadius.setProgress(5);
            tvCurrentLocationStatus.setVisibility(View.GONE);
            btnApply.setEnabled(false);
        });

        btnApply.setOnClickListener(v -> {
            updateFilterChips();
            scheduleSearch();
            builder.create().dismiss();
        });

        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void getCurrentLocationWithGeocoding(TextView statusView, MaterialButton applyButton) {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        statusView.setText("🔄 Getting your location...");
        statusView.setVisibility(View.VISIBLE);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        searchLatitude = location.getLatitude();
                        searchLongitude = location.getLongitude();

                        convertCoordinatesToAddress(location.getLatitude(), location.getLongitude(),
                                statusView, applyButton);

                        Log.d(TAG, "✅ Current location: " + searchLatitude + ", " + searchLongitude);
                    } else {
                        statusView.setText("❌ Unable to get current location");
                        Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location", e);
                    statusView.setText("❌ Failed to get location");
                    Toast.makeText(getContext(), "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void convertCoordinatesToAddress(double latitude, double longitude,
                                             TextView statusView, MaterialButton applyButton) {

        new Thread(() -> {
            try {
                if (geocoder != null && Geocoder.isPresent()) {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);

                        StringBuilder addressText = new StringBuilder();

                        if (address.getSubThoroughfare() != null) {
                            addressText.append(address.getSubThoroughfare()).append(" ");
                        }
                        if (address.getThoroughfare() != null) {
                            addressText.append(address.getThoroughfare()).append(", ");
                        }

                        if (address.getSubLocality() != null) {
                            addressText.append(address.getSubLocality()).append(", ");
                        }

                        if (address.getLocality() != null) {
                            addressText.append(address.getLocality());
                        } else if (address.getAdminArea() != null) {
                            addressText.append(address.getAdminArea());
                        }

                        if (address.getCountryName() != null) {
                            addressText.append(", ").append(address.getCountryName());
                        }

                        String finalAddress = addressText.toString();
                        if (finalAddress.isEmpty()) {
                            finalAddress = "Current Location";
                        }

                        searchLocationName = finalAddress;

                        requireActivity().runOnUiThread(() -> {
                            statusView.setText("📍 " + searchLocationName);
                            applyButton.setEnabled(true);
                            Log.d(TAG, "✅ Geocoded address: " + searchLocationName);
                        });

                    } else {
                        requireActivity().runOnUiThread(() -> {
                            searchLocationName = "Current Location (" +
                                    String.format("%.4f", latitude) + ", " +
                                    String.format("%.4f", longitude) + ")";
                            statusView.setText("📍 " + searchLocationName);
                            applyButton.setEnabled(true);
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        searchLocationName = "Current Location";
                        statusView.setText("📍 " + searchLocationName);
                        applyButton.setEnabled(true);
                    });
                }

            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);

                requireActivity().runOnUiThread(() -> {
                    searchLocationName = "Current Location";
                    statusView.setText("📍 " + searchLocationName);
                    applyButton.setEnabled(true);
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Location permission granted. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showConditionFilterDialog() {
        String[] conditions = {"NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};
        String[] conditionDisplayNames = {"New", "Like New", "Good", "Fair", "Poor"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Condition");

        int selectedIndex = -1;
        if (selectedCondition != null) {
            for (int i = 0; i < conditions.length; i++) {
                if (conditions[i].equals(selectedCondition)) {
                    selectedIndex = i;
                    break;
                }
            }
        }

        builder.setSingleChoiceItems(conditionDisplayNames, selectedIndex, (dialog, which) -> {
            selectedCondition = conditions[which];
            Log.d(TAG, "✅ Selected condition: " + selectedCondition);
            updateFilterChips();
            scheduleSearch();
            dialog.dismiss();
        });

        builder.setNeutralButton("Clear", (dialog, which) -> {
            selectedCondition = null;
            Log.d(TAG, "✅ Condition filter cleared");
            updateFilterChips();
            scheduleSearch();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showSortDialog() {
        String[] sortOptions = {"Relevance", "Newest First", "Price: Low to High", "Price: High to Low"};
        String[] sortValues = {"relevance", "newest", "price_low", "price_high"};

        int selectedIndex = 0;
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(currentSortOption)) {
                selectedIndex = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sort by");
        builder.setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
            currentSortOption = sortValues[which];
            if (tvSortOption != null) {
                tvSortOption.setText(sortOptions[which]);
            }
            applySorting();
            dialog.dismiss();
        });
        builder.show();
    }

    // ===============================
    // FILTER CHIPS MANAGEMENT
    // ===============================

    private void updateFilterChips() {
        if (chipGroupFilters != null) {
            chipGroupFilters.removeAllViews();

            if (selectedCategoryName != null) {
                addFilterChip("Category: " + selectedCategoryName, () -> {
                    selectedCategoryId = null;
                    selectedCategoryName = null;
                    scheduleSearch();
                });
            }

            if (selectedCondition != null) {
                addFilterChip("Condition: " + selectedCondition, () -> {
                    selectedCondition = null;
                    scheduleSearch();
                });
            }

            if (minPrice != null || maxPrice != null) {
                String priceText = "Price: ";
                if (minPrice != null && maxPrice != null) {
                    priceText += formatPrice(minPrice) + " - " + formatPrice(maxPrice);
                } else if (minPrice != null) {
                    priceText += "From " + formatPrice(minPrice);
                } else {
                    priceText += "Up to " + formatPrice(maxPrice);
                }

                addFilterChip(priceText, () -> {
                    minPrice = null;
                    maxPrice = null;
                    scheduleSearch();
                });
            }

            if (!searchLocationName.isEmpty()) {
                addFilterChip("Location: " + searchLocationName + " (" + searchRadiusKm + "km)", () -> {
                    searchLocationName = "";
                    searchLatitude = null;
                    searchLongitude = null;
                    scheduleSearch();
                });
            }
        }
    }

    private String formatPrice(Double price) {
        if (price >= 1000000) {
            return String.format("%.0fM", price / 1000000);
        } else if (price >= 1000) {
            return String.format("%.0fK", price / 1000);
        } else {
            return String.format("%.0f", price);
        }
    }

    private void addFilterChip(String text, Runnable onCloseClick) {
        if (getContext() != null && chipGroupFilters != null) {
            Chip chip = new Chip(getContext());
            chip.setText(text);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                chipGroupFilters.removeView(chip);
                onCloseClick.run();
                updateFilterChips();
            });
            chipGroupFilters.addView(chip);
        }
    }

    // ===============================
    // UI STATE MANAGEMENT
    // ===============================

    private void showInitialState() {
        loadAllProductsInitially();
    }

    private void showLoadingState() {
        isSearching = true;
        if (llLoadingState != null) llLoadingState.setVisibility(View.VISIBLE);
        if (llRecentSearches != null) llRecentSearches.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
    }

    private void showResults() {
        isSearching = false;

        if (searchResults.isEmpty()) {
            showEmptyResults();
        } else {
            if (rvSearchResults != null) rvSearchResults.setVisibility(View.VISIBLE);
            if (tvResultsCount != null) tvResultsCount.setVisibility(View.VISIBLE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
            if (llRecentSearches != null) llRecentSearches.setVisibility(View.GONE);
            if (llLoadingState != null) llLoadingState.setVisibility(View.GONE);
        }

        updateResultsCount(searchResults.size());
    }

    private void showEmptyResults() {
        searchResults.clear();
        if (searchAdapter != null) {
            searchAdapter.notifyDataSetChanged();
        }
        if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (llRecentSearches != null) llRecentSearches.setVisibility(View.GONE);
        if (llLoadingState != null) llLoadingState.setVisibility(View.GONE);
        if (tvResultsCount != null) tvResultsCount.setVisibility(View.GONE);
    }

    private void updateResultsCount(int count) {
        if (tvResultsCount != null) {
            String text = count == 1 ? count + " result found" : count + " results found";
            tvResultsCount.setText(text);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "Error: " + message);
    }

    private void navigateToProductDetail(Product product) {
        if (getContext() != null && product != null) {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_title", product.getTitle());
            if (product.getPrice() != null) {
                intent.putExtra("product_price", product.getPrice().toString());
            }
            startActivity(intent);
        }
    }
}