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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductImageAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.ui.offer.MakeOfferActivity;
import com.example.newtrade.ui.profile.UserProfileActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private static final int REQUEST_MAKE_OFFER = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private ViewPager2 vpProductImages;
    private TabLayout tlImageIndicator;
    private TextView tvTitle, tvPrice, tvDescription, tvLocation, tvCondition;
    private TextView tvSellerName, tvSellerRating, tvMemberSince, tvViewCount;
    private CircleImageView ivSellerAvatar;
    private Button btnContact, btnMakeOffer, btnViewProfile;
    private FloatingActionButton fabShare, fabSave;
    private View layoutSellerInfo;

    // Adapters
    private ProductImageAdapter imageAdapter;

    // Data
    private Long productId;
    private String productTitle;
    private String productPrice;
    private Map<String, Object> productData;
    private Map<String, Object> sellerData;
    private SharedPrefsManager prefsManager;
    private boolean isProductSaved = false;
    private boolean isOwnProduct = false;

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
        setupImageSlider();

        // Load product details
        loadProductDetails();
        recordProductView();

        Log.d(TAG, "✅ ProductDetailActivity created for product ID: " + productId);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        productId = intent.getLongExtra(Constants.EXTRA_PRODUCT_ID, -1L);
        productTitle = intent.getStringExtra(Constants.EXTRA_PRODUCT_TITLE);
        productPrice = intent.getStringExtra(Constants.EXTRA_PRODUCT_PRICE);

        if (productId == -1L) {
            Log.e(TAG, "❌ Product ID not provided in intent");
            finish();
            return;
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        vpProductImages = findViewById(R.id.vp_product_images);
        tlImageIndicator = findViewById(R.id.tl_image_indicator);
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);
        tvLocation = findViewById(R.id.tv_location);
        tvCondition = findViewById(R.id.tv_condition);
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvSellerRating = findViewById(R.id.tv_seller_rating);
        tvMemberSince = findViewById(R.id.tv_member_since);
        tvViewCount = findViewById(R.id.tv_view_count);
        ivSellerAvatar = findViewById(R.id.iv_seller_avatar);
        btnContact = findViewById(R.id.btn_contact);
        btnMakeOffer = findViewById(R.id.btn_make_offer);
        btnViewProfile = findViewById(R.id.btn_view_profile);
        fabShare = findViewById(R.id.fab_share);
        fabSave = findViewById(R.id.fab_save);
        layoutSellerInfo = findViewById(R.id.layout_seller_info);

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(productTitle != null ? productTitle : "Product Details");
        }
    }

    private void setupListeners() {
        btnContact.setOnClickListener(v -> contactSeller());
        btnMakeOffer.setOnClickListener(v -> makeOffer());
        btnViewProfile.setOnClickListener(v -> viewSellerProfile());
        fabShare.setOnClickListener(v -> shareProduct());
        fabSave.setOnClickListener(v -> toggleSaveProduct());

        layoutSellerInfo.setOnClickListener(v -> viewSellerProfile());
    }

    private void setupImageSlider() {
        imageAdapter = new ProductImageAdapter(new ArrayList<>());
        vpProductImages.setAdapter(imageAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tlImageIndicator, vpProductImages,
                (tab, position) -> {
                    // Tab configuration is handled automatically
                }
        ).attach();
    }

    private void loadProductDetails() {
        Log.d(TAG, "🔍 Loading product details for ID: " + productId);

        ApiClient.getProductService().getProduct(productId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                productData = apiResponse.getData();
                                displayProductDetails();
                                checkIfProductSaved();
                                Log.d(TAG, "✅ Product details loaded successfully");
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Product not found");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load product details", t);
                        showError("Network error. Please try again.");
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void displayProductDetails() {
        if (productData == null) return;

        try {
            // Product basic info
            tvTitle.setText((String) productData.get("title"));
            tvDescription.setText((String) productData.get("description"));
            tvLocation.setText((String) productData.get("location"));
            tvCondition.setText((String) productData.get("condition"));

            // Price formatting
            Number priceNumber = (Number) productData.get("price");
            if (priceNumber != null) {
                tvPrice.setText(Constants.formatPrice(priceNumber.doubleValue()));
            }

            // View count
            Number viewCount = (Number) productData.get("viewCount");
            if (viewCount != null) {
                tvViewCount.setText(viewCount.toString() + " views");
            }

            // Product images
            List<String> imageUrls = (List<String>) productData.get("imageUrls");
            if (imageUrls != null && !imageUrls.isEmpty()) {
                // Convert to full URLs
                List<String> fullImageUrls = new ArrayList<>();
                for (String imageUrl : imageUrls) {
                    fullImageUrls.add(Constants.getImageUrl(imageUrl));
                }
                imageAdapter.updateImages(fullImageUrls);

                // Show/hide image indicator based on image count
                if (imageUrls.size() > 1) {
                    tlImageIndicator.setVisibility(View.VISIBLE);
                } else {
                    tlImageIndicator.setVisibility(View.GONE);
                }
            }

            // Seller info
            sellerData = (Map<String, Object>) productData.get("seller");
            if (sellerData != null) {
                displaySellerInfo();
            }

            // Check if this is user's own product
            Long currentUserId = prefsManager.getUserId();
            if (currentUserId != null && sellerData != null) {
                Number sellerIdNumber = (Number) sellerData.get("id");
                if (sellerIdNumber != null) {
                    isOwnProduct = currentUserId.equals(sellerIdNumber.longValue());
                    updateActionButtons();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error displaying product details", e);
            showError("Error displaying product details");
        }
    }

    private void displaySellerInfo() {
        if (sellerData == null) return;

        try {
            tvSellerName.setText((String) sellerData.get("displayName"));

            // Seller rating
            Number rating = (Number) sellerData.get("rating");
            if (rating != null) {
                tvSellerRating.setText(String.format(Locale.getDefault(), "%.1f ★", rating.doubleValue()));
            }

            // Member since
            String createdAt = (String) sellerData.get("createdAt");
            if (createdAt != null) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
                    Date date = inputFormat.parse(createdAt);
                    if (date != null) {
                        tvMemberSince.setText("Member since " + outputFormat.format(date));
                    }
                } catch (Exception e) {
                    tvMemberSince.setText("Member since " + createdAt);
                }
            }

            // Seller avatar
            String avatarUrl = (String) sellerData.get("profilePicture");
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this)
                        .load(Constants.getImageUrl(avatarUrl))
                        .placeholder(R.drawable.ic_placeholder_avatar)
                        .error(R.drawable.ic_placeholder_avatar)
                        .circleCrop()
                        .into(ivSellerAvatar);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error displaying seller info", e);
        }
    }

    private void updateActionButtons() {
        if (isOwnProduct) {
            // Hide buyer actions for own products
            btnContact.setVisibility(View.GONE);
            btnMakeOffer.setVisibility(View.GONE);
            btnViewProfile.setVisibility(View.GONE);

            // Show edit option
            btnContact.setText("Edit Product");
            btnContact.setVisibility(View.VISIBLE);
            btnContact.setOnClickListener(v -> editProduct());
        } else {
            // Show buyer actions
            btnContact.setVisibility(View.VISIBLE);
            btnMakeOffer.setVisibility(View.VISIBLE);
            btnViewProfile.setVisibility(View.VISIBLE);

            // Check if product allows offers
            Boolean isNegotiable = (Boolean) productData.get("isNegotiable");
            if (isNegotiable != null && !isNegotiable) {
                btnMakeOffer.setVisibility(View.GONE);
            }
        }
    }

    private void recordProductView() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        // Don't record views for own products
        if (isOwnProduct) return;

        ApiClient.getProductService().recordProductView(userId, productId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "✅ Product view recorded");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.d(TAG, "Failed to record product view");
                    }
                });
    }

    private void checkIfProductSaved() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        ApiClient.getSavedItemService().isItemSaved(userId, productId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Map<String, Object> data = apiResponse.getData();
                                Boolean isSaved = (Boolean) data.get("isSaved");
                                isProductSaved = isSaved != null && isSaved;
                                updateSaveButton();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.d(TAG, "Failed to check if product is saved");
                    }
                });
    }

    private void updateSaveButton() {
        if (isProductSaved) {
            fabSave.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            fabSave.setImageResource(R.drawable.ic_bookmark_border);
        }
    }

    private void contactSeller() {
        if (isOwnProduct) {
            editProduct();
            return;
        }

        if (sellerData == null) return;

        // Start conversation with seller
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please log in to contact seller", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "🔄 Starting conversation with seller");

        ApiClient.getChatService().findOrCreateConversation(userId, productId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Map<String, Object> conversationData = apiResponse.getData();
                                Long conversationId = ((Number) conversationData.get("id")).longValue();

                                // Navigate to chat
                                Intent intent = new Intent(ProductDetailActivity.this, ChatActivity.class);
                                intent.putExtra(Constants.EXTRA_CONVERSATION_ID, conversationId);
                                intent.putExtra(Constants.EXTRA_PRODUCT_ID, productId);
                                startActivity(intent);

                                Log.d(TAG, "✅ Conversation started: " + conversationId);
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to start conversation");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to start conversation", t);
                        showError("Network error. Please try again.");
                    }
                });
    }

    private void makeOffer() {
        if (isOwnProduct) return;

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please log in to make an offer", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to make offer activity
        Intent intent = new Intent(this, MakeOfferActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, productId);
        intent.putExtra(Constants.EXTRA_PRODUCT_TITLE, tvTitle.getText().toString());

        String priceStr = tvPrice.getText().toString().replaceAll("[^0-9.]", "");
        try {
            double price = Double.parseDouble(priceStr);
            intent.putExtra(Constants.EXTRA_PRODUCT_PRICE, price);
        } catch (NumberFormatException e) {
            intent.putExtra(Constants.EXTRA_PRODUCT_PRICE, 0.0);
        }

        startActivityForResult(intent, REQUEST_MAKE_OFFER);
    }

    private void viewSellerProfile() {
        if (sellerData == null) return;

        Number sellerIdNumber = (Number) sellerData.get("id");
        if (sellerIdNumber == null) return;

        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra(Constants.EXTRA_USER_ID, sellerIdNumber.longValue());
        intent.putExtra(Constants.EXTRA_USER_NAME, (String) sellerData.get("displayName"));
        startActivity(intent);
    }

    private void shareProduct() {
        String shareText = "Check out this product: " + tvTitle.getText().toString() +
                "\nPrice: " + tvPrice.getText().toString() +
                "\nLocation: " + tvLocation.getText().toString() +
                "\n\nShared from TradeUp app";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Product from TradeUp");

        startActivity(Intent.createChooser(shareIntent, "Share product"));
    }

    private void toggleSaveProduct() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please log in to save products", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isProductSaved) {
            // Remove from saved items
            ApiClient.getSavedItemService().removeSavedItem(userId, productId)
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                               Response<StandardResponse<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();
                                if (apiResponse.isSuccess()) {
                                    isProductSaved = false;
                                    updateSaveButton();
                                    Toast.makeText(ProductDetailActivity.this, "Removed from saved items", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                            Log.e(TAG, "❌ Failed to remove from saved items", t);
                        }
                    });
        } else {
            // Add to saved items
            ApiClient.getSavedItemService().saveItem(userId, productId)
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                               Response<StandardResponse<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();
                                if (apiResponse.isSuccess()) {
                                    isProductSaved = true;
                                    updateSaveButton();
                                    Toast.makeText(ProductDetailActivity.this, "Added to saved items", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                            Log.e(TAG, "❌ Failed to save item", t);
                        }
                    });
        }
    }

    private void editProduct() {
        // Navigate to edit product activity
        Toast.makeText(this, "Edit product functionality", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MAKE_OFFER && resultCode == RESULT_OK) {
            Toast.makeText(this, "Offer submitted successfully", Toast.LENGTH_SHORT).show();
            // Refresh product details to show updated offer count
            loadProductDetails();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.w(TAG, "Error: " + message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ProductDetailActivity destroyed");
    }
}