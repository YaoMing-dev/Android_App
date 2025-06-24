// File: app/src/main/java/com/example/newtrade/ui/chat/MessagesFragment.java
package com.example.newtrade.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ConversationAdapter;
import com.example.newtrade.models.Conversation;
import com.example.newtrade.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;

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

        Log.d(TAG, "MessagesFragment created successfully");
    }

    private void initViews(View view) {
        try {
            swipeRefresh = view.findViewById(R.id.swipe_refresh);
            rvConversations = view.findViewById(R.id.rv_conversations);
            llEmptyState = view.findViewById(R.id.ll_empty_state);
            btnStartShopping = view.findViewById(R.id.btn_start_shopping);

            Log.d(TAG, "✅ MessagesFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some MessagesFragment views not found: " + e.getMessage());
        }
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        if (rvConversations != null) {
            conversationAdapter = new ConversationAdapter(conversations, this::openConversation);
            rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
            rvConversations.setAdapter(conversationAdapter);
        }
    }

    private void setupListeners() {
        try {
            // Swipe to refresh
            if (swipeRefresh != null) {
                swipeRefresh.setOnRefreshListener(this::loadConversations);
            }

            // 🔥 START SHOPPING BUTTON
            if (btnStartShopping != null) {
                btnStartShopping.setOnClickListener(v -> startShopping());
            }

            Log.d(TAG, "✅ MessagesFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }

    private void loadConversations() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        // For now, load sample conversations since we don't have conversation API yet
        loadSampleConversations();
    }

    private void loadSampleConversations() {
        // Clear existing conversations
        conversations.clear();

        // Add sample conversations
        conversations.add(new Conversation(
                1L,
                "John Doe",
                "Hi, is the iPhone still available?",
                "2 hours ago",
                "https://example.com/avatar1.jpg",
                true,
                false
        ));

        conversations.add(new Conversation(
                2L,
                "Jane Smith",
                "Thanks for the quick delivery!",
                "1 day ago",
                "https://example.com/avatar2.jpg",
                false,
                false
        ));

        conversations.add(new Conversation(
                3L,
                "Mike Johnson",
                "Can you lower the price a bit?",
                "3 days ago",
                "https://example.com/avatar3.jpg",
                false,
                true
        ));

        // Update UI
        if (conversationAdapter != null) {
            conversationAdapter.notifyDataSetChanged();
        }

        updateEmptyState();

        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }

        Log.d(TAG, "✅ Loaded " + conversations.size() + " sample conversations");
    }

    private void updateEmptyState() {
        if (conversations.isEmpty()) {
            if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
            if (rvConversations != null) rvConversations.setVisibility(View.GONE);
        } else {
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
            if (rvConversations != null) rvConversations.setVisibility(View.VISIBLE);
        }
    }

    // 🔥 START SHOPPING - Navigate to search
    private void startShopping() {
        try {
            // Navigate to search/home to browse products
            if (getActivity() != null) {
                Toast.makeText(getContext(), "Let's start shopping! 🛍️", Toast.LENGTH_SHORT).show();

                // Switch to home tab (index 0) in bottom navigation
                // You can implement this in MainActivity later
                Log.d(TAG, "Start shopping clicked - navigate to home/search");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting shopping", e);
        }
    }

    // 🔥 SIMPLIFIED - NO CHATACTIVITY YET
    private void openConversation(Conversation conversation) {
        Toast.makeText(getContext(), "Chat with " + conversation.getOtherUserName() + " 💬", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Open conversation with: " + conversation.getOtherUserName());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh conversations when returning to fragment
        loadConversations();
    }
}