// app/src/main/java/com/example/newtrade/ui/profile/SavedItemsActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.product.adapter.SavedItemsAdapter;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavedItemsActivity extends AppCompatActivity implements SavedItemsAdapter.OnSavedItemActionListener {
    private static final String TAG = "SavedItemsActivity";

    // UI Components
    private Toolbar toolbar;
    private RecyclerView rvSavedItems;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    // Data
    private SavedItemsAdapter adapter;
    private List<Product> savedItems = new ArrayList<>();

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        prefsManager = new SharedPrefsManager(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadSavedItems();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvSavedItems = findViewById(R.id.rv_saved_items);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Saved Items");
        }
    }

    private void setupRecyclerView() {
        adapter = new SavedItemsAdapter(savedItems, this);
        rvSavedItems.setLayoutManager(new GridLayoutManager(this, 2));
        rvSavedItems.setAdapter(adapter);

        // Pagination scroll listener
        rvSavedItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreSavedItems();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshSavedItems);
    }

    private void loadSavedItems() {
        if (isLoading) return;

        isLoading = true;
        showLoading(currentPage == 0);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getSavedProducts(currentPage, Constants.DEFAULT_PAGE_SIZE, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;
                hideLoading();

                handleSavedItemsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                isLoading = false;
                hideLoading();

                Log.e(TAG, "Failed to load saved items", t);
                if (savedItems.isEmpty()) {
                    showEmptyState();
                }
                Toast.makeText(SavedItemsActivity.this, "Failed to load saved items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleSavedItemsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null) {
                    List<Product> newItems = new ArrayList<>();
                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            newItems.add(product);
                        }
                    }

                    int oldSize = savedItems.size();
                    savedItems.addAll(newItems);
                    adapter.notifyItemRangeInserted(oldSize, newItems.size());

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : newItems.size() < Constants.DEFAULT_PAGE_SIZE;
                }

                if (savedItems.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                }

            } else {
                Log.e(TAG, "Failed to load saved items: " + response.message());
                if (savedItems.isEmpty()) {
                    showEmptyState();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing saved items response", e);
            if (savedItems.isEmpty()) {
                showEmptyState();
            }
        }
    }

    private Product parseProductFromMap(Map<String, Object> productMap) {
        // TODO: Implement proper product parsing (reuse from other activities)
        try {
            Product product = new Product();
            product.setId(getLongFromMap(productMap, "id"));
            product.setTitle((String) productMap.get("title"));
            product.setDescription((String) productMap.get("description"));

            // Parse price
            Object priceObj = productMap.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(java.math.BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }

            product.setLocation((String) productMap.get("location"));
            product.setCreatedAt((String) productMap.get("createdAt"));

            return product;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing product", e);
            return null;
        }
    }

    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private void loadMoreSavedItems() {
        if (!isLoading && !isLastPage) {
            currentPage++;
            loadSavedItems();
        }
    }

    private void refreshSavedItems() {
        currentPage = 0;
        isLastPage = false;
        savedItems.clear();
        adapter.notifyDataSetChanged();
        loadSavedItems();
    }

    private void showLoading(boolean isInitialLoad) {
        if (isInitialLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
        swipeRefresh.setRefreshing(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void showEmptyState() {
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText("No saved items yet");
        rvSavedItems.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        tvEmpty.setVisibility(View.GONE);
        rvSavedItems.setVisibility(View.VISIBLE);
    }

    // SavedItemsAdapter.OnSavedItemActionListener implementation
    @Override
    public void onItemClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public void onRemoveClick(Product product) {
        removeSavedItem(product);
    }

    private void removeSavedItem(Product product) {
        Call<StandardResponse<Void>> call = ApiClient.getProductService()
                .unsaveProduct(product.getId(), prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                   @NonNull Response<StandardResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Remove from list
                    int position = savedItems.indexOf(product);
                    if (position != -1) {
                        savedItems.remove(position);
                        adapter.notifyItemRemoved(position);
                    }

                    if (savedItems.isEmpty()) {
                        showEmptyState();
                    }

                    Toast.makeText(SavedItemsActivity.this, "Item removed from saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SavedItemsActivity.this, "Failed to remove item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to remove saved item", t);
                Toast.makeText(SavedItemsActivity.this, "Failed to remove item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}