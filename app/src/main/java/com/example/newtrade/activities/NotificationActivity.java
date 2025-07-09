// app/src/main/java/com/example/newtrade/activities/NotificationActivity.java
package com.example.newtrade.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.NotificationAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.NotificationService;
import com.example.newtrade.models.NotificationResponse;
import com.example.newtrade.models.PagedResponse;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.NotificationResponseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity implements
        NotificationAdapter.OnNotificationClickListener {

    private static final String TAG = "NotificationActivity";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private NotificationAdapter adapter;
    private NotificationService notificationService;

    private List<NotificationResponse> notifications = new ArrayList<>();
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();

        // Initialize API service
        notificationService = ApiClient.getNotificationService();

        loadNotifications(0);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notifications");
        }

        recyclerView = findViewById(R.id.recycler_notifications);
        swipeRefresh = findViewById(R.id.swipe_refresh);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notifications, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Pagination scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && hasMoreData && !isLoading) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                        loadNotifications(currentPage + 1);
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 0;
            hasMoreData = true;
            notifications.clear();
            loadNotifications(0);
        });
    }

    private void loadNotifications(int page) {
        if (isLoading) return;

        isLoading = true;
        if (page == 0) {
            swipeRefresh.setRefreshing(true);
        }

        // ✅ UPDATED: Sử dụng Map<String, Object> response
        Call<StandardResponse<Map<String, Object>>> call =
                notificationService.getNotifications(page, Constants.DEFAULT_PAGE_SIZE);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                isLoading = false;
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        Map<String, Object> responseData = standardResponse.getData();

                        // ✅ UPDATED: Parse Map to PagedResponse
                        PagedResponse<NotificationResponse> pagedResponse =
                                NotificationResponseHelper.parsePagedResponse(responseData);

                        if (page == 0) {
                            notifications.clear();
                        }

                        // Add new content
                        if (pagedResponse.hasContent()) {
                            notifications.addAll(pagedResponse.getContent());
                        }

                        // Update pagination state
                        currentPage = pagedResponse.getPage();
                        hasMoreData = pagedResponse.hasNext();

                        adapter.notifyDataSetChanged();

                        Log.d(TAG, "Loaded " + pagedResponse.getNumberOfElements() + " notifications");
                    } else {
                        Toast.makeText(NotificationActivity.this,
                                standardResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NotificationActivity.this,
                            "Failed to load notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);

                Log.e(TAG, "Error loading notifications", t);
                Toast.makeText(NotificationActivity.this,
                        "Error loading notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNotificationClick(NotificationResponse notification) {
        // Mark as read
        if (notification.isUnread()) {
            markAsRead(notification);
        }

        // Handle deep linking
        handleNotificationDeepLink(notification);
    }

    @Override
    public void onMarkAsReadClick(NotificationResponse notification) {
        markAsRead(notification);
    }

    private void markAsRead(NotificationResponse notification) {
        // ✅ UPDATED: Sử dụng String response
        Call<StandardResponse<String>> call = notificationService.markAsRead(notification.getId());

        call.enqueue(new Callback<StandardResponse<String>>() {
            @Override
            public void onResponse(Call<StandardResponse<String>> call,
                                   Response<StandardResponse<String>> response) {
                if (response.isSuccessful()) {
                    notification.setIsRead(true);
                    adapter.notifyDataSetChanged();

                    // Update badge count
                    updateUnreadCount();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                Log.e(TAG, "Error marking notification as read", t);
            }
        });
    }

    private void handleNotificationDeepLink(NotificationResponse notification) {
        // Handle deep linking based on notification type
        switch (notification.getType()) {
            case MESSAGE:
                // Navigate to chat screen
                // TODO: Implement navigation
                break;

            case OFFER:
                // Navigate to offer details
                // TODO: Implement navigation
                break;

            case TRANSACTION:
                // Navigate to transaction/listing details
                // TODO: Implement navigation
                break;

            case REVIEW:
                // Navigate to review details
                // TODO: Implement navigation
                break;

            case GENERAL:
                // Handle general notifications
                break;
        }
    }

    private void updateUnreadCount() {
        // ✅ UPDATED: Sử dụng Map<String, Object> response
        Call<StandardResponse<Map<String, Object>>> call = notificationService.getUnreadCount();

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        Map<String, Object> data = standardResponse.getData();

                        // ✅ UPDATED: Parse unread count from Map
                        long unreadCount = NotificationResponseHelper.parseUnreadCount(data);

                        // Update UI badge
                        // TODO: Update badge in UI
                        Log.d(TAG, "Unread count: " + unreadCount);
                    }
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Error getting unread count", t);
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