// app/src/main/java/com/example/newtrade/ui/payment/PaymentDetailActivity.java
package com.example.newtrade.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.PaymentService;
import com.example.newtrade.models.Payment;
import com.example.newtrade.models.PaymentStatus;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.Transaction;
import com.example.newtrade.models.User;
import com.example.newtrade.utils.DateUtils;
import com.example.newtrade.utils.ImageUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentDetailActivity extends AppCompatActivity {

    private static final String TAG = "PaymentDetailActivity";

    // Intent extras
    public static final String EXTRA_PAYMENT_ID = "payment_id";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";

    // UI Components
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;

    // Payment Info
    private TextView tvPaymentId;
    private TextView tvTransactionId;
    private TextView tvAmount;
    private TextView tvCurrency;
    private TextView tvStatus;
    private TextView tvPaymentMethod;
    private TextView tvCreatedDate;
    private TextView tvCompletedDate;
    private TextView tvStripeFee;
    private TextView tvNetAmount;
    private TextView tvFailureReason;
    private View statusIndicator;

    // Transaction Info
    private MaterialCardView cardTransactionInfo;
    private ImageView ivProductImage;
    private TextView tvProductTitle;
    private TextView tvProductPrice;
    private TextView tvSellerName;

    // Payment Method Info
    private MaterialCardView cardPaymentMethodInfo;
    private ImageView ivPaymentMethodIcon;
    private TextView tvPaymentMethodName;
    private TextView tvPaymentMethodDescription;

    // Action Buttons
    private MaterialButton btnViewTransaction;
    private MaterialButton btnRequestRefund;
    private MaterialButton btnPrintReceipt;

    // Data
    private Long paymentId;
    private Long transactionId;
    private Payment payment;
    private PaymentService paymentService;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_detail);

        initializeComponents();
        setupToolbar();
        getIntentData();
        setupListeners();
        loadPaymentDetail();

        Log.d(TAG, "PaymentDetailActivity created for payment: " + paymentId);
    }

    private void initializeComponents() {
        // Initialize SharedPrefsManager and API service
        prefsManager = new SharedPrefsManager(this);
        paymentService = ApiClient.getPaymentService();

        // UI components
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        contentLayout = findViewById(R.id.content_layout);

        // Payment info
        tvPaymentId = findViewById(R.id.tv_payment_id);
        tvTransactionId = findViewById(R.id.tv_transaction_id);
        tvAmount = findViewById(R.id.tv_amount);
        tvCurrency = findViewById(R.id.tv_currency);
        tvStatus = findViewById(R.id.tv_status);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvCreatedDate = findViewById(R.id.tv_created_date);
        tvCompletedDate = findViewById(R.id.tv_completed_date);
        tvStripeFee = findViewById(R.id.tv_stripe_fee);
        tvNetAmount = findViewById(R.id.tv_net_amount);
        tvFailureReason = findViewById(R.id.tv_failure_reason);
        statusIndicator = findViewById(R.id.status_indicator);

        // Transaction info
        cardTransactionInfo = findViewById(R.id.card_transaction_info);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvSellerName = findViewById(R.id.tv_seller_name);

        // Payment method info
        cardPaymentMethodInfo = findViewById(R.id.card_payment_method_info);
        ivPaymentMethodIcon = findViewById(R.id.iv_payment_method_icon);
        tvPaymentMethodName = findViewById(R.id.tv_payment_method_name);
        tvPaymentMethodDescription = findViewById(R.id.tv_payment_method_description);

        // Action buttons
        btnViewTransaction = findViewById(R.id.btn_view_transaction);
        btnRequestRefund = findViewById(R.id.btn_request_refund);
        btnPrintReceipt = findViewById(R.id.btn_print_receipt);

        Log.d(TAG, "Components initialized, user: " + prefsManager.getUserName());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment Details");
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        paymentId = intent.getLongExtra(EXTRA_PAYMENT_ID, 0L);
        transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, 0L);

        if (paymentId <= 0 && transactionId <= 0) {
            Toast.makeText(this, "Invalid payment information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Intent data - Payment ID: " + paymentId + ", Transaction ID: " + transactionId);
    }

    private void setupListeners() {
        btnViewTransaction.setOnClickListener(v -> viewTransactionDetails());
        btnRequestRefund.setOnClickListener(v -> requestRefund());
        btnPrintReceipt.setOnClickListener(v -> printReceipt());
    }

    private void loadPaymentDetail() {
        showLoading(true);

        String userId = String.valueOf(prefsManager.getUserId());

        Call<StandardResponse<Payment>> call;
        if (paymentId > 0) {
            // Load by payment ID (this endpoint might not exist in current backend)
            // call = paymentService.getPaymentById(userId, paymentId);
            // For now, use transaction ID
            if (transactionId > 0) {
                call = paymentService.getPaymentByTransaction(userId, transactionId);
            } else {
                showError("Unable to load payment details");
                return;
            }
        } else {
            // Load by transaction ID
            call = paymentService.getPaymentByTransaction(userId, transactionId);
        }

        call.enqueue(new Callback<StandardResponse<Payment>>() {
            @Override
            public void onResponse(Call<StandardResponse<Payment>> call, Response<StandardResponse<Payment>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Payment> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        payment = standardResponse.getData();
                        displayPaymentDetails();
                        Log.d(TAG, "Payment details loaded successfully");
                    } else {
                        showError("Failed to load payment details: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Failed to load payment details");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Payment>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to load payment details", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void displayPaymentDetails() {
        if (payment == null) return;

        // Payment basic info
        tvPaymentId.setText("Payment #" + payment.getId());
        tvTransactionId.setText("Transaction #" + payment.getTransactionId());
        tvAmount.setText(payment.getDisplayAmount());
        tvCurrency.setText(payment.getCurrency() != null ? payment.getCurrency().toUpperCase() : "VND");

        // Status
        tvStatus.setText(payment.getStatusDisplay());
        setStatusColor(payment.getStatus());

        // Payment method
        tvPaymentMethod.setText(payment.getPaymentMethod().getDisplayName());
        setPaymentMethodInfo(payment.getPaymentMethod());

        // Dates
        if (payment.getCreatedAt() != null) {
            tvCreatedDate.setText(DateUtils.formatDateTime(payment.getCreatedAt().toString()));
        }

        if (payment.getCompletedAt() != null) {
            tvCompletedDate.setText(DateUtils.formatDateTime(payment.getCompletedAt().toString()));
            findViewById(R.id.layout_completed_date).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_completed_date).setVisibility(View.GONE);
        }

        // Financial details
        if (payment.getStripeFee() != null && payment.getStripeFee().doubleValue() > 0) {
            tvStripeFee.setText(String.format("$%.2f", payment.getStripeFee()));
            findViewById(R.id.layout_stripe_fee).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_stripe_fee).setVisibility(View.GONE);
        }

        if (payment.getNetAmount() != null) {
            tvNetAmount.setText(String.format("$%.2f", payment.getNetAmount()));
            findViewById(R.id.layout_net_amount).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_net_amount).setVisibility(View.GONE);
        }

        // Failure reason
        if (payment.getFailureReason() != null && !payment.getFailureReason().trim().isEmpty()) {
            tvFailureReason.setText(payment.getFailureReason());
            findViewById(R.id.layout_failure_reason).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_failure_reason).setVisibility(View.GONE);
        }

        // Transaction info
        displayTransactionInfo();

        // Action buttons
        setupActionButtons();
    }

    private void displayTransactionInfo() {
        Transaction transaction = payment.getTransaction();
        if (transaction != null) {
            cardTransactionInfo.setVisibility(View.VISIBLE);

            // Product info - ưu tiên dùng transaction data trực tiếp
            if (transaction.getProductTitle() != null) {
                tvProductTitle.setText(transaction.getProductTitle());
            } else if (transaction.getProduct() != null) {
                tvProductTitle.setText(transaction.getProduct().getTitle());
            }

            if (transaction.getDisplayPrice() != null) {
                tvProductPrice.setText(transaction.getDisplayPrice());
            } else if (transaction.getProduct() != null) {
                tvProductPrice.setText(transaction.getProduct().getDisplayPrice());
            }

            // Load product image - ưu tiên transaction image URL
            if (transaction.getProductImageUrl() != null) {
                ImageUtils.loadImage(this, transaction.getProductImageUrl(), ivProductImage);
            } else if (transaction.getProduct() != null &&
                    transaction.getProduct().getImageUrls() != null &&
                    !transaction.getProduct().getImageUrls().isEmpty()) {
                ImageUtils.loadImage(this, transaction.getProduct().getImageUrls().get(0), ivProductImage);
            }

            // Seller info - ưu tiên dùng transaction data
            if (transaction.getSellerName() != null) {
                tvSellerName.setText("Seller: " + transaction.getSellerName());
            } else {
                User seller = transaction.getSeller();
                if (seller != null) {
                    tvSellerName.setText("Seller: " + seller.getDisplayName());
                }
            }
        } else {
            cardTransactionInfo.setVisibility(View.GONE);
        }
    }

    private void setPaymentMethodInfo(com.example.newtrade.models.PaymentMethod paymentMethod) {
        int iconRes;
        String description;

        switch (paymentMethod) {
            case CARD:
                iconRes = R.drawable.ic_credit_card;
                description = "Secure payment via credit/debit card";
                break;
            case DIGITAL_WALLET:
                iconRes = R.drawable.ic_wallet;
                description = "Digital wallet payment";
                break;
            case BANK_TRANSFER:
                iconRes = R.drawable.ic_bank;
                description = "Bank transfer payment";
                break;
            case CASH:
                iconRes = R.drawable.ic_cash;
                description = "Cash payment on delivery";
                break;
            default:
                iconRes = R.drawable.ic_payment;
                description = "Payment method";
                break;
        }

        ivPaymentMethodIcon.setImageResource(iconRes);
        tvPaymentMethodName.setText(paymentMethod.getDisplayName());
        tvPaymentMethodDescription.setText(description);
    }

    private void setStatusColor(PaymentStatus status) {
        int colorRes;

        switch (status) {
            case SUCCEEDED:
                colorRes = R.color.success;
                break;
            case PENDING:
            case PROCESSING:
                colorRes = R.color.warning;
                break;
            case FAILED:
            case CANCELED:
                colorRes = R.color.error;
                break;
            case REFUNDED:
            case PARTIALLY_REFUNDED:
                colorRes = R.color.info;
                break;
            default:
                colorRes = R.color.on_surface_variant;
                break;
        }

        tvStatus.setTextColor(getResources().getColor(colorRes, null));
        statusIndicator.setBackgroundColor(getResources().getColor(colorRes, null));
    }

    private void setupActionButtons() {
        if (payment == null) return;

        // View Transaction button - always visible if transaction exists
        btnViewTransaction.setVisibility(payment.getTransaction() != null ? View.VISIBLE : View.GONE);

        // Request Refund button - only for successful card payments
        boolean canRefund = payment.getStatus() == PaymentStatus.SUCCEEDED &&
                payment.getPaymentMethod() != com.example.newtrade.models.PaymentMethod.CASH;
        btnRequestRefund.setVisibility(canRefund ? View.VISIBLE : View.GONE);

        // Print Receipt button - for completed payments
        btnPrintReceipt.setVisibility(payment.isCompleted() ? View.VISIBLE : View.GONE);
    }

    private void viewTransactionDetails() {
        if (payment != null && payment.getTransactionId() != null) {
            // Navigate to transaction detail
            Intent intent = new Intent(this, com.example.newtrade.ui.transaction.TransactionDetailActivity.class);
            intent.putExtra("transaction_id", payment.getTransactionId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Transaction details not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestRefund() {
        if (payment == null) return;

        // Show refund confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Request Refund");
        builder.setMessage("Are you sure you want to request a refund for this payment?\n\n" +
                "Amount: " + payment.getDisplayAmount() + "\n" +
                "This action cannot be undone.");

        builder.setPositiveButton("Request Refund", (dialog, which) -> {
            // For now, show coming soon message
            Toast.makeText(this, "Refund request feature coming soon", Toast.LENGTH_SHORT).show();

            // Future implementation:
            // Intent intent = new Intent(this, RefundRequestActivity.class);
            // intent.putExtra("payment_id", payment.getId());
            // startActivity(intent);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void printReceipt() {
        if (payment == null) return;

        // Generate and share receipt
        String receipt = generateReceiptText();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Payment Receipt - TradeUp");
        shareIntent.putExtra(Intent.EXTRA_TEXT, receipt);

        startActivity(Intent.createChooser(shareIntent, "Share Receipt"));
    }

    private String generateReceiptText() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== TRADEUP PAYMENT RECEIPT ===\n\n");
        receipt.append("Payment ID: ").append(payment.getId()).append("\n");
        receipt.append("Transaction ID: ").append(payment.getTransactionId()).append("\n");
        receipt.append("Amount: ").append(payment.getDisplayAmount()).append("\n");
        receipt.append("Payment Method: ").append(payment.getPaymentMethod().getDisplayName()).append("\n");
        receipt.append("Status: ").append(payment.getStatusDisplay()).append("\n");

        if (payment.getCreatedAt() != null) {
            receipt.append("Date: ").append(DateUtils.formatDateTime(payment.getCreatedAt())).append("\n");
        }

        if (payment.getTransaction() != null && payment.getTransaction().getProduct() != null) {
            receipt.append("\nProduct: ").append(payment.getTransaction().getProduct().getTitle()).append("\n");
        }

        receipt.append("\nCustomer: ").append(prefsManager.getUserName()).append("\n");
        receipt.append("Email: ").append(prefsManager.getUserEmail()).append("\n");

        receipt.append("\n=== Thank you for using TradeUp! ===");

        return receipt.toString();
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
}