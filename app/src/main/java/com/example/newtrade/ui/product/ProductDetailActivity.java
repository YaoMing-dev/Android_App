// app/src/main/java/com/example/newtrade/ui/product/ProductDetailActivity.java
package com.example.newtrade.ui.product;

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
import androidx.viewpager2.widget.ViewPager2;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.ui.offer.MakeOfferActivity;
import com.example.newtrade.ui.product.adapter.ProductImagePagerAdapter;
import com.example.newtrade.ui.profile.UserProfileActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";

    // UI Components
    private Toolbar toolbar;
    private ViewPager2 viewPagerImages;
    private TabLayout tabLayoutImages;
    private TextView tvTitle, tvPrice, tvDescription, tvLocation, tvSellerName, tvCreatedAt;
    private Chip chipCondition, chipStatus;
    private ImageView ivSellerAvatar;
    private MaterialButton btnContact, btnMakeOffer;
    private View layoutSeller;

    // Data
    private Product product;
    private Long productId;
    private boolean isSaved = false;

    // Utils
    private SharedPrefsManager prefsManager;
    private ProductImagePagerAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        prefsManager = new SharedPrefsManager(this);

        // Get product ID from intent
        productId = getIntent().getLongExtra(Constants.BUNDLE_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadProduct();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        viewPagerImages = findViewById(R.id.viewpager_images);
        tabLayoutImages = findViewById(R.id.tablayout_images);
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);
        tvLocation = findViewById(R.id.tv_location);
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvCreatedAt = findViewById(R.id.tv_created_at);
        chipCondition = findViewById(R.id.chip_condition);
        chipStatus = findViewById(R.id.chip_status);
        ivSellerAvatar = findViewById(R.id.iv_seller_avatar);
        btnContact = findViewById(R.id.btn_contact);
        btnMakeOffer = findViewById(R.id.btn_make_offer);
        layoutSeller = findViewById(R.id.layout_seller);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void loadProduct() {
        Call<StandardResponse<Product>> call = ApiClient.getProductService().getProductById(productId);
        call.enqueue(new Callback<StandardResponse<Product>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Product>> call,
                                   @NonNull Response<StandardResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    product = response.body().getData();
                    updateUI();
                    incrementViewCount();
                } else {
                    Log.e(TAG, "Failed to load product: " + response.message());
                    Toast.makeText(ProductDetailActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Product>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading product", t);
                Toast.makeText(ProductDetailActivity.this, "Error loading product", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI() {
        if (product == null) return;

        // Basic info
        tvTitle.setText(product.getTitle());
        tvPrice.setText(product.getFormattedPrice());

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            tvDescription.setText(product.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        if (product.getLocation() != null && !product.getLocation().isEmpty()) {
            tvLocation.setText(product.getLocation());
            tvLocation.setVisibility(View.VISIBLE);
        } else {
            tvLocation.setVisibility(View.GONE);
        }

        // Date
        if (product.getCreatedAt() != null) {
            tvCreatedAt.setText("Posted " + formatDate(product.getCreatedAt()));
        }

        // Condition
        if (product.getCondition() != null) {
            chipCondition.setText(product.getCondition().getDisplayName());
            chipCondition.setVisibility(View.VISIBLE);
        } else {
            chipCondition.setVisibility(View.GONE);
        }

        // Status
        if (product.getStatus() != null) {
            chipStatus.setText(product.getStatus().getDisplayName());
            setupStatusChip();
            chipStatus.setVisibility(View.VISIBLE);
        } else {
            chipStatus.setVisibility(View.GONE);
        }

        // Seller info
        if (product.getSeller() != null) {
            tvSellerName.setText(product.getSeller().getDisplayNameOrFullName());

            // Load seller avatar
            if (product.getSeller().getAvatarUrl() != null) {
                // TODO: Load with Glide
            }

            layoutSeller.setOnClickListener(v -> openSellerProfile());
        }

        // Images
        setupImagePager();

        // Action buttons
        setupActionButtons();
    }

    private void setupImagePager() {
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageAdapter = new ProductImagePagerAdapter(product.getImages());
            viewPagerImages.setAdapter(imageAdapter);

            // Setup tab indicator if multiple images
            if (product.getImages().size() > 1) {
                new TabLayoutMediator(tabLayoutImages, viewPagerImages,
                        (tab, position) -> {
                            // Tab setup is handled automatically
                        }).attach();
                tabLayoutImages.setVisibility(View.VISIBLE);
            } else {
                tabLayoutImages.setVisibility(View.GONE);
            }
        }
    }

    private void setupStatusChip() {
        if (product.getStatus() == null) return;

        switch (product.getStatus()) {
            case AVAILABLE:
                chipStatus.setChipBackgroundColorResource(R.color.status_available);
                break;
            case SOLD:
                chipStatus.setChipBackgroundColorResource(R.color.status_sold);
                break;
            case RESERVED:
                chipStatus.setChipBackgroundColorResource(R.color.status_reserved);
                break;
            default:
                chipStatus.setChipBackgroundColorResource(R.color.status_default);
                break;
        }
    }

    private void setupActionButtons() {
        if (product == null) return;

        // Check if user owns this product
        Long currentUserId = prefsManager.getUserId();
        boolean isOwner = currentUserId != null && product.getSeller() != null
                && currentUserId.equals(product.getSeller().getId());

        if (isOwner) {
            btnContact.setText("Edit Product");
            btnContact.setOnClickListener(v -> editProduct());
            btnMakeOffer.setVisibility(View.GONE);
        } else {
            // Check product status
            if (product.getStatus() == Product.ProductStatus.SOLD) {
                btnContact.setEnabled(false);
                btnContact.setText("Sold");
                btnMakeOffer.setVisibility(View.GONE);
            } else {
                btnContact.setText("Contact Seller");
                btnContact.setOnClickListener(v -> contactSeller());

                btnMakeOffer.setVisibility(View.VISIBLE);
                btnMakeOffer.setOnClickListener(v -> makeOffer());
            }
        }
    }

    private void incrementViewCount() {
        if (product == null) return;

        Long currentUserId = prefsManager.getUserId();
        if (currentUserId != null) {
            Call<StandardResponse<Void>> call = ApiClient.getProductService()
                    .incrementProductViews(productId, currentUserId);
            call.enqueue(new Callback<StandardResponse<Void>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                       @NonNull Response<StandardResponse<Void>> response) {
                    // Success - view count incremented
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                    // Ignore failure
                }
            });
        }
    }

    private void contactSeller() {
        if (product == null || product.getSeller() == null) return;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        intent.putExtra("sellerId", product.getSeller().getId());
        intent.putExtra("sellerName", product.getSeller().getDisplayNameOrFullName());
        startActivity(intent);
    }

    private void makeOffer() {
        if (product == null) return;

        Intent intent = new Intent(this, MakeOfferActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        intent.putExtra(Constants.BUNDLE_PRODUCT_TITLE, product.getTitle());
        intent.putExtra(Constants.BUNDLE_PRODUCT_PRICE, product.getPrice().toString());
        startActivity(intent);
    }

    private void editProduct() {
        if (product == null) return;

        Intent intent = new Intent(this, AddProductActivity.class);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
        intent.putExtra("editMode", true);
        startActivity(intent);
    }

    private void openSellerProfile() {
        if (product == null || product.getSeller() == null) return;

        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra(Constants.BUNDLE_USER_ID, product.getSeller().getId());
        startActivity(intent);
    }

    private String formatDate(String dateString) {
        // TODO: Implement proper date formatting
        try {
            if (dateString.length() >= 10) {
                return dateString.substring(0, 10);
            }
        } catch (Exception e) {
            // Ignore
        }
        return dateString;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_detail_menu, menu);

        // Update save icon based on state
        MenuItem saveItem = menu.findItem(R.id.action_save);
        if (saveItem != null) {
            saveItem.setIcon(isSaved ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_save) {
            toggleSaveProduct();
            return true;
        } else if (itemId == R.id.action_share) {
            shareProduct();
            return true;
        } else if (itemId == R.id.action_report) {
            reportProduct();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleSaveProduct() {
        // TODO: Implement save/unsave product
        isSaved = !isSaved;
        invalidateOptionsMenu();

        String message = isSaved ? "Product saved" : "Product removed from saved";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void shareProduct() {
        if (product == null) return;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Check out this product: " + product.getTitle() +
                        " - " + product.getFormattedPrice());
        startActivity(Intent.createChooser(shareIntent, "Share Product"));
    }

    private void reportProduct() {
        // TODO: Implement report product functionality
        Toast.makeText(this, "Report functionality coming soon", Toast.LENGTH_SHORT).show();
    }
}