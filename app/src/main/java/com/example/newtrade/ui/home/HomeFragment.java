// app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
package com.example.newtrade.ui.home;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.search.SearchActivity;
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
            tvWelcome.setText("Welcome to TradeUp!");
        }
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView (horizontal)
        categoriesAdapter = new CategoriesAdapter(categories, this);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoriesAdapter);

        // Sections RecyclerView (vertical)
        sectionsAdapter = new HomeSectionsAdapter(homeSections, this);
        rvContent.setLayoutManager(new LinearLayoutManager(getContext()));
        rvContent.setAdapter(sectionsAdapter);
    }

    private void setupListeners() {
        // Search field click
        etSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });

        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        // Swipe refresh
        swipeRefresh.setOnRefreshListener(this::refreshHomeData);
    }

    private void getCurrentLocation() {
        if (locationManager.hasLocationPermission()) {
            locationManager.getCurrentLocation();
        } else {
            // Use last known location
            currentLatitude = prefsManager.getLastLatitude();
            currentLongitude = prefsManager.getLastLongitude();
        }
    }

    @Override
    public void onLocationReceived(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        prefsManager.saveLastLocation(currentLatitude, currentLongitude);

        // Reload nearby products with new location
        loadNearbyProducts();
    }

    @Override
    public void onLocationError(String error) {
        Log.w(TAG, "Location error: " + error);
        // Use last known location
        currentLatitude = prefsManager.getLastLatitude();
        currentLongitude = prefsManager.getLastLongitude();
    }

    private void loadHomeData() {
        loadCategories();
        loadRecommendedProducts();
        loadPopularProducts();
        loadNearbyProducts();
    }

    private void refreshHomeData() {
        homeSections.clear();
        sectionsAdapter.notifyDataSetChanged();
        loadHomeData();
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
                                categoriesAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<List<Category>>> call, Throwable t) {
                        Log.e(TAG, "Failed to load categories", t);
                    }
                });
    }

    private void loadRecommendedProducts() {
        // Load personalized recommendations based on user history
        ApiClient.getProductService().getProducts(
                0, 10, // page, size
                null, null, null, null, // filters
                currentLatitude, currentLongitude, 50, // location with 50km radius
                "createdAt", "desc" // sort by newest
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                handleProductsResponse(response, "Recommended for You", HomeSection.SectionType.RECOMMENDED);
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Failed to load recommended products", t);
            }
        });
    }

    private void loadPopularProducts() {
        // Load popular products (sorted by view count)
        ApiClient.getProductService().getProducts(
                0, 10, // page, size
                null, null, null, null, // filters
                null, null, null, // no location filter for popular
                "viewCount", "desc" // sort by most viewed
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                handleProductsResponse(response, "Popular Products", HomeSection.SectionType.POPULAR);
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Failed to load popular products", t);
            }
        });
    }

    private void loadNearbyProducts() {
        if (currentLatitude == null || currentLongitude == null) return;

        // Load nearby products
        ApiClient.getProductService().getProducts(
                0, 10, // page, size
                null, null, null, null, // filters
                currentLatitude, currentLongitude, 10, // location with 10km radius
                "createdAt", "desc" // sort by newest
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                handleProductsResponse(response, "Near You", HomeSection.SectionType.NEARBY);
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Failed to load nearby products", t);
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void handleProductsResponse(Response<StandardResponse<Map<String, Object>>> response,
                                        String title, HomeSection.SectionType type) {
        if (response.isSuccessful() && response.body() != null) {
            StandardResponse<Map<String, Object>> apiResponse = response.body();

            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                List<Product> products = parseProductsFromResponse(apiResponse.getData());

                if (!products.isEmpty()) {
                    HomeSection section = new HomeSection(title, products, type);

                    // Remove existing section of same type and add new one
                    removeExistingSection(type);
                    homeSections.add(section);
                    sectionsAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private List<Product> parseProductsFromResponse(Map<String, Object> data) {
        List<Product> products = new ArrayList<>();

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productsData = (List<Map<String, Object>>) data.get("content");

            if (productsData != null) {
                for (Map<String, Object> productData : productsData) {
                    Product product = parseProductFromMap(productData);
                    products.add(product);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing products", e);
        }

        return products;
    }

    private Product parseProductFromMap(Map<String, Object> data) {
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

    private void removeExistingSection(HomeSection.SectionType type) {
        for (int i = 0; i < homeSections.size(); i++) {
            if (homeSections.get(i).getType() == type) {
                homeSections.remove(i);
                break;
            }
        }
    }

    @Override
    public void onCategoryClick(Category category) {
        Intent intent = new Intent(getContext(), SearchActivity.class);
        intent.putExtra("category_id", category.getId());
        intent.putExtra("category_name", category.getName());
        startActivity(intent);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    @Override
    public void onSeeAllClick(HomeSection.SectionType type) {
        Intent intent = new Intent(getContext(), SearchActivity.class);

        switch (type) {
            case POPULAR:
                intent.putExtra("sort_by", "viewCount");
                intent.putExtra("sort_direction", "desc");
                intent.putExtra("title", "Popular Products");
                break;
            case NEARBY:
                if (currentLatitude != null && currentLongitude != null) {
                    intent.putExtra("latitude", currentLatitude);
                    intent.putExtra("longitude", currentLongitude);
                    intent.putExtra("radius", 10);
                    intent.putExtra("title", "Near You");
                }
                break;
            case RECOMMENDED:
            default:
                intent.putExtra("title", "Recommended");
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

    // Home section data class
    public static class HomeSection {
        public enum SectionType {
            RECOMMENDED, POPULAR, NEARBY, CATEGORY
        }

        private String title;
        private List<Product> products;
        private SectionType type;

        public HomeSection(String title, List<Product> products, SectionType type) {
            this.title = title;
            this.products = products;
            this.type = type;
        }

        public String getTitle() { return title; }
        public List<Product> getProducts() { return products; }
        public SectionType getType() { return type; }
    }
}