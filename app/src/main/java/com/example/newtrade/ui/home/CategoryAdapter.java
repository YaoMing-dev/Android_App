// app/src/main/java/com/example/newtrade/ui/home/CategoryAdapter.java
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
import com.example.newtrade.models.Category;
import com.example.newtrade.utils.Constants;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<Category> categories;
    private final OnCategoryClickListener clickListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener clickListener) {
        this.categories = categories;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);

        // Set category name
        holder.tvName.setText(category.getName());

        // Set product count
        int productCount = category.getProductCount();
        if (productCount > 0) {
            holder.tvCount.setText(productCount + " items");
            holder.tvCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvCount.setVisibility(View.GONE);
        }

        // Load category icon
        loadCategoryIcon(holder.ivIcon, category);

        // Click listener
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCategoryClick(category);
            }
        });

        // Add ripple effect
        holder.cardView.setRippleColor(
                holder.itemView.getContext().getColorStateList(R.color.ripple_color));
    }

    private void loadCategoryIcon(ImageView imageView, Category category) {
        String iconUrl = category.getIconImageUrl();

        if (iconUrl != null && !iconUrl.isEmpty()) {
            // Load real category icon
            Glide.with(imageView.getContext())
                    .load(iconUrl)
                    .placeholder(getCategoryPlaceholder(category.getName()))
                    .error(getCategoryPlaceholder(category.getName()))
                    .transform(new RoundedCorners(16))
                    .into(imageView);
        } else {
            // Use placeholder based on category name
            imageView.setImageResource(getCategoryPlaceholder(category.getName()));
        }
    }

    private int getCategoryPlaceholder(String categoryName) {
        if (categoryName == null) return R.drawable.ic_category_default;

        String name = categoryName.toLowerCase();

        // Map category names to appropriate icons
        if (name.contains("electronic") || name.contains("phone") || name.contains("computer")) {
            return R.drawable.ic_category_electronics;
        } else if (name.contains("fashion") || name.contains("clothing") || name.contains("apparel")) {
            return R.drawable.ic_category_fashion;
        } else if (name.contains("home") || name.contains("furniture") || name.contains("garden")) {
            return R.drawable.ic_category_home;
        } else if (name.contains("sport") || name.contains("fitness") || name.contains("exercise")) {
            return R.drawable.ic_category_sports;
        } else if (name.contains("book") || name.contains("education") || name.contains("study")) {
            return R.drawable.ic_category_books;
        } else if (name.contains("vehicle") || name.contains("car") || name.contains("motor")) {
            return R.drawable.ic_category_vehicles;
        } else if (name.contains("beauty") || name.contains("health") || name.contains("cosmetic")) {
            return R.drawable.ic_category_beauty;
        } else if (name.contains("toy") || name.contains("kid") || name.contains("baby")) {
            return R.drawable.ic_category_toys;
        } else if (name.contains("pet") || name.contains("animal")) {
            return R.drawable.ic_category_pets;
        } else {
            return R.drawable.ic_category_default;
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView ivIcon;
        TextView tvName;
        TextView tvCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_category);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvCount = itemView.findViewById(R.id.tv_product_count);
        }
    }
}