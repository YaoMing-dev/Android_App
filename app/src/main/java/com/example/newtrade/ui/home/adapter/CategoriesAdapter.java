// app/src/main/java/com/example/newtrade/ui/home/adapter/CategoriesAdapter.java
package com.example.newtrade.ui.home.adapter;

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

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoriesAdapter(List<Category> categories, OnCategoryClickListener listener) {
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

        // Category name
        holder.tvName.setText(category.getName());

        // Category icon (you can map category names to drawable resources)
        int iconRes = getCategoryIcon(category.getName());
        holder.ivIcon.setImageResource(iconRes);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    private int getCategoryIcon(String categoryName) {
        // Map category names to drawable resources
        switch (categoryName.toLowerCase()) {
            case "electronics": return R.drawable.ic_category_electronics;
            case "fashion": return R.drawable.ic_category_fashion;
            case "home": return R.drawable.ic_category_home;
            case "sports": return R.drawable.ic_category_sports;
            case "books": return R.drawable.ic_category_books;
            case "vehicles": return R.drawable.ic_category_vehicles;
            case "toys": return R.drawable.ic_category_toys;
            case "services": return R.drawable.ic_category_services;
            default: return R.drawable.ic_category_other;
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}