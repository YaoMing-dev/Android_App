package com.example.newtrade.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newtrade.R;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<String> categories;
    private final OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryAdapter(List<String> categories, OnCategoryClickListener listener) {
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
        String category = categories.get(position);
        holder.bind(category, position == selectedPosition, listener, this);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<String> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int newPosition) {
        int oldPosition = selectedPosition;
        selectedPosition = newPosition;
        notifyItemChanged(oldPosition);
        notifyItemChanged(newPosition);
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        CategoryViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvCategoryName);
        }

        void bind(String category, boolean isSelected, OnCategoryClickListener listener, CategoryAdapter adapter) {
            textView.setText(category);
            textView.setSelected(isSelected);

            itemView.setOnClickListener(v -> {
                adapter.setSelectedPosition(getAdapterPosition());
                listener.onCategoryClick(category);
            });
        }
    }
}
