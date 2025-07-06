// app/src/main/java/com/example/newtrade/ui/profile/SavedItemsActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavedItemsActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "SavedItemsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvSavedItems;
    private LinearLayout layoutEmptyState;

    // Data
    private ProductAdapter productAdapter;
    private List<Product> savedItems = new ArrayList<>();
    private SharedPrefsManager prefsManager;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

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
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvSavedItems = findViewById(R.id.rv_saved_items);
        layoutEmptyState = findViewById(R.id.tv_empty_state);

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
        productAdapter = new ProductAdapter(savedItems, this);
        rvSavedItems.setLayoutManager(new GridLayoutManager(this, 2));
        rvSavedItems.setAdapter(productAdapter);

        rvSavedItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && hasMoreData) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= Constants.DEFAULT_PAGE_SIZE) {
                        loadMoreSavedItems();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 0;
            hasMoreData = true;
            loadSavedItems();
        });
    }

    private void loadSavedItems() {
        if (isLoading) return;

        isLoading = true;
        if (currentPage == 0) {
            swipeRefresh.setRefreshing(true);
        }

        // ✅ SỬA: Không cần userId parameter, backend sẽ dùng User-ID header
        ApiClient.getProductService().getSavedItems(currentPage, Constants.DEFAULT_PAGE_SIZE)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        isLoading = false;
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            handleSavedItemsResponse(response.body().getData());
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to load saved items";
                            Log.e(TAG, "Error loading saved items: " + errorMsg);
                            Toast.makeText(SavedItemsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            updateUI();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        isLoading = false;
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "Network error loading saved items", t);
                        Toast.makeText(SavedItemsActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                        updateUI();
                    }
                });
    }

    private void loadMoreSavedItems() {
        currentPage++;
        loadSavedItems();
    }

    private void handleSavedItemsResponse(Map<String, Object> data) {
        try {
            if (data != null && data.containsKey("content")) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Product>>(){}.getType();
                List<Product> newItems = gson.fromJson(gson.toJson(data.get("content")), listType);

                if (currentPage == 0) {
                    savedItems.clear();
                }

                if (newItems != null && !newItems.isEmpty()) {
                    savedItems.addAll(newItems);
                    hasMoreData = newItems.size() >= Constants.DEFAULT_PAGE_SIZE;
                } else {
                    hasMoreData = false;
                }

                Log.d(TAG, "Loaded " + (newItems != null ? newItems.size() : 0) + " saved items");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing saved items response", e);
        }

        updateUI();
    }

    private void updateUI() {
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        if (savedItems.isEmpty()) {
            rvSavedItems.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvSavedItems.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    @Override
    public void onProductLongClick(Product product) {
        Log.d(TAG, "Long clicked on saved product: " + product.getTitle());
    }

    private void showError(String message) {
        Log.e(TAG, "Error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentPage = 0;
        hasMoreData = true;
        loadSavedItems();
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