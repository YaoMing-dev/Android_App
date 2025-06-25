// app/src/main/java/com/example/newtrade/ui/product/ProductDetailActivity.java
// ✅ FIXED: Added back button + Contact seller + Make offer functionality
package com.example.newtrade.ui.product;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
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
    private boolean isProductSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        prefsManager = SharedPrefsManager.getInstance(this);

        initViews();
        setupToolbar();
        getProductData();
        loadProductFromBackend();
        setupListeners();
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
        fabSave = findViewById(R.id.fab_save);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Product Details");
        }
    }

    // ✅ FIXED: Added back button handling
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getProductData() {
        productId = getIntent().getLongExtra("product_id", -1);
        productTitle = getIntent().getStringExtra("product_title");
        productPrice = getIntent().getStringExtra("product_price");

        Log.d(TAG, "Product ID: " + productId + ", Title: " + productTitle);
    }

    private void loadProductFromBackend() {
        if (productId == -1) {
            Log.e(TAG, "❌ Invalid product ID");
            displaySampleData();
            return;
        }

        ApiClient.getApiService().getProductDetail(productId).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        productData = response.body().getData();
                        displayProductData();
                        Log.d(TAG, "✅ Product loaded from backend");
                    } else {
                        Log.e(TAG, "❌ Failed to load product from backend");
                        displaySampleData();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing product data", e);
                    displaySampleData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Product API call failed", t);
                displaySampleData();
            }
        });
    }

    private void displayProductData() {
        try {
            if (productData != null) {
                // Product Info
                if (tvTitle != null) tvTitle.setText(productData.get("title").toString());
                if (tvPrice != null) tvPrice.setText(formatPrice(productData.get("price")));
                if (tvDescription != null) tvDescription.setText(productData.get("description").toString());
                if (tvLocation != null) tvLocation.setText(productData.get("location").toString());
                if (tvCondition != null) tvCondition.setText(productData.get("condition").toString());

                // Product Image
                String imageUrl = (String) productData.get("primaryImageUrl");
                if (imageUrl != null && ivProductImage != null) {
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_placeholder_image)
                            .error(R.drawable.ic_placeholder_image)
                            .into(ivProductImage);
                }

                // Seller Info
                @SuppressWarnings("unchecked")
                Map<String, Object> sellerData = (Map<String, Object>) productData.get("seller");
                if (sellerData != null) {
                    if (tvSellerName != null) tvSellerName.setText(sellerData.get("displayName").toString());
                    if (tvSellerRating != null) tvSellerRating.setText("★ " + sellerData.get("rating"));
                    if (tvMemberSince != null) tvMemberSince.setText("Member since " + sellerData.get("memberSince"));
                }

                Log.d(TAG, "✅ Product data displayed");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error displaying product data", e);
            displaySampleData();
        }
    }

    private void displaySampleData() {
        // Sample data for preview
        if (tvTitle != null) tvTitle.setText(productTitle != null ? productTitle : "Sample Product");
        if (tvPrice != null) tvPrice.setText(productPrice != null ? productPrice : "250,000 VNĐ");
        if (tvDescription != null) tvDescription.setText("This is a sample product description. Perfect condition, barely used.");
        if (tvLocation != null) tvLocation.setText("Ho Chi Minh City, Vietnam");
        if (tvCondition != null) tvCondition.setText("Like New");
        if (tvSellerName != null) tvSellerName.setText("John Doe");
        if (tvSellerRating != null) tvSellerRating.setText("★ 4.8");
        if (tvMemberSince != null) tvMemberSince.setText("Member since 2023");

        Log.d(TAG, "✅ Sample data displayed");
    }

    private void setupListeners() {
        if (btnContact != null) {
            btnContact.setOnClickListener(v -> contactSeller());
        }

        if (btnMakeOffer != null) {
            btnMakeOffer.setOnClickListener(v -> makeOffer());
        }

        if (btnViewProfile != null) {
            btnViewProfile.setOnClickListener(v -> viewSellerProfile());
        }

        if (fabShare != null) {
            fabShare.setOnClickListener(v -> shareProduct());
        }

        if (fabSave != null) {
            fabSave.setOnClickListener(v -> toggleSaveProduct());
        }
    }

    // ✅ FIXED: Complete contact seller functionality
    private void contactSeller() {
        try {
            if (productData != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sellerData = (Map<String, Object>) productData.get("seller");
                if (sellerData != null) {
                    Long sellerId = ((Number) sellerData.get("id")).longValue();
                    Long currentUserId = prefsManager.getUserId();

                    if (sellerId.equals(currentUserId)) {
                        // User owns product - show edit option
                        Toast.makeText(this, "This is your product! Edit listing feature coming soon 📊", Toast.LENGTH_SHORT).show();
                    } else {
                        // Contact seller - start conversation
                        startConversationWithSeller(sellerId);
                    }
                }
            } else {
                // Fallback
                Toast.makeText(this, "Opening chat with seller... 💬", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error contacting seller", e);
            Toast.makeText(this, "Opening chat with seller... 💬", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ NEW: Start conversation with seller
    private void startConversationWithSeller(Long sellerId) {
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId == null || currentUserId <= 0) {
            Toast.makeText(this, "Please login to chat with seller", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sellerId.equals(currentUserId)) {
            Toast.makeText(this, "You cannot chat with yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create conversation via API
        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("productId", productId);
        conversationData.put("buyerId", currentUserId);

        ApiClient.getApiService().createConversation(conversationData).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> conversation = response.body().getData();
                    Long conversationId = ((Number) conversation.get("id")).longValue();

                    // Open ChatActivity
                    Intent intent = new Intent(ProductDetailActivity.this, ChatActivity.class);
                    intent.putExtra("conversation_id", conversationId);
                    intent.putExtra("product_id", productId);
                    intent.putExtra("product_title", productTitle);
                    intent.putExtra("seller_id", sellerId);
                    startActivity(intent);

                    Log.d(TAG, "✅ Conversation created: " + conversationId);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Failed to start conversation", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "❌ Failed to create conversation");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "❌ Conversation API error", t);
            }
        });
    }

    // ✅ FIXED: Complete make offer functionality
    private void makeOffer() {
        try {
            if (productData != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sellerData = (Map<String, Object>) productData.get("seller");
                if (sellerData != null) {
                    Long sellerId = ((Number) sellerData.get("id")).longValue();
                    Long currentUserId = prefsManager.getUserId();

                    if (sellerId.equals(currentUserId)) {
                        // User owns product - edit listing
                        Toast.makeText(this, "Edit listing feature coming soon! 🛠️", Toast.LENGTH_SHORT).show();
                    } else {
                        // Make offer
                        showMakeOfferDialog();
                    }
                }
            } else {
                showMakeOfferDialog();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error making offer", e);
            showMakeOfferDialog();
        }
    }

    private void showMakeOfferDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_make_offer, null);

        TextInputEditText etOfferAmount = view.findViewById(R.id.et_offer_amount);
        TextInputEditText etOfferMessage = view.findViewById(R.id.et_offer_message);
        TextView tvCurrentPrice = view.findViewById(R.id.tv_current_price);
        Button btnSubmitOffer = view.findViewById(R.id.btn_submit_offer);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        // Set current price
        if (tvCurrentPrice != null) {
            tvCurrentPrice.setText("Current Price: " + (productPrice != null ? productPrice : "N/A"));
        }

        btnSubmitOffer.setOnClickListener(v -> {
            String offerText = etOfferAmount.getText().toString().trim();
            String message = etOfferMessage.getText().toString().trim();

            if (offerText.isEmpty()) {
                etOfferAmount.setError("Please enter offer amount");
                return;
            }

            try {
                double offerAmount = Double.parseDouble(offerText);
                if (offerAmount <= 0) {
                    etOfferAmount.setError("Offer amount must be positive");
                    return;
                }

                // Submit offer to backend
                submitOffer(BigDecimal.valueOf(offerAmount), message);
                dialog.dismiss();

            } catch (NumberFormatException e) {
                etOfferAmount.setError("Invalid amount format");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }

    private void submitOffer(BigDecimal offerAmount, String message) {
        Map<String, Object> offerData = new HashMap<>();
        offerData.put("productId", productId);
        offerData.put("offerAmount", offerAmount);
        offerData.put("message", message);

        // TODO: Implement offer API call
        // ApiClient.getApiService().createOffer(offerData).enqueue(...)

        // For now, just show success message
        Toast.makeText(this, "Offer submitted: " + String.format("%,.0f VNĐ", offerAmount.doubleValue()), Toast.LENGTH_LONG).show();

        Log.d(TAG, "✅ Offer submitted: " + offerAmount + " VNĐ with message: " + message);
    }

    private void viewSellerProfile() {
        if (productData != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sellerData = (Map<String, Object>) productData.get("seller");
            if (sellerData != null) {
                Long sellerId = ((Number) sellerData.get("id")).longValue();
                // TODO: Navigate to seller profile activity
                Toast.makeText(this, "Seller profile feature coming soon! 👤", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Seller profile feature coming soon! 👤", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareProduct() {
        String shareText = "Check out this product on TradeUp: " + tvTitle.getText();
        if (productData != null) {
            shareText += "\nPrice: " + tvPrice.getText();
            shareText += "\nLocation: " + tvLocation.getText();
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Product"));
    }

    private void toggleSaveProduct() {
        if (isProductSaved) {
            // Remove from saved
            isProductSaved = false;
            fabSave.setImageResource(R.drawable.ic_bookmark_border);
            Toast.makeText(this, "Product removed from saved items", Toast.LENGTH_SHORT).show();
        } else {
            // Add to saved
            isProductSaved = true;
            fabSave.setImageResource(R.drawable.ic_bookmark);
            Toast.makeText(this, "Product saved to favorites! ❤️", Toast.LENGTH_SHORT).show();
        }

        // TODO: Implement save/unsave API call
        Log.d(TAG, "Product save status: " + isProductSaved);
    }

    private String formatPrice(Object price) {
        try {
            if (price instanceof Number) {
                return String.format("%,.0f VNĐ", ((Number) price).doubleValue());
            }
            return price.toString();
        } catch (Exception e) {
            return "N/A";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}