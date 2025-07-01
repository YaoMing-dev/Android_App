// app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
package com.example.newtrade.ui.home;

import android.content.Intent;
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
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.ProductResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.search.SearchActivity;
import com.example.newtrade.utils.ApiCallback;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // UI Components
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvWelcome;
    private MaterialCardView cardSearch;
    private RecyclerView rvCategories, rvRecentProducts, rvFeaturedProducts;
    private TextView tvViewAllCategories, tvViewAllRecent, tvViewAllFeatured;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private ProductAdapter recentProductsAdapter;
    private ProductAdapter featuredProductsAdapter;

    // Data
    private final List<Category> categories = new ArrayList<>();
    private final List<ProductResponse> recentProducts = new ArrayList<>();
    private final List<ProductResponse> featuredProducts = new ArrayList<>();

    // Utils
    private SharedPrefsManager prefsManager;

    // State
    private boolean isLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents(view);
        setupUI();
        loadInitialData();

        Log.d(TAG, "✅ HomeFragment initialized");
    }

    // =============================================
    // INITIALIZATION
    // =============================================

    private void initializeComponents(View view) {
        // Find views
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        tvWelcome = view.findViewById(R.id.tv_welcome);
        cardSearch = view.findViewById(R.id.card_search);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvRecentProducts = view.findViewById(R.id.rv_recent_products);
        rvFeaturedProducts = view.findViewById(R.id.rv_featured_products);
        tvViewAllCategories = view.findViewById(R.id.tv_view_all_categories);
        tvViewAllRecent = view.findViewById(R.id.tv_view_all_recent);
        tvViewAllFeatured = view.findViewById(R.id.tv_view_all_featured);

        // Initialize utils
        prefsManager = new SharedPrefsManager(requireContext());
    }

    private void setupUI() {
        setupWelcomeText();
        setupRecyclerViews();
        setupClickListeners();
        setupSwipeRefresh();
    }

    private void setupWelcomeText() {
        String displayName = prefsManager.getUserDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            tvWelcome.setText("Welcome back, " + displayName + "!");
        } else {
            tvWelcome.setText("Welcome to TradeUp!");
        }
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView (Horizontal)
        categoryAdapter = new CategoryAdapter(categories, this::onCategoryClick);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Recent Products RecyclerView (Horizontal)
        recentProductsAdapter = new ProductAdapter(recentProducts, this::onProductClick, ProductAdapter.VIEW_TYPE_HORIZONTAL);
        rvRecentProducts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecentProducts.setAdapter(recentProductsAdapter);

        // Featured Products RecyclerView (Grid)
        featuredProductsAdapter = new ProductAdapter(featuredProducts, this::onProductClick, ProductAdapter.VIEW_TYPE_GRID);
        rvFeaturedProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvFeaturedProducts.setAdapter(featuredProductsAdapter);
    }

    private void setupClickListeners() {
        cardSearch.setOnClickListener(v -> openSearch());
        tvViewAllCategories.setOnClickListener(v -> openAllCategories());
        tvViewAllRecent.setOnClickListener(v -> openAllRecentProducts());
        tvViewAllFeatured.setOnClickListener(v -> openAllFeaturedProducts());
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorSecondary,
                R.color.colorAccent
        );
    }

    // =============================================
    // DATA LOADING
    // =============================================

    private void loadInitialData() {
        if (isLoading) return;

        setLoading(true);
        loadCategories();
    }

    private void refreshData() {
        categories.clear();
        recentProducts.clear();
        featuredProducts.clear();

        categoryAdapter.notifyDataSetChanged();
        recentProductsAdapter.notifyDataSetChanged();
        featuredProductsAdapter.notifyDataSetChanged();

        loadInitialData();
    }

    private void loadCategories() {
        ApiClient.getCategoryService().getAllActiveCategories()
                .enqueue(new ApiCallback<List<Category>>() {
                    @Override
                    public void onSuccess(List<Category> result) {
                        handleCategoriesLoaded(result);
                        loadRecentProducts();
                    }

                    @Override
                    public void onError(String error) {
                        handleLoadError("Failed to load categories", error);
                        loadRecentProducts(); // Continue loading other data
                    }
                });
    }

    private void loadRecentProducts() {
        ApiClient.getProductService().getRecentProducts(Constants.HOME_RECENT_LIMIT)
                .enqueue(new ApiCallback<List<ProductResponse>>() {
                    @Override
                    public void onSuccess(List<ProductResponse> result) {
                        handleRecentProductsLoaded(result);
                        loadFeaturedProducts();
                    }

                    @Override
                    public void onError(String error) {
                        handleLoadError("Failed to load recent products", error);
                        loadFeaturedProducts(); // Continue loading
                    }
                });
    }

    private void loadFeaturedProducts() {
        ApiClient.getProductService().getFeaturedProducts(Constants.HOME_FEATURED_LIMIT)
                .enqueue(new ApiCallback<List<ProductResponse>>() {
                    @Override
                    public void onSuccess(List<ProductResponse> result) {
                        handleFeaturedProductsLoaded(result);
                        setLoading(false);
                    }

                    @Override
                    public void onError(String error) {
                        handleLoadError("Failed to load featured products", error);
                        setLoading(false);
                    }
                });
    }

    // =============================================
    // DATA HANDLING
    // =============================================

    private void handleCategoriesLoaded(List<Category> loadedCategories) {
        if (!isAdded()) return;

        categories.clear();
        categories.addAll(loadedCategories);
        categoryAdapter.notifyDataSetChanged();

        Log.d(TAG, "✅ Loaded " + categories.size() + " categories");
    }

    private void handleRecentProductsLoaded(List<ProductResponse> products) {
        if (!isAdded()) return;

        recentProducts.clear();
        recentProducts.addAll(products);
        recentProductsAdapter.notifyDataSetChanged();

        Log.d(TAG, "✅ Loaded " + recentProducts.size() + " recent products");
    }

    private void handleFeaturedProductsLoaded(List<ProductResponse> products) {
        if (!isAdded()) return;

        featuredProducts.clear();
        featuredProducts.addAll(products);
        featuredProductsAdapter.notifyDataSetChanged();

        Log.d(TAG, "✅ Loaded " + featuredProducts.size() + " featured products");
    }

    private void handleLoadError(String context, String error) {
        if (!isAdded()) return;

        Log.e(TAG, context + ": " + error);
        // Don't show error toast for individual sections to avoid spam
    }

    // =============================================
    // CLICK HANDLERS
    // =============================================

    private void onCategoryClick(Category category) {
        try {
            Intent intent = new Intent(getContext(), CategoryProductsActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
            intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening category", e);
            showError("Failed to open category");
        }
    }

    private void onProductClick(ProductResponse product) {
        try {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening product", e);
            showError("Failed to open product");
        }
    }

    private void openSearch() {
        try {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening search", e);
            showError("Failed to open search");
        }
    }

    private void openAllCategories() {
        try {
            Intent intent = new Intent(getContext(), AllCategoriesActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening all categories", e);
            showError("Failed to open categories");
        }
    }

    private void openAllRecentProducts() {
        try {
            Intent intent = new Intent(getContext(), AllProductsActivity.class);
            intent.putExtra("type", "recent");
            intent.putExtra("title", "Recent Products");
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening recent products", e);
            showError("Failed to open recent products");
        }
    }

    private void openAllFeaturedProducts() {
        try {
            Intent intent = new Intent(getContext(), AllProductsActivity.class);
            intent.putExtra("type", "featured");
            intent.putExtra("title", "Featured Products");
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening featured products", e);
            showError("Failed to open featured products");
        }
    }

    // =============================================
    // UI STATE
    // =============================================

    private void setLoading(boolean loading) {
        isLoading = loading;

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(loading);
        }
    }

    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: " + message);
        }
    }

    // =============================================
    // LIFECYCLE
    // =============================================

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to home
        if (!isLoading && (categories.isEmpty() || recentProducts.isEmpty())) {
            loadInitialData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isLoading = false;
    }
}