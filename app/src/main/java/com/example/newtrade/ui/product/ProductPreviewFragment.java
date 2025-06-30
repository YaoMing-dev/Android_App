// app/src/main/java/com/example/newtrade/ui/product/ProductPreviewFragment.java
package com.example.newtrade.ui.product;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.newtrade.R;
import com.example.newtrade.models.Product;
import com.example.newtrade.ui.product.adapter.PreviewImageAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class ProductPreviewFragment extends Fragment {
    private static final String TAG = "ProductPreviewFragment";

    // UI Components
    private ViewPager2 viewPagerImages;
    private TabLayout tabLayoutImages;
    private TextView tvTitle, tvPrice, tvDescription, tvLocation, tvCategory;
    private Chip chipCondition;
    private MaterialButton btnEdit, btnPublish;

    // Data
    private Product previewProduct;
    private List<Uri> productImages;
    private PreviewImageAdapter imageAdapter;

    // Listener
    private OnPreviewActionListener listener;

    public interface OnPreviewActionListener {
        void onEditProduct();
        void onPublishProduct();
    }

    public static ProductPreviewFragment newInstance() {
        return new ProductPreviewFragment();
    }

    public void setOnPreviewActionListener(OnPreviewActionListener listener) {
        this.listener = listener;
    }

    public void setPreviewData(Product product, List<Uri> images) {
        this.previewProduct = product;
        this.productImages = images;

        if (getView() != null) {
            updatePreview();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupImagePager();
        setupListeners();
        updatePreview();
    }

    private void initViews(View view) {
        viewPagerImages = view.findViewById(R.id.viewpager_images);
        tabLayoutImages = view.findViewById(R.id.tablayout_images);
        tvTitle = view.findViewById(R.id.tv_title);
        tvPrice = view.findViewById(R.id.tv_price);
        tvDescription = view.findViewById(R.id.tv_description);
        tvLocation = view.findViewById(R.id.tv_location);
        tvCategory = view.findViewById(R.id.tv_category);
        chipCondition = view.findViewById(R.id.chip_condition);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnPublish = view.findViewById(R.id.btn_publish);
    }

    private void setupImagePager() {
        if (productImages != null) {
            imageAdapter = new PreviewImageAdapter(productImages, null);
            viewPagerImages.setAdapter(imageAdapter);

            // Setup tab indicator if multiple images
            if (productImages.size() > 1) {
                new TabLayoutMediator(tabLayoutImages, viewPagerImages,
                        (tab, position) -> {
                            // Tab setup is handled automatically
                        }).attach();
                tabLayoutImages.setVisibility(View.VISIBLE);
            } else {
                tabLayoutImages.setVisibility(View.GONE);
            }
        }
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct();
            }
        });

        btnPublish.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPublishProduct();
            }
        });
    }

    private void updatePreview() {
        if (previewProduct == null) return;

        // Product info
        if (previewProduct.getTitle() != null) {
            tvTitle.setText(previewProduct.getTitle());
        }

        if (previewProduct.getPrice() != null) {
            tvPrice.setText(previewProduct.getFormattedPrice());
        }

        if (previewProduct.getDescription() != null && !previewProduct.getDescription().isEmpty()) {
            tvDescription.setText(previewProduct.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        if (previewProduct.getLocation() != null && !previewProduct.getLocation().isEmpty()) {
            tvLocation.setText(previewProduct.getLocation());
            tvLocation.setVisibility(View.VISIBLE);
        } else {
            tvLocation.setVisibility(View.GONE);
        }

        // Category
        if (previewProduct.getCategory() != null) {
            tvCategory.setText(previewProduct.getCategory().getName());
            tvCategory.setVisibility(View.VISIBLE);
        } else {
            tvCategory.setVisibility(View.GONE);
        }

        // Condition
        if (previewProduct.getCondition() != null) {
            chipCondition.setText(previewProduct.getCondition().getDisplayName());
            chipCondition.setVisibility(View.VISIBLE);
        } else {
            chipCondition.setVisibility(View.GONE);
        }

        // Update image pager
        if (imageAdapter != null && productImages != null) {
            imageAdapter.updateImages(productImages);
        }
    }
}