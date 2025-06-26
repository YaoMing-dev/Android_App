// app/src/main/java/com/example/newtrade/ui/chat/MessagesFragment.java
// ✅ FIXED - Remove WebSocketListener and fix all errors
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    // ✅ FIX: Remove WebSocket related fields
    private Long currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initData();
        setupRecyclerView();
        setupListeners();
        loadConversations();

        Log.d(TAG, "✅ MessagesFragment created");
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        rvConversations = view.findViewById(R.id.rv_conversations);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        btnStartShopping = view.findViewById(R.id.btn_start_shopping);
    }

    private void initData() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        currentUserId = getCurrentUserId();

        Log.d(TAG, "Current user ID: " + currentUserId);
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(conversations, this::openChatActivity);
        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvConversations.setAdapter(conversationAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadConversations);

        if (btnStartShopping != null) {
            btnStartShopping.setOnClickListener(v -> {
                // Navigate to home or search
                Toast.makeText(requireContext(), "Let's start shopping!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadConversations() {
        swipeRefresh.setRefreshing(true);

        if (currentUserId == null) {
            Log.w(TAG, "⚠️ No user ID, loading mock conversations");
            loadMockConversations();
            return;
        }

        Log.d(TAG, "📋 Loading conversations for user: " + currentUserId);

        // For now, just load mock conversations
        loadMockConversations();

        // TODO: Uncomment when API is ready
        /*
        ApiClient.getApiService().getUserConversations(currentUserId)
                .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                         Response<StandardResponse<List<Map<String, Object>>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<Map<String, Object>> conversationData = response.body().getData();
                            updateConversationsFromData(conversationData);
                        } else {
                            Log.w(TAG, "⚠️ API response unsuccessful, loading mock conversations");
                            loadMockConversations();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load conversations", t);
                        loadMockConversations();
                    }
                });
        */
    }

    private void loadMockConversations() {
        conversations.clear();

        // Mock conversation 1
        Conversation mockConversation1 = new Conversation();
        mockConversation1.setId(1L);
        mockConversation1.setOtherUserName("John Doe");
        mockConversation1.setLastMessage("Hi! Is this still available?");
        mockConversation1.setLastMessageTime("2 hours ago");
        mockConversation1.setProductTitle("iPhone 13 Pro Max");
        conversations.add(mockConversation1);

        // Mock conversation 2
        Conversation mockConversation2 = new Conversation();
        mockConversation2.setId(2L);
        mockConversation2.setOtherUserName("Jane Smith");
        mockConversation2.setLastMessage("Thanks for the quick response!");
        mockConversation2.setLastMessageTime("1 day ago");
        mockConversation2.setProductTitle("MacBook Air M1");
        conversations.add(mockConversation2);

        // Mock conversation 3
        Conversation mockConversation3 = new Conversation();
        mockConversation3.setId(3L);
        mockConversation3.setOtherUserName("Mike Johnson");
        mockConversation3.setLastMessage("When can we meet?");
        mockConversation3.setLastMessageTime("3 days ago");
        mockConversation3.setProductTitle("Samsung Galaxy S23");
        conversations.add(mockConversation3);

        updateUI();
        Log.d(TAG, "✅ Mock conversations created: " + conversations.size());
    }

    private void updateConversationsFromData(@Nullable List<Map<String, Object>> conversationData) {
        conversations.clear();
        if (conversationData != null && !conversationData.isEmpty()) {
            for (Map<String, Object> data : conversationData) {
                Conversation conversation = new Conversation();
                if (data.get("id") instanceof Number) {
                    conversation.setId(((Number) data.get("id")).longValue());
                }
                conversation.setOtherUserName((String) data.get("otherUserName"));
                conversation.setLastMessage((String) data.get("lastMessage"));
                conversation.setLastMessageTime((String) data.get("lastMessageTime"));
                conversation.setProductTitle((String) data.get("productTitle"));
                conversations.add(conversation);
            }
            showConversations();
        } else {
            showEmptyState();
        }
        updateUI();
    }

    private void updateUI() {
        swipeRefresh.setRefreshing(false);

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
            Log.d(TAG, "✅ Opening chat for conversation: " + conversation.getId());

            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("conversation_id", conversation.getId());
            intent.putExtra("other_user_name", conversation.getOtherUserName());
            intent.putExtra("product_title", conversation.getProductTitle());
            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to open chat", e);
            Toast.makeText(getContext(), "Failed to open chat", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private Long getCurrentUserId() {
        try {
            Long userId = prefsManager.getUserId();
            return (userId != null && userId > 0) ? userId : null;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting user ID", e);
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh conversations when fragment becomes visible
        loadConversations();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🧹 MessagesFragment destroyed");
    }
}