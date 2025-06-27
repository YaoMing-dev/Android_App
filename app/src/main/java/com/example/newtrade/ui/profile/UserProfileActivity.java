// app/src/main/java/com/example/newtrade/ui/profile/UserProfileActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private CircleImageView ivProfilePicture;
    private TextView tvDisplayName, tvMemberSince, tvRating, tvBio, tvContactInfo;
    private TextView tvListingsCount, tvSoldCount, tvResponseRate;
    private Button btnContact, btnViewListings;
    private LinearLayout llUserStats, llContactSection;
    private RecyclerView rvUserProducts;
    private TextView tvEmptyProducts;

    // Data
    private Long userId;
    private User userProfile;
    private SharedPrefsManager prefsManager;
    private ProductAdapter productAdapter;
    private List<Product> userProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getUserIdFromIntent();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadUserProfile();
        loadUserProducts();

        Log.d(TAG, "UserProfileActivity created for user: " + userId);
    }

    private void getUserIdFromIntent() {
        userId = getIntent().getLongExtra("user_id", -1L);
        if (userId <= 0) {
            Log.e(TAG, "Invalid user ID");
            Toast.makeText(this, "Invalid user profile", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvMemberSince = findViewById(R.id.tv_member_since);
        tvRating = findViewById(R.id.tv_rating);
        tvBio = findViewById(R.id.tv_bio);
        tvContactInfo = findViewById(R.id.tv_contact_info);

        tvListingsCount = findViewById(R.id.tv_listings_count);
        tvSoldCount = findViewById(R.id.tv_sold_count);
        tvResponseRate = findViewById(R.id.tv_response_rate);

        btnContact = findViewById(R.id.btn_contact);
        btnViewListings = findViewById(R.id.btn_view_listings);

        llUserStats = findViewById(R.id.ll_user_stats);
        llContactSection = findViewById(R.id.ll_contact_section);
        rvUserProducts = findViewById(R.id.rv_user_products);
        tvEmptyProducts = findViewById(R.id.tv_empty_products);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "User Profile");
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(userProducts, product -> {
            // Open product detail
            Intent intent = new Intent(this, com.example.newtrade.ui.product.ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        rvUserProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvUserProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        btnContact.setOnClickListener(v -> contactUser());
        btnViewListings.setOnClickListener(v -> viewAllListings());
    }

    private void loadUserProfile() {
        Log.d(TAG, "Loading profile for user: " + userId);

        // First load mock data
        loadMockUserProfile();

        // TODO: Load real profile from API
        /*
        ApiClient.getUserService().getUserProfile(userId).enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(Call<StandardResponse<User>> call, Response<StandardResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    userProfile = response.body().getData();
                    displayUserProfile(userProfile);
                } else {
                    Log.e(TAG, "Failed to load user profile");
                    Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<User>> call, Throwable t) {
                Log.e(TAG, "Error loading user profile", t);
                Toast.makeText(UserProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    private void loadMockUserProfile() {
        // Create mock user profile
        userProfile = new User();
        userProfile.setId(userId);
        userProfile.setDisplayName("John Seller");
        userProfile.setEmail("john.seller@example.com");
        userProfile.setBio("Experienced seller with quality items. Fast shipping and excellent customer service.");
        userProfile.setContactInfo("+1 (555) 123-4567");
        userProfile.setRating(4.5);
        userProfile.setTotalTransactions(47);
        // userProfile.setCreatedAt(new Date()); // Member since

        displayUserProfile(userProfile);
    }

    private void displayUserProfile(User user) {
        if (user == null) return;

        // Basic info
        tvDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
        tvMemberSince.setText("Member since 2023");

        // Rating
        if (user.getRating() != null && user.getRating() > 0) {
            tvRating.setText(String.format("⭐ %.1f", user.getRating()));
            tvRating.setVisibility(View.VISIBLE);
        } else {
            tvRating.setVisibility(View.GONE);
        }

        // Bio
        if (user.getBio() != null && !user.getBio().trim().isEmpty()) {
            tvBio.setText(user.getBio());
            tvBio.setVisibility(View.VISIBLE);
        } else {
            tvBio.setVisibility(View.GONE);
        }

        // Contact info (only show if available)
        if (user.getContactInfo() != null && !user.getContactInfo().trim().isEmpty()) {
            tvContactInfo.setText(user.getContactInfo());
            llContactSection.setVisibility(View.VISIBLE);
        } else {
            llContactSection.setVisibility(View.GONE);
        }

        // Profile picture
        if (user.getProfilePicture() != null && !user.getProfilePicture().trim().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfilePicture())
                    .placeholder(R.drawable.placeholder_avatar)
                    .error(R.drawable.placeholder_avatar)
                    .into(ivProfilePicture);
        } else {
            ivProfilePicture.setImageResource(R.drawable.placeholder_avatar);
        }

        // Stats
        tvListingsCount.setText(String.valueOf(userProducts.size()));
        tvSoldCount.setText(user.getTotalTransactions() != null ?
                String.valueOf(user.getTotalTransactions()) : "0");
        tvResponseRate.setText("95%"); // Mock response rate

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(user.getDisplayName());
        }
    }

    private void loadUserProducts() {
        // Load mock products for now
        loadMockUserProducts();

        // TODO: Load real products from API
        /*
        ApiClient.getProductService().getUserProducts(userId, 0, 20).enqueue(new Callback<...>() {
            // Implementation here
        });
        */
    }

    private void loadMockUserProducts() {
        userProducts.clear();

        // Mock product 1
        Product product1 = new Product();
        product1.setId(1L);
        product1.setTitle("iPhone 13 Pro Max");
        product1.setPrice(new java.math.BigDecimal("25000000"));
        product1.setImageUrl("https://example.com/iphone.jpg");
        product1.setCondition(Product.ProductCondition.GOOD);
        userProducts.add(product1);

        // Mock product 2
        Product product2 = new Product();
        product2.setId(2L);
        product2.setTitle("MacBook Air M2");
        product2.setPrice(new java.math.BigDecimal("30000000"));
        product2.setImageUrl("https://example.com/macbook.jpg");
        product2.setCondition(Product.ProductCondition.LIKE_NEW);
        userProducts.add(product2);

        productAdapter.notifyDataSetChanged();
        updateProductsVisibility();
    }

    private void updateProductsVisibility() {
        if (userProducts.isEmpty()) {
            rvUserProducts.setVisibility(View.GONE);
            tvEmptyProducts.setVisibility(View.VISIBLE);
        } else {
            rvUserProducts.setVisibility(View.VISIBLE);
            tvEmptyProducts.setVisibility(View.GONE);
        }

        // Update listings count
        tvListingsCount.setText(String.valueOf(userProducts.size()));
    }

    private void contactUser() {
        if (userProfile == null) {
            Toast.makeText(this, "Profile not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open chat with this user
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("conversation_id", -1L); // Create new conversation
        intent.putExtra("seller_id", userId);
        intent.putExtra("other_user_name", userProfile.getDisplayName());
        startActivity(intent);

        Log.d(TAG, "Opening chat with user: " + userId);
    }

    private void viewAllListings() {
        if (userProducts.isEmpty()) {
            Toast.makeText(this, "This user has no listings", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Open all listings activity
        Toast.makeText(this, "View all " + userProducts.size() + " listings", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (NavigationUtils.handleBackButton(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}