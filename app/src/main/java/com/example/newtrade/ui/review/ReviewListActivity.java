// app/src/main/java/com/example/newtrade/ui/review/ReviewListActivity.java
package com.example.newtrade.ui.review;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class ReviewListActivity extends AppCompatActivity {

    private static final String TAG = "ReviewListActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvReviews;
    private LinearLayout llEmptyState;

    // Data
    private List<Object> reviews; // Replace with Review model

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadReviews();

        Log.d(TAG, "ReviewListActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvReviews = findViewById(R.id.rv_reviews);
        llEmptyState = findViewById(R.id.ll_empty_state);

        reviews = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reviews");
        }
    }

    private void setupRecyclerView() {
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Set adapter
    }

    private void loadReviews() {
        // TODO: Load reviews from API
        updateUI();
    }

    private void updateUI() {
        if (reviews.isEmpty()) {
            llEmptyState.setVisibility(android.view.View.VISIBLE);
            rvReviews.setVisibility(android.view.View.GONE);
        } else {
            llEmptyState.setVisibility(android.view.View.GONE);
            rvReviews.setVisibility(android.view.View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}