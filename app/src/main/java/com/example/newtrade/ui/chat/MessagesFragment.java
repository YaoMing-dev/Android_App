// app/src/main/java/com/example/newtrade/ui/chat/MessagesFragment.java
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
import com.example.newtrade.websocket.ChatWebSocketClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagesFragment extends Fragment implements ChatWebSocketClient.WebSocketListener {

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

    // 🔥 FIX: Add WebSocket support
    private ChatWebSocketClient webSocketClient;
    private Long currentUserId;
    private boolean isWebSocketInitialized = false;

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

        // 🔥 FIX: Initialize WebSocket if user is logged in
        initWebSocketIfNeeded();

        loadConversations();

        Log.d(TAG, "MessagesFragment created successfully with WebSocket");
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
        currentUserId = getCurrentUserId();
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

    // 🔥 FIX: WebSocket Implementation
    private void initWebSocketIfNeeded() {
        if (currentUserId == null || currentUserId <= 0) {
            Log.w(TAG, "❌ Cannot initialize WebSocket - user not logged in");
            return;
        }

        if (isWebSocketInitialized) {
            Log.d(TAG, "WebSocket already initialized");
            return;
        }

        try {
            // 🔥 FIX: Use correct WebSocket endpoint from backend
            String wsUrl = "ws://10.0.2.2:8080/ws-native";
            URI serverUri = URI.create(wsUrl);

            webSocketClient = new ChatWebSocketClient(serverUri, this);
            connectWebSocket();
            isWebSocketInitialized = true;

            Log.d(TAG, "✅ WebSocket initialized for user: " + currentUserId);
            Log.d(TAG, "MessagesFragment created successfully with WebSocket");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize WebSocket", e);
        }
    }

    private void connectWebSocket() {
        if (webSocketClient != null && !webSocketClient.isOpen()) {
            try {
                Log.d(TAG, "🔌 Connecting WebSocket...");
                webSocketClient.connect();
            } catch (IllegalStateException e) {
                Log.e(TAG, "❌ Failed to connect WebSocket", e);
                // Create new WebSocket client if the old one is not reusable
                initWebSocketIfNeeded();
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to connect WebSocket", e);
            }
        }
    }

    private void loadConversations() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);

        if (currentUserId == null || currentUserId <= 0) {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            showEmptyState();
            return;
        }

        // 🔥 FIX: Create mock conversations for now since API might not be ready
        createMockConversations();
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

        // TODO: Uncomment when conversations API is ready
        /*
        ApiClient.getApiService().getConversations().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
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
                    createMockConversations();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                Log.e(TAG, "❌ Conversations API error", t);
                createMockConversations();
            }
        });
        */
    }

    private void createMockConversations() {
        conversations.clear();

        // Mock conversation 1
        Conversation mockConversation1 = new Conversation();
        mockConversation1.setId(1L);
        mockConversation1.setOtherUserName("John Doe");
        mockConversation1.setLastMessage("Hello, is this item still available?");
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

            // TODO: Implement ChatActivity
            Toast.makeText(getContext(),
                    "Opening chat for conversation: " + conversation.getId(),
                    Toast.LENGTH_SHORT).show();

            /*
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("conversation_id", conversation.getId());
            intent.putExtra("other_user_name", conversation.getOtherUserName());
            intent.putExtra("product_title", conversation.getProductTitle());
            startActivity(intent);
            */

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
            Log.e(TAG, "❌ Failed to get current user ID", e);
            return null;
        }
    }

    // 🔥 FIX: WebSocket Listener Implementation
    @Override
    public void onMessageReceived(String message) {
        Log.d(TAG, "📨 WebSocket message received: " + message);

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // TODO: Parse message and update conversations
                loadConversations();
            });
        }
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "✅ WebSocket connected");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // TODO: Show connected status if needed
            });
        }
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "❌ WebSocket disconnected");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // TODO: Show disconnected status
                // 🔥 FIX: Reconnect after delay instead of immediately creating new WebSocket
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (webSocketClient != null && !webSocketClient.isOpen()) {
                        connectWebSocket();
                    }
                }, 3000);
            });
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "❌ WebSocket error: " + error);

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // TODO: Show error status if needed
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadConversations();

        // 🔥 FIX: Reconnect WebSocket if needed
        if (currentUserId != null && currentUserId > 0) {
            initWebSocketIfNeeded();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 🔥 FIX: Clean up WebSocket
        if (webSocketClient != null) {
            try {
                webSocketClient.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing WebSocket", e);
            }
            webSocketClient = null;
            isWebSocketInitialized = false;
        }

        Log.d(TAG, "MessagesFragment destroyed");
    }
}