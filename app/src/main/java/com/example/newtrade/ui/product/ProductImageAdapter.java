// app/src/main/java/com/example/newtrade/ui/product/ProductImageAdapter.java
package com.example.newtrade.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.newtrade.R;
import com.example.newtrade.utils.Constants;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ViewHolder> {

    private final List<String> imageUrls;
    private final OnImageDeleteListener deleteListener;

    public interface OnImageDeleteListener {
        void onImageDelete(int position);
    }

    public ProductImageAdapter(List<String> imageUrls, OnImageDeleteListener deleteListener) {
        this.imageUrls = imageUrls;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Load image với Glide
        Glide.with(holder.itemView.getContext())
                .load(getFullImageUrl(imageUrl))
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .centerCrop()
                .transform(new RoundedCorners(16))
                .into(holder.ivImage);

        // Delete button click
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && deleteListener != null) {
                deleteListener.onImageDelete(adapterPosition);
            }
        });

        // Show position indicator
        holder.ivPosition.setVisibility(imageUrls.size() > 1 ? View.VISIBLE : View.GONE);
        if (imageUrls.size() > 1) {
            // Set position number or primary indicator
            if (position == 0) {
                holder.ivPosition.setImageResource(R.drawable.ic_star); // Primary image
            } else {
                // Could set number badge here
            }
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    private String getFullImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        // If already full URL, return as is
        if (imageUrl.startsWith("http")) {
            return imageUrl;
        }

        // Otherwise, prepend base URL
        return Constants.PRODUCT_IMAGES_URL + imageUrl;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView ivImage;
        ImageView btnDelete;
        ImageView ivPosition;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_image);
            ivImage = itemView.findViewById(R.id.iv_image);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            ivPosition = itemView.findViewById(R.id.iv_position);
        }
    }
}