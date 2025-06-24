// app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
package com.example.newtrade.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.CategoryAdapter;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // UI Components
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvCategories;
    private RecyclerView rvRecentProducts;
    private TextView tvViewAllCategories;
    private TextView tvViewAllProducts;
    private FloatingActionButton fabQuickAdd;
    private View emptyView;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;

    // Data
    private List<Category> categories = new ArrayList<>();
    private List<Product> products = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupListeners();
        loadData();
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvRecentProducts = view.findViewById(R.id.rv_recent_products);
        tvViewAllCategories = view.findViewById(R.id.tv_view_all_categories);
        tvViewAllProducts = view.findViewById(R.id.tv_view_all_products);
        fabQuickAdd = view.findViewById(R.id.fab_quick_add);
        emptyView = view.findViewById(R.id.empty_view);
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView
        categoryAdapter = new CategoryAdapter(categories, category -> {
            // Navigate to category products
            HomeFragmentDirections.ActionHomeToCategoryProducts action =
                    HomeFragmentDirections.actionHomeToCategoryProducts(category.getId(), category.getName());
            Navigation.findNavController(requireView()).navigate(action);
        });

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Products RecyclerView
        productAdapter = new ProductAdapter(products, product -> {
            // Navigate to product detail
            Bundle bundle = new Bundle();
            bundle.putLong("productId", product.getId());
            Navigation.findNavController(requireView()).navigate(R.id.productDetailActivity, bundle);
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvRecentProducts.setLayoutManager(gridLayoutManager);
        rvRecentProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadData);

        tvViewAllCategories.setOnClickListener(v -> {
            // Navigate to all categories
        });

        tvViewAllProducts.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.nav_search);
        });

        fabQuickAdd.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.nav_add_product);
        });
    }

    private void loadData() {
        loadCategories();
        loadProducts();
    }

    private void loadCategories() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                   Response<StandardResponse<List<Map<String, Object>>>> response) {
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Map<String, Object>> categoryMaps = response.body().getData();
                    categories.clear();

                    for (Map<String, Object> map : categoryMaps) {
                        Category category = new Category();
                        category.setId(((Double) map.get("id")).longValue());
                        category.setName((String) map.get("name"));
                        category.setIcon((String) map.get("icon"));
                        categories.add(category);
                    }

                    categoryAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + categories.size() + " categories");
                } else {
                    Log.e(TAG, "Failed to load categories");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Error loading categories: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        ApiClient.getProductService().getProducts(0, 10, null, null)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                            products.clear();
                            for (Map<String, Object> map : productMaps) {
                                Product product = Product.fromMap(map);
                                products.add(product);
                            }

                            productAdapter.notifyDataSetChanged();
                            updateEmptyView();
                            Log.d(TAG, "Loaded " + products.size() + " products");
                        } else {
                            Log.e(TAG, "Failed to load products");
                            updateEmptyView();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "Error loading products: " + t.getMessage());
                        updateEmptyView();
                    }
                });
    }

    private void updateEmptyView() {
        if (products.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            rvRecentProducts.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            rvRecentProducts.setVisibility(View.VISIBLE);
        }
    }
}