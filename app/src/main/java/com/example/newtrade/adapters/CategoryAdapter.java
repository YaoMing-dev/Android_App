// File: app/src/main/java/com/example/newtrade/adapters/CategoryAdapter.java
package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categories;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCategoryIcon;
        private final TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
        }

        void bind(Category category, OnCategoryClickListener listener) {
            if (tvCategoryName != null) {
                tvCategoryName.setText(category.getName());
            }

            // Set category icon
            if (ivCategoryIcon != null) {
                ivCategoryIcon.setImageResource(getCategoryIcon(category.getName()));
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }

        private int getCategoryIcon(String categoryName) {
            if (categoryName == null) return R.drawable.ic_category_default;

            switch (categoryName.toLowerCase()) {
                case "electronics":
                    return R.drawable.ic_electronics;
                case "fashion":
                    return R.drawable.ic_fashion;
                case "home & garden":
                case "home":
                    return R.drawable.ic_home_category;
                case "books & education":
                case "books":
                    return R.drawable.ic_books;
                case "sports":
                    return R.drawable.ic_sports;
                case "beauty & health":
                case "beauty":
                    return R.drawable.ic_beauty;
                case "vehicles":
                    return R.drawable.ic_vehicles;
                case "toys & kids":
                case "toys":
                    return R.drawable.ic_toys;
                default:
                    return R.drawable.ic_category_default;
            }
        }
    }
}