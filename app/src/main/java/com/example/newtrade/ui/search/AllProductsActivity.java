// app/src/main/java/com/example/newtrade/ui/search/AllProductsActivity.java
package com.example.newtrade.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.google.android.material.appbar.MaterialToolbar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllProductsActivity extends AppCompatActivity {

    private static final String TAG = "AllProductsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyState;

    // Data
    private ProductAdapter productAdapter;
    private final List<Product> products = new ArrayList<>();
    private boolean isLoading = false;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadProducts();

        Log.d(TAG, "AllProductsActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvProducts = findViewById(R.id.rv_products);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("All Products");
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(products, product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_title", product.getTitle());
            intent.putExtra("product_price", product.getFormattedPrice());
            startActivity(intent);
        });

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshProducts);
    }

    private void refreshProducts() {
        currentPage = 0;
        products.clear();
        loadProducts();
    }

    private void loadProducts() {
        if (isLoading) return;

        isLoading = true;
        swipeRefresh.setRefreshing(true);

        ApiClient.getApiService().getProducts(currentPage, 20, null, null)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        isLoading = false;
                        swipeRefresh.setRefreshing(false);

                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();

                                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                    Map<String, Object> data = apiResponse.getData();
                                    List<Map<String, Object>> productList =
                                            (List<Map<String, Object>>) data.get("products");

                                    if (productList != null) {
                                        updateProducts(productList);
                                    } else {
                                        showEmptyState();
                                    }
                                } else {
                                    Log.e(TAG, "API error: " + apiResponse.getMessage());
                                    showEmptyState();
                                }
                            } else {
                                Log.e(TAG, "Request failed: " + response.code());
                                showEmptyState();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing response", e);
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        isLoading = false;
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "Request failed", t);
                        showEmptyState();
                    }
                });
    }

    // ✅ FIX: Handle BigDecimal and ProductCondition conversion properly
    private void updateProducts(List<Map<String, Object>> productMaps) {
        if (currentPage == 0) {
            products.clear();
        }

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
                showProducts();
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

            // ✅ FIX: Handle ProductCondition to String conversion
            Object conditionObj = productMap.get("condition");
            if (conditionObj != null) {
                if (conditionObj instanceof Product.ProductCondition) {
                    product.setCondition(((Product.ProductCondition) conditionObj).getDisplayName());
                } else {
                    product.setCondition(conditionObj.toString());
                }
            }

            // Parse other fields
            product.setStatus((String) productMap.get("status"));
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

    private void showProducts() {
        if (rvProducts != null) rvProducts.setVisibility(View.VISIBLE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        if (rvProducts != null) rvProducts.setVisibility(View.GONE);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No products found");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}