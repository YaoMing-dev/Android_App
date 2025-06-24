// File: app/src/main/java/com/example/newtrade/ui/search/CategoryProductsActivity.java
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
    private List<Product> products = new ArrayList<>();
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
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        swipeRefresh.setRefreshing(false);

                        try {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Map<String, Object> data = response.body().getData();
                                List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                                products.clear();
                                if (productList != null) {
                                    for (Map<String, Object> productData : productList) {
                                        Product product = parseProductFromBackend(productData);
                                        if (product != null) {
                                            products.add(product);
                                        }
                                    }
                                }

                                productAdapter.notifyDataSetChanged();
                                updateEmptyState();

                                Log.d(TAG, "✅ Loaded " + products.size() + " products for category: " + categoryName);
                            } else {
                                Log.w(TAG, "❌ Failed to load category products");
                                updateEmptyState();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing products", e);
                            updateEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "❌ Category products API failed", t);
                        updateEmptyState();
                    }
                });
    }

    private Product parseProductFromBackend(Map<String, Object> productData) {
        try {
            Product product = new Product();
            product.setId(((Number) productData.get("id")).longValue());
            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));

            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(((Number) priceObj).doubleValue());
            }

            product.setCondition((String) productData.get("condition"));
            product.setLocation((String) productData.get("location"));
            product.setStatus((String) productData.get("status"));

            // Parse images
            Object imagesObj = productData.get("images");
            if (imagesObj instanceof List) {
                List<Map<String, Object>> imagesList = (List<Map<String, Object>>) imagesObj;
                List<String> imageUrls = new ArrayList<>();
                for (Map<String, Object> imageData : imagesList) {
                    String imageUrl = (String) imageData.get("imageUrl");
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl);
                    }
                }
                product.setImageUrls(imageUrls);
            }

            return product;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing product", e);
            return null;
        }
    }

    private void updateEmptyState() {
        if (products.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
            tvEmptyState.setText("No products found in " + categoryName);
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