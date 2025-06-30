// app/src/main/java/com/example/newtrade/ui/search/filter/SearchFilterBottomSheet.java
package com.example.newtrade.ui.search.filter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.newtrade.R;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.ui.search.SearchFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class SearchFilterBottomSheet extends BottomSheetDialogFragment {

    // UI Components
    private AutoCompleteTextView actvCategory, actvCondition;
    private EditText etMinPrice, etMaxPrice;
    private SeekBar seekDistance;
    private TextView tvDistanceValue;
    private Button btnClear, btnApply;

    // Data
    private SearchFragment.SearchFilter currentFilter;
    private List<Category> categories;
    private OnFilterAppliedListener listener;

    public interface OnFilterAppliedListener {
        void onFilterApplied(SearchFragment.SearchFilter filter);
    }

    public static SearchFilterBottomSheet newInstance(SearchFragment.SearchFilter filter, List<Category> categories) {
        SearchFilterBottomSheet fragment = new SearchFilterBottomSheet();
        fragment.currentFilter = new SearchFragment.SearchFilter(filter);
        fragment.categories = categories;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_search_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupCategories();
        setupConditions();
        setupDistance();
        populateCurrentFilter();
        setupListeners();
    }

    private void initViews(View view) {
        actvCategory = view.findViewById(R.id.actv_category);
        actvCondition = view.findViewById(R.id.actv_condition);
        etMinPrice = view.findViewById(R.id.et_min_price);
        etMaxPrice = view.findViewById(R.id.et_max_price);
        seekDistance = view.findViewById(R.id.seek_distance);
        tvDistanceValue = view.findViewById(R.id.tv_distance_value);
        btnClear = view.findViewById(R.id.btn_clear);
        btnApply = view.findViewById(R.id.btn_apply);
    }

    private void setupCategories() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All Categories");

        if (categories != null) {
            for (Category category : categories) {
                categoryNames.add(category.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(adapter);
    }

    private void setupConditions() {
        List<String> conditionNames = new ArrayList<>();
        conditionNames.add("Any Condition");

        for (Product.ProductCondition condition : Product.ProductCondition.values()) {
            conditionNames.add(condition.getDisplayName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, conditionNames);
        actvCondition.setAdapter(adapter);
    }

    private void setupDistance() {
        seekDistance.setMax(100); // 100km max
        seekDistance.setProgress(50); // Default 50km

        seekDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) progress = 1;
                tvDistanceValue.setText(progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void populateCurrentFilter() {
        if (currentFilter == null) return;

        // Category
        if (currentFilter.categoryId != null && categories != null) {
            for (Category category : categories) {
                if (category.getId().equals(currentFilter.categoryId)) {
                    actvCategory.setText(category.getName());
                    break;
                }
            }
        } else {
            actvCategory.setText("All Categories");
        }

        // Condition
        if (currentFilter.condition != null) {
            try {
                Product.ProductCondition condition = Product.ProductCondition.valueOf(currentFilter.condition);
                actvCondition.setText(condition.getDisplayName());
            } catch (IllegalArgumentException e) {
                actvCondition.setText("Any Condition");
            }
        } else {
            actvCondition.setText("Any Condition");
        }

        // Price range
        if (currentFilter.minPrice != null) {
            etMinPrice.setText(String.valueOf(currentFilter.minPrice.intValue()));
        }
        if (currentFilter.maxPrice != null) {
            etMaxPrice.setText(String.valueOf(currentFilter.maxPrice.intValue()));
        }

        // Distance
        if (currentFilter.radius != null) {
            seekDistance.setProgress(currentFilter.radius);
            tvDistanceValue.setText(currentFilter.radius + " km");
        } else {
            seekDistance.setProgress(50);
            tvDistanceValue.setText("50 km");
        }
    }

    private void setupListeners() {
        btnClear.setOnClickListener(v -> clearAllFilters());
        btnApply.setOnClickListener(v -> applyFilters());
    }

    private void clearAllFilters() {
        actvCategory.setText("All Categories");
        actvCondition.setText("Any Condition");
        etMinPrice.setText("");
        etMaxPrice.setText("");
        seekDistance.setProgress(50);
        tvDistanceValue.setText("50 km");
    }

    private void applyFilters() {
        SearchFragment.SearchFilter filter = new SearchFragment.SearchFilter();

        // Category
        String categoryText = actvCategory.getText().toString();
        if (!categoryText.equals("All Categories") && categories != null) {
            for (Category category : categories) {
                if (category.getName().equals(categoryText)) {
                    filter.categoryId = category.getId();
                    break;
                }
            }
        }

        // Condition
        String conditionText = actvCondition.getText().toString();
        if (!conditionText.equals("Any Condition")) {
            for (Product.ProductCondition condition : Product.ProductCondition.values()) {
                if (condition.getDisplayName().equals(conditionText)) {
                    filter.condition = condition.name();
                    break;
                }
            }
        }

        // Price range
        String minPriceText = etMinPrice.getText().toString().trim();
        if (!minPriceText.isEmpty()) {
            try {
                filter.minPrice = Double.parseDouble(minPriceText);
            } catch (NumberFormatException e) {
                // Ignore invalid price
            }
        }

        String maxPriceText = etMaxPrice.getText().toString().trim();
        if (!maxPriceText.isEmpty()) {
            try {
                filter.maxPrice = Double.parseDouble(maxPriceText);
            } catch (NumberFormatException e) {
                // Ignore invalid price
            }
        }

        // Distance
        filter.radius = seekDistance.getProgress();
        if (filter.radius < 1) filter.radius = 1;

        // Keep existing location if available
        if (currentFilter != null) {
            filter.latitude = currentFilter.latitude;
            filter.longitude = currentFilter.longitude;
        }

        if (listener != null) {
            listener.onFilterApplied(filter);
        }

        dismiss();
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }
}