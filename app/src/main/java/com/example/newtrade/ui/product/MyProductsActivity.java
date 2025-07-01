// app/src/main/java/com/example/newtrade/ui/product/MyProductsActivity.java
package com.example.newtrade.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProductsActivity extends AppCompatActivity {
    private static final String TAG = "MyProductsActivity";

    // UI Components
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;
    private View loadingView, contentView;

    // Data
    private List<Product> products = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    // State
    private String currentTab = "ACTIVE"; // ACTIVE, SOLD, PAUSED
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);

        initViews();
        initUtils();
        setupToolbar();
        setupListeners();
        setupRecyclerView();
        setupTabs();

        loadMyProducts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        rvProducts = findViewById(R.id.rv_products);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        fabAdd = findViewById(R.id.fab_add);
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
            getSupportActionBar().setTitle("My Products");
        }
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
        fabAdd.setOnClickListener(v -> openAddProduct());
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Create MyProductsAdapter with action buttons (edit, delete, mark sold, etc.)
        // MyProductsAdapter adapter = new MyProductsAdapter(products, this);
        // rvProducts.setAdapter(adapter);

        // Pagination scroll listener
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && !isLastPage && layoutManager != null && dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        loadMoreProducts();
                    }
                }
            }
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Active"));
        tabLayout.addTab(tabLayout.newTab().setText("Sold"));
        tabLayout.addTab(tabLayout.newTab().setText("Paused"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "ACTIVE";
                        break;
                    case 1:
                        currentTab = "SOLD";
                        break;
                    case 2:
                        currentTab = "PAUSED";
                        break;
                }
                refreshData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // FR-2.2.1: View/edit/delete listings from user dashboard
    private void loadMyProducts() {
        if (isLoading) return;

        isLoading = true;
        if (currentPage == 0) {
            showLoadingState();
        }

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getUserProducts(prefsManager.getUserId(), currentPage, Constants.DEFAULT_PAGE_SIZE);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleProductsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleLoadingError(t);
            }
        });
    }

    private void loadMoreProducts() {
        if (isLoading || isLastPage) return;

        currentPage++;
        loadMyProducts();
    }

    private void refreshData() {
        currentPage = 0;
        isLastPage = false;
        products.clear();
        loadMyProducts();
    }

    @SuppressWarnings("unchecked")
    private void handleProductsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        isLoading = false;
        swipeRefresh.setRefreshing(false);

        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null) {
                    int oldSize = products.size();

                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            // Filter by current tab
                            if (matchesCurrentTab(product)) {
                                products.add(product);
                            }
                        }
                    }

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : true;

                    // Update UI
                    showContentState();

                    if (products.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        // TODO: Notify adapter
                        // adapter.notifyItemRangeInserted(oldSize, products.size() - oldSize);
                    }
                }
            } else {
                handleLoadingError(new Exception("Failed to load products"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing products response", e);
            handleLoadingError(e);
        }
    }

    private boolean matchesCurrentTab(Product product) {
        switch (currentTab) {
            case "ACTIVE":
                return product.isAvailable();
            case "SOLD":
                return product.isSold();
            case "PAUSED":
                return Product.ProductStatus.PAUSED.equals(product.getStatus());
            default:
                return true;
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

            Object viewCount = productMap.get("viewCount");
            if (viewCount instanceof Number) {
                product.setViewCount(((Number) viewCount).intValue());
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

    // Product action methods
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    public void onEditProduct(Product product) {
        Intent intent = new Intent(this, AddProductActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        intent.putExtra("isEdit", true);
        startActivity(intent);
    }

    // FR-2.2.2: Listing status options: Available, Sold, Paused
    public void onToggleProductStatus(Product product) {
        String[] statusOptions;
        Product.ProductStatus[] statusValues;

        if (product.isAvailable()) {
            statusOptions = new String[]{"Mark as Sold", "Pause Listing"};
            statusValues = new Product.ProductStatus[]{Product.ProductStatus.SOLD, Product.ProductStatus.PAUSED};
        } else if (product.isSold()) {
            statusOptions = new String[]{"Mark as Available", "Pause Listing"};
            statusValues = new Product.ProductStatus[]{Product.ProductStatus.AVAILABLE, Product.ProductStatus.PAUSED};
        } else {
            statusOptions = new String[]{"Mark as Available", "Mark as Sold"};
            statusValues = new Product.ProductStatus[]{Product.ProductStatus.AVAILABLE, Product.ProductStatus.SOLD};
        }

        new AlertDialog.Builder(this)
                .setTitle("Change Status")
                .setItems(statusOptions, (dialog, which) -> {
                    updateProductStatus(product, statusValues[which]);
                })
                .show();
    }

    private void updateProductStatus(Product product, Product.ProductStatus newStatus) {
        Map<String, String> statusData = new HashMap<>();
        statusData.put("status", newStatus.name());

        Call<StandardResponse<Product>> call = ApiClient.getProductService()
                .updateProductStatus(product.getId(), statusData, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Product>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Product>> call,
                                   @NonNull Response<StandardResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MyProductsActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                    refreshData();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to update status";
                    Toast.makeText(MyProductsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Product>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to update product status", t);
                Toast.makeText(MyProductsActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onDeleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + product.getTitle() + "\"?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .show();
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
                    refreshData();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to delete product";
                    Toast.makeText(MyProductsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to delete product", t);
                Toast.makeText(MyProductsActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onViewAnalytics(Product product) {
        Intent intent = new Intent(this, ProductAnalyticsActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    private void openAddProduct() {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
    }

    private void handleLoadingError(Throwable t) {
        isLoading = false;
        swipeRefresh.setRefreshing(false);

        Log.e(TAG, "Failed to load products", t);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, NetworkUtils.getNetworkErrorMessage(t), Toast.LENGTH_SHORT).show();
        }

        if (products.isEmpty()) {
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
        rvProducts.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);

        String emptyMessage;
        switch (currentTab) {
            case "SOLD":
                emptyMessage = "No sold items\n\nItems you've sold will appear here";
                break;
            case "PAUSED":
                emptyMessage = "No paused listings\n\nPaused listings will appear here";
                break;
            default:
                emptyMessage = "No active listings\n\nTap + to create your first listing";
                break;
        }
        tvEmpty.setText(emptyMessage);
    }

    private void hideEmptyState() {
        rvProducts.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_products, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_add) {
            openAddProduct();
            return true;
        } else if (itemId == R.id.action_analytics) {
            // TODO: Open overall analytics
            Toast.makeText(this, "Overall analytics - Coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from edit/add
        refreshData();
    }
}