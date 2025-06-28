// app/src/main/java/com/example/newtrade/ui/profile/MyListingsActivity.java
package com.example.newtrade.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.models.Product;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class MyListingsActivity extends AppCompatActivity {

    private static final String TAG = "MyListingsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvMyListings;
    private SwipeRefreshLayout swipeRefresh;

    // Data
    private ProductAdapter productAdapter;
    private List<Product> myProducts = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    // ✅ FIX: XỎA @Override không đúng, chỉ để override khi cần
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        initViews();
        initData();
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
    }

    private void initData() {
        prefsManager = new SharedPrefsManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Listings");
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(myProducts, product -> {
            // Handle product click
            Log.d(TAG, "Product clicked: " + product.getTitle());
        });

        rvMyListings.setLayoutManager(new LinearLayoutManager(this));
        rvMyListings.setAdapter(productAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadMyListings);
    }

    private void loadMyListings() {
        swipeRefresh.setRefreshing(true);

        // TODO: Load from API
        // For now, show empty state
        myProducts.clear();
        productAdapter.notifyDataSetChanged();

        swipeRefresh.setRefreshing(false);
        Log.d(TAG, "My listings loaded: " + myProducts.size());
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