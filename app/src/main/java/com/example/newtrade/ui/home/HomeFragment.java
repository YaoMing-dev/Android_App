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
import com.example.newtrade.ui.search.CategoryProductsActivity;
import com.example.newtrade.ui.search.AllProductsActivity;
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
    private final List<Category> categories = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();

    // State
    private boolean isDataLoaded = false;

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

        // Only load data if not already loaded
        if (!isDataLoaded) {
            loadData();
        }

        Log.d(TAG, "✅ HomeFragment initialized successfully");
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
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            categoryAdapter = new CategoryAdapter(categories, category -> {
                // Navigate to category products
                Intent intent = new Intent(requireContext(), CategoryProductsActivity.class);
                intent.putExtra("category_id", category.getId());
                intent.putExtra("category_name", category.getName());
                startActivity(intent);
            });
            rvCategories.setAdapter(categoryAdapter);
        }

        // Products RecyclerView
        if (rvRecentProducts != null) {
            rvRecentProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            productAdapter = new ProductAdapter(products, this::openProductDetail);
            rvRecentProducts.setAdapter(productAdapter);
        }
    }

    private void setupListeners() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::loadData);
        }

        if (tvViewAllCategories != null) {
            tvViewAllCategories.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "View all categories", Toast.LENGTH_SHORT).show();
            });
        }

        if (tvViewAllProducts != null) {
            tvViewAllProducts.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AllProductsActivity.class);
                startActivity(intent);
            });
        }

        if (fabQuickAdd != null) {
            fabQuickAdd.setOnClickListener(v -> {
                try {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.nav_add_product);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error", e);
                    Toast.makeText(requireContext(), "Quick add coming soon", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadData() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        loadCategories();
        loadRecentProducts();
    }

    // ✅ FIXED: Load categories from backend (no more forced mock)
    private void loadCategories() {
        if (!ApiClient.isInitialized()) {
            Log.w(TAG, "⚠️ ApiClient not initialized, showing mock categories");
            showMockCategories();
            return;
        }

        if (getContext() == null || !isAdded()) {
            Log.w(TAG, "⚠️ Fragment not attached, skipping categories load");
            return;
        }

        Log.d(TAG, "🔄 Loading categories from backend...");

        try {
            ApiClient.getApiService().getCategories()
                    .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                        @Override
                        public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                               @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {

                            if (!isAdded() || getContext() == null) {
                                Log.w(TAG, "Fragment detached, ignoring categories response");
                                return;
                            }

                            try {
                                if (response.isSuccessful() && response.body() != null) {
                                    StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                        updateCategories(apiResponse.getData());
                                        Log.d(TAG, "✅ Categories loaded from backend: " + apiResponse.getData().size());
                                    } else {
                                        Log.e(TAG, "Categories API error: " + apiResponse.getMessage());
                                        showMockCategories(); // Fallback only on API error
                                    }
                                } else {
                                    Log.e(TAG, "Categories request failed: " + response.code());
                                    showMockCategories(); // Fallback on HTTP error
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing categories response", e);
                                showMockCategories(); // Fallback on parsing error
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                            if (!isAdded() || getContext() == null) {
                                Log.w(TAG, "Fragment detached, ignoring categories failure");
                                return;
                            }

                            Log.e(TAG, "❌ Categories API request failed: " + t.getMessage());
                            showMockCategories(); // Fallback on network error
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error calling categories API", e);
            showMockCategories(); // Fallback on unexpected error
        }
    }

    // ✅ FIXED: Load products from backend (not forced mock anymore)
    private void loadRecentProducts() {
        if (!ApiClient.isInitialized()) {
            Log.w(TAG, "⚠️ ApiClient not initialized, showing mock products");
            showMockProducts();
            return;
        }

        if (getContext() == null || !isAdded()) {
            Log.w(TAG, "⚠️ Fragment not attached, skipping products load");
            return;
        }

        Log.d(TAG, "🔄 Loading recent products from backend...");

        try {
            // ✅ FIXED: Use ProductService to get real products from backend
            ApiClient.getProductService().getProducts()
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                               @NonNull Response<StandardResponse<Map<String, Object>>> response) {

                            if (!isAdded() || getContext() == null) {
                                Log.w(TAG, "Fragment detached, ignoring products response");
                                return;
                            }

                            try {
                                if (response.isSuccessful() && response.body() != null) {
                                    StandardResponse<Map<String, Object>> apiResponse = response.body();

                                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                        Map<String, Object> data = apiResponse.getData();
                                        @SuppressWarnings("unchecked")
                                        List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                                        if (productList != null && !productList.isEmpty()) {
                                            updateProductsFromBackend(productList);
                                            Log.d(TAG, "✅ Products loaded from backend: " + productList.size());
                                        } else {
                                            Log.w(TAG, "⚠️ No products found in backend, showing mock");
                                            showMockProducts();
                                        }
                                    } else {
                                        Log.e(TAG, "Products API error: " + apiResponse.getMessage());
                                        showMockProducts(); // Fallback on API error
                                    }
                                } else {
                                    Log.e(TAG, "Products request failed: " + response.code());
                                    showMockProducts(); // Fallback on HTTP error
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing products response", e);
                                showMockProducts(); // Fallback on parsing error
                            }

                            // Stop refreshing
                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }
                            isDataLoaded = true;
                        }

                        @Override
                        public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                            if (!isAdded() || getContext() == null) {
                                Log.w(TAG, "Fragment detached, ignoring products failure");
                                return;
                            }

                            Log.e(TAG, "❌ Products API request failed: " + t.getMessage());
                            showMockProducts(); // Fallback on network error

                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }
                            isDataLoaded = true;
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error calling products API", e);
            showMockProducts(); // Fallback on unexpected error
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
            isDataLoaded = true;
        }
    }

    // ✅ NEW: Update products from backend data
    private void updateProductsFromBackend(List<Map<String, Object>> productMaps) {
        try {
            products.clear();

            if (productMaps != null && !productMaps.isEmpty()) {
                for (Map<String, Object> productMap : productMaps) {
                    try {
                        Product product = parseProductFromBackendMap(productMap);
                        products.add(product);
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing product: " + productMap, e);
                    }
                }
            }

            if (productAdapter != null) {
                productAdapter.notifyDataSetChanged();
            }

            if (products.isEmpty()) {
                showEmptyState();
            } else {
                showProductsState();
            }

            Log.d(TAG, "✅ Products updated from backend: " + products.size());

        } catch (Exception e) {
            Log.e(TAG, "Error updating products from backend", e);
            showMockProducts();
        }
    }

    // ✅ NEW: Parse product from backend response
    private Product parseProductFromBackendMap(Map<String, Object> productMap) {
        Product product = new Product();

        // Parse ID
        if (productMap.get("id") instanceof Number) {
            product.setId(((Number) productMap.get("id")).longValue());
        }

        // Parse basic fields
        product.setTitle((String) productMap.get("title"));
        product.setDescription((String) productMap.get("description"));
        product.setLocation((String) productMap.get("location"));
        product.setCondition((String) productMap.get("condition"));
        product.setStatus((String) productMap.get("status"));
        product.setCreatedAt((String) productMap.get("createdAt"));

        // Parse images
        String primaryImageUrl = (String) productMap.get("primaryImageUrl");
        if (primaryImageUrl != null && !primaryImageUrl.isEmpty()) {
            // Convert relative URL to full URL using Constants
            product.setImageUrl(primaryImageUrl);
            product.setPrimaryImageUrl(primaryImageUrl);
        } else {
            product.setImageUrl("https://via.placeholder.com/300x300");
            product.setPrimaryImageUrl("https://via.placeholder.com/300x300");
        }

        // Parse price (handle BigDecimal from backend)
        Object priceObj = productMap.get("price");
        if (priceObj instanceof Number) {
            product.setPrice(((Number) priceObj).doubleValue());
        } else {
            product.setPrice(0.0);
        }

        // Parse view count
        Object viewCountObj = productMap.get("viewCount");
        if (viewCountObj instanceof Number) {
            product.setViewCount(((Number) viewCountObj).intValue());
        } else {
            product.setViewCount(0);
        }

        return product;
    }

    // ✅ FIXED: Update categories from backend
    private void updateCategories(List<Map<String, Object>> categoryMaps) {
        try {
            categories.clear();

            if (categoryMaps != null && !categoryMaps.isEmpty()) {
                for (Map<String, Object> categoryMap : categoryMaps) {
                    try {
                        Category category = new Category();

                        if (categoryMap.get("id") instanceof Number) {
                            category.setId(((Number) categoryMap.get("id")).longValue());
                        }

                        category.setName((String) categoryMap.get("name"));
                        category.setDescription((String) categoryMap.get("description"));
                        category.setIconUrl((String) categoryMap.get("iconUrl"));

                        categories.add(category);
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing category: " + categoryMap, e);
                    }
                }
            } else {
                // Show mock categories if API returns empty
                showMockCategories();
                return;
            }

            if (categoryAdapter != null) {
                categoryAdapter.notifyDataSetChanged();
            }

            Log.d(TAG, "✅ Categories updated from backend: " + categories.size());

        } catch (Exception e) {
            Log.e(TAG, "Error updating categories from backend", e);
            showMockCategories();
        }
    }

    // ✅ FALLBACK: Mock categories (only used when backend fails)
    private void showMockCategories() {
        try {
            categories.clear();

            String[] categoryNames = {"Electronics", "Fashion", "Home", "Sports", "Books", "Auto"};

            for (int i = 0; i < categoryNames.length; i++) {
                Category category = new Category();
                category.setId((long) (i + 1));
                category.setName(categoryNames[i]);
                category.setDescription("Browse " + categoryNames[i].toLowerCase() + " items");
                category.setIconUrl("https://via.placeholder.com/64x64?text=" + categoryNames[i].charAt(0));
                categories.add(category);
            }

            if (categoryAdapter != null) {
                categoryAdapter.notifyDataSetChanged();
            }

            Log.d(TAG, "✅ Mock categories loaded: " + categories.size());

        } catch (Exception e) {
            Log.e(TAG, "Error showing mock categories", e);
        }
    }

    // ✅ FALLBACK: Mock products (only used when backend fails)
    private void showMockProducts() {
        try {
            products.clear();

            // Create some mock products
            for (int i = 1; i <= 6; i++) {
                Product product = new Product();
                product.setId((long) i);
                product.setTitle("Sample Product " + i);
                product.setDescription("This is a sample product description for item " + i);

                double price = 100000.0 + (i * 50000.0);
                product.setPrice(price);

                product.setLocation("Ho Chi Minh City");
                product.setCondition("GOOD");
                product.setImageUrl("https://via.placeholder.com/300x300?text=Product+" + i);
                product.setViewCount(42 + i);
                products.add(product);
            }

            if (productAdapter != null) {
                productAdapter.notifyDataSetChanged();
            }

            showProductsState();
            isDataLoaded = true;

            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }

            Log.d(TAG, "✅ Mock products loaded: " + products.size());

        } catch (Exception e) {
            Log.e(TAG, "Error showing mock products", e);
            showEmptyState();
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
        }
    }

    private void openProductDetail(Product product) {
        try {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_title", product.getTitle());
            intent.putExtra("product_price", product.getFormattedPrice());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening product detail", e);
            Toast.makeText(requireContext(), "Cannot open product details", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyState() {
        if (rvRecentProducts != null) {
            rvRecentProducts.setVisibility(View.GONE);
        }
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void showProductsState() {
        if (rvRecentProducts != null) {
            rvRecentProducts.setVisibility(View.VISIBLE);
        }
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Don't reload data every time, only refresh if needed
        if (!isDataLoaded) {
            loadData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear references to prevent memory leaks
        categoryAdapter = null;
        productAdapter = null;
    }
}