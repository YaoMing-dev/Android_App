// app/src/main/java/com/example/newtrade/ui/transaction/TransactionHistoryActivity.java
package com.example.newtrade.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.TransactionAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.Transaction;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.ui.review.WriteReviewActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionHistoryActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionClickListener {

    private static final String TAG = "TransactionHistoryActivity";
    private static final int REQUEST_WRITE_REVIEW = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTransactions;
    private TextView tvEmptyState;

    // Data
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactions = new ArrayList<>();
    private SharedPrefsManager prefsManager;
    private String currentTab = "PURCHASES"; // PURCHASES or SALES
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupListeners();
        loadTransactions();

        Log.d(TAG, "TransactionHistoryActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvTransactions = findViewById(R.id.rv_transactions);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Transaction History");
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Purchases"));
        tabLayout.addTab(tabLayout.newTab().setText("Sales"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition() == 0 ? "PURCHASES" : "SALES";
                currentPage = 0;
                hasMoreData = true;
                loadTransactions();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(this, transactions, this);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);

        // Add pagination
        rvTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        loadMoreTransactions();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 0;
            hasMoreData = true;
            loadTransactions();
        });
    }

    private void loadTransactions() {
        if (isLoading) return;

        isLoading = true;
        if (currentPage == 0) {
            swipeRefresh.setRefreshing(true);
        }

        Call<StandardResponse<Map<String, Object>>> call;
        if ("PURCHASES".equals(currentTab)) {
            call = ApiClient.getTransactionService().getPurchases(currentPage, Constants.DEFAULT_PAGE_SIZE);
        } else {
            call = ApiClient.getTransactionService().getSales(currentPage, Constants.DEFAULT_PAGE_SIZE);
        }

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    handleTransactionsResponse(response.body().getData());
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to load transactions";
                    Log.e(TAG, "Error loading transactions: " + errorMsg);
                    Toast.makeText(TransactionHistoryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Network error loading transactions", t);
                Toast.makeText(TransactionHistoryActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
    }

    private void loadMoreTransactions() {
        currentPage++;
        loadTransactions();
    }

    private void handleTransactionsResponse(Map<String, Object> data) {
        try {
            if (data != null && data.containsKey("content")) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Transaction>>(){}.getType();
                List<Transaction> newTransactions = gson.fromJson(gson.toJson(data.get("content")), listType);

                if (currentPage == 0) {
                    transactions.clear();
                }

                if (newTransactions != null && !newTransactions.isEmpty()) {
                    transactions.addAll(newTransactions);
                    hasMoreData = newTransactions.size() >= Constants.DEFAULT_PAGE_SIZE;
                } else {
                    hasMoreData = false;
                }

                Log.d(TAG, "Loaded " + (newTransactions != null ? newTransactions.size() : 0) + " transactions for " + currentTab);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing transactions response", e);
        }

        updateUI();
    }

    private void updateUI() {
        if (transactionAdapter != null) {
            transactionAdapter.notifyDataSetChanged();
        }

        if (transactions.isEmpty()) {
            rvTransactions.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("PURCHASES".equals(currentTab) ?
                    "No purchases yet" : "No sales yet");
        } else {
            rvTransactions.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    // TransactionAdapter.OnTransactionClickListener implementation
    @Override
    public void onTransactionClick(Transaction transaction) {
        Intent intent = new Intent(this, TransactionDetailActivity.class);
        intent.putExtra(TransactionDetailActivity.EXTRA_TRANSACTION_ID, transaction.getId());
        startActivity(intent);
    }

    @Override
    public void onReviewClick(Transaction transaction) {
        Intent intent = new Intent(this, WriteReviewActivity.class);
        intent.putExtra(WriteReviewActivity.EXTRA_TRANSACTION_ID, transaction.getId());

        Long currentUserId = prefsManager.getUserId();
        String revieweeName = transaction.getOtherPartyName(currentUserId);
        intent.putExtra(WriteReviewActivity.EXTRA_REVIEWEE_NAME, revieweeName);
        intent.putExtra(WriteReviewActivity.EXTRA_PRODUCT_TITLE, transaction.getProductTitle());

        startActivityForResult(intent, REQUEST_WRITE_REVIEW);
    }

    @Override
    public void onContactClick(Transaction transaction) {
        // Start conversation with other party
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_WRITE_REVIEW && resultCode == RESULT_OK) {
            // Reload transactions to update review status
            currentPage = 0;
            hasMoreData = true;
            loadTransactions();
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