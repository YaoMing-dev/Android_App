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
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private CircleImageView ivProfilePicture;
    private TextView tvDisplayName, tvMemberSince, tvLocation;
    private TextView tvListingsCount, tvRating, tvResponseRate;
    private Button btnContact;
    // ✅ FIX: Make btnViewReviews optional (may not exist in layout)
    private Button btnViewReviews;
    private RecyclerView rvUserProducts;
    private LinearLayout llEmptyState;

    // Data
    private ProductAdapter productAdapter;
    private List<Product> userProducts = new ArrayList<>();
    private SharedPrefsManager prefsManager;
    private Long userId;
    private User userProfile;

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
        Intent intent = getIntent();
        userId = intent.getLongExtra("user_id", -1L);

        if (userId == -1L) {
            Log.e(TAG, "❌ No user ID provided");
            Toast.makeText(this, "Invalid user profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvMemberSince = findViewById(R.id.tv_member_since);
        tvLocation = findViewById(R.id.tv_location);
        tvListingsCount = findViewById(R.id.tv_listings_count);
        tvRating = findViewById(R.id.tv_rating);
        tvResponseRate = findViewById(R.id.tv_response_rate);
        btnContact = findViewById(R.id.btn_contact);
        btnViewReviews = findViewById(R.id.btn_view_reviews); // May be null if not in layout
        rvUserProducts = findViewById(R.id.rv_user_products);
        llEmptyState = findViewById(R.id.ll_empty_state);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "User Profile");
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvUserProducts.setLayoutManager(layoutManager);

        productAdapter = new ProductAdapter(userProducts, product -> {
            // Navigate to product details
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        rvUserProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        btnContact.setOnClickListener(v -> startChat());

        if (btnViewReviews != null) {
            btnViewReviews.setOnClickListener(v -> viewUserReviews());
        }
    }

    private void loadUserProfile() {
        Log.d(TAG, "Loading profile for user: " + userId);

        ApiClient.getApiService().getUserById(userId)
            .enqueue(new Callback<StandardResponse<User>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                       @NonNull Response<StandardResponse<User>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<User> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            userProfile = apiResponse.getData();
                            updateProfileUI();
                            Log.d(TAG, "✅ User profile loaded: " + userProfile.getDisplayName());
                        } else {
                            showError("Failed to load user profile");
                        }
                    } else {
                        showError("User not found");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                    showError("Network error: " + t.getMessage());
                    Log.e(TAG, "Failed to load user profile", t);
                }
            });
    }

    private void updateProfileUI() {
        if (userProfile == null) return;

        // Profile picture
        if (userProfile.getProfilePicture() != null && !userProfile.getProfilePicture().isEmpty()) {
            Glide.with(this)
                .load(userProfile.getProfilePicture())
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(ivProfilePicture);
        }

        // Basic info
        tvDisplayName.setText(userProfile.getDisplayName());

        // Member since (if available)
        if (userProfile.getCreatedAt() != null) {
            tvMemberSince.setText("Member since " + formatMemberSince(userProfile.getCreatedAt()));
        }

        // Location (if available in user model)
        if (userProfile.getLocation() != null && !userProfile.getLocation().isEmpty()) {
            tvLocation.setText(userProfile.getLocation());
            tvLocation.setVisibility(View.VISIBLE);
        } else {
            tvLocation.setVisibility(View.GONE);
        }

        // Stats
        updateUserStats();

        // Check if this is current user's profile
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId != null && currentUserId.equals(userId)) {
            // Hide contact button for own profile
            btnContact.setVisibility(View.GONE);
        } else {
            btnContact.setVisibility(View.VISIBLE);
        }
    }

    private void updateUserStats() {
        if (userProfile == null) return;

        // Listings count (will be updated when products load)
        tvListingsCount.setText(userProducts.size() + " listings");

        // Rating
        if (userProfile.getRating() != null && userProfile.getRating() > 0) {
            tvRating.setText(String.format("%.1f ⭐", userProfile.getRating()));
            tvRating.setVisibility(View.VISIBLE);
        } else {
            tvRating.setText("No ratings yet");
        }

        // Total transactions
        if (userProfile.getTotalTransactions() != null) {
            tvResponseRate.setText(userProfile.getTotalTransactions() + " transactions");
        } else {
            tvResponseRate.setText("0 transactions");
        }
    }

    private void loadUserProducts() {
        Log.d(TAG, "Loading products for user: " + userId);

        ApiClient.getApiService().getUserProducts(userId, 0, 20)
            .enqueue(new Callback<StandardResponse<List<Product>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<List<Product>>> call,
                                       @NonNull Response<StandardResponse<List<Product>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<List<Product>> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            List<Product> products = apiResponse.getData();

                            userProducts.clear();
                            if (products != null) {
                                userProducts.addAll(products);
                            }

                            productAdapter.notifyDataSetChanged();
                            updateListingsCount();
                            updateEmptyState();

                            Log.d(TAG, "✅ User products loaded: " + userProducts.size());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<List<Product>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failed to load user products", t);
                    updateEmptyState();
                }
            });
    }

    private void startChat() {
        if (userProfile == null) return;

        // Create or get conversation with this user
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("other_user_id", userId);
        chatIntent.putExtra("other_user_name", userProfile.getDisplayName());
        startActivity(chatIntent);

        Log.d(TAG, "Starting chat with user: " + userProfile.getDisplayName());
    }

    private void viewUserReviews() {
        // Navigate to user reviews activity
        Intent intent = new Intent(this, UserReviewsActivity.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("user_name", userProfile != null ? userProfile.getDisplayName() : "User");
        startActivity(intent);
    }

    private void updateListingsCount() {
        tvListingsCount.setText(userProducts.size() + " listings");
    }

    private void updateEmptyState() {
        if (userProducts.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvUserProducts.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvUserProducts.setVisibility(View.VISIBLE);
        }
    }

    private String formatMemberSince(String createdAt) {
        // Simple format - you can improve this with proper date formatting
        try {
            // Assuming createdAt is in format "2025-06-27T14:16:56"
            return createdAt.substring(0, 7); // Returns "2025-06"
        } catch (Exception e) {
            return "Recently";
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}