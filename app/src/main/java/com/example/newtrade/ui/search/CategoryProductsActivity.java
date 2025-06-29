// app/src/main/java/com/example/newtrade/ui/search/CategoryProductsActivity.java
package com.example.newtrade.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryProductsActivity extends AppCompatActivity {

    private static final String TAG = "CategoryProductsActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView tvEmptyState;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvProducts;

    // Data
    private ProductAdapter productAdapter;
    private final List<Product> products = new ArrayList<>();
    private Long categoryId;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadProducts();
    }

    private void getIntentData() {
        categoryId = getIntent().getLongExtra("category_id", -1L);
        categoryName = getIntent().getStringExtra("category_name");

        if (categoryId <= 0) {
            Log.e(TAG, "Invalid category ID");
            finish();
            return;
        }

        Log.d(TAG, "Loading products for category: " + categoryName + " (ID: " + categoryId + ")");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvProducts = findViewById(R.id.rv_products);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName != null ? categoryName : "Category Products");
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
        swipeRefresh.setOnRefreshListener(this::loadProducts);
    }

    private void loadProducts() {
        swipeRefresh.setRefreshing(true);

        // ✅ FIX: Updated to use StandardResponse
        ApiClient.getApiService().searchProducts("", 0, 20, categoryId, null, null, null)
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Product>> call,
                                           @NonNull Response<List<Product>> response) {
                        swipeRefresh.setRefreshing(false);

                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Product> products = response.body();
                                updateProductList(products);
                            } else {
                                showError("Không thể tải sản phẩm");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing response", e);
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        showError("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    private void updateProductList(List<Product> newProducts) {
        products.clear();

        if (newProducts != null && !newProducts.isEmpty()) {
            products.addAll(newProducts);
            showProducts();
        } else {
            showEmptyState();
        }

        productAdapter.notifyDataSetChanged();
        Log.d(TAG, "✅ Products updated: " + products.size());
    }

    // ✅ FIX: Handle BigDecimal conversion properly
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
                showProducts();
            }
        } else {
            showEmptyState();
        }

        productAdapter.notifyDataSetChanged();
        Log.d(TAG, "✅ Products updated: " + products.size());
    }

    // ✅ FIX: Parse product with proper BigDecimal conversion
    private Product parseProductFromMap(Map<String, Object> productData) {
        Product product = new Product();

        try {
            if (productData.get("id") instanceof Number) {
                product.setId(((Number) productData.get("id")).longValue());
            }

            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));

            // ✅ FIX: Handle BigDecimal to Double conversion
            Object priceObj = productData.get("price");
            if (priceObj != null) {
                if (priceObj instanceof BigDecimal) {
                    product.setPriceFromBigDecimal((BigDecimal) priceObj);
                } else if (priceObj instanceof Number) {
                    product.setPrice(((Number) priceObj).doubleValue());
                }
            }

            product.setLocation((String) productData.get("location"));
            product.setImageUrl((String) productData.get("primaryImageUrl"));
            product.setPrimaryImageUrl((String) productData.get("primaryImageUrl"));

            // Handle condition conversion
            Object conditionObj = productData.get("condition");
            if (conditionObj != null) {
                product.setCondition(conditionObj.toString());
            }

            // Handle status conversion
            Object statusObj = productData.get("status");
            if (statusObj != null) {
                product.setStatus(statusObj.toString());
            }

            product.setCreatedAt((String) productData.get("createdAt"));
            product.setUpdatedAt((String) productData.get("updatedAt"));

            if (productData.get("userId") instanceof Number) {
                product.setUserId(((Number) productData.get("userId")).longValue());
            }

            if (productData.get("categoryId") instanceof Number) {
                product.setCategoryId(((Number) productData.get("categoryId")).longValue());
            }

            product.setCategoryName((String) productData.get("categoryName"));

            if (productData.get("viewCount") instanceof Number) {
                product.setViewCount(((Number) productData.get("viewCount")).intValue());
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
            tvEmptyState.setText("No products found in " + categoryName);
        }
    }

    private void showError(String message) {
        swipeRefresh.setRefreshing(false);
        Log.e(TAG, message);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText(message);
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