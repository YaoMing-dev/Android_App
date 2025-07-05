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

import java.math.BigDecimal;
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

        Log.d(TAG, "✅ Adapters setup completed");
    }

    private void setupListeners() {
        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(this::loadData);

        // View all categories
        if (tvViewAllCategories != null) {
            tvViewAllCategories.setOnClickListener(v -> navigateToAllCategories());
        }

        // View all products
        if (tvViewAllProducts != null) {
            tvViewAllProducts.setOnClickListener(v -> navigateToAllProducts());
        }

        // Quick add product
        if (fabQuickAdd != null) {
            fabQuickAdd.setOnClickListener(v -> navigateToAddProduct());
        }

        Log.d(TAG, "✅ Listeners setup completed");
    }

    private void loadData() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }
        loadCategories();
        loadRecentProducts();
    }

    private void loadCategories() {
        Log.d(TAG, "🔍 Loading categories");

        ApiClient.getProductService().getCategories()
                .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                           @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                categories.clear();

                                for (Map<String, Object> categoryData : apiResponse.getData()) {
                                    Category category = parseCategory(categoryData);
                                    categories.add(category);
                                }

                                if (categoryAdapter != null) {
                                    categoryAdapter.notifyDataSetChanged();
                                }
                                Log.d(TAG, "✅ Loaded " + categories.size() + " categories");
                            } else {
                                Log.e(TAG, "Categories API Error: " + apiResponse.getMessage());
                                showMockCategories();
                            }
                        } else {
                            Log.e(TAG, "Categories response unsuccessful: " + response.code());
                            showMockCategories();
                        }
                        checkLoadingComplete();
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                          @NonNull Throwable t) {
                        Log.e(TAG, "Categories API call failed", t);
                        showMockCategories();
                        checkLoadingComplete();
                    }
                });
    }

    private void loadRecentProducts() {
        Log.d(TAG, "🔍 Loading recent products");

        ApiClient.getProductService().getProducts(0, 10, null, null, null, null, null, null, "newest")
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                           @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                handleProductsResponse(apiResponse.getData());
                            } else {
                                Log.e(TAG, "Products API Error: " + apiResponse.getMessage());
                                showMockProducts();
                            }
                        } else {
                            Log.e(TAG, "Products response unsuccessful: " + response.code());
                            showMockProducts();
                        }
                        checkLoadingComplete();
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                          @NonNull Throwable t) {
                        Log.e(TAG, "Products API call failed", t);
                        showMockProducts();
                        checkLoadingComplete();
                    }
                });
    }

    // ✅ THÊM parseCategory METHOD
    private Category parseCategory(Map<String, Object> categoryData) {
        Category category = new Category();

        try {
            if (categoryData.get("id") != null) {
                category.setId(((Number) categoryData.get("id")).longValue());
            }

            category.setName((String) categoryData.get("name"));
            category.setDescription((String) categoryData.get("description"));
            category.setIcon((String) categoryData.get("icon"));

            if (categoryData.get("productCount") != null) {
                category.setProductCount(((Number) categoryData.get("productCount")).intValue());
            }

            category.setCreatedAt((String) categoryData.get("createdAt"));

        } catch (Exception e) {
            Log.e(TAG, "Error parsing category data", e);
        }

        return category;
    }

    // ✅ THÊM showMockCategories METHOD
    private void showMockCategories() {
        categories.clear();

        // Create mock categories
        Category electronics = new Category();
        electronics.setId(1L);
        electronics.setName("Electronics");
        electronics.setDescription("Electronic devices and gadgets");
        electronics.setIcon("electronics");
        electronics.setProductCount(45);
        categories.add(electronics);

        Category fashion = new Category();
        fashion.setId(2L);
        fashion.setName("Fashion");
        fashion.setDescription("Clothing and accessories");
        fashion.setIcon("fashion");
        fashion.setProductCount(23);
        categories.add(fashion);

        Category homeGarden = new Category();
        homeGarden.setId(3L);
        homeGarden.setName("Home & Garden");
        homeGarden.setDescription("Home improvement and garden items");
        homeGarden.setIcon("home");
        homeGarden.setProductCount(12);
        categories.add(homeGarden);

        Category sports = new Category();
        sports.setId(4L);
        sports.setName("Sports");
        sports.setDescription("Sports equipment and gear");
        sports.setIcon("sports");
        sports.setProductCount(8);
        categories.add(sports);

        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }
        Log.d(TAG, "✅ Mock categories loaded: " + categories.size());
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
                if (productAdapter != null) {
                    productAdapter.notifyDataSetChanged();
                }
                updateEmptyState();
                Log.d(TAG, "✅ Products loaded: " + products.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing products", e);
            showMockProducts();
        }
    }

    private void showMockProducts() {
        products.clear();

        // Create mock products
        Product product1 = new Product();
        product1.setId(1L);
        product1.setTitle("iPhone 14 Pro");
        product1.setDescription("Latest iPhone with amazing features");
        product1.setPrice(new BigDecimal("25000000"));
        product1.setLocation("Ho Chi Minh City");
        product1.setCondition(Product.ProductCondition.LIKE_NEW);
        product1.setStatus(Product.ProductStatus.AVAILABLE);

        List<String> images1 = new ArrayList<>();
        images1.add("/uploads/products/iphone14.jpg");
        product1.setImageUrls(images1);

        products.add(product1);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setTitle("MacBook Pro M2");
        product2.setDescription("Powerful laptop for work and creativity");
        product2.setPrice(new BigDecimal("35000000"));
        product2.setLocation("Hanoi");
        product2.setCondition(Product.ProductCondition.GOOD);
        product2.setStatus(Product.ProductStatus.AVAILABLE);

        List<String> images2 = new ArrayList<>();
        images2.add("/uploads/products/macbook.jpg");
        product2.setImageUrls(images2);

        products.add(product2);

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
        updateEmptyState();
        Log.d(TAG, "✅ Mock products loaded: " + products.size());
    }

    private void checkLoadingComplete() {
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void updateEmptyState() {
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

    private void navigateToAllCategories() {
        Toast.makeText(requireContext(), "Navigate to all categories", Toast.LENGTH_SHORT).show();
    }

    private void navigateToAllProducts() {
        Intent intent = new Intent(requireContext(), AllProductsActivity.class);
        startActivity(intent);
    }

    private void navigateToAddProduct() {
        if (prefsManager != null && !prefsManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Please log in to add products", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_home_to_addProduct);
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleSaveProduct(Product product) {
        if (prefsManager == null) {
            return;
        }

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
                        public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                               @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();
                                if (apiResponse.isSuccess()) {
                                    product.setSaved(false);
                                    if (productAdapter != null) {
                                        productAdapter.notifyDataSetChanged();
                                    }
                                    Toast.makeText(requireContext(), "Product removed from saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                              @NonNull Throwable t) {
                            Log.e(TAG, "Failed to remove saved item", t);
                        }
                    });
        } else {
            // Add to saved
            ApiClient.getSavedItemService().saveItem(userId, product.getId())
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                               @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();
                                if (apiResponse.isSuccess()) {
                                    product.setSaved(true);
                                    if (productAdapter != null) {
                                        productAdapter.notifyDataSetChanged();
                                    }
                                    Toast.makeText(requireContext(), "Product saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                              @NonNull Throwable t) {
                            Log.e(TAG, "Failed to save item", t);
                        }
                    });
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}