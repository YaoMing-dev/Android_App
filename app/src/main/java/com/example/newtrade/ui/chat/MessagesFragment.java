// app/src/main/java/com/example/newtrade/ui/chat/MessagesFragment.java
package com.example.newtrade.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;

public class MessagesFragment extends Fragment {

    private static final String TAG = "MessagesFragment";

    // UI Components - chỉ những cái cơ bản và an toàn
    private RecyclerView rvConversations;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();

        Log.d(TAG, "MessagesFragment created successfully");
    }

    private void initViews(View view) {
        // Chỉ init RecyclerView cơ bản - tránh lỗi ID không tồn tại
        try {
            rvConversations = view.findViewById(R.id.rv_conversations);
        } catch (Exception e) {
            Log.w(TAG, "Some views not found: " + e.getMessage());
        }
    }

    private void setupRecyclerView() {
        if (rvConversations != null) {
            rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }
}