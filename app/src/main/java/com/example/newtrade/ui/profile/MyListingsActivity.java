// app/src/main/java/com/example/newtrade/ui/profile/MyListingsActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ProductService;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.AddProductActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyListingsActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "MyListingsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvMyListings;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddProduct;

    // Data & Adapters
    private final List<Product> myProducts = new ArrayList<>();
    private ProductAdapter productAdapter;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        prefsManager = SharedPrefsManager.getInstance(this);

        loadMyListings();

        Log.d(TAG, "✅ MyListingsActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvMyListings = findViewById(R.id.rv_my_listings);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        fabAddProduct = findViewById(R.id.fab_add_product);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Listings");
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(myProducts, this);
        rvMyListings.setLayoutManager(new LinearLayoutManager(this));
        rvMyListings.setAdapter(productAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadMyListings);

        if (fabAddProduct != null) {
            fabAddProduct.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddProductActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadMyListings() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Log.e(TAG, "User ID is null");
            showMockData();
            return;
        }

        ProductService productService = ApiClient.getProductService();
        Call<StandardResponse<List<Map<String, Object>>>> call = productService.getProductsByUser(userId, 0, 20);

        call.enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                   @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        myProducts.clear();

                        for (Map<String, Object> productData : apiResponse.getData()) {
                            Product product = parseProductFromMap(productData);
                            myProducts.add(product);
                        }

                        updateUI();
                        Log.d(TAG, "✅ Loaded " + myProducts.size() + " products");
                    } else {
                        Log.e(TAG, "API Error: " + apiResponse.getMessage());
                        showMockData();
                    }
                } else {
                    Log.e(TAG, "Response unsuccessful: " + response.code());
                    showMockData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "API call failed", t);
                showMockData();
            }
        });
    }

    // ✅ SỬA PHƯƠNG THỨC PARSE PRODUCT
    private Product parseProductFromMap(Map<String, Object> productData) {
        Product product = new Product();

        try {
            // Basic info
            Object idObj = productData.get("id");
            if (idObj instanceof Number) {
                product.setId(((Number) idObj).longValue());
            }

            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));
            product.setLocation((String) productData.get("location"));

            // ✅ SỬA PRICE - sử dụng BigDecimal
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }

            // ✅ SỬA CONDITION - sử dụng enum
            String conditionStr = (String) productData.get("condition");
            if (conditionStr != null) {
                try {
                    product.setCondition(Product.ProductCondition.valueOf(conditionStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Unknown condition: " + conditionStr + ", using GOOD as default");
                    product.setCondition(Product.ProductCondition.GOOD);
                }
            }

            // ✅ SỬA STATUS - sử dụng enum
            String statusStr = (String) productData.get("status");
            if (statusStr != null) {
                try {
                    product.setStatus(Product.ProductStatus.valueOf(statusStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Unknown status: " + statusStr + ", using AVAILABLE as default");
                    product.setStatus(Product.ProductStatus.AVAILABLE);
                }
            }

            // Image URLs
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) imageUrlsObj;
                product.setImageUrls(imageUrls);
            }

            // View count
            Object viewCountObj = productData.get("viewCount");
            if (viewCountObj instanceof Number) {
                product.setViewCount(((Number) viewCountObj).intValue());
            }

            // Category
            Object categoryObj = productData.get("category");
            if (categoryObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> categoryData = (Map<String, Object>) categoryObj;
                product.setCategoryName((String) categoryData.get("name"));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing product data", e);
        }

        return product;
    }

    private void showMockData() {
        myProducts.clear();

        // Mock listing 1
        Product product1 = new Product();
        product1.setId(1L);
        product1.setTitle("iPhone 14 Pro");
        product1.setDescription("iPhone 14 Pro 256GB Deep Purple - Excellent condition");
        product1.setPrice(new BigDecimal("18500000"));
        product1.setCondition(Product.ProductCondition.LIKE_NEW);
        product1.setLocation("Ho Chi Minh City");
        product1.setStatus(Product.ProductStatus.AVAILABLE);
        product1.setViewCount(156);
        product1.setCategoryName("Electronics");

        List<String> images1 = new ArrayList<>();
        images1.add("/uploads/products/iphone14.jpg");
        product1.setImageUrls(images1);

        myProducts.add(product1);

        // Mock listing 2
        Product product2 = new Product();
        product2.setId(2L);
        product2.setTitle("MacBook Pro M2");
        product2.setDescription("MacBook Pro M2 13-inch 512GB - Like new with original box");
        product2.setPrice(new BigDecimal("22000000"));
        product2.setCondition(Product.ProductCondition.GOOD);
        product2.setLocation("Ho Chi Minh City");
        product2.setStatus(Product.ProductStatus.SOLD);
        product2.setViewCount(78);
        product2.setCategoryName("Electronics");

        List<String> images2 = new ArrayList<>();
        images2.add("/uploads/products/macbook.jpg");
        product2.setImageUrls(images2);

        myProducts.add(product2);

        // Mock listing 3
        Product product3 = new Product();
        product3.setId(3L);
        product3.setTitle("Gaming Chair");
        product3.setDescription("Ergonomic gaming chair with lumbar support - very comfortable");
        product3.setPrice(new BigDecimal("3500000"));
        product3.setCondition(Product.ProductCondition.FAIR);
        product3.setLocation("Ho Chi Minh City");
        product3.setStatus(Product.ProductStatus.AVAILABLE);
        product3.setViewCount(23);
        product3.setCategoryName("Home & Garden");

        List<String> images3 = new ArrayList<>();
        images3.add("/uploads/products/chair.jpg");
        product3.setImageUrls(images3);

        myProducts.add(product3);

        updateUI();
    }

    private void updateUI() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        // Show/hide empty state
        if (myProducts.isEmpty()) {
            rvMyListings.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvMyListings.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }

        Log.d(TAG, "UI updated with " + myProducts.size() + " products");
    }

    // ✅ IMPLEMENT INTERFACE METHODS CORRECTLY
    @Override
    public void onProductClick(Product product) {
        openProductDetail(product);
    }

    // ✅ IMPLEMENT onProductSave method
    @Override
    public void onProductSave(Product product) {
        // Toggle save state
        product.setSaved(!product.isSaved());
        productAdapter.notifyDataSetChanged();

        // TODO: Call API to save/unsave product
        Log.d(TAG, "Product save toggled: " + product.getTitle());
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());

        // ✅ SỬA LỖI PRICE - sử dụng BigDecimal
        if (product.getPrice() != null) {
            intent.putExtra("product_price", product.getPrice().toString());
        }

        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}