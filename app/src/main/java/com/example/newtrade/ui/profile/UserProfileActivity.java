// app/src/main/java/com/example/newtrade/ui/profile/UserProfileActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    // UI Components
    private Toolbar toolbar;
    private ImageView ivAvatar;
    private TextView tvName, tvJoinDate, tvRating, tvLocation, tvBio;
    private MaterialButton btnMessage, btnCall;
    private TabLayout tabLayout;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View loadingView, contentView, errorView;

    // Data
    private User user;
    private Long userId;
    private List<Product> userProducts = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    // State
    private String currentTab = "ACTIVE"; // ACTIVE, SOLD
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userId = getIntent().getLongExtra(Constants.BUNDLE_USER_ID, -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initUtils();
        setupToolbar();
        setupListeners();
        setupRecyclerView();
        setupTabs();

        loadUserProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvName = findViewById(R.id.tv_name);
        tvJoinDate = findViewById(R.id.tv_join_date);
        tvRating = findViewById(R.id.tv_rating);
        tvLocation = findViewById(R.id.tv_location);
        tvBio = findViewById(R.id.tv_bio);
        btnMessage = findViewById(R.id.btn_message);
        btnCall = findViewById(R.id.btn_call);
        tabLayout = findViewById(R.id.tab_layout);
        rvProducts = findViewById(R.id.rv_products);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        loadingView = findViewById(R.id.view_loading);
        contentView = findViewById(R.id.view_content);
        errorView = findViewById(R.id.view_error);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
        btnMessage.setOnClickListener(v -> startConversation());
        btnCall.setOnClickListener(v -> callUser());
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));

        // TODO: Create ProductGridAdapter
        // ProductGridAdapter adapter = new ProductGridAdapter(userProducts, this::onProductClick);
        // rvProducts.setAdapter(adapter);

        // Pagination scroll listener
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && !isLastPage && layoutManager != null && dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        loadMoreProducts();
                    }
                }
            }
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Active"));
        tabLayout.addTab(tabLayout.newTab().setText("Sold"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition() == 0 ? "ACTIVE" : "SOLD";
                refreshProducts();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadUserProfile() {
        showLoadingState();

        // FR-1.2.4: Allow viewing other users' public profiles
        Call<StandardResponse<User>> call = ApiClient.getUserService().getUserById(userId);
        call.enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                handleUserProfileResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                handleLoadingError(t);
            }
        });
    }

    private void handleUserProfileResponse(Response<StandardResponse<User>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                user = response.body().getData();
                if (user != null) {
                    updateUserUI();
                    loadUserProducts();
                } else {
                    showErrorState("User not found");
                }
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Failed to load profile";
                showErrorState(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing user profile response", e);
            showErrorState("Error loading profile");
        }
    }

    private void updateUserUI() {
        if (user == null) return;

        // Profile picture
        String avatarUrl = user.getProfileImageUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }

        // Basic info
        tvName.setText(user.getDisplayOrFullName());

        // Join date
        if (user.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            tvJoinDate.setText("Member since " + sdf.format(user.getCreatedAt()));
        }

        // Rating - FR-7.2.1: Profile shows average rating, total transactions
        if (user.getRating() != null && user.getRating() > 0) {
            tvRating.setText(user.getRatingString() + " ⭐ (" + user.getTransactionCountText() + ")");
            tvRating.setVisibility(View.VISIBLE);
        } else {
            tvRating.setText(user.getTransactionCountText());
            tvRating.setVisibility(View.VISIBLE);
        }

        // Location
        if (user.getLocation() != null && !user.getLocation().isEmpty()) {
            tvLocation.setText(user.getLocation());
            tvLocation.setVisibility(View.VISIBLE);
        } else {
            tvLocation.setVisibility(View.GONE);
        }

        // Bio
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvBio.setText(user.getBio());
            tvBio.setVisibility(View.VISIBLE);
        } else {
            tvBio.setVisibility(View.GONE);
        }

        // Hide message/call buttons if viewing own profile
        boolean isOwnProfile = user.getId().equals(prefsManager.getUserId());
        if (isOwnProfile) {
            btnMessage.setVisibility(View.GONE);
            btnCall.setVisibility(View.GONE);
        } else {
            btnMessage.setVisibility(View.VISIBLE);
            btnCall.setVisibility(View.VISIBLE);

            // Hide call button if no contact info
            if (user.getContactInfo() == null || user.getContactInfo().isEmpty()) {
                btnCall.setVisibility(View.GONE);
            }
        }

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(user.getDisplayOrFullName());
        }
    }

    private void loadUserProducts() {
        if (isLoading) return;

        isLoading = true;
        if (currentPage == 0) {
            progressBar.setVisibility(View.VISIBLE);
        }

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getUserProducts(userId, currentPage, Constants.DEFAULT_PAGE_SIZE);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleProductsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleProductsError(t);
            }
        });
    }

    private void loadMoreProducts() {
        if (isLoading || isLastPage) return;

        currentPage++;
        loadUserProducts();
    }

    private void refreshData() {
        currentPage = 0;
        isLastPage = false;
        userProducts.clear();
        loadUserProfile();
    }

    private void refreshProducts() {
        currentPage = 0;
        isLastPage = false;
        userProducts.clear();
        loadUserProducts();
    }

    @SuppressWarnings("unchecked")
    private void handleProductsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        isLoading = false;
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);

        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null) {
                    int oldSize = userProducts.size();

                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            // Filter by current tab
                            if ((currentTab.equals("ACTIVE") && product.isAvailable()) ||
                                    (currentTab.equals("SOLD") && product.isSold())) {
                                userProducts.add(product);
                            }
                        }
                    }

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : true;

                    // Update UI
                    showContentState();

                    if (userProducts.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        // TODO: Notify adapter
                        // adapter.notifyItemRangeInserted(oldSize, userProducts.size() - oldSize);
                    }
                }
            } else {
                handleProductsError(new Exception("Failed to load products"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing products response", e);
            handleProductsError(e);
        }
    }

    private Product parseProductFromMap(Map<String, Object> productMap) {
        // Same implementation as in other activities
        try {
            Product product = new Product();
            product.setId(((Number) productMap.get("id")).longValue());
            product.setTitle((String) productMap.get("title"));
            product.setDescription((String) productMap.get("description"));
            product.setLocation((String) productMap.get("location"));

            Object price = productMap.get("price");
            if (price instanceof Number) {
                product.setPrice(new java.math.BigDecimal(price.toString()));
            }

            String conditionStr = (String) productMap.get("condition");
            if (conditionStr != null) {
                product.setCondition(Product.ProductCondition.fromString(conditionStr));
            }

            String statusStr = (String) productMap.get("status");
            if (statusStr != null) {
                product.setStatus(Product.ProductStatus.fromString(statusStr));
            }

            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) productMap.get("imageUrls");
            if (imageUrls != null) {
                product.setImageUrls(imageUrls);
            }

            return product;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing product", e);
            return null;
        }
    }

    private void handleProductsError(Throwable t) {
        isLoading = false;
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);

        Log.e(TAG, "Failed to load products", t);

        if (userProducts.isEmpty()) {
            showEmptyState();
        }
    }

    private void startConversation() {
        if (user == null) return;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.BUNDLE_USER_ID, userId);
        intent.putExtra("otherUserName", user.getDisplayOrFullName());
        startActivity(intent);
    }

    private void callUser() {
        if (user == null || user.getContactInfo() == null) {
            Toast.makeText(this, "Contact information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement call functionality
        Toast.makeText(this, "Call feature - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    private void handleLoadingError(Throwable t) {
        Log.e(TAG, "Failed to load user profile", t);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showErrorState("No internet connection");
        } else {
            showErrorState(NetworkUtils.getNetworkErrorMessage(t));
        }
    }

    private void showLoadingState() {
        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }

    private void showContentState() {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);

        TextView tvError = errorView.findViewById(R.id.tv_error);
        if (tvError != null) {
            tvError.setText(message);
        }
    }

    private void showEmptyState() {
        rvProducts.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);

        String emptyMessage = currentTab.equals("ACTIVE") ?
                "No active listings" : "No sold items";
        tvEmpty.setText(emptyMessage);
    }

    private void hideEmptyState() {
        rvProducts.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_share) {
            shareProfile();
            return true;
        } else if (itemId == R.id.action_report) {
            reportUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareProfile() {
        if (user == null) return;

        String shareText = user.getDisplayOrFullName() + "'s profile on TradeUp";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        Intent chooser = Intent.createChooser(shareIntent, "Share Profile");
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    private void reportUser() {
        // TODO: Implement report user functionality
        Toast.makeText(this, "Report user - Coming soon", Toast.LENGTH_SHORT).show();
    }
}