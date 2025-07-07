// File: app/src/main/java/com/example/newtrade/ui/search/CategoryProductsActivity.java
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
            Log.d(TAG, "Product clicked: " + product.getTitle());
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

        Log.d(TAG, "🔄 Loading products for category: " + categoryId);

        // ✅ FIX: Sử dụng đúng signature
        ApiClient.getApiService().getProducts(
                0,           // page
                50,          // size
                null,        // search
                categoryId,  // categoryId
                null,        // condition
                null,        // minPrice
                null,        // maxPrice
                "createdAt", // sortBy
                "desc"       // sortDir
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                swipeRefresh.setRefreshing(false);

                Log.d(TAG, "🔍 Response code: " + response.code());

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> standardResponse = response.body();

                        Log.d(TAG, "🔍 Response success: " + standardResponse.isSuccess());
                        Log.d(TAG, "🔍 Response message: " + standardResponse.getMessage());

                        if (standardResponse.isSuccess()) {
                            Map<String, Object> data = standardResponse.getData();

                            if (data != null && data.containsKey("content")) {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");
                                updateProductList(productList);
                                Log.d(TAG, "✅ Found " + productList.size() + " products in category");
                            } else {
                                Log.w(TAG, "⚠️ No content field in response, trying fallback...");
                                loadProductsByCategory();
                            }
                        } else {
                            Log.e(TAG, "❌ Response not successful: " + standardResponse.getMessage());
                            loadProductsByCategory();
                        }
                    } else {
                        Log.e(TAG, "❌ Response not successful or null body");
                        loadProductsByCategory();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing products response", e);
                    loadProductsByCategory();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "❌ Products API call failed for category " + categoryId, t);
                loadProductsByCategory();
            }
        });
    }

    // ✅ STRATEGY 2: Fallback sử dụng dedicated category endpoint
    private void loadProductsByCategory() {
        Log.d(TAG, "🔄 Trying dedicated category endpoint...");

        ApiClient.getProductService().getProductsByCategory(categoryId)
                .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                           @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {

                        try {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                List<Map<String, Object>> productList = response.body().getData();
                                updateProductList(productList);
                                Log.d(TAG, "✅ Loaded " + productList.size() + " products via category endpoint");
                            } else {
                                Log.e(TAG, "❌ Category endpoint failed, loading sample data");
                                loadSampleProducts();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing category products", e);
                            loadSampleProducts();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                        Log.e(TAG, "❌ Category endpoint failed", t);
                        loadSampleProducts();
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

            productAdapter.notifyDataSetChanged();
            showProductsView();
            Log.d(TAG, "✅ Updated UI with " + products.size() + " products");
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

            // Image URLs handling
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                product.setImageUrls((List<String>) imageUrlsObj);
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

    // ✅ STRATEGY 3: Sample data nếu backend fails
    private void loadSampleProducts() {
        Log.d(TAG, "📦 Loading sample products for " + categoryName);

        products.clear();

        // Tạo sample products dựa trên category
        if ("Electronics".equalsIgnoreCase(categoryName)) {
            products.add(createSampleProduct(1L, "iPhone 15 Pro Max 1TB", "Latest iPhone with titanium design", "32,500,000 VND", "Ho Chi Minh City", Product.ProductCondition.LIKE_NEW));
            products.add(createSampleProduct(2L, "Samsung Galaxy S24 Ultra", "256GB, Titanium Black", "25,000,000 VND", "Ho Chi Minh City", Product.ProductCondition.GOOD));
            products.add(createSampleProduct(3L, "MacBook Pro M3 16\"", "1TB SSD, 32GB RAM", "65,000,000 VND", "Ho Chi Minh City", Product.ProductCondition.LIKE_NEW));
        } else if ("Fashion".equalsIgnoreCase(categoryName)) {
            products.add(createSampleProduct(4L, "Louis Vuitton Neverfull", "Authentic, like new condition", "38,000,000 VND", "Ho Chi Minh City", Product.ProductCondition.LIKE_NEW));
            products.add(createSampleProduct(5L, "Gucci Ace Sneakers", "Size 42, white leather", "15,000,000 VND", "Ho Chi Minh City", Product.ProductCondition.GOOD));
        }
        // Thêm sample cho categories khác...

        productAdapter.notifyDataSetChanged();
        showProductsView();
        Log.d(TAG, "✅ Loaded " + products.size() + " sample products");
    }

    private Product createSampleProduct(Long id, String title, String description, String price, String location, Product.ProductCondition condition) {
        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(new BigDecimal("1000000")); // Default price
        product.setLocation(location);
        product.setCondition(condition);
        product.setImageUrl("https://via.placeholder.com/300x300?text=" + title.charAt(0));
        return product;
    }

    private void showProductsView() {
        rvProducts.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        rvProducts.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText("No products found in " + categoryName);
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