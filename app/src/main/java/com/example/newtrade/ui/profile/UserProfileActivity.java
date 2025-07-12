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

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.utils.ImageUtils;
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

    // ✅ SỬA: Load real profile data from API
    private void loadUserProfile() {
        Log.d(TAG, "Loading profile for user: " + userId);

        // ✅ SỬA: Dùng UserService thay vì ApiService và handle real User object
        ApiClient.getUserService().getUserProfile(userId)
                .enqueue(new Callback<StandardResponse<User>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                           @NonNull Response<StandardResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // ✅ REAL DATA từ API - không cần parse
                            User user = response.body().getData();
                            userProfile = user;
                            displayUserProfile(user);
                            Log.d(TAG, "✅ Real profile loaded from API");
                        } else {
                            Log.e(TAG, "Failed to load user profile: " + response.code());
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to load user profile";
                            showErrorAndFallback(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Error loading user profile", t);
                        showErrorAndFallback("Network error while loading profile");
                    }
                });
    }

    // ✅ SỬA: Better error handling thay vì loadMockUserProfile
    private void showErrorAndFallback(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();

        // ✅ Show empty state instead của mock data
        tvDisplayName.setText("User Profile");
        tvMemberSince.setText("Member since recently");
        tvBio.setVisibility(View.GONE);
        llContactSection.setVisibility(View.GONE);
        tvRating.setVisibility(View.GONE);
        tvListingsCount.setText("0");
        tvSoldCount.setText("0");
        tvResponseRate.setText("--");
        ivProfilePicture.setImageResource(R.drawable.placeholder_avatar);

        Log.d(TAG, "Showing error state for user profile");
    }

    // ✅ SỬA: displayUserProfile để handle real User object
    private void displayUserProfile(User user) {
        if (user == null) return;

        try {
            // ✅ Basic info từ User object
            tvDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");

            // ✅ Member since từ createdAt
            if (user.getCreatedAt() != null) {
                String year = extractYearFromDate(user.getCreatedAt());
                tvMemberSince.setText("Member since " + year);
            } else {
                tvMemberSince.setText("Member since recently");
            }

            // ✅ Rating
            if (user.getRating() != null && user.getRating() > 0) {
                tvRating.setText(String.format("⭐ %.1f", user.getRating()));
                tvRating.setVisibility(View.VISIBLE);
            } else {
                tvRating.setVisibility(View.GONE);
            }

            // ✅ Bio - REAL DATA
            if (user.getBio() != null && !user.getBio().trim().isEmpty()) {
                tvBio.setText(user.getBio());
                tvBio.setVisibility(View.VISIBLE);
            } else {
                tvBio.setVisibility(View.GONE);
            }

            // ✅ Contact Info - REAL DATA
            if (user.getContactInfo() != null && !user.getContactInfo().trim().isEmpty()) {
                tvContactInfo.setText(user.getContactInfo());
                llContactSection.setVisibility(View.VISIBLE);
            } else {
                llContactSection.setVisibility(View.GONE);
            }

            // ✅ Profile Picture - REAL DATA
            if (user.getProfilePicture() != null && !user.getProfilePicture().trim().isEmpty()) {
                ImageUtils.loadAvatarImage(this, user.getProfilePicture(), ivProfilePicture);
            } else {
                ivProfilePicture.setImageResource(R.drawable.placeholder_avatar);
            }

            // ✅ Stats - REAL DATA (sẽ được update khi products load)
            tvListingsCount.setText(String.valueOf(userProducts.size()));
            tvSoldCount.setText(user.getTotalTransactions() != null ?
                    String.valueOf(user.getTotalTransactions()) : "0");
            tvResponseRate.setText("95%"); // TODO: Get from API if available

            // ✅ Update toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(user.getDisplayName());
            }

            Log.d(TAG, "✅ Real user profile displayed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error displaying user profile", e);
            showErrorAndFallback("Error displaying profile");
        }
    }

    // ✅ THÊM helper method
    private String extractYearFromDate(String dateStr) {
        try {
            // Backend date format: "2023-12-25T10:30:00" hoặc "2023-12-25"
            if (dateStr != null && dateStr.length() >= 4) {
                return dateStr.substring(0, 4);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date: " + dateStr, e);
        }
        return "2023"; // fallback
    }

    // ✅ SỬA: Load real user products
    private void loadUserProducts() {
        Log.d(TAG, "Loading products for user: " + userId);

        // ✅ SỬA: Load real products thay vì mock
        ApiClient.getProductService().getProductsByUser(userId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                           @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            parseUserProducts(data);
                            Log.d(TAG, "✅ Real user products loaded");
                        } else {
                            Log.e(TAG, "Failed to load user products: " + response.code());
                            updateProductsVisibility(); // Show empty state
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Error loading user products", t);
                        updateProductsVisibility(); // Show empty state
                    }
                });
    }

    // ✅ THÊM method để parse real products
    private void parseUserProducts(Map<String, Object> data) {
        userProducts.clear();

        try {
            // Parse paginated response like in MyListingsActivity
            List<Map<String, Object>> productList = extractProductsFromPaginatedResponse(data);

            if (productList != null) {
                for (Map<String, Object> productData : productList) {
                    Product product = parseProductFromData(productData);
                    if (product != null) {
                        userProducts.add(product);
                    }
                }
            }

            productAdapter.notifyDataSetChanged();
            updateProductsVisibility();

            // ✅ Update listings count sau khi load products
            tvListingsCount.setText(String.valueOf(userProducts.size()));

            Log.d(TAG, "✅ Parsed " + userProducts.size() + " user products");

        } catch (Exception e) {
            Log.e(TAG, "Error parsing user products", e);
            updateProductsVisibility();
        }
    }

    // ✅ Copy methods từ MyListingsActivity
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractProductsFromPaginatedResponse(Map<String, Object> paginatedData) {
        if (paginatedData == null) {
            Log.w(TAG, "Paginated data is null");
            return new ArrayList<>();
        }

        Log.d(TAG, "🔍 Paginated response keys: " + paginatedData.keySet());

        // Spring Boot pagination thường có field "content"
        Object contentObj = paginatedData.get("content");
        if (contentObj instanceof List) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) contentObj;
            Log.d(TAG, "✅ Found 'content' field with " + content.size() + " items");
            return content;
        }

        // Thử các field khác có thể có
        String[] possibleFields = {"items", "data", "products", "results"};
        for (String field : possibleFields) {
            Object fieldValue = paginatedData.get(field);
            if (fieldValue instanceof List) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) fieldValue;
                Log.d(TAG, "✅ Found '" + field + "' field with " + list.size() + " items");
                return list;
            }
        }

        Log.w(TAG, "❌ Could not find product list in paginated response");
        return new ArrayList<>();
    }

    private Product parseProductFromData(Map<String, Object> productData) {
        try {
            Product product = new Product();

            // Basic info
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

            // Condition
            String conditionStr = (String) productData.get("condition");
            if (conditionStr != null) {
                try {
                    Product.ProductCondition condition = Product.ProductCondition.valueOf(conditionStr);
                    product.setCondition(condition);
                } catch (IllegalArgumentException e) {
                    product.setCondition(Product.ProductCondition.GOOD);
                }
            }

            // Status
            String statusStr = (String) productData.get("status");
            if (statusStr != null) {
                try {
                    Product.ProductStatus status = Product.ProductStatus.valueOf(statusStr);
                    product.setStatus(status);
                } catch (IllegalArgumentException e) {
                    product.setStatus(Product.ProductStatus.AVAILABLE);
                }
            }

            // Image URLs
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) imageUrlsObj;
                product.setImageUrls(imageUrls);
            } else if (imageUrlsObj instanceof String) {
                List<String> imageUrls = new ArrayList<>();
                imageUrls.add((String) imageUrlsObj);
                product.setImageUrls(imageUrls);
            }

            // Additional fields
            Object viewCountObj = productData.get("viewCount");
            if (viewCountObj instanceof Number) {
                product.setViewCount(((Number) viewCountObj).intValue());
            }

            product.setCreatedAt((String) productData.get("createdAt"));
            product.setCategoryName((String) productData.get("categoryName"));

            return product;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing product data", e);
            return null;
        }
    }

    private void updateProductsVisibility() {
        if (userProducts.isEmpty()) {
            rvUserProducts.setVisibility(View.GONE);
            tvEmptyProducts.setVisibility(View.VISIBLE);
            tvEmptyProducts.setText("This user has no public listings");
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

        // Check if user is trying to contact themselves
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId != null && currentUserId.equals(userId)) {
            Toast.makeText(this, "You cannot message yourself", Toast.LENGTH_SHORT).show();
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

        // TODO: Implement view all listings activity hoặc show dialog với full list
        Toast.makeText(this, "View all " + userProducts.size() + " listings", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUtils.handleBackButton(this, item) || super.onOptionsItemSelected(item);
    }
}