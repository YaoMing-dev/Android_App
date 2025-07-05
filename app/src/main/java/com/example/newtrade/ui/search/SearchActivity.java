// app/src/main/java/com/example/newtrade/ui/search/SearchActivity.java
package com.example.newtrade.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.google.android.material.appbar.MaterialToolbar;

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
    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmptyState;

    // Data
    private ProductAdapter productAdapter;
    private List<Product> searchResults;
    private String currentQuery = "";
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();

        Log.d(TAG, "SearchActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.et_search);
        rvSearchResults = findViewById(R.id.rv_search_results);
        llEmptyState = findViewById(R.id.ll_empty_state);

        searchResults = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Products");
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, searchResults);
        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        rvSearchResults.setAdapter(productAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.equals(currentQuery)) {
                    currentQuery = query;
                    searchProducts(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchProducts(String query) {
        if (isLoading) return;

        if (query.isEmpty()) {
            searchResults.clear();
            productAdapter.notifyDataSetChanged();
            updateUI();
            return;
        }

        isLoading = true;

        Call<StandardResponse<Map<String, Object>>> call =
                ApiClient.getProductService().searchProducts(query, 0, 20);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    // Handle search results
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                isLoading = false;
                Log.e(TAG, "Search failed", t);
            }
        });
    }

    private void updateUI() {
        if (searchResults.isEmpty()) {
            llEmptyState.setVisibility(android.view.View.VISIBLE);
            rvSearchResults.setVisibility(android.view.View.GONE);
        } else {
            llEmptyState.setVisibility(android.view.View.GONE);
            rvSearchResults.setVisibility(android.view.View.VISIBLE);
            productAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}