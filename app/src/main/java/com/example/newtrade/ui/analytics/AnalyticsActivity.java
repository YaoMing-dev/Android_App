package com.example.newtrade.ui.analytics;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.TransactionAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsActivity extends AppCompatActivity {

    private static final String TAG = "AnalyticsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private TextView tvTotalSales, tvTotalEarnings, tvActiveListings, tvTotalViews;
    private RecyclerView rvTransactions;
    private LinearLayout llEmptyState;

    // Data
    private TransactionAdapter transactionAdapter;
    private List<Map<String, Object>> transactions = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        prefsManager = SharedPrefsManager.getInstance(this);

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        loadAnalyticsData();

        Log.d(TAG, "AnalyticsActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        tvTotalSales = findViewById(R.id.tv_total_sales);
        tvTotalEarnings = findViewById(R.id.tv_total_earnings);
        tvActiveListings = findViewById(R.id.tv_active_listings);
        tvTotalViews = findViewById(R.id.tv_total_views);
        rvTransactions = findViewById(R.id.rv_transactions);
        llEmptyState = findViewById(R.id.ll_empty_state);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "Sales Analytics");
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Sales History"));
        tabLayout.addTab(tabLayout.newTab().setText("Purchase History"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadTransactionHistory(tab.getPosition() == 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTransactions.setLayoutManager(layoutManager);

        transactionAdapter = new TransactionAdapter(transactions);
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void loadAnalyticsData() {
        // Load analytics summary
        loadTransactionHistory(true); // Start with sales history

        // Set dummy data for now - would come from API
        tvTotalSales.setText("12");
        tvTotalEarnings.setText("$2,450");
        tvActiveListings.setText("5");
        tvTotalViews.setText("145");
    }

    private void loadTransactionHistory(boolean isSalesHistory) {
        Log.d(TAG, "Loading " + (isSalesHistory ? "sales" : "purchase") + " history");

        ApiClient.getApiService().getUserTransactions()
            .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                       @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            List<Map<String, Object>> transactionList = apiResponse.getData();

                            transactions.clear();
                            if (transactionList != null) {
                                transactions.addAll(transactionList);
                            }

                            transactionAdapter.notifyDataSetChanged();
                            updateEmptyState();

                            Log.d(TAG, "✅ Transaction history loaded: " + transactions.size());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failed to load transaction history", t);
                    updateEmptyState();
                }
            });
    }

    private void updateEmptyState() {
        if (transactions.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }
    }
}
