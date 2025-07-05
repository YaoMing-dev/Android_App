// app/src/main/java/com/example/newtrade/ui/chat/MessagesFragment.java
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private RecyclerView rvConversations;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout llEmptyState;
    private ImageButton btnNewMessage;

    // Data
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations;
    private SharedPrefsManager prefsManager;
    private boolean isLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupButtons();
        loadConversations();

        Log.d(TAG, "✅ MessagesFragment created successfully");
    }

    private void initViews(View view) {
        rvConversations = view.findViewById(R.id.rv_conversations);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        btnNewMessage = view.findViewById(R.id.btn_new_message);

        // Initialize SharedPrefsManager
        prefsManager = SharedPrefsManager.getInstance(requireContext());

        // Initialize conversations list
        conversations = new ArrayList<>();
    }

    private void setupRecyclerView() {
        // ✅ FIX: Sử dụng constructor đúng với context
        conversationAdapter = new ConversationAdapter(requireContext(), conversations, this::openChatActivity);
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConversations.setAdapter(conversationAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadConversations);
    }

    private void setupButtons() {
        if (btnNewMessage != null) {
            btnNewMessage.setOnClickListener(v -> {
                // TODO: Implement new message functionality
                showToast("New message feature coming soon!");
            });
        }
    }

    private void loadConversations() {
        if (isLoading) return;

        isLoading = true;
        swipeRefresh.setRefreshing(true);

        Long userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "❌ User not logged in");
            showToast("Please login to view messages");
            isLoading = false;
            swipeRefresh.setRefreshing(false);
            return;
        }

        // ✅ FIX: Sử dụng ChatService thay vì ApiService
        Call<StandardResponse<Map<String, Object>>> call =
                ApiClient.getChatService().getConversations(userId, 0, 20);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleConversationsResponse(response.body());
                } else {
                    Log.e(TAG, "❌ Failed to load conversations: " + response.code());
                    createMockConversations(); // Fallback to mock data
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "❌ Network error loading conversations", t);
                createMockConversations(); // Fallback to mock data
            }
        });
    }

    private void handleConversationsResponse(StandardResponse<Map<String, Object>> response) {
        try {
            if (response.isSuccess() && response.getData() != null) {
                // Parse conversations from response
                Map<String, Object> data = response.getData();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> conversationMaps = (List<Map<String, Object>>) data.get("conversations");

                conversations.clear();
                if (conversationMaps != null) {
                    for (Map<String, Object> convMap : conversationMaps) {
                        Conversation conversation = parseConversation(convMap);
                        if (conversation != null) {
                            conversations.add(conversation);
                        }
                    }
                }

                updateUI();
                Log.d(TAG, "✅ Loaded " + conversations.size() + " conversations");
            } else {
                Log.w(TAG, "⚠️ Empty response or failed status");
                createMockConversations();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing conversations", e);
            createMockConversations();
        }
    }

    private Conversation parseConversation(Map<String, Object> convMap) {
        try {
            Conversation conversation = new Conversation();

            // Parse conversation data from map
            Object idObj = convMap.get("id");
            if (idObj instanceof Number) {
                conversation.setId(((Number) idObj).longValue());
            }

            conversation.setOtherUserName((String) convMap.get("otherUserName"));
            conversation.setLastMessage((String) convMap.get("lastMessage"));
            conversation.setLastMessageTime((String) convMap.get("lastMessageTime"));
            conversation.setProductTitle((String) convMap.get("productTitle"));

            Object unreadObj = convMap.get("unreadCount");
            if (unreadObj instanceof Number) {
                conversation.setUnreadCount(((Number) unreadObj).intValue());
            }

            return conversation;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing conversation", e);
            return null;
        }
    }

    private void createMockConversations() {
        conversations.clear();

        // Mock conversation 1
        Conversation mockConversation1 = new Conversation();
        mockConversation1.setId(1L);
        mockConversation1.setOtherUserName("Nguyễn Văn A");
        mockConversation1.setLastMessage("Hello, is this still available?");
        mockConversation1.setLastMessageTime("10 phút trước");
        mockConversation1.setProductTitle("iPhone 14 Pro 128GB Deep Purple");
        mockConversation1.setUnreadCount(1);
        conversations.add(mockConversation1);

        // Mock conversation 2
        Conversation mockConversation2 = new Conversation();
        mockConversation2.setId(2L);
        mockConversation2.setOtherUserName("Trần Thị Lan");
        mockConversation2.setLastMessage("Em lấy 55 triệu được không chị?");
        mockConversation2.setLastMessageTime("6 giờ trước");
        mockConversation2.setProductTitle("MacBook Pro M2 16 inch 512GB");
        mockConversation2.setUnreadCount(0);
        conversations.add(mockConversation2);

        updateUI();
        Log.d(TAG, "✅ Mock conversations created: " + conversations.size());
    }

    private void updateUI() {
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
            intent.putExtra("product_id", conversation.getProductId());
            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to open chat", e);
            showToast("Failed to open chat");
        }
    }

    @Nullable
    private Long getCurrentUserId() {
        try {
            Long userId = prefsManager.getUserId();
            return (userId != null && userId > 0) ? userId : null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID", e);
            return null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh conversations when returning to fragment
        loadConversations();
    }
}