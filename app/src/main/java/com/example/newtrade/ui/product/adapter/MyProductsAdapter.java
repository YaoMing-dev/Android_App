// app/src/main/java/com/example/newtrade/ui/product/adapter/MyProductsAdapter.java
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class MyProductsAdapter extends RecyclerView.Adapter<MyProductsAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onProductClick(Product product);
        void onEditClick(Product product);
        void onDeleteClick(Product product);
        void onMarkSoldClick(Product product);
        void onArchiveClick(Product product);
        void onRestoreClick(Product product);
        void onViewAnalyticsClick(Product product);
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

        // Product info
        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText(product.getFormattedPrice());

        // Views count
        if (product.getViewCount() != null && product.getViewCount() > 0) {
            holder.tvViews.setText(product.getViewCount() + " views");
            holder.tvViews.setVisibility(View.VISIBLE);
        } else {
            holder.tvViews.setVisibility(View.GONE);
        }

        // Created date
        if (product.getCreatedAt() != null) {
            holder.tvDate.setText(formatDate(product.getCreatedAt()));
        }

        // Status chip
        setupStatusChip(holder.chipStatus, product.getStatus());

        // Product image
        String imageUrl = product.getFirstImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .transform(new RoundedCorners(8))
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.ic_error_image)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_product);
        }

        // Action buttons based on status
        setupActionButtons(holder, product);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        holder.btnPrimary.setOnClickListener(v -> handlePrimaryAction(product));
        holder.btnSecondary.setOnClickListener(v -> handleSecondaryAction(product));
        holder.ivMenu.setOnClickListener(v -> showProductMenu(holder.ivMenu, product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private void setupStatusChip(Chip chip, Product.ProductStatus status) {
        if (status == null) {
            chip.setVisibility(View.GONE);
            return;
        }

        chip.setVisibility(View.VISIBLE);
        chip.setText(status.getDisplayName());

        // Set chip color based on status
        switch (status) {
            case AVAILABLE:
                chip.setChipBackgroundColorResource(R.color.status_available);
                break;
            case SOLD:
                chip.setChipBackgroundColorResource(R.color.status_sold);
                break;
            case RESERVED:
                chip.setChipBackgroundColorResource(R.color.status_reserved);
                break;
            case PAUSED:
                chip.setChipBackgroundColorResource(R.color.status_paused);
                break;
            case ARCHIVED:
                chip.setChipBackgroundColorResource(R.color.status_archived);
                break;
            default:
                chip.setChipBackgroundColorResource(R.color.status_default);
                break;
        }
    }

    private void setupActionButtons(ProductViewHolder holder, Product product) {
        Product.ProductStatus status = product.getStatus();

        if (status == Product.ProductStatus.AVAILABLE) {
            holder.btnPrimary.setText("Mark as Sold");
            holder.btnPrimary.setVisibility(View.VISIBLE);
            holder.btnSecondary.setText("Edit");
            holder.btnSecondary.setVisibility(View.VISIBLE);
        } else if (status == Product.ProductStatus.SOLD) {
            holder.btnPrimary.setText("View Analytics");
            holder.btnPrimary.setVisibility(View.VISIBLE);
            holder.btnSecondary.setText("Archive");
            holder.btnSecondary.setVisibility(View.VISIBLE);
        } else if (status == Product.ProductStatus.ARCHIVED) {
            holder.btnPrimary.setText("Restore");
            holder.btnPrimary.setVisibility(View.VISIBLE);
            holder.btnSecondary.setVisibility(View.GONE);
        } else {
            holder.btnPrimary.setText("Edit");
            holder.btnPrimary.setVisibility(View.VISIBLE);
            holder.btnSecondary.setVisibility(View.GONE);
        }
    }

    private void handlePrimaryAction(Product product) {
        if (listener == null) return;

        Product.ProductStatus status = product.getStatus();
        if (status == Product.ProductStatus.AVAILABLE) {
            listener.onMarkSoldClick(product);
        } else if (status == Product.ProductStatus.SOLD) {
            listener.onViewAnalyticsClick(product);
        } else if (status == Product.ProductStatus.ARCHIVED) {
            listener.onRestoreClick(product);
        } else {
            listener.onEditClick(product);
        }
    }

    private void handleSecondaryAction(Product product) {
        if (listener == null) return;

        Product.ProductStatus status = product.getStatus();
        if (status == Product.ProductStatus.AVAILABLE) {
            listener.onEditClick(product);
        } else if (status == Product.ProductStatus.SOLD) {
            listener.onArchiveClick(product);
        }
    }

    private void showProductMenu(View anchor, Product product) {
        // TODO: Show popup menu with more options (delete, share, duplicate, etc.)
        // For now, just call delete
        if (listener != null) {
            listener.onDeleteClick(product);
        }
    }

    private String formatDate(String dateString) {
        // TODO: Implement proper date formatting
        try {
            if (dateString.length() >= 10) {
                return dateString.substring(0, 10);
            }
        } catch (Exception e) {
            // Ignore
        }
        return dateString;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivMenu;
        TextView tvTitle, tvPrice, tvViews, tvDate;
        Chip chipStatus;
        MaterialButton btnPrimary, btnSecondary;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivMenu = itemView.findViewById(R.id.iv_menu);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvViews = itemView.findViewById(R.id.tv_views);
            tvDate = itemView.findViewById(R.id.tv_date);
            chipStatus = itemView.findViewById(R.id.chip_status);
            btnPrimary = itemView.findViewById(R.id.btn_primary);
            btnSecondary = itemView.findViewById(R.id.btn_secondary);
        }
    }
}