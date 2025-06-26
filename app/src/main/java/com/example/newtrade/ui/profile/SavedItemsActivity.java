// app/src/main/java/com/example/newtrade/ui/profile/SavedItemsActivity.java
package com.example.newtrade.ui.profile;

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
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Create mock data for testing
        createMockSavedItems();

        // TODO: Load from API when SavedItemService is available
        // loadSavedItemsFromAPI();
    }

    private void createMockSavedItems() {
        savedProducts.clear();

        // Mock saved item 1
        Product product1 = new Product();
        product1.setId(10L);
        product1.setTitle("Samsung Galaxy S23");
        product1.setDescription("Samsung Galaxy S23 Ultra 256GB");
        product1.setPrice(new BigDecimal("25000000"));
        product1.setCondition(Product.ProductCondition.LIKE_NEW);
        product1.setLocation("Hanoi");
        product1.setStatus(Product.ProductStatus.AVAILABLE);
        savedProducts.add(product1);

        // Mock saved item 2
        Product product2 = new Product();
        product2.setId(11L);
        product2.setTitle("iPad Pro M2");
        product2.setDescription("iPad Pro 12.9-inch M2 2022");
        product2.setPrice(new BigDecimal("28000000"));
        product2.setCondition(Product.ProductCondition.GOOD);
        product2.setLocation("Da Nang");
        product2.setStatus(Product.ProductStatus.AVAILABLE);
        savedProducts.add(product2);

        // Mock saved item 3
        Product product3 = new Product();
        product3.setId(12L);
        product3.setTitle("Nike Air Jordan 1");
        product3.setDescription("Nike Air Jordan 1 High OG Chicago");
        product3.setPrice(new BigDecimal("3500000"));
        product3.setCondition(Product.ProductCondition.GOOD);
        product3.setLocation("Ho Chi Minh City");
        product3.setStatus(Product.ProductStatus.AVAILABLE);
        savedProducts.add(product3);

        updateUI();
    }

    private void updateUI() {
        swipeRefresh.setRefreshing(false);
        productAdapter.notifyDataSetChanged();

        if (savedProducts.isEmpty()) {
            rvSavedItems.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvSavedItems.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getPrice().toString());
        startActivity(intent);
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