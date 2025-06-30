// app/src/main/java/com/example/newtrade/ui/product/CategoryProductsActivity.java
package com.example.newtrade.ui.product;

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
import com.example.newtrade.ui.product.adapter.ProductGridAdapter;
import com.example.newtrade.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryProductsActivity extends AppCompatActivity implements ProductGridAdapter.OnProductClickListener {
    private static final String TAG = "CategoryProductsActivity";

    // UI Components
    private Toolbar toolbar;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    // Data
    private ProductGridAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private Long categoryId;
    private String categoryName;

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        // Get category info from intent
        categoryId = getIntent().getLongExtra(Constants.BUNDLE_CATEGORY_ID, -1);
        categoryName = getIntent().getStringExtra("categoryName");

        if (categoryId == -1) {
            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadProducts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvProducts = findViewById(R.id.rv_products);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName != null ? categoryName : "Category Products");
        }
    }

    private void setupRecyclerView() {
        adapter = new ProductGridAdapter(products, this);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(adapter);

        // Pagination scroll listener
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        loadMoreProducts();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshProducts);
    }

    private void loadProducts() {
        if (isLoading) return;

        isLoading = true;
        showLoading(currentPage == 0);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getCategoryProducts(categoryId, currentPage, Constants.DEFAULT_PAGE_SIZE,
                        null, null, null, Constants.SORT_NEWEST, null);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;
                hideLoading();

                handleProductsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                isLoading = false;
                hideLoading();

                Log.e(TAG, "Failed to load products", t);
                if (products.isEmpty()) {
                    showEmptyState();
                }
                Toast.makeText(CategoryProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleProductsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null) {
                    List<Product> newProducts = new ArrayList<>();
                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            newProducts.add(product);
                        }
                    }

                    int oldSize = products.size();
                    products.addAll(newProducts);
                    adapter.notifyItemRangeInserted(oldSize, newProducts.size());

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : newProducts.size() < Constants.DEFAULT_PAGE_SIZE;
                }

                if (products.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                }

            } else {
                Log.e(TAG, "Failed to load products: " + response.message());
                if (products.isEmpty()) {
                    showEmptyState();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing products response", e);
            if (products.isEmpty()) {
                showEmptyState();
            }
        }
    }

    private Product parseProductFromMap(Map<String, Object> productMap) {
        // TODO: Use same parsing logic as in SearchFragment
        // This is a simplified version
        try {
            Product product = new Product();
            product.setId(getLongFromMap(productMap, "id"));
            product.setTitle((String) productMap.get("title"));
            product.setDescription((String) productMap.get("description"));
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

    private void loadMoreProducts() {
        if (!isLoading && !isLastPage) {
            currentPage++;
            loadProducts();
        }
    }

    private void refreshProducts() {
        currentPage = 0;
        isLastPage = false;
        products.clear();
        adapter.notifyDataSetChanged();
        loadProducts();
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
        tvEmpty.setText("No products found in this category");
    }

    private void hideEmptyState() {
        tvEmpty.setVisibility(View.GONE);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
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