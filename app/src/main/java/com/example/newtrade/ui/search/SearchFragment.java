// File: app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
    private static final int SEARCH_DELAY_MS = 300; // Delay for search input

    // UI Components
    private TextInputEditText etSearch;
    private MaterialButton btnCategoryFilter, btnPriceFilter, btnLocationFilter, btnConditionFilter;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmptyState, llLoadingState, llRecentSearches;
    private TextView tvResultsCount, tvSortOption;

    // Data & Adapters
    private ProductAdapter searchAdapter;
    private List<Product> searchResults = new ArrayList<>();
    private String currentQuery = "";
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Filters
    private Long selectedCategoryId = null;
    private String selectedCondition = null;
    private Double minPrice = null;
    private Double maxPrice = null;
    private String sortBy = "relevance";

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
        showInitialState();

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

            llRecentSearches = view.findViewById(R.id.ll_recent_searches);
            tvResultsCount = view.findViewById(R.id.tv_results_count);
            tvSortOption = view.findViewById(R.id.tv_sort_option);

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
            // 🔥 SEARCH INPUT WITH DELAY
            if (etSearch != null) {
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String query = s.toString().trim();

                        // Cancel previous search
                        if (searchRunnable != null) {
                            searchHandler.removeCallbacks(searchRunnable);
                        }

                        // Schedule new search
                        searchRunnable = () -> {
                            if (query.length() >= 2 || query.isEmpty()) {
                                currentQuery = query;
                                performSearch();
                            }
                        };

                        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }

            // 🔥 FILTER BUTTONS
            if (btnCategoryFilter != null) {
                btnCategoryFilter.setOnClickListener(v -> showCategoryFilterDialog());
            }

            if (btnPriceFilter != null) {
                btnPriceFilter.setOnClickListener(v -> showPriceFilterDialog());
            }

            if (btnConditionFilter != null) {
                btnConditionFilter.setOnClickListener(v -> showConditionFilterDialog());
            }

            if (btnLocationFilter != null) {
                btnLocationFilter.setOnClickListener(v -> showLocationFilterDialog());
            }

            // 🔥 SORT OPTIONS
            if (tvSortOption != null) {
                tvSortOption.setOnClickListener(v -> showSortDialog());
            }

            Log.d(TAG, "✅ SearchFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }

    private void showInitialState() {
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.VISIBLE);
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.GONE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
        if (llLoadingState != null) {
            llLoadingState.setVisibility(View.GONE);
        }
    }

    private void performSearch() {
        Log.d(TAG, "🔍 Performing search: '" + currentQuery + "'");

        showLoadingState();

        if (currentQuery.isEmpty()) {
            showInitialState();
            return;
        }

        // 🔥 CALL SEARCH API
        ApiClient.getApiService().searchProducts(
                currentQuery.isEmpty() ? null : currentQuery,
                0, // page
                50, // size
                selectedCategoryId,
                selectedCondition,
                minPrice,
                maxPrice
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Map<String, Object> data = response.body().getData();
                        List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                        searchResults.clear();
                        if (productList != null) {
                            for (Map<String, Object> productData : productList) {
                                Product product = parseProductFromBackend(productData);
                                if (product != null) {
                                    searchResults.add(product);
                                }
                            }
                        }

                        updateSearchResults();
                        Log.d(TAG, "✅ Search completed: " + searchResults.size() + " results");
                    } else {
                        Log.w(TAG, "❌ Search API failed: " + response.message());
                        showEmptyState("Search failed. Please try again.");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing search results", e);
                    showEmptyState("Error loading search results.");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "❌ Search API call failed", t);
                showEmptyState("Network error. Check your connection.");
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
            Log.e(TAG, "❌ Error parsing product", e);
            return null;
        }
    }

    private void updateSearchResults() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            if (searchAdapter != null) {
                searchAdapter.notifyDataSetChanged();
            }

            if (tvResultsCount != null) {
                tvResultsCount.setText("Found " + searchResults.size() + " products");
            }

            if (searchResults.isEmpty()) {
                showEmptyState("No products found for '" + currentQuery + "'");
            } else {
                showResultsState();
            }
        });
    }

    private void showLoadingState() {
        if (llLoadingState != null) {
            llLoadingState.setVisibility(View.VISIBLE);
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.GONE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.GONE);
        }
    }

    private void showResultsState() {
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.VISIBLE);
        }
        if (llLoadingState != null) {
            llLoadingState.setVisibility(View.GONE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(String message) {
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.VISIBLE);
            // Update empty state message if needed
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.GONE);
        }
        if (llLoadingState != null) {
            llLoadingState.setVisibility(View.GONE);
        }
        if (llRecentSearches != null) {
            llRecentSearches.setVisibility(View.GONE);
        }
    }

    // 🔥 FILTER DIALOGS (TẠM THỜI SHOW TOAST)
    private void showCategoryFilterDialog() {
        Toast.makeText(getContext(), "Category filter dialog coming soon! 📱", Toast.LENGTH_SHORT).show();
    }

    private void showPriceFilterDialog() {
        Toast.makeText(getContext(), "Price filter dialog coming soon! 💰", Toast.LENGTH_SHORT).show();
    }

    private void showConditionFilterDialog() {
        Toast.makeText(getContext(), "Condition filter dialog coming soon! ⭐", Toast.LENGTH_SHORT).show();
    }

    private void showLocationFilterDialog() {
        Toast.makeText(getContext(), "Location filter dialog coming soon! 📍", Toast.LENGTH_SHORT).show();
    }

    private void showSortDialog() {
        Toast.makeText(getContext(), "Sort options dialog coming soon! 🔄", Toast.LENGTH_SHORT).show();
    }

    private void navigateToProductDetail(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getFormattedPrice());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}