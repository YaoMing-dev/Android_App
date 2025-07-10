package com.example.newtrade.ui.payment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.PaymentService;
import com.example.newtrade.models.PaymentConfig;
import com.example.newtrade.models.PaymentMethod;
import com.example.newtrade.models.PaymentIntentRequest;
import com.example.newtrade.models.PaymentIntentResponse;
import com.example.newtrade.models.Payment;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

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
    private androidx.core.widget.NestedScrollView contentLayout;

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

    // Card input fields
    private CardView cardStripeInput;
    private TextView tvCardInstructions;
    private TextInputEditText etCardNumber;
    private TextInputEditText etExpiryDate;
    private TextInputEditText etCvc;
    private MaterialButton btnFillVisa;
    private MaterialButton btnFillMastercard;
    private MaterialButton btnFillDeclined;

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

    // ✅ NEW: Payment Intent Data
    private String paymentIntentId;
    private String clientSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initializeComponents();
        setupToolbar();
        getIntentData();
        setupListeners();

        // ✅ CHANGED: Load payment config from backend first
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
        selectedPaymentCard = findViewById(R.id.card_selected_payment);
        tvSelectedPaymentMethod = findViewById(R.id.tv_selected_payment_method);
        ivSelectedPaymentIcon = findViewById(R.id.iv_selected_payment_icon);

        // Card input fields
        cardStripeInput = findViewById(R.id.card_stripe_input);
        tvCardInstructions = findViewById(R.id.tv_card_instructions);
        etCardNumber = findViewById(R.id.et_card_number);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        etCvc = findViewById(R.id.et_cvc);
        btnFillVisa = findViewById(R.id.btn_fill_visa);
        btnFillMastercard = findViewById(R.id.btn_fill_mastercard);
        btnFillDeclined = findViewById(R.id.btn_fill_declined);

        // Action buttons
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
        amount = BigDecimal.valueOf(intent.getDoubleExtra(EXTRA_AMOUNT, 0.0));
        currency = intent.getStringExtra(EXTRA_CURRENCY);
        description = intent.getStringExtra(EXTRA_DESCRIPTION);

        if (transactionId == 0L) {
            Toast.makeText(this, "Invalid transaction", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayTransactionInfo();
    }

    private void displayTransactionInfo() {
        tvProductTitle.setText(description != null ? description : "Product Purchase");
        tvAmount.setText(String.format("₫%,.0f", amount));
        tvDescription.setText("Select your payment method");
    }

    // ✅ NEW: Load payment config from backend
    private void loadPaymentConfig() {
        Log.d(TAG, "🔄 Loading payment config from backend...");
        showLoading(true);

        paymentService.getPaymentConfig().enqueue(new Callback<StandardResponse<PaymentConfig>>() {
            @Override
            public void onResponse(Call<StandardResponse<PaymentConfig>> call,
                                   Response<StandardResponse<PaymentConfig>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    paymentConfig = response.body().getData();
                    Log.d(TAG, "✅ Payment config loaded successfully");
                    setupPaymentMethods();
                } else {
                    Log.e(TAG, "❌ Failed to load payment config: " +
                            (response.body() != null ? response.body().getMessage() : "Unknown error"));
                    // Fallback to default config
                    setupPaymentMethodsWithFallback();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<PaymentConfig>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "❌ Network error loading payment config", t);
                setupPaymentMethodsWithFallback();
            }
        });
    }

    private void setupPaymentMethodsWithFallback() {
        Log.d(TAG, "Using fallback payment config");
        paymentConfig = new PaymentConfig();
        paymentConfig.setPublishableKey("pk_test_fallback");
        paymentConfig.setCurrency("VND");
        setupPaymentMethods();
    }

    private void setupPaymentMethods() {
        layoutPaymentMethods.removeAllViews();

        addPaymentMethodOption(PaymentMethod.CARD, "Credit/Debit Card", R.drawable.ic_credit_card);
        addPaymentMethodOption(PaymentMethod.DIGITAL_WALLET, "Digital Wallet", R.drawable.ic_wallet);
        addPaymentMethodOption(PaymentMethod.BANK_TRANSFER, "Bank Transfer", R.drawable.ic_bank);
        addPaymentMethodOption(PaymentMethod.CASH, "Cash Payment", R.drawable.ic_cash);

        // Default to cash for simplicity
        selectPaymentMethod(PaymentMethod.CASH);
        Log.d(TAG, "✅ Payment methods setup completed");
    }

    private void setupListeners() {
        btnProceedPayment.setOnClickListener(v -> proceedWithPayment());
        btnCancel.setOnClickListener(v -> showCancelConfirmation());
        selectedPaymentCard.setOnClickListener(v -> showPaymentMethodSelection());

        setupCardInputListeners();
    }

    private void setupCardInputListeners() {
        // Auto-format card number
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("\\s", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < input.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(input.charAt(i));
                }

                etCardNumber.setText(formatted.toString());
                etCardNumber.setSelection(formatted.length());
                isFormatting = false;
            }
        });

        // Auto-format expiry date
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("/", "");
                if (input.length() >= 2) {
                    String formatted = input.substring(0, 2) + "/" + input.substring(2);
                    etExpiryDate.setText(formatted);
                    etExpiryDate.setSelection(formatted.length());
                }
                isFormatting = false;
            }
        });

        // Quick fill buttons
        btnFillVisa.setOnClickListener(v -> fillTestCard("4242 4242 4242 4242", "12/25", "123"));
        btnFillMastercard.setOnClickListener(v -> fillTestCard("5555 5555 5555 4444", "12/25", "123"));
        btnFillDeclined.setOnClickListener(v -> fillTestCard("4000 0000 0000 0002", "12/25", "123"));
    }

    private void fillTestCard(String cardNumber, String expiry, String cvc) {
        etCardNumber.setText(cardNumber);
        etExpiryDate.setText(expiry);
        etCvc.setText(cvc);
        Toast.makeText(this, "Test card filled", Toast.LENGTH_SHORT).show();
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

        tvSelectedPaymentMethod.setText(method.getDisplayName());
        int iconRes = getPaymentMethodIcon(method);
        ivSelectedPaymentIcon.setImageResource(iconRes);

        selectedPaymentCard.setVisibility(View.VISIBLE);
        btnProceedPayment.setEnabled(true);

        if (method == PaymentMethod.CARD) {
            showStripeCardInput();
        } else {
            hideStripeCardInput();
        }

        updatePaymentMethodSelection(method);
        Log.d(TAG, "Selected payment method: " + method);
    }

    private void showStripeCardInput() {
        cardStripeInput.setVisibility(View.VISIBLE);
        btnProceedPayment.setText("Pay with Card");
        fillTestCard("4242 4242 4242 4242", "12/25", "123");
    }

    private void hideStripeCardInput() {
        cardStripeInput.setVisibility(View.GONE);
        btnProceedPayment.setText("Proceed Payment");
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

    private void updatePaymentMethodSelection(PaymentMethod method) {
        for (int i = 0; i < layoutPaymentMethods.getChildCount(); i++) {
            View child = layoutPaymentMethods.getChildAt(i);
            MaterialCardView card = child.findViewById(R.id.card_payment_method);

            if (method.equals(child.getTag())) {
                card.setStrokeColor(getColor(R.color.primary));
                card.setStrokeWidth(3);
            } else {
                card.setStrokeColor(getColor(R.color.outline));
                card.setStrokeWidth(1);
            }
        }
    }

    private boolean validateCardInput() {
        String cardNumber = etCardNumber.getText().toString().replaceAll("\\s", "");
        String expiry = etExpiryDate.getText().toString();
        String cvc = etCvc.getText().toString();

        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            etCardNumber.setError("Invalid card number");
            return false;
        }

        if (expiry.length() != 5 || !expiry.contains("/")) {
            etExpiryDate.setError("Invalid expiry date");
            return false;
        }

        if (cvc.length() < 3 || cvc.length() > 4) {
            etCvc.setError("Invalid CVC");
            return false;
        }

        return true;
    }

    // ✅ ENHANCED: Proceed with payment
    private void proceedWithPayment() {
        if (selectedPaymentMethod == null) {
            showError("Please select a payment method");
            return;
        }

        switch (selectedPaymentMethod) {
            case CARD:
                if (validateCardInput()) {
                    createPaymentIntentAndProcess();
                }
                break;

            case CASH:
                processNonCardPayment("Cash payment arranged. Please contact the seller.");
                break;

            case BANK_TRANSFER:
                processNonCardPayment("Bank transfer arranged. Please contact the seller for details.");
                break;

            case DIGITAL_WALLET:
                processNonCardPayment("Digital wallet payment arranged.");
                break;

            default:
                showError("Payment method not supported");
                break;
        }
    }

    // ✅ NEW: Create payment intent and process
    private void createPaymentIntentAndProcess() {
        Log.d(TAG, "🔄 Creating payment intent for transaction: " + transactionId);
        showLoading(true);

        PaymentIntentRequest request = new PaymentIntentRequest();
        request.setTransactionId(transactionId);
        request.setDescription(description);

        String userId = String.valueOf(prefsManager.getUserId());

        paymentService.createPaymentIntent(userId, request)
                .enqueue(new Callback<StandardResponse<PaymentIntentResponse>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<PaymentIntentResponse>> call,
                                           Response<StandardResponse<PaymentIntentResponse>> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            PaymentIntentResponse data = response.body().getData();
                            paymentIntentId = data.getPaymentIntentId();
                            clientSecret = data.getClientSecret();

                            Log.d(TAG, "✅ Payment intent created: " + paymentIntentId);

                            // Now process the card payment
                            processCardPaymentWithIntent();
                        } else {
                            Log.e(TAG, "❌ Failed to create payment intent: " +
                                    (response.body() != null ? response.body().getMessage() : "Unknown error"));
                            showError("Failed to create payment intent");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<PaymentIntentResponse>> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "❌ Network error creating payment intent", t);
                        showError("Network error. Please try again.");
                    }
                });
    }

    // ✅ NEW: Process card payment with intent
    private void processCardPaymentWithIntent() {
        String cardNumber = etCardNumber.getText().toString().replaceAll("\\s", "");
        String expiry = etExpiryDate.getText().toString();
        String cvc = etCvc.getText().toString();

        Log.d(TAG, "🔄 Processing card payment with intent: " + paymentIntentId);
        Log.d(TAG, "Card: " + cardNumber.substring(0, 4) + "****");

        showLoading(true);

        // ✅ SIMULATE: Stripe payment processing
        // In real implementation, này sẽ gọi Stripe SDK
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check if it's a decline test card
            if (cardNumber.equals("4000000000000002")) {
                showLoading(false);
                showError("Card declined. Please try a different card.");
            } else {
                // Payment successful, confirm with backend
                confirmPaymentWithBackend();
            }
        }, 2000);
    }

    // ✅ NEW: Confirm payment with backend
    private void confirmPaymentWithBackend() {
        Log.d(TAG, "🔄 Confirming payment with backend: " + paymentIntentId);

        paymentService.confirmPayment(paymentIntentId)
                .enqueue(new Callback<StandardResponse<Payment>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Payment>> call,
                                           Response<StandardResponse<Payment>> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Payment payment = response.body().getData();
                            Log.d(TAG, "✅ Payment confirmed successfully with backend");

                            // Show success and return result
                            showCardPaymentSuccessWithData(payment);
                        } else {
                            Log.e(TAG, "❌ Failed to confirm payment with backend: " +
                                    (response.body() != null ? response.body().getMessage() : "Unknown error"));

                            // Payment succeeded với Stripe nhưng backend confirmation failed
                            showPartialSuccessDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Payment>> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "❌ Network error confirming payment", t);
                        showPartialSuccessDialog();
                    }
                });
    }

    // ✅ NEW: Show card payment success with data
    private void showCardPaymentSuccessWithData(Payment payment) {
        String cardNumber = etCardNumber.getText().toString().replaceAll("\\s", "");
        String cardType = getCardType(cardNumber);
        String maskedCard = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Successful! 🎉");
        builder.setMessage("Your " + cardType + " card (" + maskedCard + ") has been charged successfully.\n\n" +
                "Transaction has been completed and the seller will be notified.");

        builder.setPositiveButton("OK", (dialog, which) -> {
            // ✅ QUAN TRỌNG: Return với transaction data
            Intent resultIntent = new Intent();
            resultIntent.putExtra("payment_successful", true);
            resultIntent.putExtra("transaction_id", transactionId);
            resultIntent.putExtra("payment_intent_id", paymentIntentId);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        builder.setCancelable(false);
        builder.show();
    }

    // ✅ NEW: Show partial success dialog
    private void showPartialSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Completed");
        builder.setMessage("Your payment was processed successfully, but there was an issue updating the transaction status. Please contact support if needed.");

        builder.setPositiveButton("OK", (dialog, which) -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("payment_successful", true);
            resultIntent.putExtra("transaction_id", transactionId);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        builder.setCancelable(false);
        builder.show();
    }

    // ✅ ENHANCED: Process non-card payment
    private void processNonCardPayment(String message) {
        Log.d(TAG, "🔄 Processing non-card payment: " + selectedPaymentMethod);

        // For non-card payments, we still need to create a payment intent
        // but mark it as pending awaiting manual confirmation
        showLoading(true);

        // Simulate processing
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showLoading(false);
            showSimplePaymentSuccess(message);
        }, 1500);
    }

    private String getCardType(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return "Visa";
        } else if (cardNumber.startsWith("5")) {
            return "Mastercard";
        } else {
            return "Card";
        }
    }

    private void showSimplePaymentSuccess(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Arranged! 🎉");
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> {
            // ✅ FIXED: Return transaction data
            Intent resultIntent = new Intent();
            resultIntent.putExtra("payment_successful", true);
            resultIntent.putExtra("transaction_id", transactionId);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showPaymentMethodSelection() {
        Toast.makeText(this, "Select a payment method above", Toast.LENGTH_SHORT).show();
    }

    private void showCancelConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Payment");
        builder.setMessage("Are you sure you want to cancel this payment?");
        builder.setPositiveButton("Yes", (dialog, which) -> finish());
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        btnProceedPayment.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}