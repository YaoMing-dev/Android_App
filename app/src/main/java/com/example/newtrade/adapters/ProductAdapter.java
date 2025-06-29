package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        default void onFavoriteClick(Product product) {}
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
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
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvTitle, tvPrice, tvLocation, tvCondition;
        private ImageButton btnFavorite;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCondition = itemView.findViewById(R.id.tv_condition);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }

        public void bind(Product product) {
            // Product image
            String imageUrl = product.getDisplayImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.placeholder_image);
            }

            // Product details
            tvTitle.setText(product.getTitle());
            tvPrice.setText(product.getFormattedPrice());
            tvLocation.setText(product.getLocation());
            tvCondition.setText(product.getCondition());

            // Favorite button
            boolean isFavorited = product.getIsFavorited() != null && product.getIsFavorited();
            btnFavorite.setImageResource(isFavorited ?
                R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(product);
                }
            });

            // Show sold overlay if product is sold
            if (product.isSold()) {
                itemView.setAlpha(0.6f);
                // Could add a "SOLD" overlay here
            } else {
                itemView.setAlpha(1.0f);
            }
        }
    }
}
