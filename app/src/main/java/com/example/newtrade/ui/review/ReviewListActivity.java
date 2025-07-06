// app/src/main/java/com/example/newtrade/ui/review/ReviewListActivity.java
package com.example.newtrade.ui.review;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ReviewAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Review;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewListActivity extends AppCompatActivity {

    private static final String TAG = "ReviewListActivity";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvReviews;
    private TextView tvEmptyState;

    // Data
    private ReviewAdapter reviewAdapter;
    private List<Review> reviews = new ArrayList<>();
    private Long userId;
    private String userName;
    private SharedPrefsManager prefsManager;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        initViews();
        setupToolbar();
        getIntentData();
        setupRecyclerView();
        setupListeners();
        loadReviews();

        Log.d(TAG, "ReviewListActivity created for user: " + userId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvReviews = findViewById(R.id.rv_reviews);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(userName != null ? userName + "'s Reviews" : "Reviews");
        }
    }

    private void getIntentData() {
        userId = getIntent().getLongExtra(EXTRA_USER_ID, 0L);
        userName = getIntent().getStringExtra(EXTRA_USER_NAME);

        if (userId == 0L) {
            // Default to current user
            userId = prefsManager.getUserId();
            userName = prefsManager.getUserName();
        }
    }

    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter(reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        // Add pagination
        rvReviews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && hasMoreData) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= Constants.DEFAULT_PAGE_SIZE) {
                        loadMoreReviews();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 0;
            hasMoreData = true;
            loadReviews();
        });
    }

    private void loadReviews() {
        if (isLoading) return;

        isLoading = true;
        if (currentPage == 0) {
            swipeRefresh.setRefreshing(true);
        }

        ApiClient.getReviewService().getUserReviews(userId, currentPage, Constants.DEFAULT_PAGE_SIZE)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        isLoading = false;
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            handleReviewsResponse(response.body().getData());
                        } else {
                            Log.e(TAG, "Error loading reviews: " + (response.body() != null ? response.body().getMessage() : "Unknown error"));
                            updateUI();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        isLoading = false;
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "Network error loading reviews", t);
                        updateUI();
                    }
                });
    }

    private void loadMoreReviews() {
        currentPage++;
        loadReviews();
    }

    private void handleReviewsResponse(Map<String, Object> data) {
        try {
            if (data != null && data.containsKey("content")) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Review>>(){}.getType();
                List<Review> newReviews = gson.fromJson(gson.toJson(data.get("content")), listType);

                if (currentPage == 0) {
                    reviews.clear();
                }

                if (newReviews != null && !newReviews.isEmpty()) {
                    reviews.addAll(newReviews);
                    hasMoreData = newReviews.size() >= Constants.DEFAULT_PAGE_SIZE;
                } else {
                    hasMoreData = false;
                }

                Log.d(TAG, "Loaded " + (newReviews != null ? newReviews.size() : 0) + " reviews");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing reviews response", e);
        }

        updateUI();
    }

    private void updateUI() {
        if (reviewAdapter != null) {
            reviewAdapter.notifyDataSetChanged();
        }

        if (reviews.isEmpty()) {
            rvReviews.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No reviews yet");
        } else {
            rvReviews.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
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