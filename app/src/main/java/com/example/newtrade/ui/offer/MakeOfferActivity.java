// app/src/main/java/com/example/newtrade/ui/offer/MakeOfferActivity.java
package com.example.newtrade.ui.offer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.NetworkUtils;
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
    private ProgressBar progressBar;

    // Data
    private Long productId;
    private String productTitle;
    private String originalPrice;
    private SharedPrefsManager prefsManager;

    // State
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_offer);

        prefsManager = new SharedPrefsManager(this);

        getIntentData();
        initViews();
        setupToolbar();
        setupListeners();
        updateUI();
    }

    private void getIntentData() {
        productId = getIntent().getLongExtra(Constants.BUNDLE_PRODUCT_ID, -1);
        productTitle = getIntent().getStringExtra(Constants.BUNDLE_PRODUCT_TITLE);
        originalPrice = getIntent().getStringExtra(Constants.BUNDLE_PRODUCT_PRICE);

        if (productId == -1 || productTitle == null) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show();
            finish();
        }
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
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Make Offer");
        }
    }

    private void setupListeners() {
        etOfferAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilOfferAmount.setError(null);
                updateOfferSummary();
                updateMakeOfferButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilMessage.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnMakeOffer.setOnClickListener(v -> makeOffer());
    }

    private void updateUI() {
        tvProductTitle.setText(productTitle);
        if (originalPrice != null) {
            tvOriginalPrice.setText("Listed at: " + originalPrice);
        }
    }

    private void updateOfferSummary() {
        String offerAmountStr = etOfferAmount.getText().toString().trim();

        if (offerAmountStr.isEmpty()) {
            tvOfferSummary.setVisibility(View.GONE);
            return;
        }

        try {
            BigDecimal offerAmount = new BigDecimal(offerAmountStr);
            String summary = "Your offer: $" + offerAmount.toString();

            // Calculate difference if original price is available
            if (originalPrice != null) {
                try {
                    String originalPriceNum = originalPrice.replace("$", "").trim();
                    BigDecimal original = new BigDecimal(originalPriceNum);
                    BigDecimal difference = original.subtract(offerAmount);
                    BigDecimal percentDiff = difference.divide(original, 2, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal("100"));

                    if (difference.compareTo(BigDecimal.ZERO) > 0) {
                        summary += "\n$" + difference + " less than asking price (" + percentDiff + "% off)";
                    } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
                        summary += "\n$" + difference.abs() + " more than asking price";
                    } else {
                        summary += "\nSame as asking price";
                    }
                } catch (Exception e) {
                    // Ignore parsing errors for original price
                }
            }

            tvOfferSummary.setText(summary);
            tvOfferSummary.setVisibility(View.VISIBLE);

        } catch (NumberFormatException e) {
            tvOfferSummary.setVisibility(View.GONE);
        }
    }

    private void updateMakeOfferButtonState() {
        String offerAmount = etOfferAmount.getText().toString().trim();
        btnMakeOffer.setEnabled(ValidationUtils.isValidPrice(offerAmount) && !isLoading);
    }

    // FR-5.1.1: Buyers can make offers if price is negotiable
    private void makeOffer() {
        if (!validateForm()) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection");
            return;
        }

        setLoading(true);

        String offerAmountStr = etOfferAmount.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        Map<String, Object> offerData = new HashMap<>();
        offerData.put("productId", productId);
        offerData.put("amount", new BigDecimal(offerAmountStr));
        if (!message.isEmpty()) {
            offerData.put("message", message);
        }

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getOfferService()
                .makeOffer(offerData, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);
                handleOfferResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Failed to make offer", t);
                showError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }

    private void handleOfferResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Toast.makeText(this, "Offer sent successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Failed to send offer";
                handleOfferError(message, response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing offer response", e);
            showError("Failed to send offer");
        }
    }

    private void handleOfferError(String message, int responseCode) {
        if (responseCode == 409 || message.toLowerCase().contains("already")) {
            showError("You have already made an offer for this product");
        } else if (responseCode == 400 || message.toLowerCase().contains("amount")) {
            tilOfferAmount.setError("Invalid offer amount");
        } else if (message.toLowerCase().contains("sold")) {
            showError("This product is no longer available");
        } else {
            showError(message);
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        String offerAmountStr = etOfferAmount.getText().toString().trim();
        if (!ValidationUtils.isValidPrice(offerAmountStr)) {
            tilOfferAmount.setError("Please enter a valid offer amount");
            isValid = false;
        } else {
            try {
                BigDecimal offerAmount = new BigDecimal(offerAmountStr);
                if (offerAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    tilOfferAmount.setError("Offer amount must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilOfferAmount.setError("Please enter a valid number");
                isValid = false;
            }
        }

        String message = etMessage.getText().toString().trim();
        if (message.length() > 500) {
            tilMessage.setError("Message must be less than 500 characters");
            isValid = false;
        }

        return isValid;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnMakeOffer.setEnabled(!loading);
        btnMakeOffer.setText(loading ? "Sending Offer..." : "Make Offer");
        updateMakeOfferButtonState();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}