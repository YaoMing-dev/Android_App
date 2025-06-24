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

        Log.d(TAG, "HomeFragment created successfully");
    }

    private void initViews(View view) {
        try {
            swipeRefresh = view.findViewById(R.id.swipe_refresh);
            rvCategories = view.findViewById(R.id.rv_categories);
            rvRecentProducts = view.findViewById(R.id.rv_recent_products);
            tvViewAllCategories = view.findViewById(R.id.tv_view_all_categories);
            tvViewAllProducts = view.findViewById(R.id.tv_view_all_products);
            fabQuickAdd = view.findViewById(R.id.fab_quick_add);
            emptyView = view.findViewById(R.id.empty_view);

            Log.d(TAG, "✅ HomeFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some HomeFragment views not found: " + e.getMessage());
        }
    }

    private void setupRecyclerViews() {
        try {
            // Categories RecyclerView (horizontal)
            if (rvCategories != null) {
                categoryAdapter = new CategoryAdapter(categories, category -> {
                    Toast.makeText(getContext(), "Category: " + category.getName(), Toast.LENGTH_SHORT).show();
                });
                rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                rvCategories.setAdapter(categoryAdapter);
            }

            // Products RecyclerView (grid)
            if (rvRecentProducts != null) {
                productAdapter = new ProductAdapter(products, product -> {
                    Toast.makeText(getContext(), "Product: " + product.getTitle(), Toast.LENGTH_SHORT).show();
                });
                rvRecentProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
                rvRecentProducts.setAdapter(productAdapter);
            }

            Log.d(TAG, "✅ HomeFragment RecyclerViews setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up RecyclerViews", e);
        }
    }

    private void setupListeners() {
        try {
            if (swipeRefresh != null) {
                swipeRefresh.setOnRefreshListener(() -> {
                    loadData();
                });
            }

            if (tvViewAllCategories != null) {
                tvViewAllCategories.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "View all categories", Toast.LENGTH_SHORT).show();
                });
            }

            if (tvViewAllProducts != null) {
                tvViewAllProducts.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "View all products", Toast.LENGTH_SHORT).show();
                });
            }

            if (fabQuickAdd != null) {
                fabQuickAdd.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Quick add product", Toast.LENGTH_SHORT).show();
                });
            }

            Log.d(TAG, "✅ HomeFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }

    private void loadData() {
        try {
            // Load sample data for now
            loadSampleCategories();
            loadSampleProducts();

            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }

            Log.d(TAG, "✅ Data loaded");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading data", e);
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
        }
    }

    private void loadSampleCategories() {
        categories.clear();

        // Add sample categories
        categories.add(new Category(1L, "Electronics", "Electronic devices", null, true));
        categories.add(new Category(2L, "Fashion", "Clothing and accessories", null, true));
        categories.add(new Category(3L, "Home", "Home and garden items", null, true));
        categories.add(new Category(4L, "Books", "Books and magazines", null, true));
        categories.add(new Category(5L, "Sports", "Sports equipment", null, true));

        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }
    }

    private void loadSampleProducts() {
        products.clear();

        // Add sample products would go here
        // For now just update adapter
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        // Show/hide empty view
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (emptyView != null && rvRecentProducts != null) {
            if (products.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                rvRecentProducts.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                rvRecentProducts.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}