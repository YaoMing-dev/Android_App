// app/src/main/java/com/example/newtrade/ui/product/ProductPreviewFragment.java
package com.example.newtrade.ui.product;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ImagePreviewAdapter;
import com.example.newtrade.models.PreviewData;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProductPreviewFragment extends Fragment {

    private static final String TAG = "ProductPreviewFragment";

    private ViewPager2 vpImages;
    private TabLayout tabIndicators;
    private TextView tvTitle, tvPrice, tvDescription, tvLocation, tvTags, tvNegotiable;
    private Chip chipCategory, chipCondition;
    private LinearLayout llTagsContainer;
    private Button btnEditListing, btnPublishNow;
    private ImageView btnBack, btnEdit; // ✅ FIXED: Changed from ImageButton to ImageView

    private ImagePreviewAdapter imagePreviewAdapter;
    private PreviewData previewData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_preview, container, false);

        initViews(view);
        loadPreviewData();
        setupImageViewer();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        // ✅ FIXED: Use ImageView instead of ImageButton
        btnBack = view.findViewById(R.id.btn_back);
        btnEdit = view.findViewById(R.id.btn_edit);

        // Images
        vpImages = view.findViewById(R.id.vp_images);
        tabIndicators = view.findViewById(R.id.tab_indicators);

        // Product info
        tvTitle = view.findViewById(R.id.tv_title);
        tvPrice = view.findViewById(R.id.tv_price);
        tvNegotiable = view.findViewById(R.id.tv_negotiable);
        chipCategory = view.findViewById(R.id.chip_category);
        chipCondition = view.findViewById(R.id.chip_condition);
        tvDescription = view.findViewById(R.id.tv_description);
        tvLocation = view.findViewById(R.id.tv_location);
        tvTags = view.findViewById(R.id.tv_tags);
        llTagsContainer = view.findViewById(R.id.ll_tags_container);

        // Action buttons
        btnEditListing = view.findViewById(R.id.btn_edit_listing);
        btnPublishNow = view.findViewById(R.id.btn_publish_now);
    }

    private void loadPreviewData() {
        if (getArguments() != null) {
            previewData = getArguments().getParcelable("preview_data");
        }

        if (previewData == null) {
            Log.e(TAG, "No preview data found");
            goBack();
            return;
        }

        // Set product information
        tvTitle.setText(previewData.getTitle());
        tvPrice.setText(formatPrice(previewData.getPrice()));
        tvDescription.setText(previewData.getDescription());
        tvLocation.setText(previewData.getLocation());

        chipCategory.setText(previewData.getCategory());
        chipCondition.setText(previewData.getCondition());

        // Handle negotiable price
        if (previewData.isNegotiable()) {
            tvNegotiable.setVisibility(View.VISIBLE);
        } else {
            tvNegotiable.setVisibility(View.GONE);
        }

        // Handle tags
        String tags = previewData.getTags();
        if (!TextUtils.isEmpty(tags)) {
            llTagsContainer.setVisibility(View.VISIBLE);
            tvTags.setText(tags);
        } else {
            llTagsContainer.setVisibility(View.GONE);
        }

        Log.d(TAG, "Preview data loaded: " + previewData.getTitle());
    }

    private void setupImageViewer() {
        if (previewData.getImageUris() == null || previewData.getImageUris().isEmpty()) {
            vpImages.setVisibility(View.GONE);
            tabIndicators.setVisibility(View.GONE);
            return;
        }

        imagePreviewAdapter = new ImagePreviewAdapter(previewData.getImageUris());
        vpImages.setAdapter(imagePreviewAdapter);

        // Setup indicators for multiple images
        if (previewData.getImageUris().size() > 1) {
            tabIndicators.setVisibility(View.VISIBLE);

            TabLayoutMediator mediator = new TabLayoutMediator(tabIndicators, vpImages,
                    (tab, position) -> {
                        // Create dot indicator - no text needed
                    });
            mediator.attach();
        } else {
            tabIndicators.setVisibility(View.GONE);
        }

        Log.d(TAG, "Image viewer setup with " + previewData.getImageUris().size() + " images");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> goBack());
        btnEdit.setOnClickListener(v -> goBack());
        btnEditListing.setOnClickListener(v -> goBack());

        btnPublishNow.setOnClickListener(v -> publishProduct());
    }

    private void goBack() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating back", e);
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }

    private void publishProduct() {
        // Return to AddProductFragment to trigger publish
        Bundle result = new Bundle();
        result.putBoolean("publish_now", true);
        getParentFragmentManager().setFragmentResult("preview_result", result);

        goBack();
    }

    private String formatPrice(String price) {
        if (price == null || price.isEmpty()) {
            return "0 VND";
        }

        try {
            long priceValue = Long.parseLong(price);
            return String.format("%,d VND", priceValue);
        } catch (NumberFormatException e) {
            return price + " VND";
        }
    }
}