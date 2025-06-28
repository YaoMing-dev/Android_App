// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
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

import androidx.annotation.NonNull;
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

        // Get intent data
        getIntentData();

        // Initialize components
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        // Load existing messages
        loadMessages();

        // Bind to WebSocket service
        bindToWebSocketService();

        Log.d(TAG, "ChatActivity created");
    }

    private void getIntentData() {
        Intent intent = getIntent();
        conversationId = intent.getLongExtra("conversation_id", -1L);
        productId = intent.getLongExtra("product_id", -1L);
        productTitle = intent.getStringExtra("product_title");
        sellerId = intent.getLongExtra("seller_id", -1L);
        otherUserName = intent.getStringExtra("other_user_name");

        prefsManager = SharedPrefsManager.getInstance(this);
        currentUserId = prefsManager.getUserId();

        Log.d(TAG, "Chat data - Conversation: " + conversationId + ", Product: " + productTitle);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        tvProductInfo = findViewById(R.id.tv_product_info);
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        tvTypingIndicator = findViewById(R.id.tv_typing_indicator);

        typingHandler = new Handler(Looper.getMainLooper());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(otherUserName != null ? otherUserName : "Chat");
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !isUserTyping) {
                    isUserTyping = true;
                    sendTypingIndicator(true);
                }

                // Reset typing timeout
                typingHandler.removeCallbacks(stopTypingRunnable);
                typingHandler.postDelayed(stopTypingRunnable, TYPING_TIMEOUT);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Update product info if available
        if (productTitle != null) {
            tvProductInfo.setText("About: " + productTitle);
            tvProductInfo.setVisibility(View.VISIBLE);
        }
    }

    private final Runnable stopTypingRunnable = () -> {
        if (isUserTyping) {
            isUserTyping = false;
            sendTypingIndicator(false);
        }
    };

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        if (webSocketService != null && webSocketService.isConnected()) {
            // ✅ FIX: Add sendChatMessage method implementation
            sendChatMessage(conversationId, messageText, "text");
            etMessage.setText("");
        } else {
            Toast.makeText(this, "Not connected. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ FIX: Add missing sendChatMessage method
    private void sendChatMessage(Long conversationId, String content, String messageType) {
        if (webSocketService != null && webSocketService.isConnected()) {
            webSocketService.sendMessage(conversationId, content);

            // Add message to local list immediately for better UX
            Message localMessage = new Message();
            localMessage.setConversationId(conversationId);
            localMessage.setSenderId(currentUserId);
            localMessage.setContent(content);
            localMessage.setMessageType(messageType);
            localMessage.setTimestamp(java.text.DateFormat.getTimeInstance().format(new java.util.Date()));

            messages.add(localMessage);
            messageAdapter.notifyItemInserted(messages.size() - 1);
            rvMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private void sendTypingIndicator(boolean isTyping) {
        if (webSocketService != null && webSocketService.isConnected() && conversationId != null) {
            webSocketService.sendTypingIndicator(conversationId, isTyping);
        }
    }

    private void loadMessages() {
        // TODO: Load existing messages from API
        // For now, create mock messages
        createMockMessages();
    }

    private void createMockMessages() {
        // Mock message 1
        Message message1 = new Message();
        message1.setId(1L);
        message1.setConversationId(conversationId);
        message1.setSenderId(sellerId);
        message1.setContent("Hi! Is this product still available?");
        message1.setMessageType("text");
        message1.setTimestamp("10:30 AM");
        messages.add(message1);

        // Mock message 2
        Message message2 = new Message();
        message2.setId(2L);
        message2.setConversationId(conversationId);
        message2.setSenderId(currentUserId);
        message2.setContent("Yes, it's still available. Would you like to see it?");
        message2.setMessageType("text");
        message2.setTimestamp("10:35 AM");
        messages.add(message2);

        messageAdapter.notifyDataSetChanged();
        rvMessages.scrollToPosition(messages.size() - 1);
    }

    private void bindToWebSocketService() {
        Intent intent = new Intent(this, RealtimeWebSocketService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void updateConnectionStatus(boolean isConnected) {
        if (tvConnectionStatus != null) {
            tvConnectionStatus.setText(isConnected ? "Connected" : "Connecting...");
            tvConnectionStatus.setTextColor(getResources().getColor(
                    isConnected ? android.R.color.holo_green_dark : android.R.color.holo_orange_dark
            ));
        }
    }

    // ===== WebSocketListener Implementation =====

    @Override
    public void onConnected() {
        runOnUiThread(() -> updateConnectionStatus(true));
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> updateConnectionStatus(false));
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            updateConnectionStatus(false);
            Toast.makeText(this, "Connection error: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    // ===== ChatListener Implementation =====

    @Override
    public void onMessageReceived(Message message) {
        runOnUiThread(() -> {
            messages.add(message);
            messageAdapter.notifyItemInserted(messages.size() - 1);
            rvMessages.scrollToPosition(messages.size() - 1);
        });
    }

    @Override
    public void onTypingIndicator(Long userId, boolean isTyping) {
        runOnUiThread(() -> {
            if (!userId.equals(currentUserId)) { // Don't show our own typing
                tvTypingIndicator.setText(isTyping ? otherUserName + " is typing..." : "");
                tvTypingIndicator.setVisibility(isTyping ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onMessageDelivered(Long messageId) {
        // Update message delivery status
        runOnUiThread(() -> {
            // TODO: Update message status in list
        });
    }

    @Override
    public void onMessageRead(Long messageId) {
        // Update message read status
        runOnUiThread(() -> {
            // TODO: Update message status in list
        });
    }

    // ✅ FIX: Add missing leaveConversation method
    private void leaveConversation(Long conversationId) {
        if (webSocketService != null && conversationId != null) {
            webSocketService.removeChatListener(conversationId, this);
            Log.d(TAG, "Left conversation: " + conversationId);
        }
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
    protected void onDestroy() {
        super.onDestroy();

        // Remove typing timeout
        if (typingHandler != null) {
            typingHandler.removeCallbacks(stopTypingRunnable);
        }

        // Leave conversation and remove listeners
        if (conversationId != null) {
            leaveConversation(conversationId);
        }

        // Unbind from service
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }

        Log.d(TAG, "ChatActivity destroyed");
    }
}