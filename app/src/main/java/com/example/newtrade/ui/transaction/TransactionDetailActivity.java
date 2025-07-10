// app/src/main/java/com/example/newtrade/ui/transaction/TransactionDetailActivity.java
package com.example.newtrade.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.Transaction;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.ui.review.WriteReviewActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionDetailActivity extends AppCompatActivity {

    private static final String TAG = "TransactionDetailActivity";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    private static final int REQUEST_WRITE_REVIEW = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivProductImage;
    private TextView tvProductTitle;
    private TextView tvTransactionId;
    private TextView tvFinalAmount;
    private TextView tvStatus;
    private TextView tvCreatedAt;
    private TextView tvPaymentMethod;
    private TextView tvDeliveryMethod;
    private TextView tvDeliveryAddress;

    // Other party info
    private CircleImageView ivOtherPartyAvatar;
    private TextView tvOtherPartyName;
    private TextView tvTransactionRole;

    // Action buttons
    private Button btnWriteReview;
    private Button btnContact;
    private Button btnCompleteTransaction;

    // Data
    private Long transactionId;
    private Transaction transaction;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        initViews();
        setupToolbar();
        getIntentData();
        setupListeners();
        loadTransactionDetail();

        Log.d(TAG, "TransactionDetailActivity created for transaction: " + transactionId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvTransactionId = findViewById(R.id.tv_transaction_id);
        tvFinalAmount = findViewById(R.id.tv_final_amount);
        tvStatus = findViewById(R.id.tv_status);
        tvCreatedAt = findViewById(R.id.tv_created_at);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvDeliveryMethod = findViewById(R.id.tv_delivery_method);
        tvDeliveryAddress = findViewById(R.id.tv_delivery_address);

        ivOtherPartyAvatar = findViewById(R.id.iv_other_party_avatar);
        tvOtherPartyName = findViewById(R.id.tv_other_party_name);
        tvTransactionRole = findViewById(R.id.tv_transaction_role);

        btnWriteReview = findViewById(R.id.btn_write_review);
        btnContact = findViewById(R.id.btn_contact);
        btnCompleteTransaction = findViewById(R.id.btn_complete_transaction);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Transaction Details");
        }
    }

    private void getIntentData() {
        transactionId = getIntent().getLongExtra(EXTRA_TRANSACTION_ID, 0L);

        if (transactionId == 0L) {
            Toast.makeText(this, "Invalid transaction", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        btnWriteReview.setOnClickListener(v -> openWriteReview());
        btnContact.setOnClickListener(v -> openChat());
        btnCompleteTransaction.setOnClickListener(v -> completeTransaction());
    }

    private void loadTransactionDetail() {
        String userId = String.valueOf(prefsManager.getUserId());

        Log.d(TAG, "🔍 Loading transaction detail: userId=" + userId + ", transactionId=" + transactionId);

        ApiClient.getTransactionService().getTransactionWithAuth(userId, transactionId)
                .enqueue(new Callback<StandardResponse<Transaction>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Transaction>> call,
                                           Response<StandardResponse<Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            transaction = response.body().getData();
                            displayTransactionDetails();
                            Log.d(TAG, "✅ Transaction loaded successfully");
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to load transaction";
                            Toast.makeText(TransactionDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "❌ Error loading transaction: " + errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Transaction>> call, Throwable t) {
                        Log.e(TAG, "❌ Network error loading transaction", t);
                        Toast.makeText(TransactionDetailActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayTransactionDetails() {
        if (transaction == null) {
            Log.e(TAG, "❌ Transaction is null, cannot display details");
            return;
        }

        Long currentUserId = prefsManager.getUserId();

        // Product info
        tvProductTitle.setText(transaction.getProductTitle() != null ?
                transaction.getProductTitle() : "Unknown Product");

        if (transaction.getProductImageUrl() != null && !transaction.getProductImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(transaction.getProductImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .centerCrop()
                    .into(ivProductImage);
        }

        // Transaction info
        tvTransactionId.setText("ID: " + transaction.getId());
        tvFinalAmount.setText(transaction.getFormattedPrice());
        tvStatus.setText(transaction.getStatusDisplayText());
        tvStatus.setTextColor(getColor(transaction.getStatusColor()));
        tvCreatedAt.setText(transaction.getCreatedAt());
        tvPaymentMethod.setText(transaction.getPaymentMethod() != null ?
                transaction.getPaymentMethod() : "Not specified");
        tvDeliveryMethod.setText(transaction.getDeliveryMethodText());

        if (transaction.getDeliveryAddress() != null && !transaction.getDeliveryAddress().isEmpty()) {
            tvDeliveryAddress.setText(transaction.getDeliveryAddress());
            tvDeliveryAddress.setVisibility(View.VISIBLE);
        } else {
            tvDeliveryAddress.setVisibility(View.GONE);
        }

        // Other party info
        String otherPartyName = transaction.getOtherPartyName(currentUserId);
        tvOtherPartyName.setText(otherPartyName != null ? otherPartyName : "Unknown User");

        String role = transaction.getTransactionRole(currentUserId);
        tvTransactionRole.setText(role);

        if ("Purchase".equals(role)) {
            tvTransactionRole.setTextColor(getColor(android.R.color.holo_blue_dark));
        } else if ("Sale".equals(role)) {
            tvTransactionRole.setTextColor(getColor(android.R.color.holo_green_dark));
        }

        String otherPartyAvatarUrl = transaction.getOtherPartyAvatarUrl(currentUserId);
        if (otherPartyAvatarUrl != null && !otherPartyAvatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(otherPartyAvatarUrl)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(ivOtherPartyAvatar);
        }

        // Button states
        setupButtonStates();
    }

    // ✅ ENHANCED: Setup button states với debug logging
    private void setupButtonStates() {
        if (transaction == null) {
            Log.e(TAG, "❌ Cannot setup button states - transaction is null");
            return;
        }

        Long currentUserId = prefsManager.getUserId();

        // ✅ DEBUG: Log transaction state chi tiết
        Log.d(TAG, "=== TRANSACTION DEBUG INFO ===");
        Log.d(TAG, "Transaction ID: " + transaction.getId());
        Log.d(TAG, "Payment Status: " + transaction.getPaymentStatus());
        Log.d(TAG, "isCompleted(): " + transaction.isCompleted());
        Log.d(TAG, "isPaid(): " + transaction.isPaid());
        Log.d(TAG, "isCanReview(): " + transaction.isCanReview());
        Log.d(TAG, "isHasReviewed(): " + transaction.isHasReviewed());
        Log.d(TAG, "Current User ID: " + currentUserId);
        Log.d(TAG, "isSeller: " + transaction.isSeller(currentUserId));
        Log.d(TAG, "isBuyer: " + transaction.isBuyer(currentUserId));
        Log.d(TAG, "=============================");

        // ✅ ENHANCED: Review button logic - check backend cho mọi transaction PAID/COMPLETED
        boolean transactionEligible = transaction.isCompleted() || transaction.isPaid();

        if (transactionEligible) {
            Log.d(TAG, "✅ Transaction eligible for review, checking backend...");
            checkCanReviewFromBackend();
        } else {
            btnWriteReview.setVisibility(View.GONE);
            Log.d(TAG, "❌ Transaction not eligible for review yet (not PAID/COMPLETED)");
        }

        // Contact button - always visible for completed/paid transactions
        btnContact.setVisibility(View.VISIBLE);

        // Complete transaction button - only for sellers when transaction is paid but not completed
        if (transaction.isSeller(currentUserId) && transaction.isPaid() && !transaction.isCompleted()) {
            btnCompleteTransaction.setVisibility(View.VISIBLE);
            btnCompleteTransaction.setText("Mark as Complete");
            btnCompleteTransaction.setEnabled(true);
            Log.d(TAG, "✅ Showing 'Complete Transaction' button for seller");
        } else {
            btnCompleteTransaction.setVisibility(View.GONE);
            Log.d(TAG, "❌ Hiding 'Complete Transaction' button");
        }
    }

    // ✅ NEW: Check backend để xác định có thể review không
    private void checkCanReviewFromBackend() {
        if (transaction == null) {
            Log.e(TAG, "❌ Cannot check review eligibility - transaction is null");
            return;
        }

        Long currentUserId = prefsManager.getUserId();

        Log.d(TAG, "🔍 Checking review eligibility with backend for transaction: " + transaction.getId());

        ApiClient.getReviewService().canReviewTransaction(transaction.getId(), currentUserId)
                .enqueue(new Callback<StandardResponse<Map<String, Boolean>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Boolean>>> call,
                                           Response<StandardResponse<Map<String, Boolean>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Boolean> data = response.body().getData();
                            boolean canReview = data != null && Boolean.TRUE.equals(data.get("canReview"));

                            Log.d(TAG, "✅ Backend canReview response: " + canReview);

                            if (canReview) {
                                showReviewButton(true);
                            } else {
                                showReviewButton(false);
                            }
                        } else {
                            Log.e(TAG, "❌ Backend error checking review eligibility: " +
                                    (response.body() != null ? response.body().getMessage() : response.message()));
                            // Fallback: show button for user to try
                            showReviewButtonFallback();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Boolean>>> call, Throwable t) {
                        Log.e(TAG, "❌ Network error checking review eligibility", t);
                        // Fallback: show button anyway
                        showReviewButtonFallback();
                    }
                });
    }

    // ✅ NEW: Show review button based on eligibility
    private void showReviewButton(boolean canReview) {
        if (canReview) {
            btnWriteReview.setVisibility(View.VISIBLE);
            btnWriteReview.setText("Write Review ⭐");
            btnWriteReview.setEnabled(true);
            // Set primary color if available
            try {
                btnWriteReview.setBackgroundTintList(getColorStateList(R.color.primary_color));
            } catch (Exception e) {
                // Fallback to default if color not found
                Log.w(TAG, "Primary color not found, using default");
            }
            Log.d(TAG, "✅ Showing enabled 'Write Review' button");
        } else {
            btnWriteReview.setVisibility(View.VISIBLE);
            btnWriteReview.setText("Already Reviewed ✓");
            btnWriteReview.setEnabled(false);
            // Set gray color for disabled state
            try {
                btnWriteReview.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            } catch (Exception e) {
                Log.w(TAG, "Gray color not found, using default");
            }
            Log.d(TAG, "⚪ Showing disabled 'Already Reviewed' button");
        }
    }

    // ✅ NEW: Fallback method để show review button
    private void showReviewButtonFallback() {
        btnWriteReview.setVisibility(View.VISIBLE);
        btnWriteReview.setText("Write Review");
        btnWriteReview.setEnabled(true);
        // Use default button color
        try {
            btnWriteReview.setBackgroundTintList(getColorStateList(R.color.primary_color));
        } catch (Exception e) {
            Log.w(TAG, "Primary color not found, using default");
        }
        Log.d(TAG, "✅ Showing review button as fallback (backend check failed)");
    }

    // ✅ ENHANCED: Open write review với validation tốt hơn
    private void openWriteReview() {
        if (transaction == null) {
            Toast.makeText(this, "Transaction data not available", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Cannot open review - transaction is null");
            return;
        }

        // ✅ Check eligibility - allow cho cả PAID và COMPLETED
        boolean canProceed = transaction.isCompleted() || transaction.isPaid();

        if (!canProceed) {
            Toast.makeText(this, "Transaction must be completed before writing a review", Toast.LENGTH_LONG).show();
            Log.w(TAG, "❌ Cannot review - transaction not completed/paid");
            return;
        }

        // Disabled button check
        if (!btnWriteReview.isEnabled()) {
            Toast.makeText(this, "You have already reviewed this transaction", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "❌ Cannot review - already reviewed");
            return;
        }

        Intent intent = new Intent(this, WriteReviewActivity.class);
        intent.putExtra(WriteReviewActivity.EXTRA_TRANSACTION_ID, transaction.getId());

        Long currentUserId = prefsManager.getUserId();
        String revieweeName = transaction.getOtherPartyName(currentUserId);
        intent.putExtra(WriteReviewActivity.EXTRA_REVIEWEE_NAME, revieweeName);
        intent.putExtra(WriteReviewActivity.EXTRA_PRODUCT_TITLE, transaction.getProductTitle());

        Log.d(TAG, "🌟 Opening WriteReviewActivity for transaction: " + transaction.getId() +
                ", reviewee: " + revieweeName);

        startActivityForResult(intent, REQUEST_WRITE_REVIEW);
    }

    private void openChat() {
        if (transaction == null) {
            Log.e(TAG, "❌ Cannot open chat - transaction is null");
            return;
        }

        Long currentUserId = prefsManager.getUserId();
        Long otherPartyId;

        if (transaction.isBuyer(currentUserId)) {
            otherPartyId = transaction.getSellerId();
        } else {
            otherPartyId = transaction.getBuyerId();
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("product_id", transaction.getProductId());
        intent.putExtra("other_user_id", otherPartyId);
        startActivity(intent);

        Log.d(TAG, "💬 Opening chat with user: " + otherPartyId + " for product: " + transaction.getProductId());
    }

    private void completeTransaction() {
        if (transaction == null) {
            Log.e(TAG, "❌ Cannot complete transaction - transaction is null");
            return;
        }

        String userId = String.valueOf(prefsManager.getUserId());

        Log.d(TAG, "🔄 Completing transaction: " + transaction.getId());

        ApiClient.getTransactionService().completeTransactionWithAuth(userId, transaction.getId())
                .enqueue(new Callback<StandardResponse<Transaction>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Transaction>> call,
                                           Response<StandardResponse<Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            transaction = response.body().getData();
                            displayTransactionDetails();
                            Toast.makeText(TransactionDetailActivity.this, "Transaction completed successfully! 🎉", Toast.LENGTH_SHORT).show();

                            Log.d(TAG, "✅ Transaction completed successfully");

                            // ✅ NEW: Auto-prompt cho review sau khi complete
                            if (transaction.isCompleted()) {
                                Toast.makeText(TransactionDetailActivity.this, "You can now write a review! ⭐", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to complete transaction";
                            Toast.makeText(TransactionDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "❌ Error completing transaction: " + errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Transaction>> call, Throwable t) {
                        Log.e(TAG, "❌ Network error completing transaction", t);
                        Toast.makeText(TransactionDetailActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_WRITE_REVIEW && resultCode == RESULT_OK) {
            // ✅ ENHANCED: Show success message and reload data
            Toast.makeText(this, "Thank you for your review! ⭐", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "✅ Review submitted successfully, reloading transaction data");
            loadTransactionDetail(); // Reload để update review status
        }
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