// app/src/main/java/com/example/newtrade/ui/chat/MessagesFragment.java
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.newtrade.api.ApiService;
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
        loadConversations();

        Log.d(TAG, "✅ MessagesFragment created");
    }

    private void initViews(View view) {
        rvConversations = view.findViewById(R.id.rv_conversations);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        llEmptyState = view.findViewById(R.id.ll_empty_state);

        prefsManager = SharedPrefsManager.getInstance(requireContext());
        conversations = new ArrayList<>();
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(requireContext(), conversations, this::openChatActivity);
        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvConversations.setAdapter(conversationAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadConversations);
        swipeRefresh.setColorSchemeResources(
                R.color.primary_color,
                R.color.primary_dark
        );
    }

    // ✅ NEW: Load conversations from API
    private void loadConversations() {
        if (isLoading) return;

        Long userId = getCurrentUserId();
        if (userId == null) {
            showError("Please login to view messages");
            return;
        }

        isLoading = true;
        swipeRefresh.setRefreshing(true);

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.getConversations(0, 20);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                isLoading = false;
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        Map<String, Object> pageData = standardResponse.getData();
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> conversationList = (List<Map<String, Object>>) pageData.get("content");

                        if (conversationList != null) {
                            conversations.clear();
                            for (Map<String, Object> conversationData : conversationList) {
                                Conversation conversation = parseConversationFromApi(conversationData, userId);
                                if (conversation != null) {
                                    conversations.add(conversation);
                                }
                            }

                            updateUI();
                            Log.d(TAG, "✅ Loaded " + conversations.size() + " conversations");

                        } else {
                            Log.e(TAG, "Conversation list is null");
                            createMockConversations(); // Fallback
                        }

                    } else {
                        Log.e(TAG, "Failed to load conversations: " + standardResponse.getMessage());
                        createMockConversations(); // Fallback
                    }
                } else {
                    Log.e(TAG, "Failed to load conversations: HTTP " + response.code());
                    createMockConversations(); // Fallback
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Error loading conversations", t);
                createMockConversations(); // Fallback
            }
        });
    }

    // ✅ NEW: Parse conversation from API response
    private Conversation parseConversationFromApi(Map<String, Object> conversationData, Long currentUserId) {
        try {
            Conversation conversation = new Conversation();

            if (conversationData.get("id") != null) {
                conversation.setId(((Number) conversationData.get("id")).longValue());
            }
            if (conversationData.get("productId") != null) {
                conversation.setProductId(((Number) conversationData.get("productId")).longValue());
            }
            if (conversationData.get("productTitle") != null) {
                conversation.setProductTitle(conversationData.get("productTitle").toString());
            }
            if (conversationData.get("buyerId") != null) {
                conversation.setBuyerId(((Number) conversationData.get("buyerId")).longValue());
            }
            if (conversationData.get("sellerId") != null) {
                conversation.setSellerId(((Number) conversationData.get("sellerId")).longValue());
            }
            if (conversationData.get("buyerName") != null) {
                conversation.setBuyerName(conversationData.get("buyerName").toString());
            }
            if (conversationData.get("sellerName") != null) {
                conversation.setSellerName(conversationData.get("sellerName").toString());
            }
            if (conversationData.get("lastMessage") != null) {
                conversation.setLastMessage(conversationData.get("lastMessage").toString());
            }
            if (conversationData.get("lastMessageTime") != null) {
                conversation.setLastMessageTime(conversationData.get("lastMessageTime").toString());
            }
            if (conversationData.get("unreadCount") != null) {
                conversation.setUnreadCount(((Number) conversationData.get("unreadCount")).intValue());
            }

            // Setup other user info
            conversation.setupOtherUserInfo(currentUserId);

            return conversation;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing conversation from API", e);
            return null;
        }
    }

    private void createMockConversations() {
        conversations.clear();

        // Mock conversation 1
        Conversation mockConversation1 = new Conversation();
        mockConversation1.setId(3L);
        mockConversation1.setOtherUserName("Nguyễn Văn An");
        mockConversation1.setLastMessage("Chào bạn, sản phẩm còn không?");
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
            showError("Failed to open chat");
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

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh conversations when returning to fragment
        loadConversations();
    }
}