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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newtrade.ui.offer.MakeOfferBottomSheetDialogFragment;
import com.example.newtrade.ui.profile.UserProfileActivity;
import com.example.newtrade.ui.payment.PaymentActivity;
import com.example.newtrade.models.Transaction;
import com.example.newtrade.api.TransactionService;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

// Thêm imports này nếu chưa có:
import com.example.newtrade.ui.profile.SavedItemsActivity;
import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductImageAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.ui.review.WriteReviewActivity;
import com.example.newtrade.ui.transaction.TransactionDetailActivity;
import com.example.newtrade.utils.ImageUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.newtrade.utils.NavigationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
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
    private ProgressBar progressBar;

    // ✅ Image Gallery Components
    private ViewPager2 vpProductImages;
    private TabLayout tabImageIndicators;
    private TextView tvImageCounter;
    private ImageView ivProductImage;

    // Product Info Components
    private TextView tvTitle, tvPrice, tvDescription, tvLocation, tvCondition;
    private TextView tvSellerName, tvSellerRating, tvMemberSince;
    private Button btnContact, btnMakeOffer, btnViewProfile, btnBuyNow;
    private FloatingActionButton fabShare, fabSave;

    // ✅ Image Gallery Data
    private ProductImageAdapter imageAdapter;
    private List<String> productImages = new ArrayList<>();

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
        prefsManager = new SharedPrefsManager(this); // ✅ SỬA LẠI CONSTRUCTOR

        // Get product data from intent
        getIntentData();

        // Initialize views
        initViews();
        setupToolbar();

        // ✅ THÊM: Setup image gallery
        setupImageGallery();

        setupListeners();

        // Load product details
        loadProductDetails();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        productId = intent.getLongExtra("product_id", 0L);
        productTitle = intent.getStringExtra("product_title");
        productPrice = intent.getStringExtra("product_price");

        Log.d(TAG, "📱 Product ID: " + productId + ", Title: " + productTitle);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);

        // ✅ Image Gallery Views
        vpProductImages = findViewById(R.id.vp_product_images);
        tabImageIndicators = findViewById(R.id.tab_image_indicators);
        tvImageCounter = findViewById(R.id.tv_image_counter);
        ivProductImage = findViewById(R.id.iv_product_image);

        // Product Info Views
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);
        tvLocation = findViewById(R.id.tv_location);
        tvCondition = findViewById(R.id.tv_condition);

        // Seller Info Views
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvSellerRating = findViewById(R.id.tv_seller_rating);
        tvMemberSince = findViewById(R.id.tv_member_since);

        // Action Buttons
        btnContact = findViewById(R.id.btn_contact);
        btnMakeOffer = findViewById(R.id.btn_make_offer);
        btnViewProfile = findViewById(R.id.btn_view_profile);
        btnBuyNow = findViewById(R.id.btn_buy_now); // ✅ THÊM BUY NOW BUTTON
        fabShare = findViewById(R.id.fab_share);

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(productTitle != null ? productTitle : "Product Detail");
        }
    }

    // ✅ NEW: Setup image gallery
    private void setupImageGallery() {
        // Initialize image adapter
        imageAdapter = new ProductImageAdapter(this, productImages);
        vpProductImages.setAdapter(imageAdapter);

        // Setup indicators with TabLayoutMediator
        new TabLayoutMediator(tabImageIndicators, vpProductImages, (tab, position) -> {
            // Empty implementation - just shows dots
        }).attach();

        // Setup page change listener for counter
        vpProductImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateImageCounter(position + 1, productImages.size());
            }
        });

        Log.d(TAG, "✅ Image gallery setup complete");
    }

    // ✅ NEW: Update image counter
    private void updateImageCounter(int current, int total) {
        if (tvImageCounter != null && total > 1) {
            tvImageCounter.setText(current + "/" + total);
            tvImageCounter.setVisibility(View.VISIBLE);
        } else if (tvImageCounter != null) {
            tvImageCounter.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        Button btnSave = findViewById(R.id.btn_save);

        // ✅ SAVE BUTTON LISTENER
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> toggleSaveItem());
        }

        // Existing listeners
        btnContact.setOnClickListener(v -> showContactOptions());
        btnMakeOffer.setOnClickListener(v -> makeOffer());
        btnViewProfile.setOnClickListener(v -> viewSellerProfile());
        fabShare.setOnClickListener(v -> shareProduct());

        // ✅ THÊM BUY NOW LISTENER
        if (btnBuyNow != null) {
            btnBuyNow.setOnClickListener(v -> startPaymentFlow());
        }
    }

    // ✅ NEW: Start payment flow
    private void startPaymentFlow() {
        if (productData == null) {
            Toast.makeText(this, "Product information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user is logged in
        if (!prefsManager.isLoggedIn()) {
            Toast.makeText(this, "Please login to make a purchase", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to login
            return;
        }

        // Check if user is trying to buy their own product
        Long currentUserId = prefsManager.getUserId();
        Map<String, Object> seller = (Map<String, Object>)
                (productData.get("seller") != null ? productData.get("seller") : productData.get("user"));

        if (seller != null && currentUserId != null) {
            Long sellerId = parseToLong(seller.get("id"));
            if (currentUserId.equals(sellerId)) {
                Toast.makeText(this, "You cannot buy your own product", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Check if product is still available
        String status = productData.get("status") != null ? productData.get("status").toString() : "";
        if ("SOLD".equalsIgnoreCase(status) || "UNAVAILABLE".equalsIgnoreCase(status)) {
            Toast.makeText(this, "This item is no longer available", Toast.LENGTH_SHORT).show();
            return;
        }

        // First create a transaction, then start payment
        createTransactionForPayment();
    }

    // ✅ NEW: Create transaction for payment
    private void createTransactionForPayment() {
        showLoading(true);

        String userId = String.valueOf(prefsManager.getUserId());

        // ✅ SỬA: Gọi với @Query parameters và đúng enum values
        Call<StandardResponse<Transaction>> call = ApiClient.getTransactionService()
                .createTransaction(
                        userId,
                        productId,                  // productId
                        null,                       // offerId (optional)
                        "CREDIT_CARD",              // ✅ SỬA: CREDIT_CARD thay vì CARD
                        "PICKUP",                   // deliveryMethod
                        null,                       // deliveryAddress (optional)
                        "Purchase from product detail"  // notes
                );

        call.enqueue(new Callback<StandardResponse<Transaction>>() {
            @Override
            public void onResponse(Call<StandardResponse<Transaction>> call, Response<StandardResponse<Transaction>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Transaction> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        Transaction transaction = standardResponse.getData();
                        launchPaymentActivity(transaction);
                        Log.d(TAG, "✅ Transaction created successfully: " + transaction.getId());
                    } else {
                        showError("Failed to create transaction: " + standardResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ Error: " + response.code() + " - " + response.message());
                    showError("Failed to create transaction");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Transaction>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "❌ Failed to create transaction", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    // ✅ NEW: Launch payment activity
    private void launchPaymentActivity(Transaction transaction) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_TRANSACTION_ID, transaction.getId());
        intent.putExtra(PaymentActivity.EXTRA_AMOUNT, transaction.getFinalAmount().doubleValue());
        intent.putExtra(PaymentActivity.EXTRA_CURRENCY, "VND");
        intent.putExtra(PaymentActivity.EXTRA_DESCRIPTION, "Purchase: " +
                (productData.get("title") != null ? productData.get("title").toString() : "Product"));

        startActivityForResult(intent, Constants.RC_PAYMENT);

        Log.d(TAG, "🚀 Launched payment activity for transaction: " + transaction.getId());
    }

    // ✅ NEW: Handle payment result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_PAYMENT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Payment successful! 🎉", Toast.LENGTH_LONG).show();
                loadProductDetails();
                showPaymentSuccessDialog(data);
            } else {
                Toast.makeText(this, "Payment was cancelled", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.REQUEST_CODE_WRITE_REVIEW) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Thank you for your review! ⭐", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ✅ NEW: Show payment success dialog
    private void showPaymentSuccessDialog(Intent data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Successful! 🎉");
        builder.setMessage("Your payment has been processed successfully. You can now contact the seller and leave a review.");

        // ✅ THÊM: Write Review button
        builder.setPositiveButton("Write Review", (dialog, which) -> {
            if (data != null) {
                Long transactionId = data.getLongExtra("transaction_id", 0L);
                if (transactionId > 0) {
                    // Ensure transaction is completed first
                    completeTransactionAndOpenReview(transactionId);
                } else {
                    Toast.makeText(this, "Unable to find transaction", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNeutralButton("Contact Seller", (dialog, which) -> {
            showContactOptions();
        });

        builder.setNegativeButton("View Transaction", (dialog, which) -> {
            if (data != null) {
                Long transactionId = data.getLongExtra("transaction_id", 0L);
                if (transactionId > 0) {
                    Intent intent = new Intent(this, TransactionDetailActivity.class);
                    intent.putExtra(TransactionDetailActivity.EXTRA_TRANSACTION_ID, transactionId);
                    startActivity(intent);
                }
            }
        });

        builder.show();
    }

    private void completeTransactionAndOpenReview(Long transactionId) {
        String userId = String.valueOf(prefsManager.getUserId());

        // First, complete the transaction
        ApiClient.getTransactionService().completeTransactionWithAuth(userId, transactionId)
                .enqueue(new Callback<StandardResponse<Transaction>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Transaction>> call,
                                           Response<StandardResponse<Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // Transaction completed, now open review
                            openWriteReviewActivity(transactionId);
                        } else {
                            // Still allow review even if complete fails
                            openWriteReviewActivity(transactionId);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Transaction>> call, Throwable t) {
                        Log.e(TAG, "Error completing transaction", t);
                        // Still allow review
                        openWriteReviewActivity(transactionId);
                    }
                });
    }
    private void openWriteReviewActivity(Long transactionId) {
        Intent intent = new Intent(this, WriteReviewActivity.class);
        intent.putExtra(WriteReviewActivity.EXTRA_TRANSACTION_ID, transactionId);

        // Get seller info for review
        if (productData.get("seller") != null) {
            Map<String, Object> seller = (Map<String, Object>) productData.get("seller");
            String sellerName = seller.get("displayName") != null ? seller.get("displayName").toString() : "Seller";
            intent.putExtra(WriteReviewActivity.EXTRA_REVIEWEE_NAME, sellerName);
        }

        String productTitle = productData.get("title") != null ? productData.get("title").toString() : "Product";
        intent.putExtra(WriteReviewActivity.EXTRA_PRODUCT_TITLE, productTitle);

        startActivityForResult(intent, Constants.REQUEST_CODE_WRITE_REVIEW);

        Log.d(TAG, "🌟 Opened WriteReviewActivity for transaction: " + transactionId);
    }


    private void checkSaveStatus() {
        if (productId == null || productId <= 0) return;

        ApiClient.getProductService().isProductSaved(productId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            if (data != null && data.containsKey("saved")) {
                                isItemSaved = (Boolean) data.get("saved");
                                updateSaveButton();
                                Log.d(TAG, "✅ Save status checked: " + isItemSaved);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Error checking save status", t);
                    }
                });
    }

    private void toggleSaveItem() {
        if (productId == null || productId <= 0) return;

        Call<StandardResponse<Void>> call;
        if (isItemSaved) {
            call = ApiClient.getProductService().unsaveProduct(productId);
        } else {
            call = ApiClient.getProductService().saveProduct(productId);
        }

        call.enqueue(new Callback<StandardResponse<Void>>() {
            @Override
            public void onResponse(Call<StandardResponse<Void>> call,
                                   Response<StandardResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    isItemSaved = !isItemSaved;
                    updateSaveButton();

                    String message = isItemSaved ? "Product saved!" : "Product removed from saved";
                    Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ Save toggled: " + isItemSaved);
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to update save status";
                    Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Void>> call, Throwable t) {
                Log.e(TAG, "❌ Error toggling save status", t);
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

        Log.d(TAG, "🔄 Loading product details for ID: " + productId);

        // Show loading state
        showLoadingState(true);

        ApiClient.getApiService().getProductDetail(productId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        // Hide loading state
                        showLoadingState(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> standardResponse = response.body();

                            if (standardResponse.isSuccess()) {
                                productData = standardResponse.getData();

                                // Display product data
                                displayProductData(productData);

                                // Show FABs after data loaded
                                showFABs();

                                // Check save status
                                checkSaveStatus();

                                // Update buy button based on product status
                                updateBuyButton();

                                Log.d(TAG, "✅ Product details loaded successfully");

                            } else {
                                showError("Failed to load product: " + standardResponse.getMessage());
                            }
                        } else {
                            // Better error handling
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
                        // Hide loading state
                        showLoadingState(false);

                        // Better network error handling
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
                        Log.e(TAG, "❌ Failed to load product details", t);
                    }
                });
    }

    // ✅ NEW: Update buy button based on product status
    private void updateBuyButton() {
        if (btnBuyNow == null || productData == null) return;

        String status = productData.get("status") != null ? productData.get("status").toString() : "";

        // Check if product is sold or unavailable
        if ("SOLD".equalsIgnoreCase(status)) {
            btnBuyNow.setText("SOLD");
            btnBuyNow.setEnabled(false);
            btnBuyNow.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            return;
        }

        if ("UNAVAILABLE".equalsIgnoreCase(status)) {
            btnBuyNow.setText("UNAVAILABLE");
            btnBuyNow.setEnabled(false);
            btnBuyNow.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            return;
        }

        // Check if user is the owner
        Long currentUserId = prefsManager.getUserId();
        Map<String, Object> seller = (Map<String, Object>)
                (productData.get("seller") != null ? productData.get("seller") : productData.get("user"));

        if (seller != null && currentUserId != null) {
            Long sellerId = parseToLong(seller.get("id"));
            if (currentUserId.equals(sellerId)) {
                btnBuyNow.setText("YOUR ITEM");
                btnBuyNow.setEnabled(false);
                btnBuyNow.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
                return;
            }
        }

        // Check login status
        if (!prefsManager.isLoggedIn()) {
            btnBuyNow.setText("LOGIN TO BUY");
            btnBuyNow.setEnabled(true);
            btnBuyNow.setBackgroundTintList(getColorStateList(R.color.primary));
            return;
        }

        // Default state - available for purchase
        btnBuyNow.setText("BUY NOW");
        btnBuyNow.setEnabled(true);
        btnBuyNow.setBackgroundTintList(getColorStateList(R.color.primary));
    }

    private void showLoadingState(boolean isLoading) {
        Button btnSave = findViewById(R.id.btn_save);

        if (isLoading) {
            // Show progress bar
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

            // Disable all buttons during loading
            if (btnSave != null) btnSave.setEnabled(false);
            if (btnContact != null) btnContact.setEnabled(false);
            if (btnMakeOffer != null) btnMakeOffer.setEnabled(false);
            if (btnBuyNow != null) btnBuyNow.setEnabled(false);

            Log.d(TAG, "🔄 Showing loading state");
        } else {
            // Hide progress bar
            if (progressBar != null) progressBar.setVisibility(View.GONE);

            // Enable all buttons after loading
            if (btnSave != null) btnSave.setEnabled(true);
            if (btnContact != null) btnContact.setEnabled(true);
            if (btnMakeOffer != null) btnMakeOffer.setEnabled(true);
            if (btnBuyNow != null) btnBuyNow.setEnabled(true);

            Log.d(TAG, "✅ Hiding loading state");
        }
    }

    // ✅ NEW: Show loading overlay
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        // Disable interaction during loading
        if (btnBuyNow != null) btnBuyNow.setEnabled(!show);
        if (btnContact != null) btnContact.setEnabled(!show);
        if (btnMakeOffer != null) btnMakeOffer.setEnabled(!show);
    }

    private void showFABs() {
        if (fabShare != null) {
            fabShare.setVisibility(View.VISIBLE);
            fabShare.show();
            Log.d(TAG, "✅ FAB Share shown");
        }
    }

    // Retry dialog for network errors
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

            // ✅ SETUP PRODUCT IMAGES - Support multiple images
            setupProductImages(data);

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

            Log.d(TAG, "✅ Product data displayed successfully");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error displaying product data", e);
        }
    }

    // ✅ ENHANCED: Setup product images with gallery support
    private void setupProductImages(Map<String, Object> productData) {
        if (productData == null) return;

        try {
            productImages.clear();

            // ✅ TRY TO GET MULTIPLE IMAGES FIRST
            if (productData.get("images") != null) {
                Object imagesObj = productData.get("images");
                if (imagesObj instanceof List) {
                    List<String> images = (List<String>) imagesObj;
                    if (!images.isEmpty()) {
                        productImages.addAll(images);
                        Log.d(TAG, "✅ Found " + images.size() + " images in 'images' field");
                    }
                }
            }

            // ✅ FALLBACK TO imageUrls array
            if (productImages.isEmpty() && productData.get("imageUrls") != null) {
                Object imageUrlsObj = productData.get("imageUrls");
                if (imageUrlsObj instanceof List) {
                    List<String> imageUrls = (List<String>) imageUrlsObj;
                    if (!imageUrls.isEmpty()) {
                        productImages.addAll(imageUrls);
                        Log.d(TAG, "✅ Found " + imageUrls.size() + " images in 'imageUrls' field");
                    }
                }
            }

            // ✅ FALLBACK TO SINGLE IMAGE
            if (productImages.isEmpty()) {
                String singleImageUrl = null;
                if (productData.get("primaryImageUrl") != null) {
                    singleImageUrl = productData.get("primaryImageUrl").toString();
                } else if (productData.get("imageUrl") != null) {
                    singleImageUrl = productData.get("imageUrl").toString();
                }

                if (singleImageUrl != null && !singleImageUrl.isEmpty()) {
                    productImages.add(singleImageUrl);
                    Log.d(TAG, "✅ Found single image: " + singleImageUrl);
                }
            }

            // ✅ DISPLAY IMAGES BASED ON COUNT
            if (productImages.size() > 1) {
                // Multiple images - show ViewPager2 gallery
                showImageGallery();
                Log.d(TAG, "✅ Displaying " + productImages.size() + " images in gallery");

            } else if (productImages.size() == 1) {
                // Single image - show in ImageView
                showSingleImage();
                Log.d(TAG, "✅ Displaying single image");

            } else {
                // No images - show placeholder
                showPlaceholderImage();
                Log.d(TAG, "📷 No product images available, showing placeholder");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up product images", e);
            showPlaceholderImage();
        }
    }

    // ✅ NEW: Show image gallery (multiple images)
    private void showImageGallery() {
        vpProductImages.setVisibility(View.VISIBLE);
        ivProductImage.setVisibility(View.GONE);
        tabImageIndicators.setVisibility(View.VISIBLE);

        // Update adapter with new images
        imageAdapter.updateImages(productImages);

        // Update counter
        updateImageCounter(1, productImages.size());

        Log.d(TAG, "✅ Image gallery visible with " + productImages.size() + " images");
    }

    // ✅ NEW: Show single image
    private void showSingleImage() {
        vpProductImages.setVisibility(View.GONE);
        ivProductImage.setVisibility(View.VISIBLE);
        tabImageIndicators.setVisibility(View.GONE);
        if (tvImageCounter != null) tvImageCounter.setVisibility(View.GONE);

        // Load single image using ImageUtils
        String imageUrl = productImages.get(0);
        ImageUtils.loadProductImage(this, imageUrl, ivProductImage);

        Log.d(TAG, "✅ Single image displayed: " + imageUrl);
    }

    // ✅ NEW: Show placeholder
    private void showPlaceholderImage() {
        vpProductImages.setVisibility(View.GONE);
        ivProductImage.setVisibility(View.VISIBLE);
        tabImageIndicators.setVisibility(View.GONE);
        if (tvImageCounter != null) tvImageCounter.setVisibility(View.GONE);

        ivProductImage.setImageResource(R.drawable.ic_image_placeholder);

        Log.d(TAG, "📷 Placeholder image displayed");
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "Free";
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " VNĐ";
    }

    // Contact seller with safe ID parsing
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

            // Parse ID safely for both Integer and Double
            Long sellerId = parseToLong(seller.get("id"));
            String sellerName = seller.get("displayName") != null ? seller.get("displayName").toString() : "Seller";

            if (sellerId == null || sellerId <= 0) {
                Toast.makeText(this, "Invalid seller ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "🎯 Starting chat with seller ID: " + sellerId + ", name: " + sellerName);

            // Start chat
            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.putExtra("product_id", productId);
            chatIntent.putExtra("seller_id", sellerId);
            chatIntent.putExtra("seller_name", sellerName);
            startActivity(chatIntent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting chat", e);
            Toast.makeText(this, "Unable to start conversation", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to parse ID safely
    private Long parseToLong(Object value) {
        if (value == null) return null;

        try {
            String valueStr = value.toString();

            // If decimal number, convert to integer
            if (valueStr.contains(".")) {
                return Math.round(Double.parseDouble(valueStr));
            }

            // If integer
            return Long.parseLong(valueStr);

        } catch (NumberFormatException e) {
            Log.e(TAG, "❌ Error parsing ID: " + value, e);
            return null;
        }
    }

    // Make offer with simplified approach
    private void makeOffer() {
        MakeOfferBottomSheetDialogFragment bottomSheet =
                MakeOfferBottomSheetDialogFragment.newInstance(productId, productPrice);

        bottomSheet.setOnOfferSubmittedListener(new MakeOfferBottomSheetDialogFragment.OnOfferSubmittedListener() {
            @Override
            public void onOfferSubmitted(boolean success, String message) {
                if (success) {
                    Log.d(TAG, "✅ Offer submitted successfully: " + message);
                    // Optional: Refresh product data or show some indication
                } else {
                    Log.e(TAG, "❌ Offer submission failed: " + message);
                }
            }
        });

        bottomSheet.show(getSupportFragmentManager(), "make_offer");
    }

    private void submitOffer(String amount, String message) {
        Map<String, Object> offerData = new HashMap<>();
        offerData.put("productId", productId);
        offerData.put("amount", Double.parseDouble(amount));
        offerData.put("message", message);

        // TODO: Implement offer submission API call
        Toast.makeText(this, "Offer submitted: " + amount + " VNĐ", Toast.LENGTH_SHORT).show();
    }

    // View seller profile with safe ID parsing
    private void viewSellerProfile() {
        if (productData == null) return;

        try {
            Map<String, Object> seller = (Map<String, Object>)
                    (productData.get("seller") != null ? productData.get("seller") : productData.get("user"));

            if (seller != null && seller.get("id") != null) {
                // Parse ID safely
                Long sellerId = parseToLong(seller.get("id"));

                if (sellerId != null && sellerId > 0) {
                    Intent intent = new Intent(this, UserProfileActivity.class);
                    intent.putExtra("user_id", sellerId);
                    startActivity(intent);

                    Log.d(TAG, "🎯 Opening seller profile for ID: " + sellerId);
                } else {
                    Toast.makeText(this, "Invalid seller ID", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Seller information not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error viewing seller profile", e);
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

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "❌ Error: " + message);

        // Show retry option for network errors
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