// app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
package com.example.newtrade.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
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
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.search.AllProductsActivity;
import com.example.newtrade.ui.search.CategoryProductsActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
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
    private LinearLayout emptyView;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;

    // Data
    private final List<Category> categories = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initUtils();
        setupAdapters();
        setupListeners();
        loadData();

        Log.d(TAG, "✅ HomeFragment initialized");
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvRecentProducts = view.findViewById(R.id.rv_recent_products);
        tvViewAllCategories = view.findViewById(R.id.tv_view_all_categories);
        tvViewAllProducts = view.findViewById(R.id.tv_view_all_products);
        fabQuickAdd = view.findViewById(R.id.fab_quick_add);
        emptyView = view.findViewById(R.id.empty_view);

        Log.d(TAG, "✅ Views initialized");
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void setupAdapters() {
        // Category adapter
        categoryAdapter = new CategoryAdapter(categories, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                navigateToCategoryProducts(category);
            }
        });

        LinearLayoutManager categoriesLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(categoriesLayoutManager);
        rvCategories.setAdapter(categoryAdapter);

        // Product adapter
        productAdapter = new ProductAdapter(products, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                navigateToProductDetail(product);
            }

            @Override
            public void onProductSave(Product product) {
                toggleSaveProduct(product);
            }
        });

        GridLayoutManager productsLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvRecentProducts.setLayoutManager(productsLayoutManager);
        rvRecentProducts.setAdapter(productAdapter);

        Log.d(TAG, "✅ Adapters set up");
    }

    private void setupListeners() {
        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(this::loadData);

        // View all categories
        tvViewAllCategories.setOnClickListener(v -> {
            // Navigate to all categories
            Toast.makeText(requireContext(), "All categories", Toast.LENGTH_SHORT).show();
        });

        // View all products
        tvViewAllProducts.setOnClickListener(v -> {
            navigateToAllProducts();
        });

        // Quick add FAB
        fabQuickAdd.setOnClickListener(v -> {
            navigateToAddProduct();
        });

        Log.d(TAG, "✅ Listeners set up");
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        loadCategories();
        loadRecentProducts();
    }

    private void loadCategories() {
        Log.d(TAG, "🔍 Loading categories");

        ApiClient.getProductService().getCategories()
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                handleCategoriesResponse(apiResponse.getData());
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to load categories");
                        }
                        checkLoadingComplete();
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load categories", t);
                        showError("Network error loading categories");
                        checkLoadingComplete();
                    }
                });
    }

    private void loadRecentProducts() {
        Log.d(TAG, "🔍 Loading recent products");

        ApiClient.getProductService().getProducts(0, 10, null, null, null, null, null, null, "newest")
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                handleProductsResponse(apiResponse.getData());
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to load products");
                        }
                        checkLoadingComplete();
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load products", t);
                        showError("Network error loading products");
                        checkLoadingComplete();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void handleCategoriesResponse(Map<String, Object> data) {
        try {
            List<Map<String, Object>> categoryList = (List<Map<String, Object>>) data.get("categories");
            if (categoryList != null) {
                categories.clear();
                for (Map<String, Object> categoryData : categoryList) {
                    Category category = Category.fromMap(categoryData);
                    categories.add(category);
                }
                categoryAdapter.notifyDataSetChanged();
                Log.d(TAG, "✅ Categories loaded: " + categories.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing categories", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleProductsResponse(Map<String, Object> data) {
        try {
            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");
            if (productList != null) {
                products.clear();
                for (Map<String, Object> productData : productList) {
                    Product product = Product.fromMap(productData);
                    products.add(product);
                }
                productAdapter.notifyDataSetChanged();
                updateEmptyState();
                Log.d(TAG, "✅ Products loaded: " + products.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing products", e);
        }
    }

    private void checkLoadingComplete() {
        // Stop refresh animation when both requests are complete
        if (swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void updateEmptyState() {
        if (products.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            rvRecentProducts.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            rvRecentProducts.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToCategoryProducts(Category category) {
        Intent intent = new Intent(requireContext(), CategoryProductsActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
        intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
        startActivity(intent);
    }

    private void navigateToProductDetail(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId());
        intent.putExtra(Constants.EXTRA_PRODUCT_TITLE, product.getTitle());
        intent.putExtra(Constants.EXTRA_PRODUCT_PRICE, Constants.formatPrice(product.getPrice()));
        startActivity(intent);
    }

    private void navigateToAllProducts() {
        Intent intent = new Intent(requireContext(), AllProductsActivity.class);
        startActivity(intent);
    }

    private void navigateToAddProduct() {
        if (!prefsManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Please log in to add products", Toast.LENGTH_SHORT).show();
            return;
        }

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_home_to_addProduct);
    }

    private void toggleSaveProduct(Product product) {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "Please log in to save products", Toast.LENGTH_SHORT).show();
            return;
        }

        if (product.isSaved()) {
            // Remove from saved
            ApiClient.getSavedItemService().removeSavedItem(userId, product.getId())
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                               Response<StandardResponse<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();
                                if (apiResponse.isSuccess()) {
                                    product.setSaved(false);
                                    productAdapter.notifyDataSetChanged();
                                    Toast.makeText(requireContext(), "Removed from saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                            Log.e(TAG, "❌ Failed to remove from saved", t);
                        }
                    });
        } else {
            // Add to saved
            ApiClient.getSavedItemService().saveItem(userId, product.getId())
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                               Response<StandardResponse<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();
                                if (apiResponse.isSuccess()) {
                                    product.setSaved(true);
                                    productAdapter.notifyDataSetChanged();
                                    Toast.makeText(requireContext(), "Added to saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                            Log.e(TAG, "❌ Failed to save item", t);
                        }
                    });
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        Log.w(TAG, "Error: " + message);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "HomeFragment destroyed");
    }
}