// app/src/main/java/com/example/newtrade/ui/profile/MyListingsActivity.java
package com.example.newtrade.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ProductService;
import com.example.newtrade.api.AnalyticsService;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.AddProductActivity;
import com.example.newtrade.ui.product.EditProductActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyListingsActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "MyListingsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvMyListings;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddProduct;

    // Data & Adapters
    private final List<Product> myProducts = new ArrayList<>();
    private ProductAdapter productAdapter;
    private SharedPrefsManager prefsManager;
    private LinearLayout llEmptyStateContainer;

    // Services
    private ProductService productService;
    private AnalyticsService analyticsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);
        prefsManager = SharedPrefsManager.getInstance(this);
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        initServices();

        prefsManager = SharedPrefsManager.getInstance(this);
        loadMyProducts();
        loadDashboardAnalytics();

        debugApiResponse();

        Log.d(TAG, "MyListingsActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvMyListings = findViewById(R.id.rv_my_listings);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        fabAddProduct = findViewById(R.id.fab_add_product);
        llEmptyStateContainer = findViewById(R.id.ll_empty_state_container);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // ✅ KIỂM TRA NULL SAFETY
            String currentUserName = (prefsManager != null) ? prefsManager.getUserName() : null;
            String title = (currentUserName != null && !currentUserName.isEmpty())
                    ? currentUserName + "'s Listings"
                    : "My Listings";

            getSupportActionBar().setTitle(title);
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(myProducts, this);
        rvMyListings.setLayoutManager(new LinearLayoutManager(this));
        rvMyListings.setAdapter(productAdapter);
        rvMyListings.setHasFixedSize(true);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
        fabAddProduct.setOnClickListener(v -> openAddProduct());
    }

    private void initServices() {
        productService = ApiClient.getProductService();
        analyticsService = ApiClient.getAnalyticsService();
    }

    private void refreshData() {
        loadMyProducts();
        loadDashboardAnalytics();
    }

    // ===== DASHBOARD ANALYTICS =====

    private void loadDashboardAnalytics() {
        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) return;

        analyticsService.getDashboard().enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        Map<String, Object> dashboard = standardResponse.getData();
                        updateDashboardUI(dashboard);
                        Log.d(TAG, "✅ Dashboard analytics loaded");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Failed to load dashboard analytics", t);
            }
        });
    }

    private void updateDashboardUI(Map<String, Object> dashboard) {
        // Update toolbar subtitle with stats
        if (getSupportActionBar() != null && dashboard != null) {
            Object productsListed = dashboard.get("productsListed");
            Object productsSold = dashboard.get("productsSold");

            String subtitle = String.format("Listed: %s • Sold: %s",
                    productsListed != null ? productsListed.toString() : "0",
                    productsSold != null ? productsSold.toString() : "0"
            );
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    // ===== PRODUCT LOADING =====

    private void loadMyProducts() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            Log.w(TAG, "User not logged in");
            showEmptyState("Please log in to view your listings");
            return;
        }

        Log.d(TAG, "Loading products for user: " + userId);

        Call<StandardResponse<Map<String, Object>>> call = productService.getProductsByUser(userId);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {

                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        Map<String, Object> paginatedData = standardResponse.getData();
                        List<Map<String, Object>> productDataList = extractProductsFromPaginatedResponse(paginatedData);

                        if (productDataList != null && !productDataList.isEmpty()) {
                            parseAndDisplayProducts(productDataList);
                            Log.d(TAG, "✅ Loaded " + productDataList.size() + " user products");
                        } else {
                            Log.w(TAG, "No products found for user - showing empty state");
                            // ✅ THAY THẾ loadMockProducts() bằng empty state
                            showEmptyState("You haven't listed any products yet.\n\nTap the + button to create your first listing!");
                        }
                    } else {
                        Log.w(TAG, "❌ Failed to load user products: " + standardResponse.getMessage());
                        showError("Failed to load your products");
                        // ✅ THAY THẾ loadMockProducts() bằng empty state
                        showEmptyState("Unable to load your listings.\nPlease try again.");
                    }
                } else {
                    Log.w(TAG, "❌ User products API response not successful: " + response.code());
                    showError("Failed to load your products");
                    // ✅ THAY THẾ loadMockProducts() bằng empty state
                    showEmptyState("Unable to load your listings.\nPlease try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                Log.e(TAG, "❌ User products API call failed", t);
                showError("Network error while loading your products");
                // ✅ THAY THẾ loadMockProducts() bằng empty state
                showEmptyState("Network error.\nPlease check your connection and try again.");
            }
        });
    }

    private void showEmptyState(String message) {
        myProducts.clear();
        updateUI(); // This will show empty state container

        if (tvEmptyState != null) {
            tvEmptyState.setText(message);
        }

        Log.d(TAG, "Showing empty state: " + message);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractProductsFromPaginatedResponse(Map<String, Object> paginatedData) {
        if (paginatedData == null) {
            Log.w(TAG, "Paginated data is null");
            return null;
        }

        Log.d(TAG, "🔍 Paginated response keys: " + paginatedData.keySet());

        // Spring Boot pagination thường có field "content"
        Object contentObj = paginatedData.get("content");
        if (contentObj instanceof List) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) contentObj;
            Log.d(TAG, "✅ Found 'content' field with " + content.size() + " items");
            return content;
        }

        // Thử các field khác có thể có
        String[] possibleFields = {"items", "data", "products", "results"};
        for (String field : possibleFields) {
            Object fieldValue = paginatedData.get(field);
            if (fieldValue instanceof List) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) fieldValue;
                Log.d(TAG, "✅ Found '" + field + "' field with " + list.size() + " items");
                return list;
            }
        }

        Log.w(TAG, "❌ Could not find product list in paginated response");
        return new ArrayList<>();
    }

    private void debugApiResponse() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        // Test getMyProducts endpoint
        productService.getMyProducts(0, 10).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {

                Log.d(TAG, "🔍 DEBUG getMyProducts response:");
                Log.d(TAG, "  - Success: " + response.isSuccessful());
                Log.d(TAG, "  - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();
                    Log.d(TAG, "  - StandardResponse success: " + standardResponse.isSuccess());
                    Log.d(TAG, "  - StandardResponse message: " + standardResponse.getMessage());

                    Map<String, Object> data = standardResponse.getData();
                    if (data != null) {
                        Log.d(TAG, "  - Data keys: " + data.keySet());
                        for (String key : data.keySet()) {
                            Object value = data.get(key);
                            Log.d(TAG, "    - " + key + ": " + (value != null ? value.getClass().getSimpleName() : "null"));
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "🔍 DEBUG getMyProducts failed", t);
            }
        });
    }

    private void parseAndDisplayProducts(@NonNull List<Map<String, Object>> productDataList) {
        myProducts.clear();

        for (Map<String, Object> productData : productDataList) {
            try {
                Product product = new Product();

                // Basic info
                Object idObj = productData.get("id");
                if (idObj instanceof Number) {
                    product.setId(((Number) idObj).longValue());
                }

                product.setTitle((String) productData.get("title"));
                product.setDescription((String) productData.get("description"));
                product.setLocation((String) productData.get("location"));

                // Price
                Object priceObj = productData.get("price");
                if (priceObj instanceof Number) {
                    product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                }

                // Condition
                String conditionStr = (String) productData.get("condition");
                if (conditionStr != null) {
                    try {
                        Product.ProductCondition condition = Product.ProductCondition.valueOf(conditionStr);
                        product.setCondition(condition);
                    } catch (IllegalArgumentException e) {
                        product.setCondition(Product.ProductCondition.GOOD);
                    }
                }

                // Status
                String statusStr = (String) productData.get("status");
                if (statusStr != null) {
                    try {
                        Product.ProductStatus status = Product.ProductStatus.valueOf(statusStr);
                        product.setStatus(status);
                    } catch (IllegalArgumentException e) {
                        product.setStatus(Product.ProductStatus.AVAILABLE);
                    }
                }

                // Image URLs
                Object imageUrlsObj = productData.get("imageUrls");
                if (imageUrlsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> imageUrls = (List<String>) imageUrlsObj;
                    product.setImageUrls(imageUrls);
                } else if (imageUrlsObj instanceof String) {
                    List<String> imageUrls = new ArrayList<>();
                    imageUrls.add((String) imageUrlsObj);
                    product.setImageUrls(imageUrls);
                }

                // Additional fields
                Object viewCountObj = productData.get("viewCount");
                if (viewCountObj instanceof Number) {
                    product.setViewCount(((Number) viewCountObj).intValue());
                }

                product.setCreatedAt((String) productData.get("createdAt"));
                product.setCategoryName((String) productData.get("categoryName"));

                myProducts.add(product);

            } catch (Exception e) {
                Log.e(TAG, "Error parsing product data", e);
            }
        }

        updateUI();
    }



    private void updateUI() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        // Show/hide empty state container
        if (myProducts.isEmpty()) {
            rvMyListings.setVisibility(View.GONE);
            if (llEmptyStateContainer != null) {
                llEmptyStateContainer.setVisibility(View.VISIBLE);
            }
        } else {
            rvMyListings.setVisibility(View.VISIBLE);
            if (llEmptyStateContainer != null) {
                llEmptyStateContainer.setVisibility(View.GONE);
            }
        }

        Log.d(TAG, "UI updated with " + myProducts.size() + " products");
    }

    // ===== PRODUCT ACTIONS =====

    @Override
    public void onProductClick(Product product) {
        // Hiển thị dialog với 3 lựa chọn thay vì trực tiếp vào chi tiết
        showProductOptionsDialog(product);
    }

    private void showProductOptionsDialog(Product product) {
        String[] options = {
                "👁️ View Product",
                "✏️ Edit Product",
                "🗑️ Delete Product"
        };

        new AlertDialog.Builder(this)
                .setTitle(product.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // View Product
                            openProductDetail(product);
                            break;
                        case 1: // Edit Product
                            editProduct(product);
                            break;
                        case 2: // Delete Product
                            confirmDeleteProduct(product);
                            break;
                    }
                })
                .show();
    }

    @Override
    public void onProductLongClick(Product product) {
        showProductActionsMenu(product);
    }

    private void showProductActionsMenu(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String[] actions = {
                "✏️ Edit Product",
                "📊 View Analytics",
                "🔄 Change Status",
                "🗑️ Delete Product"
        };

        builder.setTitle(product.getTitle())
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            editProduct(product);
                            break;
                        case 1: // Analytics
                            viewProductAnalytics(product);
                            break;
                        case 2: // Change Status
                            showStatusMenu(product);
                            break;
                        case 3: // Delete
                            confirmDeleteProduct(product);
                            break;
                    }
                })
                .show();
    }

    private void showStatusMenu(Product product) {
        String[] statusOptions = {
                "🟢 Mark as Available",
                "🔴 Mark as Sold",
                "⏸️ Pause Listing",
                "📦 Archive"
        };

        new AlertDialog.Builder(this)
                .setTitle("Change Status")
                .setItems(statusOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Available
                            updateProductStatus(product, Product.ProductStatus.AVAILABLE);
                            break;
                        case 1: // Sold
                            markProductAsSold(product);
                            break;
                        case 2: // Paused
                            updateProductStatus(product, Product.ProductStatus.PAUSED);
                            break;
                        case 3: // Archive
                            updateProductStatus(product, Product.ProductStatus.ARCHIVED);
                            break;
                    }
                })
                .show();
    }

    private void markProductAsSold(Product product) {
        ApiClient.getProductService().markProductAsSold(product.getId())
                .enqueue(new Callback<StandardResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                           @NonNull Response<StandardResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            product.setStatus(Product.ProductStatus.SOLD);
                            productAdapter.notifyDataSetChanged();
                            showSuccess("Product marked as sold!");
                            Log.d(TAG, "✅ Product marked as sold: " + product.getTitle());
                        } else {
                            showError("Failed to mark as sold");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                        Log.e(TAG, "❌ Failed to mark as sold", t);
                        showError("Network error");
                    }
                });
    }

    private void viewProductAnalytics(Product product) {
        // Simple analytics dialog
        String message = String.format(
                "📊 Product Analytics\n\n" +
                        "👁️ Views: %d\n" +
                        "📅 Listed: %s\n" +
                        "📍 Location: %s\n" +
                        "🏷️ Status: %s",
                product.getViewCount() != null ? product.getViewCount() : 0,
                product.getCreatedAt() != null ? product.getCreatedAt() : "Recently",
                product.getLocation() != null ? product.getLocation() : "Unknown",
                product.getStatus() != null ? product.getStatus().getDisplayName() : "Unknown"
        );

        new AlertDialog.Builder(this)
                .setTitle("Analytics")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // ===== ANALYTICS =====

    private void showProductAnalytics(Product product) {
        analyticsService.getProductStats(product.getId()).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        Map<String, Object> stats = standardResponse.getData();
                        displayAnalyticsDialog(product, stats);
                    }
                } else {
                    showError("Failed to load analytics");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Failed to load product analytics", t);
                showError("Network error while loading analytics");
            }
        });
    }

    private void displayAnalyticsDialog(Product product, Map<String, Object> stats) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String message = String.format(
                "📊 Analytics for %s\n\n" +
                        "👁️ Views: %s\n" +
                        "💾 Saves: %s\n" +
                        "💰 Offers: %s\n" +
                        "💬 Conversations: %s",
                product.getTitle(),
                stats.get("viewCount"),
                stats.get("saveCount"),
                stats.get("offerCount"),
                stats.get("conversationCount")
        );

        builder.setTitle("Product Analytics")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // ===== EDIT PRODUCT =====

    private void editProduct(Product product) {
        Intent intent = new Intent(this, com.example.newtrade.ui.product.EditProductActivity.class);

        // Pass product data
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_description", product.getDescription());
        intent.putExtra("product_price", product.getPrice() != null ? product.getPrice().toString() : "");
        intent.putExtra("product_location", product.getLocation());
        intent.putExtra("product_condition", product.getCondition() != null ? product.getCondition().name() : "");
        intent.putExtra("product_category", product.getCategoryName());

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            intent.putExtra("product_image_url", product.getImageUrls().get(0));
        }

        startActivityForResult(intent, 100); // Request code for edit
        Log.d(TAG, "Opening edit for product: " + product.getTitle());
    }

    // ===== STATUS CHANGE =====

    private void showStatusChangeDialog(Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_status, null);
        RadioGroup rgStatus = dialogView.findViewById(R.id.rg_status);

        // Pre-select current status
        switch (product.getStatus()) {
            case AVAILABLE:
                rgStatus.check(R.id.rb_available);
                break;
            case SOLD:
                rgStatus.check(R.id.rb_sold);
                break;
            case PAUSED:
                rgStatus.check(R.id.rb_paused);
                break;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_update).setOnClickListener(v -> {
            Product.ProductStatus newStatus = getSelectedStatus(rgStatus);
            if (newStatus != product.getStatus()) {
                updateProductStatus(product, newStatus);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private Product.ProductStatus getSelectedStatus(RadioGroup rgStatus) {
        int selectedId = rgStatus.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_available) {
            return Product.ProductStatus.AVAILABLE;
        } else if (selectedId == R.id.rb_sold) {
            return Product.ProductStatus.SOLD;
        } else if (selectedId == R.id.rb_paused) {
            return Product.ProductStatus.PAUSED;
        }
        return Product.ProductStatus.AVAILABLE; // Default
    }

    // ===== FIX: UPDATE PRODUCT STATUS METHODS =====

    // Method chính nhận ProductStatus enum
    private void updateProductStatus(Product product, Product.ProductStatus newStatus) {
        // ✅ THÊM: Show loading với progress dialog
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Updating product status...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", newStatus.name());

        ApiClient.getProductService().updateProduct(product.getId(), updateData)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                           @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                        progressDialog.dismiss();

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // Update local product status
                            product.setStatus(newStatus);
                            productAdapter.notifyDataSetChanged();

                            // ✅ THÊM: Better success feedback
                            String statusName = getStatusDisplayName(newStatus);
                            showSuccessWithSnackbar("✅ Product status changed to " + statusName);

                        } else {
                            String error = response.body() != null ?
                                    response.body().getMessage() : "Failed to update status";
                            showErrorWithSnackbar("❌ " + error);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                        progressDialog.dismiss();
                        Log.e(TAG, "❌ Failed to update product status", t);
                        showErrorWithSnackbar("❌ Network error. Please try again.");
                    }
                });
    }

    private String getStatusDisplayName(Product.ProductStatus status) {
        switch (status) {
            case AVAILABLE: return "Available";
            case SOLD: return "Sold";
            case PAUSED: return "Paused";
            case ARCHIVED: return "Archived";
            default: return status.name();
        }
    }
    private void showSuccessWithSnackbar(String message) {
        com.google.android.material.snackbar.Snackbar snackbar =
                com.google.android.material.snackbar.Snackbar.make(
                        findViewById(android.R.id.content), message,
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_green_dark));
        snackbar.show();
    }
    private void showErrorWithSnackbar(String message) {
        com.google.android.material.snackbar.Snackbar snackbar =
                com.google.android.material.snackbar.Snackbar.make(
                        findViewById(android.R.id.content), message,
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_red_dark));
        snackbar.setAction("RETRY", v -> refreshData());
        snackbar.show();
    }

    // Overload method để hỗ trợ String (backward compatibility)
    private void updateProductStatus(Product product, String statusString) {
        try {
            Product.ProductStatus statusEnum = Product.ProductStatus.valueOf(statusString);
            updateProductStatus(product, statusEnum);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Unknown status string: " + statusString);
            showError("Invalid status: " + statusString);
        }
    }

    // ===== DELETE PRODUCT =====

    private void confirmDeleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + product.getTitle() + "\"?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProduct(Product product) {
        ApiClient.getProductService().deleteProduct(product.getId())
                .enqueue(new Callback<StandardResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                           @NonNull Response<StandardResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // Remove from list
                            myProducts.remove(product);
                            productAdapter.notifyDataSetChanged();
                            updateUI();
                            showSuccess("Product deleted successfully");
                            Log.d(TAG, "✅ Product deleted: " + product.getTitle());
                        } else {
                            showError("Failed to delete product");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                        Log.e(TAG, "❌ Failed to delete product", t);
                        showError("Network error");
                    }
                });
    }

    private void showSuccess(String message) {
        showSuccessWithSnackbar("✅ " + message);
        Log.d(TAG, "Success: " + message);
    }

    // ===== NAVIGATION =====

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        if (product.getPrice() != null) {
            intent.putExtra("product_price", product.getPrice().toString());
        }
        startActivity(intent);
    }

    private void openAddProduct() {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
    }

    // ===== UTILITY =====

    private void showError(String message) {
        showErrorWithSnackbar("❌ " + message);
        Log.e(TAG, "Error: " + message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh products when returning to this activity
        refreshData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Product was edited successfully, refresh list
            showSuccess("Product updated successfully");
            loadMyProducts(); // Refresh data
        }
    }
}