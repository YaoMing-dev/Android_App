// app/src/main/java/com/example/newtrade/ui/transaction/TransactionActivity.java
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

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionActivity extends AppCompatActivity {

    private static final String TAG = "TransactionActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivProductImage;
    private TextView tvProductTitle, tvProductPrice, tvTransactionId;
    private TextView tvBuyerName, tvSellerName, tvTransactionStatus;
    private TextView tvPaymentMethod, tvDeliveryMethod, tvTransactionDate;
    private Button btnCompleteTransaction, btnCancelTransaction;
    private LinearLayout llBuyerSection, llSellerSection;

    // Data
    private Long transactionId;
    private SharedPrefsManager prefsManager;
    private Map<String, Object> transactionData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

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

        Log.d(TAG, "✅ TransactionActivity created for transaction ID: " + transactionId);
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
        tvTransactionId = findViewById(R.id.tv_transaction_id);
        tvBuyerName = findViewById(R.id.tv_buyer_name);
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvTransactionStatus = findViewById(R.id.tv_transaction_status);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvDeliveryMethod = findViewById(R.id.tv_delivery_method);
        tvTransactionDate = findViewById(R.id.tv_transaction_date);
        btnCompleteTransaction = findViewById(R.id.btn_complete_transaction);
        btnCancelTransaction = findViewById(R.id.btn_cancel_transaction);
        llBuyerSection = findViewById(R.id.ll_buyer_section);
        llSellerSection = findViewById(R.id.ll_seller_section);

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Transaction Details");
        }
    }

    private void setupListeners() {
        btnCompleteTransaction.setOnClickListener(v -> completeTransaction());
        btnCancelTransaction.setOnClickListener(v -> cancelTransaction());
    }

    private void loadTransactionDetails() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "🔍 Loading transaction details for ID: " + transactionId);

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
            // Transaction info
            tvTransactionId.setText("Transaction #" + transactionData.get("id"));
            tvTransactionStatus.setText((String) transactionData.get("status"));
            tvPaymentMethod.setText((String) transactionData.get("paymentMethod"));
            tvDeliveryMethod.setText((String) transactionData.get("deliveryMethod"));
            tvTransactionDate.setText((String) transactionData.get("createdAt"));

            // Product info
            Map<String, Object> productData = (Map<String, Object>) transactionData.get("product");
            if (productData != null) {
                tvProductTitle.setText((String) productData.get("title"));
                tvProductPrice.setText(Constants.formatPrice(((Number) productData.get("price")).doubleValue()));

                String imageUrl = (String) productData.get("imageUrl");
                if (imageUrl != null) {
                    Glide.with(this)
                            .load(Constants.getImageUrl(imageUrl))
                            .placeholder(R.drawable.ic_placeholder_image)
                            .error(R.drawable.ic_placeholder_image)
                            .into(ivProductImage);
                }
            }

            // Buyer info
            Map<String, Object> buyerData = (Map<String, Object>) transactionData.get("buyer");
            if (buyerData != null) {
                tvBuyerName.setText((String) buyerData.get("displayName"));
            }

            // Seller info
            Map<String, Object> sellerData = (Map<String, Object>) transactionData.get("seller");
            if (sellerData != null) {
                tvSellerName.setText((String) sellerData.get("displayName"));
            }

            // Show/hide action buttons based on status and user role
            updateActionButtons();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error displaying transaction details", e);
            showError("Error displaying transaction details");
        }
    }

    private void updateActionButtons() {
        String status = (String) transactionData.get("status");
        Long currentUserId = prefsManager.getUserId();

        // Determine user role
        @SuppressWarnings("unchecked")
        Map<String, Object> buyerData = (Map<String, Object>) transactionData.get("buyer");
        @SuppressWarnings("unchecked")
        Map<String, Object> sellerData = (Map<String, Object>) transactionData.get("seller");

        boolean isBuyer = buyerData != null && currentUserId != null &&
                currentUserId.equals(((Number) buyerData.get("id")).longValue());
        boolean isSeller = sellerData != null && currentUserId != null &&
                currentUserId.equals(((Number) sellerData.get("id")).longValue());

        // Show/hide buttons based on status and role
        if ("PENDING".equals(status)) {
            btnCompleteTransaction.setVisibility(isBuyer ? View.VISIBLE : View.GONE);
            btnCancelTransaction.setVisibility(View.VISIBLE);
        } else {
            btnCompleteTransaction.setVisibility(View.GONE);
            btnCancelTransaction.setVisibility(View.GONE);
        }
    }

    private void completeTransaction() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        Log.d(TAG, "🔄 Completing transaction: " + transactionId);

        ApiClient.getTransactionService().completeTransaction(userId, transactionId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(TransactionActivity.this,
                                        "Transaction completed successfully", Toast.LENGTH_SHORT).show();
                                loadTransactionDetails(); // Refresh data
                                Log.d(TAG, "✅ Transaction completed successfully");
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to complete transaction");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to complete transaction", t);
                        showError("Network error. Please try again.");
                    }
                });
    }

    private void cancelTransaction() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        Log.d(TAG, "🔄 Cancelling transaction: " + transactionId);

        ApiClient.getTransactionService().cancelTransaction(userId, transactionId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(TransactionActivity.this,
                                        "Transaction cancelled successfully", Toast.LENGTH_SHORT).show();
                                loadTransactionDetails(); // Refresh data
                                Log.d(TAG, "✅ Transaction cancelled successfully");
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to cancel transaction");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to cancel transaction", t);
                        showError("Network error. Please try again.");
                    }
                });
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
        Log.d(TAG, "TransactionActivity destroyed");
    }
}