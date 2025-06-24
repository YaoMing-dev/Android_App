// File: app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    // UI Components
    private TextInputEditText etSearch;
    private MaterialButton btnCategoryFilter;
    private MaterialButton btnPriceFilter;
    private MaterialButton btnLocationFilter;
    private MaterialButton btnConditionFilter;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmptyState;
    private RecyclerView rvPopularCategories;

    // Data
    private List<Product> searchResults = new ArrayList<>();
    private ProductAdapter searchAdapter;
    private String currentQuery = "";
    private String selectedCategory = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupListeners();
        setupFilterChips();

        // Check if we have category filter from arguments
        if (getArguments() != null) {
            selectedCategory = getArguments().getString("category", "");
            if (!selectedCategory.isEmpty()) {
                performSearch(currentQuery);
            }
        }

        Log.d(TAG, "SearchFragment created successfully");
    }

    private void initViews(View view) {
        try {
            etSearch = view.findViewById(R.id.et_search);
            btnCategoryFilter = view.findViewById(R.id.btn_category_filter);
            btnPriceFilter = view.findViewById(R.id.btn_price_filter);
            btnLocationFilter = view.findViewById(R.id.btn_location_filter);
            btnConditionFilter = view.findViewById(R.id.btn_condition_filter);
            chipGroupFilters = view.findViewById(R.id.chip_group_filters);
            rvSearchResults = view.findViewById(R.id.rv_search_results);
            llEmptyState = view.findViewById(R.id.ll_empty_state);
            rvPopularCategories = view.findViewById(R.id.rv_popular_categories);

            Log.d(TAG, "✅ SearchFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some SearchFragment views not found: " + e.getMessage());
        }
    }

    private void setupRecyclerViews() {
        try {
            if (rvSearchResults != null) {
                searchAdapter = new ProductAdapter(searchResults, product -> {
                    navigateToProductDetail(product);
                });
                rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
                rvSearchResults.setAdapter(searchAdapter);
            }

            Log.d(TAG, "✅ SearchFragment RecyclerViews setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up RecyclerViews", e);
        }
    }

    private void setupListeners() {
        try {
            // Search input listener
            if (etSearch != null) {
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        currentQuery = s.toString().trim();
                        if (currentQuery.length() >= 2 || currentQuery.isEmpty()) {
                            performSearch(currentQuery);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }

            // Filter button listeners
            if (btnCategoryFilter != null) {
                btnCategoryFilter.setOnClickListener(v -> showCategoryFilter());
            }

            if (btnPriceFilter != null) {
                btnPriceFilter.setOnClickListener(v -> showPriceFilter());
            }

            if (btnLocationFilter != null) {
                btnLocationFilter.setOnClickListener(v -> showLocationFilter());
            }

            if (btnConditionFilter != null) {
                btnConditionFilter.setOnClickListener(v -> showConditionFilter());
            }

            Log.d(TAG, "✅ SearchFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }

    private void setupFilterChips() {
        if (chipGroupFilters != null) {
            // Add category filter chips
            String[] categories = {"All", "Electronics", "Fashion", "Home & Garden", "Books", "Sports"};

            for (String category : categories) {
                Chip chip = new Chip(getContext());
                chip.setText(category);
                chip.setCheckable(true);
                chip.setChecked(category.equals("All"));

                chip.setOnCheckedChangeListener((view, isChecked) -> {
                    if (isChecked) {
                        // Uncheck other chips
                        for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
                            Chip otherChip = (Chip) chipGroupFilters.getChildAt(i);
                            if (otherChip != chip) {
                                otherChip.setChecked(false);
                            }
                        }

                        selectedCategory = category.equals("All") ? "" : category;
                        performSearch(currentQuery);
                    }
                });

                chipGroupFilters.addView(chip);
            }
        }
    }

    private void performSearch(String query) {
        Log.d(TAG, "🔍 Performing search: " + query + ", category: " + selectedCategory);

        ApiClient.getApiService().getProducts(0, 20, query, selectedCategory)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.hasData()) {
                                Map<String, Object> data = apiResponse.getData();
                                List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

                                searchResults.clear();
                                if (content != null) {
                                    for (Map<String, Object> productData : content) {
                                        searchResults.add(parseProductFromMap(productData));
                                    }
                                }

                                if (searchAdapter != null) {
                                    searchAdapter.notifyDataSetChanged();
                                }

                                updateEmptyState();
                                Log.d(TAG, "✅ Search completed: " + searchResults.size() + " results");
                            } else {
                                Log.w(TAG, "❌ Search API failed");
                                showEmptyState();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Search API call failed", t);
                        showEmptyState();
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

        List<String> imageUrls = (List<String>) data.get("imageUrls");
        product.setImageUrls(imageUrls);

        product.setCreatedAt((String) data.get("createdAt"));

        return product;
    }

    private void updateEmptyState() {
        if (rvSearchResults != null && llEmptyState != null) {
            if (searchResults.isEmpty()) {
                showEmptyState();
            } else {
                rvSearchResults.setVisibility(View.VISIBLE);
                llEmptyState.setVisibility(View.GONE);
            }
        }
    }

    private void showEmptyState() {
        if (rvSearchResults != null && llEmptyState != null) {
            rvSearchResults.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        }
    }

    // Filter dialog methods
    private void showCategoryFilter() {
        Toast.makeText(getContext(), "Category filter dialog coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement category filter bottom sheet
    }

    private void showPriceFilter() {
        Toast.makeText(getContext(), "Price filter dialog coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement price filter bottom sheet
    }

    private void showLocationFilter() {
        Toast.makeText(getContext(), "Location filter dialog coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement location filter bottom sheet
    }

    private void showConditionFilter() {
        Toast.makeText(getContext(), "Condition filter dialog coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement condition filter bottom sheet
    }

    private void navigateToProductDetail(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getFormattedPrice());
        startActivity(intent);
    }
}