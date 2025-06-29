package com.example.newtrade.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.example.newtrade.models.PagedResponse;
import com.example.newtrade.ui.adapters.ProductAdapter;
import com.example.newtrade.ui.adapters.CategoryAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvProducts;
    private RecyclerView rvCategories;
    private ProgressBar progressBar;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private boolean isLoading = false;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupRecyclerViews();
        setupSwipeRefresh();
        loadCategories();
        loadProducts(true);
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        rvProducts = view.findViewById(R.id.rvProducts);
        rvCategories = view.findViewById(R.id.rvCategories);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerViews() {
        // Products RecyclerView
        productAdapter = new ProductAdapter(new ArrayList<>(), product -> {
            // Navigate to product details
            Bundle args = new Bundle();
            args.putSerializable("product", product);
            // Navigate using NavController or start Activity
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(productAdapter);

        // Categories RecyclerView
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), category -> {
            // Filter products by category
            loadProducts(true, category);
        });

        LinearLayoutManager categoriesLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(categoriesLayout);
        rvCategories.setAdapter(categoryAdapter);

        // Pagination
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreProducts();
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> loadProducts(true));
    }

    private void loadCategories() {
        // In a real app, these would come from the backend
        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("Electronics");
        categories.add("Fashion");
        categories.add("Home");
        categories.add("Sports");
        categories.add("Books");
        categories.add("Others");
        categoryAdapter.updateCategories(categories);
    }

    private void loadProducts(boolean refresh) {
        loadProducts(refresh, null);
    }

    private void loadProducts(boolean refresh, String category) {
        if (refresh) {
            currentPage = 0;
            productAdapter.clearProducts();
        }

        if (isLoading) return;
        isLoading = true;

        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiClient.getClient().getAllProducts(currentPage, PAGE_SIZE, category, null, null, null, null, null)
            .enqueue(new Callback<StandardResponse<PagedResponse<Product>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<PagedResponse<Product>>> call, @NonNull Response<StandardResponse<PagedResponse<Product>>> response) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<PagedResponse<Product>> standardResponse = response.body();

                        if (standardResponse.isSuccess() && standardResponse.getData() != null) {
                            PagedResponse<Product> pagedData = standardResponse.getData();
                            List<Product> products = pagedData.getContent();

                            if (products != null) {
                                if (refresh) {
                                    productAdapter.updateProducts(products);
                                } else {
                                    productAdapter.addProducts(products);
                                }
                                currentPage++;
                            }
                        } else {
                            showError(standardResponse.getMessage() != null ?
                                standardResponse.getMessage() : "Could not load products");
                        }
                    } else {
                        showError("Could not load products");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<PagedResponse<Product>>> call, @NonNull Throwable t) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void loadMoreProducts() {
        loadProducts(false);
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction("Retry", v -> loadProducts(true))
                .show();
        }
    }
}
