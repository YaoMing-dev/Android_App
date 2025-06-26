// app/src/main/java/com/example/newtrade/ui/chat/ChatActivity.java
// ✅ FIXED: Sửa lỗi SharedPrefsManager initialization
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Message;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvProductInfo;

    // Data
    private MessageAdapter messageAdapter;
    private final List<Message> messages = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    private Long conversationId;
    private Long productId;
    private String productTitle;
    private Long sellerId;
    private Long currentUserId;
    private String otherUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadMessages();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        conversationId = intent.getLongExtra("conversation_id", -1);
        productId = intent.getLongExtra("product_id", -1);
        productTitle = intent.getStringExtra("product_title");
        sellerId = intent.getLongExtra("seller_id", -1);
        otherUserName = intent.getStringExtra("other_user_name");

        // ✅ FIX: Proper SharedPrefsManager initialization
        prefsManager = SharedPrefsManager.getInstance(this);
        currentUserId = prefsManager.getUserId();

        Log.d(TAG, "Chat data - Conversation: " + conversationId + ", Product: " + productTitle);
        Log.d(TAG, "Current User: " + currentUserId + ", Seller: " + sellerId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        tvProductInfo = findViewById(R.id.tv_product_info);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(otherUserName != null ? otherUserName : "Chat");
        }

        if (tvProductInfo != null && productTitle != null) {
            tvProductInfo.setText("Về: " + productTitle);
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

    private void setupRecyclerView() {
        // ✅ FIX: Check if currentUserId is valid
        if (currentUserId == null || currentUserId <= 0) {
            Log.e(TAG, "❌ Invalid current user ID: " + currentUserId);
            Toast.makeText(this, "Lỗi: Không thể xác định người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messageAdapter = new MessageAdapter(messages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSend.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        Log.d(TAG, "Loading messages for conversation: " + conversationId);

        // ✅ FIX: Add mock messages for testing
        addMockMessages();
    }

    // ✅ FIX: Add mock messages for testing
    private void addMockMessages() {
        Message msg1 = new Message();
        msg1.setId(1L);
        msg1.setSenderId(sellerId);
        msg1.setContent("Xin chào! Bạn quan tâm đến sản phẩm này?");
        msg1.setTimestamp("2 phút trước");
        messages.add(msg1);

        Message msg2 = new Message();
        msg2.setId(2L);
        msg2.setSenderId(currentUserId);
        msg2.setContent("Chào bạn! Có thể cho tôi biết thêm chi tiết không?");
        msg2.setTimestamp("1 phút trước");
        messages.add(msg2);

        messageAdapter.notifyDataSetChanged();
        rvMessages.scrollToPosition(messages.size() - 1);
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        // ✅ FIX: Add message to UI immediately
        Message newMessage = new Message();
        newMessage.setId(System.currentTimeMillis());
        newMessage.setSenderId(currentUserId);
        newMessage.setContent(messageText);
        newMessage.setTimestamp("Vừa xong");

        messages.add(newMessage);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);

        etMessage.setText("");

        Log.d(TAG, "Message sent: " + messageText);
        Toast.makeText(this, "Tin nhắn đã gửi (demo)", Toast.LENGTH_SHORT).show();

        // TODO: Implement actual API call to send message
        // sendMessageToServer(messageText);
    }

    // ✅ FIX: Add method for future API integration
    private void sendMessageToServer(String messageText) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("conversationId", conversationId);
        messageData.put("content", messageText);
        messageData.put("senderId", currentUserId);

        // TODO: Implement actual API call
        Log.d(TAG, "Sending message to server: " + messageText);
    }
}