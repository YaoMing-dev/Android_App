// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
package com.example.newtrade.ui.chat;

import android.content.Intent;
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
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.api.NotificationService;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.ChatNotificationManager;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.websocket.ChatWebSocketManager;
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

    // Chat data - ✅ FIXED: Khởi tạo default values
    private Long conversationId = 0L;
    private String otherUserName = "";
    private String productTitle = "";
    private Long currentUserId = 0L;
    private Long productId = 0L;
    private Long receiverId = 0L;

    // Typing indicator
    private Handler typingHandler = new Handler();
    private Runnable stopTypingRunnable;
    private boolean isTyping = false;

    // ✅ THÊM: Notification manager
    private ChatNotificationManager chatNotificationManager;

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

        // ✅ THÊM: Initialize notification manager
        chatNotificationManager = new ChatNotificationManager(this);

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

        // ✅ THÊM: Null safety
        if (currentUserId == null) {
            currentUserId = 0L;
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            conversationId = intent.getLongExtra("conversation_id", 0L);
            otherUserName = intent.getStringExtra("other_user_name");
            productTitle = intent.getStringExtra("product_title");
            productId = intent.getLongExtra("product_id", 0L);
            receiverId = intent.getLongExtra("receiverId", 0L);

            // ✅ THÊM: Null safety và default values
            if (otherUserName == null) {
                otherUserName = "Unknown User";
            }
            if (productTitle == null) {
                productTitle = "Product";
            }
            if (conversationId <= 0) {
                conversationId = 1L; // Default conversation ID
            }
        }

        Log.d(TAG, "Chat data - ConversationID: " + conversationId +
                ", ProductID: " + productId +
                ", Other user: " + otherUserName +
                ", ReceiverID: " + receiverId);
    }

    private void setupToolbar() {
        String title = !otherUserName.isEmpty() ? otherUserName : "Chat";
        String subtitle = !productTitle.isEmpty() ? productTitle : "";

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
        chatWebSocketManager = ChatWebSocketManager.getInstance();
        chatWebSocketManager.addChatListener(this);

        if (currentUserId != null && currentUserId > 0) {
            if (!chatWebSocketManager.isConnected()) {
                chatWebSocketManager.connect(currentUserId);
            } else {
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

    private void loadOrCreateConversation() {
        if (conversationId != null && conversationId > 0) {
            loadMessages();
        } else if (productId != null && productId > 0) {
            createConversation();
        } else {
            addMockMessages();
        }
    }

    private void createConversation() {
        if (currentUserId == null || currentUserId <= 0) {
            Log.e(TAG, "❌ User not logged in, currentUserId: " + currentUserId);
            Toast.makeText(this, "Bạn cần đăng nhập để chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (productId == null || productId <= 0) {
            Log.e(TAG, "❌ Invalid product ID: " + productId);
            addMockMessages();
            return;
        }

        Log.d(TAG, "🔍 Creating conversation - UserID: " + currentUserId + ", ProductID: " + productId);

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.findOrCreateConversation(productId);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                Log.d(TAG, "🔍 Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();
                    Log.d(TAG, "🔍 Response success: " + standardResponse.isSuccess());

                    if (standardResponse.isSuccess()) {
                        Map<String, Object> conversationResponse = standardResponse.getData();
                        conversationId = ((Number) conversationResponse.get("id")).longValue();

                        Log.d(TAG, "✅ Conversation created/found: " + conversationId);

                        if (chatWebSocketManager.isConnected()) {
                            joinConversation();
                        }

                        loadMessages();

                    } else {
                        Log.e(TAG, "❌ Failed to create conversation: " + standardResponse.getMessage());
                        addMockMessages();
                    }
                } else {
                    Log.e(TAG, "❌ HTTP Error " + response.code() + ": " + response.message());
                    addMockMessages();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "❌ Network error creating conversation", t);
                addMockMessages();
            }
        });
    }

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
                        addMockMessages();
                    }
                } else {
                    Log.e(TAG, "Failed to load messages: HTTP " + response.code());
                    addMockMessages();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Error loading messages", t);
                addMockMessages();
            }
        });
    }

    private Message parseMessageFromApi(Map<String, Object> messageData) {
        try {
            Message message = new Message();

            if (messageData.get("id") != null) {
                message.setId(((Number) messageData.get("id")).longValue());
            }

            if (messageData.get("sender") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sender = (Map<String, Object>) messageData.get("sender");
                if (sender.get("id") != null) {
                    message.setSenderId(((Number) sender.get("id")).longValue());
                }
            }

            if (messageData.get("messageText") != null) {
                message.setContent(messageData.get("messageText").toString());
            }

            if (messageData.get("messageType") != null) {
                message.setMessageType(messageData.get("messageType").toString());
            } else {
                message.setMessageType("TEXT");
            }

            if (messageData.get("createdAt") != null) {
                String timestamp = messageData.get("createdAt").toString();
                message.setCreatedAt(formatTimestampString(timestamp));
            }

            return message;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing message from API", e);
            return null;
        }
    }

    private void addMockMessages() {
        messages.clear();

        Message msg1 = new Message();
        msg1.setId(1L);
        msg1.setSenderId(2L);
        msg1.setContent("Hello! Is this item still available?");
        msg1.setCreatedAt("10:30 AM");
        msg1.setMessageType("TEXT");
        messages.add(msg1);

        Message msg2 = new Message();
        msg2.setId(2L);
        msg2.setSenderId(currentUserId);
        msg2.setContent("Yes, it's still available! Would you like to know more details?");
        msg2.setCreatedAt("10:32 AM");
        msg2.setMessageType("TEXT");
        messages.add(msg2);

        messageAdapter.notifyDataSetChanged();
        scrollToBottom();

        Log.d(TAG, "✅ Mock messages loaded");
    }

    // ✅ CẬP NHẬT: sendMessage() method với notification
    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            Log.w(TAG, "Message text is empty");
            return;
        }

        // ✅ THÊM: Validation
        if (currentUserId == null || currentUserId <= 0) {
            Log.e(TAG, "Current user ID is invalid");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add message to UI immediately for better UX
        Message newMessage = new Message();
        newMessage.setId(System.currentTimeMillis());
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

        // ✅ THÊM: Send notification to receiver
        sendNotificationToReceiver(messageText);

        Log.d(TAG, "📤 Sent message: " + messageText);
    }

    private void sendMessageViaWebSocket(String messageText) {
        if (chatWebSocketManager != null && chatWebSocketManager.isConnected()) {
            chatWebSocketManager.sendChatMessage(messageText);
        }
    }

    private void sendMessageViaApi(String messageText) {
        if (conversationId == null || conversationId <= 0) {
            Log.w(TAG, "No conversation ID, cannot send via API");
            return;
        }

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("conversationId", conversationId);
        messageData.put("messageText", messageText);
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
                        updateLastMessageStatus("✅ Sent");
                        Log.d(TAG, "✅ Message sent via API");
                    } else {
                        updateLastMessageStatus("❌ Failed");
                        Log.e(TAG, "Failed to send message via API: " + standardResponse.getMessage());
                    }
                } else {
                    updateLastMessageStatus("❌ Failed");
                    Log.e(TAG, "Failed to send message via API: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                updateLastMessageStatus("❌ Network Error");
                Log.e(TAG, "Error sending message via API", t);
            }
        });
    }

    // ✅ THÊM: Method để gửi notification cho người nhận
    private void sendNotificationToReceiver(String messageText) {
        if (receiverId == null || receiverId <= 0) {
            Log.w(TAG, "No receiver ID, cannot send notification");
            return;
        }

        try {
            // Get sender name
            String senderName = prefsManager.getUserName();
            if (senderName == null || senderName.isEmpty()) {
                senderName = "Someone";
            }

            // ✅ CALL BACKEND NOTIFICATION API
            NotificationService notificationService = ApiClient.getNotificationService();

            // Create notification request
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("receiverId", receiverId);
            notificationRequest.put("senderName", senderName);
            notificationRequest.put("message", messageText);
            notificationRequest.put("conversationId", conversationId);
            notificationRequest.put("productTitle", productTitle);

            // Call backend để gửi push notification
            Call<StandardResponse<String>> call = notificationService.sendMessageNotification(notificationRequest);

            call.enqueue(new Callback<StandardResponse<String>>() {
                @Override
                public void onResponse(Call<StandardResponse<String>> call,
                                       Response<StandardResponse<String>> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Notification sent to receiver: " + receiverId);
                    } else {
                        Log.e(TAG, "❌ Failed to send notification: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                    Log.e(TAG, "❌ Error sending notification", t);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error in sendNotificationToReceiver", e);
        }
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
        }

        if (stopTypingRunnable != null) {
            typingHandler.removeCallbacks(stopTypingRunnable);
        }

        stopTypingRunnable = () -> {
            if (isTyping) {
                isTyping = false;
            }
        };
        typingHandler.postDelayed(stopTypingRunnable, 1000);
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            rvMessages.smoothScrollToPosition(messages.size() - 1);
        }
    }

    private String formatTimestampString(String timestampStr) {
        try {
            SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String cleanTimestamp = timestampStr.split("\\.")[0];
            Date dateTime = inputFormatter.parse(cleanTimestamp);

            if (dateTime != null) {
                SimpleDateFormat outputFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return outputFormatter.format(dateTime);
            }

            return "Now";
        } catch (Exception e) {
            Log.w(TAG, "Could not parse timestamp: " + timestampStr, e);
            return "Now";
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
        });
    }

    @Override
    public void onMessageReceived(JsonObject messageJson) {
        runOnUiThread(() -> {
            try {
                Message newMessage = new Message();

                newMessage.setId(messageJson.has("id") ?
                        messageJson.get("id").getAsLong() : System.currentTimeMillis());
                newMessage.setSenderId(messageJson.get("senderId").getAsLong());
                newMessage.setContent(messageJson.get("content").getAsString());
                newMessage.setMessageType(messageJson.has("messageType") ?
                        messageJson.get("messageType").getAsString() : "TEXT");

                if (messageJson.has("timestamp")) {
                    String timestampStr = messageJson.get("timestamp").getAsString();
                    newMessage.setCreatedAt(formatTimestampString(timestampStr));
                } else {
                    newMessage.setCreatedAt("Now");
                }

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
    public void onError(String error) {
        runOnUiThread(() -> {
            Log.e(TAG, "❌ WebSocket error: " + error);
        });
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date dateTime = inputFormatter.parse(timestamp);

            if (dateTime != null) {
                SimpleDateFormat outputFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return outputFormatter.format(dateTime);
            }

            return timestamp;
        } catch (ParseException e) {
            return timestamp;
        }
    }

    // ===== LIFECYCLE METHODS =====

    // ✅ CẬP NHẬT: Clear notifications when chat is active
    @Override
    protected void onResume() {
        super.onResume();

        if (chatWebSocketManager != null && chatWebSocketManager.isConnected() && conversationId != null) {
            joinConversation();
        }

        // ✅ THÊM: Clear notifications when chat is active
        if (conversationId != null && conversationId > 0) {
            try {
                chatNotificationManager.clearNotifications(conversationId);
                Log.d(TAG, "✅ Cleared notifications for conversation: " + conversationId);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error clearing notifications", e);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (chatWebSocketManager != null) {
            chatWebSocketManager.leaveConversation();
        }

        if (isTyping) {
            isTyping = false;
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

        // ✅ THÊM: Cleanup notification manager
        if (chatNotificationManager != null) {
            if (conversationId != null && conversationId > 0) {
                try {
                    chatNotificationManager.clearNotifications(conversationId);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error clearing notifications on destroy", e);
                }
            }
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