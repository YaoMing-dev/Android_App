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
import com.example.newtrade.utils.Constants;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);

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
        Call<StandardResponse<Map<String, Object>>> call =
                ApiClient.getOfferService().getOfferById(offerId);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Handle offer details
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Failed to load offer details", t);
            }
        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}