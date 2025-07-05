// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
package com.example.newtrade.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.MessageAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Message;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.websocket.RealtimeWebSocketService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private static final int MESSAGES_PAGE_SIZE = 50;

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private LinearLayout llTypingIndicator;
    private TextView tvTypingIndicator;
    private TextView tvConnectionStatus;

    // Data
    private Long conversationId;
    private Long productId;
    private SharedPrefsManager prefsManager;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private boolean isLoading = false;
    private int currentPage = 0;
    private boolean hasMoreMessages = true;

    // WebSocket
    private RealtimeWebSocketService webSocketService;
    private BroadcastReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize
        prefsManager = SharedPrefsManager.getInstance(this);

        // Get data from intent
        getIntentData();

        // Initialize views
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupWebSocket();
        setupBroadcastReceiver();

        // Load messages
        loadMessages();

        Log.d(TAG, "✅ ChatActivity created for conversation: " + conversationId);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        conversationId = intent.getLongExtra(Constants.EXTRA_CONVERSATION_ID, -1L);
        productId = intent.getLongExtra(Constants.EXTRA_PRODUCT_ID, -1L);

        if (conversationId == -1L) {
            Log.e(TAG, "❌ Conversation ID not provided");
            finish();
            return;
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        llTypingIndicator = findViewById(R.id.ll_typing_indicator);
        tvTypingIndicator = findViewById(R.id.tv_typing_indicator);
        tvConnectionStatus = findViewById(R.id.tv_connection_status);

        // Initially disable send button
        btnSend.setEnabled(false);

        // Hide typing indicator
        llTypingIndicator.setVisibility(View.GONE);

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chat");
        }
    }

    private void setupRecyclerView() {
        Long currentUserId = prefsManager.getUserId();
        messageAdapter = new MessageAdapter(messages, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);

        // Add scroll listener for pagination
        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (manager != null && !isLoading && hasMoreMessages) {
                    int firstVisibleItem = manager.findFirstVisibleItemPosition();
                    if (firstVisibleItem <= 2) { // Load more when near top
                        loadMoreMessages();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        // Message input text watcher
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                btnSend.setEnabled(hasText);

                // Send typing indicator
                if (hasText && webSocketService != null) {
                    sendTypingIndicator(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Send button click
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupWebSocket() {
        // Start WebSocket service
        Intent serviceIntent = new Intent(this, RealtimeWebSocketService.class);
        startService(serviceIntent);

        // Bind to service
        bindService(serviceIntent, new android.content.ServiceConnection() {
            @Override
            public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {
                RealtimeWebSocketService.LocalBinder binder = (RealtimeWebSocketService.LocalBinder) service;
                webSocketService = binder.getService();

                // Add listener for this activity
                webSocketService.addListener(TAG, new RealtimeWebSocketService.WebSocketListener() {
                    @Override
                    public void onMessageReceived(String type, JsonObject data) {
                        runOnUiThread(() -> handleWebSocketMessage(type, data));
                    }

                    @Override
                    public void onConnectionStatusChanged(boolean connected) {
                        runOnUiThread(() -> updateConnectionStatus(connected));
                    }
                });

                // Join conversation
                webSocketService.joinConversation(conversationId);

                Log.d(TAG, "✅ WebSocket service connected");
            }

            @Override
            public void onServiceDisconnected(android.content.ComponentName name) {
                webSocketService = null;
                Log.d(TAG, "WebSocket service disconnected");
            }
        }, Context.BIND_AUTO_CREATE);
    }

    private void setupBroadcastReceiver() {
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("NEW_MESSAGE_RECEIVED".equals(intent.getAction())) {
                    String messageData = intent.getStringExtra("message_data");
                    handleNewMessageBroadcast(messageData);
                }
            }
        };

        IntentFilter filter = new IntentFilter("NEW_MESSAGE_RECEIVED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(messageReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(messageReceiver, filter);
        }
    }

    private void loadMessages() {
        if (isLoading) return;

        isLoading = true;
        currentPage = 0;

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "🔍 Loading messages for conversation: " + conversationId);

        ApiClient.getChatService().getMessages(userId, conversationId, currentPage, MESSAGES_PAGE_SIZE)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        isLoading = false;

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                handleMessagesResponse(apiResponse.getData(), false);

                                // Mark messages as read
                                markMessagesAsRead();

                                Log.d(TAG, "✅ Messages loaded successfully");
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to load messages");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        isLoading = false;
                        Log.e(TAG, "❌ Failed to load messages", t);
                        showError("Network error. Please try again.");
                    }
                });
    }

    private void loadMoreMessages() {
        if (isLoading || !hasMoreMessages) return;

        isLoading = true;
        currentPage++;

        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        ApiClient.getChatService().getMessages(userId, conversationId, currentPage, MESSAGES_PAGE_SIZE)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        isLoading = false;

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                handleMessagesResponse(apiResponse.getData(), true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        isLoading = false;
                        Log.e(TAG, "❌ Failed to load more messages", t);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void handleMessagesResponse(Map<String, Object> data, boolean isLoadMore) {
        try {
            List<Map<String, Object>> messageList = (List<Map<String, Object>>) data.get("content");
            Boolean hasMore = (Boolean) data.get("hasNext");

            if (messageList != null) {
                List<Message> newMessages = new ArrayList<>();

                for (Map<String, Object> messageData : messageList) {
                    Message message = Message.fromMap(messageData);
                    newMessages.add(message);
                }

                if (isLoadMore) {
                    // Add to beginning for older messages
                    messages.addAll(0, newMessages);
                    messageAdapter.notifyItemRangeInserted(0, newMessages.size());
                } else {
                    // Replace all messages
                    messages.clear();
                    messages.addAll(newMessages);
                    messageAdapter.notifyDataSetChanged();

                    // Scroll to bottom
                    if (!messages.isEmpty()) {
                        rvMessages.scrollToPosition(messages.size() - 1);
                    }
                }

                hasMoreMessages = hasMore != null && hasMore;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing messages", e);
        }
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        // Clear input immediately
        etMessage.setText("");
        btnSend.setEnabled(false);

        // Send typing indicator stop
        sendTypingIndicator(false);

        Log.d(TAG, "📤 Sending message: " + messageText);

        // Send via WebSocket if connected
        if (webSocketService != null && webSocketService.isConnected()) {
            webSocketService.sendChatMessage(conversationId, messageText);
        } else {
            // Fallback to API call
            sendMessageViaAPI(messageText);
        }
    }

    private void sendMessageViaAPI(String messageText) {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        Map<String, Object> messageRequest = new HashMap<>();
        messageRequest.put("conversationId", conversationId);
        messageRequest.put("messageText", messageText);
        messageRequest.put("messageType", "TEXT");

        ApiClient.getChatService().sendMessage(userId, messageRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                // Message sent successfully
                                Log.d(TAG, "✅ Message sent via API");
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to send message");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to send message", t);
                        showError("Network error. Please try again.");
                    }
                });
    }

    private void sendTypingIndicator(boolean isTyping) {
        if (webSocketService != null && webSocketService.isConnected()) {
            Map<String, Object> data = new HashMap<>();
            data.put("conversationId", conversationId);
            data.put("isTyping", isTyping);
            webSocketService.sendMessage(isTyping ? "TYPING" : "STOP_TYPING", data);
        }
    }

    private void markMessagesAsRead() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        ApiClient.getChatService().markMessagesAsRead(userId, conversationId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "✅ Messages marked as read");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.d(TAG, "Failed to mark messages as read");
                    }
                });
    }

    private void handleWebSocketMessage(String type, JsonObject data) {
        switch (type) {
            case Constants.WS_MESSAGE_TYPE_NEW_MESSAGE:
                handleNewMessage(data);
                break;
            case Constants.WS_MESSAGE_TYPE_TYPING:
                handleTypingIndicator(data, true);
                break;
            case "STOP_TYPING":
                handleTypingIndicator(data, false);
                break;
        }
    }

    private void handleNewMessage(JsonObject data) {
        try {
            Long msgConversationId = data.get("conversationId").getAsLong();

            // Only handle messages for this conversation
            if (msgConversationId.equals(conversationId)) {
                // Convert JsonObject to Message
                Message message = Message.fromJsonObject(data);

                // Add to messages list
                messages.add(message);
                messageAdapter.notifyItemInserted(messages.size() - 1);

                // Scroll to bottom
                rvMessages.scrollToPosition(messages.size() - 1);

                // Mark as read
                markMessagesAsRead();

                Log.d(TAG, "✅ New message received via WebSocket");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling new message", e);
        }
    }

    private void handleNewMessageBroadcast(String messageData) {
        try {
            Gson gson = new Gson();
            JsonObject data = gson.fromJson(messageData, JsonObject.class);
            handleNewMessage(data);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling message broadcast", e);
        }
    }

    private void handleTypingIndicator(JsonObject data, boolean isTyping) {
        try {
            Long msgConversationId = data.get("conversationId").getAsLong();
            Long senderId = data.get("senderId").getAsLong();
            Long currentUserId = prefsManager.getUserId();

            // Only show typing indicator for this conversation and from other users
            if (msgConversationId.equals(conversationId) && !senderId.equals(currentUserId)) {
                if (isTyping) {
                    tvTypingIndicator.setText("Other user is typing...");
                    llTypingIndicator.setVisibility(View.VISIBLE);
                } else {
                    llTypingIndicator.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling typing indicator", e);
        }
    }

    private void updateConnectionStatus(boolean connected) {
        if (connected) {
            tvConnectionStatus.setText("Connected");
            tvConnectionStatus.setTextColor(getResources().getColor(R.color.success_color));
        } else {
            tvConnectionStatus.setText("Reconnecting...");
            tvConnectionStatus.setTextColor(getResources().getColor(R.color.warning_color));
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.w(TAG, "Error: " + message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up WebSocket
        if (webSocketService != null) {
            webSocketService.removeListener(TAG);
            webSocketService.leaveConversation(conversationId);
        }

        // Unregister broadcast receiver
        if (messageReceiver != null) {
            try {
                unregisterReceiver(messageReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver was not registered
            }
        }

        Log.d(TAG, "ChatActivity destroyed");
    }
}