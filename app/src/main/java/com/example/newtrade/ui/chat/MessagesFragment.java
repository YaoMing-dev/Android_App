// File: app/src/main/java/com/example/newtrade/ui/chat/MessagesFragment.java
package com.example.newtrade.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.google.android.material.button.MaterialButton;

public class MessagesFragment extends Fragment {

    private static final String TAG = "MessagesFragment";

    // UI Components
    private RecyclerView rvConversations;
    private LinearLayout llEmptyState, llLoading;
    private MaterialButton btnStartShopping;

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
        showEmptyState();

        Log.d(TAG, "MessagesFragment created successfully");
    }

    private void initViews(View view) {
        try {
            rvConversations = view.findViewById(R.id.rv_conversations);
            llEmptyState = view.findViewById(R.id.ll_empty_state);
            llLoading = view.findViewById(R.id.ll_loading);
            btnStartShopping = view.findViewById(R.id.btn_start_shopping);

            Log.d(TAG, "✅ MessagesFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some MessagesFragment views not found: " + e.getMessage());
        }
    }

    private void setupRecyclerView() {
        try {
            if (rvConversations != null) {
                rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
                // TODO: Set adapter when conversation feature is implemented
            }

            Log.d(TAG, "✅ MessagesFragment RecyclerView setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up RecyclerView", e);
        }
    }

    private void setupListeners() {
        try {
            // 🔥 START SHOPPING BUTTON HOẠT ĐỘNG
            if (btnStartShopping != null) {
                btnStartShopping.setOnClickListener(v -> {
                    Log.d(TAG, "Start Shopping button clicked");
                    navigateToHome();
                });
            }

            Log.d(TAG, "✅ MessagesFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }

    private void navigateToHome() {
        try {
            if (getActivity() != null) {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.nav_home);
                Toast.makeText(getContext(), "🛍️ Happy shopping!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Navigation error", e);
            Toast.makeText(getContext(), "🛍️ Let's start shopping!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyState() {
        try {
            if (rvConversations != null) {
                rvConversations.setVisibility(View.GONE);
            }

            if (llLoading != null) {
                llLoading.setVisibility(View.GONE);
            }

            if (llEmptyState != null) {
                llEmptyState.setVisibility(View.VISIBLE);
            }

            Log.d(TAG, "✅ Empty state shown");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing empty state", e);
        }
    }

    private void showLoadingState() {
        if (llLoading != null) {
            llLoading.setVisibility(View.VISIBLE);
        }
        if (rvConversations != null) {
            rvConversations.setVisibility(View.GONE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showConversations() {
        if (rvConversations != null) {
            rvConversations.setVisibility(View.VISIBLE);
        }
        if (llLoading != null) {
            llLoading.setVisibility(View.GONE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
    }

    // TODO: Load conversations from backend
    private void loadConversations() {
        showLoadingState();

        // For now, just show empty state
        // In the future, load from ConversationService
        showEmptyState();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh conversations when fragment becomes visible
        loadConversations();
    }
}