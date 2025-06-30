// app/src/main/java/com/example/newtrade/ui/chat/MessagesFragment.java
package com.example.newtrade.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;

public class MessagesFragment extends Fragment {
    private static final String TAG = "MessagesFragment";

    // UI Components
    private RecyclerView rvConversations;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadConversations();
    }

    private void initViews(View view) {
        rvConversations = view.findViewById(R.id.rv_conversations);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        tvEmpty = view.findViewById(R.id.tv_empty);
    }

    private void setupRecyclerView() {
        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        // TODO: Implement ConversationsAdapter
        // ConversationsAdapter adapter = new ConversationsAdapter(conversations, this);
        // rvConversations.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    private void loadConversations() {
        // TODO: Implement conversations loading
        Log.d(TAG, "Loading conversations - not implemented yet");
        showEmptyState();
    }

    // ✅ ADD: Missing refreshData method
    public void refreshData() {
        Log.d(TAG, "Refreshing messages data");
        loadConversations();
    }

    private void showEmptyState() {
        tvEmpty.setVisibility(View.VISIBLE);
        rvConversations.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }
}