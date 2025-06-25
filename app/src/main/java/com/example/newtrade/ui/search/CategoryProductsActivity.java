// app/src/main/java/com/example/newtrade/ui/search/CategoryProductsActivity.java
// ✅ FIXED: Added back button handling
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
        categoryId = getIntent().getLongExtra("category_id", -1);
        categoryName = getIntent().getStringExtra("category_name");

        if (categoryName == null) {
            categoryName = "Category Products";
        }

        Log.d(TAG, "Category ID: " + categoryId + ", Name: " + categoryName);
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
            getSupportActionBar().setTitle(categoryName);
        }
    }

    // ✅ FIXED: Added back button handling
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        // Search products by category
        ApiClient.getApiService().searchProducts(null, 0, 50, categoryId, null, null, null)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                           @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                        swipeRefresh.setRefreshing(false);

                        try {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Map<String, Object> data = response.body().getData();
                                if (data != null && data.containsKey("content")) {
                                    @SuppressWarnings("unchecked")
                                    List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");
                                    updateProductList(productList);
                                } else {
                                    showEmptyState();
                                }
                            } else {
                                Log.e(TAG, "❌ Failed to load products");
                                showEmptyState();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing products", e);
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "❌ Products API call failed", t);
                        showEmptyState();
                    }
                });
    }

    private void updateProductList(List<Map<String, Object>> productList) {
        products.clear();

        if (productList != null && !productList.isEmpty()) {
            for (Map<String, Object> productData : productList) {
                try {
                    Product product = parseProductFromMap(productData);
                    products.add(product);
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing product: " + productData, e);
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

    private Product parseProductFromMap(Map<String, Object> productData) {
        Product product = new Product();

        if (productData.get("id") instanceof Number) {
            product.setId(((Number) productData.get("id")).longValue());
        }

        product.setTitle((String) productData.get("title"));
        product.setDescription((String) productData.get("description"));

        if (productData.get("price") instanceof Number) {
            product.setPrice(BigDecimal.valueOf(((Number) productData.get("price")).doubleValue()));
        }

        product.setLocation((String) productData.get("location"));
        product.setPrimaryImageUrl((String) productData.get("primaryImageUrl"));

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
}