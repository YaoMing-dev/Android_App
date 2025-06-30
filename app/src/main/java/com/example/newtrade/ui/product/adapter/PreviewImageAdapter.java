// app/src/main/java/com/example/newtrade/ui/product/adapter/PreviewImageAdapter.java
package com.example.newtrade.ui.product.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.newtrade.R;

import java.util.List;

public class PreviewImageAdapter extends RecyclerView.Adapter<PreviewImageAdapter.ImageViewHolder> {

    private List<Uri> imageUris;
    private OnImageActionListener listener;

    public interface OnImageActionListener {
        void onImageClick(Uri imageUri, int position);
        void onImageRemove(int position);
        void onImageReorder(int fromPosition, int toPosition);
    }

    public PreviewImageAdapter(List<Uri> imageUris, OnImageActionListener listener) {
        this.imageUris = imageUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_preview_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);

        // Load image
        Glide.with(holder.itemView.getContext())
                .load(imageUri)
                .transform(new RoundedCorners(12))
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.ic_error_image)
                .into(holder.ivImage);

        // Position indicator
        holder.tvPosition.setText(String.valueOf(position + 1));

        // Click listeners
        holder.ivImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(imageUri, position);
            }
        });

        holder.ivRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public void updateImages(List<Uri> newImageUris) {
        this.imageUris = newImageUris;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivRemove;
        TextView tvPosition;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivRemove = itemView.findViewById(R.id.iv_remove);
            tvPosition = itemView.findViewById(R.id.tv_position);
        }
    }
}