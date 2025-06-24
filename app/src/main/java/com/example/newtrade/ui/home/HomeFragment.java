// app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
package com.example.newtrade.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // UI Components
    private RecyclerView rvCategories;
    private RecyclerView rvNearbyProducts;
    private RecyclerView rvAllProducts;
    private TextView tvViewAllCategories;
    private TextView tvViewAllNearby;
    private TextView tvSortBy;
    private FloatingActionButton fabQuickAdd;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupListeners();

        Log.d(TAG, "HomeFragment created successfully");
    }

    private void initViews(View view) {
        rvCategories = view.findViewById(R.id.rv_categories);
        rvNearbyProducts = view.findViewById(R.id.rv_nearby_products);
        rvAllProducts = view.findViewById(R.id.rv_all_products);
        tvViewAllCategories = view.findViewById(R.id.tv_view_all_categories);
        tvViewAllNearby = view.findViewById(R.id.tv_view_all_nearby);
        tvSortBy = view.findViewById(R.id.tv_sort_by);
        fabQuickAdd = view.findViewById(R.id.fab_quick_add);
    }

    private void setupRecyclerViews() {
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        if (rvNearbyProducts != null) {
            rvNearbyProducts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        if (rvAllProducts != null) {
            rvAllProducts.setLayoutManager(new LinearLayoutManager(getContext()));
            rvAllProducts.setNestedScrollingEnabled(false);
        }
    }

    private void setupListeners() {
        if (fabQuickAdd != null) {
            fabQuickAdd.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.nav_add_product);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation failed", e);
                }
            });
        }

        if (tvViewAllCategories != null) {
            tvViewAllCategories.setOnClickListener(v -> {
                // TODO: Navigate to all categories
            });
        }

        if (tvViewAllNearby != null) {
            tvViewAllNearby.setOnClickListener(v -> {
                // TODO: Navigate to nearby products
            });
        }

        if (tvSortBy != null) {
            tvSortBy.setOnClickListener(v -> {
                // TODO: Show sort options
            });
        }
    }
}