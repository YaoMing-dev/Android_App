// app/src/main/java/com/example/newtrade/ui/offer/MakeOfferActivity.java
package com.example.newtrade.ui.offer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakeOfferActivity extends AppCompatActivity {

    private static final String TAG = "MakeOfferActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivProductImage;
    private TextView tvProductTitle, tvProductPrice, tvOfferPercentage;
    private TextInputLayout tilOfferAmount, tilOfferMessage;
    private EditText etOfferAmount, etOfferMessage;
    private Button btnSubmitOffer, btnCancel;

    // Data
    private Long productId;
    private String productTitle;
    private double originalPrice;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_offer);

        // Initialize
        prefsManager = SharedPrefsManager.getInstance(this);

        // Get product data from intent
        getIntentData();

        // Initialize views
        initViews();
        setupToolbar();
        setupListeners();

        // Display product info
        displayProductInfo();

        Log.d(TAG, "✅ MakeOfferActivity created for product ID: " + productId);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        productId = intent.getLongExtra(Constants.EXTRA_PRODUCT_ID, -1L);
        productTitle = intent.getStringExtra(Constants.EXTRA_PRODUCT_TITLE);
        originalPrice = intent.getDoubleExtra(Constants.EXTRA_PRODUCT_PRICE, 0.0);

        if (productId == -1L) {
            Log.e(TAG, "❌ Product ID not provided in intent");
            finish();
            return;
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvOfferPercentage = findViewById(R.id.tv_offer_percentage);
        tilOfferAmount = findViewById(R.id.til_offer_amount);
        tilOfferMessage = findViewById(R.id.til_offer_message);
        etOfferAmount = findViewById(R.id.et_offer_amount);
        etOfferMessage = findViewById(R.id.et_offer_message);
        btnSubmitOffer = findViewById(R.id.btn_submit_offer);
        btnCancel = findViewById(R.id.btn_cancel);

        // Initially disable submit button
        btnSubmitOffer.setEnabled(false);

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Make Offer");
        }
    }

    private void setupListeners() {
        // Offer amount text change listener
        etOfferAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateOfferPercentage();
                validateOfferAmount();
                updateSubmitButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Submit offer button
        btnSubmitOffer.setOnClickListener(v -> submitOffer());

        // Cancel button
        btnCancel.setOnClickListener(v -> finish());
    }

    private void displayProductInfo() {
        if (productTitle != null) {
            tvProductTitle.setText(productTitle);
        }

        if (originalPrice > 0) {
            tvProductPrice.setText(Constants.formatPrice(originalPrice));
        }

        // Load product image if available
        // In a real app, you would pass the image URL through intent
        // For now, we'll use a placeholder
        Glide.with(this)
                .load(R.drawable.ic_placeholder_image)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(ivProductImage);
    }

    private void updateOfferPercentage() {
        String offerAmountStr = etOfferAmount.getText().toString().trim();

        if (offerAmountStr.isEmpty() || originalPrice <= 0) {
            tvOfferPercentage.setText("");
            return;
        }

        try {
            double offerAmount = Double.parseDouble(offerAmountStr);
            double percentage = (offerAmount / originalPrice) * 100;
            tvOfferPercentage.setText(String.format("%.1f%% of original price", percentage));
        } catch (NumberFormatException e) {
            tvOfferPercentage.setText("");
        }
    }

    private void validateOfferAmount() {
        String offerAmountStr = etOfferAmount.getText().toString().trim();

        if (offerAmountStr.isEmpty()) {
            tilOfferAmount.setError(null);
            return;
        }

        try {
            double offerAmount = Double.parseDouble(offerAmountStr);

            if (offerAmount <= 0) {
                tilOfferAmount.setError("Offer amount must be greater than 0");
                return;
            }

            if (offerAmount >= originalPrice) {
                tilOfferAmount.setError("Offer amount should be less than original price");
                return;
            }

            if (!ValidationUtils.isValidOffer(offerAmount, originalPrice)) {
                tilOfferAmount.setError("Offer must be between 10% and 90% of original price");
                return;
            }

            tilOfferAmount.setError(null);
        } catch (NumberFormatException e) {
            tilOfferAmount.setError("Please enter a valid amount");
        }
    }

    private void updateSubmitButtonState() {
        String offerAmountStr = etOfferAmount.getText().toString().trim();
        boolean isValid = !offerAmountStr.isEmpty() && tilOfferAmount.getError() == null;
        btnSubmitOffer.setEnabled(isValid);
    }

    private void submitOffer() {
        String offerAmountStr = etOfferAmount.getText().toString().trim();
        String offerMessage = etOfferMessage.getText().toString().trim();

        if (!validateOfferForm(offerAmountStr, offerMessage)) {
            return;
        }

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        double offerAmount = Double.parseDouble(offerAmountStr);

        Log.d(TAG, "🔄 Submitting offer for product: " + productId);

        // Disable button during submission
        btnSubmitOffer.setEnabled(false);
        btnSubmitOffer.setText("Submitting...");

        Map<String, Object> offerRequest = new HashMap<>();
        offerRequest.put("productId", productId);
        offerRequest.put("offerAmount", offerAmount);
        if (!offerMessage.isEmpty()) {
            offerRequest.put("message", offerMessage);
        }

        ApiClient.getOfferService().createOffer(userId, offerRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(MakeOfferActivity.this,
                                        "Offer submitted successfully!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "✅ Offer submitted successfully");

                                // Return to previous screen
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                showError(apiResponse.getMessage());
                                resetSubmitButton();
                            }
                        } else {
                            showError("Failed to submit offer. Please try again.");
                            resetSubmitButton();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to submit offer", t);
                        showError("Network error. Please try again.");
                        resetSubmitButton();
                    }
                });
    }

    private boolean validateOfferForm(String offerAmountStr, String offerMessage) {
        boolean isValid = true;

        // Validate offer amount
        if (offerAmountStr.isEmpty()) {
            tilOfferAmount.setError("Offer amount is required");
            isValid = false;
        } else {
            try {
                double offerAmount = Double.parseDouble(offerAmountStr);
                if (!ValidationUtils.isValidOffer(offerAmount, originalPrice)) {
                    tilOfferAmount.setError("Offer must be between 10% and 90% of original price");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilOfferAmount.setError("Please enter a valid amount");
                isValid = false;
            }
        }

        // Validate offer message (optional but if provided, should be reasonable length)
        if (!offerMessage.isEmpty() && offerMessage.length() < 5) {
            tilOfferMessage.setError("Message should be at least 5 characters if provided");
            isValid = false;
        } else {
            tilOfferMessage.setError(null);
        }

        return isValid;
    }

    private void resetSubmitButton() {
        btnSubmitOffer.setEnabled(true);
        btnSubmitOffer.setText("Submit Offer");
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
        Log.d(TAG, "MakeOfferActivity destroyed");
    }
}