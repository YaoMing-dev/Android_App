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
import android.location.Location;
import androidx.core.app.ActivityCompat;
import com.example.newtrade.utils.LocationUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.example.newtrade.R;
import com.example.newtrade.adapters.CategoryAdapter;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.CategoryProductsActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.search.AllProductsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
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

    // UI Components
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvCategories;
    private RecyclerView rvRecentProducts;
    private TextView tvViewAllCategories;
    private TextView tvViewAllProducts;
    private FloatingActionButton fabQuickAdd;
    private View emptyView;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;

    private Double userLatitude = null;
    private Double userLongitude = null;
    private FusedLocationProviderClient fusedLocationClient;

    // Data
    private final List<Category> categories = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupListeners();
        loadData();

        Log.d(TAG, "HomeFragment created successfully");
    }

    private void initViews(View view) {
        try {
            swipeRefresh = view.findViewById(R.id.swipe_refresh);
            rvCategories = view.findViewById(R.id.rv_categories);
            rvRecentProducts = view.findViewById(R.id.rv_recent_products);
            tvViewAllCategories = view.findViewById(R.id.tv_view_all_categories);
            tvViewAllProducts = view.findViewById(R.id.tv_view_all_products);
            fabQuickAdd = view.findViewById(R.id.fab_quick_add);
            emptyView = view.findViewById(R.id.empty_view);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

            Log.d(TAG, "✅ HomeFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some HomeFragment views not found: " + e.getMessage());
        }
    }

    private void setupRecyclerViews() {
        try {
            debugApiConnection();

            // Categories RecyclerView
            categoryAdapter = new CategoryAdapter(categories, category -> {
                Log.d(TAG, "📱 Category clicked: " + category.getName() + " (ID: " + category.getId() + ")");
                Intent intent = new Intent(getActivity(), CategoryProductsActivity.class);
                intent.putExtra("category_id", category.getId());
                intent.putExtra("category_name", category.getName());
                startActivity(intent);
            });

            rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvCategories.setAdapter(categoryAdapter);

            // Products RecyclerView
            productAdapter = new ProductAdapter(products, product -> {
                Log.d(TAG, "📱 Product clicked: " + product.getTitle() + " (ID: " + product.getId() + ")");
                Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                intent.putExtra("product_title", product.getTitle());
                intent.putExtra("product_price", product.getFormattedPrice());
                startActivity(intent);
            });

            rvRecentProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
            rvRecentProducts.setAdapter(productAdapter);

            Log.d(TAG, "✅ HomeFragment RecyclerViews setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up RecyclerViews", e);
        }
    }

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

    private void loadData() {
        try {
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(true);
            }
            getCurrentLocationForFeed();
            loadCategoriesFromBackend();
            loadProductsFromBackend();

            Log.d(TAG, "✅ Data loading started - REAL DATA ONLY");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting data load", e);
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
        }
    }
    private void getCurrentLocationForFeed() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            try {
                fusedLocationClient.getCurrentLocation(
                                com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                                null
                        )
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                userLatitude = location.getLatitude();
                                userLongitude = location.getLongitude();
                                Log.d(TAG, "📍 User location for feed: " + userLatitude + ", " + userLongitude);

                                // Re-sort products if already loaded
                                if (!products.isEmpty()) {
                                    prioritizeNearbyProducts();
                                    if (productAdapter != null) {
                                        productAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Could not get location for feed prioritization", e);
                        });
            } catch (SecurityException e) {
                Log.e(TAG, "Location permission denied", e);
            }
        } else {
            Log.d(TAG, "Location permission not granted, skipping nearby prioritization");
        }
    }

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

                                    Log.d(TAG, "✅ Added REAL category: " + category.getName() + " (ID: " + category.getId() + ")");
                                } catch (Exception e) {
                                    Log.w(TAG, "❌ Error parsing category: " + e.getMessage());
                                }
                            }
                        }

                        if (categoryAdapter != null) {
                            categoryAdapter.notifyDataSetChanged();
                        }

                        Log.d(TAG, "✅ Loaded " + categories.size() + " REAL categories");
                    } else {
                        Log.e(TAG, "❌ Categories API failed - NO FALLBACK TO SAMPLE DATA");
                        // ✅ NO FALLBACK - Just empty list
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing categories", e);
                    // ✅ NO FALLBACK - Just empty list
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                Log.e(TAG, "❌ Categories network error - NO FALLBACK TO SAMPLE DATA", t);
                // ✅ NO FALLBACK - Just empty list
            }
        });
    }

    // ✅ REMOVED: loadSampleCategories() method completely

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
                                            Log.d(TAG, "✅ Added REAL product: " + product.getTitle() + " (ID: " + product.getId() + ")");
                                        }
                                    } catch (Exception e) {
                                        Log.w(TAG, "❌ Error parsing product: " + e.getMessage());
                                    }
                                }
                            }

                            // ✅ THÊM: Prioritize nearby products after loading
                            prioritizeNearbyProducts();

                            if (productAdapter != null) {
                                productAdapter.notifyDataSetChanged();
                            }

                            Log.d(TAG, "✅ Loaded " + products.size() + " REAL products");
                        } else {
                            Log.e(TAG, "❌ Products API failed - NO FALLBACK TO SAMPLE DATA");
                        }
                    } else {
                        Log.e(TAG, "❌ Products response not successful - NO FALLBACK TO SAMPLE DATA");
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
                Log.e(TAG, "❌ Products network error - NO FALLBACK TO SAMPLE DATA", t);
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

            // ✅ THÊM: Location coordinates parsing
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

            // Image handling - Support multiple formats
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                List<String> imageUrls = (List<String>) imageUrlsObj;
                product.setImageUrls(imageUrls);
                if (!imageUrls.isEmpty()) {
                    product.setImageUrl(imageUrls.get(0)); // Set primary image
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

            return product;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating product from data", e);
            return null;
        }
    }

    // Navigation methods
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

    private void prioritizeNearbyProducts() {
        if (userLatitude != null && userLongitude != null && !products.isEmpty()) {

            // Sort products by distance from user location
            products.sort((p1, p2) -> {
                Double distance1 = calculateProductDistance(p1);
                Double distance2 = calculateProductDistance(p2);

                // Products without location go to end
                if (distance1 == null && distance2 == null) return 0;
                if (distance1 == null) return 1;
                if (distance2 == null) return -1;

                // Closer products first
                return distance1.compareTo(distance2);
            });

            Log.d(TAG, "✅ Products sorted by distance from user location (" +
                    userLatitude + ", " + userLongitude + ")");

            // Log first few products with distances for debugging
            for (int i = 0; i < Math.min(3, products.size()); i++) {
                Product p = products.get(i);
                Double distance = calculateProductDistance(p);
                Log.d(TAG, "  Product " + (i+1) + ": " + p.getTitle() +
                        " - Distance: " + (distance != null ? String.format("%.1fkm", distance) : "Unknown"));
            }
        } else {
            Log.d(TAG, "⚠️ Cannot prioritize nearby products - User location: " +
                    userLatitude + ", " + userLongitude + " | Products count: " + products.size());
        }
    }
    private Double calculateProductDistance(Product product) {
        if (product.getLatitude() != null && product.getLongitude() != null
                && userLatitude != null && userLongitude != null) {

            return LocationUtils.calculateDistance(
                    userLatitude, userLongitude,
                    product.getLatitude(), product.getLongitude()
            );
        }
        return null;
    }

    private void navigateToAllProducts() {
        try {
            Intent intent = new Intent(getActivity(), AllProductsActivity.class);
            startActivity(intent);
            Log.d(TAG, "✅ Navigated to all products");
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 HomeFragment resumed");
    }
}