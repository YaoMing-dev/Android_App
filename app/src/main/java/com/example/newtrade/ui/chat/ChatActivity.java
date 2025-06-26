// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
// ✅ REALTIME WebSocket Chat Activity
package com.example.newtrade.ui.chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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
import com.example.newtrade.adapters.MessageAdapter;
import com.example.newtrade.models.Message;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.websocket.RealtimeWebSocketService;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements
        RealtimeWebSocketService.WebSocketListener,
        RealtimeWebSocketService.ChatListener {

    private static final String TAG = "ChatActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvProductInfo;
    private TextView tvConnectionStatus;
    private TextView tvTypingIndicator;

    // Data
    private MessageAdapter messageAdapter;
    private final List<Message> messages = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    // Intent data
    private Long conversationId;
    private Long productId;
    private String productTitle;
    private Long sellerId;
    private Long currentUserId;
    private String otherUserName;

    // WebSocket Service
    private RealtimeWebSocketService webSocketService;
    private boolean isServiceBound = false;

    // Typing indicator
    private Handler typingHandler;
    private boolean isUserTyping = false;
    private static final long TYPING_TIMEOUT = 3000; // 3 seconds

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RealtimeWebSocketService.LocalBinder binder = (RealtimeWebSocketService.LocalBinder) service;
            webSocketService = binder.getService();
            isServiceBound = true;

            // Add listeners
            webSocketService.addWebSocketListener(ChatActivity.this);
            webSocketService.addChatListener(conversationId, ChatActivity.this);

            // Join conversation
            if (conversationId != null && conversationId > 0) {
                webSocketService.joinConversation(conversationId);
            }

            // Update connection status
            updateConnectionStatus(webSocketService.isConnected());

            Log.d(TAG, "✅ WebSocket service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketService = null;
            isServiceBound = false;
            updateConnectionStatus(false);
            Log.d(TAG, "❌ WebSocket service disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        // Start and bind to WebSocket service
        startAndBindWebSocketService();

        // Load existing messages
        loadMessages();

        Log.d(TAG, "✅ ChatActivity created");
    }

    private void getIntentData() {
        Intent intent = getIntent();
        conversationId = intent.getLongExtra("conversation_id", -1);
        productId = intent.getLongExtra("product_id", -1);
        productTitle = intent.getStringExtra("product_title");
        sellerId = intent.getLongExtra("seller_id", -1);
        otherUserName = intent.getStringExtra("other_user_name");

        prefsManager = SharedPrefsManager.getInstance(this);
        currentUserId = prefsManager.getUserId();

        Log.d(TAG, "💬 Chat - Conversation: " + conversationId + ", Product: " + productTitle);
        Log.d(TAG, "👤 Current User: " + currentUserId + ", Other: " + otherUserName);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        tvProductInfo = findViewById(R.id.tv_product_info);

        // Optional UI elements
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        tvTypingIndicator = findViewById(R.id.tv_typing_indicator);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar,
                otherUserName != null ? otherUserName : "Chat");

        if (getSupportActionBar() != null && productTitle != null) {
            getSupportActionBar().setSubtitle(productTitle);
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messages, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        // Send button
        btnSend.setOnClickListener(v -> sendMessage());

        // Text change listener for typing indicator
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                btnSend.setEnabled(hasText);

                handleTypingIndicator(hasText);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Product info click
        if (tvProductInfo != null) {
            tvProductInfo.setOnClickListener(v -> openProductDetail());
        }
    }

    private void startAndBindWebSocketService() {
        Intent serviceIntent = new Intent(this, RealtimeWebSocketService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void loadMessages() {
        // Load mock messages for now
        loadMockMessages();

        // TODO: Load real messages from API
        // loadMessagesFromAPI();
    }

    private void loadMockMessages() {
        messages.clear();

        // Mock message 1 (from other user)
        Message msg1 = new Message();
        msg1.setId(1L);
        msg1.setConversationId(conversationId);
        msg1.setSenderId(sellerId);
        msg1.setSenderName(otherUserName);
        msg1.setContent("Hi! Is this item still available?");
        msg1.setMessageType("TEXT");
        msg1.setTimestamp("10:30 AM");
        messages.add(msg1);

        // Mock message 2 (from current user)
        Message msg2 = new Message();
        msg2.setId(2L);
        msg2.setConversationId(conversationId);
        msg2.setSenderId(currentUserId);
        msg2.setSenderName("You");
        msg2.setContent("Yes, it's still available! Would you like more details?");
        msg2.setMessageType("TEXT");
        msg2.setTimestamp("10:35 AM");
        messages.add(msg2);

        messageAdapter.notifyDataSetChanged();
        scrollToBottom();

        Log.d(TAG, "✅ Mock messages loaded: " + messages.size());
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        Log.d(TAG, "📤 Sending message: " + messageText);

        // Clear input
        etMessage.setText("");

        // Create message object for immediate UI update
        Message newMessage = new Message();
        newMessage.setId(System.currentTimeMillis()); // Temporary ID
        newMessage.setConversationId(conversationId);
        newMessage.setSenderId(currentUserId);
        newMessage.setSenderName("You");
        newMessage.setContent(messageText);
        newMessage.setMessageType("TEXT");
        newMessage.setTimestamp("Sending...");

        // Add to UI immediately
        messages.add(newMessage);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        // Send via WebSocket
        if (webSocketService != null && webSocketService.isConnected()) {
            webSocketService.sendChatMessage(conversationId, messageText, "TEXT");
        } else {
            Toast.makeText(this, "⚠️ Not connected to chat server", Toast.LENGTH_SHORT).show();
            // Update message status to failed
            newMessage.setTimestamp("Failed to send");
            messageAdapter.notifyItemChanged(messages.size() - 1);
        }
    }

    private void handleTypingIndicator(boolean hasText) {
        if (typingHandler == null) {
            typingHandler = new Handler(Looper.getMainLooper());
        }

        if (hasText && !isUserTyping) {
            // User started typing
            isUserTyping = true;
            if (webSocketService != null && webSocketService.isConnected()) {
                webSocketService.sendTypingIndicator(conversationId, true);
            }

        } else if (!hasText && isUserTyping) {
            // User stopped typing
            isUserTyping = false;
            if (webSocketService != null && webSocketService.isConnected()) {
                webSocketService.sendTypingIndicator(conversationId, false);
            }
        }

        // Auto-stop typing after timeout
        typingHandler.removeCallbacksAndMessages(null);
        typingHandler.postDelayed(() -> {
            if (isUserTyping) {
                isUserTyping = false;
                if (webSocketService != null && webSocketService.isConnected()) {
                    webSocketService.sendTypingIndicator(conversationId, false);
                }
            }
        }, TYPING_TIMEOUT);
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            rvMessages.smoothScrollToPosition(messages.size() - 1);
        }
    }

    private void openProductDetail() {
        if (productId != null && productId > 0) {
            Toast.makeText(this, "Product: " + productTitle, Toast.LENGTH_SHORT).show();
            // TODO: Open ProductDetailActivity
        }
    }

    // ===== WebSocket Listeners =====

    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            updateConnectionStatus(true);
            Toast.makeText(this, "✅ Connected to chat", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            updateConnectionStatus(false);
            Toast.makeText(this, "❌ Disconnected from chat", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Connection error: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    // ===== Chat Listeners =====

    @Override
    public void onMessageReceived(Message message) {
        runOnUiThread(() -> {
            // Only add if it's for this conversation and not from current user
            if (message.getConversationId().equals(conversationId) &&
                    !message.getSenderId().equals(currentUserId)) {

                messages.add(message);
                messageAdapter.notifyItemInserted(messages.size() - 1);
                scrollToBottom();

                Log.d(TAG, "✅ New message received: " + message.getContent());
            }
        });
    }

    @Override
    public void onTypingIndicator(Long userId, boolean isTyping) {
        runOnUiThread(() -> {
            if (!userId.equals(currentUserId)) {
                showTypingIndicator(isTyping);
            }
        });
    }

    @Override
    public void onMessageDelivered(Long messageId) {
        runOnUiThread(() -> {
            // Update message status to delivered
            updateMessageStatus(messageId, "Delivered");
        });
    }

    @Override
    public void onMessageRead(Long messageId) {
        runOnUiThread(() -> {
            // Update message status to read
            updateMessageStatus(messageId, "Read");
        });
    }

    // ===== Helper Methods =====

    private void updateConnectionStatus(boolean connected) {
        if (tvConnectionStatus != null) {
            if (connected) {
                tvConnectionStatus.setVisibility(View.GONE);
            } else {
                tvConnectionStatus.setVisibility(View.VISIBLE);
                tvConnectionStatus.setText("⚠️ Connecting...");
            }
        }

        Log.d(TAG, (connected ? "🟢 Connected" : "🔴 Disconnected"));
    }

    private void showTypingIndicator(boolean show) {
        if (tvTypingIndicator != null) {
            if (show) {
                tvTypingIndicator.setVisibility(View.VISIBLE);
                tvTypingIndicator.setText(otherUserName + " is typing...");
            } else {
                tvTypingIndicator.setVisibility(View.GONE);
            }
        }

        Log.d(TAG, (show ? "👀 User is typing..." : "✋ User stopped typing"));
    }

    private void updateMessageStatus(Long messageId, String status) {
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message.getId().equals(messageId)) {
                // Update timestamp to show status
                String currentTime = message.getTimestamp();
                if (currentTime.equals("Sending...")) {
                    message.setTimestamp(status);
                    messageAdapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (NavigationUtils.handleBackButton(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Leave conversation
        if (webSocketService != null && conversationId != null) {
            webSocketService.leaveConversation(conversationId);
            webSocketService.removeChatListener(conversationId, this);
            webSocketService.removeWebSocketListener(this);
        }

        // Unbind service
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }

        // Cleanup handlers
        if (typingHandler != null) {
            typingHandler.removeCallbacksAndMessages(null);
        }

        Log.d(TAG, "🧹 ChatActivity destroyed");
    }
}