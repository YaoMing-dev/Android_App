// app/src/main/java/com/example/newtrade/ui/product/adapter/SavedItemsAdapter.java
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
import com.example.newtrade.utils.DateTimeUtils;

import java.util.List;

public class SavedItemsAdapter extends RecyclerView.Adapter<SavedItemsAdapter.SavedItemViewHolder> {

    private List<Product> savedItems;
    private OnSavedItemActionListener listener;

    public interface OnSavedItemActionListener {
        void onItemClick(Product product);
        void onRemoveClick(Product product);
    }

    public SavedItemsAdapter(List<Product> savedItems, OnSavedItemActionListener listener) {
        this.savedItems = savedItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SavedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_product, parent, false);
        return new SavedItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedItemViewHolder holder, int position) {
        Product product = savedItems.get(position);

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

        // Saved date (using created date as placeholder)
        if (product.getCreatedAt() != null) {
            String formattedDate = DateTimeUtils.formatProductDate(product.getCreatedAt());
            holder.tvSavedDate.setText("Saved " + formattedDate);
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

        // Status indicator for sold items
        if (product.getStatus() == Product.ProductStatus.SOLD) {
            holder.viewSoldOverlay.setVisibility(View.VISIBLE);
            holder.tvSoldLabel.setVisibility(View.VISIBLE);
        } else {
            holder.viewSoldOverlay.setVisibility(View.GONE);
            holder.tvSoldLabel.setVisibility(View.GONE);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(product);
            }
        });

        holder.ivRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return savedItems.size();
    }

    static class SavedItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivRemove;
        TextView tvTitle, tvPrice, tvLocation, tvSavedDate, tvSoldLabel;
        View viewSoldOverlay;

        public SavedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivRemove = itemView.findViewById(R.id.iv_remove);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvSavedDate = itemView.findViewById(R.id.tv_saved_date);
            tvSoldLabel = itemView.findViewById(R.id.tv_sold_label);
            viewSoldOverlay = itemView.findViewById(R.id.view_sold_overlay);
        }
    }
}