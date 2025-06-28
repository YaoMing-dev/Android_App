// app/src/main/java/com/example/newtrade/ui/profile/SavedItemsActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.models.Product;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedItemsActivity extends AppCompatActivity {

    private static final String TAG = "SavedItemsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvSavedItems;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyState;

    // Data
    private ProductAdapter productAdapter;
    private List<Product> savedProducts = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadSavedItems();

        Log.d(TAG, "SavedItemsActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvSavedItems = findViewById(R.id.rv_saved_items);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Saved Items");
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(savedProducts, this::openProductDetail);
        rvSavedItems.setLayoutManager(new GridLayoutManager(this, 2));
        rvSavedItems.setAdapter(productAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadSavedItems);
    }

    private void loadSavedItems() {
        swipeRefresh.setRefreshing(true);

        // Create mock data for testing - replace with actual API call
        createMockSavedItems();
    }

    // ✅ FIX: Create mock data with proper BigDecimal/ProductCondition/ProductStatus handling
    private void createMockSavedItems() {
        savedProducts.clear();

        // Mock saved item 1
        Product product1 = createMockProduct(1L, "iPhone 14 Pro Max",
                "Like new iPhone 14 Pro Max 256GB", 25000000.0,
                "Ho Chi Minh City", "LIKE_NEW", "AVAILABLE");

        // Mock saved item 2
        Product product2 = createMockProduct(2L, "MacBook Air M2",
                "MacBook Air M2 8GB RAM 256GB SSD", 28000000.0,
                "Hanoi", "GOOD", "AVAILABLE");

        // Mock saved item 3
        Product product3 = createMockProduct(3L, "Samsung Galaxy S23",
                "Samsung Galaxy S23 Ultra 512GB", 22000000.0,
                "Da Nang", "NEW", "SOLD");

        savedProducts.add(product1);
        savedProducts.add(product2);
        savedProducts.add(product3);

        swipeRefresh.setRefreshing(false);

        if (savedProducts.isEmpty()) {
            showEmptyState();
        } else {
            showSavedItems();
        }

        productAdapter.notifyDataSetChanged();
        Log.d(TAG, "✅ Mock saved items created: " + savedProducts.size());
    }

    // ✅ FIX: Helper method to create mock product with proper type handling
    private Product createMockProduct(Long id, String title, String description,
                                      Double price, String location, String condition, String status) {
        Product product = new Product();

        product.setId(id);
        product.setTitle(title);
        product.setDescription(description);

        // ✅ FIX: Handle BigDecimal to Double conversion
        product.setPrice(price);

        product.setLocation(location);

        // ✅ FIX: Handle ProductCondition to String conversion
        product.setCondition(condition);

        // ✅ FIX: Handle ProductStatus to String conversion
        product.setStatus(status);

        product.setImageUrl("https://via.placeholder.com/300x300");
        product.setPrimaryImageUrl("https://via.placeholder.com/300x300");
        product.setCreatedAt("2024-01-15T10:30:00");
        product.setUpdatedAt("2024-01-15T10:30:00");
        product.setUserId(2L);
        product.setCategoryId(1L);
        product.setCategoryName("Electronics");
        product.setViewCount(25);

        return product;
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getFormattedPrice());
        startActivity(intent);
    }

    private void showSavedItems() {
        if (rvSavedItems != null) rvSavedItems.setVisibility(View.VISIBLE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        if (rvSavedItems != null) rvSavedItems.setVisibility(View.GONE);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No saved items yet.\nSave products you're interested in!");
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh saved items when returning to activity
        loadSavedItems();
    }
}