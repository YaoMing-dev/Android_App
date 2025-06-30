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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.newtrade.R;
import com.example.newtrade.models.Product;

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

        // Product title
        holder.tvTitle.setText(product.getTitle());

        // Product price
        holder.tvPrice.setText(product.getFormattedPrice());

        // Product location
        holder.tvLocation.setText(product.getLocation());

        // View count
        if (product.getViewCount() != null && product.getViewCount() > 0) {
            holder.tvViews.setText(product.getViewCount() + " views");
            holder.tvViews.setVisibility(View.VISIBLE);
        } else {
            holder.tvViews.setVisibility(View.GONE);
        }

        // Load product image
        String imageUrl = product.getFirstImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .transform(new RoundedCorners(16))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.placeholder_image);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        // Show condition if not good
        if (product.getCondition() != null && product.getCondition() != Product.ProductCondition.GOOD) {
            holder.tvCondition.setText(product.getCondition().getDisplayName());
            holder.tvCondition.setVisibility(View.VISIBLE);
        } else {
            holder.tvCondition.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvTitle, tvPrice, tvLocation, tvViews, tvCondition;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvViews = itemView.findViewById(R.id.tv_views);
            tvCondition = itemView.findViewById(R.id.tv_condition);
        }
    }
}