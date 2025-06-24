package com.example.newtrade.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    // UI Components
    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private LinearLayout llRecentSearches;
    private RecyclerView rvRecentSearches;
    private RecyclerView rvPopularCategories;
    private FrameLayout flLoadingState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupListeners();

        Log.d(TAG, "SearchFragment created successfully");
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        llRecentSearches = view.findViewById(R.id.ll_recent_searches);
        rvRecentSearches = view.findViewById(R.id.rv_recent_searches);
        rvPopularCategories = view.findViewById(R.id.rv_popular_categories);
        flLoadingState = view.findViewById(R.id.fl_loading_state);
    }

    private void setupRecyclerViews() {
        if (rvSearchResults != null) {
            rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        if (rvRecentSearches != null) {
            rvRecentSearches.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        if (rvPopularCategories != null) {
            rvPopularCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }
    }

    private void setupListeners() {
        // TODO: Setup search functionality
    }
}