// app/src/main/java/com/example/newtrade/ui/review/WriteReviewActivity.java
package com.example.newtrade.ui.review;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Review;
import com.example.newtrade.models.ReviewRequest;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.Transaction;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriteReviewActivity extends AppCompatActivity {

    private static final String TAG = "WriteReviewActivity";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    public static final String EXTRA_REVIEWEE_NAME = "reviewee_name";
    public static final String EXTRA_PRODUCT_TITLE = "product_title";

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivRevieweeAvatar;
    private TextView tvRevieweeName, tvProductTitle, tvRatingLabel;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmitReview;

    // Data
    private Long transactionId;
    private String revieweeName;
    private String productTitle;
    private SharedPrefsManager prefsManager;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        initViews();
        setupToolbar();
        getIntentData();
        setupListeners();
        displayTransactionInfo();

        Log.d(TAG, "WriteReviewActivity created for transaction: " + transactionId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivRevieweeAvatar = findViewById(R.id.iv_reviewee_avatar);
        tvRevieweeName = findViewById(R.id.tv_reviewee_name);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvRatingLabel = findViewById(R.id.tv_rating_label);
        ratingBar = findViewById(R.id.rating_bar);
        etComment = findViewById(R.id.et_comment);
        btnSubmitReview = findViewById(R.id.btn_submit_review);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Write Review");
        }
    }

    private void getIntentData() {
        transactionId = getIntent().getLongExtra(EXTRA_TRANSACTION_ID, 0L);
        revieweeName = getIntent().getStringExtra(EXTRA_REVIEWEE_NAME);
        productTitle = getIntent().getStringExtra(EXTRA_PRODUCT_TITLE);

        if (transactionId == 0L) {
            Toast.makeText(this, "Invalid transaction", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                updateRatingLabel((int) rating);
                updateSubmitButton();
            }
        });

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void displayTransactionInfo() {
        if (revieweeName != null) {
            tvRevieweeName.setText(revieweeName);
        }

        if (productTitle != null) {
            tvProductTitle.setText(productTitle);
        }

        // Load default avatar or from cache
        Glide.with(this)
                .load(R.drawable.ic_user_placeholder)
                .circleCrop()
                .into(ivRevieweeAvatar);

        updateRatingLabel(0);
        updateSubmitButton();
    }

    private void updateRatingLabel(int rating) {
        String label;
        switch (rating) {
            case 1:
                label = "Poor";
                break;
            case 2:
                label = "Fair";
                break;
            case 3:
                label = "Good";
                break;
            case 4:
                label = "Very Good";
                break;
            case 5:
                label = "Excellent";
                break;
            default:
                label = "Tap to rate";
                break;
        }
        tvRatingLabel.setText(label);
    }

    private void updateSubmitButton() {
        boolean canSubmit = ratingBar.getRating() > 0;
        btnSubmitReview.setEnabled(canSubmit && !isSubmitting);
        btnSubmitReview.setText(isSubmitting ? "Submitting..." : "Submit Review");
    }

    private void submitReview() {
        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmitting = true;
        updateSubmitButton();

        ReviewRequest request = new ReviewRequest(transactionId, rating,
                TextUtils.isEmpty(comment) ? null : comment);

        ApiClient.getReviewService().submitReview(request).enqueue(new Callback<StandardResponse<Review>>() {
            @Override
            public void onResponse(Call<StandardResponse<Review>> call, Response<StandardResponse<Review>> response) {
                isSubmitting = false;
                updateSubmitButton();

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(WriteReviewActivity.this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to submit review";
                    Toast.makeText(WriteReviewActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Review>> call, Throwable t) {
                isSubmitting = false;
                updateSubmitButton();
                Log.e(TAG, "Error submitting review", t);
                Toast.makeText(WriteReviewActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
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