// app/src/main/java/com/example/newtrade/ui/chat/MessagesFragment.java
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.Date;
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
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View loadingView, contentView, errorView;

    // Data
    private List<ConversationItem> conversations = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Data classes
    public static class ConversationItem {
        public Long id;
        public Long productId;
        public String productTitle;
        public String productImageUrl;
        public Long otherUserId;
        public String otherUserName;
        public String otherUserAvatar;
        public String lastMessage;
        public Date lastMessageTime;
        public int unreadCount;
        public boolean isActive;
        public boolean isLastMessageFromMe;

        public ConversationItem() {}
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initUtils();
        setupRecyclerView();
        setupListeners();

        loadConversations();
    }

    private void initViews(View view) {
        rvConversations = view.findViewById(R.id.rv_conversations);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        loadingView = view.findViewById(R.id.view_loading);
        contentView = view.findViewById(R.id.view_content);
        errorView = view.findViewById(R.id.view_error);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(requireContext());
    }

    private void setupRecyclerView() {
        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Pagination scroll listener
        rvConversations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && !isLastPage && dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            loadMoreConversations();
                        }
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    // FR-4.1.1: Secure chat between users (buyer/seller)
    private void loadConversations() {
        if (isLoading) return;

        isLoading = true;

        if (currentPage == 0) {
            showLoadingState();
        }

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getChatService()
                .getConversations(currentPage, Constants.DEFAULT_PAGE_SIZE, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleConversationsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleLoadingError(t);
            }
        });
    }

    private void loadMoreConversations() {
        if (isLoading || isLastPage) return;

        currentPage++;
        loadConversations();
    }

    public void refreshData() {
        Log.d(TAG, "Refreshing messages data");
        currentPage = 0;
        isLastPage = false;
        conversations.clear();
        loadConversations();
    }

    @SuppressWarnings("unchecked")
    private void handleConversationsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        isLoading = false;
        swipeRefresh.setRefreshing(false);

        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> conversationMaps = (List<Map<String, Object>>) data.get("content");

                if (conversationMaps != null) {
                    int oldSize = conversations.size();

                    for (Map<String, Object> conversationMap : conversationMaps) {
                        ConversationItem conversation = parseConversationFromMap(conversationMap);
                        if (conversation != null) {
                            conversations.add(conversation);
                        }
                    }

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : true;

                    // Update UI
                    if (conversations.isEmpty()) {
                        showEmptyState();
                    } else {
                        showContentState();
                        updateConversationsUI();
                    }
                } else {
                    if (conversations.isEmpty()) {
                        showEmptyState();
                    } else {
                        showContentState();
                    }
                }
            } else {
                handleApiError(response.body() != null ? response.body().getMessage() : "Failed to load conversations");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing conversations response", e);
            handleApiError("Error loading conversations");
        }
    }

    private ConversationItem parseConversationFromMap(Map<String, Object> conversationMap) {
        try {
            ConversationItem conversation = new ConversationItem();
            conversation.id = ((Number) conversationMap.get("id")).longValue();

            // Parse product info
            @SuppressWarnings("unchecked")
            Map<String, Object> productMap = (Map<String, Object>) conversationMap.get("product");
            if (productMap != null) {
                conversation.productId = ((Number) productMap.get("id")).longValue();
                conversation.productTitle = (String) productMap.get("title");

                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) productMap.get("imageUrls");
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    conversation.productImageUrl = imageUrls.get(0);
                }
            }

            // Parse other user info
            @SuppressWarnings("unchecked")
            Map<String, Object> otherUserMap = (Map<String, Object>) conversationMap.get("otherUser");
            if (otherUserMap != null) {
                conversation.otherUserId = ((Number) otherUserMap.get("id")).longValue();
                conversation.otherUserName = (String) otherUserMap.get("displayName");
                if (conversation.otherUserName == null) {
                    conversation.otherUserName = (String) otherUserMap.get("fullName");
                }
                conversation.otherUserAvatar = (String) otherUserMap.get("avatarUrl");
            }

            // Parse last message
            @SuppressWarnings("unchecked")
            Map<String, Object> lastMessageMap = (Map<String, Object>) conversationMap.get("lastMessage");
            if (lastMessageMap != null) {
                conversation.lastMessage = (String) lastMessageMap.get("content");
                // Parse timestamp if available
                String timestampStr = (String) lastMessageMap.get("createdAt");
                if (timestampStr != null) {
                    // TODO: Parse timestamp string to Date
                    conversation.lastMessageTime = new Date();
                }
            }

            // Parse unread count
            Object unreadCount = conversationMap.get("unreadCount");
            if (unreadCount instanceof Number) {
                conversation.unreadCount = ((Number) unreadCount).intValue();
            }

            // Parse active status
            Object isActive = conversationMap.get("isActive");
            if (isActive instanceof Boolean) {
                conversation.isActive = (Boolean) isActive;
            } else {
                conversation.isActive = true; // Default to active
            }

            // Parse last message from me status
            Object isLastMessageFromMe = conversationMap.get("isLastMessageFromMe");
            if (isLastMessageFromMe instanceof Boolean) {
                conversation.isLastMessageFromMe = (Boolean) isLastMessageFromMe;
            } else {
                conversation.isLastMessageFromMe = false; // Default to false
            }

            return conversation;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing conversation", e);
            return null;
        }
    }

    private void updateConversationsUI() {
        // TODO: Implement ConversationsAdapter
        Log.d(TAG, "Conversations loaded: " + conversations.size());

        // For now, just show a placeholder
        if (conversations.isEmpty()) {
            showEmptyState();
        } else {
            showContentState();
        }
    }

    private void onConversationClick(ConversationItem conversation) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(Constants.BUNDLE_CONVERSATION_ID, conversation.id);
        intent.putExtra(Constants.BUNDLE_PRODUCT_ID, conversation.productId);
        intent.putExtra("otherUserId", conversation.otherUserId);
        intent.putExtra("otherUserName", conversation.otherUserName);
        intent.putExtra("productTitle", conversation.productTitle);
        startActivity(intent);
    }

    private void handleLoadingError(Throwable t) {
        isLoading = false;
        swipeRefresh.setRefreshing(false);

        Log.e(TAG, "Failed to load conversations", t);

        String errorMessage = NetworkUtils.getNetworkErrorMessage(t);

        if (conversations.isEmpty()) {
            showErrorState(errorMessage);
        } else {
            showContentState();
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleApiError(String message) {
        Log.e(TAG, "API Error: " + message);

        if (conversations.isEmpty()) {
            showErrorState(message);
        } else {
            showContentState();
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoadingState() {
        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showContentState() {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        if (conversations.isEmpty()) {
            showEmptyState();
        }
    }

    private void showErrorState(String message) {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        // TODO: Set error message in error view
        Log.e(TAG, "Error state: " + message);
    }

    private void showEmptyState() {
        rvConversations.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText("No conversations yet\nStart chatting with sellers!");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh conversations when fragment becomes visible
        // This will show new messages and update read status
        if (conversations.size() > 0) {
            refreshData();
        }
    }
}