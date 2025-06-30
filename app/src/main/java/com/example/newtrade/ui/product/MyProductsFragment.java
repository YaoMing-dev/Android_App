// app/src/main/java/com/example/newtrade/ui/product/MyProductsFragment.java
package com.example.newtrade.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.adapter.MyProductsAdapter;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProductsFragment extends Fragment implements MyProductsAdapter.OnProductActionListener {
    private static final String TAG = "MyProductsFragment";

    // UI Components
    private TabLayout tabLayout;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    // Data and Adapter
    private MyProductsAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    // State
    private String currentStatus = "ALL";
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = new SharedPrefsManager(requireContext());

        initViews(view);
        setupTabLayout();
        setupRecyclerView();
        setupListeners();

        loadProducts();
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        rvProducts = view.findViewById(R.id.rv_products);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        fabAdd = view.findViewById(R.id.fab_add);
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Available"));
        tabLayout.addTab(tabLayout.newTab().setText("Sold"));
        tabLayout.addTab(tabLayout.newTab().setText("Paused"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentStatus = "ALL"; break;
                    case 1: currentStatus = "AVAILABLE"; break;
                    case 2: currentStatus = "SOLD"; break;
                    case 3: currentStatus = "PAUSED"; break;
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
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);

        // Pagination
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                        loadMoreProducts();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshProducts);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddProductActivity.class);
            startActivity(intent);
        });
    }

    private void loadProducts() {
        if (isLoading) return;

        setLoading(true);
        currentPage = 0;

        Long userId = prefsManager.getUserId();
        String status = currentStatus.equals("ALL") ? null : currentStatus;

        ApiClient.getProductService().getUserProducts(userId, currentPage, 20, status)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                handleProductsResponse(apiResponse.getData(), false);
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to load products");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "Failed to load products", t);
                        showError("Network error");
                    }
                });
    }

    private void loadMoreProducts() {
        if (isLoading || isLastPage) return;

        setLoading(true);
        currentPage++;

        Long userId = prefsManager.getUserId();
        String status = currentStatus.equals("ALL") ? null : currentStatus;

        ApiClient.getProductService().getUserProducts(userId, currentPage, 20, status)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                handleProductsResponse(apiResponse.getData(), true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Failed to load more products", t);
                    }
                });
    }

    private void handleProductsResponse(Map<String, Object> data, boolean isLoadMore) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productsData = (List<Map<String, Object>>) data.get("content");

            List<Product> newProducts = new ArrayList<>();
            if (productsData != null) {
                for (Map<String, Object> productData : productsData) {
                    Product product = parseProductFromMap(productData);
                    newProducts.add(product);
                }
            }

            if (isLoadMore) {
                int startPosition = products.size();
                products.addAll(newProducts);
                adapter.notifyItemRangeInserted(startPosition, newProducts.size());
            } else {
                products.clear();
                products.addAll(newProducts);
                adapter.notifyDataSetChanged();
            }

            // Check if last page
            Boolean isLast = (Boolean) data.get("last");
            isLastPage = isLast != null && isLast;

            updateEmptyState();

        } catch (Exception e) {
            Log.e(TAG, "Error parsing products response", e);
            showError("Error loading products");
        }
    }

    private Product parseProductFromMap(Map<String, Object> data) {
        Product product = new Product();

        if (data.get("id") != null) {
            product.setId(Long.valueOf(data.get("id").toString()));
        }
        product.setTitle((String) data.get("title"));
        product.setDescription((String) data.get("description"));

        if (data.get("price") != null) {
            product.setPrice(new java.math.BigDecimal(data.get("price").toString()));
        }

        String conditionStr = (String) data.get("condition");
        if (conditionStr != null) {
            try {
                product.setCondition(Product.ProductCondition.valueOf(conditionStr));
            } catch (IllegalArgumentException e) {
                product.setCondition(Product.ProductCondition.GOOD);
            }
        }

        product.setLocation((String) data.get("location"));

        String statusStr = (String) data.get("status");
        if (statusStr != null) {
            try {
                product.setStatus(Product.ProductStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                product.setStatus(Product.ProductStatus.AVAILABLE);
            }
        }

        if (data.get("viewCount") != null) {
            product.setViewCount(Integer.valueOf(data.get("viewCount").toString()));
        }

        product.setCreatedAt((String) data.get("createdAt"));
        product.setUpdatedAt((String) data.get("updatedAt"));

        // Parse images
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> imagesData = (List<Map<String, Object>>) data.get("images");
        if (imagesData != null) {
            List<com.example.newtrade.models.ProductImage> images = new ArrayList<>();
            for (Map<String, Object> imageData : imagesData) {
                com.example.newtrade.models.ProductImage image = new com.example.newtrade.models.ProductImage();
                image.setImageUrl((String) imageData.get("imageUrl"));
                if (imageData.get("displayOrder") != null) {
                    image.setDisplayOrder(Integer.valueOf(imageData.get("displayOrder").toString()));
                }
                images.add(image);
            }
            product.setImages(images);
        }

        return product;
    }

    private void refreshProducts() {
        currentPage = 0;
        isLastPage = false;
        loadProducts();
    }

    private void updateEmptyState() {
        if (products.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);

            switch (currentStatus) {
                case "ALL":
                    tvEmpty.setText("You haven't listed any products yet.\nTap the + button to get started!");
                    break;
                case "AVAILABLE":
                    tvEmpty.setText("No available products");
                    break;
                case "SOLD":
                    tvEmpty.setText("No sold products");
                    break;
                case "PAUSED":
                    tvEmpty.setText("No paused products");
                    break;
            }
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        if (currentPage == 0) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // MyProductsAdapter.OnProductActionListener implementations
    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("is_owner", true);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Product product) {
        Intent intent = new Intent(getContext(), EditProductActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    @Override
    public void onStatusChange(Product product, Product.ProductStatus newStatus) {
        updateProductStatus(product, newStatus);
    }

    @Override
    public void onDeleteClick(Product product) {
        showDeleteConfirmDialog(product);
    }

    @Override
    public void onAnalyticsClick(Product product) {
        Intent intent = new Intent(getContext(), ProductAnalyticsActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    private void updateProductStatus(Product product, Product.ProductStatus newStatus) {
        Long userId = prefsManager.getUserId();

        java.util.Map<String, String> statusRequest = new java.util.HashMap<>();
        statusRequest.put("status", newStatus.name());

        ApiClient.getProductService().updateProductStatus(product.getId(), statusRequest, userId)
                .enqueue(new Callback<StandardResponse<Product>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Product>> call,
                                           Response<StandardResponse<Product>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Product> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                product.setStatus(newStatus);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Status updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to update status");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Product>> call, Throwable t) {
                        Log.e(TAG, "Failed to update product status", t);
                        showError("Network error");
                    }
                });
    }

    private void showDeleteConfirmDialog(Product product) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + product.getTitle() + "\"?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProduct(Product product) {
        Long userId = prefsManager.getUserId();

        ApiClient.getProductService().deleteProduct(product.getId(), userId)
                .enqueue(new Callback<StandardResponse<Void>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Void>> call,
                                           Response<StandardResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Void> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                int position = products.indexOf(product);
                                if (position != -1) {
                                    products.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    updateEmptyState();
                                }
                                Toast.makeText(getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to delete product");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Void>> call, Throwable t) {
                        Log.e(TAG, "Failed to delete product", t);
                        showError("Network error");
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh products when returning from other activities
        refreshProducts();
    }
}