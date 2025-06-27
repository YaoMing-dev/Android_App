// app/src/main/java/com/example/newtrade/ui/profile/MyListingsActivity.java
// ✅ FIXED: Complete MyListingsActivity with FAB
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

public class MyListingsActivity extends AppCompatActivity {

    private static final String TAG = "MyListingsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvMyListings;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddProduct;

    // Data
    private ProductAdapter productAdapter;
    private List<Product> myProducts = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadMyListings();

        Log.d(TAG, "MyListingsActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMyListings = findViewById(R.id.rv_my_listings);
        swipeRefresh = findViewById(R.id.swipe_refresh);
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
        productAdapter = new ProductAdapter(myProducts, this::openProductDetail);
        rvMyListings.setLayoutManager(new LinearLayoutManager(this));
        rvMyListings.setAdapter(productAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadMyListings);

        // ✅ FIX: Setup FAB to launch AddProductActivity
        fabAddProduct.setOnClickListener(v -> {
            Log.d(TAG, "🎯 FAB clicked - launching AddProductActivity");
            Intent intent = new Intent(this, AddProductActivity.class);
            startActivity(intent);
        });
    }

    private void loadMyListings() {
        swipeRefresh.setRefreshing(true);

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Log.w(TAG, "No user ID available");
            createMockListings();
            return;
        }

        Log.d(TAG, "📋 Loading listings for user: " + userId);

        // TODO: Implement API call when ready
        createMockListings();
    }

    private void createMockListings() {
        myProducts.clear();

        // Mock listing 1
        Product product1 = new Product();
        product1.setId(101L);
        product1.setTitle("iPhone 14 Pro 128GB Deep Purple");
        product1.setDescription("Brand new, sealed in box. Still has warranty.");
        product1.setPrice(new BigDecimal("25000000"));
        product1.setImageUrl("https://example.com/iphone14.jpg");
        product1.setLocation("Ho Chi Minh City");
        product1.setCondition("NEW");
        myProducts.add(product1);

        // Mock listing 2
        Product product2 = new Product();
        product2.setId(102L);
        product2.setTitle("MacBook Pro M2 16 inch 512GB");
        product2.setDescription("Lightly used, excellent condition. Includes original charger and box.");
        product2.setPrice(new BigDecimal("55000000"));
        product2.setImageUrl("https://example.com/macbook.jpg");
        product2.setLocation("Ho Chi Minh City");
        product2.setCondition("LIKE_NEW");
        myProducts.add(product2);

        updateUI();
        Log.d(TAG, "✅ Mock listings created: " + myProducts.size());
    }

    private void updateUI() {
        swipeRefresh.setRefreshing(false);

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        if (myProducts.isEmpty()) {
            showEmptyState();
        } else {
            showListings();
        }
    }

    private void showListings() {
        if (rvMyListings != null) rvMyListings.setVisibility(View.VISIBLE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        if (rvMyListings != null) rvMyListings.setVisibility(View.GONE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh listings when returning from AddProduct
        loadMyListings();
    }
}