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

import com.example.newtrade.R;
import com.example.newtrade.adapters.CategoryAdapter;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.search.CategoryProductsActivity;
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

        Log.d(TAG, "✅ HomeFragment initialized successfully");
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvRecentProducts = view.findViewById(R.id.rv_recent_products);
        tvViewAllCategories = view.findViewById(R.id.tv_view_all_categories);
        tvViewAllProducts = view.findViewById(R.id.tv_view_all_products);
        fabQuickAdd = view.findViewById(R.id.fab_quick_add);
        emptyView = view.findViewById(R.id.empty_view);
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(categories, category -> {
            // Navigate to category products
            Intent intent = new Intent(requireContext(), CategoryProductsActivity.class);
            intent.putExtra("category_id", category.getId());
            intent.putExtra("category_name", category.getName());
            startActivity(intent);
        });
        rvCategories.setAdapter(categoryAdapter);

        // Products RecyclerView
        rvRecentProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        productAdapter = new ProductAdapter(products, this::openProductDetail);
        rvRecentProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadData);

        tvViewAllCategories.setOnClickListener(v -> {
            // Navigate to all categories
            Toast.makeText(requireContext(), "View all categories", Toast.LENGTH_SHORT).show();
        });

        tvViewAllProducts.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AllProductsActivity.class);
            startActivity(intent);
        });

        fabQuickAdd.setOnClickListener(v -> {
            // Navigate to add product
            try {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.nav_add_product);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error", e);
                Toast.makeText(requireContext(), "Quick add coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        loadCategories();
        loadRecentProducts();
    }

    private void loadCategories() {
        ApiClient.getApiService().getCategories()
                .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                           Response<StandardResponse<List<Map<String, Object>>>> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                    updateCategories(apiResponse.getData());
                                } else {
                                    Log.e(TAG, "Categories API error: " + apiResponse.getMessage());
                                }
                            } else {
                                Log.e(TAG, "Categories request failed: " + response.code());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing categories response", e);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                        Log.e(TAG, "Categories request failed", t);
                        showError("Failed to load categories");
                    }
                });
    }

    private void loadRecentProducts() {
        ApiClient.getApiService().getRecentProducts(10)
                .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                           Response<StandardResponse<List<Map<String, Object>>>> response) {
                        swipeRefresh.setRefreshing(false);

                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                    updateProducts(apiResponse.getData());
                                } else {
                                    Log.e(TAG, "Products API error: " + apiResponse.getMessage());
                                    showEmptyState();
                                }
                            } else {
                                Log.e(TAG, "Products request failed: " + response.code());
                                showEmptyState();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing products response", e);
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "Products request failed", t);
                        showError("Failed to load products");
                        showEmptyState();
                    }
                });
    }

    // ✅ FIX: Handle BigDecimal to Double conversion properly
    private void updateProducts(List<Map<String, Object>> productMaps) {
        products.clear();

        if (productMaps != null && !productMaps.isEmpty()) {
            for (Map<String, Object> productMap : productMaps) {
                try {
                    Product product = parseProductFromMap(productMap);
                    products.add(product);
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing product: " + productMap, e);
                }
            }

            if (products.isEmpty()) {
                showEmptyState();
            } else {
                showProductsState();
            }
        } else {
            showEmptyState();
        }

        productAdapter.notifyDataSetChanged();
        Log.d(TAG, "✅ Products updated: " + products.size());
    }

    // ✅ FIX: Parse product with proper BigDecimal and ProductCondition conversion
    private Product parseProductFromMap(Map<String, Object> productMap) {
        Product product = new Product();

        try {
            // Parse ID
            if (productMap.get("id") instanceof Number) {
                product.setId(((Number) productMap.get("id")).longValue());
            }

            // Parse basic fields
            product.setTitle((String) productMap.get("title"));
            product.setDescription((String) productMap.get("description"));
            product.setLocation((String) productMap.get("location"));
            product.setImageUrl((String) productMap.get("primaryImageUrl"));
            product.setPrimaryImageUrl((String) productMap.get("primaryImageUrl"));

            // ✅ FIX: Handle BigDecimal to Double conversion
            Object priceObj = productMap.get("price");
            if (priceObj != null) {
                if (priceObj instanceof BigDecimal) {
                    product.setPriceFromBigDecimal((BigDecimal) priceObj);
                } else if (priceObj instanceof Number) {
                    product.setPrice(((Number) priceObj).doubleValue());
                }
            }

            // ✅ FIX: Handle ProductCondition string conversion
            Object conditionObj = productMap.get("condition");
            if (conditionObj != null) {
                if (conditionObj instanceof Product.ProductCondition) {
                    product.setCondition(((Product.ProductCondition) conditionObj).getDisplayName());
                } else {
                    product.setCondition(conditionObj.toString());
                }
            }

            // ✅ FIX: Handle ProductStatus string conversion
            Object statusObj = productMap.get("status");
            if (statusObj != null) {
                if (statusObj instanceof Product.ProductStatus) {
                    product.setStatus(((Product.ProductStatus) statusObj).getDisplayName());
                } else {
                    product.setStatus(statusObj.toString());
                }
            }

            // Parse other fields
            product.setCreatedAt((String) productMap.get("createdAt"));
            product.setUpdatedAt((String) productMap.get("updatedAt"));

            if (productMap.get("userId") instanceof Number) {
                product.setUserId(((Number) productMap.get("userId")).longValue());
            }

            if (productMap.get("categoryId") instanceof Number) {
                product.setCategoryId(((Number) productMap.get("categoryId")).longValue());
            }

            product.setCategoryName((String) productMap.get("categoryName"));

            if (productMap.get("viewCount") instanceof Number) {
                product.setViewCount(((Number) productMap.get("viewCount")).intValue());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing product from map", e);
        }

        return product;
    }

    private void updateCategories(List<Map<String, Object>> categoryMaps) {
        categories.clear();

        if (categoryMaps != null) {
            for (Map<String, Object> categoryMap : categoryMaps) {
                try {
                    Category category = new Category();

                    if (categoryMap.get("id") instanceof Number) {
                        category.setId(((Number) categoryMap.get("id")).longValue());
                    }

                    category.setName((String) categoryMap.get("name"));
                    category.setDescription((String) categoryMap.get("description"));
                    category.setIconUrl((String) categoryMap.get("iconUrl"));

                    categories.add(category);
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing category: " + categoryMap, e);
                }
            }
        }

        categoryAdapter.notifyDataSetChanged();
        Log.d(TAG, "✅ Categories updated: " + categories.size());
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getFormattedPrice());
        startActivity(intent);
    }

    private void showEmptyState() {
        if (rvRecentProducts != null) {
            rvRecentProducts.setVisibility(View.GONE);
        }
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void showProductsState() {
        if (rvRecentProducts != null) {
            rvRecentProducts.setVisibility(View.VISIBLE);
        }
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        loadData();
    }
}