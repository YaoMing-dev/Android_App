// app/src/main/java/com/example/newtrade/ui/product/MyProductsActivity.java
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.adapter.MyProductsAdapter;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProductsActivity extends AppCompatActivity implements MyProductsAdapter.OnProductActionListener {
    private static final String TAG = "MyProductsActivity";

    // UI Components
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    // Data
    private MyProductsAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private String currentStatus = "all";

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);

        prefsManager = new SharedPrefsManager(this);

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupListeners();
        loadProducts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        rvProducts = findViewById(R.id.rv_products);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Products");
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Available"));
        tabLayout.addTab(tabLayout.newTab().setText("Sold"));
        tabLayout.addTab(tabLayout.newTab().setText("Archived"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentStatus = "all";
                        break;
                    case 1:
                        currentStatus = "AVAILABLE";
                        break;
                    case 2:
                        currentStatus = "SOLD";
                        break;
                    case 3:
                        currentStatus = "ARCHIVED";
                        break;
                }
                refreshProducts();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new MyProductsAdapter(products, this);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);

        // Pagination scroll listener
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
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

        String status = currentStatus.equals("all") ? null : currentStatus;
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getMyProducts(currentPage, Constants.DEFAULT_PAGE_SIZE, status, prefsManager.getUserId());

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
                Toast.makeText(MyProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
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
        // TODO: Implement proper product parsing
        // This is a simplified version
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

            product.setCreatedAt((String) productMap.get("createdAt"));

            // Parse status
            String statusStr = (String) productMap.get("status");
            if (statusStr != null) {
                try {
                    product.setStatus(Product.ProductStatus.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    product.setStatus(Product.ProductStatus.AVAILABLE);
                }
            }

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
        String emptyMessage;
        switch (currentStatus) {
            case "AVAILABLE":
                emptyMessage = "No available products";
                break;
            case "SOLD":
                emptyMessage = "No sold products";
                break;
            case "ARCHIVED":
                emptyMessage = "No archived products";
                break;
            default:
                emptyMessage = "No products yet";
                break;
        }
        tvEmpty.setText(emptyMessage);
    }

    private void hideEmptyState() {
        tvEmpty.setVisibility(View.GONE);
    }

    // MyProductsAdapter.OnProductActionListener implementation
    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Product product) {
        Intent intent = new Intent(this, AddProductActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        intent.putExtra("editMode", true);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + product.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onMarkSoldClick(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Sold")
                .setMessage("Mark \"" + product.getTitle() + "\" as sold?")
                .setPositiveButton("Mark Sold", (dialog, which) -> markProductAsSold(product))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onArchiveClick(Product product) {
        archiveProduct(product);
    }

    @Override
    public void onRestoreClick(Product product) {
        restoreProduct(product);
    }

    @Override
    public void onViewAnalyticsClick(Product product) {
        // TODO: Open analytics screen
        Toast.makeText(this, "Analytics coming soon", Toast.LENGTH_SHORT).show();
    }

    private void deleteProduct(Product product) {
        Call<StandardResponse<Void>> call = ApiClient.getProductService()
                .deleteProduct(product.getId(), prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                   @NonNull Response<StandardResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MyProductsActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
                    removeProductFromList(product);
                } else {
                    Toast.makeText(MyProductsActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to delete product", t);
                Toast.makeText(MyProductsActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markProductAsSold(Product product) {
        Call<StandardResponse<Void>> call = ApiClient.getProductService()
                .markProductAsSold(product.getId(), prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                   @NonNull Response<StandardResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MyProductsActivity.this, "Product marked as sold", Toast.LENGTH_SHORT).show();
                    product.setStatus(Product.ProductStatus.SOLD);
                    updateProductInList(product);
                } else {
                    Toast.makeText(MyProductsActivity.this, "Failed to mark as sold", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to mark product as sold", t);
                Toast.makeText(MyProductsActivity.this, "Failed to mark as sold", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void archiveProduct(Product product) {
        Call<StandardResponse<Void>> call = ApiClient.getProductService()
                .archiveProduct(product.getId(), prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                   @NonNull Response<StandardResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MyProductsActivity.this, "Product archived", Toast.LENGTH_SHORT).show();
                    product.setStatus(Product.ProductStatus.ARCHIVED);
                    updateProductInList(product);
                } else {
                    Toast.makeText(MyProductsActivity.this, "Failed to archive product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to archive product", t);
                Toast.makeText(MyProductsActivity.this, "Failed to archive product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreProduct(Product product) {
        Call<StandardResponse<Void>> call = ApiClient.getProductService()
                .restoreArchivedProduct(product.getId(), prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                   @NonNull Response<StandardResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MyProductsActivity.this, "Product restored", Toast.LENGTH_SHORT).show();
                    product.setStatus(Product.ProductStatus.AVAILABLE);
                    updateProductInList(product);
                } else {
                    Toast.makeText(MyProductsActivity.this, "Failed to restore product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to restore product", t);
                Toast.makeText(MyProductsActivity.this, "Failed to restore product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeProductFromList(Product product) {
        int position = products.indexOf(product);
        if (position != -1) {
            products.remove(position);
            adapter.notifyItemRemoved(position);
        }

        if (products.isEmpty()) {
            showEmptyState();
        }
    }

    private void updateProductInList(Product product) {
        int position = products.indexOf(product);
        if (position != -1) {
            adapter.notifyItemChanged(position);
        }
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