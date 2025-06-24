// app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    // UI Components - chỉ declare những gì thực sự có trong layout
    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;
    private ImageView ivFilter;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvSearchResults;
    private LinearLayout llRecentSearches;
    private RecyclerView rvRecentSearches;
    private RecyclerView rvPopularCategories;

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
        // Chỉ init những view có thực
        try {
            tilSearch = view.findViewById(R.id.til_search);
            etSearch = view.findViewById(R.id.et_search);
           ;
            chipGroupFilters = view.findViewById(R.id.chip_group_filters);
            rvSearchResults = view.findViewById(R.id.rv_search_results);
            llRecentSearches = view.findViewById(R.id.ll_recent_searches);
            rvRecentSearches = view.findViewById(R.id.rv_recent_searches);
            rvPopularCategories = view.findViewById(R.id.rv_popular_categories);
        } catch (Exception e) {
            Log.w(TAG, "Some views not found in layout: " + e.getMessage());
        }
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
        if (ivFilter != null) {
            ivFilter.setOnClickListener(v -> {
                // TODO: Show filter bottom sheet
            });
        }

        // TODO: Setup search functionality
    }
}