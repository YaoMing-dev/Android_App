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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivProductImage;
    private TextView tvTitle, tvPrice, tvDescription, tvLocation, tvCondition;
    private TextView tvSellerName, tvSellerRating, tvMemberSince;
    private Button btnContact, btnMakeOffer, btnViewProfile;
    private FloatingActionButton fabShare, fabSave;

    private Long productId;
    private String productTitle;
    private String productPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        setupToolbar();
        getProductData();
        displayProductData();
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

        if (productId == -1) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayProductData() {
        // Use data from intent first, then call API for full details
        if (productTitle != null) {
            tvTitle.setText(productTitle);
        }
        if (productPrice != null) {
            tvPrice.setText(productPrice);
        }

        // Set sample data for missing fields
        tvDescription.setText("iPhone 15 Pro Max 256GB, màu xanh dương, còn 95% pin, full box. Máy mới mua 3 tháng, sử dụng cẩn thận, không có trầy xước.");
        tvLocation.setText("Ho Chi Minh City");
        tvCondition.setText("Like New");
        tvSellerName.setText("Nguyễn Văn A");
        tvSellerRating.setText("4.8 ⭐ (25 reviews)");
        tvMemberSince.setText("Member since Dec 2022");

        // Load placeholder image
        Glide.with(this)
                .load(R.drawable.ic_placeholder_image)
                .into(ivProductImage);

        // TODO: Call real API to get full product details
        // ApiClient.getProductService().getProductById(productId)
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        btnContact.setOnClickListener(v -> contactSeller());
        btnMakeOffer.setOnClickListener(v -> showMakeOfferDialog());
        btnViewProfile.setOnClickListener(v -> viewSellerProfile());
        fabShare.setOnClickListener(v -> shareProduct());
        fabSave.setOnClickListener(v -> saveProduct());
    }

    private void contactSeller() {
        // Open WhatsApp or Phone
        String phoneNumber = "+84901234567";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=Hi, I'm interested in your product: " + tvTitle.getText()));

        try {
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to regular phone call
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        }
    }

    private void showMakeOfferDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_make_offer, null);

        // Setup offer dialog components
        TextView tvCurrentPrice = view.findViewById(R.id.tv_current_price);
        com.google.android.material.textfield.TextInputEditText etOfferAmount = view.findViewById(R.id.et_offer_amount);
        com.google.android.material.textfield.TextInputEditText etMessage = view.findViewById(R.id.et_message);
        Button btnSubmitOffer = view.findViewById(R.id.btn_submit_offer);

        tvCurrentPrice.setText("Current Price: " + tvPrice.getText());

        btnSubmitOffer.setOnClickListener(v -> {
            String offerAmount = etOfferAmount.getText().toString().trim();
            String message = etMessage.getText().toString().trim();

            if (!offerAmount.isEmpty()) {
                submitOffer(offerAmount, message);
                dialog.dismiss();
            } else {
                etOfferAmount.setError("Enter offer amount");
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void submitOffer(String amount, String message) {
        // TODO: Call backend API to submit offer
        Toast.makeText(this, "Offer submitted: " + amount + " ₫", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Offer submitted - Amount: " + amount + ", Message: " + message);
    }

    private void viewSellerProfile() {
        Toast.makeText(this, "Viewing seller profile...", Toast.LENGTH_SHORT).show();
        // TODO: Navigate to seller profile activity
    }

    private void shareProduct() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing product on TradeUp: " + tvTitle.getText() + " - " + tvPrice.getText());
        startActivity(Intent.createChooser(shareIntent, "Share Product"));
    }

    private void saveProduct() {
        // TODO: Call backend to save product
        Toast.makeText(this, "Product saved to your wishlist! ❤️", Toast.LENGTH_SHORT).show();

        // Update FAB icon to indicate saved state
        fabSave.setImageResource(R.drawable.ic_bookmark_filled);
    }
}