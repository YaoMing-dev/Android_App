// app/src/main/java/com/example/newtrade/ui/offer/OfferDetailActivity.java
package com.example.newtrade.ui.offer;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfferDetailActivity extends AppCompatActivity {

    private static final String TAG = "OfferDetailActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvOfferAmount, tvOfferStatus, tvProductTitle, tvOfferMessage;
    private Button btnAcceptOffer, btnRejectOffer, btnCounterOffer;

    // Data
    private Long offerId;
    private SharedPrefsManager prefsManager; // ✅ THÊM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);

        // ✅ KHỞI TẠO SharedPrefsManager
        prefsManager = SharedPrefsManager.getInstance(this);

        offerId = getIntent().getLongExtra("offer_id", -1L);
        if (offerId == -1L) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupListeners();
        loadOfferDetails();

        Log.d(TAG, "OfferDetailActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvOfferAmount = findViewById(R.id.tv_offer_amount);
        tvOfferStatus = findViewById(R.id.tv_offer_status);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvOfferMessage = findViewById(R.id.tv_offer_message);
        btnAcceptOffer = findViewById(R.id.btn_accept_offer);
        btnRejectOffer = findViewById(R.id.btn_reject_offer);
        btnCounterOffer = findViewById(R.id.btn_counter_offer);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Offer Details");
        }
    }

    private void setupListeners() {
        btnAcceptOffer.setOnClickListener(v -> acceptOffer());
        btnRejectOffer.setOnClickListener(v -> rejectOffer());
        btnCounterOffer.setOnClickListener(v -> counterOffer());
    }

    private void loadOfferDetails() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ SỬA METHOD CALL - thêm userId parameter
        Call<StandardResponse<Map<String, Object>>> call =
                ApiClient.getOfferService().getOfferById(userId, offerId);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        displayOfferDetails(apiResponse.getData());
                    } else {
                        Log.e(TAG, "Offer API Error: " + apiResponse.getMessage());
                        showError("Failed to load offer details");
                    }
                } else {
                    Log.e(TAG, "Offer response unsuccessful: " + response.code());
                    showError("Failed to load offer details");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Failed to load offer details", t);
                showError("Network error. Please try again.");
            }
        });
    }

    private void displayOfferDetails(Map<String, Object> offerData) {
        try {
            // Display offer amount
            Object amountObj = offerData.get("offerAmount");
            if (amountObj != null && tvOfferAmount != null) {
                tvOfferAmount.setText("$" + amountObj.toString());
            }

            // Display offer status
            String status = (String) offerData.get("status");
            if (status != null && tvOfferStatus != null) {
                tvOfferStatus.setText(status);
            }

            // Display offer message
            String message = (String) offerData.get("message");
            if (message != null && tvOfferMessage != null) {
                tvOfferMessage.setText(message);
            }

            // Display product title
            Map<String, Object> productData = (Map<String, Object>) offerData.get("product");
            if (productData != null) {
                String productTitle = (String) productData.get("title");
                if (productTitle != null && tvProductTitle != null) {
                    tvProductTitle.setText(productTitle);
                }
            }

            Log.d(TAG, "✅ Offer details displayed");
        } catch (Exception e) {
            Log.e(TAG, "Error displaying offer details", e);
        }
    }

    private void acceptOffer() {
        Toast.makeText(this, "Offer accepted", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void rejectOffer() {
        Toast.makeText(this, "Offer rejected", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void counterOffer() {
        Toast.makeText(this, "Counter offer feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}