// app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ProductService;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.DateUtils;
import com.example.newtrade.utils.UserBehaviorTracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final int SEARCH_DELAY_MS = 200;
    private static final int SUGGESTION_DELAY_MS = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // ===============================
    // ✅ SEARCH SUGGESTION MODELS
    // ===============================

    public static class SearchSuggestion {
        private String text;
        private String type; // "RECENT", "POPULAR", "PRODUCT", "CATEGORY"
        private String description;
        private Long productId;
        private Long categoryId;
        private int count;

        public SearchSuggestion(String text, String type) {
            this.text = text;
            this.type = type;
        }

        public SearchSuggestion(String text, String type, String description) {
            this.text = text;
            this.type = type;
            this.description = description;
        }

        // Getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    // ===============================
    // ✅ SEARCH SUGGESTION ADAPTER
    // ===============================

    public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.SuggestionViewHolder> {
        private List<SearchSuggestion> suggestions;

        public SearchSuggestionAdapter(List<SearchSuggestion> suggestions) {
            this.suggestions = suggestions;
        }

        @NonNull
        @Override
        public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Create simple suggestion item programmatically
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(48, 32, 48, 32);
            layout.setBackground(getActivity().getDrawable(android.R.drawable.list_selector_background));

            // Icon
            ImageView icon = new ImageView(parent.getContext());
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(72, 72);
            iconParams.setMarginEnd(36);
            icon.setLayoutParams(iconParams);
            icon.setAlpha(0.6f);
            layout.addView(icon);

            // Text container
            LinearLayout textContainer = new LinearLayout(parent.getContext());
            textContainer.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            textContainer.setOrientation(LinearLayout.VERTICAL);

            // Main text
            TextView mainText = new TextView(parent.getContext());
            mainText.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mainText.setTextSize(16);
            mainText.setTextColor(getResources().getColor(android.R.color.black, null));
            textContainer.addView(mainText);

            // Type text
            TextView typeText = new TextView(parent.getContext());
            typeText.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            typeText.setTextSize(12);
            typeText.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            typeText.setVisibility(View.GONE);
            textContainer.addView(typeText);

            layout.addView(textContainer);

            // Insert arrow
            ImageView insertArrow = new ImageView(parent.getContext());
            LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(72, 72);
            insertArrow.setLayoutParams(arrowParams);
            insertArrow.setPadding(12, 12, 12, 12);
            insertArrow.setImageResource(android.R.drawable.ic_menu_edit);
            insertArrow.setAlpha(0.4f);
            insertArrow.setBackground(getActivity().getDrawable(android.R.drawable.list_selector_background));
            layout.addView(insertArrow);

            return new SuggestionViewHolder(layout, icon, mainText, typeText, insertArrow);
        }

        @Override
        public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
            holder.bind(suggestions.get(position));
        }

        @Override
        public int getItemCount() {
            return suggestions.size();
        }

        public void updateSuggestions(List<SearchSuggestion> newSuggestions) {
            this.suggestions = newSuggestions;
            notifyDataSetChanged();
        }

        class SuggestionViewHolder extends RecyclerView.ViewHolder {
            private ImageView icon, insertArrow;
            private TextView mainText, typeText;

            public SuggestionViewHolder(@NonNull View itemView, ImageView icon,
                                        TextView mainText, TextView typeText, ImageView insertArrow) {
                super(itemView);
                this.icon = icon;
                this.mainText = mainText;
                this.typeText = typeText;
                this.insertArrow = insertArrow;

                // Click to search immediately
                itemView.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        applySuggestionAndSearch(suggestions.get(pos));
                    }
                });

                // Click insert arrow to just fill search box
                insertArrow.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        insertSuggestionToSearchBox(suggestions.get(pos));
                    }
                });
            }

            public void bind(SearchSuggestion suggestion) {
                mainText.setText(suggestion.getText());

                if (suggestion.getDescription() != null) {
                    typeText.setText(suggestion.getDescription());
                    typeText.setVisibility(View.VISIBLE);
                } else {
                    typeText.setVisibility(View.GONE);
                }

                // Set icon based on type
                switch (suggestion.getType()) {
                    case "RECENT":
                        icon.setImageResource(android.R.drawable.ic_menu_recent_history);
                        break;
                    case "POPULAR":
                        icon.setImageResource(android.R.drawable.ic_menu_sort_by_size);
                        break;
                    case "PRODUCT":
                        icon.setImageResource(android.R.drawable.ic_search_category_default);
                        break;
                    case "CATEGORY":
                        icon.setImageResource(android.R.drawable.ic_menu_view);
                        break;
                    default:
                        icon.setImageResource(android.R.drawable.ic_search_category_default);
                        break;
                }
            }
        }
    }

    // UI Components
    private TextInputEditText etSearch;
    private MaterialButton btnCategoryFilter, btnPriceFilter, btnLocationFilter, btnConditionFilter;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmptyState, llRecentSearches, llLoadingState;
    private TextView tvResultsCount, tvSortOption;

    // Search Suggestions UI
    private RecyclerView rvSearchSuggestions;
    private SearchSuggestionAdapter suggestionAdapter;
    private final List<SearchSuggestion> searchSuggestions = new ArrayList<>();
    private boolean showingSuggestions = false;

    // Data & Adapters
    private ProductAdapter searchAdapter;
    private final List<Product> searchResults = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private String currentQuery = "";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Suggestion handler
    private final Handler suggestionHandler = new Handler(Looper.getMainLooper());
    private Runnable suggestionRunnable;

    // Services
    private ProductService productService;
    private ApiService apiService;
    private SharedPrefsManager prefsManager;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;
    private UserBehaviorTracker behaviorTracker;

    // Filters
    private Long selectedCategoryId = null;
    private String selectedCategoryName = null;
    private String selectedCondition = null;
    private Double minPrice = null;
    private Double maxPrice = null;
    private String currentSortOption = "relevance";
    private boolean isSearching = false;

    // Location Filter
    private Double searchLatitude = null;
    private Double searchLongitude = null;
    private String searchLocationName = "";
    private int searchRadiusKm = 10;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initServices();
        initViews(view);
        setupRecyclerViews();
        setupSuggestions();
        setupListeners();
        loadCategories();
        loadAllProductsInitially();

        Log.d(TAG, "SearchFragment created successfully with search suggestions");
    }

    private void initServices() {
        productService = ApiClient.getProductService();
        apiService = ApiClient.getApiService();
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        geocoder = new Geocoder(requireContext(), Locale.getDefault());
        behaviorTracker = UserBehaviorTracker.getInstance(requireContext());
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        btnCategoryFilter = view.findViewById(R.id.btn_category_filter);
        btnPriceFilter = view.findViewById(R.id.btn_price_filter);
        btnLocationFilter = view.findViewById(R.id.btn_location_filter);
        btnConditionFilter = view.findViewById(R.id.btn_condition_filter);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        llRecentSearches = view.findViewById(R.id.ll_recent_searches);
        llLoadingState = view.findViewById(R.id.ll_loading_state);
        tvResultsCount = view.findViewById(R.id.tv_results_count);
        tvSortOption = view.findViewById(R.id.tv_sort_option);

        // Create suggestions RecyclerView programmatically
        createSuggestionsRecyclerView(view);

        Log.d(TAG, "✅ SearchFragment views initialized");
    }

    private void createSuggestionsRecyclerView(View parentView) {
        // Find the parent container after search input
        ViewGroup searchContainer = parentView.findViewById(R.id.til_search);
        if (searchContainer != null && searchContainer.getParent() instanceof ViewGroup) {
            ViewGroup mainContainer = (ViewGroup) searchContainer.getParent();

            // Create suggestions RecyclerView
            rvSearchSuggestions = new RecyclerView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(48, 0, 48, 0);
            rvSearchSuggestions.setLayoutParams(params);
            rvSearchSuggestions.setBackgroundColor(getResources().getColor(android.R.color.white, null));
            rvSearchSuggestions.setElevation(12f);
            rvSearchSuggestions.setVisibility(View.GONE);

            // Add to container after search input
            int searchIndex = mainContainer.indexOfChild(searchContainer);
            mainContainer.addView(rvSearchSuggestions, searchIndex + 1);

            Log.d(TAG, "✅ Created suggestions RecyclerView programmatically");
        }
    }

    private void setupRecyclerViews() {
        if (rvSearchResults != null) {
            searchAdapter = new ProductAdapter(searchResults, this::navigateToProductDetail);
            rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
            rvSearchResults.setAdapter(searchAdapter);
        }
    }

    private void setupSuggestions() {
        if (rvSearchSuggestions != null) {
            suggestionAdapter = new SearchSuggestionAdapter(searchSuggestions);
            rvSearchSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
            rvSearchSuggestions.setAdapter(suggestionAdapter);
            rvSearchSuggestions.setVisibility(View.GONE);
            Log.d(TAG, "✅ Search suggestions setup completed");
        }
    }

    private void setupListeners() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();

                    // Handle search suggestions
                    if (query.length() >= 1) {
                        scheduleSuggestions(query);
                    } else {
                        hideSuggestions();
                    }

                    // Handle actual search
                    if (!query.equals(currentQuery)) {
                        currentQuery = query;
                        scheduleSearch();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Handle focus changes
            etSearch.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && !currentQuery.trim().isEmpty()) {
                    scheduleSuggestions(currentQuery.trim());
                } else if (!hasFocus) {
                    suggestionHandler.postDelayed(this::hideSuggestions, 150);
                }
            });

            // Handle search action
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideSuggestions();
                    performSearch();
                    hideKeyboard();
                    return true;
                }
                return false;
            });
        }

        if (btnCategoryFilter != null) {
            btnCategoryFilter.setOnClickListener(v -> showCategoryFilterDialog());
        }

        if (btnPriceFilter != null) {
            btnPriceFilter.setOnClickListener(v -> showPriceFilterDialog());
        }

        if (btnLocationFilter != null) {
            btnLocationFilter.setOnClickListener(v -> showLocationFilterDialog());
        }

        if (btnConditionFilter != null) {
            btnConditionFilter.setOnClickListener(v -> showConditionFilterDialog());
        }

        if (tvSortOption != null) {
            View llSortOptions = requireView().findViewById(R.id.ll_sort_options);
            if (llSortOptions != null) {
                llSortOptions.setOnClickListener(v -> showSortDialog());
            }
        }

        Log.d(TAG, "✅ SearchFragment listeners setup completed");
    }

    // ===============================
    // ✅ SEARCH SUGGESTIONS IMPLEMENTATION
    // ===============================

    private void scheduleSuggestions(String query) {
        if (suggestionRunnable != null) {
            suggestionHandler.removeCallbacks(suggestionRunnable);
        }

        suggestionRunnable = () -> loadSearchSuggestions(query);
        suggestionHandler.postDelayed(suggestionRunnable, SUGGESTION_DELAY_MS);
    }

    private void loadSearchSuggestions(String query) {
        if (query.trim().isEmpty()) {
            hideSuggestions();
            return;
        }

        Log.d(TAG, "🔍 Loading suggestions for: '" + query + "'");

        searchSuggestions.clear();

        // 1. Add recent searches
        addRecentSearchSuggestions(query);

        // 2. Add popular searches
        addPopularSearchSuggestions(query);

        // 3. Add category suggestions
        addCategorySuggestions(query);

        // 4. Add product name suggestions
        loadProductSuggestions(query);

        // Show suggestions if we have any
        if (!searchSuggestions.isEmpty()) {
            showSuggestions();
        }
    }

    private void addRecentSearchSuggestions(String query) {
        List<String> recentSearches = behaviorTracker.getSearchHistory();

        int added = 0;
        for (String recentSearch : recentSearches) {
            if (containsIgnoreCase(recentSearch, query) && added < 3) {
                SearchSuggestion suggestion = new SearchSuggestion(recentSearch, "RECENT", "Recent search");
                searchSuggestions.add(suggestion);
                added++;
            }
        }

        Log.d(TAG, "✅ Added " + added + " recent search suggestions");
    }

    private void addPopularSearchSuggestions(String query) {
        // ✅ Popular searches for all languages
        String[] popularSearches = {
                // Vietnamese
                "máy tính", "máy ảnh", "máy giặt", "máy lạnh", "máy xay",
                "điện thoại", "laptop", "ny hoàng", "áo thun", "áo dài",
                "giày", "túi xách", "xe máy", "oto", "iphone", "samsung",
                "macbook", "quần jean", "đồng hồ", "tai nghe",
                // English
                "headphones", "headset", "hello kitty", "helmet", "heavy metal",
                "heart", "heating", "health", "help", "hero", "her", "he",
                // Mixed
                "iphone", "samsung", "laptop", "gaming", "fashion", "beauty"
        };

        int added = 0;
        for (String popular : popularSearches) {
            if (containsIgnoreCase(popular, query) && added < 4) {
                SearchSuggestion suggestion = new SearchSuggestion(popular, "POPULAR", "Popular search");
                searchSuggestions.add(suggestion);
                added++;
            }
        }

        Log.d(TAG, "✅ Added " + added + " popular search suggestions");
    }

    private void addCategorySuggestions(String query) {
        int added = 0;
        for (Category category : categories) {
            if (containsIgnoreCase(category.getName(), query) && added < 2) {
                SearchSuggestion suggestion = new SearchSuggestion(category.getName(), "CATEGORY", "Category");
                suggestion.setCategoryId(category.getId());
                searchSuggestions.add(suggestion);
                added++;
            }
        }

        Log.d(TAG, "✅ Added " + added + " category suggestions");
    }

    private void loadProductSuggestions(String query) {
        // API call to get product suggestions
        ApiClient.getApiService().getProducts(
                0, 8, // Get more results to filter
                query,
                null, null, null, null,
                "createdAt", "desc"
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Map<String, Object> data = apiResponse.getData();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            if (productList != null) {
                                int added = 0;
                                for (Map<String, Object> productData : productList) {
                                    if (added >= 3) break; // Max 3 product suggestions

                                    String title = (String) productData.get("title");
                                    Object idObj = productData.get("id");

                                    // ✅ STRICT FILTERING: Only show if title contains query
                                    if (title != null && idObj instanceof Number &&
                                            containsIgnoreCase(title, query)) {
                                        SearchSuggestion suggestion = new SearchSuggestion(title, "PRODUCT", "Product");
                                        suggestion.setProductId(((Number) idObj).longValue());
                                        searchSuggestions.add(suggestion);
                                        added++;
                                    }
                                }

                                // Update UI on main thread
                                requireActivity().runOnUiThread(() -> {
                                    if (suggestionAdapter != null) {
                                        suggestionAdapter.notifyDataSetChanged();
                                    }

                                    if (!searchSuggestions.isEmpty()) {
                                        showSuggestions();
                                    }
                                });

                                Log.d(TAG, "✅ Added " + added + " product suggestions");
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error loading product suggestions", e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Product suggestions API failed", t);
            }
        });
    }

    // ✅ Helper method for case-insensitive contains check
    private boolean containsIgnoreCase(String text, String query) {
        if (text == null || query == null) return false;
        return text.toLowerCase().contains(query.toLowerCase());
    }

    private void showSuggestions() {
        if (rvSearchSuggestions != null && !searchSuggestions.isEmpty()) {
            rvSearchSuggestions.setVisibility(View.VISIBLE);
            showingSuggestions = true;

            if (suggestionAdapter != null) {
                suggestionAdapter.notifyDataSetChanged();
            }

            Log.d(TAG, "✅ Showing " + searchSuggestions.size() + " suggestions");
        }
    }

    private void hideSuggestions() {
        if (rvSearchSuggestions != null) {
            rvSearchSuggestions.setVisibility(View.GONE);
            showingSuggestions = false;
            Log.d(TAG, "✅ Hidden suggestions");
        }
    }

    private void applySuggestionAndSearch(SearchSuggestion suggestion) {
        Log.d(TAG, "🔍 Applying suggestion: " + suggestion.getText());

        // Set search text
        if (etSearch != null) {
            etSearch.setText(suggestion.getText());
            etSearch.setSelection(suggestion.getText().length());
        }

        // Track search
        behaviorTracker.trackSearch(suggestion.getText(), selectedCategoryName, searchLocationName);

        // Apply filters if needed
        if ("CATEGORY".equals(suggestion.getType()) && suggestion.getCategoryId() != null) {
            applyCategoryFilter(suggestion.getCategoryId(), suggestion.getText());
        }

        // Hide suggestions
        hideSuggestions();

        // Hide keyboard
        hideKeyboard();

        // Perform search
        currentQuery = suggestion.getText();
        performSearch();
    }

    private void insertSuggestionToSearchBox(SearchSuggestion suggestion) {
        if (etSearch != null) {
            etSearch.setText(suggestion.getText());
            etSearch.setSelection(suggestion.getText().length());
            // Keep suggestions open for further editing
            scheduleSuggestions(suggestion.getText());
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null && etSearch != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        }
    }

    // ===============================
    // ✅ ENHANCED SEARCH METHODS
    // ===============================

    private void loadAllProductsInitially() {
        Log.d(TAG, "🔄 Loading all products initially...");
        showLoadingState();

        ApiClient.getApiService().getProducts().enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                try {
                    Log.d(TAG, "✅ Initial products API response: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Map<String, Object> data = apiResponse.getData();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            if (productList != null) {
                                searchResults.clear();
                                for (Map<String, Object> productData : productList) {
                                    Product product = parseProductFromMapHomeStyle(productData);
                                    if (product != null) {
                                        searchResults.add(product);
                                    }
                                }

                                if (searchAdapter != null) {
                                    searchAdapter.notifyDataSetChanged();
                                }

                                showResults();
                                Log.d(TAG, "✅ Initially loaded " + searchResults.size() + " products");
                            }
                        } else {
                            Log.w(TAG, "❌ Products response unsuccessful");
                            showEmptyResults();
                        }
                    } else {
                        Log.w(TAG, "❌ Products response failed: HTTP " + response.code());
                        showEmptyResults();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing products", e);
                    showEmptyResults();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Products API call failed", t);
                showEmptyResults();
            }
        });
    }

    private Product parseProductFromMapHomeStyle(Map<String, Object> productData) {
        try {
            Product product = new Product();

            // Basic fields
            Object idObj = productData.get("id");
            if (idObj instanceof Number) {
                product.setId(((Number) idObj).longValue());
            }

            product.setTitle((String) productData.get("title"));
            product.setDescription((String) productData.get("description"));
            product.setLocation((String) productData.get("location"));

            // Price
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }

            // Category
            Object categoryIdObj = productData.get("categoryId");
            if (categoryIdObj instanceof Number) {
                product.setCategoryId(((Number) categoryIdObj).longValue());
            }

            // Category name
            String categoryName = (String) productData.get("categoryName");
            if (categoryName != null) {
                product.setCategoryName(categoryName);
            }

            // User display name
            String userDisplayName = (String) productData.get("userDisplayName");
            if (userDisplayName != null) {
                product.setUserDisplayName(userDisplayName);
            }

            // View count
            Object viewCountObj = productData.get("viewCount");
            if (viewCountObj instanceof Number) {
                product.setViewCount(((Number) viewCountObj).intValue());
            }

            // Condition
            String conditionStr = (String) productData.get("condition");
            if (conditionStr != null) {
                try {
                    Product.ProductCondition condition = Product.ProductCondition.valueOf(conditionStr);
                    product.setCondition(condition);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid condition: " + conditionStr + ", using GOOD as default");
                    product.setCondition(Product.ProductCondition.GOOD);
                }
            } else {
                product.setCondition(Product.ProductCondition.GOOD);
            }

            // Status
            String statusStr = (String) productData.get("status");
            if (statusStr != null) {
                try {
                    Product.ProductStatus status = Product.ProductStatus.valueOf(statusStr);
                    product.setStatus(status);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid status: " + statusStr + ", using AVAILABLE as default");
                    product.setStatus(Product.ProductStatus.AVAILABLE);
                }
            } else {
                product.setStatus(Product.ProductStatus.AVAILABLE);
            }

            // Image handling
            boolean imageFound = false;

            // Try imageUrls array first
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) imageUrlsObj;
                if (!imageUrls.isEmpty()) {
                    product.setImageUrls(imageUrls);
                    imageFound = true;
                    Log.d(TAG, "✅ Product " + product.getId() + " has " + imageUrls.size() + " images");
                }
            } else if (imageUrlsObj instanceof String && !((String) imageUrlsObj).isEmpty()) {
                product.setImageUrl((String) imageUrlsObj);
                imageFound = true;
            }

            // Fallback to "imageUrl" field
            if (!imageFound) {
                String imageUrl = (String) productData.get("imageUrl");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    product.setImageUrl(imageUrl);
                    imageFound = true;
                }
            }

            // Try primaryImageUrl field
            if (!imageFound) {
                String primaryImageUrl = (String) productData.get("primaryImageUrl");
                if (primaryImageUrl != null && !primaryImageUrl.isEmpty()) {
                    product.setImageUrl(primaryImageUrl);
                    imageFound = true;
                }
            }

            if (!imageFound) {
                Log.w(TAG, "❌ No imageUrl found for product " + product.getId());
            }

            // Created date
            String createdAtStr = (String) productData.get("createdAt");
            if (createdAtStr != null && DateUtils.isValidDate(createdAtStr)) {
                product.setCreatedAt(createdAtStr);
            } else {
                product.setCreatedAt(DateUtils.getCurrentTimestamp());
            }

            return product;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing product from map", e);
            return null;
        }
    }

    private void scheduleSearch() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        searchRunnable = this::performSearch;
        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
    }

    private void performSearch() {
        String query = currentQuery.trim();

        // Track search behavior
        behaviorTracker.trackSearch(
                query.isEmpty() ? null : query,
                selectedCategoryName,
                searchLocationName
        );

        // ✅ STRICT SEARCH: If there's a query, it must be in results
        if (query.isEmpty() && !hasActiveFilters()) {
            loadAllProductsInitially();
            return;
        }

        showLoadingState();
        searchWithFiltersAPI(query);
    }

    private void searchWithFiltersAPI(String query) {
        Log.d(TAG, "🔍 Searching with API filters...");
        Log.d(TAG, "  - Query: '" + query + "'");
        Log.d(TAG, "  - Category: " + selectedCategoryId + " (" + selectedCategoryName + ")");
        Log.d(TAG, "  - Price: " + minPrice + " - " + maxPrice);
        Log.d(TAG, "  - Condition: " + selectedCondition);

        // Use search API with filters
        ApiClient.getApiService().getProducts(
                0,                                          // page
                50,                                         // size
                query.isEmpty() ? null : query,             // search query
                selectedCategoryId,                         // categoryId
                selectedCondition,                          // condition
                minPrice,                                   // minPrice
                maxPrice,                                   // maxPrice
                getSortByParameter(),                       // sortBy
                getSortDirectionParameter()                 // sortDir
        ).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleSearchResponse(response, query);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Search API failed, falling back to client filtering", t);
                fallbackToClientFiltering(query);
            }
        });
    }

    private void handleSearchResponse(Response<StandardResponse<Map<String, Object>>> response, String query) {
        try {
            Log.d(TAG, "✅ Search API response: " + response.code());

            if (response.isSuccessful() && response.body() != null) {
                StandardResponse<Map<String, Object>> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    Map<String, Object> data = apiResponse.getData();
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                    if (productList != null) {
                        searchResults.clear();
                        for (Map<String, Object> productData : productList) {
                            Product product = parseProductFromMapHomeStyle(productData);
                            if (product != null) {
                                // ✅ STRICT FILTERING: Double-check if product matches query
                                if (query.isEmpty() || productMatchesQuery(product, query)) {
                                    searchResults.add(product);
                                }
                            }
                        }

                        if (searchAdapter != null) {
                            searchAdapter.notifyDataSetChanged();
                        }

                        showResults();
                        Log.d(TAG, "✅ Search completed: " + searchResults.size() + " results for '" + query + "'");
                        return;
                    }
                }
            }

            // If API search failed, fallback to client filtering
            Log.w(TAG, "❌ Search API unsuccessful, falling back to client filtering");
            fallbackToClientFiltering(query);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing search response, falling back to client filtering", e);
            fallbackToClientFiltering(query);
        }
    }

    // ✅ STRICT PRODUCT MATCHING
    private boolean productMatchesQuery(Product product, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true; // No query means show all
        }

        String queryLower = query.toLowerCase().trim();

        // Check title
        if (product.getTitle() != null &&
                product.getTitle().toLowerCase().contains(queryLower)) {
            return true;
        }

        // Check description
        if (product.getDescription() != null &&
                product.getDescription().toLowerCase().contains(queryLower)) {
            return true;
        }

        // Check category name
        if (product.getCategoryName() != null &&
                product.getCategoryName().toLowerCase().contains(queryLower)) {
            return true;
        }

        return false;
    }

    private void fallbackToClientFiltering(String query) {
        Log.d(TAG, "🔄 Fallback: Loading all products and applying client-side filters...");

        ApiClient.getApiService().getProducts().enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Map<String, Object> data = apiResponse.getData();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> productList = (List<Map<String, Object>>) data.get("content");

                            if (productList != null) {
                                searchResults.clear();
                                for (Map<String, Object> productData : productList) {
                                    Product product = parseProductFromMapHomeStyle(productData);
                                    if (product != null) {
                                        searchResults.add(product);
                                    }
                                }

                                // Apply client-side filtering
                                applyClientSideFilters(query);
                                applySorting();

                                if (searchAdapter != null) {
                                    searchAdapter.notifyDataSetChanged();
                                }

                                showResults();
                                Log.d(TAG, "✅ Client-side filtering completed: " + searchResults.size() + " results");
                            }
                        } else {
                            showEmptyResults();
                        }
                    } else {
                        showEmptyResults();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error in fallback filtering", e);
                    showEmptyResults();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallback API call failed", t);
                showEmptyResults();
            }
        });
    }

    private String getSortByParameter() {
        switch (currentSortOption) {
            case "newest":
                return "createdAt";
            case "price_low":
            case "price_high":
                return "price";
            default:
                return "createdAt";
        }
    }

    private String getSortDirectionParameter() {
        switch (currentSortOption) {
            case "price_low":
                return "asc";
            case "newest":
            case "price_high":
            default:
                return "desc";
        }
    }

    // ✅ ENHANCED CLIENT-SIDE FILTERING
    private void applyClientSideFilters(String query) {
        if (searchResults.isEmpty()) return;

        List<Product> originalResults = new ArrayList<>(searchResults);
        searchResults.clear();

        for (Product product : originalResults) {
            boolean passesFilters = true;

            // ✅ STRICT Query filter - must contain the search term
            if (!query.trim().isEmpty() && !productMatchesQuery(product, query)) {
                passesFilters = false;
            }

            // Price filter
            if (minPrice != null && product.getPrice() != null) {
                if (product.getPrice().doubleValue() < minPrice) {
                    passesFilters = false;
                }
            }
            if (maxPrice != null && product.getPrice() != null) {
                if (product.getPrice().doubleValue() > maxPrice) {
                    passesFilters = false;
                }
            }

            // Condition filter
            if (selectedCondition != null && product.getCondition() != null) {
                if (!selectedCondition.equals(product.getCondition().name())) {
                    passesFilters = false;
                }
            }

            // Category filter
            if (selectedCategoryId != null && product.getCategoryId() != null) {
                if (!selectedCategoryId.equals(product.getCategoryId())) {
                    passesFilters = false;
                }
            }

            if (passesFilters) {
                searchResults.add(product);
            }
        }

        Log.d(TAG, "✅ Client-side filtering applied - Results: " + searchResults.size() + " (Query: '" + query + "')");
    }

    private void applySorting() {
        if (searchResults.isEmpty()) return;

        switch (currentSortOption) {
            case "newest":
                searchResults.sort((p1, p2) -> {
                    String date1 = p1.getCreatedAt();
                    String date2 = p2.getCreatedAt();

                    if (!DateUtils.isValidDate(date1) && !DateUtils.isValidDate(date2)) {
                        return 0;
                    }
                    if (!DateUtils.isValidDate(date1)) {
                        return 1;
                    }
                    if (!DateUtils.isValidDate(date2)) {
                        return -1;
                    }

                    Date d1 = DateUtils.parseBackendDate(date1);
                    Date d2 = DateUtils.parseBackendDate(date2);

                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1;
                    if (d2 == null) return -1;

                    return d2.compareTo(d1);
                });
                break;

            case "price_low":
                searchResults.sort((p1, p2) -> {
                    if (p1.getPrice() != null && p2.getPrice() != null) {
                        return p1.getPrice().compareTo(p2.getPrice());
                    }
                    return 0;
                });
                break;

            case "price_high":
                searchResults.sort((p1, p2) -> {
                    if (p1.getPrice() != null && p2.getPrice() != null) {
                        return p2.getPrice().compareTo(p1.getPrice());
                    }
                    return 0;
                });
                break;

            case "relevance":
            default:
                break;
        }

        Log.d(TAG, "✅ Applied sorting: " + currentSortOption);
    }

    private boolean hasActiveFilters() {
        return selectedCategoryId != null ||
                selectedCondition != null ||
                minPrice != null ||
                maxPrice != null ||
                (searchLatitude != null && searchLongitude != null);
    }

    // ===============================
    // CATEGORY FILTER IMPLEMENTATION
    // ===============================

    private void showCategoryFilterDialog() {
        Log.d(TAG, "🔍 Showing category filter dialog...");

        if (categories.isEmpty()) {
            Log.d(TAG, "No categories loaded, loading first...");
            loadCategoriesForFilter();
            return;
        }

        showCategoryDialog();
    }

    private void loadCategoriesForFilter() {
        apiService.getCategories().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                   @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Map<String, Object>> categoriesData = response.body().getData();

                        categories.clear();
                        for (Map<String, Object> categoryData : categoriesData) {
                            Category category = new Category();
                            Object idObj = categoryData.get("id");
                            if (idObj instanceof Number) {
                                category.setId(((Number) idObj).longValue());
                            }
                            category.setName((String) categoryData.get("name"));
                            category.setDescription((String) categoryData.get("description"));
                            category.setIcon((String) categoryData.get("icon"));
                            categories.add(category);
                        }

                        showCategoryDialog();
                        Log.d(TAG, "✅ Loaded " + categories.size() + " categories for filter");
                    } else {
                        Log.e(TAG, "❌ Failed to load categories for filter");
                        loadSampleCategoriesForDialog();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error loading categories for filter", e);
                    loadSampleCategoriesForDialog();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Categories API failed for filter", t);
                loadSampleCategoriesForDialog();
            }
        });
    }

    private void loadSampleCategoriesForDialog() {
        categories.clear();
        categories.add(new Category(1L, "Electronics", "Electronics devices", "electronics", true));
        categories.add(new Category(2L, "Fashion", "Clothing and accessories", "fashion", true));
        categories.add(new Category(3L, "Home & Garden", "Home decor and garden", "home", true));
        categories.add(new Category(4L, "Books", "Books and educational materials", "books", true));
        categories.add(new Category(5L, "Sports", "Sports and outdoor equipment", "sports", true));
        categories.add(new Category(6L, "Beauty", "Beauty and health products", "beauty", true));
        categories.add(new Category(7L, "Vehicles", "Cars and motorbikes", "vehicles", true));
        categories.add(new Category(8L, "Toys", "Toys and kids items", "toys", true));

        showCategoryDialog();
        Log.d(TAG, "✅ Loaded sample categories for filter");
    }

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Category");

        // Create category options
        List<String> categoryNames = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();

        // Add "All Categories" option
        categoryNames.add("All Categories");
        categoryIds.add(null);

        // Add actual categories
        for (Category category : categories) {
            categoryNames.add(category.getName());
            categoryIds.add(category.getId());
        }

        String[] options = categoryNames.toArray(new String[0]);

        // Find current selection
        int selectedIndex = 0;
        if (selectedCategoryId != null) {
            for (int i = 1; i < categoryIds.size(); i++) {
                if (selectedCategoryId.equals(categoryIds.get(i))) {
                    selectedIndex = i;
                    break;
                }
            }
        }

        builder.setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
            if (which == 0) {
                // All Categories selected
                applyCategoryFilter(null, null);
            } else {
                // Specific category selected
                Long categoryId = categoryIds.get(which);
                String categoryName = categoryNames.get(which);
                applyCategoryFilter(categoryId, categoryName);
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void applyCategoryFilter(Long categoryId, String categoryName) {
        Log.d(TAG, "🔍 Applying category filter: " + categoryName + " (ID: " + categoryId + ")");

        this.selectedCategoryId = categoryId;
        this.selectedCategoryName = categoryName;

        // Track category browsing
        if (categoryId != null && categoryName != null) {
            behaviorTracker.trackCategoryBrowse(categoryId, categoryName);
        }

        // Update UI
        updateCategoryFilterButton();
        updateFilterChips();

        // Search with category filter
        scheduleSearch();
    }

    private void updateCategoryFilterButton() {
        if (btnCategoryFilter != null) {
            if (selectedCategoryName != null) {
                btnCategoryFilter.setText(selectedCategoryName);
                btnCategoryFilter.setSelected(true);
            } else {
                btnCategoryFilter.setText("Category");
                btnCategoryFilter.setSelected(false);
            }
        }
    }

    // ===============================
    // LOAD CATEGORIES FROM BACKEND
    // ===============================

    private void loadCategories() {
        Log.d(TAG, "🔄 Loading categories from backend...");

        apiService.getCategories().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                   @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        categories.clear();

                        for (Map<String, Object> categoryData : apiResponse.getData()) {
                            Category category = new Category();

                            Object idObj = categoryData.get("id");
                            if (idObj instanceof Number) {
                                category.setId(((Number) idObj).longValue());
                            }

                            category.setName((String) categoryData.get("name"));
                            category.setDescription((String) categoryData.get("description"));
                            category.setIcon((String) categoryData.get("icon"));

                            categories.add(category);
                        }

                        Log.d(TAG, "✅ Loaded " + categories.size() + " categories from backend");
                    } else {
                        Log.w(TAG, "❌ Categories response unsuccessful: " + apiResponse.getMessage());
                        loadSampleCategories();
                    }
                } else {
                    Log.w(TAG, "❌ Categories response failed: HTTP " + response.code());
                    loadSampleCategories();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Categories API call failed", t);
                loadSampleCategories();
            }
        });
    }

    private void loadSampleCategories() {
        categories.clear();
        categories.add(new Category(1L, "Electronics", "Electronics devices", "electronics", true));
        categories.add(new Category(2L, "Fashion", "Clothing and accessories", "fashion", true));
        categories.add(new Category(3L, "Home & Garden", "Home decor and garden", "home", true));
        categories.add(new Category(4L, "Books", "Books and educational materials", "books", true));
        categories.add(new Category(5L, "Sports", "Sports and outdoor equipment", "sports", true));
        categories.add(new Category(6L, "Beauty", "Beauty and health products", "beauty", true));
        categories.add(new Category(7L, "Vehicles", "Cars and motorbikes", "vehicles", true));
        categories.add(new Category(8L, "Toys", "Toys and kids items", "toys", true));

        Log.d(TAG, "✅ Loaded sample categories");
    }

    // ===============================
    // OTHER FILTER DIALOGS
    // ===============================

    private void showPriceFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Price Range");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_price_filter, null);

        TextInputEditText etMinPrice = dialogView.findViewById(R.id.et_min_price);
        TextInputEditText etMaxPrice = dialogView.findViewById(R.id.et_max_price);

        MaterialButton btnUnder10M = dialogView.findViewById(R.id.btn_under_10m);
        MaterialButton btn10MTo50M = dialogView.findViewById(R.id.btn_10m_to_50m);
        MaterialButton btnOver50M = dialogView.findViewById(R.id.btn_over_50m);

        // Set current values
        if (minPrice != null) {
            etMinPrice.setText(String.valueOf(minPrice.longValue()));
        }
        if (maxPrice != null) {
            etMaxPrice.setText(String.valueOf(maxPrice.longValue()));
        }

        // Quick price option listeners
        btnUnder10M.setOnClickListener(v -> {
            etMinPrice.setText("");
            etMaxPrice.setText("10000000");
        });

        btn10MTo50M.setOnClickListener(v -> {
            etMinPrice.setText("10000000");
            etMaxPrice.setText("50000000");
        });

        btnOver50M.setOnClickListener(v -> {
            etMinPrice.setText("50000000");
            etMaxPrice.setText("");
        });

        builder.setView(dialogView);

        builder.setPositiveButton("Apply", (dialog, which) -> {
            try {
                String minPriceStr = etMinPrice.getText().toString().trim();
                String maxPriceStr = etMaxPrice.getText().toString().trim();

                minPrice = minPriceStr.isEmpty() ? null : Double.valueOf(minPriceStr);
                maxPrice = maxPriceStr.isEmpty() ? null : Double.valueOf(maxPriceStr);

                Log.d(TAG, "✅ Price filter applied: " + minPrice + " - " + maxPrice);

                updateFilterChips();
                scheduleSearch();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.setNeutralButton("Clear", (dialog, which) -> {
            minPrice = null;
            maxPrice = null;
            Log.d(TAG, "✅ Price filter cleared");
            updateFilterChips();
            scheduleSearch();
        });

        builder.show();
    }

    private void showLocationFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Location Filter");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_filter, null);

        TextInputEditText etCustomLocation = dialogView.findViewById(R.id.et_custom_location);
        SeekBar seekBarRadius = dialogView.findViewById(R.id.seek_bar_radius);
        TextView tvRadiusValue = dialogView.findViewById(R.id.tv_radius_value);
        TextView tvCurrentLocationStatus = dialogView.findViewById(R.id.tv_current_location_status);

        MaterialButton btnUseCurrentLocation = dialogView.findViewById(R.id.btn_use_current_location);
        MaterialButton btnUseCustomLocation = dialogView.findViewById(R.id.btn_use_custom_location);
        MaterialButton btnClear = dialogView.findViewById(R.id.btn_clear);
        MaterialButton btnApply = dialogView.findViewById(R.id.btn_apply);

        // Set current values
        etCustomLocation.setText(searchLocationName);
        seekBarRadius.setProgress(Math.max(0, searchRadiusKm - 5));
        tvRadiusValue.setText(searchRadiusKm + " km");

        // Radius seekbar listener
        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int radius = progress + 5;
                tvRadiusValue.setText(radius + " km");
                searchRadiusKm = radius;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnUseCurrentLocation.setOnClickListener(v -> {
            getCurrentLocationWithGeocoding(etCustomLocation, tvCurrentLocationStatus, btnApply);
        });

        btnUseCustomLocation.setOnClickListener(v -> {
            String customLocation = etCustomLocation.getText().toString().trim();
            if (!customLocation.isEmpty()) {
                searchLocationName = customLocation;
                searchLatitude = null;
                searchLongitude = null;

                tvCurrentLocationStatus.setText("📍 Custom location: " + customLocation);
                tvCurrentLocationStatus.setVisibility(View.VISIBLE);
                btnApply.setEnabled(true);

                Toast.makeText(getContext(), "Custom location set: " + customLocation, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter a location", Toast.LENGTH_SHORT).show();
            }
        });

        btnClear.setOnClickListener(v -> {
            searchLocationName = "";
            searchLatitude = null;
            searchLongitude = null;
            searchRadiusKm = 10;

            etCustomLocation.setText("");
            seekBarRadius.setProgress(5);
            tvRadiusValue.setText("10 km");
            tvCurrentLocationStatus.setVisibility(View.GONE);
            btnApply.setEnabled(false);

            Toast.makeText(getContext(), "Location filter cleared", Toast.LENGTH_SHORT).show();
        });

        btnApply.setOnClickListener(v -> {
            Log.d(TAG, "✅ Applying location filter: " + searchLocationName);

            // Track location activity
            if (searchLatitude != null && searchLongitude != null) {
                behaviorTracker.trackLocationActivity(searchLocationName, searchLatitude, searchLongitude);
            }

            updateFilterChips();
            scheduleSearch();
            builder.create().dismiss();
        });

        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getCurrentLocationWithGeocoding(TextInputEditText etCustomLocation,
                                                 TextView statusView,
                                                 MaterialButton applyButton) {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        statusView.setText("🔄 Getting your location...");
        statusView.setVisibility(View.VISIBLE);
        etCustomLocation.setText("");
        applyButton.setEnabled(false);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        searchLatitude = location.getLatitude();
                        searchLongitude = location.getLongitude();

                        Log.d(TAG, "✅ Got GPS location: " + searchLatitude + ", " + searchLongitude);

                        convertCoordinatesToAddress(location.getLatitude(), location.getLongitude(),
                                etCustomLocation, statusView, applyButton);

                    } else {
                        statusView.setText("❌ Unable to get current location");
                        statusView.setVisibility(View.VISIBLE);
                        applyButton.setEnabled(false);
                        Toast.makeText(getContext(), "Unable to get current location. Try again or enter manually.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location", e);
                    statusView.setText("❌ Failed to get location");
                    statusView.setVisibility(View.VISIBLE);
                    applyButton.setEnabled(false);
                    Toast.makeText(getContext(), "Failed to get location: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void convertCoordinatesToAddress(double latitude, double longitude,
                                             TextInputEditText etCustomLocation,
                                             TextView statusView,
                                             MaterialButton applyButton) {

        new Thread(() -> {
            try {
                if (geocoder != null && Geocoder.isPresent()) {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);

                        StringBuilder addressText = new StringBuilder();

                        if (address.getSubThoroughfare() != null) {
                            addressText.append(address.getSubThoroughfare()).append(" ");
                        }
                        if (address.getThoroughfare() != null) {
                            addressText.append(address.getThoroughfare()).append(", ");
                        }

                        if (address.getSubLocality() != null) {
                            addressText.append(address.getSubLocality()).append(", ");
                        }

                        if (address.getLocality() != null) {
                            addressText.append(address.getLocality());
                        } else if (address.getAdminArea() != null) {
                            addressText.append(address.getAdminArea());
                        }

                        if (address.getCountryName() != null && !address.getCountryName().equals("Vietnam")) {
                            addressText.append(", ").append(address.getCountryName());
                        }

                        String finalAddress = addressText.toString();
                        if (finalAddress.isEmpty()) {
                            finalAddress = "Current Location";
                        }

                        finalAddress = finalAddress.replaceAll(", $", "");
                        searchLocationName = finalAddress;

                        requireActivity().runOnUiThread(() -> {
                            etCustomLocation.setText(searchLocationName);
                            statusView.setText("📍 Location detected: " + searchLocationName);
                            statusView.setVisibility(View.VISIBLE);
                            applyButton.setEnabled(true);

                            Log.d(TAG, "✅ Auto-filled address: " + searchLocationName);
                            Toast.makeText(getContext(), "Location detected!", Toast.LENGTH_SHORT).show();
                        });

                    } else {
                        requireActivity().runOnUiThread(() -> {
                            searchLocationName = "Lat: " + String.format("%.4f", latitude) +
                                    ", Lng: " + String.format("%.4f", longitude);

                            etCustomLocation.setText(searchLocationName);
                            statusView.setText("📍 " + searchLocationName);
                            statusView.setVisibility(View.VISIBLE);
                            applyButton.setEnabled(true);

                            Log.d(TAG, "✅ Used coordinates as address: " + searchLocationName);
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        searchLocationName = "Current Location (" +
                                String.format("%.4f", latitude) + ", " +
                                String.format("%.4f", longitude) + ")";

                        etCustomLocation.setText(searchLocationName);
                        statusView.setText("📍 " + searchLocationName);
                        statusView.setVisibility(View.VISIBLE);
                        applyButton.setEnabled(true);

                        Log.d(TAG, "✅ Geocoder unavailable, used coordinates: " + searchLocationName);
                    });
                }

            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);

                requireActivity().runOnUiThread(() -> {
                    searchLocationName = "Current Location";

                    etCustomLocation.setText(searchLocationName);
                    statusView.setText("📍 " + searchLocationName + " (geocoding failed)");
                    statusView.setVisibility(View.VISIBLE);
                    applyButton.setEnabled(true);

                    Toast.makeText(getContext(), "Location detected but address lookup failed", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Location permission granted. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showConditionFilterDialog() {
        String[] conditions = {"NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};
        String[] conditionDisplayNames = {"New", "Like New", "Good", "Fair", "Poor"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Condition");

        int selectedIndex = -1;
        if (selectedCondition != null) {
            for (int i = 0; i < conditions.length; i++) {
                if (conditions[i].equals(selectedCondition)) {
                    selectedIndex = i;
                    break;
                }
            }
        }

        builder.setSingleChoiceItems(conditionDisplayNames, selectedIndex, (dialog, which) -> {
            selectedCondition = conditions[which];
            Log.d(TAG, "✅ Selected condition: " + selectedCondition);
            updateFilterChips();
            scheduleSearch();
            dialog.dismiss();
        });

        builder.setNeutralButton("Clear", (dialog, which) -> {
            selectedCondition = null;
            Log.d(TAG, "✅ Condition filter cleared");
            updateFilterChips();
            scheduleSearch();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showSortDialog() {
        String[] sortOptions = {"Relevance", "Newest First", "Price: Low to High", "Price: High to Low"};
        String[] sortValues = {"relevance", "newest", "price_low", "price_high"};

        int selectedIndex = 0;
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(currentSortOption)) {
                selectedIndex = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sort by");
        builder.setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
            currentSortOption = sortValues[which];
            if (tvSortOption != null) {
                tvSortOption.setText(sortOptions[which]);
            }
            applySorting();
            if (searchAdapter != null) {
                searchAdapter.notifyDataSetChanged();
            }
            dialog.dismiss();
        });
        builder.show();
    }

    // ===============================
    // FILTER CHIPS MANAGEMENT
    // ===============================

    private void updateFilterChips() {
        if (chipGroupFilters != null) {
            chipGroupFilters.removeAllViews();

            if (selectedCategoryName != null) {
                addFilterChip("Category: " + selectedCategoryName, () -> {
                    selectedCategoryId = null;
                    selectedCategoryName = null;
                    updateCategoryFilterButton();
                    scheduleSearch();
                });
            }

            if (selectedCondition != null) {
                addFilterChip("Condition: " + selectedCondition, () -> {
                    selectedCondition = null;
                    scheduleSearch();
                });
            }

            if (minPrice != null || maxPrice != null) {
                String priceText = "Price: ";
                if (minPrice != null && maxPrice != null) {
                    priceText += formatPrice(minPrice) + " - " + formatPrice(maxPrice);
                } else if (minPrice != null) {
                    priceText += "From " + formatPrice(minPrice);
                } else {
                    priceText += "Up to " + formatPrice(maxPrice);
                }

                addFilterChip(priceText, () -> {
                    minPrice = null;
                    maxPrice = null;
                    scheduleSearch();
                });
            }

            if (!searchLocationName.isEmpty()) {
                addFilterChip("Location: " + searchLocationName + " (" + searchRadiusKm + "km)", () -> {
                    searchLocationName = "";
                    searchLatitude = null;
                    searchLongitude = null;
                    scheduleSearch();
                });
            }
        }
    }

    private String formatPrice(Double price) {
        if (price >= 1000000) {
            return String.format("%.0fM", price / 1000000);
        } else if (price >= 1000) {
            return String.format("%.0fK", price / 1000);
        } else {
            return String.format("%.0f", price);
        }
    }

    private void addFilterChip(String text, Runnable onCloseClick) {
        if (getContext() != null && chipGroupFilters != null) {
            Chip chip = new Chip(getContext());
            chip.setText(text);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                chipGroupFilters.removeView(chip);
                onCloseClick.run();
                updateFilterChips();
            });
            chipGroupFilters.addView(chip);
        }
    }

    // ===============================
    // UI STATE MANAGEMENT
    // ===============================

    private void showLoadingState() {
        isSearching = true;
        if (llLoadingState != null) llLoadingState.setVisibility(View.VISIBLE);
        if (llRecentSearches != null) llRecentSearches.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
    }

    private void showResults() {
        isSearching = false;

        if (searchResults.isEmpty()) {
            showEmptyResults();
        } else {
            if (rvSearchResults != null) rvSearchResults.setVisibility(View.VISIBLE);
            if (tvResultsCount != null) tvResultsCount.setVisibility(View.VISIBLE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
            if (llRecentSearches != null) llRecentSearches.setVisibility(View.GONE);
            if (llLoadingState != null) llLoadingState.setVisibility(View.GONE);
        }

        updateResultsCount(searchResults.size());
    }

    private void showEmptyResults() {
        searchResults.clear();
        if (searchAdapter != null) {
            searchAdapter.notifyDataSetChanged();
        }
        if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (llRecentSearches != null) llRecentSearches.setVisibility(View.GONE);
        if (llLoadingState != null) llLoadingState.setVisibility(View.GONE);
        if (tvResultsCount != null) tvResultsCount.setVisibility(View.GONE);
    }

    private void updateResultsCount(int count) {
        if (tvResultsCount != null) {
            String text = count == 1 ? count + " result found" : count + " results found";
            tvResultsCount.setText(text);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "Error: " + message);
    }

    private void navigateToProductDetail(Product product) {
        if (getContext() != null && product != null) {
            // Track product view for recommendations
            behaviorTracker.trackProductView(product.getId(), product.getCategoryName(), currentQuery);

            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_title", product.getTitle());
            intent.putExtra("search_query", currentQuery);
            if (product.getPrice() != null) {
                intent.putExtra("product_price", product.getPrice().toString());
            }
            startActivity(intent);
        }
    }
}