// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
package com.example.newtrade.ui.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.MessageAdapter;
import com.example.newtrade.location.LocationService;
import com.example.newtrade.models.Message;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.websocket.RealtimeWebSocketService;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements
        RealtimeWebSocketService.WebSocketListener,
        RealtimeWebSocketService.ChatListener,
        LocationService.LocationCallback {

    private static final String TAG = "ChatActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvProductInfo;
    private TextView tvConnectionStatus;
    private TextView tvTypingIndicator;
    private ImageButton btnShareLocation;

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

    // Location sharing
    private LocationService locationService;
    private static final int LOCATION_PERMISSION_REQUEST = 100;

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

        // Initialize location service
        locationService = new LocationService(this);
        locationService.setLocationCallback(this);

        Log.d(TAG, "✅ ChatActivity created for conversation: " + conversationId);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        conversationId = intent.getLongExtra("conversation_id", 0L);
        productId = intent.getLongExtra("product_id", 0L);
        productTitle = intent.getStringExtra("product_title");
        sellerId = intent.getLongExtra("seller_id", 0L);
        otherUserName = intent.getStringExtra("other_user_name");

        // For creating new chat from user profile
        Long otherUserId = intent.getLongExtra("other_user_id", 0L);
        if (otherUserId > 0 && conversationId == 0L) {
            // This is a new chat - we'll create the conversation when first message is sent
            sellerId = otherUserId;
        }

        Log.d(TAG, "Intent data - conversationId: " + conversationId + ", productId: " + productId +
                ", sellerId: " + sellerId + ", otherUser: " + otherUserName);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        tvProductInfo = findViewById(R.id.tv_product_info);
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        tvTypingIndicator = findViewById(R.id.tv_typing_indicator);
        btnShareLocation = findViewById(R.id.btn_share_location);
    }

    private void setupToolbar() {
        String title = otherUserName != null ? otherUserName : "Chat";
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, title);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(messages, currentUserId);
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

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

        btnShareLocation.setOnClickListener(v -> requestLocationAndShare());
    }

    private void initWebSocketService() {
        webSocketService = RealtimeWebSocketService.getInstance();
        webSocketService.setWebSocketListener(this);
        webSocketService.setChatListener(this);

        if (!webSocketService.isConnected()) {
            String token = prefsManager.getToken();
            webSocketService.connect(token);
        }

        // Join conversation if exists
        if (conversationId > 0) {
            webSocketService.joinConversation(conversationId);
            loadChatHistory();
        }
    }

    private void loadChatHistory() {
        // Load existing messages from API
        // This would typically be done via REST API call
        Log.d(TAG, "Loading chat history for conversation: " + conversationId);
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        // Clear input
        etMessage.setText("");

        // Create message object
        Message message = new Message();
        message.setContent(messageText);
        message.setSenderId(currentUserId);
        message.setConversationId(conversationId);
        message.setMessageType("text");
        message.setCreatedAt(System.currentTimeMillis());

        // Add to local list immediately for better UX
        messages.add(message);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);

        // Send via WebSocket
        if (conversationId > 0) {
            webSocketService.sendMessage(conversationId, messageText, "text");
        } else {
            // Create new conversation first
            createConversationAndSendMessage(messageText);
        }

        Log.d(TAG, "✅ Message sent: " + messageText);
    }

    private void createConversationAndSendMessage(String messageText) {
        // Create new conversation via API then send message
        // This is a simplified version - in real app you'd call API
        Log.d(TAG, "Creating new conversation with user: " + sellerId);
        // For now, simulate conversation creation
        conversationId = System.currentTimeMillis(); // Temporary ID
        webSocketService.joinConversation(conversationId);
        webSocketService.sendMessage(conversationId, messageText, "text");
    }

    private void handleTyping() {
        if (!isUserTyping) {
            isUserTyping = true;
            if (conversationId > 0) {
                webSocketService.sendTypingIndicator(conversationId, true);
            }
        }

        // Reset typing timeout
        typingHandler.removeCallbacksAndMessages(null);
        typingHandler.postDelayed(() -> {
            isUserTyping = false;
            if (conversationId > 0) {
                webSocketService.sendTypingIndicator(conversationId, false);
            }
        }, TYPING_TIMEOUT);
    }

    private void requestLocationAndShare() {
        if (checkLocationPermission()) {
            shareCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            },
            LOCATION_PERMISSION_REQUEST);
    }

    private void shareCurrentLocation() {
        btnShareLocation.setEnabled(false);
        Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();
        locationService.getCurrentLocation();
    }

    // LocationService.LocationCallback implementation
    @Override
    public void onLocationReceived(double latitude, double longitude, String address) {
        runOnUiThread(() -> {
            btnShareLocation.setEnabled(true);

            // Create location message
            Message locationMessage = Message.createLocationMessage(
                currentUserId, conversationId, latitude, longitude, address);

            // Add to local list
            messages.add(locationMessage);
            messageAdapter.notifyItemInserted(messages.size() - 1);
            rvMessages.scrollToPosition(messages.size() - 1);

            // Send via WebSocket
            if (conversationId > 0) {
                webSocketService.sendLocationMessage(conversationId, latitude, longitude, address);
            }

            Log.d(TAG, "✅ Location shared: " + latitude + ", " + longitude + " - " + address);
            Toast.makeText(this, "Location shared!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onLocationError(String error) {
        runOnUiThread(() -> {
            btnShareLocation.setEnabled(true);
            Toast.makeText(this, "Failed to get location: " + error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Location error: " + error);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission required to share location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // WebSocket Listener Implementation
    @Override
    public void onWebSocketConnected() {
        runOnUiThread(() -> {
            tvConnectionStatus.setText("Connected");
            tvConnectionStatus.setVisibility(View.GONE);
            Log.d(TAG, "✅ WebSocket connected");
        });
    }

    @Override
    public void onWebSocketDisconnected() {
        runOnUiThread(() -> {
            tvConnectionStatus.setText("Disconnected - Reconnecting...");
            tvConnectionStatus.setVisibility(View.VISIBLE);
            Log.w(TAG, "⚠️ WebSocket disconnected");
        });
    }

    @Override
    public void onWebSocketError(String error) {
        runOnUiThread(() -> {
            tvConnectionStatus.setText("Connection Error");
            tvConnectionStatus.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Connection error: " + error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ WebSocket error: " + error);
        });
    }

    // Chat Listener Implementation
    @Override
    public void onMessageReceived(Message message) {
        runOnUiThread(() -> {
            // Only add if not from current user and not duplicate
            if (!message.getSenderId().equals(currentUserId)) {
                messages.add(message);
                messageAdapter.notifyItemInserted(messages.size() - 1);
                rvMessages.scrollToPosition(messages.size() - 1);
                Log.d(TAG, "✅ Message received: " + message.getContent());
            }
        });
    }

    @Override
    public void onTypingIndicator(Long userId, boolean isTyping) {
        runOnUiThread(() -> {
            if (!userId.equals(currentUserId)) {
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
    public void onMessageStatusUpdated(Long messageId, String status) {
        runOnUiThread(() -> {
            // Update message status (sent, delivered, read)
            for (Message message : messages) {
                if (message.getId() != null && message.getId().equals(messageId)) {
                    message.setStatus(status);
                    messageAdapter.notifyDataSetChanged();
                    break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webSocketService != null && conversationId > 0) {
            webSocketService.joinConversation(conversationId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webSocketService != null && conversationId > 0) {
            webSocketService.leaveConversation(conversationId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (typingHandler != null) {
            typingHandler.removeCallbacksAndMessages(null);
        }
        if (webSocketService != null && conversationId > 0) {
            webSocketService.leaveConversation(conversationId);
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
}