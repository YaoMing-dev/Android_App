// app/src/main/java/com/example/newtrade/ui/profile/TransactionsActivity.java
package com.example.newtrade.ui.profile;

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
import com.example.newtrade.models.Transaction;
import com.example.newtrade.ui.profile.adapter.TransactionAdapter;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.tabs.TabLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionClickListener {
    private static final String TAG = "TransactionsActivity";

    // UI Components
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvTransactions;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    // Data
    private TransactionAdapter adapter;
    private List<Transaction> transactions = new ArrayList<>();
    private String currentTab = "purchases"; // purchases or sales

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        prefsManager = new SharedPrefsManager(this);

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupListeners();
        loadTransactions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        rvTransactions = findViewById(R.id.rv_transactions);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Transactions");
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Purchases"));
        tabLayout.addTab(tabLayout.newTab().setText("Sales"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "purchases";
                        break;
                    case 1:
                        currentTab = "sales";
                        break;
                }
                refreshTransactions();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(transactions, this);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);

        // Pagination scroll listener
        rvTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreTransactions();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshTransactions);
    }

    private void loadTransactions() {
        if (isLoading) return;

        isLoading = true;
        showLoading(currentPage == 0);

        // TODO: Replace with actual transaction API calls
        // For now, showing empty state
        isLoading = false;
        hideLoading();
        showEmptyState();
    }

    private void loadMoreTransactions() {
        if (!isLoading && !isLastPage) {
            currentPage++;
            loadTransactions();
        }
    }

    private void refreshTransactions() {
        currentPage = 0;
        isLastPage = false;
        transactions.clear();
        adapter.notifyDataSetChanged();
        loadTransactions();
    }

    private void showLoading(boolean isInitialLoad) {
        if (isInitialLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
        swipeRefresh.setRefreshing(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void showEmptyState() {
        tvEmpty.setVisibility(View.VISIBLE);
        rvTransactions.setVisibility(View.GONE);

        String emptyMessage = currentTab.equals("purchases") ?
                "No purchases yet" : "No sales yet";
        tvEmpty.setText(emptyMessage);
    }

    private void hideEmptyState() {
        tvEmpty.setVisibility(View.GONE);
        rvTransactions.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        // TODO: Open transaction detail
        Toast.makeText(this, "Transaction details coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}