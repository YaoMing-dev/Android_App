// app/src/main/java/com/example/newtrade/ui/product/adapter/MyProductsAdapter.java
package com.example.newtrade.ui.product.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.newtrade.R;
import com.example.newtrade.models.Product;
import com.google.android.material.chip.Chip;

import java.util.List;

public class MyProductsAdapter extends RecyclerView.Adapter<MyProductsAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onProductClick(Product product);
        void onEditClick(Product product);
        void onStatusChange(Product product, Product.ProductStatus newStatus);
        void onDeleteClick(Product product);
        void onAnalyticsClick(Product product);
    }

    public MyProductsAdapter(List<Product> products, OnProductActionListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        // Basic info
        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText(product.getFormattedPrice());
        holder.tvLocation.setText(product.getLocation());

        // Status chip
        holder.chipStatus.setText(product.getStatus().getDisplayName());
        holder.chipStatus.setChipBackgroundColorResource(getStatusColor(product.getStatus()));

        // View count
        holder.tvViews.setText(String.valueOf(product.getViewCount() != null ? product.getViewCount() : 0) + " views");

        // Created date
        if (product.getCreatedAt() != null) {
            holder.tvDate.setText(formatDate(product.getCreatedAt()));
        }

        // Load product image
        String imageUrl = product.getFirstImageUrl();
        if (imageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .transform(new RoundedCorners(16))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.placeholder_image);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        holder.ivMore.setOnClickListener(v -> showPopupMenu(v, product));
    }

    private void showPopupMenu(View anchor, Product product) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_my_product, popup.getMenu());

        // Show/hide menu items based on status
        if (product.getStatus() == Product.ProductStatus.SOLD) {
            popup.getMenu().findItem(R.id.action_mark_sold).setVisible(false);
            popup.getMenu().findItem(R.id.action_pause).setVisible(false);
        } else if (product.getStatus() == Product.ProductStatus.PAUSED) {
            popup.getMenu().findItem(R.id.action_pause).setVisible(false);
            popup.getMenu().findItem(R.id.action_resume).setVisible(true);
        } else {
            popup.getMenu().findItem(R.id.action_resume).setVisible(false);
        }

        popup.setOnMenuItemClickListener(item -> {
            if (listener == null) return false;

            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                listener.onEditClick(product);
                return true;
            } else if (itemId == R.id.action_mark_sold) {
                listener.onStatusChange(product, Product.ProductStatus.SOLD);
                return true;
            } else if (itemId == R.id.action_pause) {
                listener.onStatusChange(product, Product.ProductStatus.PAUSED);
                return true;
            } else if (itemId == R.id.action_resume) {
                listener.onStatusChange(product, Product.ProductStatus.AVAILABLE);
                return true;
            } else if (itemId == R.id.action_analytics) {
                listener.onAnalyticsClick(product);
                return true;
            } else if (itemId == R.id.action_delete) {
                listener.onDeleteClick(product);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private int getStatusColor(Product.ProductStatus status) {
        switch (status) {
            case AVAILABLE:
                return R.color.status_available;
            case SOLD:
                return R.color.status_sold;
            case PAUSED:
                return R.color.status_paused;
            case RESERVED:
                return R.color.status_reserved;
            default:
                return R.color.status_default;
        }
    }

    private String formatDate(String dateString) {
        try {
            // Simple date formatting
            return dateString.substring(0, 10); // yyyy-MM-dd
        } catch (Exception e) {
            return dateString;
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct, ivMore;
        TextView tvTitle, tvPrice, tvLocation, tvViews, tvDate;
        Chip chipStatus;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            ivMore = itemView.findViewById(R.id.iv_more);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvViews = itemView.findViewById(R.id.tv_views);
            tvDate = itemView.findViewById(R.id.tv_date);
            chipStatus = itemView.findViewById(R.id.chip_status);
        }
    }
}