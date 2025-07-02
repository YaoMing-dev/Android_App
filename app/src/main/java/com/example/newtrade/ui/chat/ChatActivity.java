// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
package com.example.newtrade.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.Message;
import com.example.newtrade.adapters.MessageAdapter;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.websocket.ChatWebSocketManager;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity implements ChatWebSocketManager.ChatListener {

    private static final String TAG = "ChatActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvTypingIndicator;

    // Data
    private SharedPrefsManager prefsManager;
    private ChatWebSocketManager chatWebSocketManager;
    private MessageAdapter messageAdapter;
    private List<Message> messages;

    // Chat data
    private Long conversationId;
    private String otherUserName;
    private String productTitle;
    private Long currentUserId;
    private Long productId;

    // Typing indicator
    private Handler typingHandler = new Handler();
    private Runnable stopTypingRunnable;
    private boolean isTyping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        getIntentData();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        initWebSocket();
        loadOrCreateConversation();

        Log.d(TAG, "✅ ChatActivity created for conversation: " + conversationId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        tvTypingIndicator = findViewById(R.id.tv_typing_indicator);

        prefsManager = SharedPrefsManager.getInstance(this);
        currentUserId = prefsManager.getUserId();
        messages = new ArrayList<>();
    }

    private void getIntentData() {
        conversationId = getIntent().getLongExtra("conversation_id", -1);
        otherUserName = getIntent().getStringExtra("other_user_name");
        productTitle = getIntent().getStringExtra("product_title");
        productId = getIntent().getLongExtra("product_id", -1);

        // ✅ NEW: If no conversationId but has productId, we'll create conversation
        if (conversationId == -1 && productId != -1) {
            Log.d(TAG, "No conversation ID, will create for product: " + productId);
        }

        Log.d(TAG, "Chat data - ConversationID: " + conversationId + ", ProductID: " + productId + ", Other user: " + otherUserName);
    }

    private void setupToolbar() {
        String title = otherUserName != null ? otherUserName : "Chat";
        String subtitle = productTitle != null ? productTitle : "";

        NavigationUtils.setupToolbarWithBackButton(this, toolbar, title);
        toolbar.setSubtitle(subtitle);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, messages, currentUserId);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        // Typing indicator
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleTyping();
            }

            @Override
            public void afterTextChanged(Editable s) {
                btnSend.setEnabled(!s.toString().trim().isEmpty());
            }
        });
    }

    private void initWebSocket() {
        // ✅ ENABLE WebSocket with fixed backend
        chatWebSocketManager = ChatWebSocketManager.getInstance();
        chatWebSocketManager.addChatListener(this);

        // Connect if not already connected
        if (currentUserId != null && currentUserId > 0) {
            if (!chatWebSocketManager.isConnected()) {
                chatWebSocketManager.connect(currentUserId);
            } else {
                // Already connected, join conversation immediately if we have one
                if (conversationId != null && conversationId > 0) {
                    joinConversation();
                }
            }
        } else {
            Toast.makeText(this, "Please login to chat", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void joinConversation() {
        if (conversationId != null && conversationId > 0) {
            chatWebSocketManager.joinConversation(conversationId);
        }
    }

    // ✅ NEW: Load existing conversation or create new one
    private void loadOrCreateConversation() {
        if (conversationId != null && conversationId > 0) {
            // Load existing conversation
            loadMessages();
        } else if (productId != null && productId > 0) {
            // Create new conversation for product
            createConversation();
        } else {
            // Fallback to mock messages
            addMockMessages();
        }
    }

    // ✅ NEW: Create conversation via API
    private void createConversation() {
        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("productId", productId);

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.findOrCreateConversation(conversationData);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        Map<String, Object> conversationResponse = standardResponse.getData();
                        conversationId = ((Number) conversationResponse.get("id")).longValue();

                        Log.d(TAG, "✅ Conversation created/found: " + conversationId);

                        // Join WebSocket conversation
                        if (chatWebSocketManager.isConnected()) {
                            joinConversation();
                        }

                        // Load messages
                        loadMessages();

                    } else {
                        Log.e(TAG, "Failed to create conversation: " + standardResponse.getMessage());
                        addMockMessages(); // Fallback
                    }
                } else {
                    Log.e(TAG, "Failed to create conversation: HTTP " + response.code());
                    addMockMessages(); // Fallback
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Error creating conversation", t);
                addMockMessages(); // Fallback
            }
        });
    }

    // ✅ NEW: Load messages from API
    private void loadMessages() {
        if (conversationId == null || conversationId <= 0) {
            addMockMessages();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.getConversationMessages(conversationId, 0, 50);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        Map<String, Object> pageData = standardResponse.getData();
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> messageList = (List<Map<String, Object>>) pageData.get("content");

                        if (messageList != null) {
                            messages.clear();
                            for (Map<String, Object> messageData : messageList) {
                                Message message = parseMessageFromApi(messageData);
                                if (message != null) {
                                    messages.add(message);
                                }
                            }

                            messageAdapter.notifyDataSetChanged();
                            scrollToBottom();

                            Log.d(TAG, "✅ Loaded " + messages.size() + " messages");
                        }

                    } else {
                        Log.e(TAG, "Failed to load messages: " + standardResponse.getMessage());
                        addMockMessages(); // Fallback
                    }
                } else {
                    Log.e(TAG, "Failed to load messages: HTTP " + response.code());
                    addMockMessages(); // Fallback
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Error loading messages", t);
                addMockMessages(); // Fallback
            }
        });
    }

    // ✅ NEW: Parse message from API response
    private Message parseMessageFromApi(Map<String, Object> messageData) {
        try {
            Message message = new Message();

            if (messageData.get("id") != null) {
                message.setId(((Number) messageData.get("id")).longValue());
            }
            if (messageData.get("senderId") != null) {
                message.setSenderId(((Number) messageData.get("senderId")).longValue());
            }
            if (messageData.get("content") != null) {
                message.setContent(messageData.get("content").toString());
            }
            if (messageData.get("messageType") != null) {
                message.setMessageType(messageData.get("messageType").toString());
            }
            if (messageData.get("createdAt") != null) {
                message.setCreatedAt(formatTimestamp(messageData.get("createdAt").toString()));
            }

            return message;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing message from API", e);
            return null;
        }
    }

    private void addMockMessages() {
        // Mock messages for testing
        messages.clear();

        Message msg1 = new Message();
        msg1.setId(1L);
        msg1.setSenderId(2L); // Other user
        msg1.setContent("Hello! Is this item still available?");
        msg1.setCreatedAt("10:30 AM");
        msg1.setMessageType("TEXT");
        messages.add(msg1);

        Message msg2 = new Message();
        msg2.setId(2L);
        msg2.setSenderId(currentUserId); // Current user
        msg2.setContent("Yes, it's still available! Would you like to know more details?");
        msg2.setCreatedAt("10:32 AM");
        msg2.setMessageType("TEXT");
        messages.add(msg2);

        messageAdapter.notifyDataSetChanged();
        scrollToBottom();

        Log.d(TAG, "✅ Mock messages loaded");
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        // Add message to UI immediately for better UX
        Message newMessage = new Message();
        newMessage.setId(System.currentTimeMillis()); // Temporary ID
        newMessage.setSenderId(currentUserId);
        newMessage.setContent(messageText);
        newMessage.setCreatedAt("Sending...");
        newMessage.setMessageType("TEXT");

        messages.add(newMessage);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        // Clear input
        etMessage.setText("");

        // ✅ Send via WebSocket AND API
        sendMessageViaWebSocket(messageText);
        sendMessageViaApi(messageText);

        Log.d(TAG, "📤 Sent message: " + messageText);
    }

    // ✅ NEW: Send via WebSocket (real-time)
    private void sendMessageViaWebSocket(String messageText) {
        if (chatWebSocketManager != null && chatWebSocketManager.isConnected()) {
            chatWebSocketManager.sendChatMessage(messageText);
        }
    }

    // ✅ NEW: Send via API (persistence)
    private void sendMessageViaApi(String messageText) {
        if (conversationId == null || conversationId <= 0) {
            Log.w(TAG, "No conversation ID, cannot send via API");
            return;
        }

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("conversationId", conversationId);
        messageData.put("content", messageText);
        messageData.put("messageType", "TEXT");

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.sendMessage(messageData);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        // Update last message status to "Sent"
                        updateLastMessageStatus("Sent");
                        Log.d(TAG, "✅ Message sent via API");
                    } else {
                        updateLastMessageStatus("Failed");
                        Log.e(TAG, "Failed to send message via API: " + standardResponse.getMessage());
                    }
                } else {
                    updateLastMessageStatus("Failed");
                    Log.e(TAG, "Failed to send message via API: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                updateLastMessageStatus("Failed");
                Log.e(TAG, "Error sending message via API", t);
            }
        });
    }

    private void updateLastMessageStatus(String status) {
        if (!messages.isEmpty()) {
            Message lastMessage = messages.get(messages.size() - 1);
            lastMessage.setCreatedAt(status);
            messageAdapter.notifyItemChanged(messages.size() - 1);
        }
    }

    private void handleTyping() {
        if (!isTyping) {
            isTyping = true;
            // TODO: Send typing indicator via WebSocket
        }

        // Cancel previous stop typing task
        if (stopTypingRunnable != null) {
            typingHandler.removeCallbacks(stopTypingRunnable);
        }

        // Schedule stop typing
        stopTypingRunnable = () -> {
            if (isTyping) {
                isTyping = false;
                // TODO: Send stop typing via WebSocket
            }
        };
        typingHandler.postDelayed(stopTypingRunnable, 1000); // Stop typing after 1 second
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            rvMessages.smoothScrollToPosition(messages.size() - 1);
        }
    }

    // ===== WEBSOCKET LISTENER IMPLEMENTATIONS =====

    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            Log.d(TAG, "✅ Chat WebSocket connected");
            if (conversationId != null && conversationId > 0) {
                joinConversation();
            }
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            Log.d(TAG, "❌ Chat WebSocket disconnected");
            // Don't show toast, might reconnect automatically
        });
    }

    @Override
    public void onMessageReceived(JsonObject messageJson) {
        runOnUiThread(() -> {
            try {
                Log.d(TAG, "📨 New message received: " + messageJson.toString());

                // Parse message from JSON
                Message newMessage = new Message();
                newMessage.setId(messageJson.has("id") ? messageJson.get("id").getAsLong() : System.currentTimeMillis());
                newMessage.setSenderId(messageJson.get("senderId").getAsLong());
                newMessage.setContent(messageJson.get("content").getAsString());
                newMessage.setMessageType(messageJson.has("messageType") ? messageJson.get("messageType").getAsString() : "TEXT");
                newMessage.setCreatedAt(messageJson.has("timestamp") ?
                        formatTimestamp(messageJson.get("timestamp").getAsLong()) : "Now");

                // Only add if it's from someone else (avoid duplicates)
                if (!newMessage.getSenderId().equals(currentUserId)) {
                    messages.add(newMessage);
                    messageAdapter.notifyItemInserted(messages.size() - 1);
                    scrollToBottom();
                }

            } catch (Exception e) {
                Log.e(TAG, "Error parsing received message", e);
            }
        });
    }

    @Override
    public void onTypingIndicator(Long userId, boolean isTyping) {
        runOnUiThread(() -> {
            if (!userId.equals(currentUserId)) { // Not from current user
                if (isTyping) {
                    tvTypingIndicator.setText(otherUserName + " is typing...");
                    tvTypingIndicator.setVisibility(View.VISIBLE);
                } else {
                    tvTypingIndicator.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Log.e(TAG, "❌ WebSocket error: " + error);
            // Don't show toast for every error, might be temporary
        });
    }

    // ===== UTILITY METHODS - FIXED API LEVEL =====

    // ✅ FIXED: Use SimpleDateFormat instead of java.time
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // ✅ FIXED: Use SimpleDateFormat instead of java.time
    private String formatTimestamp(String timestamp) {
        try {
            // Parse ISO timestamp and convert to HH:mm
            SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date dateTime = inputFormatter.parse(timestamp);

            if (dateTime != null) {
                SimpleDateFormat outputFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return outputFormatter.format(dateTime);
            }

            return timestamp; // Return as-is if parsing fails
        } catch (ParseException e) {
            return timestamp; // Return as-is if parsing fails
        }
    }

    // ===== LIFECYCLE METHODS =====

    @Override
    protected void onResume() {
        super.onResume();
        if (chatWebSocketManager != null && chatWebSocketManager.isConnected() && conversationId != null) {
            joinConversation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (chatWebSocketManager != null) {
            chatWebSocketManager.leaveConversation();
        }

        // Stop typing indicator
        if (isTyping) {
            isTyping = false;
            // TODO: Send stop typing via WebSocket
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatWebSocketManager != null) {
            chatWebSocketManager.removeChatListener(this);
        }

        if (typingHandler != null && stopTypingRunnable != null) {
            typingHandler.removeCallbacks(stopTypingRunnable);
        }
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