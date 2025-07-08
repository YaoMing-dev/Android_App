// app/src/main/java/com/example/newtrade/adapters/SelectedImageAdapter.java
package com.example.newtrade.adapters;

import android.net.Uri;
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

import java.util.List;

public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder> {

    private List<Uri> imageUris;
    private OnImageActionListener listener;

    public interface OnImageActionListener {
        void onImageRemove(int position);
        void onImageClick(int position);
    }

    public SelectedImageAdapter(List<Uri> imageUris, OnImageActionListener listener) {
        this.imageUris = imageUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        // ✅ FIXED: Null check
        if (imageUris == null || position >= imageUris.size()) {
            return;
        }

        Uri imageUri = imageUris.get(position);

        // Load image with Glide
        Glide.with(holder.itemView.getContext())
                .load(imageUri)
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error)
                .into(holder.ivImage);

        // Position indicator
        holder.tvPosition.setText((position + 1) + "/" + imageUris.size());

        // Remove button click
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageRemove(position);
            }
        });

        // Image click for full view
        holder.ivImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(position);
            }
        });

        // Add main photo indicator for first image
        if (position == 0) {
            holder.tvPosition.setText("MAIN");
            holder.tvPosition.setBackgroundResource(R.drawable.bg_main_indicator);
        } else {
            holder.tvPosition.setText((position + 1) + "/" + imageUris.size());
            holder.tvPosition.setBackgroundResource(R.drawable.bg_position_indicator);
        }
    }

    @Override
    public int getItemCount() {
        // ✅ FIXED: Null check
        return imageUris != null ? imageUris.size() : 0;
    }

    // ✅ NEW: Method to update data safely
    public void updateData(List<Uri> newImageUris) {
        this.imageUris = newImageUris;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvPosition;
        ImageButton btnRemove;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvPosition = itemView.findViewById(R.id.tv_position);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}