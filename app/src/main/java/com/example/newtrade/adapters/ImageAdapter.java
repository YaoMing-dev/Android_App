package com.example.newtrade.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<Uri> imageUris;
    private OnImageActionListener listener;

    public interface OnImageActionListener {
        void onImageRemove(int position);
    }

    public ImageAdapter(List<Uri> imageUris, OnImageActionListener listener) {
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
        Uri imageUri = imageUris.get(position);
        holder.bind(imageUri, position);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private ImageButton btnRemove;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }

        public void bind(Uri imageUri, int position) {
            // Load image using Glide
            Glide.with(itemView.getContext())
                .load(imageUri)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(ivImage);

            // Remove button click listener
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageRemove(position);
                }
            });
        }
    }
}
