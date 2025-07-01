// app/src/main/java/com/example/newtrade/ui/home/ProductAdapter.java
package com.example.newtrade.ui.home;

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
import com.example.newtrade.models.ProductResponse;
import com.example.newtrade.utils.Constants;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public static final int VIEW_TYPE_HORIZONTAL = 1;
    public static final int VIEW_TYPE_GRID = 2;
    public static final int VIEW_TYPE_LIST = 3;

    private final List<ProductResponse> products;
    private final OnProductClickListener clickListener;
    private final int viewType;

    public interface OnProductClickListener {
        void onProductClick(ProductResponse product);
    }

    public ProductAdapter(List<ProductResponse> products, OnProductClickListener clickListener, int viewType) {
        this.products = products;
        this.clickListener = clickListener;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        switch (this.viewType) {
            case VIEW_TYPE_HORIZONTAL:
                layoutId = R.layout.item_product_horizontal;
                break;
            case VIEW_TYPE_GRID:
                layoutId = R.layout.item_product_grid;
                break;
            case VIEW_TYPE_LIST:
            default:
                layoutId = R.layout.item_product_list;
                break;
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductResponse product = products.get(position);

        // Basic product info
        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText(product.getFormattedPrice());

        // Product image
        loadProductImage(holder.ivImage, product);

        // Additional info based on view type
        if (holder.tvLocation != null) {
            holder.tvLocation.setText(product.getLocation());
        }

        if (holder.tvCondition != null) {
            holder.tvCondition.setText(getConditionDisplayName(product.getCondition()));
        }

        if (holder.tvSeller != null) {
            holder.tvSeller.setText("by " + product.getSellerName());
        }

        if (holder.tvViews != null) {
            holder.tvViews.setText(product.getViewCount() + " views");
        }

        // Status indicator
        if (holder.viewStatus != null) {
            if (product.isSold()) {
                holder.viewStatus.setVisibility(View.VISIBLE);
                holder.viewStatus.setBackgroundResource(R.drawable.bg_status_sold);
            } else if (!product.isAvailable()) {
                holder.viewStatus.setVisibility(View.VISIBLE);
                holder.viewStatus.setBackgroundResource(R.drawable.bg_status_unavailable);
            } else {
                holder.viewStatus.setVisibility(View.GONE);
            }
        }

        // Click listener
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onProductClick(product);
            }
        });

        // Ripple effect
        holder.cardView.setRippleColor(
                holder.itemView.getContext().getColorStateList(R.color.ripple_color));
    }

    private void loadProductImage(ImageView imageView, ProductResponse product) {
        String imageUrl = product.getFirstImageUrl();

        Glide.with(imageView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.error_product)
                .centerCrop()
                .transform(new RoundedCorners(12))
                .into(imageView);
    }

    private String getConditionDisplayName(String condition) {
        if (condition == null) return "Unknown";

        switch (condition.toUpperCase()) {
            case "NEW": return "New";
            case "LIKE_NEW": return "Like New";
            case "GOOD": return "Good";
            case "FAIR": return "Fair";
            case "POOR": return "Poor";
            default: return condition;
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView ivImage;
        TextView tvTitle;
        TextView tvPrice;
        TextView tvLocation;
        TextView tvCondition;
        TextView tvSeller;
        TextView tvViews;
        View viewStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_product);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvTitle = itemView.findViewById(R.id.tv_product_title);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvLocation = itemView.findViewById(R.id.tv_product_location);
            tvCondition = itemView.findViewById(R.id.tv_product_condition);
            tvSeller = itemView.findViewById(R.id.tv_seller_name);
            tvViews = itemView.findViewById(R.id.tv_view_count);
            viewStatus = itemView.findViewById(R.id.view_status);
        }
    }
}