// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.ui.profile.UserProfileActivity;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView tvOtherUserName;
    private ImageView ivOtherUserAvatar;
    private MaterialCardView cardProduct;
    private TextView tvProductTitle, tvProductPrice;
    private ImageView ivProductImage;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private MaterialButton btnSend;
    private ProgressBar progressBar;
    private View loadingView, contentView;

    // Data
    private Long conversationId = -1L;
    private Long otherUserId;
    private String otherUserName;
    private Long productId;
    private String productTitle;
    private List<MessageItem> messages = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Message data class
    public static class MessageItem {
        public Long id;
        public String content;
        public Date timestamp;
        public boolean isFromMe;
        public String senderName;
        public String senderAvatar;
        public boolean isRead;

        public MessageItem() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getIntentData();
        initViews();
        initUtils();
        setupToolbar();
        setupListeners();
        setupRecyclerView();

        if (conversationId != -1) {
            loadMessages();
        } else {
            createConversation();
        }

        setupProductCard();
    }

    private void getIntentData() {
        conversationId = getIntent().getLongExtra(Constants.BUNDLE_CONVERSATION_ID, -1);
        otherUserId = getIntent().getLongExtra(Constants.BUNDLE_USER_ID, -1);
        otherUserName = getIntent().getStringExtra("otherUserName");
        productId = getIntent().getLongExtra(Constants.BUNDLE_PRODUCT_ID, -1);
        productTitle = getIntent().getStringExtra("productTitle");

        if (otherUserId == -1 || otherUserName == null) {
            Toast.makeText(this, "Invalid chat data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvOtherUserName = findViewById(R.id.tv_other_user_name);
        ivOtherUserAvatar = findViewById(R.id.iv_other_user_avatar);
        cardProduct = findViewById(R.id.card_product);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvProductPrice = findViewById(R.id.tv_product_price);
        ivProductImage = findViewById(R.id.iv_product_image);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        progressBar = findViewById(R.id.progress_bar);
        loadingView = findViewById(R.id.view_loading);
        contentView = findViewById(R.id.view_content);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        tvOtherUserName.setText(otherUserName);

        // Make toolbar clickable to view profile
        toolbar.setOnClickListener(v -> viewOtherUserProfile());
    }

    private void setupListeners() {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSend.setOnClickListener(v -> sendMessage());

        // Product card click
        cardProduct.setOnClickListener(v -> viewProduct());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true); // Show latest messages at bottom
        rvMessages.setLayoutManager(layoutManager);

        // TODO: Create MessagesAdapter
        // MessagesAdapter adapter = new MessagesAdapter(messages, prefsManager.getUserId());
        // rvMessages.setAdapter(adapter);

        // Pagination scroll listener
        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && !isLastPage && dy < 0) { // Scrolling up to load older messages
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (pastVisibleItems <= 2) { // Load when near top
                        loadMoreMessages();
                    }
                }
            }
        });
    }

    private void setupProductCard() {
        if (productId != -1 && productTitle != null) {
            cardProduct.setVisibility(View.VISIBLE);
            tvProductTitle.setText(productTitle);
            // TODO: Load product details if needed
        } else {
            cardProduct.setVisibility(View.GONE);
        }
    }

    // FR-4.1.1: Secure chat between users
    private void createConversation() {
        if (productId == -1) {
            Toast.makeText(this, "Cannot start conversation without product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoadingState();

        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("productId", productId);
        conversationData.put("message", "Hi! I'm interested in your product.");

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getChatService()
                .startConversationForProduct(productId, conversationData, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleCreateConversationResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to create conversation", t);
                showError("Failed to start conversation");
                finish();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleCreateConversationResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                Object convIdObj = data.get("conversationId");
                if (convIdObj instanceof Number) {
                    conversationId = ((Number) convIdObj).longValue();
                    loadMessages();
                } else {
                    showError("Invalid conversation data");
                    finish();
                }
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Failed to start conversation";
                showError(message);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing create conversation response", e);
            showError("Failed to start conversation");
            finish();
        }
    }

    private void loadMessages() {
        if (isLoading || conversationId == -1) return;

        isLoading = true;
        if (currentPage == 0) {
            showLoadingState();
        }

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getChatService()
                .getMessages(conversationId, currentPage, Constants.CHAT_PAGE_SIZE, null, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleMessagesResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleLoadingError(t);
            }
        });
    }

    private void loadMoreMessages() {
        if (isLoading || isLastPage) return;

        currentPage++;
        loadMessages();
    }

    @SuppressWarnings("unchecked")
    private void handleMessagesResponse(Response<StandardResponse<Map<String, Object>>> response) {
        isLoading = false;

        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> messageMaps = (List<Map<String, Object>>) data.get("content");

                if (messageMaps != null) {
                    int oldSize = messages.size();

                    for (Map<String, Object> messageMap : messageMaps) {
                        MessageItem message = parseMessageFromMap(messageMap);
                        if (message != null) {
                            if (currentPage == 0) {
                                messages.add(message); // Add to end for first page
                            } else {
                                messages.add(0, message); // Add to beginning for pagination
                            }
                        }
                    }

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : true;

                    // Update UI
                    showContentState();
                    // TODO: Notify adapter
                    // adapter.notifyDataSetChanged();

                    // Scroll to bottom for first load
                    if (currentPage == 0 && !messages.isEmpty()) {
                        rvMessages.scrollToPosition(messages.size() - 1);
                    }
                }
            } else {
                showError("Failed to load messages");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing messages response", e);
            showError("Error loading messages");
        }
    }

    private MessageItem parseMessageFromMap(Map<String, Object> messageMap) {
        try {
            MessageItem message = new MessageItem();

            message.id = ((Number) messageMap.get("id")).longValue();
            message.content = (String) messageMap.get("content");
            message.isRead = (Boolean) messageMap.getOrDefault("isRead", false);

            // Parse sender info
            @SuppressWarnings("unchecked")
            Map<String, Object> senderMap = (Map<String, Object>) messageMap.get("sender");
            if (senderMap != null) {
                Long senderId = ((Number) senderMap.get("id")).longValue();
                message.isFromMe = senderId.equals(prefsManager.getUserId());
                message.senderName = (String) senderMap.get("displayName");
                message.senderAvatar = (String) senderMap.get("avatarUrl");
            }

            // Parse timestamp
            String timestampStr = (String) messageMap.get("timestamp");
            if (timestampStr != null) {
                // TODO: Parse date from string
                message.timestamp = new Date();
            }

            return message;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message", e);
            return null;
        }
    }

    // FR-4.1.2: Support text, emojis, and optional image sharing
    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty() || conversationId == -1) {
            return;
        }

        if (messageText.length() > Constants.MAX_MESSAGE_LENGTH) {
            showError("Message too long");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection");
            return;
        }

        // Disable send button
        btnSend.setEnabled(false);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("content", messageText);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getChatService()
                .sendMessage(conversationId, messageData, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleSendMessageResponse(response, messageText);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                btnSend.setEnabled(true);
                Log.e(TAG, "Failed to send message", t);
                showError("Failed to send message");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleSendMessageResponse(Response<StandardResponse<Map<String, Object>>> response, String messageText) {
        btnSend.setEnabled(true);

        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                // Clear input
                etMessage.setText("");

                // Add message to list optimistically
                MessageItem newMessage = new MessageItem();
                Map<String, Object> messageData = response.body().getData();
                if (messageData != null && messageData.get("id") != null) {
                    newMessage.id = ((Number) messageData.get("id")).longValue();
                } else {
                    newMessage.id = System.currentTimeMillis(); // Temporary ID
                }
                newMessage.content = messageText;
                newMessage.timestamp = new Date();
                newMessage.isFromMe = true;
                newMessage.isRead = false;

                messages.add(newMessage);

                // TODO: Notify adapter and scroll to bottom
                // adapter.notifyItemInserted(messages.size() - 1);
                rvMessages.scrollToPosition(messages.size() - 1);

            } else {
                String message = response.body() != null ? response.body().getMessage() : "Failed to send message";
                showError(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing send message response", e);
            showError("Failed to send message");
        }
    }

    private void updateSendButtonState() {
        String messageText = etMessage.getText().toString().trim();
        btnSend.setEnabled(!messageText.isEmpty() && conversationId != -1);
    }

    private void viewOtherUserProfile() {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra(Constants.BUNDLE_USER_ID, otherUserId);
        startActivity(intent);
    }

    private void viewProduct() {
        if (productId != -1) {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(Constants.BUNDLE_PRODUCT_ID, productId);
            startActivity(intent);
        }
    }

    private void handleLoadingError(Throwable t) {
        isLoading = false;
        Log.e(TAG, "Failed to load messages", t);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection");
        } else {
            showError(NetworkUtils.getNetworkErrorMessage(t));
        }

        if (messages.isEmpty()) {
            finish();
        }
    }

    private void showLoadingState() {
        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
    }

    private void showContentState() {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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