// app/src/main/java/com/example/newtrade/ui/chat/MessagesFragment.java
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ConversationAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Conversation;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagesFragment extends Fragment {

    private static final String TAG = "MessagesFragment";

    // UI Components
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvConversations;
    private LinearLayout llEmptyState;
    private Button btnStartShopping;

    // Data
    private ConversationAdapter conversationAdapter;
    private final List<Conversation> conversations = new ArrayList<>();
    private SharedPrefsManager prefsManager;
    private Long currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initData();
        setupRecyclerView();
        setupListeners();
        loadConversations();

        Log.d(TAG, "✅ MessagesFragment created");
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        rvConversations = view.findViewById(R.id.rv_conversations);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        btnStartShopping = view.findViewById(R.id.btn_start_shopping);
    }

    private void initData() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        currentUserId = getCurrentUserId();

        Log.d(TAG, "Current user ID: " + currentUserId);
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(conversations, this::openChatActivity);
        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvConversations.setAdapter(conversationAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadConversations);

        if (btnStartShopping != null) {
            btnStartShopping.setOnClickListener(v -> {
                // Navigate to home or search
                Toast.makeText(requireContext(), "Let's start shopping!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadConversations() {
        swipeRefresh.setRefreshing(true);

        if (currentUserId == null) {
            Log.w(TAG, "⚠️ No user ID, loading mock conversations");
            loadMockConversations();
            return;
        }

        Log.d(TAG, "📋 Loading conversations for user: " + currentUserId);

        // ✅ FIX: Try to load real conversations first, fallback to mock
        loadConversationsFromAPI();
    }

    private void loadConversationsFromAPI() {
        // TODO: Implement when conversation API is ready
        // For now, test with health check then fallback to mock
        testAPIThenLoadMock();

        /*
        ApiClient.getApiService().getUserConversations(currentUserId)
                .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                         Response<StandardResponse<List<Map<String, Object>>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<Map<String, Object>> conversationData = response.body().getData();
                            updateConversationsFromData(conversationData);
                        } else {
                            Log.w(TAG, "⚠️ API response unsuccessful, loading mock conversations");
                            loadMockConversations();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load conversations", t);
                        loadMockConversations();
                    }
                });
        */
    }

    private void testAPIThenLoadMock() {
        ApiClient.getAuthService().healthCheck()
                .enqueue(new Callback<StandardResponse<String>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<String>> call,
                                           Response<StandardResponse<String>> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "✅ Backend connected, but conversation API not implemented yet");
                        } else {
                            Log.w(TAG, "⚠️ Backend connection issues");
                        }
                        loadMockConversations();
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                        Log.e(TAG, "❌ Backend connection failed: " + t.getMessage());
                        loadMockConversations();
                    }
                });
    }

    private void loadMockConversations() {
        conversations.clear();

        // Mock conversation 1 - Based on sample data
        Conversation mockConversation1 = new Conversation();
        mockConversation1.setId(1L);
        mockConversation1.setOtherUserName("Nguyễn Văn Anh");
        mockConversation1.setLastMessage("Máy còn nguyên seal không anh?");
        mockConversation1.setLastMessageTime("10 phút trước");
        mockConversation1.setProductTitle("iPhone 14 Pro 128GB Deep Purple");
        mockConversation1.setUnreadCount(1);
        conversations.add(mockConversation1);

        // Mock conversation 2
        Conversation mockConversation2 = new Conversation();
        mockConversation2.setId(4L);
        mockConversation2.setOtherUserName("Trần Thị Lan");
        mockConversation2.setLastMessage("Em lấy 55 triệu được không chị?");
        mockConversation2.setLastMessageTime("6 giờ trước");
        mockConversation2.setProductTitle("MacBook Pro M2 16 inch 512GB");
        mockConversation2.setUnreadCount(0);
        conversations.add(mockConversation2);

        // Mock conversation 3
        Conversation mockConversation3 = new Conversation();
        mockConversation3.setId(5L);
        mockConversation3.setOtherUserName("Lê Hoàng Nam");
        mockConversation3.setLastMessage("Được rồi chị, em lấy luôn!");
        mockConversation3.setLastMessageTime("3 ngày trước");
        mockConversation3.setProductTitle("Túi Chanel Classic Medium");
        mockConversation3.setUnreadCount(0);
        conversations.add(mockConversation3);

        updateUI();
        Log.d(TAG, "✅ Mock conversations created: " + conversations.size());
    }

    private void updateUI() {
        swipeRefresh.setRefreshing(false);

        if (conversationAdapter != null) {
            conversationAdapter.notifyDataSetChanged();
        }

        if (conversations.isEmpty()) {
            showEmptyState();
        } else {
            showConversations();
        }
    }

    private void showConversations() {
        if (rvConversations != null) rvConversations.setVisibility(View.VISIBLE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        if (rvConversations != null) rvConversations.setVisibility(View.GONE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
    }

    private void openChatActivity(Conversation conversation) {
        try {
            Log.d(TAG, "✅ Opening chat for conversation: " + conversation.getId());

            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("conversation_id", conversation.getId());
            intent.putExtra("other_user_name", conversation.getOtherUserName());
            intent.putExtra("product_title", conversation.getProductTitle());
            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to open chat", e);
            Toast.makeText(getContext(), "Failed to open chat", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private Long getCurrentUserId() {
        try {
            Long userId = prefsManager.getUserId();
            return (userId != null && userId > 0) ? userId : null;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting user ID", e);
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh conversations when fragment becomes visible
        loadConversations();
    }
}