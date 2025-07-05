// app/src/main/java/com/example/newtrade/ui/search/SearchActivity.java
package com.example.newtrade.ui.search;

import android.content.Intent; // ✅ THÊM IMPORT
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ProductService;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity; // ✅ THÊM IMPORT
import com.example.newtrade.utils.Constants; // ✅ THÊM IMPORT
import com.example.newtrade.utils.PriceFormatter; // ✅ THÊM IMPORT
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TextInputEditText etSearch;
    private RecyclerView rvSearchResults;
    private TextView tvEmptyState;

    // Data
    private final List<Product> searchResults = new ArrayList<>();
    private ProductAdapter productAdapter;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        prefsManager = SharedPrefsManager.getInstance(this);

        Log.d(TAG, "✅ SearchActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.et_search);
        rvSearchResults = findViewById(R.id.rv_search_results);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search");
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(searchResults, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(SearchActivity.this, ProductDetailActivity.class);
                intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId());
                intent.putExtra(Constants.EXTRA_PRODUCT_TITLE, product.getTitle());
                intent.putExtra(Constants.EXTRA_PRODUCT_PRICE, PriceFormatter.format(product.getPrice()));
                startActivity(intent);
            }
        });

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(productAdapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() > 2) {
                    searchProducts(query);
                } else {
                    searchResults.clear();
                    productAdapter.notifyDataSetChanged();
                    updateUI();
                }
            }
        });
    }

    private void searchProducts(String query) {
        ProductService productService = ApiClient.getProductService();

        // ✅ SỬA METHOD CALL - thêm đủ parameters
        Call<StandardResponse<Map<String, Object>>> call = productService.searchProducts(
                query, 0, 20, null, null, null, null, null, null, null);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        searchResults.clear();

                        Map<String, Object> data = apiResponse.getData();
                        if (data.get("content") instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> products = (List<Map<String, Object>>) data.get("content");

                            for (Map<String, Object> productData : products) {
                                Product product = Product.fromMap(productData);
                                searchResults.add(product);
                            }
                        }

                        productAdapter.notifyDataSetChanged();
                        updateUI();

                        // Save search query
                        prefsManager.saveRecentSearch(query);

                        Log.d(TAG, "✅ Search completed: " + searchResults.size() + " results for '" + query + "'");
                    } else {
                        Log.e(TAG, "Search API Error: " + apiResponse.getMessage());
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Search response unsuccessful: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Search API call failed", t);
                showEmptyState();
            }
        });
    }

    private void updateUI() {
        if (searchResults.isEmpty()) {
            showEmptyState();
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        searchResults.clear();
        productAdapter.notifyDataSetChanged();
        rvSearchResults.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
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