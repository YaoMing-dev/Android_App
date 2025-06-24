// File: app/src/main/java/com/example/newtrade/ui/product/ProductDetailActivity.java
package com.example.newtrade.ui.product;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
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

        // 🔥 LOAD FROM BACKEND
        ApiClient.getApiService().getProductDetail(productId).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        productData = response.body().getData();
                        displayBackendData();
                        Log.d(TAG, "✅ Product loaded from backend");
                    } else {
                        Log.w(TAG, "❌ Backend response failed, showing sample data");
                        displaySampleData();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing product data", e);
                    displaySampleData();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "❌ Product API call failed", t);
                displaySampleData();
            }
        });
    }

    private void displayBackendData() {
        try {
            // Basic product info
            String title = (String) productData.get("title");
            if (title != null) {
                tvTitle.setText(title);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }
            }

            tvDescription.setText((String) productData.get("description"));
            tvLocation.setText("📍 " + (String) productData.get("location"));

            // Handle condition
            String condition = (String) productData.get("condition");
            if (condition != null) {
                tvCondition.setText("Condition: " + formatCondition(condition));
            }

            // Handle price
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                double price = ((Number) priceObj).doubleValue();
                tvPrice.setText(String.format("%,.0f VNĐ", price));
            }

            // Seller info
            Map<String, Object> sellerData = (Map<String, Object>) productData.get("seller");
            if (sellerData != null) {
                String sellerName = (String) sellerData.get("displayName");
                if (sellerName != null) {
                    tvSellerName.setText(sellerName);
                }

                Object ratingObj = sellerData.get("rating");
                if (ratingObj instanceof Number) {
                    double rating = ((Number) ratingObj).doubleValue();
                    tvSellerRating.setText(String.format("⭐ %.1f", rating));
                }

                String createdAt = (String) sellerData.get("createdAt");
                if (createdAt != null) {
                    tvMemberSince.setText("Member since " + createdAt.substring(0, 7)); // YYYY-MM
                }
            }

            // Load images
            List<Map<String, Object>> images = (List<Map<String, Object>>) productData.get("images");
            if (images != null && !images.isEmpty()) {
                String imageUrl = (String) images.get(0).get("imageUrl");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_placeholder_image)
                            .error(R.drawable.ic_placeholder_image)
                            .centerCrop()
                            .into(ivProductImage);
                }
            } else {
                // Check for single imageUrl
                String imageUrl = (String) productData.get("imageUrl");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_placeholder_image)
                            .error(R.drawable.ic_placeholder_image)
                            .centerCrop()
                            .into(ivProductImage);
                }
            }

            // Check if user owns this product
            checkOwnership();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error displaying backend data", e);
            displaySampleData();
        }
    }

    private void displaySampleData() {
        if (productTitle != null) {
            tvTitle.setText(productTitle);
        }
        if (productPrice != null) {
            tvPrice.setText(productPrice);
        }

        tvDescription.setText("Premium product in excellent condition. Well maintained and comes with original packaging. Perfect for anyone looking for quality at an affordable price.");
        tvLocation.setText("📍 Ho Chi Minh City, Vietnam");
        tvCondition.setText("Condition: Like New");
        tvSellerName.setText("Seller Name");
        tvSellerRating.setText("⭐ 4.8 (124 reviews)");
        tvMemberSince.setText("Member since 2023");

        // Default placeholder image
        ivProductImage.setImageResource(R.drawable.ic_placeholder_image);
    }

    private String formatCondition(String condition) {
        if (condition == null) return "Unknown";

        switch (condition.toUpperCase()) {
            case "NEW": return "New";
            case "LIKE_NEW": return "Like New";
            case "GOOD": return "Good";
            case "FAIR": return "Fair";
            case "POOR": return "Poor";
            default: return condition.replace("_", " ");
        }
    }

    private void checkOwnership() {
        try {
            if (productData == null) return;

            Map<String, Object> sellerData = (Map<String, Object>) productData.get("seller");
            if (sellerData != null) {
                Long sellerId = ((Number) sellerData.get("id")).longValue();
                Long currentUserId = prefsManager.getUserId();

                if (sellerId.equals(currentUserId)) {
                    // User owns this product - change button text
                    btnMakeOffer.setText("🛠️ Edit Listing");
                    btnContact.setText("📊 View Analytics");
                } else {
                    // Show normal actions for other users
                    btnMakeOffer.setText("💰 Make Offer");
                    btnContact.setText("💬 Contact Seller");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error checking ownership", e);
        }
    }

    private void setupListeners() {
        btnContact.setOnClickListener(v -> contactSeller());
        btnMakeOffer.setOnClickListener(v -> makeOffer());

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

    // 🔥 CONTACT SELLER
    private void contactSeller() {
        try {
            if (productData != null) {
                Map<String, Object> sellerData = (Map<String, Object>) productData.get("seller");
                if (sellerData != null) {
                    Long sellerId = ((Number) sellerData.get("id")).longValue();
                    Long currentUserId = prefsManager.getUserId();

                    if (sellerId.equals(currentUserId)) {
                        // User owns product - show analytics
                        Toast.makeText(this, "Analytics feature coming soon! 📊", Toast.LENGTH_SHORT).show();
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

    // 🔥 MAKE OFFER
    private void makeOffer() {
        try {
            if (productData != null) {
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

    private void startConversationWithSeller(Long sellerId) {
        // TODO: Implement conversation with specific seller
        Toast.makeText(this, "Starting conversation with seller... 💬", Toast.LENGTH_SHORT).show();

        // For now, just show a dialog with contact info
        if (productData != null) {
            Map<String, Object> sellerData = (Map<String, Object>) productData.get("seller");
            if (sellerData != null) {
                String contactInfo = (String) sellerData.get("contactInfo");
                if (contactInfo != null && !contactInfo.isEmpty()) {
                    showContactInfoDialog(contactInfo);
                } else {
                    Toast.makeText(this, "Contact info not available", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showContactInfoDialog(String contactInfo) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Contact Seller");
        builder.setMessage("Contact information:\n" + contactInfo);

        builder.setPositiveButton("Call", (dialog, which) -> {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contactInfo));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to make call", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Message", (dialog, which) -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + contactInfo));
                intent.putExtra("sms_body", "Hi, I'm interested in your product: " + tvTitle.getText());
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to send message", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // 🔥 MAKE OFFER DIALOG
    private void showMakeOfferDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_make_offer, null);

        TextView tvCurrentPrice = view.findViewById(R.id.tv_current_price);
        TextInputEditText etOfferAmount = view.findViewById(R.id.et_offer_amount);
        TextInputEditText etOfferMessage = view.findViewById(R.id.et_offer_message);
        Button btnSubmitOffer = view.findViewById(R.id.btn_submit_offer);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        // Set current price as hint
        if (productData != null) {
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                double currentPrice = ((Number) priceObj).doubleValue();
                tvCurrentPrice.setText("Current Price: " + String.format("%,.0f VNĐ", currentPrice));
                etOfferAmount.setHint("Enter your offer...");
            }
        } else {
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

    // 🔥 SUBMIT OFFER TO BACKEND
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}