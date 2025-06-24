// app/src/main/java/com/example/newtrade/ui/search/SearchFragment.java
package com.example.newtrade.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        // Chỉ init những view có thực với error handling
        try {
            tilSearch = view.findViewById(R.id.til_search);
            etSearch = view.findViewById(R.id.et_search);

            chipGroupFilters = view.findViewById(R.id.chip_group_filters);
            rvSearchResults = view.findViewById(R.id.rv_search_results);
            llRecentSearches = view.findViewById(R.id.ll_recent_searches);
            rvRecentSearches = view.findViewById(R.id.rv_recent_searches);
            rvPopularCategories = view.findViewById(R.id.rv_popular_categories);

            Log.d(TAG, "✅ SearchFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some SearchFragment views not found: " + e.getMessage());
        }
    }

    private void setupRecyclerViews() {
        try {
            if (rvSearchResults != null) {
                rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
            }

            if (rvRecentSearches != null) {
                rvRecentSearches.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }

            if (rvPopularCategories != null) {
                rvPopularCategories.setLayoutManager(new LinearLayoutManager(getContext()));
            }

            Log.d(TAG, "✅ SearchFragment RecyclerViews setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up RecyclerViews", e);
        }
    }

    private void setupListeners() {
        try {
            if (ivFilter != null) {
                ivFilter.setOnClickListener(v -> {
                    // TODO: Show filter dialog
                    Log.d(TAG, "Filter clicked");
                });
            }

            if (etSearch != null) {
                // TODO: Add text change listener for search
            }

            Log.d(TAG, "✅ SearchFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }
}