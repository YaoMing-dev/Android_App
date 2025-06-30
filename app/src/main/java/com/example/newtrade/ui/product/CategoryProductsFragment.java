// app/src/main/java/com/example/newtrade/ui/product/CategoryProductsFragment.java
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

public class CategoryProductsFragment extends Fragment implements ProductGridAdapter.OnProductClickListener {
    private static final String TAG = "CategoryProductsFragment";

    // Arguments
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    // UI Components
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

    public static CategoryProductsFragment newInstance(Long categoryId, String categoryName) {
        CategoryProductsFragment fragment = new CategoryProductsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getLong(ARG_CATEGORY_ID, -1);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadProducts();
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
    }

    private void setupRecyclerView() {
        adapter = new ProductGridAdapter(products, this);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
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
        if (isLoading || categoryId == -1) return;

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
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                }
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
        // TODO: Implement proper product parsing (reuse from other fragments)
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
        rvProducts.setVisibility(View.GONE);
        tvEmpty.setText("No products found in " + (categoryName != null ? categoryName : "this category"));
    }

    private void hideEmptyState() {
        tvEmpty.setVisibility(View.GONE);
        rvProducts.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
    }
}