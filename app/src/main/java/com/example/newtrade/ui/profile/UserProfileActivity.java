// app/src/main/java/com/example/newtrade/ui/profile/UserProfileActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.example.newtrade.ui.product.adapter.ProductGridAdapter;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity implements ProductGridAdapter.OnProductClickListener {
    private static final String TAG = "UserProfileActivity";

    // UI Components
    private Toolbar toolbar;
    private ImageView ivAvatar;
    private TextView tvName, tvJoinDate, tvRating, tvLocation, tvBio;
    private MaterialButton btnMessage, btnFollow;
    private TabLayout tabLayout;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty;

    // Data
    private User user;
    private Long userId;
    private ProductGridAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private String currentTab = "products";

    // State
    private boolean isFollowing = false;
    private boolean isOwnProfile = false;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        prefsManager = new SharedPrefsManager(this);

        // Get user ID from intent
        userId = getIntent().getLongExtra(Constants.BUNDLE_USER_ID, -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if viewing own profile
        Long currentUserId = prefsManager.getUserId();
        isOwnProfile = currentUserId != null && currentUserId.equals(userId);

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupListeners();
        loadUserProfile();
        loadUserProducts();
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
        btnFollow = findViewById(R.id.btn_follow);
        tabLayout = findViewById(R.id.tab_layout);
        rvProducts = findViewById(R.id.rv_products);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        tvEmpty = findViewById(R.id.tv_empty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Products"));
        tabLayout.addTab(tabLayout.newTab().setText("Reviews"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "products";
                        loadUserProducts();
                        break;
                    case 1:
                        currentTab = "reviews";
                        loadUserReviews();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new ProductGridAdapter(products, this);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshCurrentTab);

        // Hide action buttons for own profile
        if (isOwnProfile) {
            btnMessage.setVisibility(View.GONE);
            btnFollow.setVisibility(View.GONE);
        } else {
            btnMessage.setOnClickListener(v -> startChatWithUser());
            btnFollow.setOnClickListener(v -> toggleFollowUser());
        }
    }

    private void loadUserProfile() {
        Call<StandardResponse<User>> call = ApiClient.getUserService().getUserById(userId);
        call.enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    user = response.body().getData();
                    updateUserUI();
                } else {
                    Log.e(TAG, "Failed to load user profile: " + response.message());
                    Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading user profile", t);
                Toast.makeText(UserProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUserUI() {
        if (user == null) return;

        // User basic info
        tvName.setText(user.getDisplayNameOrFullName());

        if (user.getCreatedAt() != null) {
            tvJoinDate.setText("Joined " + user.getCreatedAt().substring(0, 4));
        }

        if (user.getLocation() != null && !user.getLocation().isEmpty()) {
            tvLocation.setText(user.getLocation());
            tvLocation.setVisibility(View.VISIBLE);
        } else {
            tvLocation.setVisibility(View.GONE);
        }

        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvBio.setText(user.getBio());
            tvBio.setVisibility(View.VISIBLE);
        } else {
            tvBio.setVisibility(View.GONE);
        }

        // Rating
        if (user.getAverageRating() != null && user.getAverageRating() > 0) {
            tvRating.setText(user.getFormattedRating());
            tvRating.setVisibility(View.VISIBLE);
        } else {
            tvRating.setVisibility(View.GONE);
        }

        // Avatar
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(user.getDisplayNameOrFullName());
        }
    }

    private void loadUserProducts() {
        products.clear();
        adapter.notifyDataSetChanged();

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService()
                .getUserProducts(userId, 0, 20);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                swipeRefresh.setRefreshing(false);
                handleProductsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Failed to load user products", t);
                showEmptyState("Failed to load products");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleProductsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) data.get("content");

                if (productMaps != null && !productMaps.isEmpty()) {
                    products.clear();
                    for (Map<String, Object> productMap : productMaps) {
                        Product product = parseProductFromMap(productMap);
                        if (product != null) {
                            products.add(product);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    hideEmptyState();
                } else {
                    showEmptyState("No products found");
                }
            } else {
                showEmptyState("Failed to load products");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing products response", e);
            showEmptyState("Error loading products");
        }
    }

    private Product parseProductFromMap(Map<String, Object> productMap) {
        // TODO: Implement proper product parsing
        try {
            Product product = new Product();
            product.setId(getLongFromMap(productMap, "id"));
            product.setTitle((String) productMap.get("title"));
            return product;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing product", e);
            return null;
        }
    }

    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private void loadUserReviews() {
        // TODO: Implement reviews loading
        showEmptyState("Reviews coming soon");
    }

    private void refreshCurrentTab() {
        if (currentTab.equals("products")) {
            loadUserProducts();
        } else {
            loadUserReviews();
        }
    }

    private void startChatWithUser() {
        if (user == null) return;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("sellerId", user.getId());
        intent.putExtra("sellerName", user.getDisplayNameOrFullName());
        startActivity(intent);
    }

    private void toggleFollowUser() {
        // TODO: Implement follow/unfollow functionality
        isFollowing = !isFollowing;
        updateFollowButton();

        String message = isFollowing ? "Following " + user.getDisplayNameOrFullName() : "Unfollowed";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("Unfollow");
            btnFollow.setBackgroundColor(getColor(R.color.secondary_color));
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundColor(getColor(R.color.primary_color));
        }
    }

    private void showEmptyState(String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
        rvProducts.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        tvEmpty.setVisibility(View.GONE);
        rvProducts.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isOwnProfile) {
            getMenuInflater().inflate(R.menu.user_profile_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_share) {
            shareUserProfile();
            return true;
        } else if (itemId == R.id.action_report) {
            reportUser();
            return true;
        } else if (itemId == R.id.action_block) {
            blockUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareUserProfile() {
        if (user == null) return;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Check out " + user.getDisplayNameOrFullName() + "'s profile on NewTrade!");
        startActivity(Intent.createChooser(shareIntent, "Share Profile"));
    }

    private void reportUser() {
        // TODO: Implement report user functionality
        Toast.makeText(this, "Report functionality coming soon", Toast.LENGTH_SHORT).show();
    }

    private void blockUser() {
        // TODO: Implement block user functionality
        Toast.makeText(this, "Block functionality coming soon", Toast.LENGTH_SHORT).show();
    }
}