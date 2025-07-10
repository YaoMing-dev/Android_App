// app/src/main/java/com/example/newtrade/ui/payment/PaymentHistoryActivity.java
package com.example.newtrade.ui.payment;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.PaymentHistoryAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.PaymentService;
import com.example.newtrade.models.PagedResponse;
import com.example.newtrade.models.Payment;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentHistoryActivity extends AppCompatActivity implements PaymentHistoryAdapter.OnPaymentClickListener {

    private static final String TAG = "PaymentHistoryActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    // Data
    private PaymentHistoryAdapter adapter;
    private List<Payment> payments = new ArrayList<>();
    private PaymentService paymentService;
    private SharedPrefsManager prefsManager;

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private final int pageSize = Constants.DEFAULT_PAGE_SIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        initializeComponents();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        loadPaymentHistory(0);

        Log.d(TAG, "PaymentHistoryActivity created");
    }

    private void initializeComponents() {
        prefsManager = new SharedPrefsManager(this);
        paymentService = ApiClient.getPaymentService();

        // UI components
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment History");
        }
    }

    private void setupRecyclerView() {
        adapter = new PaymentHistoryAdapter(this, payments, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) { // Scrolling down
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMoreData &&
                            (visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                            firstVisibleItemPosition >= 0) {
                        loadPaymentHistory(currentPage + 1);
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            hasMoreData = true;
            payments.clear();
            adapter.notifyDataSetChanged();
            loadPaymentHistory(0);
        });

        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.primary_variant,
                R.color.secondary
        );
    }

    private void loadPaymentHistory(int page) {
        if (isLoading) return;

        isLoading = true;
        currentPage = page;

        // Show progress bar only for first page
        if (page == 0) {
            showLoading(true);
        }

        String userId = String.valueOf(prefsManager.getUserId());

        Call<StandardResponse<PagedResponse<Payment>>> call =
                paymentService.getMyPayments(userId, page, pageSize);

        call.enqueue(new Callback<StandardResponse<PagedResponse<Payment>>>() {
            @Override
            public void onResponse(Call<StandardResponse<PagedResponse<Payment>>> call,
                                   Response<StandardResponse<PagedResponse<Payment>>> response) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<PagedResponse<Payment>> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        PagedResponse<Payment> pagedResponse = standardResponse.getData();
                        handlePaymentHistoryResponse(pagedResponse, page);
                    } else {
                        showError("Failed to load payment history: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Failed to load payment history");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<PagedResponse<Payment>>> call, Throwable t) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                Log.e(TAG, "Failed to load payment history", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void handlePaymentHistoryResponse(PagedResponse<Payment> pagedResponse, int page) {
        List<Payment> newPayments = pagedResponse.getContent();

        if (page == 0) {
            payments.clear();
        }

        if (newPayments != null && !newPayments.isEmpty()) {
            payments.addAll(newPayments);
            adapter.notifyDataSetChanged();
        }

        // Update pagination state
        hasMoreData = !pagedResponse.isLast();

        // Show/hide empty state
        updateEmptyState();

        Log.d(TAG, "Loaded " + (newPayments != null ? newPayments.size() : 0) +
                " payments, total: " + payments.size() +
                ", hasMore: " + hasMoreData);
    }

    private void updateEmptyState() {
        if (payments.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setText("No payment history found.\nMake your first purchase to see payment history here.");
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    public void onPaymentClick(Payment payment) {
        // Open payment details
        Intent intent = new Intent(this, PaymentDetailActivity.class);
        intent.putExtra(PaymentDetailActivity.EXTRA_PAYMENT_ID, payment.getId());
        intent.putExtra(PaymentDetailActivity.EXTRA_TRANSACTION_ID, payment.getTransactionId());
        startActivity(intent);
    }

    @Override
    public void onRefundClick(Payment payment) {
        // Show refund confirmation dialog
        showRefundConfirmationDialog(payment);
    }

    private void showRefundConfirmationDialog(Payment payment) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Request Refund");
        builder.setMessage("Are you sure you want to request a refund for this payment?\n\nAmount: " +
                payment.getDisplayAmount());

        builder.setPositiveButton("Request Refund", (dialog, which) -> {
            requestRefund(payment);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void requestRefund(Payment payment) {
        // For now, show toast. In real implementation, navigate to refund form
        Toast.makeText(this, "Refund request feature will be available soon", Toast.LENGTH_SHORT).show();

        // Future implementation:
        // Intent intent = new Intent(this, RefundRequestActivity.class);
        // intent.putExtra("payment_id", payment.getId());
        // startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (!payments.isEmpty()) {
            currentPage = 0;
            hasMoreData = true;
            payments.clear();
            adapter.notifyDataSetChanged();
            loadPaymentHistory(0);
        }
    }
}