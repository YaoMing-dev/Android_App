// File: app/src/main/java/com/example/newtrade/ui/search/AllProductsActivity.java
package com.example.newtrade.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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

public class AllProductsActivity extends AppCompatActivity {

    private static final String TAG = "AllProductsActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView tvEmptyState;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvProducts;

    // Data
    private ProductAdapter productAdapter;
    private final List<Product> products = new ArrayList<>();
    private int currentPage = 0;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadProducts();
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
                                    List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                                    if (productList != null) {
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

                                        productAdapter.notifyDataSetChanged();
                                        updateEmptyView();

                                        Log.d(TAG, "✅ Loaded " + productList.size() + " products");
                                    }
                                } else {
                                    Log.w(TAG, "❌ Products response unsuccessful");
                                }
                            } else {
                                Log.w(TAG, "❌ Products response failed");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing products", e);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        isLoading = false;
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "❌ Products API call failed", t);
                    }
                });
    }

    private void updateEmptyView() {
        if (products.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}