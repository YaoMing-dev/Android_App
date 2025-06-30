// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
package com.example.newtrade.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.chat.adapter.ChatMessagesAdapter;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity implements ChatMessagesAdapter.OnMessageActionListener {
    private static final String TAG = "ChatActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView tvSellerName, tvProductTitle, tvOnlineStatus;
    private ImageView ivSellerAvatar;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private MaterialButton btnSend;
    private View layoutTypingIndicator;

    // Data
    private ChatMessagesAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private Long conversationId;
    private Long productId;
    private String sellerName;
    private String productTitle;

    // Utils
    private SharedPrefsManager prefsManager;

    // State
    private boolean isLoading = false;
    private int currentPage = 0;
    private boolean isLastPage = false;

    public static class ChatMessage {
        public Long id;
        public String content;
        public String timestamp;
        public Long senderId;
        public String senderName;
        public String senderAvatar;
        public boolean isRead;
        public MessageType type = MessageType.TEXT;
        public String attachmentUrl;

        public enum MessageType {
            TEXT, IMAGE, OFFER, SYSTEM
        }

        // Helper method
        public boolean isSentByCurrentUser(Long currentUserId) {
            return currentUserId != null && currentUserId.equals(senderId);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        prefsManager = new SharedPrefsManager(this);

        // Get data from intent
        conversationId = getIntent().getLongExtra(Constants.BUNDLE_CONVERSATION_ID, -1);
        productId = getIntent().getLongExtra(Constants.BUNDLE_PRODUCT_ID, -1);
        sellerName = getIntent().getStringExtra("sellerName");
        productTitle = getIntent().getStringExtra("productTitle");

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        if (conversationId != -1) {
            loadMessages();
        } else if (productId != -1) {
            // Create new conversation for product
            createConversationForProduct();
        } else {
            Toast.makeText(this, "Invalid conversation", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvOnlineStatus = findViewById(R.id.tv_online_status);
        ivSellerAvatar = findViewById(R.id.iv_seller_avatar);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        layoutTypingIndicator = findViewById(R.id.layout_typing_indicator);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Set chat info
        if (sellerName != null) {
            tvSellerName.setText(sellerName);
        }
        if (productTitle != null) {
            tvProductTitle.setText(productTitle);
            tvProductTitle.setVisibility(View.VISIBLE);
        } else {
            tvProductTitle.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        adapter = new ChatMessagesAdapter(messages, prefsManager.getUserId(), this);
        rvMessages.setAdapter(adapter);

        // Scroll to bottom when new message is added
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                rvMessages.scrollToPosition(adapter.getItemCount() - 1);
            }
        });

        // Load more messages when scrolling up
        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0 && !isLoading && !isLastPage) {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null && lm.findFirstVisibleItemPosition() == 0) {
                        loadMoreMessages();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        // Send button
        btnSend.setOnClickListener(v -> sendMessage());

        // Text input
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                btnSend.setEnabled(hasText);

                // TODO: Send typing indicator
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Enter key to send
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void createConversationForProduct() {
        if (productId == -1) return;

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("content", "Hi, I'm interested in your product.");

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getChatService()
                .createConversationForProduct(productId, messageData, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    Object convIdObj = data.get("conversationId");
                    if (convIdObj instanceof Number) {
                        conversationId = ((Number) convIdObj).longValue();
                        loadMessages();
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to start conversation", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to create conversation", t);
                Toast.makeText(ChatActivity.this, "Failed to start conversation", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadMessages() {
        if (isLoading || conversationId == -1) return;

        isLoading = true;

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getChatService()
                .getMessages(conversationId, currentPage, Constants.CHAT_PAGE_SIZE, null, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;
                handleMessagesResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                isLoading = false;
                Log.e(TAG, "Failed to load messages", t);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleMessagesResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> messageMaps = (List<Map<String, Object>>) data.get("content");

                if (messageMaps != null) {
                    List<ChatMessage> newMessages = new ArrayList<>();
                    for (Map<String, Object> messageMap : messageMaps) {
                        ChatMessage message = parseMessageFromMap(messageMap);
                        if (message != null) {
                            newMessages.add(message);
                        }
                    }

                    if (currentPage == 0) {
                        messages.clear();
                        messages.addAll(newMessages);
                        adapter.notifyDataSetChanged();
                    } else {
                        // Insert at beginning for pagination
                        messages.addAll(0, newMessages);
                        adapter.notifyItemRangeInserted(0, newMessages.size());
                    }

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : newMessages.size() < Constants.CHAT_PAGE_SIZE;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing messages response", e);
        }
    }

    private ChatMessage parseMessageFromMap(Map<String, Object> messageMap) {
        try {
            ChatMessage message = new ChatMessage();
            message.id = getLongFromMap(messageMap, "id");
            message.content = (String) messageMap.get("content");
            message.timestamp = (String) messageMap.get("timestamp");
            message.senderId = getLongFromMap(messageMap, "senderId");

            Object isReadObj = messageMap.get("isRead");
            message.isRead = isReadObj instanceof Boolean ? (Boolean) isReadObj : false;

            // Parse sender info
            Object senderObj = messageMap.get("sender");
            if (senderObj instanceof Map) {
                Map<String, Object> senderMap = (Map<String, Object>) senderObj;
                message.senderName = (String) senderMap.get("displayName");
                message.senderAvatar = (String) senderMap.get("avatarUrl");
            }

            return message;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message from map", e);
            return null;
        }
    }

    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private void loadMoreMessages() {
        if (!isLoading && !isLastPage) {
            currentPage++;
            loadMessages();
        }
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty() || conversationId == -1) return;

        // Clear input immediately
        etMessage.setText("");

        // Create message data
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("content", content);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getChatService()
                .sendMessage(conversationId, messageData, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Message sent successfully
                    // In a real app, we'd add the message to the list immediately
                    // and update it when we get the server response
                    Map<String, Object> data = response.body().getData();
                    ChatMessage newMessage = parseMessageFromMap(data);
                    if (newMessage != null) {
                        messages.add(newMessage);
                        adapter.notifyItemInserted(messages.size() - 1);
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    // Restore message to input
                    etMessage.setText(content);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to send message", t);
                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                // Restore message to input
                etMessage.setText(content);
            }
        });
    }

    // ChatMessagesAdapter.OnMessageActionListener implementation
    @Override
    public void onMessageClick(ChatMessage message) {
        // Handle message click (e.g., show details, copy text)
    }

    @Override
    public void onMessageLongClick(ChatMessage message) {
        // Handle long click (e.g., show context menu)
    }

    @Override
    public void onAttachmentClick(ChatMessage message) {
        // Handle attachment click
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mark conversation as read
        if (conversationId != -1) {
            markConversationAsRead();
        }
    }

    private void markConversationAsRead() {
        Call<StandardResponse<Void>> call = ApiClient.getChatService()
                .markConversationAsRead(conversationId, prefsManager.getUserId());
        call.enqueue(new Callback<StandardResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Void>> call, @NonNull Response<StandardResponse<Void>> response) {
                // Success
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                // Ignore failure
            }
        });
    }
}