// app/src/main/java/com/example/newtrade/ui/transaction/PaymentActivity.java
package com.example.newtrade.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivProductImage;
    private TextView tvProductTitle, tvProductPrice, tvTotalAmount;
    private TextView tvPaymentMethod, tvTransactionFee, tvNetAmount;
    private Button btnProceedPayment, btnCancelPayment;
    private LinearLayout llPaymentDetails;

    // Data
    private Long transactionId;
    private SharedPrefsManager prefsManager;
    private Map<String, Object> transactionData;
    private String paymentIntentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize
        prefsManager = SharedPrefsManager.getInstance(this);

        // Get transaction data from intent
        getIntentData();

        // Initialize views
        initViews();
        setupToolbar();
        setupListeners();

        // Load transaction details
        loadTransactionDetails();

        Log.d(TAG, "✅ PaymentActivity created for transaction ID: " + transactionId);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        transactionId = intent.getLongExtra(Constants.EXTRA_TRANSACTION_ID, -1L);

        if (transactionId == -1L) {
            Log.e(TAG, "❌ Transaction ID not provided in intent");
            finish();
            return;
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvTransactionFee = findViewById(R.id.tv_transaction_fee);
        tvNetAmount = findViewById(R.id.tv_net_amount);
        btnProceedPayment = findViewById(R.id.btn_proceed_payment);
        btnCancelPayment = findViewById(R.id.btn_cancel_payment);
        llPaymentDetails = findViewById(R.id.ll_payment_details);

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment");
        }
    }

    private void setupListeners() {
        btnProceedPayment.setOnClickListener(v -> proceedWithPayment());
        btnCancelPayment.setOnClickListener(v -> cancelPayment());
    }

    private void loadTransactionDetails() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "🔍 Loading transaction details for payment");

        ApiClient.getTransactionService().getTransaction(userId, transactionId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                transactionData = apiResponse.getData();
                                displayTransactionDetails();
                                createPaymentIntent();
                                Log.d(TAG, "✅ Transaction details loaded successfully");
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to load transaction details");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load transaction details", t);
                        showError("Network error. Please try again.");
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void displayTransactionDetails() {
        if (transactionData == null) return;

        try {
            // Product info
            Map<String, Object> productData = (Map<String, Object>) transactionData.get("product");
            if (productData != null) {
                tvProductTitle.setText((String) productData.get("title"));

                double price = ((Number) productData.get("price")).doubleValue();
                tvProductPrice.setText(Constants.formatPrice(price));

                // Calculate fees (3% transaction fee)
                double transactionFee = price * 0.03;
                double totalAmount = price + transactionFee;

                tvTransactionFee.setText(Constants.formatPrice(transactionFee));
                tvTotalAmount.setText(Constants.formatPrice(totalAmount));
                tvNetAmount.setText(Constants.formatPrice(price));

                String imageUrl = (String) productData.get("imageUrl");
                if (imageUrl != null) {
                    Glide.with(this)
                            .load(Constants.getImageUrl(imageUrl))
                            .placeholder(R.drawable.ic_placeholder_image)
                            .error(R.drawable.ic_placeholder_image)
                            .into(ivProductImage);
                }
            }

            // Payment method
            String paymentMethod = (String) transactionData.get("paymentMethod");
            tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "Credit Card");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error displaying transaction details", e);
            showError("Error displaying transaction details");
        }
    }

    private void createPaymentIntent() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        Log.d(TAG, "🔄 Creating payment intent for transaction: " + transactionId);

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("transactionId", transactionId);
        paymentRequest.put("description", "Payment for " + tvProductTitle.getText().toString());

        ApiClient.getPaymentService().createPaymentIntent(userId, paymentRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Map<String, Object> paymentData = apiResponse.getData();
                                paymentIntentId = (String) paymentData.get("paymentIntentId");
                                Log.d(TAG, "✅ Payment intent created: " + paymentIntentId);

                                // Enable payment button
                                btnProceedPayment.setEnabled(true);
                                btnProceedPayment.setText("Pay Now");
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to create payment intent");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to create payment intent", t);
                        showError("Network error. Please try again.");
                    }
                });
    }

    private void proceedWithPayment() {
        if (paymentIntentId == null) {
            showError("Payment not ready. Please try again.");
            return;
        }

        Log.d(TAG, "🔄 Processing payment for intent: " + paymentIntentId);

        btnProceedPayment.setEnabled(false);
        btnProceedPayment.setText("Processing...");

        // In a real app, you would integrate with Stripe SDK here
        // For now, we'll simulate payment confirmation
        confirmPayment();
    }

    private void confirmPayment() {
        ApiClient.getPaymentService().confirmPayment(paymentIntentId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(PaymentActivity.this,
                                        "Payment successful!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "✅ Payment confirmed successfully");

                                // Navigate to transaction details
                                Intent intent = new Intent(PaymentActivity.this, TransactionActivity.class);
                                intent.putExtra(Constants.EXTRA_TRANSACTION_ID, transactionId);
                                startActivity(intent);
                                finish();
                            } else {
                                showError(apiResponse.getMessage());
                                resetPaymentButton();
                            }
                        } else {
                            showError("Payment failed. Please try again.");
                            resetPaymentButton();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Payment confirmation failed", t);
                        showError("Network error. Please try again.");
                        resetPaymentButton();
                    }
                });
    }

    private void resetPaymentButton() {
        btnProceedPayment.setEnabled(true);
        btnProceedPayment.setText("Pay Now");
    }

    private void cancelPayment() {
        finish();
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
        Log.d(TAG, "PaymentActivity destroyed");
    }
}