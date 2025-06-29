package com.example.newtrade.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ReviewAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Review;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.NavigationUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserReviewsActivity extends AppCompatActivity {

    private static final String TAG = "UserReviewsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvReviews;
    private LinearLayout llEmptyState;

    // Data
    private ReviewAdapter reviewAdapter;
    private List<Review> reviews = new ArrayList<>();
    private Long userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reviews);

        getUserDataFromIntent();
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadUserReviews();
    }

    private void getUserDataFromIntent() {
        userId = getIntent().getLongExtra("user_id", -1L);
        userName = getIntent().getStringExtra("user_name");

        if (userId == -1L) {
            Log.e(TAG, "❌ No user ID provided");
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvReviews = findViewById(R.id.rv_reviews);
        llEmptyState = findViewById(R.id.ll_empty_state);
    }

    private void setupToolbar() {
        String title = userName != null ? userName + "'s Reviews" : "User Reviews";
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, title);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvReviews.setLayoutManager(layoutManager);

        reviewAdapter = new ReviewAdapter(reviews);
        rvReviews.setAdapter(reviewAdapter);
    }

    private void loadUserReviews() {
        Log.d(TAG, "Loading reviews for user: " + userId);

        ApiClient.getApiService().getUserReviews(userId)
            .enqueue(new Callback<StandardResponse<List<Review>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<List<Review>>> call,
                                       @NonNull Response<StandardResponse<List<Review>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<List<Review>> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            List<Review> userReviews = apiResponse.getData();

                            reviews.clear();
                            if (userReviews != null) {
                                reviews.addAll(userReviews);
                            }

                            reviewAdapter.notifyDataSetChanged();
                            updateEmptyState();

                            Log.d(TAG, "✅ User reviews loaded: " + reviews.size());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<List<Review>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failed to load user reviews", t);
                    Toast.makeText(UserReviewsActivity.this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            });
    }

    private void updateEmptyState() {
        if (reviews.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvReviews.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvReviews.setVisibility(View.VISIBLE);
        }
    }
}
