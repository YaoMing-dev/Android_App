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
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;

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
    private Toolbar toolbar;
    private RecyclerView rvSavedItems;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View loadingView, contentView;

    // Data
    private List<Product> savedProducts = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        initViews();
        initUtils();
        setupToolbar();
        setupListeners();
        setupRecyclerView();

        loadSavedItems();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvSavedItems = findViewById(R.id.rv_saved_items);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        loadingView = findViewById(R.id.view_loading);
        contentView = findViewById(R.id.view_content);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Saved Items");
        }
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    private void setupRecyclerView() {
        rvSavedItems.setLayoutManager(new GridLayoutManager(this, 2));

        // TODO: Create ProductGridAdapter
        // ProductGridAdapter adapter = new ProductGridAdapter(savedProducts, this::onProductClick);
        // rvSavedItems.setAdapter(adapter);

        // Pagination scroll listener
        rvSavedItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && !isLastPage && layoutManager != null && dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        loadMoreSavedItems();
                    }
                }
            }
        });
    }

    // FR-9.2.1: View saved items
    private void loadSavedItems() {
        if (isLoading) return;

        isLoading = true;
        if (currentPage == 0) {
            showLoadingState();
        }

        // TODO: Create saved items API endpoint
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getUserService()
                .getSavedItems(currentPage, Constants.DEFAULT_PAGE_SIZE, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleSavedItemsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleLoadingError(t);
            }
        });
    }

    private void loadMoreSavedItems() {
        if (isLoading || isLastPage) return;

        currentPage++;
        loadSavedItems();
    }

    private void refreshData() {
        currentPage = 0;
        isLastPage = false;
        savedProducts.clear();
        loadSavedItems();
    }

    @SuppressWarnings("unchecked")
    private void handleSavedItemsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        isLoading = false;
        swipeRefresh.setRefreshing(false);

        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null) {
                    int oldSize = savedProducts.size();

                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            savedProducts.add(product);
                        }
                    }

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : true;

                    // Update UI
                    showContentState();

                    if (savedProducts.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        // TODO: Notify adapter
                        // adapter.notifyItemRangeInserted(oldSize, savedProducts.size() - oldSize);
                    }
                }
            } else {
                handleLoadingError(new Exception("Failed to load saved items"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing saved items response", e);
            handleLoadingError(e);
        }
    }

    private Product parseProductFromMap(Map<String, Object> productMap) {
        try {
            Product product = new Product();
            product.setId(((Number) productMap.get("id")).longValue());
            product.setTitle((String) productMap.get("title"));
            product.setDescription((String) productMap.get("description"));
            product.setLocation((String) productMap.get("location"));

            Object price = productMap.get("price");
            if (price instanceof Number) {
                product.setPrice(new BigDecimal(price.toString()));
            }

            String conditionStr = (String) productMap.get("condition");
            if (conditionStr != null) {
                product.setCondition(Product.ProductCondition.fromString(conditionStr));
            }

            String statusStr = (String) productMap.get("status");
            if (statusStr != null) {
                product.setStatus(Product.ProductStatus.fromString(statusStr));
            }

            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) productMap.get("imageUrls");
            if (imageUrls != null) {
                product.setImageUrls(imageUrls);
            }

            return product;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing product", e);
            return null;
        }
    }

    private void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    private void handleLoadingError(Throwable t) {
        isLoading = false;
        swipeRefresh.setRefreshing(false);

        Log.e(TAG, "Failed to load saved items", t);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showErrorToast("No internet connection");
        } else {
            showErrorToast(NetworkUtils.getNetworkErrorMessage(t));
        }

        if (savedProducts.isEmpty()) {
            showEmptyState();
        } else {
            showContentState();
        }
    }

    private void showLoadingState() {
        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
    }

    private void showContentState() {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        rvSavedItems.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText("No saved items\n\nItems you save will appear here");
    }

    private void hideEmptyState() {
        rvSavedItems.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}