// app/src/main/java/com/example/newtrade/adapters/ProductAdapter.java
package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.models.Product;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.PriceFormatter;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;

    // ✅ SỬA INTERFACE - chỉ có 1 abstract method để có thể dùng lambda
    public interface OnProductClickListener {
        void onProductClick(Product product);

        // ✅ THÊM default methods để không bắt buộc implement
        default void onProductSave(Product product) {
            // Default implementation - do nothing
        }

        default void onProductLongClick(Product product) {
            // Default implementation - do nothing
        }
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_grid, parent, false);
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

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage, ivSaveIcon;
        private TextView tvTitle, tvPrice, tvLocation, tvCondition;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            ivSaveIcon = itemView.findViewById(R.id.iv_save_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCondition = itemView.findViewById(R.id.tv_condition);
        }

        public void bind(Product product, OnProductClickListener listener) {
            // Set title
            if (tvTitle != null) {
                tvTitle.setText(product.getTitle());
            }

            // Set price
            if (tvPrice != null) {
                tvPrice.setText(PriceFormatter.format(product.getPrice()));
            }

            // Set location
            if (tvLocation != null) {
                tvLocation.setText(product.getLocation());
            }

            // Set condition
            if (tvCondition != null && product.getCondition() != null) {
                tvCondition.setText(product.getCondition().name());
            }

            // Load image
            if (ivProductImage != null) {
                String imageUrl = product.getPrimaryImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(Constants.BASE_URL + imageUrl)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .into(ivProductImage);
                } else {
                    ivProductImage.setImageResource(R.drawable.ic_placeholder);
                }
            }

            // Set save icon
            if (ivSaveIcon != null) {
                ivSaveIcon.setImageResource(product.isSaved() ?
                        R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outlined);

                ivSaveIcon.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onProductSave(product);
                    }
                });
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
                }
                return true;
            });
        }
    }
}