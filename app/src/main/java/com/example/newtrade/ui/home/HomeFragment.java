// app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
package com.example.newtrade.ui.home;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.home.adapter.CategoriesAdapter;
import com.example.newtrade.ui.home.adapter.HomeSectionsAdapter;
import com.example.newtrade.ui.home.adapter.ProductSectionAdapter;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.search.SearchActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.LocationManager;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements
        LocationManager.LocationCallback,
        CategoriesAdapter.OnCategoryClickListener,
        ProductSectionAdapter.OnProductClickListener {

    private static final String TAG = "HomeFragment";

    // UI Components
    private TextView tvWelcome;
    private TextInputEditText etSearch;
    private RecyclerView rvCategories, rvContent;
    private SwipeRefreshLayout swipeRefresh;

    // Adapters
    private CategoriesAdapter categoriesAdapter;
    private HomeSectionsAdapter sectionsAdapter;

    // Data
    private List<Category> categories = new ArrayList<>();
    private List<HomeSection> homeSections = new ArrayList<>();

    // Utils
    private SharedPrefsManager prefsManager;
    private LocationManager locationManager;
    private Double currentLatitude;
    private Double currentLongitude;

    // State
    private boolean isLoadingData = false;

    public static class HomeSection {
        public enum SectionType {
            RECENT_PRODUCTS,
            NEARBY_PRODUCTS,
            POPULAR_PRODUCTS,
            RECOMMENDED_PRODUCTS
        }

        private String title;
        private SectionType type;
        private List<Product> products;

        public HomeSection(String title, SectionType type, List<Product> products) {
            this.title = title;
            this.type = type;
            this.products = products;
        }

        // Getters
        public String getTitle() { return title; }
        public SectionType getType() { return type; }
        public List<Product> getProducts() { return products; }

        // Setters
        public void setTitle(String title) { this.title = title; }
        public void setType(SectionType type) { this.type = type; }
        public void setProducts(List<Product> products) { this.products = products; }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = new SharedPrefsManager(requireContext());
        locationManager = new LocationManager(requireContext(), this);

        initViews(view);
        setupRecyclerViews();
        setupListeners();

        loadHomeData();
        getCurrentLocation();
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_welcome);
        etSearch = view.findViewById(R.id.et_search);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvContent = view.findViewById(R.id.rv_content);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        // Set welcome message
        String userName = prefsManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText(getString(R.string.welcome_user, userName));
        } else {
            tvWelcome.setText("Welcome to NewTrade!");
        }
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        categoriesAdapter = new CategoriesAdapter(categories, this);
        rvCategories.setAdapter(categoriesAdapter);

        // Content RecyclerView
        rvContent.setLayoutManager(new LinearLayoutManager(requireContext()));
        sectionsAdapter = new HomeSectionsAdapter(homeSections, this);
        rvContent.setAdapter(sectionsAdapter);
    }

    private void setupListeners() {
        // Search input click
        etSearch.setOnClickListener(v -> openSearchActivity());
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    openSearchActivity(s.toString());
                }
            }
        });

        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    private void openSearchActivity() {
        openSearchActivity(null);
    }

    private void openSearchActivity(String query) {
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        if (query != null && !query.trim().isEmpty()) {
            intent.putExtra(Constants.BUNDLE_SEARCH_QUERY, query);
        }
        startActivity(intent);

        // Clear search text
        etSearch.setText("");
    }

    private void loadHomeData() {
        if (isLoadingData) return;

        isLoadingData = true;
        swipeRefresh.setRefreshing(true);

        loadCategories();
        loadProducts();
    }

    private void loadCategories() {
        Call<StandardResponse<List<Category>>> call = ApiClient.getProductService().getCategories();
        call.enqueue(new Callback<StandardResponse<List<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Category>>> call,
                                   @NonNull Response<StandardResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {

                    categories.clear();
                    categories.addAll(response.body().getData());
                    categoriesAdapter.notifyDataSetChanged();

                    Log.d(TAG, "Categories loaded: " + categories.size());
                } else {
                    Log.e(TAG, "Failed to load categories: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Category>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Error loading categories", t);
            }
        });
    }

    private void loadProducts() {
        // Load recent products
        loadRecentProducts();

        // Load nearby products if location is available
        if (currentLatitude != null && currentLongitude != null) {
            loadNearbyProducts();
        }

        // Load popular products
        loadPopularProducts();
    }

    private void loadRecentProducts() {
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getProducts(0, 10, null, null, null, null,
                        Constants.SORT_NEWEST, null);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleProductsResponse(response, "Recent Products",
                        HomeSection.SectionType.RECENT_PRODUCTS);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Error loading recent products", t);
                checkLoadingComplete();
            }
        });
    }

    private void loadNearbyProducts() {
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getNearbyProducts(currentLatitude, currentLongitude,
                        (int) Constants.DEFAULT_LOCATION_RADIUS, 0, 10, null);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleProductsResponse(response, "Nearby Products",
                        HomeSection.SectionType.NEARBY_PRODUCTS);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Error loading nearby products", t);
                checkLoadingComplete();
            }
        });
    }

    private void loadPopularProducts() {
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getProducts(0, 10, null, null, null, null,
                        Constants.SORT_POPULARITY, null);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleProductsResponse(response, "Popular Products",
                        HomeSection.SectionType.POPULAR_PRODUCTS);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Error loading popular products", t);
                checkLoadingComplete();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleProductsResponse(Response<StandardResponse<Map<String, Object>>> response,
                                        String sectionTitle, HomeSection.SectionType sectionType) {
        try {
            if (response.isSuccessful() && response.body() != null
                    && response.body().isSuccess()) {

                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null && !productMaps.isEmpty()) {
                    List<Product> products = new ArrayList<>();
                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            products.add(product);
                        }
                    }

                    if (!products.isEmpty()) {
                        HomeSection section = new HomeSection(sectionTitle, sectionType, products);
                        homeSections.add(section);
                        sectionsAdapter.notifyItemInserted(homeSections.size() - 1);
                    }
                }

                Log.d(TAG, sectionTitle + " loaded: " + (productMaps != null ? productMaps.size() : 0));
            } else {
                Log.e(TAG, "Failed to load " + sectionTitle + ": " + response.message());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing " + sectionTitle, e);
        } finally {
            checkLoadingComplete();
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
            if (priceObj != null) {
                if (priceObj instanceof Number) {
                    product.setPrice(java.math.BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                }
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

    private void checkLoadingComplete() {
        // Simple check - if we have at least one section, consider loading complete
        if (!homeSections.isEmpty() || categories.isEmpty()) {
            isLoadingData = false;
            swipeRefresh.setRefreshing(false);
        }
    }

    private void getCurrentLocation() {
        locationManager.requestLocation();
    }

    public void refreshData() {
        homeSections.clear();
        sectionsAdapter.notifyDataSetChanged();

        categories.clear();
        categoriesAdapter.notifyDataSetChanged();

        loadHomeData();
    }

    public boolean onBackPressed() {
        // Handle back press if needed
        return false;
    }

    // LocationManager.LocationCallback implementation
    @Override
    public void onLocationReceived(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        // Save location to preferences
        prefsManager.saveLocation(currentLatitude, currentLongitude, null);

        // Load nearby products now that we have location
        if (!isLoadingData) {
            loadNearbyProducts();
        }

        Log.d(TAG, "Location received: " + currentLatitude + ", " + currentLongitude);
    }

    @Override
    public void onLocationError(String error) {
        Log.e(TAG, "Location error: " + error);
        // Continue without location-based features
    }

    // CategoriesAdapter.OnCategoryClickListener implementation
    @Override
    public void onCategoryClick(Category category) {
        Intent intent = new Intent(requireContext(), CategoryProductsActivity.class);
        intent.putExtra(Constants.BUNDLE_CATEGORY_ID, category.getId());
        intent.putExtra("categoryName", category.getName());
        startActivity(intent);
    }

    // ProductSectionAdapter.OnProductClickListener implementation
    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public void onSeeAllClick(HomeSection.SectionType type) {
        // Navigate to search/filter page based on section type
        Intent intent = new Intent(requireContext(), SearchActivity.class);

        switch (type) {
            case RECENT_PRODUCTS:
                intent.putExtra("sortBy", Constants.SORT_NEWEST);
                break;
            case NEARBY_PRODUCTS:
                intent.putExtra("nearbyOnly", true);
                break;
            case POPULAR_PRODUCTS:
                intent.putExtra("sortBy", Constants.SORT_POPULARITY);
                break;
            case RECOMMENDED_PRODUCTS:
                intent.putExtra("recommended", true);
                break;
        }

        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.cleanup();
        }
    }
}