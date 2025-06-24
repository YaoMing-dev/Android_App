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

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.adapters.CategoryAdapter;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
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
                    navigateToCategoryProducts(category);
                });
                rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                rvCategories.setAdapter(categoryAdapter);
            }

            // Products RecyclerView (grid)
            if (rvRecentProducts != null) {
                productAdapter = new ProductAdapter(products, product -> {
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
                swipeRefresh.setOnRefreshListener(() -> {
                    loadData();
                });
            }

            if (tvViewAllCategories != null) {
                tvViewAllCategories.setOnClickListener(v -> {
                    navigateToAllCategories();
                });
            }

            if (tvViewAllProducts != null) {
                tvViewAllProducts.setOnClickListener(v -> {
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

            // Load categories từ backend
            loadCategoriesFromBackend();

            // Load products từ backend
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
                            Log.w(TAG, "❌ Categories API response not successful: " + apiResponse.getMessage());
                            loadSampleCategories(); // Fallback to sample data
                        }
                    } else {
                        Log.w(TAG, "❌ Categories API call failed: " + response.code());
                        loadSampleCategories(); // Fallback to sample data
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing categories response", e);
                    loadSampleCategories(); // Fallback to sample data
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                Log.e(TAG, "❌ Categories API call failed", t);
                loadSampleCategories(); // Fallback to sample data
            }
        });
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
                                    List<Map<String, Object>> productsList = (List<Map<String, Object>>) data.get("content");

                                    if (productsList != null) {
                                        products.clear();

                                        for (Map<String, Object> productData : productsList) {
                                            Product product = parseProductFromMap(productData);
                                            products.add(product);
                                        }

                                        if (productAdapter != null) {
                                            productAdapter.notifyDataSetChanged();
                                        }

                                        updateEmptyView();

                                        Log.d(TAG, "✅ Loaded " + products.size() + " products from backend");
                                    }
                                } else {
                                    Log.w(TAG, "❌ Products API response not successful: " + apiResponse.getMessage());
                                    loadSampleProducts(); // Fallback
                                }
                            } else {
                                Log.w(TAG, "❌ Products API call failed: " + response.code());
                                loadSampleProducts(); // Fallback
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing products response", e);
                            loadSampleProducts(); // Fallback
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Products API call failed", t);
                        if (swipeRefresh != null) {
                            swipeRefresh.setRefreshing(false);
                        }
                        loadSampleProducts(); // Fallback
                    }
                });
    }

    private Product parseProductFromMap(Map<String, Object> data) {
        Product product = new Product();

        if (data.get("id") != null) {
            product.setId(((Number) data.get("id")).longValue());
        }
        product.setTitle((String) data.get("title"));
        product.setDescription((String) data.get("description"));

        if (data.get("price") != null) {
            product.setPrice(((Number) data.get("price")).doubleValue());
        }

        product.setCondition((String) data.get("conditionType"));
        product.setLocation((String) data.get("location"));
        product.setStatus((String) data.get("status"));
        product.setCategoryName((String) data.get("categoryName"));
        product.setUserDisplayName((String) data.get("userDisplayName"));

        if (data.get("viewCount") != null) {
            product.setViewCount(((Number) data.get("viewCount")).intValue());
        }

        // Parse image URLs
        List<String> imageUrls = (List<String>) data.get("imageUrls");
        product.setImageUrls(imageUrls);

        product.setCreatedAt((String) data.get("createdAt"));

        return product;
    }

    private void loadSampleCategories() {
        categories.clear();

        categories.add(new Category(1L, "Electronics", "Electronic devices", "smartphone", true));
        categories.add(new Category(2L, "Fashion", "Clothing and accessories", "shirt", true));
        categories.add(new Category(3L, "Home & Garden", "Home and garden items", "home", true));
        categories.add(new Category(4L, "Books & Education", "Books and magazines", "book", true));
        categories.add(new Category(5L, "Sports", "Sports equipment", "dumbbell", true));

        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }
    }

    private void loadSampleProducts() {
        products.clear();

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

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

    // Navigation methods
    private void navigateToCategoryProducts(Category category) {
        Toast.makeText(getContext(), "Viewing products in: " + category.getName(), Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            try {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.nav_search);
            } catch (Exception e) {
                Log.e(TAG, "❌ Category navigation error", e);
            }
        }
    }

    private void navigateToProductDetail(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getFormattedPrice());
        startActivity(intent);
    }

    private void navigateToAllCategories() {
        Toast.makeText(getContext(), "Viewing all categories", Toast.LENGTH_SHORT).show();
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