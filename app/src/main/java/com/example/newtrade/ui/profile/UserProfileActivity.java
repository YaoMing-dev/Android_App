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

import java.math.BigDecimal;
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

        if (userId <= 0) {
            Log.e(TAG, "Invalid user ID");
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

        // ✅ FIX: Check if btn_view_reviews exists in layout
        btnViewReviews = findViewById(R.id.btn_view_reviews);
        if (btnViewReviews == null) {
            Log.w(TAG, "btn_view_reviews not found in layout - this is optional");
        }

        rvUserProducts = findViewById(R.id.rv_user_products);
        llEmptyState = findViewById(R.id.ll_empty_state);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Profile");
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(userProducts, this::openProductDetail);
        rvUserProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvUserProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        btnContact.setOnClickListener(v -> contactUser());

        // ✅ FIX: Only set listener if button exists
        if (btnViewReviews != null) {
            btnViewReviews.setOnClickListener(v -> viewUserReviews());
        }
    }

    private void loadUserProfile() {
        // TODO: Replace with actual API call to get user profile
        createMockUserProfile();
    }

    private void createMockUserProfile() {
        // Mock user profile data
        tvDisplayName.setText("John Seller");
        tvMemberSince.setText("Member since Jan 2023");
        tvLocation.setText("Ho Chi Minh City, Vietnam");
        tvListingsCount.setText("12 listings");
        tvRating.setText("4.8 ★");
        tvResponseRate.setText("95% response rate");

        // Load profile picture
        Glide.with(this)
                .load("https://via.placeholder.com/150x150")
                .centerCrop()
                .placeholder(R.drawable.ic_person)
                .into(ivProfilePicture);

        Log.d(TAG, "✅ Mock user profile loaded");
    }

    private void loadUserProducts() {
        // Create mock products for this user
        createMockUserProducts();
    }

    private void createMockUserProducts() {
        userProducts.clear();

        // Mock product 1
        Product product1 = createMockProduct(101L, "Vintage Camera",
                "Canon AE-1 vintage film camera", 1500000.0,
                "Ho Chi Minh City", "GOOD");

        // Mock product 2
        Product product2 = createMockProduct(102L, "Gaming Laptop",
                "ASUS ROG gaming laptop", 18000000.0,
                "Ho Chi Minh City", "LIKE_NEW");

        // Mock product 3
        Product product3 = createMockProduct(103L, "Wireless Headphones",
                "Sony WH-1000XM4 wireless headphones", 6500000.0,
                "Ho Chi Minh City", "NEW");

        userProducts.add(product1);
        userProducts.add(product2);
        userProducts.add(product3);

        if (userProducts.isEmpty()) {
            showEmptyState();
        } else {
            showProducts();
        }

        productAdapter.notifyDataSetChanged();
        Log.d(TAG, "✅ Mock user products created: " + userProducts.size());
    }

    private Product createMockProduct(Long id, String title, String description,
                                      Double price, String location, String condition) {
        Product product = new Product();

        product.setId(id);
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setLocation(location);
        product.setCondition(condition);
        product.setStatus("AVAILABLE");
        product.setImageUrl("https://via.placeholder.com/300x300");
        product.setPrimaryImageUrl("https://via.placeholder.com/300x300");
        product.setCreatedAt("2024-01-10T15:30:00");
        product.setUpdatedAt("2024-01-10T15:30:00");
        product.setUserId(userId);
        product.setCategoryId(1L);
        product.setCategoryName("Electronics");
        product.setViewCount(42);

        return product;
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, com.example.newtrade.ui.product.ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_price", product.getFormattedPrice());
        startActivity(intent);
    }

    private void contactUser() {
        if (userId != null && userId > 0) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("other_user_id", userId);
            intent.putExtra("other_user_name", tvDisplayName.getText().toString());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Cannot contact user", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewUserReviews() {
        Toast.makeText(this, "User reviews coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showProducts() {
        if (rvUserProducts != null) rvUserProducts.setVisibility(View.VISIBLE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        if (rvUserProducts != null) rvUserProducts.setVisibility(View.GONE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
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