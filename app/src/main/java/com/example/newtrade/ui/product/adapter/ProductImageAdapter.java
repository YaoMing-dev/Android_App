// app/src/main/java/com/example/newtrade/ui/product/adapter/ProductImageAdapter.java
package com.example.newtrade.ui.product.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.newtrade.R;

import java.io.File;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder> {

    private List<String> imagePaths;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(int position);
        void onRemoveClick(int position);
    }

    public ProductImageAdapter(List<String> imagePaths, OnImageClickListener listener) {
        this.imagePaths = imagePaths;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(new File(imagePath))
                .transform(new RoundedCorners(16))
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.ivImage);

        // Click listeners
        holder.ivImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(position);
            }
        });

        holder.ivRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivRemove;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivRemove = itemView.findViewById(R.id.iv_remove);
        }
    }
}