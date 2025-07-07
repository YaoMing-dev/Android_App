// app/src/main/java/com/example/newtrade/ui/product/ProductDetailActivity.java
package com.example.newtrade.ui.product;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newtrade.ui.offer.MakeOfferBottomSheetDialogFragment;
import com.example.newtrade.ui.profile.UserProfileActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
// Thêm imports này nếu chưa có:
import com.example.newtrade.ui.profile.SavedItemsActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.utils.ImageUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.newtrade.utils.NavigationUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";

    // UI Components
    private MaterialToolbar toolbar;


    private ImageView ivProductImage;
    private TextView tvTitle, tvPrice, tvDescription, tvLocation, tvCondition;
    private TextView tvSellerName, tvSellerRating, tvMemberSince;
    private Button btnContact, btnMakeOffer, btnViewProfile;
    private FloatingActionButton fabShare, fabSave;

    // Data
    private Long productId;
    private String productTitle;
    private String productPrice;
    private Map<String, Object> productData;
    private SharedPrefsManager prefsManager;
    private boolean isItemSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize
        prefsManager = SharedPrefsManager.getInstance(this);

        // Get product data from intent
        getIntentData();

        // Initialize views
        initViews();
        setupToolbar();
        setupListeners();

        // Load product details
        loadProductDetails();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        productId = intent.getLongExtra("product_id", 0L);
        productTitle = intent.getStringExtra("product_title");
        productPrice = intent.getStringExtra("product_price");

        Log.d(TAG, "Product ID: " + productId + ", Title: " + productTitle);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);
        tvLocation = findViewById(R.id.tv_location);
        tvCondition = findViewById(R.id.tv_condition);
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvSellerRating = findViewById(R.id.tv_seller_rating);
        tvMemberSince = findViewById(R.id.tv_member_since);
        btnContact = findViewById(R.id.btn_contact);
        btnMakeOffer = findViewById(R.id.btn_make_offer);
        btnViewProfile = findViewById(R.id.btn_view_profile);
        fabShare = findViewById(R.id.fab_share);
        Button btnSave = findViewById(R.id.btn_save);


    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(productTitle != null ? productTitle : "Product Detail");
        }
    }

    private void setupListeners() {
        Button btnSave = findViewById(R.id.btn_save);

        // ✅ SAVE BUTTON LISTENER
        btnSave.setOnClickListener(v -> toggleSaveItem());

        // Existing listeners
        btnContact.setOnClickListener(v -> showContactOptions());
        btnMakeOffer.setOnClickListener(v -> makeOffer());
        btnViewProfile.setOnClickListener(v -> viewSellerProfile());
        fabShare.setOnClickListener(v -> shareProduct());
    }

    private void checkSaveStatus() {
        if (productId == null || productId <= 0) return;

        ApiClient.getSavedItemsService().isItemSaved(productId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            if (data != null && data.containsKey("isSaved")) {
                                isItemSaved = (Boolean) data.get("isSaved");
                                updateSaveButton();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "Error checking save status", t);
                    }
                });
    }

    private void toggleSaveItem() {
        if (productId == null || productId <= 0) return;

        Call<StandardResponse<String>> call;
        if (isItemSaved) {
            call = ApiClient.getSavedItemsService().removeSavedItem(productId);
        } else {
            call = ApiClient.getSavedItemsService().saveItem(productId);
        }

        call.enqueue(new Callback<StandardResponse<String>>() {
            @Override
            public void onResponse(Call<StandardResponse<String>> call,
                                   Response<StandardResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    isItemSaved = !isItemSaved;
                    updateSaveButton();

                    String message = isItemSaved ? "Item saved" : "Item removed from saved";
                    Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to update save status";
                    Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                Log.e(TAG, "Error toggling save status", t);
                Toast.makeText(ProductDetailActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSaveButton() {
        Button btnSave = findViewById(R.id.btn_save);
        if (btnSave != null) {
            if (isItemSaved) {
                btnSave.setText("💖 Saved");
                btnSave.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
            } else {
                btnSave.setText("❤️ Save");
                btnSave.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
            }
        }
    }

    private void loadProductDetails() {
        if (productId == null || productId <= 0) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading product details for ID: " + productId);

        // ✅ THÊM: Show loading state
        showLoadingState(true);

        ApiClient.getApiService().getProductDetail(productId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        // ✅ Hide loading state
                        showLoadingState(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> standardResponse = response.body();

                            if (standardResponse.isSuccess()) {
                                productData = standardResponse.getData();

                                // ✅ Display product data
                                displayProductData(productData);

                                // ✅ THÊM: Show FABs after data loaded
                                showFABs();

                                // ✅ THÊM: Check save status
                                checkSaveStatus();

                                Log.d(TAG, "Product details loaded successfully");

                            } else {
                                showError("Failed to load product: " + standardResponse.getMessage());
                            }
                        } else {
                            // ✅ IMPROVED: Better error handling
                            String errorMsg = "Failed to load product details";
                            if (response.code() == 404) {
                                errorMsg = "Product not found";
                            } else if (response.code() == 403) {
                                errorMsg = "Access denied";
                            } else if (response.code() >= 500) {
                                errorMsg = "Server error. Please try again later.";
                            }
                            showError(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        // ✅ Hide loading state
                        showLoadingState(false);

                        // ✅ IMPROVED: Better network error handling
                        String errorMsg = "Network error";
                        if (t.getMessage() != null) {
                            if (t.getMessage().contains("timeout")) {
                                errorMsg = "Connection timeout. Please try again.";
                            } else if (t.getMessage().contains("Unable to resolve host")) {
                                errorMsg = "No internet connection";
                            } else {
                                errorMsg = "Network error: " + t.getMessage();
                            }
                        }

                        showError(errorMsg);
                        Log.e(TAG, "Failed to load product details", t);
                    }
                });
    }

    private void showLoadingState(boolean isLoading) {
        Button btnSave = findViewById(R.id.btn_save);

        if (isLoading) {
            // Disable all buttons during loading
            if (btnSave != null) btnSave.setEnabled(false);
            if (btnContact != null) btnContact.setEnabled(false);
            if (btnMakeOffer != null) btnMakeOffer.setEnabled(false);

            Log.d(TAG, "Showing loading state");
        } else {
            // Enable all buttons after loading
            if (btnSave != null) btnSave.setEnabled(true);
            if (btnContact != null) btnContact.setEnabled(true);
            if (btnMakeOffer != null) btnMakeOffer.setEnabled(true);

            Log.d(TAG, "Hiding loading state");
        }
    }
    private void showFABs() {
        if (fabShare != null) {
            fabShare.setVisibility(View.VISIBLE);
            fabShare.show();
            Log.d(TAG, "FAB Share shown");
        }
    }


    // ✅ THÊM: Retry dialog for network errors
    private void showRetryDialog(String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Connection Error")
                .setMessage(errorMessage + "\n\nWould you like to try again?")
                .setPositiveButton("Retry", (dialog, which) -> {
                    loadProductDetails(); // Retry loading
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    finish(); // Close activity
                })
                .setCancelable(false)
                .show();
    }

    private void displayProductData(Map<String, Object> data) {
        try {
            // Basic product info
            if (data.get("title") != null) {
                tvTitle.setText(data.get("title").toString());
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(data.get("title").toString());
                }
            }

            if (data.get("description") != null) {
                tvDescription.setText(data.get("description").toString());
            }

            if (data.get("location") != null) {
                tvLocation.setText(data.get("location").toString());
            }

            if (data.get("condition") != null) {
                tvCondition.setText(data.get("condition").toString());
            }

            // Price formatting
            if (data.get("price") != null) {
                Object priceObj = data.get("price");
                if (priceObj instanceof Number) {
                    BigDecimal price = new BigDecimal(priceObj.toString());
                    tvPrice.setText(formatPrice(price));
                }
            }

            // ✅ SETUP PRODUCT IMAGE USING ImageUtils
            setupProductImage(data);

            // Seller information
            if (data.get("seller") != null || data.get("user") != null) {
                Map<String, Object> seller = (Map<String, Object>)
                        (data.get("seller") != null ? data.get("seller") : data.get("user"));

                if (seller.get("displayName") != null) {
                    tvSellerName.setText(seller.get("displayName").toString());
                }

                if (seller.get("rating") != null) {
                    tvSellerRating.setText("★ " + seller.get("rating").toString());
                }

                if (seller.get("createdAt") != null) {
                    tvMemberSince.setText("Member since " +
                            seller.get("createdAt").toString().substring(0, 4));
                }
            }

            // ✅ THÊM: Check save status sau khi load xong product
            checkSaveStatus();

        } catch (Exception e) {
            Log.e(TAG, "Error displaying product data", e);
        }
    }

    // ✅ THÊM METHOD setupProductImage
    private void setupProductImage(Map<String, Object> productData) {
        if (ivProductImage == null || productData == null) return;

        try {
            String imageUrl = null;

            // Try different ways to get image URL from backend response
            if (productData.get("primaryImageUrl") != null) {
                imageUrl = productData.get("primaryImageUrl").toString();
            } else if (productData.get("imageUrl") != null) {
                imageUrl = productData.get("imageUrl").toString();
            } else if (productData.get("imageUrls") != null) {
                Object imageUrlsObj = productData.get("imageUrls");
                if (imageUrlsObj instanceof List) {
                    List<String> imageUrls = (List<String>) imageUrlsObj;
                    if (!imageUrls.isEmpty()) {
                        imageUrl = imageUrls.get(0);
                    }
                }
            }

            // Use ImageUtils to load the image
            ImageUtils.loadProductImage(this, imageUrl, ivProductImage);

            Log.d(TAG, "Product image setup complete. URL: " + imageUrl);

        } catch (Exception e) {
            Log.e(TAG, "Error setting up product image", e);
            ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "Free";
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " VNĐ";
    }

    // ✅ FIXED: showContactOptions với safe ID parsing
    private void showContactOptions() {
        if (productData == null) return;

        try {
            // Get seller info
            Map<String, Object> seller = (Map<String, Object>)
                    (productData.get("seller") != null ? productData.get("seller") : productData.get("user"));

            if (seller == null) {
                Toast.makeText(this, "Seller information not available", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ FIX: Parse ID an toàn cho cả Integer và Double
            Long sellerId = parseToLong(seller.get("id"));
            String sellerName = seller.get("displayName") != null ? seller.get("displayName").toString() : "Seller";

            if (sellerId == null || sellerId <= 0) {
                Toast.makeText(this, "Invalid seller ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Starting chat with seller ID: " + sellerId + ", name: " + sellerName);

            // Start chat
            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.putExtra("product_id", productId);
            chatIntent.putExtra("seller_id", sellerId);
            chatIntent.putExtra("seller_name", sellerName);
            startActivity(chatIntent);

        } catch (Exception e) {
            Log.e(TAG, "Error starting chat", e);
            Toast.makeText(this, "Unable to start conversation", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ THÊM HELPER METHOD để parse ID an toàn
    private Long parseToLong(Object value) {
        if (value == null) return null;

        try {
            String valueStr = value.toString();

            // Nếu là số thập phân, chuyển thành số nguyên
            if (valueStr.contains(".")) {
                return Math.round(Double.parseDouble(valueStr));
            }

            // Nếu là số nguyên
            return Long.parseLong(valueStr);

        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing ID: " + value, e);
            return null;
        }
    }

    // ✅ FIXED: makeOffer với đơn giản hóa
    private void makeOffer() {
        MakeOfferBottomSheetDialogFragment bottomSheet =
                MakeOfferBottomSheetDialogFragment.newInstance(productId, productPrice);

        bottomSheet.setOnOfferSubmittedListener(new MakeOfferBottomSheetDialogFragment.OnOfferSubmittedListener() {
            @Override
            public void onOfferSubmitted(boolean success, String message) {
                if (success) {
                    Log.d(TAG, "Offer submitted successfully: " + message);
                    // Optional: Refresh product data or show some indication
                } else {
                    Log.e(TAG, "Offer submission failed: " + message);
                }
            }
        });

        bottomSheet.show(getSupportFragmentManager(), "make_offer");
    }

// ✅ XÓA method submitOffer cũ vì đã move vào Fragment

// ✅ THÊM import cần thiết vào đầu file


    private void submitOffer(String amount, String message) {
        Map<String, Object> offerData = new HashMap<>();
        offerData.put("productId", productId);
        offerData.put("amount", Double.parseDouble(amount));
        offerData.put("message", message);

        // TODO: Implement offer submission API call
        Toast.makeText(this, "Offer submitted: " + amount + " VNĐ", Toast.LENGTH_SHORT).show();
    }

    // ✅ FIXED: viewSellerProfile với safe ID parsing
    private void viewSellerProfile() {
        if (productData == null) return;

        try {
            Map<String, Object> seller = (Map<String, Object>)
                    (productData.get("seller") != null ? productData.get("seller") : productData.get("user"));

            if (seller != null && seller.get("id") != null) {
                // ✅ FIX: Parse ID an toàn
                Long sellerId = parseToLong(seller.get("id"));

                if (sellerId != null && sellerId > 0) {
                    Intent intent = new Intent(this, UserProfileActivity.class);
                    intent.putExtra("user_id", sellerId);
                    startActivity(intent);

                    Log.d(TAG, "Opening seller profile for ID: " + sellerId);
                } else {
                    Toast.makeText(this, "Invalid seller ID", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Seller information not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error viewing seller profile", e);
            Toast.makeText(this, "Unable to view seller profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareProduct() {
        if (productData == null) return;

        String shareText = "Check out this item: " +
                (productData.get("title") != null ? productData.get("title").toString() : "Product") +
                " - " +
                (productData.get("price") != null ? productData.get("price").toString() + " VNĐ" : "");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Product"));
    }

    private void saveProduct() {
        // TODO: Implement save product functionality
        Toast.makeText(this, "Product saved to favorites", Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);

        // ✅ THÊM: Show retry option for network errors
        if (message.contains("Network") || message.contains("timeout") || message.contains("internet")) {
            showRetryDialog(message);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}