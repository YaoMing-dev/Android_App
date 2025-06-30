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
import com.example.newtrade.ui.chat.adapter.ConversationsAdapter;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagesFragment extends Fragment implements ConversationsAdapter.OnConversationClickListener {
    private static final String TAG = "MessagesFragment";

    // UI Components
    private RecyclerView rvConversations;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    // Data and Adapter
    private ConversationsAdapter adapter;
    private List<ConversationItem> conversations = new ArrayList<>();

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Utils
    private SharedPrefsManager prefsManager;

    public static class ConversationItem {
        public Long id;
        public String productTitle;
        public String productImage;
        public String lastMessage;
        public String lastMessageTime;
        public String otherUserName;
        public String otherUserAvatar;
        public boolean isRead;
        public int unreadCount;

        // Constructor
        public ConversationItem() {}
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = new SharedPrefsManager(requireContext());

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadConversations();
    }

    private void initViews(View view) {
        rvConversations = view.findViewById(R.id.rv_conversations);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
    }

    private void setupRecyclerView() {
        adapter = new ConversationsAdapter(conversations, this);
        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvConversations.setAdapter(adapter);

        // Pagination scroll listener
        rvConversations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreConversations();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    private void loadConversations() {
        if (isLoading) return;

        isLoading = true;
        showLoading(currentPage == 0);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getChatService()
                .getConversations(currentPage, Constants.DEFAULT_PAGE_SIZE, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;
                hideLoading();

                handleConversationsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                isLoading = false;
                hideLoading();

                Log.e(TAG, "Failed to load conversations", t);
                if (conversations.isEmpty()) {
                    showEmptyState();
                }
                Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleConversationsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> conversationMaps = (List<Map<String, Object>>) data.get("content");

                if (conversationMaps != null) {
                    List<ConversationItem> newConversations = new ArrayList<>();
                    for (Map<String, Object> convMap : conversationMaps) {
                        ConversationItem conversation = parseConversationFromMap(convMap);
                        if (conversation != null) {
                            newConversations.add(conversation);
                        }
                    }

                    int oldSize = conversations.size();
                    conversations.addAll(newConversations);
                    adapter.notifyItemRangeInserted(oldSize, newConversations.size());

                    // Update pagination
                    Boolean isLast = (Boolean) data.get("last");
                    isLastPage = isLast != null ? isLast : newConversations.size() < Constants.DEFAULT_PAGE_SIZE;
                }

                if (conversations.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                }

            } else {
                Log.e(TAG, "Failed to load conversations: " + response.message());
                if (conversations.isEmpty()) {
                    showEmptyState();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing conversations response", e);
            if (conversations.isEmpty()) {
                showEmptyState();
            }
        }
    }

    private ConversationItem parseConversationFromMap(Map<String, Object> convMap) {
        try {
            ConversationItem conversation = new ConversationItem();
            conversation.id = getLongFromMap(convMap, "id");

            // Parse product info
            Object productObj = convMap.get("product");
            if (productObj instanceof Map) {
                Map<String, Object> productMap = (Map<String, Object>) productObj;
                conversation.productTitle = (String) productMap.get("title");
                // TODO: Parse product images
            }

            conversation.lastMessage = (String) convMap.get("lastMessage");
            conversation.lastMessageTime = (String) convMap.get("lastMessageTime");

            // Parse other user info
            Object otherUserObj = convMap.get("otherUser");
            if (otherUserObj instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) otherUserObj;
                conversation.otherUserName = (String) userMap.get("displayName");
                conversation.otherUserAvatar = (String) userMap.get("avatarUrl");
            }

            Object isReadObj = convMap.get("isRead");
            conversation.isRead = isReadObj instanceof Boolean ? (Boolean) isReadObj : true;

            Object unreadCountObj = convMap.get("unreadCount");
            if (unreadCountObj instanceof Number) {
                conversation.unreadCount = ((Number) unreadCountObj).intValue();
            }

            return conversation;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing conversation from map", e);
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

    private void loadMoreConversations() {
        if (!isLoading && !isLastPage) {
            currentPage++;
            loadConversations();
        }
    }

    public void refreshData() {
        currentPage = 0;
        isLastPage = false;
        conversations.clear();
        adapter.notifyDataSetChanged();
        loadConversations();
    }

    private void showLoading(boolean isInitialLoad) {
        if (isInitialLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
        swipeRefresh.setRefreshing(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void showEmptyState() {
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText("No messages yet");
    }

    private void hideEmptyState() {
        tvEmpty.setVisibility(View.GONE);
    }

    // ConversationsAdapter.OnConversationClickListener implementation
    @Override
    public void onConversationClick(ConversationItem conversation) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(Constants.BUNDLE_CONVERSATION_ID, conversation.id);
        intent.putExtra("productTitle", conversation.productTitle);
        intent.putExtra("otherUserName", conversation.otherUserName);
        startActivity(intent);
    }
}