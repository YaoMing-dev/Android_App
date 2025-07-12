// app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
package com.example.newtrade.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.example.newtrade.R;
import com.example.newtrade.adapters.CategoryAdapter;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.adapters.RecommendationSectionAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.engine.RecommendationEngine;
import com.example.newtrade.interfaces.RecommendationCallback;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.RecommendationSection;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.CategoryProductsActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.search.AllProductsActivity;
import com.example.newtrade.utils.LocationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.UserBehaviorTracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // ===== UI COMPONENTS =====
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvCategories;
    private RecyclerView rvRecentProducts;
    private RecyclerView rvRecommendations;
    private TextView tvViewAllCategories;
    private TextView tvViewAllProducts;
    private TextView tvWelcomeMessage;
    private TextView tvLocationStatus;
    private FloatingActionButton fabQuickAdd;
    private View emptyView;
    private View llLoadingRecommendations;

    // ===== ADAPTERS =====
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private RecommendationSectionAdapter recommendationAdapter;

    // ===== LOCATION TRACKING =====
    private Double userLatitude = null;
    private Double userLongitude = null;
    private boolean isLocationLoaded = false;
    private boolean isLocationRequested = false;

    // ===== RECOMMENDATION SERVICES =====
    private RecommendationEngine recommendationEngine;
    private UserBehaviorTracker behaviorTracker;
    private SharedPrefsManager prefsManager;

    // ===== DATA =====
    private final List<Category> categories = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();
    private final List<RecommendationSection> recommendationSections = new ArrayList<>();

    // ===== LOADING STATES =====
    private boolean isLoadingPersonalized = false;
    private boolean isLoadingPopular = false;
    private boolean isLoadingNearby = false;
    private boolean isLoadingCategoryRecommendations = false;
    private int completedRecommendationLoads = 0;
    private static final int TOTAL_RECOMMENDATION_LOADS = 4;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize services first
        initRecommendationServices();

        // Initialize UI
        initViews(view);
        setupRecyclerViews();
        setupListeners();

        // Load data
        loadData();

        Log.d(TAG, "✅ HomeFragment created with enhanced location prioritization");
    }

    // ===== INITIALIZATION =====
    private void initRecommendationServices() {
        try {
            prefsManager = SharedPrefsManager.getInstance(requireContext());
            behaviorTracker = UserBehaviorTracker.getInstance(requireContext());
            recommendationEngine = RecommendationEngine.getInstance(requireContext());

            // Track app launch
            behaviorTracker.trackAppLaunch();

            Log.d(TAG, "✅ Recommendation services initialized");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing recommendation services", e);
        }
    }

    private void initViews(View view) {
        try {
            // Core UI components
            swipeRefresh = view.findViewById(R.id.swipe_refresh);
            rvCategories = view.findViewById(R.id.rv_categories);
            rvRecentProducts = view.findViewById(R.id.rv_recommendations);
            tvViewAllCategories = view.findViewById(R.id.tv_view_all_categories);
            tvViewAllProducts = view.findViewById(R.id.tv_view_all_products);
            fabQuickAdd = view.findViewById(R.id.fab_quick_add);
            emptyView = view.findViewById(R.id.empty_view);

            // Optional components (may not exist in layout)
            rvRecommendations = view.findViewById(R.id.rv_recommendations);
            llLoadingRecommendations = view.findViewById(R.id.ll_loading_recommendations);
            tvWelcomeMessage = view.findViewById(R.id.tv_welcome_message);
            tvLocationStatus = view.findViewById(R.id.tv_location_status);

            // Update welcome message
            updateWelcomeMessage();

            // Update location status
            updateLocationStatus("Detecting your location...");

            debugApiConnection();

            Log.d(TAG, "✅ HomeFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some HomeFragment views not found: " + e.getMessage());
        }
    }

    private void updateWelcomeMessage() {
        if (tvWelcomeMessage != null && prefsManager != null) {
            if (prefsManager.isLoggedIn()) {
                String userName = prefsManager.getUserName();
                if (userName != null && !userName.isEmpty()) {
                    tvWelcomeMessage.setText("Welcome back, " + userName + "! 👋");
                } else {
                    tvWelcomeMessage.setText("Welcome back! 👋");
                }
            } else {
                tvWelcomeMessage.setText("Discover amazing products near you! 🌟");
            }
        }
    }

    private void updateLocationStatus(String message) {
        if (tvLocationStatus != null) {
            tvLocationStatus.setText(message);
            tvLocationStatus.setVisibility(View.VISIBLE);
        }
    }

    private void hideLocationStatus() {
        if (tvLocationStatus != null) {
            tvLocationStatus.setVisibility(View.GONE);
        }
    }

    // ===== RECYCLERVIEW SETUP =====
    private void setupRecyclerViews() {
        try {
            // Categories RecyclerView with behavior tracking
            categoryAdapter = new CategoryAdapter(categories, category -> {
                Log.d(TAG, "📱 Category clicked: " + category.getName() + " (ID: " + category.getId() + ")");

                // Track category browsing
                if (behaviorTracker != null) {
                    behaviorTracker.trackCategoryBrowse(category.getId(), category.getName());
                }

                Intent intent = new Intent(getActivity(), CategoryProductsActivity.class);
                intent.putExtra("category_id", category.getId());
                intent.putExtra("category_name", category.getName());
                startActivity(intent);
            });

            rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvCategories.setAdapter(categoryAdapter);

            // Products RecyclerView with behavior tracking
            productAdapter = new ProductAdapter(products, product -> {
                Log.d(TAG, "📱 Product clicked: " + product.getTitle() + " (ID: " + product.getId() +
                        (product.getDistanceFromUser() != null ? ", Distance: " + product.getFormattedDistance() : "") + ")");

                // Track product view
                if (behaviorTracker != null) {
                    behaviorTracker.trackProductView(product.getId(), product.getCategoryName(), null);
                }

                Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                intent.putExtra("product_title", product.getTitle());
                intent.putExtra("product_price", product.getFormattedPrice());
                startActivity(intent);
            });

            rvRecentProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
            rvRecentProducts.setAdapter(productAdapter);

            // Recommendations RecyclerView (if exists)
            if (rvRecommendations != null) {
                setupRecommendationsRecyclerView();
            }

            Log.d(TAG, "✅ HomeFragment RecyclerViews setup with location-aware adapters");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up RecyclerViews", e);
        }
    }

    private void setupRecommendationsRecyclerView() {
        try {
            recommendationAdapter = new RecommendationSectionAdapter(
                    recommendationSections,
                    this::onRecommendationProductClick,
                    this::onRecommendationSectionHeaderClick
            );

            LinearLayoutManager recommendationLayoutManager = new LinearLayoutManager(getContext());
            rvRecommendations.setLayoutManager(recommendationLayoutManager);
            rvRecommendations.setAdapter(recommendationAdapter);

            // Performance optimization
            rvRecommendations.setHasFixedSize(true);
            rvRecommendations.setNestedScrollingEnabled(false);

            Log.d(TAG, "✅ Recommendations RecyclerView setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up recommendations RecyclerView", e);
        }
    }

    // ===== RECOMMENDATION CALLBACKS =====
    private void onRecommendationProductClick(Product product, String recommendationType) {
        if (product != null) {
            Log.d(TAG, "📱 Recommendation product clicked: " + product.getTitle() +
                    " from " + recommendationType +
                    (product.getDistanceFromUser() != null ? " (" + product.getFormattedDistance() + ")" : ""));

            // Track both product view and recommendation click
            if (behaviorTracker != null) {
                behaviorTracker.trackProductView(product.getId(), product.getCategoryName(), null);
                behaviorTracker.trackRecommendationClick(product.getId(), recommendationType);
            }

            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_title", product.getTitle());
            intent.putExtra("product_price", product.getFormattedPrice());
            intent.putExtra("recommendation_type", recommendationType);
            startActivity(intent);
        }
    }

    private void onRecommendationSectionHeaderClick(String sectionTitle, String sectionType) {
        Log.d(TAG, "📱 Recommendation section clicked: " + sectionTitle + " (" + sectionType + ")");

        switch (sectionType) {
            case "popular":
            case "personalized":
            case "nearby":
                Intent intent = new Intent(getActivity(), AllProductsActivity.class);
                intent.putExtra("filter_type", sectionType);
                intent.putExtra("section_title", sectionTitle);
                if (userLatitude != null && userLongitude != null) {
                    intent.putExtra("user_latitude", userLatitude);
                    intent.putExtra("user_longitude", userLongitude);
                }
                startActivity(intent);
                break;

            case "category":
                navigateToAllCategories();
                break;

            default:
                navigateToAllProducts();
                break;
        }
    }

    // ===== EVENT LISTENERS =====
    private void setupListeners() {
        try {
            if (swipeRefresh != null) {
                swipeRefresh.setOnRefreshListener(this::loadData);
            }

            if (tvViewAllCategories != null) {
                tvViewAllCategories.setOnClickListener(v -> {
                    Log.d(TAG, "View All Categories clicked");
                    navigateToAllCategories();
                });
            }

            if (tvViewAllProducts != null) {
                tvViewAllProducts.setOnClickListener(v -> {
                    Log.d(TAG, "View All Products clicked");
                    navigateToAllProducts();
                });
            }

            if (fabQuickAdd != null) {
                fabQuickAdd.setOnClickListener(v -> {
                    navigateToAddProduct();
                });
            }

            Log.d(TAG, "✅ HomeFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }

    // ===== DATA LOADING =====
    private void loadData() {
        try {
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(true);
            }

            // Show recommendations loading and reset state
            showRecommendationsLoading();
            resetRecommendationLoadingState();

            // Start location detection
            getCurrentLocationForFeed();

            // Load basic data
            loadCategoriesFromBackend();
            loadProductsFromBackend();

            // Load recommendations
            loadAllRecommendations();

            Log.d(TAG, "✅ Data loading started with location prioritization");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting data load", e);
            hideRecommendationsLoading();
        }
    }

    // ===== LOCATION HANDLING =====
    private void getCurrentLocationForFeed() {
        if (isLocationRequested) {
            Log.d(TAG, "Location already requested, skipping");
            return;
        }

        isLocationRequested = true;
        updateLocationStatus("Getting your location...");

        LocationUtils.getCurrentLocation(requireContext(), new LocationUtils.LocationCallback() {
            @Override
            public void onLocationSuccess(double latitude, double longitude) {
                userLatitude = latitude;
                userLongitude = longitude;
                isLocationLoaded = true;

                Log.d(TAG, "📍 User location detected: " + latitude + ", " + longitude);
                updateLocationStatus("Location: " + LocationUtils.formatDistance(0) + " (You are here)");

                // Re-calculate distances for existing products
                reprocessProductsWithLocation();

                // Load nearby recommendations
                loadNearbyRecommendations();

                // Hide location status after 3 seconds
                if (getView() != null) {
                    getView().postDelayed(() -> hideLocationStatus(), 3000);
                }
            }

            @Override
            public void onLocationError(String error) {
                Log.w(TAG, "Location error: " + error);
                updateLocationStatus("📍 Location unavailable - showing all products");

                // Complete recommendation load even without location
                completeRecommendationLoad();

                // Hide location status after 5 seconds
                if (getView() != null) {
                    getView().postDelayed(() -> hideLocationStatus(), 5000);
                }
            }
        });
    }

    private void reprocessProductsWithLocation() {
        if (userLatitude != null && userLongitude != null && !products.isEmpty()) {
            // Calculate distances for all products
            LocationUtils.calculateDistancesForProducts(userLatitude, userLongitude, products);

            // Sort by distance (nearest first)
            LocationUtils.sortProductsByDistance(products);

            // Log location stats
            LocationUtils.logLocationStats(products);

            // Update UI
            if (productAdapter != null) {
                productAdapter.notifyDataSetChanged();
            }

            Log.d(TAG, "✅ Products reprocessed with user location - prioritized by distance");
        }
    }

    // ===== BACKEND DATA LOADING =====
    private void loadCategoriesFromBackend() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                   Response<StandardResponse<List<Map<String, Object>>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Map<String, Object>> categoryList = response.body().getData();

                        categories.clear();
                        if (categoryList != null && !categoryList.isEmpty()) {
                            for (Map<String, Object> categoryData : categoryList) {
                                try {
                                    Category category = new Category();
                                    category.setId(((Number) categoryData.get("id")).longValue());
                                    category.setName((String) categoryData.get("name"));
                                    category.setDescription((String) categoryData.get("description"));
                                    category.setIcon((String) categoryData.get("icon"));
                                    category.setActive(Boolean.TRUE.equals(categoryData.get("active")));
                                    categories.add(category);

                                    Log.d(TAG, "✅ Added category: " + category.getName() + " (ID: " + category.getId() + ")");
                                } catch (Exception e) {
                                    Log.w(TAG, "❌ Error parsing category: " + e.getMessage());
                                }
                            }
                        }

                        if (categoryAdapter != null) {
                            categoryAdapter.notifyDataSetChanged();
                        }

                        Log.d(TAG, "✅ Loaded " + categories.size() + " categories");
                    } else {
                        Log.e(TAG, "❌ Categories API failed");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing categories", e);
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                Log.e(TAG, "❌ Categories network error", t);
            }
        });
    }

    private void loadProductsFromBackend() {
        ApiClient.getApiService().getProducts().enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                try {
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Map<String, Object> data = apiResponse.getData();
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            products.clear();
                            if (productList != null && !productList.isEmpty()) {
                                for (Map<String, Object> productData : productList) {
                                    try {
                                        Product product = parseProductFromData(productData);
                                        if (product != null) {
                                            products.add(product);
                                        }
                                    } catch (Exception e) {
                                        Log.w(TAG, "❌ Error parsing product: " + e.getMessage());
                                    }
                                }
                            }

                            // Apply location-based prioritization
                            prioritizeNearbyProducts();

                            if (productAdapter != null) {
                                productAdapter.notifyDataSetChanged();
                            }

                            Log.d(TAG, "✅ Loaded " + products.size() + " products with location prioritization");
                        } else {
                            Log.e(TAG, "❌ Products API failed");
                        }
                    } else {
                        Log.e(TAG, "❌ Products response not successful");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing products", e);
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                Log.e(TAG, "❌ Products network error", t);
            }
        });
    }

    private Product parseProductFromData(Map<String, Object> productData) {
        try {
            Product product = new Product();

            // Basic fields
            product.setId(((Number) productData.get("id")).longValue());
            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));
            product.setLocation((String) productData.get("location"));

            // Location coordinates
            if (productData.get("latitude") instanceof Number) {
                product.setLatitude(((Number) productData.get("latitude")).doubleValue());
            }
            if (productData.get("longitude") instanceof Number) {
                product.setLongitude(((Number) productData.get("longitude")).doubleValue());
            }

            // Price handling
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }

            // Image handling
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                List<String> imageUrls = (List<String>) imageUrlsObj;
                product.setImageUrls(imageUrls);
                if (!imageUrls.isEmpty()) {
                    product.setImageUrl(imageUrls.get(0));
                }
            } else if (imageUrlsObj instanceof String && !((String) imageUrlsObj).isEmpty()) {
                product.setImageUrl((String) imageUrlsObj);
            }

            // Condition handling
            String condition = (String) productData.get("condition");
            if (condition != null) {
                try {
                    product.setCondition(Product.ProductCondition.valueOf(condition));
                } catch (IllegalArgumentException e) {
                    product.setCondition(Product.ProductCondition.GOOD);
                }
            }

            // Calculate distance immediately if user location is available
            if (userLatitude != null && userLongitude != null && product.hasLocation()) {
                double distance = LocationUtils.calculateDistance(
                        userLatitude, userLongitude,
                        product.getLatitude(), product.getLongitude()
                );
                product.setDistanceFromUser(distance);
            }

            return product;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating product from data", e);
            return null;
        }
    }

    private void prioritizeNearbyProducts() {
        if (userLatitude != null && userLongitude != null && !products.isEmpty()) {
            // Calculate distances for all products if not already done
            LocationUtils.calculateDistancesForProducts(userLatitude, userLongitude, products);

            // Sort by distance (nearest first)
            LocationUtils.sortProductsByDistance(products);

            // Log location statistics
            LocationUtils.logLocationStats(products);

            Log.d(TAG, "✅ Products prioritized by distance from user location");
        } else {
            Log.d(TAG, "⚠️ Cannot prioritize products - missing location or products");
        }
    }

    // ===== RECOMMENDATIONS LOADING =====
    private void loadAllRecommendations() {
        if (recommendationEngine != null) {
            loadPersonalizedRecommendations();
            loadPopularRecommendations();
            loadCategoryBasedRecommendations();
            // loadNearbyRecommendations() called after getting location
        } else {
            Log.w(TAG, "⚠️ RecommendationEngine not initialized, skipping recommendations");
            hideRecommendationsLoading();
        }
    }

    private void loadPersonalizedRecommendations() {
        if (isLoadingPersonalized || recommendationEngine == null) return;

        isLoadingPersonalized = true;
        Log.d(TAG, "🔄 Loading personalized recommendations...");

        recommendationEngine.getPersonalizedRecommendations(new RecommendationCallback() {
            @Override
            public void onSuccess(List<Product> products, String title) {
                try {
                    if (products != null && !products.isEmpty()) {
                        // Calculate distances for recommendation products
                        if (userLatitude != null && userLongitude != null) {
                            LocationUtils.calculateDistancesForProducts(userLatitude, userLongitude, products);
                        }

                        RecommendationSection section = new RecommendationSection(
                                title,
                                products,
                                "personalized",
                                "✨ Just for you",
                                true
                        );

                        addRecommendationSection(section, 0);
                        Log.d(TAG, "✅ Personalized recommendations loaded: " + products.size() + " items");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error processing personalized recommendations", e);
                }

                isLoadingPersonalized = false;
                completeRecommendationLoad();
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "⚠️ Personalized recommendations failed: " + error);
                isLoadingPersonalized = false;
                completeRecommendationLoad();
            }
        });
    }

    private void loadPopularRecommendations() {
        if (isLoadingPopular || recommendationEngine == null) return;

        isLoadingPopular = true;
        Log.d(TAG, "🔄 Loading popular recommendations...");

        recommendationEngine.getPopularRecommendations(new RecommendationCallback() {
            @Override
            public void onSuccess(List<Product> products, String title) {
                try {
                    if (products != null && !products.isEmpty()) {
                        // Calculate distances for recommendation products
                        if (userLatitude != null && userLongitude != null) {
                            LocationUtils.calculateDistancesForProducts(userLatitude, userLongitude, products);
                        }

                        RecommendationSection section = new RecommendationSection(
                                title,
                                products,
                                "popular",
                                "🔥 Trending right now",
                                true
                        );

                        addRecommendationSection(section, -1);
                        Log.d(TAG, "✅ Popular recommendations loaded: " + products.size() + " items");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error processing popular recommendations", e);
                }

                isLoadingPopular = false;
                completeRecommendationLoad();
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "⚠️ Popular recommendations failed: " + error);
                isLoadingPopular = false;
                completeRecommendationLoad();
            }
        });
    }

    private void loadNearbyRecommendations() {
        if (isLoadingNearby || userLatitude == null || userLongitude == null || recommendationEngine == null) {
            completeRecommendationLoad();
            return;
        }

        isLoadingNearby = true;
        Log.d(TAG, "🔄 Loading nearby recommendations...");

        recommendationEngine.getNearbyRecommendations(userLatitude, userLongitude, new RecommendationCallback() {
            @Override
            public void onSuccess(List<Product> products, String title) {
                try {
                    if (products != null && !products.isEmpty()) {
                        // Distances should already be calculated by the recommendation engine
                        // But ensure they are calculated
                        LocationUtils.calculateDistancesForProducts(userLatitude, userLongitude, products);

                        RecommendationSection section = new RecommendationSection(
                                title,
                                products,
                                "nearby",
                                "📍 Near your location",
                                true
                        );

                        addRecommendationSection(section, 1);
                        Log.d(TAG, "✅ Nearby recommendations loaded: " + products.size() + " items");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error processing nearby recommendations", e);
                }

                isLoadingNearby = false;
                completeRecommendationLoad();
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "⚠️ Nearby recommendations failed: " + error);
                isLoadingNearby = false;
                completeRecommendationLoad();
            }
        });
    }

    private void loadCategoryBasedRecommendations() {
        if (isLoadingCategoryRecommendations || recommendationEngine == null) return;

        isLoadingCategoryRecommendations = true;
        Log.d(TAG, "🔄 Loading category-based recommendations...");

        // Load for Electronics category as example
        recommendationEngine.getCategoryRecommendations(1L, "Electronics", new RecommendationCallback() {
            @Override
            public void onSuccess(List<Product> products, String title) {
                try {
                    if (products != null && !products.isEmpty()) {
                        // Calculate distances for recommendation products
                        if (userLatitude != null && userLongitude != null) {
                            LocationUtils.calculateDistancesForProducts(userLatitude, userLongitude, products);
                        }

                        RecommendationSection section = new RecommendationSection(
                                "Electronics",
                                products,
                                "category",
                                "📱 Electronics Collection",
                                true
                        );

                        addRecommendationSection(section, -1);
                        Log.d(TAG, "✅ Category recommendations loaded: " + products.size() + " items");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error processing category recommendations", e);
                }

                isLoadingCategoryRecommendations = false;
                completeRecommendationLoad();
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "⚠️ Category recommendations failed: " + error);
                isLoadingCategoryRecommendations = false;
                completeRecommendationLoad();
            }
        });
    }

    // ===== RECOMMENDATION STATE MANAGEMENT =====
    private void resetRecommendationLoadingState() {
        completedRecommendationLoads = 0;
        isLoadingPersonalized = false;
        isLoadingPopular = false;
        isLoadingNearby = false;
        isLoadingCategoryRecommendations = false;
        recommendationSections.clear();
    }

    private void addRecommendationSection(RecommendationSection section, int position) {
        try {
            if (position >= 0 && position < recommendationSections.size()) {
                recommendationSections.add(position, section);
            } else {
                recommendationSections.add(section);
            }

            if (recommendationAdapter != null) {
                recommendationAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error adding recommendation section", e);
        }
    }

    private void completeRecommendationLoad() {
        completedRecommendationLoads++;
        Log.d(TAG, "✅ Recommendation load completed: " + completedRecommendationLoads + "/" + TOTAL_RECOMMENDATION_LOADS);

        if (completedRecommendationLoads >= TOTAL_RECOMMENDATION_LOADS) {
            hideRecommendationsLoading();

            if (recommendationSections.isEmpty()) {
                Log.d(TAG, "⚠️ No recommendations available");
            } else {
                Log.d(TAG, "✅ All recommendations loaded: " + recommendationSections.size() + " sections");
            }
        }
    }

    // ===== UI STATE MANAGEMENT =====
    private void showRecommendationsLoading() {
        if (llLoadingRecommendations != null) {
            llLoadingRecommendations.setVisibility(View.VISIBLE);
        }
        if (rvRecommendations != null) {
            rvRecommendations.setVisibility(View.GONE);
        }
    }

    private void hideRecommendationsLoading() {
        if (llLoadingRecommendations != null) {
            llLoadingRecommendations.setVisibility(View.GONE);
        }
        if (rvRecommendations != null) {
            rvRecommendations.setVisibility(View.VISIBLE);
        }
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }
    }

    // ===== NAVIGATION =====
    private void navigateToAllCategories() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_home_to_search);
            Log.d(TAG, "✅ Navigated to all categories");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to categories", e);
            Toast.makeText(getContext(), "Unable to navigate", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAllProducts() {
        try {
            Intent intent = new Intent(getActivity(), AllProductsActivity.class);
            // Pass user location for sorting
            if (userLatitude != null && userLongitude != null) {
                intent.putExtra("user_latitude", userLatitude);
                intent.putExtra("user_longitude", userLongitude);
            }
            startActivity(intent);
            Log.d(TAG, "✅ Navigated to all products with location");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to all products", e);
            Toast.makeText(getContext(), "Unable to navigate", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAddProduct() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.nav_add_product);
            Log.d(TAG, "✅ Navigated to add product");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to add product", e);
            Toast.makeText(getContext(), "Unable to navigate", Toast.LENGTH_SHORT).show();
        }
    }

    // ===== UTILITY =====
    private void debugApiConnection() {
        if (ApiClient.getApiService() != null) {
            ApiClient.getApiService().healthCheck().enqueue(new Callback<StandardResponse<String>>() {
                @Override
                public void onResponse(Call<StandardResponse<String>> call, Response<StandardResponse<String>> response) {
                    Log.d(TAG, response.isSuccessful() ?
                            "✅ Backend reachable" : "❌ Backend error: " + response.code());
                }

                @Override
                public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                    Log.e(TAG, "❌ Backend unreachable: " + t.getMessage());
                }
            });
        } else {
            Log.w(TAG, "⚠️ ApiClient not initialized");
        }
    }

    // ===== LIFECYCLE =====
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 HomeFragment resumed");

        // Update welcome message in case user logged in/out
        updateWelcomeMessage();

        // Reset location request flag to allow re-requesting if needed
        isLocationRequested = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Location permission granted");
                isLocationRequested = false; // Allow re-requesting
                getCurrentLocationForFeed();
            } else {
                Log.d(TAG, "❌ Location permission denied");
                updateLocationStatus("📍 Location permission denied - showing all products");
                completeRecommendationLoad();
            }
        }
    }
}