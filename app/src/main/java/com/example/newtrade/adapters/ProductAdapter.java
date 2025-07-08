// app/src/main/java/com/example/newtrade/adapters/ProductAdapter.java
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
            // Set title
            if (tvTitle != null) {
                tvTitle.setText(product.getTitle());
            }

            // Set price
            if (tvPrice != null && product.getPrice() != null) {
                tvPrice.setText(product.getFormattedPrice());
            }

            // Set location
            if (tvLocation != null) {
                tvLocation.setText(product.getLocation());
            }

            // Set condition
            if (tvCondition != null) {
                Product.ProductCondition condition = product.getCondition();
                if (condition != null) {
                    tvCondition.setText(condition.getDisplayName());
                    tvCondition.setVisibility(View.VISIBLE);
                } else {
                    tvCondition.setVisibility(View.GONE);
                }
            }
            if (tvCategory != null) {
                String categoryName = product.getCategoryName();
                if (categoryName != null && !categoryName.isEmpty()) {
                    tvCategory.setText(categoryName);
                    tvCategory.setVisibility(View.VISIBLE);
                } else {
                    tvCategory.setVisibility(View.GONE);
                }

                // Load product image
                if (ivProduct != null) {
                    String imageUrl = product.getPrimaryImageUrl();

                    try {
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

                // Set click listeners
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onProductClick(product);
                    }
                });

                itemView.setOnLongClickListener(v -> {
                    if (listener != null) {
                        listener.onProductLongClick(product);
                        return true;
                    }
                    return false;
                });
            }
        }
    }
}