// app/src/main/java/com/example/newtrade/ui/product/adapter/ProductGridAdapter.java
package com.example.newtrade.ui.product.adapter;

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
import com.example.newtrade.utils.DateTimeUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductGridAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_grid, parent, false);
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
        private MaterialCardView cardProduct;
        private ImageView ivProductImage;
        private TextView tvTitle, tvPrice, tvLocation, tvCondition, tvTimeAgo;
        private View viewSold;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProduct = itemView.findViewById(R.id.card_product);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCondition = itemView.findViewById(R.id.tv_condition);
            tvTimeAgo = itemView.findViewById(R.id.tv_time_ago);
            viewSold = itemView.findViewById(R.id.view_sold);
        }

        public void bind(Product product, OnProductClickListener listener) {
            // Product image
            String imageUrl = product.getFirstImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .centerCrop()
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.placeholder_product);
            }

            // Product details
            tvTitle.setText(product.getTitle());
            tvPrice.setText(product.getPriceString());
            tvLocation.setText(product.getLocation());
            tvCondition.setText(product.getConditionDisplay());

            // Time ago
            if (product.getCreatedAt() != null) {
                tvTimeAgo.setText(DateTimeUtils.getRelativeTimeString(product.getCreatedAt()));
            } else {
                tvTimeAgo.setText("");
            }

            // Sold overlay
            if (product.isSold()) {
                viewSold.setVisibility(View.VISIBLE);
                cardProduct.setAlpha(0.7f);
            } else {
                viewSold.setVisibility(View.GONE);
                cardProduct.setAlpha(1.0f);
            }

            // Click listener
            cardProduct.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public void addProducts(List<Product> newProducts) {
        int oldSize = products.size();
        products.addAll(newProducts);
        notifyItemRangeInserted(oldSize, newProducts.size());
    }

    public void removeProduct(int position) {
        if (position >= 0 && position < products.size()) {
            products.remove(position);
            notifyItemRemoved(position);
        }
    }
}