// app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
package com.example.newtrade.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newtrade.R;
import com.google.android.material.card.MaterialCardView;

public class AddProductFragment extends Fragment {
    private static final String TAG = "AddProductFragment";

    // UI Components
    private MaterialCardView cardQuickSell, cardFullListing, cardBulkUpload;
    private Button btnQuickSell, btnFullListing;
    private TextView tvQuickSellDesc, tvFullListingDesc;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        cardQuickSell = view.findViewById(R.id.card_quick_sell);
        cardFullListing = view.findViewById(R.id.card_full_listing);
        cardBulkUpload = view.findViewById(R.id.card_bulk_upload);

        btnQuickSell = view.findViewById(R.id.btn_quick_sell);
        btnFullListing = view.findViewById(R.id.btn_full_listing);

        tvQuickSellDesc = view.findViewById(R.id.tv_quick_sell_desc);
        tvFullListingDesc = view.findViewById(R.id.tv_full_listing_desc);
    }

    private void setupListeners() {
        cardQuickSell.setOnClickListener(v -> openQuickSell());
        btnQuickSell.setOnClickListener(v -> openQuickSell());

        cardFullListing.setOnClickListener(v -> openFullListing());
        btnFullListing.setOnClickListener(v -> openFullListing());

        cardBulkUpload.setOnClickListener(v -> openBulkUpload());
    }

    private void openQuickSell() {
        Intent intent = new Intent(requireContext(), AddProductActivity.class);
        intent.putExtra("quickMode", true);
        startActivity(intent);
    }

    private void openFullListing() {
        Intent intent = new Intent(requireContext(), AddProductActivity.class);
        intent.putExtra("quickMode", false);
        startActivity(intent);
    }

    private void openBulkUpload() {
        // TODO: Implement bulk upload functionality
        // Could open a different activity for bulk operations
    }
}