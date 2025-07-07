// File: app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
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

            Log.d(TAG, "✅ HomeFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some HomeFragment views not found: " + e.getMessage());
        }
    }

    private void setupRecyclerViews() {
        try {
            debugApiConnection();
            // Categories RecyclerView (horizontal) với click listener hoạt động
            if (rvCategories != null) {
                categoryAdapter = new CategoryAdapter(categories, category -> {
                    // 🔥 THÊM NAVIGATION ĐẾN CATEGORY PRODUCTS
                    Log.d(TAG, "Category clicked: " + category.getName());
                    navigateToCategoryProducts(category);
                });
                rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                rvCategories.setAdapter(categoryAdapter);
            }

            // Products RecyclerView (grid)
            if (rvRecentProducts != null) {
                productAdapter = new ProductAdapter(products, product -> {
                    Log.d(TAG, "Product clicked: " + product.getTitle());
                    navigateToProductDetail(product);
                });
                rvRecentProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
                rvRecentProducts.setAdapter(productAdapter);
            }

            Log.d(TAG, "✅ HomeFragment RecyclerViews setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up RecyclerViews", e);
        }
    }
    private void debugApiConnection() {
        String baseUrl = ApiClient.getCurrentBaseUrl();
        Log.d(TAG, "🔍 Current BASE_URL: " + baseUrl);

        // Test health check
        if (ApiClient.isInitialized()) {
            ApiClient.getApiService().healthCheck().enqueue(new Callback<StandardResponse<String>>() {
                @Override
                public void onResponse(Call<StandardResponse<String>> call, Response<StandardResponse<String>> response) {
                    Log.d(TAG, response.isSuccessful() ? "✅ Backend reachable" : "❌ Backend error: " + response.code());
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

            // 🔥 HOẠT ĐỘNG CLICK VIEW ALL CATEGORIES
            if (tvViewAllCategories != null) {
                tvViewAllCategories.setOnClickListener(v -> {
                    Log.d(TAG, "View All Categories clicked");
                    navigateToAllCategories();
                });
            }

            // 🔥 HOẠT ĐỘNG CLICK VIEW ALL PRODUCTS
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

            loadCategoriesFromBackend();
            loadProductsFromBackend();

            Log.d(TAG, "✅ Data loading started");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting data load", e);
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
        }
    }

    private void loadCategoriesFromBackend() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                   Response<StandardResponse<List<Map<String, Object>>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            categories.clear();

                            for (Map<String, Object> categoryData : apiResponse.getData()) {
                                Category category = new Category();
                                category.setId(((Number) categoryData.get("id")).longValue());
                                category.setName((String) categoryData.get("name"));
                                category.setDescription((String) categoryData.get("description"));
                                category.setIcon((String) categoryData.get("icon"));
                                categories.add(category);
                            }

                            if (categoryAdapter != null) {
                                categoryAdapter.notifyDataSetChanged();
                            }

                            Log.d(TAG, "✅ Loaded " + categories.size() + " categories from backend");
                        } else {
                            Log.w(TAG, "❌ Categories response unsuccessful");
                            loadSampleCategories();
                        }
                    } else {
                        Log.w(TAG, "❌ Categories response failed");
                        loadSampleCategories();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing categories", e);
                    loadSampleCategories();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
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

        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }

        Log.d(TAG, "✅ Loaded sample categories");
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

                            if (productList != null) {
                                products.clear();

                                for (Map<String, Object> productData : productList) {
                                    Product product = new Product();
                                    product.setId(((Number) productData.get("id")).longValue());
                                    product.setTitle((String) productData.get("title"));
                                    product.setDescription((String) productData.get("description"));

                                    // Handle price conversion - SỬA LỖI TẠI ĐÂY
                                    Object priceObj = productData.get("price");
                                    if (priceObj instanceof Number) {
                                        product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                                    }

                                    product.setLocation((String) productData.get("location"));

                                    // Handle imageUrls - SỬA LỖI TẠI ĐÂY
                                    Object imageUrlsObj = productData.get("imageUrls");
                                    if (imageUrlsObj instanceof List) {
                                        product.setImageUrls((List<String>) imageUrlsObj);
                                    } else if (imageUrlsObj instanceof String) {
                                        product.setImageUrl((String) imageUrlsObj);
                                    }

                                    // Handle condition - SỬA LỖI TẠI ĐÂY
                                    String condition = (String) productData.get("condition");
                                    if (condition != null) {
                                        try {
                                            product.setCondition(Product.ProductCondition.valueOf(condition));
                                        } catch (IllegalArgumentException e) {
                                            product.setCondition(Product.ProductCondition.GOOD);
                                        }
                                    }

                                    products.add(product);
                                }

                                if (productAdapter != null) {
                                    productAdapter.notifyDataSetChanged();
                                }

                                // Show/hide empty view
                                if (emptyView != null && rvRecentProducts != null) {
                                    if (products.isEmpty()) {
                                        emptyView.setVisibility(View.VISIBLE);
                                        rvRecentProducts.setVisibility(View.GONE);
                                    } else {
                                        emptyView.setVisibility(View.GONE);
                                        rvRecentProducts.setVisibility(View.VISIBLE);
                                    }
                                }

                                Log.d(TAG, "✅ Loaded " + products.size() + " products from backend");
                            }
                        } else {
                            Log.w(TAG, "❌ Products response unsuccessful");
                            showError("Failed to load products");
                        }
                    } else {
                        Log.w(TAG, "❌ Products response failed");
                        showError("Failed to load products");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing products", e);
                    showError("Error loading products");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                Log.e(TAG, "❌ Products API call failed", t);
                showError("Network error loading products");
            }
        });
    }

    // Navigation methods
    private void navigateToCategoryProducts(Category category) {
        try {
            Intent intent = new Intent(getContext(), CategoryProductsActivity.class);
            intent.putExtra("category_id", category.getId());
            intent.putExtra("category_name", category.getName());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to category products", e);
            showError("Cannot open category");
        }
    }

    private void navigateToProductDetail(Product product) {
        try {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_title", product.getTitle());
            intent.putExtra("product_price", product.getFormattedPrice());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to product detail", e);
            showError("Cannot open product");
        }
    }

    private void navigateToAllCategories() {
        try {
            // Navigate to search fragment and show all categories
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.nav_search);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to all categories", e);
            showError("Cannot open categories");
        }
    }

    private void navigateToAllProducts() {
        try {
            Intent intent = new Intent(getContext(), AllProductsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to all products", e);
            showError("Cannot open all products");
        }
    }

    private void navigateToAddProduct() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.nav_add_product);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to add product", e);
            showError("Cannot open add product");
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}