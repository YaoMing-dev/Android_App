// app/src/main/java/com/example/newtrade/ui/search/sort/SearchSortBottomSheet.java
package com.example.newtrade.ui.search.sort;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.newtrade.R;
import com.example.newtrade.ui.search.SearchFragment;
import com.example.newtrade.utils.Constants;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SearchSortBottomSheet extends BottomSheetDialogFragment {

    // UI Components
    private RadioGroup rgSort;
    private RadioButton rbRelevance, rbNewest, rbOldest, rbPriceLowHigh, rbPriceHighLow, rbNearby, rbPopular;

    // Data
    private SearchFragment.SearchSort currentSort;
    private OnSortAppliedListener listener;

    public interface OnSortAppliedListener {
        void onSortApplied(SearchFragment.SearchSort sort);
    }

    public static SearchSortBottomSheet newInstance(SearchFragment.SearchSort sort) {
        SearchSortBottomSheet fragment = new SearchSortBottomSheet();
        fragment.currentSort = sort;
        return fragment;
    }

    public void setOnSortAppliedListener(OnSortAppliedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_search_sort, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setCurrentSort();
        setupListeners();
    }

    private void initViews(View view) {
        rgSort = view.findViewById(R.id.rg_sort);
        rbRelevance = view.findViewById(R.id.rb_relevance);
        rbNewest = view.findViewById(R.id.rb_newest);
        rbOldest = view.findViewById(R.id.rb_oldest);
        rbPriceLowHigh = view.findViewById(R.id.rb_price_low_high);
        rbPriceHighLow = view.findViewById(R.id.rb_price_high_low);
        rbNearby = view.findViewById(R.id.rb_nearby);
        rbPopular = view.findViewById(R.id.rb_popular);
    }

    private void setCurrentSort() {
        if (currentSort == null) {
            rbRelevance.setChecked(true);
            return;
        }

        String sortBy = currentSort.sortBy;
        String sortDir = currentSort.sortDirection;

        if ("createdAt".equals(sortBy) && "desc".equals(sortDir)) {
            rbNewest.setChecked(true);
        } else if ("createdAt".equals(sortBy) && "asc".equals(sortDir)) {
            rbOldest.setChecked(true);
        } else if ("price".equals(sortBy) && "asc".equals(sortDir)) {
            rbPriceLowHigh.setChecked(true);
        } else if ("price".equals(sortBy) && "desc".equals(sortDir)) {
            rbPriceHighLow.setChecked(true);
        } else if ("distance".equals(sortBy) && "asc".equals(sortDir)) {
            rbNearby.setChecked(true);
        } else if ("viewCount".equals(sortBy) && "desc".equals(sortDir)) {
            rbPopular.setChecked(true);
        } else {
            rbRelevance.setChecked(true);
        }
    }

    private void setupListeners() {
        rgSort.setOnCheckedChangeListener((group, checkedId) -> {
            SearchFragment.SearchSort sort = new SearchFragment.SearchSort();

            if (checkedId == R.id.rb_newest) {
                sort.sortBy = "createdAt";
                sort.sortDirection = "desc";
            } else if (checkedId == R.id.rb_oldest) {
                sort.sortBy = "createdAt";
                sort.sortDirection = "asc";
            } else if (checkedId == R.id.rb_price_low_high) {
                sort.sortBy = "price";
                sort.sortDirection = "asc";
            } else if (checkedId == R.id.rb_price_high_low) {
                sort.sortBy = "price";
                sort.sortDirection = "desc";
            } else if (checkedId == R.id.rb_nearby) {
                sort.sortBy = "distance";
                sort.sortDirection = "asc";
            } else if (checkedId == R.id.rb_popular) {
                sort.sortBy = "viewCount";
                sort.sortDirection = "desc";
            } else {
                // Relevance - default sorting
                sort.sortBy = "relevance";
                sort.sortDirection = "desc";
            }

            // Apply sort immediately
            if (listener != null) {
                listener.onSortApplied(sort);
            }

            // Close bottom sheet after selection
            dismiss();
        });
    }
}