// app/src/main/java/com/example/newtrade/ui/payment/PaymentActivity.java
package com.example.newtrade.ui.payment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.PaymentService;
import com.example.newtrade.models.Payment;
import com.example.newtrade.models.PaymentConfig;
import com.example.newtrade.models.PaymentIntentRequest;
import com.example.newtrade.models.PaymentIntentResponse;
import com.example.newtrade.models.PaymentMethod;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.Transaction;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.ImageUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.math.BigDecimal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    // Intent extras
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_CURRENCY = "currency";
    public static final String EXTRA_DESCRIPTION = "description";

    // UI Components
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;

    // Transaction info section
    private ImageView ivProductImage;
    private TextView tvProductTitle;
    private TextView tvAmount;
    private TextView tvDescription;

    // Payment method section
    private CardView cardPaymentMethods;
    private LinearLayout layoutPaymentMethods;
    private MaterialCardView selectedPaymentCard;
    private TextView tvSelectedPaymentMethod;
    private ImageView ivSelectedPaymentIcon;

    // Action buttons
    private MaterialButton btnSelectPaymentMethod;
    private MaterialButton btnProceedPayment;
    private MaterialButton btnCancel;

    // Data
    private Long transactionId;
    private BigDecimal amount;
    private String currency = Constants.CURRENCY_VND;
    private String description;
    private PaymentMethod selectedPaymentMethod;
    private PaymentConfig paymentConfig;
    private PaymentService paymentService;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initializeComponents();
        setupToolbar();
        getIntentData();
        setupListeners();
        loadPaymentConfig();

        Log.d(TAG, "PaymentActivity created for transaction: " + transactionId);
    }

    private void initializeComponents() {
        prefsManager = new SharedPrefsManager(this);
        paymentService = ApiClient.getPaymentService();

        // UI components
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        contentLayout = findViewById(R.id.content_layout);

        // Transaction info
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvAmount = findViewById(R.id.tv_amount);
        tvDescription = findViewById(R.id.tv_description);

        // Payment method
        cardPaymentMethods = findViewById(R.id.card_payment_methods);
        layoutPaymentMethods = findViewById(R.id.layout_payment_methods);
        selectedPaymentCard = findViewById(R.id.selected_payment_card);
        tvSelectedPaymentMethod = findViewById(R.id.tv_selected_payment_method);
        ivSelectedPaymentIcon = findViewById(R.id.iv_selected_payment_icon);

        // Buttons
        btnSelectPaymentMethod = findViewById(R.id.btn_select_payment_method);
        btnProceedPayment = findViewById(R.id.btn_proceed_payment);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment");
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, 0L);

        // Get amount as double and convert to BigDecimal
        double amountDouble = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0);
        amount = BigDecimal.valueOf(amountDouble);

        currency = intent.getStringExtra(EXTRA_CURRENCY);
        if (currency == null) currency = Constants.CURRENCY_VND;

        description = intent.getStringExtra(EXTRA_DESCRIPTION);
        if (description == null) description = "Product Purchase";

        // Update UI with transaction data
        updateTransactionInfo();
    }

    private void updateTransactionInfo() {
        tvAmount.setText(Constants.formatCurrency(amount.doubleValue(), currency));
        tvDescription.setText(description);

        // Set default product title if not provided
        tvProductTitle.setText("Item Purchase");

        Log.d(TAG, "Transaction info - ID: " + transactionId + ", Amount: " + amount + " " + currency);
    }

    private void setupListeners() {
        btnSelectPaymentMethod.setOnClickListener(v -> showPaymentMethodSelection());
        btnProceedPayment.setOnClickListener(v -> proceedWithPayment());
        btnCancel.setOnClickListener(v -> showCancelConfirmation());

        selectedPaymentCard.setOnClickListener(v -> showPaymentMethodSelection());
    }

    private void loadPaymentConfig() {
        showLoading(true);

        Call<StandardResponse<PaymentConfig>> call = paymentService.getPaymentConfig();
        call.enqueue(new Callback<StandardResponse<PaymentConfig>>() {
            @Override
            public void onResponse(Call<StandardResponse<PaymentConfig>> call, Response<StandardResponse<PaymentConfig>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<PaymentConfig> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        paymentConfig = standardResponse.getData();
                        setupPaymentMethods();
                        Log.d(TAG, "Payment config loaded successfully");
                    } else {
                        showError("Failed to load payment configuration: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Failed to load payment configuration");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<PaymentConfig>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to load payment config", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void setupPaymentMethods() {
        // Show available payment methods
        layoutPaymentMethods.removeAllViews();

        // Add payment method options
        addPaymentMethodOption(PaymentMethod.CARD, "Credit/Debit Card", R.drawable.ic_credit_card);
        addPaymentMethodOption(PaymentMethod.DIGITAL_WALLET, "Digital Wallet", R.drawable.ic_wallet);
        addPaymentMethodOption(PaymentMethod.BANK_TRANSFER, "Bank Transfer", R.drawable.ic_bank);
        addPaymentMethodOption(PaymentMethod.CASH, "Cash Payment", R.drawable.ic_cash);

        // Set default payment method
        selectPaymentMethod(PaymentMethod.CARD);
    }

    private void addPaymentMethodOption(PaymentMethod method, String name, int iconRes) {
        View methodView = getLayoutInflater().inflate(R.layout.item_payment_method, layoutPaymentMethods, false);

        ImageView icon = methodView.findViewById(R.id.iv_payment_icon);
        TextView methodName = methodView.findViewById(R.id.tv_payment_name);
        MaterialCardView card = methodView.findViewById(R.id.card_payment_method);

        icon.setImageResource(iconRes);
        methodName.setText(name);

        methodView.setOnClickListener(v -> selectPaymentMethod(method));
        methodView.setTag(method);

        layoutPaymentMethods.addView(methodView);
    }

    private void selectPaymentMethod(PaymentMethod method) {
        selectedPaymentMethod = method;

        // Update selected payment display
        tvSelectedPaymentMethod.setText(method.getDisplayName());

        // Update icon based on method
        int iconRes = getPaymentMethodIcon(method);
        ivSelectedPaymentIcon.setImageResource(iconRes);

        // Update UI state
        selectedPaymentCard.setVisibility(View.VISIBLE);
        btnProceedPayment.setEnabled(true);

        // Update visual selection in payment methods list
        updatePaymentMethodSelection(method);

        Log.d(TAG, "Selected payment method: " + method);
    }

    private int getPaymentMethodIcon(PaymentMethod method) {
        switch (method) {
            case CARD:
                return R.drawable.ic_credit_card;
            case DIGITAL_WALLET:
                return R.drawable.ic_wallet;
            case BANK_TRANSFER:
                return R.drawable.ic_bank;
            case CASH:
                return R.drawable.ic_cash;
            default:
                return R.drawable.ic_credit_card;
        }
    }

    private void updatePaymentMethodSelection(PaymentMethod selectedMethod) {
        for (int i = 0; i < layoutPaymentMethods.getChildCount(); i++) {
            View child = layoutPaymentMethods.getChildAt(i);
            MaterialCardView card = child.findViewById(R.id.card_payment_method);
            PaymentMethod method = (PaymentMethod) child.getTag();

            if (method == selectedMethod) {
                card.setCardBackgroundColor(getResources().getColor(R.color.primary_light, null));
                card.setStrokeColor(getResources().getColor(R.color.primary, null));
                card.setStrokeWidth(4);
            } else {
                card.setCardBackgroundColor(getResources().getColor(R.color.surface, null));
                card.setStrokeColor(getResources().getColor(R.color.outline, null));
                card.setStrokeWidth(2);
            }
        }
    }

    private void showPaymentMethodSelection() {
        Intent intent = new Intent(this, PaymentMethodSelectionActivity.class);
        intent.putExtra("current_method", selectedPaymentMethod);
        startActivityForResult(intent, Constants.RC_PAYMENT);
    }

    private void proceedWithPayment() {
        if (selectedPaymentMethod == null) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        if (transactionId == null || transactionId <= 0) {
            Toast.makeText(this, "Invalid transaction", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        showPaymentConfirmationDialog();
    }

    private void showPaymentConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Payment");
        builder.setMessage(String.format("Are you sure you want to proceed with payment of %s using %s?",
                Constants.formatCurrency(amount.doubleValue(), currency),
                selectedPaymentMethod.getDisplayName()));

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            if (selectedPaymentMethod == PaymentMethod.CASH) {
                processCashPayment();
            } else {
                createPaymentIntent();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void processCashPayment() {
        // For cash payments, we just mark as pending and let the backend handle it
        Toast.makeText(this, "Cash payment selected. Transaction will be completed upon delivery.", Toast.LENGTH_LONG).show();

        // Return success result
        Intent resultIntent = new Intent();
        resultIntent.putExtra("payment_method", selectedPaymentMethod);
        resultIntent.putExtra("transaction_id", transactionId);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void createPaymentIntent() {
        showLoading(true);

        PaymentIntentRequest request = new PaymentIntentRequest(transactionId, description);
        String userId = String.valueOf(prefsManager.getUserId());

        Call<StandardResponse<PaymentIntentResponse>> call = paymentService.createPaymentIntent(userId, request);
        call.enqueue(new Callback<StandardResponse<PaymentIntentResponse>>() {
            @Override
            public void onResponse(Call<StandardResponse<PaymentIntentResponse>> call, Response<StandardResponse<PaymentIntentResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<PaymentIntentResponse> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        PaymentIntentResponse paymentIntent = standardResponse.getData();
                        handlePaymentIntent(paymentIntent);
                    } else {
                        showError("Failed to create payment: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Failed to create payment intent");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<PaymentIntentResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to create payment intent", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void handlePaymentIntent(PaymentIntentResponse paymentIntent) {
        Log.d(TAG, "Payment intent created: " + paymentIntent.getPaymentIntentId());

        if (selectedPaymentMethod == PaymentMethod.CARD) {
            // Launch Stripe payment sheet or card input
            launchStripePayment(paymentIntent);
        } else {
            // Handle other payment methods
            handleOtherPaymentMethods(paymentIntent);
        }
    }

    private void launchStripePayment(PaymentIntentResponse paymentIntent) {
        // For now, simulate payment success
        // In real implementation, integrate with Stripe SDK
        simulatePaymentSuccess(paymentIntent.getPaymentIntentId());
    }

    private void handleOtherPaymentMethods(PaymentIntentResponse paymentIntent) {
        // Handle bank transfer, digital wallet, etc.
        Toast.makeText(this, "Payment method not yet implemented", Toast.LENGTH_SHORT).show();
    }

    private void simulatePaymentSuccess(String paymentIntentId) {
        // Simulate payment processing
        new android.os.Handler().postDelayed(() -> {
            confirmPaymentSuccess(paymentIntentId);
        }, 2000);

        Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();
    }

    private void confirmPaymentSuccess(String paymentIntentId) {
        showLoading(true);

        Call<StandardResponse<Payment>> call = paymentService.confirmPayment(paymentIntentId);
        call.enqueue(new Callback<StandardResponse<Payment>>() {
            @Override
            public void onResponse(Call<StandardResponse<Payment>> call, Response<StandardResponse<Payment>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Payment> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        Payment payment = standardResponse.getData();
                        handlePaymentSuccess(payment);
                    } else {
                        showError("Payment confirmation failed: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Payment confirmation failed");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Payment>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to confirm payment", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void handlePaymentSuccess(Payment payment) {
        Log.d(TAG, "Payment successful: " + payment.getId());

        // Show success message
        Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show();

        // Return success result
        Intent resultIntent = new Intent();
        resultIntent.putExtra("payment_id", payment.getId());
        resultIntent.putExtra("payment_method", selectedPaymentMethod);
        resultIntent.putExtra("transaction_id", transactionId);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showCancelConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Payment");
        builder.setMessage("Are you sure you want to cancel this payment?");

        builder.setPositiveButton("Yes, Cancel", (dialog, which) -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        builder.setNegativeButton("Continue Payment", null);
        builder.show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_PAYMENT && resultCode == RESULT_OK && data != null) {
            PaymentMethod newMethod = (PaymentMethod) data.getSerializableExtra("selected_method");
            if (newMethod != null) {
                selectPaymentMethod(newMethod);
            }
        }
    }
}