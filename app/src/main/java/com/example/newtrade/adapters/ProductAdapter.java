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

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onProductSave(Product product);
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
        private ImageView ivProductImage;
        private ImageView ivSaveProduct;
        private TextView tvCondition;
        private TextView tvDistance;
        private TextView tvProductTitle;
        private TextView tvProductPrice;
        private TextView tvOriginalPrice;
        private TextView tvProductLocation;
        private TextView tvTimePosted;
        private ImageView ivSellerAvatar;
        private TextView tvSellerName;
        private TextView tvSellerRating;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            ivSaveProduct = itemView.findViewById(R.id.iv_save_product);
            tvCondition = itemView.findViewById(R.id.tv_condition);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvProductLocation = itemView.findViewById(R.id.tv_product_location);
            tvTimePosted = itemView.findViewById(R.id.tv_time_posted);
            ivSellerAvatar = itemView.findViewById(R.id.iv_seller_avatar);
            tvSellerName = itemView.findViewById(R.id.tv_seller_name);
            tvSellerRating = itemView.findViewById(R.id.tv_seller_rating);
        }

        public void bind(Product product, OnProductClickListener listener) {
            // Product title
            tvProductTitle.setText(product.getTitle());

            // Product price
            tvProductPrice.setText(product.getFormattedPrice());

            // Product location
            tvProductLocation.setText(product.getLocation());

            // Time posted
            tvTimePosted.setText(product.getTimeAgo());

            // Condition
            tvCondition.setText(product.getConditionDisplay());

            // Seller name
            if (product.getSellerName() != null) {
                tvSellerName.setText(product.getSellerName());
            }

            // Seller rating
            if (product.getSellerRating() > 0) {
                tvSellerRating.setText(String.format("%.1f ★", product.getSellerRating()));
                tvSellerRating.setVisibility(View.VISIBLE);
            } else {
                tvSellerRating.setVisibility(View.GONE);
            }

            // Product image
            String imageUrl = product.getMainImageUrl();
            if (imageUrl != null) {
                Glide.with(itemView.getContext())
                        .load(Constants.getImageUrl(imageUrl))
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_placeholder_image)
                        .centerCrop()
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_placeholder_image);
            }

            // Seller avatar
            if (product.getSellerAvatar() != null) {
                Glide.with(itemView.getContext())
                        .load(Constants.getImageUrl(product.getSellerAvatar()))
                        .placeholder(R.drawable.ic_placeholder_avatar)
                        .error(R.drawable.ic_placeholder_avatar)
                        .circleCrop()
                        .into(ivSellerAvatar);
            } else {
                ivSellerAvatar.setImageResource(R.drawable.ic_placeholder_avatar);
            }

            // Save button
            updateSaveButton(product.isSaved());
            ivSaveProduct.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductSave(product);
                }
            });

            // Show/hide distance (placeholder implementation)
            if (product.hasLocation()) {
                tvDistance.setText("2.5 km"); // Placeholder distance
                tvDistance.setVisibility(View.VISIBLE);
            } else {
                tvDistance.setVisibility(View.GONE);
            }

            // Hide original price if no discount
            tvOriginalPrice.setVisibility(View.GONE);

            // Product click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }

        private void updateSaveButton(boolean isSaved) {
            if (isSaved) {
                ivSaveProduct.setImageResource(R.drawable.ic_bookmark_filled);
            } else {
                ivSaveProduct.setImageResource(R.drawable.ic_bookmark_border);
            }
        }
    }
}