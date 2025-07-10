// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity implements ChatWebSocketManager.ChatListener {

    private static final String TAG = "ChatActivity";
    private static final int PICK_IMAGE_REQUEST = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnUploadImage, btnEmoji;
    private TextView tvTypingIndicator;
    private LinearLayout llProductInfo;
    private ImageView ivProductThumbnail;
    private TextView tvProductTitle, tvProductPrice;

    // Data
    private SharedPrefsManager prefsManager;
    private ChatWebSocketManager chatWebSocketManager;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private ChatNotificationManager chatNotificationManager;

    // Chat data
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

        chatNotificationManager = new ChatNotificationManager(this);

        Log.d(TAG, "✅ ChatActivity created for conversation: " + conversationId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnEmoji = findViewById(R.id.btn_emoji);
        tvTypingIndicator = findViewById(R.id.tv_typing_indicator);
        llProductInfo = findViewById(R.id.ll_product_info);
        ivProductThumbnail = findViewById(R.id.iv_product_thumbnail);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvProductPrice = findViewById(R.id.tv_product_price);

        prefsManager = SharedPrefsManager.getInstance(this);
        currentUserId = prefsManager.getUserId();
        messages = new ArrayList<>();

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

            if (otherUserName == null) {
                otherUserName = "Unknown User";
            }
            if (productTitle == null) {
                productTitle = "Product";
            }
            if (conversationId <= 0) {
                conversationId = 1L;
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

        // Show product info if available
        if (productTitle != null && !productTitle.isEmpty()) {
            if (llProductInfo != null) {
                llProductInfo.setVisibility(View.VISIBLE);
                if (tvProductTitle != null) {
                    tvProductTitle.setText(productTitle);
                }
            }
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, messages, currentUserId);

        // ✅ FIXED: Đảm bảo tin nhắn mới ở dưới cùng
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // ✅ REMOVED: setStackFromEnd và setReverseLayout - để default

        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);

        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                // ✅ ALWAYS scroll to bottom when new message added
                rvMessages.scrollToPosition(messages.size() - 1);
            }
        });
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        btnUploadImage.setOnClickListener(v -> openImagePicker());
        btnEmoji.setOnClickListener(v -> toggleEmojiPicker());

        // Typing indicator and send button enable/disable
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleTyping();
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean hasText = !s.toString().trim().isEmpty();
                btnSend.setEnabled(hasText);
                btnSend.setAlpha(hasText ? 1.0f : 0.5f);
            }
        });

        // Initially disable send button
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);
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

                            // ✅ FIXED: Parse all messages first
                            List<Message> parsedMessages = new ArrayList<>();
                            for (Map<String, Object> messageData : messageList) {
                                Message message = parseMessageFromApi(messageData);
                                if (message != null) {
                                    parsedMessages.add(message);
                                }
                            }

                            // ✅ FIXED: Sort by timestamp (oldest first, newest last)
                            parsedMessages.sort((m1, m2) -> {
                                // Compare by ID (assuming higher ID = newer message)
                                return Long.compare(m1.getId() != null ? m1.getId() : 0L,
                                        m2.getId() != null ? m2.getId() : 0L);
                            });

                            messages.addAll(parsedMessages);
                            messageAdapter.notifyDataSetChanged();

                            // ✅ FIXED: Scroll to bottom to show newest messages
                            if (messages.size() > 0) {
                                rvMessages.scrollToPosition(messages.size() - 1);
                            }

                            Log.d(TAG, "✅ Loaded " + messages.size() + " messages in correct order");
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
                String messageText = messageData.get("messageText").toString();
                message.setContent(messageText);
            }

            String messageType = messageData.get("messageType") != null ?
                    messageData.get("messageType").toString() : "TEXT";
            message.setMessageType(messageType);

            // Handle image messages correctly
            if ("IMAGE".equals(messageType) && message.getContent() != null) {
                message.setImageUrl(message.getContent()); // Image URL is stored in content
                Log.d(TAG, "📷 Parsed image message: " + message.getContent());
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

    // ===== TEXT MESSAGE SENDING =====

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            Log.w(TAG, "Message text is empty");
            return;
        }

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

        // Send via WebSocket AND API
        sendMessageViaWebSocket(messageText);
        sendMessageViaApi(messageText, "TEXT");

        // Send notification to receiver
        sendNotificationToReceiver(messageText);

        Log.d(TAG, "📤 Sent text message: " + messageText);
    }

    private void sendMessageViaWebSocket(String messageText) {
        if (chatWebSocketManager != null && chatWebSocketManager.isConnected()) {
            chatWebSocketManager.sendChatMessage(messageText);
        }
    }

    // ✅ FIXED: Handle both TEXT and IMAGE message types properly
    private void sendMessageViaApi(String messageText, String messageType) {
        if (conversationId == null || conversationId <= 0) {
            Log.w(TAG, "No conversation ID, cannot send via API");
            return;
        }

        Log.d(TAG, "🔍 Sending " + messageType + " message via API: " + messageText);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("conversationId", conversationId);
        messageData.put("messageText", messageText);
        messageData.put("messageType", messageType); // ✅ IMPORTANT: Use the parameter

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.sendMessage(messageData);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        if ("IMAGE".equals(messageType)) {
                            updateLastImageMessageStatus("✅ Sent");
                        } else {
                            updateLastMessageStatus("✅ Sent");
                        }
                        Log.d(TAG, "✅ " + messageType + " message sent via API");
                    } else {
                        if ("IMAGE".equals(messageType)) {
                            updateLastImageMessageStatus("❌ Failed");
                        } else {
                            updateLastMessageStatus("❌ Failed");
                        }
                        Log.e(TAG, "Failed to send " + messageType + " message via API: " + standardResponse.getMessage());
                    }
                } else {
                    if ("IMAGE".equals(messageType)) {
                        updateLastImageMessageStatus("❌ Failed");
                    } else {
                        updateLastMessageStatus("❌ Failed");
                    }
                    Log.e(TAG, "Failed to send " + messageType + " message via API: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                if ("IMAGE".equals(messageType)) {
                    updateLastImageMessageStatus("❌ Network Error");
                } else {
                    updateLastMessageStatus("❌ Network Error");
                }
                Log.e(TAG, "Error sending " + messageType + " message via API", t);
            }
        });
    }

    private void sendNotificationToReceiver(String messageText) {
        if (receiverId == null || receiverId <= 0) {
            Log.w(TAG, "No receiver ID, cannot send notification");
            return;
        }

        try {
            String senderName = prefsManager.getUserName();
            if (senderName == null || senderName.isEmpty()) {
                senderName = "Someone";
            }

            NotificationService notificationService = ApiClient.getNotificationService();

            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("receiverId", receiverId);
            notificationRequest.put("senderName", senderName);
            notificationRequest.put("message", messageText);
            notificationRequest.put("conversationId", conversationId);
            notificationRequest.put("productTitle", productTitle);

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

    // ===== IMAGE UPLOAD & SENDING =====

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            sendImageMessage(imageUri);
        }
    }

    private void sendImageMessage(Uri imageUri) {
        // Create image message for UI immediately
        Message imageMessage = new Message();
        imageMessage.setId(System.currentTimeMillis());
        imageMessage.setSenderId(currentUserId);
        imageMessage.setMessageType("IMAGE");
        imageMessage.setImageUrl(imageUri.toString());
        imageMessage.setCreatedAt("⬆️ Uploading...");

        messages.add(imageMessage); // ✅ Add to end of list (newest)
        messageAdapter.notifyItemInserted(messages.size() - 1);

        // ✅ FIXED: Force scroll to bottom immediately
        rvMessages.post(() -> {
            rvMessages.scrollToPosition(messages.size() - 1);
        });

        // Upload to server
        uploadImageToServer(imageUri);
    }

    // ✅ NO TEMP FILE: Upload directly from Uri
    private void uploadImageToServer(Uri imageUri) {
        Log.d(TAG, "🔄 Uploading image to server...");

        try {
            // Validate user ID
            if (currentUserId == null || currentUserId <= 0) {
                updateLastImageMessageStatus("❌ Login required");
                Toast.makeText(this, "Please login to send images", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create RequestBody directly from Uri
            RequestBody requestFile = createRequestBodyFromUri(imageUri);
            String fileName = "chat_image_" + System.currentTimeMillis() + ".jpg";
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", fileName, requestFile);

            ApiService apiService = ApiClient.getApiService();
            Call<StandardResponse<Map<String, String>>> call = apiService.uploadProductImage(imagePart);

            call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                       Response<StandardResponse<Map<String, String>>> response) {

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, String>> standardResponse = response.body();

                        if (standardResponse.isSuccess()) {
                            Map<String, String> data = standardResponse.getData();
                            String uploadedImageUrl = data.get("imageUrl");

                            Log.d(TAG, "✅ Image uploaded successfully: " + uploadedImageUrl);

                            // Update the last image message with the uploaded URL
                            updateLastImageMessage(uploadedImageUrl);

                            // Send image message via API
                            sendImageMessageViaApi(uploadedImageUrl);

                        } else {
                            updateLastImageMessageStatus("❌ Upload failed");
                            showError("Failed to upload image: " + standardResponse.getMessage());
                        }
                    } else {
                        updateLastImageMessageStatus("❌ Upload failed");
                        try {
                            String errorBody = response.errorBody() != null ?
                                    response.errorBody().string() : "Unknown error";
                            Log.e(TAG, "❌ Upload failed: HTTP " + response.code() + " - " + errorBody);
                            showError("Upload failed: HTTP " + response.code());
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                            showError("Upload failed");
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                    Log.e(TAG, "❌ Network error uploading image", t);
                    updateLastImageMessageStatus("❌ Network error");
                    showError("Network error while uploading image");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error preparing image upload", e);
            updateLastImageMessageStatus("❌ Preparation failed");
            showError("Error preparing image");
        }
    }

    // ✅ Create RequestBody directly from Uri (no temp file)
    private RequestBody createRequestBodyFromUri(Uri uri) throws IOException {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                String mimeType = getContentResolver().getType(uri);
                return MediaType.parse(mimeType != null ? mimeType : "image/*");
            }

            @Override
            public void writeTo(okio.BufferedSink sink) throws IOException {
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    if (inputStream == null) {
                        throw new IOException("Could not open input stream for URI: " + uri);
                    }
                    sink.writeAll(okio.Okio.source(inputStream));
                }
            }

            @Override
            public long contentLength() throws IOException {
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    if (inputStream == null) {
                        return -1;
                    }
                    return inputStream.available();
                }
            }
        };
    }

    private void updateLastImageMessage(String imageUrl) {
        if (!messages.isEmpty()) {
            Message lastMessage = messages.get(messages.size() - 1);
            if ("IMAGE".equals(lastMessage.getMessageType())) {
                lastMessage.setImageUrl(imageUrl);
                lastMessage.setContent(imageUrl); // Set content to image URL for API
                lastMessage.setCreatedAt("✅ Uploaded");
                messageAdapter.notifyItemChanged(messages.size() - 1);
            }
        }
    }

    private void updateLastImageMessageStatus(String status) {
        if (!messages.isEmpty()) {
            Message lastMessage = messages.get(messages.size() - 1);
            if ("IMAGE".equals(lastMessage.getMessageType())) {
                lastMessage.setCreatedAt(status);
                messageAdapter.notifyItemChanged(messages.size() - 1);
            }
        }
    }

    private void sendImageMessageViaApi(String imageUrl) {
        Log.d(TAG, "🔍 Sending image message via API: " + imageUrl);
        sendMessageViaApi(imageUrl, "IMAGE"); // ✅ IMPORTANT: messageType = "IMAGE"
        sendNotificationToReceiver("📷 Sent an image");
    }

    // ===== EMOJI IMPLEMENTATION =====

    private void toggleEmojiPicker() {
        String[] emojis = {"😀", "😂", "😍", "👍", "❤️", "😢", "😮", "😡", "🙏", "👏", "🔥", "💯", "🎉", "💝", "🤔"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Emoji");
        builder.setItems(emojis, (dialog, which) -> {
            String selectedEmoji = emojis[which];
            String currentText = etMessage.getText().toString();
            etMessage.setText(currentText + selectedEmoji);
            etMessage.setSelection(etMessage.getText().length());
        });
        builder.show();
    }

    // ===== HELPER METHODS =====

    private void updateLastMessageStatus(String status) {
        if (!messages.isEmpty()) {
            Message lastMessage = messages.get(messages.size() - 1);
            if ("TEXT".equals(lastMessage.getMessageType())) {
                lastMessage.setCreatedAt(status);
                messageAdapter.notifyItemChanged(messages.size() - 1);
            }
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
            // ✅ FIXED: Use scrollToPosition instead of smoothScrollToPosition for immediate scroll
            rvMessages.post(() -> {
                rvMessages.scrollToPosition(messages.size() - 1);
            });
            Log.d(TAG, "📍 Scrolled to bottom, total messages: " + messages.size());
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

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

                // ✅ Get messageType from WebSocket
                String messageType = messageJson.has("messageType") ?
                        messageJson.get("messageType").getAsString() : "TEXT";
                newMessage.setMessageType(messageType);

                // ✅ Handle image messages from WebSocket
                if ("IMAGE".equals(messageType)) {
                    newMessage.setImageUrl(newMessage.getContent());
                    Log.d(TAG, "📷 Received image via WebSocket: " + newMessage.getContent());
                }

                if (messageJson.has("timestamp")) {
                    String timestampStr = messageJson.get("timestamp").getAsString();
                    newMessage.setCreatedAt(formatTimestampString(timestampStr));
                } else {
                    newMessage.setCreatedAt("Now");
                }

                // Only add if it's not from current user (avoid duplicates)
                if (!newMessage.getSenderId().equals(currentUserId)) {
                    messages.add(newMessage);
                    messageAdapter.notifyItemInserted(messages.size() - 1);
                    scrollToBottom();
                    Log.d(TAG, "✅ Added received " + messageType + " message to UI");
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

    // ===== LIFECYCLE METHODS =====

    @Override
    protected void onResume() {
        super.onResume();

        if (chatWebSocketManager != null && chatWebSocketManager.isConnected() && conversationId != null) {
            joinConversation();
        }

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