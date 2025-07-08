// app/src/main/java/com/example/newtrade/ui/product/CategoryProductsActivity.java
package com.example.newtrade.ui.product;

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

public class CategoryProductsActivity extends AppCompatActivity {

    private static final String TAG = "CategoryProductsActivity";

    // UI Components
    private MaterialToolbar toolbar;
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

        Log.d(TAG, "📱 Category ID: " + categoryId + ", Name: " + categoryName);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvProducts = findViewById(R.id.rv_products);

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName);
        }
    }

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
            Log.d(TAG, "📱 Product clicked: " + product.getTitle() + " (ID: " + product.getId() + ")");
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
        tvEmptyState.setVisibility(View.GONE);
        rvProducts.setVisibility(View.VISIBLE);

        Log.d(TAG, "🔄 Loading REAL products for category: " + categoryId);

        // ✅ STRATEGY 1: Load by category filter
        ApiClient.getApiService().getProducts(
                0,           // page
                50,          // size
                null,        // search
                categoryId,  // categoryId filter
                null,        // minPrice
                null,        // maxPrice
                null,        // condition
                null,        // location
                null         // radius
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                swipeRefresh.setRefreshing(false);

                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Map<String, Object> data = response.body().getData();
                        List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                        updateProductList(productList);
                        Log.d(TAG, "✅ Loaded " + (productList != null ? productList.size() : 0) + " REAL products");
                    } else {
                        Log.e(TAG, "❌ API response not successful or null body");
                        // ✅ NO FALLBACK TO SAMPLE DATA - Just show empty state
                        showEmptyState();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing products response", e);
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "❌ Products API call failed", t);
                // ✅ NO FALLBACK TO SAMPLE DATA - Just show empty state
                showEmptyState();
            }
        });
    }

    private void updateProductList(List<Map<String, Object>> productList) {
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

            if (!products.isEmpty()) {
                productAdapter.notifyDataSetChanged();
                showProductsView();
                Log.d(TAG, "✅ Updated UI with " + products.size() + " REAL products");
            } else {
                showEmptyState();
            }
        } else {
            showEmptyState();
        }
    }

    private Product parseProductFromData(Map<String, Object> productData) {
        try {
            Product product = new Product();

            // Basic fields
            product.setId(((Number) productData.get("id")).longValue());
            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));
            product.setLocation((String) productData.get("location"));

            // Price handling
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }

            // Image URLs handling - Support multiple formats
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

            Log.d(TAG, "✅ Parsed REAL product: " + product.getTitle() + " (ID: " + product.getId() + ")");
            return product;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating product from data", e);
            return null;
        }
    }

    // ✅ REMOVED: loadSampleProducts() method completely
    // ✅ REMOVED: createSampleProduct() method completely

    private void showProductsView() {
        rvProducts.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        rvProducts.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText("No products found in " + categoryName + "\n\nCheck back later or try a different category.");
        Log.d(TAG, "📭 Showing empty state for category: " + categoryName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (products.isEmpty()) {
            loadProducts();
        }
    }
}