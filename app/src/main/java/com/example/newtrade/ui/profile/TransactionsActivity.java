// app/src/main/java/com/example/newtrade/ui/profile/TransactionsActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.DateTimeUtils;
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.tabs.TabLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsActivity extends AppCompatActivity {
    private static final String TAG = "TransactionsActivity";

    // UI Components
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvTransactions;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View loadingView, contentView;

    // Data
    private List<TransactionItem> transactions = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    // State
    private String currentTab = "ALL"; // ALL, PURCHASE, SALE
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Transaction data class
    public static class TransactionItem {
        public Long id;
        public String type; // "PURCHASE" or "SALE"
        public Long productId;
        public String productTitle;
        public String productImage;
        public BigDecimal amount;
        public String status; // "COMPLETED", "PENDING", "CANCELLED"
        public Long otherUserId;
        public String otherUserName;
        public Date transactionDate;
        public String paymentMethod;
        public String notes;

        public TransactionItem() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        initViews();
        initUtils();
        setupToolbar();
        setupListeners();
        setupRecyclerView();
        setupTabs();

        loadTransactions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        rvTransactions = findViewById(R.id.rv_transactions);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        loadingView = findViewById(R.id.view_loading);
        contentView = findViewById(R.id.view_content);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Transactions");
        }
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Create TransactionsAdapter
        // TransactionsAdapter adapter = new TransactionsAdapter(transactions, this::onTransactionClick);
        // rvTransactions.setAdapter(adapter);

        // Pagination scroll listener
        rvTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && !isLastPage && layoutManager != null && dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        loadMoreTransactions();
                    }
                }
            }
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Purchases"));
        tabLayout.addTab(tabLayout.newTab().setText("Sales"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "ALL";
                        break;
                    case 1:
                        currentTab = "PURCHASE";
                        break;
                    case 2:
                        currentTab = "SALE";
                        break;
                }
                refreshData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // FR-9.2.1: Purchase history, FR-5.2.2: Archive sold items in user history
    private void loadTransactions() {
        if (isLoading) return;

        isLoading = true;
        if (currentPage == 0) {
            showLoadingState();
        }

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getUserService()
                .getUserTransactions(currentTab, currentPage, Constants.DEFAULT_PAGE_SIZE, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleTransactionsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleLoadingError(t);
            }
        });
    }

    private void loadMoreTransactions() {
        if (isLoading || isLastPage) return;

        currentPage++;
        loadTransactions();
    }

    private void refreshData() {
        currentPage = 0;
        isLastPage = false;
        transactions.clear();
        loadTransactions();
    }

    @SuppressWarnings("unchecked")
    private void handleTransactionsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        isLoading = false;
        swipeRefresh.setRefreshing(false);

        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> transactionMaps = (List<Map<String, Object>>) data.get("content");

                if (transactionMaps != null) {
                    int oldSize = transactions.size();

                    for (Map<String, Object> transactionMap : transactionMaps) {
                        TransactionItem transaction = parseTransactionFromMap(transactionMap);
                        if (transaction != null) {
                            transactions.add(transaction);
                        }
                    }

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : true;

                    // Update UI
                    showContentState();

                    if (transactions.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        // TODO: Notify adapter
                        // adapter.notifyItemRangeInserted(oldSize, transactions.size() - oldSize);
                    }
                }
            } else {
                handleLoadingError(new Exception("Failed to load transactions"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing transactions response", e);
            handleLoadingError(e);
        }
    }

    private TransactionItem parseTransactionFromMap(Map<String, Object> transactionMap) {
        try {
            TransactionItem transaction = new TransactionItem();

            transaction.id = ((Number) transactionMap.get("id")).longValue();
            transaction.type = (String) transactionMap.get("type");
            transaction.status = (String) transactionMap.get("status");
            transaction.paymentMethod = (String) transactionMap.get("paymentMethod");
            transaction.notes = (String) transactionMap.get("notes");

            Object amount = transactionMap.get("amount");
            if (amount instanceof Number) {
                transaction.amount = new BigDecimal(amount.toString());
            }

            // Parse product info
            @SuppressWarnings("unchecked")
            Map<String, Object> productMap = (Map<String, Object>) transactionMap.get("product");
            if (productMap != null) {
                transaction.productId = ((Number) productMap.get("id")).longValue();
                transaction.productTitle = (String) productMap.get("title");

                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) productMap.get("imageUrls");
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    transaction.productImage = imageUrls.get(0);
                }
            }

            // Parse other user info
            @SuppressWarnings("unchecked")
            Map<String, Object> otherUserMap = (Map<String, Object>) transactionMap.get("otherUser");
            if (otherUserMap != null) {
                transaction.otherUserId = ((Number) otherUserMap.get("id")).longValue();
                transaction.otherUserName = (String) otherUserMap.get("displayName");
            }

            // Parse transaction date
            String dateStr = (String) transactionMap.get("transactionDate");
            if (dateStr != null) {
                transaction.transactionDate = DateTimeUtils.parseISODate(dateStr);
            }

            return transaction;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing transaction", e);
            return null;
        }
    }

    private void onTransactionClick(TransactionItem transaction) {
        if (transaction.productId != null) {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(Constants.BUNDLE_PRODUCT_ID, transaction.productId);
            startActivity(intent);
        }
    }

    private void handleLoadingError(Throwable t) {
        isLoading = false;
        swipeRefresh.setRefreshing(false);

        Log.e(TAG, "Failed to load transactions", t);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showErrorToast("No internet connection");
        } else {
            showErrorToast(NetworkUtils.getNetworkErrorMessage(t));
        }

        if (transactions.isEmpty()) {
            showEmptyState();
        } else {
            showContentState();
        }
    }

    private void showLoadingState() {
        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
    }

    private void showContentState() {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        rvTransactions.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);

        String emptyMessage;
        switch (currentTab) {
            case "PURCHASE":
                emptyMessage = "No purchases yet\n\nItems you buy will appear here";
                break;
            case "SALE":
                emptyMessage = "No sales yet\n\nItems you sell will appear here";
                break;
            default:
                emptyMessage = "No transactions yet\n\nYour buying and selling history will appear here";
                break;
        }
        tvEmpty.setText(emptyMessage);
    }

    private void hideEmptyState() {
        rvTransactions.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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