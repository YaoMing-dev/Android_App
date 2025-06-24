// File: app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
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
            // Categories RecyclerView (horizontal) với click listener hoạt động
            if (rvCategories != null) {
                categoryAdapter = new CategoryAdapter(categories, category -> {
                    // 🔥 THÊM NAVIGATION ĐẾN CATEGORY PRODUCTS
                    Log.d(TAG, "Category clicked: " + category.getName());
                    navigateToCategoryProducts(category);
                });
                rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                rvCategories.setAdapter(categoryAdapter);
            }

            // Products RecyclerView (grid)
            if (rvRecentProducts != null) {
                productAdapter = new ProductAdapter(products, product -> {
                    Log.d(TAG, "Product clicked: " + product.getTitle());
                    navigateToProductDetail(product);
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
                swipeRefresh.setOnRefreshListener(() -> loadData());
            }

            // 🔥 HOẠT ĐỘNG CLICK VIEW ALL CATEGORIES
            if (tvViewAllCategories != null) {
                tvViewAllCategories.setOnClickListener(v -> {
                    Log.d(TAG, "View All Categories clicked");
                    navigateToAllCategories();
                });
            }

            // 🔥 HOẠT ĐỘNG CLICK VIEW ALL PRODUCTS
            if (tvViewAllProducts != null) {
                tvViewAllProducts.setOnClickListener(v -> {
                    Log.d(TAG, "View All Products clicked");
                    navigateToAllProducts();
                });
            }

            if (fabQuickAdd != null) {
                fabQuickAdd.setOnClickListener(v -> {
                    navigateToAddProduct();
                });
            }

            Log.d(TAG, "✅ HomeFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }

    private void loadData() {
        try {
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(true);
            }

            loadCategoriesFromBackend();
            loadProductsFromBackend();

            Log.d(TAG, "✅ Data loading started");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting data load", e);
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
        }
    }

    private void loadCategoriesFromBackend() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                   Response<StandardResponse<List<Map<String, Object>>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            categories.clear();

                            for (Map<String, Object> categoryData : apiResponse.getData()) {
                                Category category = new Category();
                                category.setId(((Number) categoryData.get("id")).longValue());
                                category.setName((String) categoryData.get("name"));
                                category.setDescription((String) categoryData.get("description"));
                                category.setIcon((String) categoryData.get("icon"));
                                category.setActive(true);

                                categories.add(category);
                            }

                            if (categoryAdapter != null) {
                                categoryAdapter.notifyDataSetChanged();
                            }

                            Log.d(TAG, "✅ Loaded " + categories.size() + " categories from backend");
                        } else {
                            loadSampleCategories();
                        }
                    } else {
                        loadSampleCategories();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing categories response", e);
                    loadSampleCategories();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                Log.e(TAG, "❌ Categories API call failed", t);
                loadSampleCategories();
            }
        });
    }

    private void loadSampleCategories() {
        categories.clear();
        categories.add(new Category(1L, "Electronics", "Electronics devices", "📱", true));
        categories.add(new Category(2L, "Fashion", "Clothing and accessories", "👕", true));
        categories.add(new Category(3L, "Home & Garden", "Home decor and garden", "🏠", true));
        categories.add(new Category(4L, "Books & Education", "Books and educational materials", "📚", true));
        categories.add(new Category(5L, "Sports & Recreation", "Sports and outdoor equipment", "⚽", true));

        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }
        Log.d(TAG, "✅ Loaded sample categories");
    }

    private void loadProductsFromBackend() {
        ApiClient.getApiService().getProducts(0, 20, null, null)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        try {
                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }

                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();

                                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                    Map<String, Object> data = apiResponse.getData();
                                    List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                                    if (productList != null) {
                                        products.clear();

                                        for (Map<String, Object> productData : productList) {
                                            Product product = parseProductFromBackend(productData);
                                            if (product != null) {
                                                products.add(product);
                                            }
                                        }

                                        if (productAdapter != null) {
                                            productAdapter.notifyDataSetChanged();
                                        }

                                        updateEmptyView();
                                        Log.d(TAG, "✅ Loaded " + products.size() + " products from backend");
                                    }
                                } else {
                                    loadSampleProducts();
                                }
                            } else {
                                loadSampleProducts();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing products response", e);
                            loadSampleProducts();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        if (swipeRefresh != null) {
                            swipeRefresh.setRefreshing(false);
                        }
                        Log.e(TAG, "❌ Products API call failed", t);
                        loadSampleProducts();
                    }
                });
    }

    private Product parseProductFromBackend(Map<String, Object> productData) {
        try {
            Product product = new Product();
            product.setId(((Number) productData.get("id")).longValue());
            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));

            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(((Number) priceObj).doubleValue());
            }

            product.setCondition((String) productData.get("condition"));
            product.setLocation((String) productData.get("location"));
            product.setStatus((String) productData.get("status"));

            // Parse images
            Object imagesObj = productData.get("images");
            if (imagesObj instanceof List) {
                List<Map<String, Object>> imagesList = (List<Map<String, Object>>) imagesObj;
                List<String> imageUrls = new ArrayList<>();
                for (Map<String, Object> imageData : imagesList) {
                    String imageUrl = (String) imageData.get("imageUrl");
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl);
                    }
                }
                product.setImageUrls(imageUrls);
            }

            return product;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing product data", e);
            return null;
        }
    }

    private void loadSampleProducts() {
        products.clear();
        // Sample data for fallback
        Product p1 = new Product(1L, "iPhone 13 Pro", "Like new condition", 15000000.0, "LIKE_NEW", "Ho Chi Minh City", "AVAILABLE");
        Product p2 = new Product(2L, "MacBook Air M1", "Excellent condition", 20000000.0, "GOOD", "Hanoi", "AVAILABLE");
        products.add(p1);
        products.add(p2);

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
        updateEmptyView();
        Log.d(TAG, "✅ Loaded sample products");
    }

    private void updateEmptyView() {
        if (emptyView != null && rvRecentProducts != null) {
            emptyView.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
            rvRecentProducts.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    // 🔥 NAVIGATION METHODS HOẠT ĐỘNG
    private void navigateToCategoryProducts(Category category) {
        Log.d(TAG, "Navigating to category: " + category.getName());

        Intent intent = new Intent(getContext(), CategoryProductsActivity.class);
        intent.putExtra("category_id", category.getId());
        intent.putExtra("category_name", category.getName());
        startActivity(intent);
    }

    private void navigateToProductDetail(Product product) {
        Log.d(TAG, "Navigating to product detail: " + product.getTitle());

        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getFormattedPrice());
        startActivity(intent);
    }

    private void navigateToAllCategories() {
        if (getActivity() != null) {
            try {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.nav_search);
            } catch (Exception e) {
                Log.e(TAG, "❌ Navigation error", e);
            }
        }
    }

    private void navigateToAllProducts() {
        if (getActivity() != null) {
            try {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.nav_search);
            } catch (Exception e) {
                Log.e(TAG, "❌ Navigation error", e);
            }
        }
    }

    private void navigateToAddProduct() {
        if (getActivity() != null) {
            try {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.nav_add_product);
            } catch (Exception e) {
                Log.e(TAG, "❌ Navigation error", e);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}