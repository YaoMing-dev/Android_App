// app/src/main/java/com/example/newtrade/ui/profile/MyListingsActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ProductService;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.AddProductActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyListingsActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "MyListingsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvMyListings;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddProduct;

    // Data & Adapters
    private final List<Product> myProducts = new ArrayList<>();
    private ProductAdapter productAdapter;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadMyProducts();

        Log.d(TAG, "MyListingsActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvMyListings = findViewById(R.id.rv_my_listings);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        fabAddProduct = findViewById(R.id.fab_add_product);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Listings");
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(myProducts, this);
        rvMyListings.setLayoutManager(new LinearLayoutManager(this));
        rvMyListings.setAdapter(productAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadMyProducts);

        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddProductActivity.class);
            startActivity(intent);
        });
    }

    private void loadMyProducts() {
        swipeRefresh.setRefreshing(true);

        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            swipeRefresh.setRefreshing(false);
            showEmptyState();
            return;
        }

        ApiClient.getProductService().getMyProducts(0, 50)
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
                    public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "Request failed", t);
                        showEmptyState();
                    }
                });
    }

    // ✅ FIX: Handle BigDecimal, ProductCondition, ProductStatus conversion properly
    private void updateProducts(List<Map<String, Object>> productMaps) {
        myProducts.clear();

        if (productMaps != null && !productMaps.isEmpty()) {
            for (Map<String, Object> productMap : productMaps) {
                try {
                    Product product = parseProductFromMap(productMap);
                    myProducts.add(product);
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing product: " + productMap, e);
                }
            }

            if (myProducts.isEmpty()) {
                showEmptyState();
            } else {
                showProducts();
            }
        } else {
            showEmptyState();
        }

        productAdapter.notifyDataSetChanged();
        Log.d(TAG, "✅ My products updated: " + myProducts.size());
    }

    // ✅ FIX: Parse product with all conversion fixes
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

            // ✅ FIX: Handle ProductStatus to String conversion
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

    private void showProducts() {
        if (rvMyListings != null) rvMyListings.setVisibility(View.VISIBLE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        if (rvMyListings != null) rvMyListings.setVisibility(View.GONE);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No products found. Tap + to add your first product!");
        }
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getFormattedPrice());
        startActivity(intent);
    }

    @Override
    public void onProductLongClick(Product product) {
        // Show options menu for product (edit, delete, etc.)
        // TODO: Implement product options
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh products when returning to activity
        loadMyProducts();
    }
}