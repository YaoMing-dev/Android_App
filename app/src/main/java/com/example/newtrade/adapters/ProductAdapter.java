// app/src/main/java/com/example/newtrade/adapters/ProductAdapter.java
package com.example.newtrade.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.newtrade.R;
import com.example.newtrade.models.Product;
import com.example.newtrade.utils.Constants;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> products;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);

        // ✅ DEFAULT METHOD - không bắt buộc implement
        default void onProductLongClick(Product product) {
            // Empty default implementation
        }
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
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
            holder.bind(product, listener);
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

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCondition = itemView.findViewById(R.id.tv_condition);
            tvCategory = itemView.findViewById(R.id.tv_category);
        }

        void bind(Product product, OnProductClickListener listener) {
            if (product == null) return;

            try {
                // Set title
                if (tvTitle != null) {
                    String title = product.getTitle();
                    tvTitle.setText(title != null ? title : "No title");
                }

                // Set price
                if (tvPrice != null) {
                    try {
                        if (product.getPrice() != null) {
                            tvPrice.setText(product.getFormattedPrice());
                        } else {
                            tvPrice.setText("Price not available");
                        }
                    } catch (Exception e) {
                        tvPrice.setText("Price not available");
                    }
                }

                // Set location
                if (tvLocation != null) {
                    String location = product.getLocation();
                    tvLocation.setText(location != null ? location : "Location not specified");
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

                // ✅ SET STATUS BADGE SAFELY
                setStatusBadgeSafe(product);

                // Load product image
                loadProductImage(product);

                // Set click listeners
                setClickListeners(product, listener);

            } catch (Exception e) {
                // Fallback in case of any error
                if (tvTitle != null) tvTitle.setText("Error loading product");
            }
        }

        private void setStatusBadgeSafe(Product product) {
            try {
                TextView tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
                if (tvStatusBadge != null && product.getStatus() != null) {
                    Context context = itemView.getContext();

                    switch (product.getStatus()) {
                        case AVAILABLE:
                            tvStatusBadge.setVisibility(View.GONE);
                            break;

                        case SOLD:
                            tvStatusBadge.setText("SOLD");
                            tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                    androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_red_dark)));
                            tvStatusBadge.setVisibility(View.VISIBLE);
                            break;

                        case PAUSED:
                            tvStatusBadge.setText("PAUSED");
                            tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                    androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_orange_dark)));
                            tvStatusBadge.setVisibility(View.VISIBLE);
                            break;

                        default:
                            tvStatusBadge.setVisibility(View.GONE);
                            break;
                    }
                }
            } catch (Exception e) {
                // Ignore status badge errors
            }
        }

        private void loadProductImage(Product product) {
            if (ivProduct != null) {
                try {
                    String imageUrl = product.getPrimaryImageUrl();

                    if (!TextUtils.isEmpty(imageUrl)) {
                        String fullImageUrl = imageUrl;
                        if (imageUrl.startsWith("/")) {
                            fullImageUrl = Constants.BASE_URL + imageUrl.substring(1);
                        }

                        Glide.with(itemView.getContext())
                                .load(fullImageUrl)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_placeholder)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .into(ivProduct);
                    } else {
                        ivProduct.setImageResource(R.drawable.ic_image_placeholder);
                    }
                } catch (Exception e) {
                    ivProduct.setImageResource(R.drawable.ic_image_placeholder);
                }
            }
        }

        private void setClickListeners(Product product, OnProductClickListener listener) {
            try {
                itemView.setOnClickListener(v -> {
                    if (listener != null && product != null) {
                        listener.onProductClick(product);
                    }
                });

                itemView.setOnLongClickListener(v -> {
                    if (listener != null && product != null) {
                        listener.onProductLongClick(product);
                        return true;
                    }
                    return false;
                });
            } catch (Exception e) {
                // Ignore click listener errors
            }
        }
    }
}