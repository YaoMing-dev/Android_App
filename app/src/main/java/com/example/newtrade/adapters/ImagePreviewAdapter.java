// app/src/main/java/com/example/newtrade/adapters/ImagePreviewAdapter.java
package com.example.newtrade.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;

import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.PreviewViewHolder> {

    private List<Uri> imageUris;

    public ImagePreviewAdapter(List<Uri> imageUris) {
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_preview, parent, false);
        return new PreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);

        Glide.with(holder.itemView.getContext())
                .load(imageUri)
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error)
                .into(holder.ivPreviewImage);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class PreviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPreviewImage;

        PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPreviewImage = itemView.findViewById(R.id.iv_preview_image);
        }
    }
}