// app/src/main/java/com/example/newtrade/ui/chat/ConversationListActivity.java
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ConversationAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Conversation;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationListActivity extends AppCompatActivity {

    private static final String TAG = "ConversationListActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvConversations;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout llEmptyState;

    // Data
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations;
    private SharedPrefsManager prefsManager;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        loadConversations();

        Log.d(TAG, "ConversationListActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvConversations = findViewById(R.id.rv_conversations);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        llEmptyState = findViewById(R.id.ll_empty_state);

        prefsManager = SharedPrefsManager.getInstance(this);
        conversations = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("All Conversations");
        }
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(this, conversations, this::openChat);
        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        rvConversations.setAdapter(conversationAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadConversations);
    }

    private void loadConversations() {
        if (isLoading) return;

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            finish();
            return;
        }

        isLoading = true;
        swipeRefresh.setRefreshing(true);

        Call<StandardResponse<Map<String, Object>>> call =
                ApiClient.getChatService().getConversations(userId, 0, 50);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Handle response
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Failed to load conversations", t);
            }
        });
    }

    private void updateUI() {
        if (conversations.isEmpty()) {
            llEmptyState.setVisibility(android.view.View.VISIBLE);
            rvConversations.setVisibility(android.view.View.GONE);
        } else {
            llEmptyState.setVisibility(android.view.View.GONE);
            rvConversations.setVisibility(android.view.View.VISIBLE);
            conversationAdapter.notifyDataSetChanged();
        }
    }

    private void openChat(Conversation conversation) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_CONVERSATION_ID, conversation.getId());
        startActivity(intent);
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