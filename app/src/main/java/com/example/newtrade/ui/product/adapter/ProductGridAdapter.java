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
import com.example.newtrade.utils.Constants;

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
        if (product.getLocation() != null && !product.getLocation().isEmpty()) {
            holder.tvLocation.setText(product.getLocation());
            holder.tvLocation.setVisibility(View.VISIBLE);
        } else {
            holder.tvLocation.setVisibility(View.GONE);
        }

        // Product condition
        if (product.getCondition() != null) {
            holder.tvCondition.setText(product.getCondition().getDisplayName());
            holder.tvCondition.setVisibility(View.VISIBLE);
        } else {
            holder.tvCondition.setVisibility(View.GONE);
        }

        // Load product image
        String imageUrl = product.getFirstImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .transform(new RoundedCorners(12))
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.ic_error_image)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_product);
        }

        // Status indicator
        if (product.getStatus() == Product.ProductStatus.SOLD) {
            holder.viewSoldOverlay.setVisibility(View.VISIBLE);
            holder.tvSoldLabel.setVisibility(View.VISIBLE);
        } else {
            holder.viewSoldOverlay.setVisibility(View.GONE);
            holder.tvSoldLabel.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvPrice, tvLocation, tvCondition, tvSoldLabel;
        View viewSoldOverlay;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCondition = itemView.findViewById(R.id.tv_condition);
            tvSoldLabel = itemView.findViewById(R.id.tv_sold_label);
            viewSoldOverlay = itemView.findViewById(R.id.view_sold_overlay);
        }
    }
}