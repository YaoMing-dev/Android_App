// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

    // Typing indicator
    private Handler typingHandler;
    private boolean isUserTyping = false;
    private static final long TYPING_TIMEOUT = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize
        prefsManager = SharedPrefsManager.getInstance(this);
        currentUserId = prefsManager.getUserId();
        typingHandler = new Handler(Looper.getMainLooper());

        // Get intent data
        getIntentData();

        // Initialize views
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        // Initialize WebSocket service
        initWebSocketService();

        Log.d(TAG, "✅ ChatActivity created for conversation: " + conversationId);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        conversationId = intent.getLongExtra("conversation_id", 0L);
        productId = intent.getLongExtra("product_id", 0L);
        productTitle = intent.getStringExtra("product_title");
        sellerId = intent.getLongExtra("seller_id", 0L);
        otherUserName = intent.getStringExtra("other_user_name");

        if (conversationId <= 0) {
            Log.e(TAG, "❌ Invalid conversation ID");
            finish();
            return;
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        tvProductInfo = findViewById(R.id.tv_product_info);
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        tvTypingIndicator = findViewById(R.id.tv_typing_indicator);

        // Set product info if available
        if (productTitle != null && !productTitle.isEmpty()) {
            tvProductInfo.setText("Về: " + productTitle);
            tvProductInfo.setVisibility(View.VISIBLE);
        } else {
            tvProductInfo.setVisibility(View.GONE);
        }

        // Initially hide typing indicator
        tvTypingIndicator.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar,
                otherUserName != null ? otherUserName : "Chat");
    }

    private void setupRecyclerView() {
        // ✅ FIX: Remove 'this' parameter - MessageAdapter constructor only needs messages and currentUserId
        messageAdapter = new MessageAdapter(messages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Show latest messages at bottom
        rvMessages.setLayoutManager(layoutManager);
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
                handleTypingIndicator();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Enable/disable send button based on text content
                boolean hasText = s.toString().trim().length() > 0;
                btnSend.setEnabled(hasText);
                btnSend.setAlpha(hasText ? 1.0f : 0.5f);
            }
        });
    }

    private void initWebSocketService() {
        webSocketService = RealtimeWebSocketService.getInstance();

        // Add listeners
        webSocketService.addWebSocketListener(TAG, this);
        webSocketService.addChatListener(conversationId, this);

        // Connect if not connected
        if (!webSocketService.isConnected() && currentUserId != null) {
            webSocketService.connect(currentUserId);
        }

        // Join conversation
        if (conversationId != null && conversationId > 0) {
            webSocketService.joinConversation(conversationId);
        }

        // Update connection status
        updateConnectionStatus(webSocketService.isConnected());
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty() || webSocketService == null) {
            return;
        }

        // Clear input
        etMessage.setText("");

        // Send via WebSocket
        webSocketService.sendMessage(conversationId, content);

        // Add message to UI immediately (optimistic update)
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(currentUserId);
        message.setContent(content);
        message.setMessageType("TEXT");
        message.setTimestamp(getCurrentTimeString());

        addMessageToUI(message);

        Log.d(TAG, "📤 Message sent: " + content);
    }

    private void handleTypingIndicator() {
        if (webSocketService == null) {
            return;
        }

        // Send typing start
        if (!isUserTyping) {
            isUserTyping = true;
            webSocketService.sendTypingIndicator(conversationId, true);
        }

        // Reset typing timeout
        typingHandler.removeCallbacksAndMessages(null);
        typingHandler.postDelayed(() -> {
            if (isUserTyping) {
                isUserTyping = false;
                webSocketService.sendTypingIndicator(conversationId, false);
            }
        }, TYPING_TIMEOUT);
    }

    private void addMessageToUI(Message message) {
        runOnUiThread(() -> {
            messages.add(message);
            messageAdapter.notifyItemInserted(messages.size() - 1);
            rvMessages.scrollToPosition(messages.size() - 1);
        });
    }

    private void updateConnectionStatus(boolean connected) {
        runOnUiThread(() -> {
            if (tvConnectionStatus != null) {
                if (connected) {
                    tvConnectionStatus.setText("🟢 Connected");
                    tvConnectionStatus.setVisibility(View.VISIBLE);
                } else {
                    tvConnectionStatus.setText("🔴 Disconnected");
                    tvConnectionStatus.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private String getCurrentTimeString() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    // ===== WebSocketListener Implementation =====

    @Override
    public void onConnectionChanged(boolean connected) {
        updateConnectionStatus(connected);
        Log.d(TAG, connected ? "✅ Connected to WebSocket" : "❌ Disconnected from WebSocket");
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Connection error: " + error, Toast.LENGTH_SHORT).show();
            updateConnectionStatus(false);
        });
        Log.e(TAG, "❌ WebSocket error: " + error);
    }

    // ===== ChatListener Implementation =====

    @Override
    public void onMessageReceived(Message message) {
        Log.d(TAG, "📨 Message received: " + message.getContent());
        addMessageToUI(message);
    }

    @Override
    public void onTypingIndicator(Long userId, boolean isTyping) {
        // Only show if it's not the current user
        if (!userId.equals(currentUserId)) {
            runOnUiThread(() -> {
                if (isTyping) {
                    tvTypingIndicator.setText(otherUserName + " đang gõ...");
                    tvTypingIndicator.setVisibility(View.VISIBLE);
                } else {
                    tvTypingIndicator.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onMessageDelivered(Long messageId) {
        Log.d(TAG, "✅ Message delivered: " + messageId);
        // Update message status in UI if needed
    }

    @Override
    public void onMessageRead(Long messageId) {
        Log.d(TAG, "✅ Message read: " + messageId);
        // Update message status in UI if needed
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

        // Stop typing indicator
        if (typingHandler != null) {
            typingHandler.removeCallbacksAndMessages(null);
        }

        // Remove listeners
        if (webSocketService != null) {
            webSocketService.removeWebSocketListener(TAG);
            webSocketService.removeChatListener(conversationId, this);

            // Leave conversation
            webSocketService.leaveConversation(conversationId);
        }

        Log.d(TAG, "🧹 ChatActivity destroyed");
    }
}