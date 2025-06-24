package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ConversationAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Conversation;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagesFragment extends Fragment {

    private static final String TAG = "MessagesFragment";

    // UI Components
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvConversations;
    private LinearLayout llEmptyState;
    private Button btnStartShopping;

    // Data
    private ConversationAdapter conversationAdapter;
    private final List<Conversation> conversations = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initUtils();
        setupRecyclerView();
        setupListeners();
        loadConversations();

        Log.d(TAG, "MessagesFragment created successfully");
    }

    private void initViews(View view) {
        try {
            swipeRefresh = view.findViewById(R.id.swipe_refresh);
            rvConversations = view.findViewById(R.id.rv_conversations);
            llEmptyState = view.findViewById(R.id.ll_empty_state);
            btnStartShopping = view.findViewById(R.id.btn_start_shopping);

            Log.d(TAG, "✅ MessagesFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some MessagesFragment views not found: " + e.getMessage());
        }
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        if (rvConversations != null) {
            conversationAdapter = new ConversationAdapter(conversations, this::openChatActivity);
            rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
            rvConversations.setAdapter(conversationAdapter);
        }
    }

    private void setupListeners() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::loadConversations);
        }

        if (btnStartShopping != null) {
            btnStartShopping.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Navigate to shop", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadConversations() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            showEmptyState();
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            return;
        }

        // Create mock conversations for testing
        createMockConversations();

        // TODO: Uncomment when API is ready
        /*
        ApiClient.getApiService().getUserConversations(userId).enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                 @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Map<String, Object>> conversationData = response.body().getData();
                    updateConversationsFromData(conversationData);
                    Log.d(TAG, "✅ Conversations loaded: " + conversationData.size());
                } else {
                    Log.e(TAG, "❌ Failed to load conversations");
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                Log.e(TAG, "❌ Conversations API error", t);
                showEmptyState();
            }
        });
        */
    }

    private void createMockConversations() {
        conversations.clear();

        Conversation mockConversation = new Conversation();
        mockConversation.setId(1L);
        mockConversation.setLastMessage("Hello, is this item still available?");

        conversations.add(mockConversation);
        updateUI();
    }

    private void updateConversationsFromData(@Nullable List<Map<String, Object>> conversationData) {
        conversations.clear();
        if (conversationData != null && !conversationData.isEmpty()) {
            for (Map<String, Object> data : conversationData) {
                Conversation conversation = new Conversation();
                // Map data to conversation object
                if (data.get("id") instanceof Number) {
                    conversation.setId(((Number) data.get("id")).longValue());
                }
                conversation.setLastMessage((String) data.get("lastMessage"));
                conversations.add(conversation);
            }
            showConversations();
        } else {
            showEmptyState();
        }
        updateUI();
    }

    private void updateUI() {
        if (conversationAdapter != null) {
            conversationAdapter.notifyDataSetChanged();
        }
        if (conversations.isEmpty()) {
            showEmptyState();
        } else {
            showConversations();
        }
    }

    private void showConversations() {
        if (rvConversations != null) rvConversations.setVisibility(View.VISIBLE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        if (rvConversations != null) rvConversations.setVisibility(View.GONE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
    }

    private void openChatActivity(Conversation conversation) {
        try {
            Toast.makeText(getContext(),
                    "Opening chat for conversation: " + conversation.getId(),
                    Toast.LENGTH_SHORT).show();

            // TODO: Implement ChatActivity later
            /*
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("conversation_id", conversation.getId());
            startActivity(intent);
            */

            Log.d(TAG, "✅ Opening chat for conversation: " + conversation.getId());
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to open chat", e);
            Toast.makeText(getContext(), "Failed to open chat", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private Long getCurrentUserId() {
        try {
            return prefsManager.getCurrentUserId();
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to get current user ID", e);
            return 1L; // Mock user ID for testing
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadConversations();
    }
}