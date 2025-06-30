// app/src/main/java/com/example/newtrade/ui/offer/MakeOfferActivity.java
package com.example.newtrade.ui.offer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakeOfferActivity extends AppCompatActivity {
    private static final String TAG = "MakeOfferActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView tvProductTitle, tvOriginalPrice, tvOfferSummary;
    private TextInputLayout tilOfferAmount, tilMessage;
    private EditText etOfferAmount, etMessage;
    private MaterialButton btnMakeOffer;

    // Data
    private Long productId;
    private String productTitle;
    private String originalPrice;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_offer);

        prefsManager = new SharedPrefsManager(this);

        // Get data from intent
        productId = getIntent().getLongExtra(Constants.BUNDLE_PRODUCT_ID, -1);
        productTitle = getIntent().getStringExtra(Constants.BUNDLE_PRODUCT_TITLE);
        originalPrice = getIntent().getStringExtra(Constants.BUNDLE_PRODUCT_PRICE);

        if (productId == -1) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        tvOfferSummary = findViewById(R.id.tv_offer_summary);
        tilOfferAmount = findViewById(R.id.til_offer_amount);
        tilMessage = findViewById(R.id.til_message);
        etOfferAmount = findViewById(R.id.et_offer_amount);
        etMessage = findViewById(R.id.et_message);
        btnMakeOffer = findViewById(R.id.btn_make_offer);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Make an Offer");
        }
    }

    private void setupListeners() {
        // Offer amount validation
        etOfferAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateOfferAmount();
                updateOfferSummary();
            }
        });

        // Make offer button
        btnMakeOffer.setOnClickListener(v -> makeOffer());
    }

    private void updateUI() {
        if (productTitle != null) {
            tvProductTitle.setText(productTitle);
        }

        if (originalPrice != null) {
            tvOriginalPrice.setText("Original Price: " + Constants.CURRENCY_SYMBOL + originalPrice);
        }

        // Set placeholder message
        etMessage.setHint("Hi, I'm interested in your " + (productTitle != null ? productTitle : "product") +
                ". Would you accept my offer?");
    }

    private void validateOfferAmount() {
        String amountStr = etOfferAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            tilOfferAmount.setError(null);
            btnMakeOffer.setEnabled(false);
            return;
        }

        try {
            BigDecimal offerAmount = new BigDecimal(amountStr);

            if (offerAmount.compareTo(BigDecimal.ZERO) <= 0) {
                tilOfferAmount.setError("Amount must be greater than 0");
                btnMakeOffer.setEnabled(false);
                return;
            }

            // Check if offer is reasonable (not more than 150% of original price)
            if (originalPrice != null) {
                try {
                    BigDecimal original = new BigDecimal(originalPrice);
                    BigDecimal maxOffer = original.multiply(new BigDecimal("1.5"));

                    if (offerAmount.compareTo(maxOffer) > 0) {
                        tilOfferAmount.setError("Offer seems too high");
                        btnMakeOffer.setEnabled(true); // Still allow it
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Ignore if can't parse original price
                }
            }

            tilOfferAmount.setError(null);
            btnMakeOffer.setEnabled(true);

        } catch (NumberFormatException e) {
            tilOfferAmount.setError("Please enter a valid amount");
            btnMakeOffer.setEnabled(false);
        }
    }

    private void updateOfferSummary() {
        String amountStr = etOfferAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            tvOfferSummary.setVisibility(View.GONE);
            return;
        }

        try {
            BigDecimal offerAmount = new BigDecimal(amountStr);
            String summary = "Your offer: " + Constants.CURRENCY_SYMBOL + offerAmount.toPlainString();

            // Show percentage difference if we have original price
            if (originalPrice != null) {
                try {
                    BigDecimal original = new BigDecimal(originalPrice);
                    BigDecimal difference = offerAmount.subtract(original);
                    BigDecimal percentage = difference.divide(original, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal("100"));

                    if (percentage.compareTo(BigDecimal.ZERO) > 0) {
                        summary += " (+" + percentage.setScale(1, BigDecimal.ROUND_HALF_UP) + "% above asking price)";
                    } else if (percentage.compareTo(BigDecimal.ZERO) < 0) {
                        summary += " (" + percentage.setScale(1, BigDecimal.ROUND_HALF_UP) + "% below asking price)";
                    } else {
                        summary += " (matches asking price)";
                    }
                } catch (NumberFormatException e) {
                    // Ignore if can't parse original price
                }
            }

            tvOfferSummary.setText(summary);
            tvOfferSummary.setVisibility(View.VISIBLE);

        } catch (NumberFormatException e) {
            tvOfferSummary.setVisibility(View.GONE);
        }
    }

    private void makeOffer() {
        String amountStr = etOfferAmount.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (amountStr.isEmpty()) {
            tilOfferAmount.setError("Please enter an offer amount");
            return;
        }

        try {
            BigDecimal offerAmount = new BigDecimal(amountStr);

            if (offerAmount.compareTo(BigDecimal.ZERO) <= 0) {
                tilOfferAmount.setError("Amount must be greater than 0");
                return;
            }

            // Create offer
            submitOffer(offerAmount, message);

        } catch (NumberFormatException e) {
            tilOfferAmount.setError("Please enter a valid amount");
        }
    }

    private void submitOffer(BigDecimal amount, String message) {
        btnMakeOffer.setEnabled(false);

        Map<String, Object> offerData = new HashMap<>();
        offerData.put("productId", productId);
        offerData.put("offerAmount", amount);
        offerData.put("message", message.isEmpty() ? null : message);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getOfferService()
                .createOffer(offerData, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                btnMakeOffer.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MakeOfferActivity.this, "Offer sent successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Failed to send offer";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(MakeOfferActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                btnMakeOffer.setEnabled(true);
                Log.e(TAG, "Failed to send offer", t);
                Toast.makeText(MakeOfferActivity.this, "Failed to send offer. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
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