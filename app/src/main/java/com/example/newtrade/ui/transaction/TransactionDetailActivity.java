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
        ApiClient.getTransactionService().getTransaction(transactionId)
                .enqueue(new Callback<StandardResponse<Transaction>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Transaction>> call,
                                           Response<StandardResponse<Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            transaction = response.body().getData();
                            displayTransactionDetails();
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to load transaction";
                            Toast.makeText(TransactionDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error loading transaction: " + errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Transaction>> call, Throwable t) {
                        Log.e(TAG, "Network error loading transaction", t);
                        Toast.makeText(TransactionDetailActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayTransactionDetails() {
        if (transaction == null) return;

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

    private void setupButtonStates() {
        if (transaction == null) return;

        // Review button
        if (transaction.isCompleted() && transaction.isCanReview() && !transaction.isHasReviewed()) {
            btnWriteReview.setVisibility(View.VISIBLE);
            btnWriteReview.setText("Write Review");
            btnWriteReview.setEnabled(true);
        } else if (transaction.isHasReviewed()) {
            btnWriteReview.setVisibility(View.VISIBLE);
            btnWriteReview.setText("Reviewed");
            btnWriteReview.setEnabled(false);
        } else {
            btnWriteReview.setVisibility(View.GONE);
        }

        // Contact button
        btnContact.setVisibility(View.VISIBLE);

        // Complete transaction button (for sellers when paid)
        Long currentUserId = prefsManager.getUserId();
        if (transaction.isSeller(currentUserId) && transaction.isPaid()) {
            btnCompleteTransaction.setVisibility(View.VISIBLE);
        } else {
            btnCompleteTransaction.setVisibility(View.GONE);
        }
    }

    private void openWriteReview() {
        if (transaction == null) return;

        Intent intent = new Intent(this, WriteReviewActivity.class);
        intent.putExtra(WriteReviewActivity.EXTRA_TRANSACTION_ID, transaction.getId());

        Long currentUserId = prefsManager.getUserId();
        String revieweeName = transaction.getOtherPartyName(currentUserId);
        intent.putExtra(WriteReviewActivity.EXTRA_REVIEWEE_NAME, revieweeName);
        intent.putExtra(WriteReviewActivity.EXTRA_PRODUCT_TITLE, transaction.getProductTitle());

        startActivityForResult(intent, REQUEST_WRITE_REVIEW);
    }

    private void openChat() {
        if (transaction == null) return;

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
    }

    private void completeTransaction() {
        if (transaction == null) return;

        ApiClient.getTransactionService().completeTransaction(transaction.getId())
                .enqueue(new Callback<StandardResponse<Transaction>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Transaction>> call,
                                           Response<StandardResponse<Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            transaction = response.body().getData();
                            displayTransactionDetails();
                            Toast.makeText(TransactionDetailActivity.this, "Transaction completed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to complete transaction";
                            Toast.makeText(TransactionDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Transaction>> call, Throwable t) {
                        Log.e(TAG, "Error completing transaction", t);
                        Toast.makeText(TransactionDetailActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_WRITE_REVIEW && resultCode == RESULT_OK) {
            // Reload transaction to update review status
            loadTransactionDetail();
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