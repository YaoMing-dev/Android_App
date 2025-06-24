// File: app/src/main/java/com/example/newtrade/adapters/ProductAdapter.java
package com.example.newtrade.adapters;

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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> products;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
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
        Product product = products.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProduct;
        private final TextView tvTitle;
        private final TextView tvPrice;
        private final TextView tvLocation;
        private final TextView tvCondition;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCondition = itemView.findViewById(R.id.tv_condition);
        }

        void bind(Product product, OnProductClickListener listener) {
            // Set title
            if (tvTitle != null) {
                tvTitle.setText(product.getTitle());
            }

            // Set price
            if (tvPrice != null) {
                tvPrice.setText(product.getFormattedPrice());
            }

            // Set location
            if (tvLocation != null) {
                tvLocation.setText(product.getLocation());
            }

            // Set condition
            if (tvCondition != null) {
                String condition = product.getCondition();
                if (condition != null && !condition.isEmpty()) {
                    tvCondition.setText(formatCondition(condition));
                    tvCondition.setVisibility(View.VISIBLE);
                } else {
                    tvCondition.setVisibility(View.GONE);
                }
            }

            // Load product image with better error handling
            if (ivProduct != null) {
                String imageUrl = product.getPrimaryImageUrl();

                try {
                    if (!TextUtils.isEmpty(imageUrl)) {
                        Glide.with(itemView.getContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_placeholder_image)
                                .error(R.drawable.ic_placeholder_image)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .into(ivProduct);
                    } else {
                        // No image URL - set placeholder directly
                        ivProduct.setImageResource(R.drawable.ic_placeholder_image);
                    }
                } catch (Exception e) {
                    // Fallback in case of any error
                    try {
                        ivProduct.setImageResource(R.drawable.ic_placeholder_image);
                    } catch (Exception ex) {
                        // Last resort - set a background color
                        ivProduct.setBackgroundColor(0xFFF5F5F5);
                    }
                }
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }

        private String formatCondition(String condition) {
            if (condition == null || condition.isEmpty()) {
                return "";
            }

            switch (condition.toUpperCase()) {
                case "NEW":
                    return "New";
                case "LIKE_NEW":
                    return "Like New";
                case "GOOD":
                    return "Good";
                case "FAIR":
                    return "Fair";
                case "POOR":
                    return "Poor";
                default:
                    return condition.substring(0, 1).toUpperCase() + condition.substring(1).toLowerCase();
            }
        }
    }
}