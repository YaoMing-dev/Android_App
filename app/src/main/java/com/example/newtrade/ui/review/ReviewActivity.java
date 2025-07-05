// app/src/main/java/com/example/newtrade/ui/review/ReviewActivity.java
package com.example.newtrade.ui.review;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {

    private static final String TAG = "ReviewActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvReviewTitle, tvRevieweeInfo, tvRatingLabel;
    private RatingBar rbRating;
    private TextInputLayout tilReviewComment;
    private EditText etReviewComment;
    private Button btnSubmitReview, btnCancel;

    // Data
    private Long transactionId;
    private Long revieweeId;
    private String revieweeName;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Initialize
        prefsManager = SharedPrefsManager.getInstance(this);

        // Get data from intent
        getIntentData();

        // Initialize views
        initViews();
        setupToolbar();
        setupListeners();

        // Display review info
        displayReviewInfo();

        Log.d(TAG, "✅ ReviewActivity created for transaction ID: " + transactionId);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        transactionId = intent.getLongExtra(Constants.EXTRA_TRANSACTION_ID, -1L);
        revieweeId = intent.getLongExtra(Constants.EXTRA_USER_ID, -1L);
        revieweeName = intent.getStringExtra(Constants.EXTRA_USER_NAME);

        if (transactionId == -1L || revieweeId == -1L) {
            Log.e(TAG, "❌ Transaction ID or reviewee ID not provided in intent");
            finish();
            return;
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvReviewTitle = findViewById(R.id.tv_review_title);
        tvRevieweeInfo = findViewById(R.id.tv_reviewee_info);
        tvRatingLabel = findViewById(R.id.tv_rating_label);
        rbRating = findViewById(R.id.rb_rating);
        tilReviewComment = findViewById(R.id.til_review_comment);
        etReviewComment = findViewById(R.id.et_review_comment);
        btnSubmitReview = findViewById(R.id.btn_submit_review);
        btnCancel = findViewById(R.id.btn_cancel);

        // Initially disable submit button
        btnSubmitReview.setEnabled(false);

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Write Review");
        }
    }

    private void setupListeners() {
        // Rating bar change listener
        rbRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            updateRatingLabel();
            updateSubmitButtonState();
        });

        // Review comment text change listener
        etReviewComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateReviewComment();
                updateSubmitButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Submit review button
        btnSubmitReview.setOnClickListener(v -> submitReview());

        // Cancel button
        btnCancel.setOnClickListener(v -> finish());
    }

    private void displayReviewInfo() {
        if (revieweeName != null) {
            tvRevieweeInfo.setText("Review for " + revieweeName);
        }

        updateRatingLabel();
    }

    private void updateRatingLabel() {
        int rating = (int) rbRating.getRating();
        String[] ratingLabels = {"", "Poor", "Fair", "Good", "Very Good", "Excellent"};

        if (rating >= 1 && rating <= 5) {
            tvRatingLabel.setText(ratingLabels[rating] + " (" + rating + "/5)");
        } else {
            tvRatingLabel.setText("Select a rating");
        }
    }

    private void validateReviewComment() {
        String comment = etReviewComment.getText().toString().trim();

        if (comment.isEmpty()) {
            tilReviewComment.setError(null);
            return;
        }

        if (!ValidationUtils.isValidReviewComment(comment)) {
            tilReviewComment.setError("Review comment should be between 10-500 characters");
        } else {
            tilReviewComment.setError(null);
        }
    }

    private void updateSubmitButtonState() {
        int rating = (int) rbRating.getRating();
        String comment = etReviewComment.getText().toString().trim();

        boolean isValid = rating >= 1 && rating <= 5 &&
                (comment.isEmpty() || ValidationUtils.isValidReviewComment(comment));

        btnSubmitReview.setEnabled(isValid);
    }

    private void submitReview() {
        int rating = (int) rbRating.getRating();
        String comment = etReviewComment.getText().toString().trim();

        if (!validateReviewForm(rating, comment)) {
            return;
        }

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "🔄 Submitting review for transaction: " + transactionId);

        // Disable button during submission
        btnSubmitReview.setEnabled(false);
        btnSubmitReview.setText("Submitting...");

        Map<String, Object> reviewRequest = new HashMap<>();
        reviewRequest.put("transactionId", transactionId);
        reviewRequest.put("rating", rating);
        if (!comment.isEmpty()) {
            reviewRequest.put("comment", comment);
        }

        ApiClient.getReviewService().createReview(userId, reviewRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(ReviewActivity.this,
                                        "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "✅ Review submitted successfully");

                                // Return to previous screen
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                showError(apiResponse.getMessage());
                                resetSubmitButton();
                            }
                        } else {
                            showError("Failed to submit review. Please try again.");
                            resetSubmitButton();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to submit review", t);
                        showError("Network error. Please try again.");
                        resetSubmitButton();
                    }
                });
    }

    private boolean validateReviewForm(int rating, String comment) {
        boolean isValid = true;

        // Validate rating
        if (rating < 1 || rating > 5) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate comment (optional but if provided, should be valid)
        if (!comment.isEmpty() && !ValidationUtils.isValidReviewComment(comment)) {
            tilReviewComment.setError("Review comment should be between 10-500 characters");
            isValid = false;
        }

        return isValid;
    }

    private void resetSubmitButton() {
        btnSubmitReview.setEnabled(true);
        btnSubmitReview.setText("Submit Review");
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
        Log.d(TAG, "ReviewActivity destroyed");
    }
}