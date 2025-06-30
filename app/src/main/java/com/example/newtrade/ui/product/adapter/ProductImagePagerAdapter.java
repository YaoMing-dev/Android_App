package com.example.newtrade.ui.product.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.newtrade.R;
import com.example.newtrade.models.ProductImage;

import java.util.List;

public class ProductImagePagerAdapter extends RecyclerView.Adapter<ProductImagePagerAdapter.ImageViewHolder> {

    private List<ProductImage> images;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(ProductImage image, int position);
    }

    public ProductImagePagerAdapter(List<ProductImage> images) {
        this.images = images;
    }

    public ProductImagePagerAdapter(List<ProductImage> images, OnImageClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_image_pager, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ProductImage image = images.get(position);

        // Load image
        String imageUrl = image.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.ic_error_image)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_product);
        }

        // Click listener for image zoom
        holder.ivImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(image, position);
            }
        });

        // Image position indicator
        if (images.size() > 1) {
            holder.tvPosition.setText((position + 1) + " / " + images.size());
            holder.tvPosition.setVisibility(View.VISIBLE);
        } else {
            holder.tvPosition.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    public void updateImages(List<ProductImage> newImages) {
        this.images = newImages;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvPosition;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvPosition = itemView.findViewById(R.id.tv_position);
        }
    }
}