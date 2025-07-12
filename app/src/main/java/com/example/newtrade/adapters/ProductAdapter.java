// app/src/main/java/com/example/newtrade/adapters/ProductAdapter.java
package com.example.newtrade.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.newtrade.R;
import com.example.newtrade.models.Product;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.ImageUtils;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> products;
    private final OnProductClickListener listener;
    private final boolean isOwnerView; // ✅ NEW: Flag to show owner-specific features

    public interface OnProductClickListener {
        void onProductClick(Product product);

        // ✅ DEFAULT METHOD - không bắt buộc implement
        default void onProductLongClick(Product product) {
            // Empty default implementation
        }

        // ✅ NEW: Analytics click for owner view
        default void onAnalyticsClick(Product product) {
            // Empty default implementation
        }

        // ✅ NEW: Status change click for owner view
        default void onStatusChangeClick(Product product) {
            // Empty default implementation
        }
    }

    // ✅ ENHANCED: Constructor with owner view flag
    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this(products, listener, false);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener, boolean isOwnerView) {
        this.products = products;
        this.listener = listener;
        this.isOwnerView = isOwnerView;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        if (position >= 0 && position < products.size()) {
            Product product = products.get(position);
            holder.bind(product, listener, isOwnerView);
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProduct;
        private final TextView tvTitle;
        private final TextView tvPrice;
        private final TextView tvLocation;
        private final TextView tvCondition;
        private final TextView tvCategory;

        // ✅ NEW: Status and owner view elements
        private final TextView tvStatusBadge;
        private final View llOwnerActions;
        private final ImageView ivAnalytics;
        private final ImageView ivStatusChange;
        private final View vStatusOverlay;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            // Existing views
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCondition = itemView.findViewById(R.id.tv_condition);
            tvCategory = itemView.findViewById(R.id.tv_category);

            // ✅ NEW: Status and owner view elements (safe findViewById)
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            llOwnerActions = itemView.findViewById(R.id.ll_owner_actions);
            ivAnalytics = itemView.findViewById(R.id.iv_analytics);
            ivStatusChange = itemView.findViewById(R.id.iv_status_change);
            vStatusOverlay = itemView.findViewById(R.id.v_status_overlay);
        }

        void bind(Product product, OnProductClickListener listener, boolean isOwnerView) {
            if (product == null) return;

            try {
                // Set basic product info
                setBasicProductInfo(product);

                // ✅ ENHANCED: Set status indicators (market-standard)
                setStatusIndicators(product);

                // ✅ NEW: Set owner view features
                setOwnerViewFeatures(product, listener, isOwnerView);

                // Set click listeners
                setClickListeners(product, listener);

            } catch (Exception e) {
                // Fallback in case of any error
                if (tvTitle != null) {
                    tvTitle.setText(R.string.error_loading_product);
                }
            }
        }

        // ===== BASIC PRODUCT INFO =====

        private void setBasicProductInfo(Product product) {
            // Set title
            if (tvTitle != null) {
                String title = product.getTitle();
                tvTitle.setText(title != null ? title : itemView.getContext().getString(R.string.no_title));
            }

            // Set price
            if (tvPrice != null) {
                try {
                    if (product.getPrice() != null) {
                        tvPrice.setText(product.getFormattedPrice());
                    } else {
                        tvPrice.setText(R.string.price_not_available);
                    }
                } catch (Exception e) {
                    tvPrice.setText(R.string.price_not_available);
                }
            }

            // Set location
            if (tvLocation != null) {
                String location = product.getLocation();
                tvLocation.setText(location != null ? location :
                        itemView.getContext().getString(R.string.location_not_specified));
            }

            // Set condition
            if (tvCondition != null) {
                try {
                    Product.ProductCondition condition = product.getCondition();
                    if (condition != null) {
                        tvCondition.setText(condition.getDisplayName());
                        tvCondition.setVisibility(View.VISIBLE);
                    } else {
                        tvCondition.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    tvCondition.setVisibility(View.GONE);
                }
            }

            // Set category
            if (tvCategory != null) {
                try {
                    String categoryName = product.getCategoryName();
                    if (categoryName != null && !categoryName.isEmpty()) {
                        tvCategory.setText(categoryName);
                        tvCategory.setVisibility(View.VISIBLE);
                    } else {
                        tvCategory.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    tvCategory.setVisibility(View.GONE);
                }
            }

            // Load product image
            loadProductImage(product);
        }

        // ===== ✅ ENHANCED: STATUS INDICATORS (Market-Standard) =====

        private void setStatusIndicators(Product product) {
            Context context = itemView.getContext();
            Product.ProductStatus status = product.getStatus();

            if (status == null) {
                status = Product.ProductStatus.AVAILABLE; // Default
            }

            // ✅ Status Badge (always visible for non-available items)
            if (tvStatusBadge != null) {
                switch (status) {
                    case AVAILABLE:
                        tvStatusBadge.setVisibility(View.GONE);
                        break;

                    case SOLD:
                        tvStatusBadge.setText(R.string.status_sold);
                        tvStatusBadge.setTextColor(Color.WHITE);
                        setStatusBadgeBackground(tvStatusBadge,
                                ContextCompat.getColor(context, R.color.status_sold));
                        tvStatusBadge.setVisibility(View.VISIBLE);
                        break;

                    case PAUSED:
                        tvStatusBadge.setText(R.string.status_paused);
                        tvStatusBadge.setTextColor(Color.WHITE);
                        setStatusBadgeBackground(tvStatusBadge,
                                ContextCompat.getColor(context, R.color.warning));
                        tvStatusBadge.setVisibility(View.VISIBLE);
                        break;

                    case ARCHIVED:
                        tvStatusBadge.setText(R.string.status_archived);
                        tvStatusBadge.setTextColor(Color.WHITE);
                        setStatusBadgeBackground(tvStatusBadge,
                                ContextCompat.getColor(context, R.color.text_secondary));
                        tvStatusBadge.setVisibility(View.VISIBLE);
                        break;

                    default:
                        tvStatusBadge.setVisibility(View.GONE);
                        break;
                }
            }

            // ✅ Status Overlay (visual indication for non-available items)
            if (vStatusOverlay != null) {
                switch (status) {
                    case SOLD:
                        vStatusOverlay.setBackgroundColor(Color.parseColor("#80000000")); // Semi-transparent black
                        vStatusOverlay.setVisibility(View.VISIBLE);
                        break;

                    case PAUSED:
                        vStatusOverlay.setBackgroundColor(Color.parseColor("#80FF9800")); // Semi-transparent orange
                        vStatusOverlay.setVisibility(View.VISIBLE);
                        break;

                    case ARCHIVED:
                        vStatusOverlay.setBackgroundColor(Color.parseColor("#80757575")); // Semi-transparent gray
                        vStatusOverlay.setVisibility(View.VISIBLE);
                        break;

                    default:
                        vStatusOverlay.setVisibility(View.GONE);
                        break;
                }
            }

            // ✅ Disable interactions for non-available items (market-standard)
            setItemInteractionState(status);
        }

        private void setStatusBadgeBackground(TextView badge, int color) {
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setColor(color);
            background.setCornerRadius(12f);
            badge.setBackground(background);
        }

        private void setItemInteractionState(Product.ProductStatus status) {
            // ✅ Market-standard: Disable interactions for SOLD/PAUSED items but keep them visible
            boolean isInteractable = (status == Product.ProductStatus.AVAILABLE);

            // Visual indication of interaction state
            float alpha = isInteractable ? 1.0f : 0.7f;
            itemView.setAlpha(alpha);
        }

        // ===== ✅ NEW: OWNER VIEW FEATURES =====

        private void setOwnerViewFeatures(Product product, OnProductClickListener listener, boolean isOwnerView) {
            if (llOwnerActions != null) {
                llOwnerActions.setVisibility(isOwnerView ? View.VISIBLE : View.GONE);
            }

            if (isOwnerView) {
                // Analytics button
                if (ivAnalytics != null) {
                    ivAnalytics.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onAnalyticsClick(product);
                        }
                    });
                }

                // Status change button
                if (ivStatusChange != null) {
                    ivStatusChange.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onStatusChangeClick(product);
                        }
                    });
                }
            }
        }

        // ===== CLICK LISTENERS =====

        private void setClickListeners(Product product, OnProductClickListener listener) {
            if (listener != null) {
                // Main click
                itemView.setOnClickListener(v -> listener.onProductClick(product));

                // Long click (for context menu)
                itemView.setOnLongClickListener(v -> {
                    listener.onProductLongClick(product);
                    return true;
                });
            }
        }

        // ===== ✅ FIXED: IMAGE LOADING =====

        private void loadProductImage(Product product) {
            if (ivProduct != null && product != null) {
                try {
                    Context context = itemView.getContext();
                    List<String> imageUrls = product.getImageUrls();

                    if (imageUrls != null && !imageUrls.isEmpty()) {
                        String imageUrl = imageUrls.get(0);
                        if (!TextUtils.isEmpty(imageUrl)) {
                            // ✅ FIXED: Use ImageUtils instead of non-existent method
                            ImageUtils.loadProductImage(context, imageUrl, ivProduct);
                        } else {
                            // Fallback to placeholder
                            setPlaceholderImage(context);
                        }
                    } else {
                        // No images available
                        setPlaceholderImage(context);
                    }
                } catch (Exception e) {
                    // Error loading image, use placeholder
                    Context context = itemView.getContext();
                    setPlaceholderImage(context);
                }
            }
        }

        private void setPlaceholderImage(Context context) {
            if (ivProduct != null && context != null) {
                try {
                    Glide.with(context)
                            .load(R.drawable.ic_image_placeholder)
                            .into(ivProduct);
                } catch (Exception e) {
                    // Last resort: set drawable directly
                    ivProduct.setImageResource(R.drawable.ic_image_placeholder);
                }
            }
        }
    }
}