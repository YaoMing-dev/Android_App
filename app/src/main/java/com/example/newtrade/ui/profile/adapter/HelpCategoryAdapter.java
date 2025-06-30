// app/src/main/java/com/example/newtrade/ui/profile/adapter/HelpCategoryAdapter.java
package com.example.newtrade.ui.profile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.ui.profile.HelpActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class HelpCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CATEGORY = 0;
    private static final int TYPE_CONTACT = 1;

    private List<HelpActivity.HelpCategory> categories;
    private OnHelpCategoryClickListener listener;

    public interface OnHelpCategoryClickListener {
        void onHelpCategoryClick(HelpActivity.HelpCategory category);
        void onContactSupportClick();
    }

    public HelpCategoryAdapter(List<HelpActivity.HelpCategory> categories, OnHelpCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return position < categories.size() ? TYPE_CATEGORY : TYPE_CONTACT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CATEGORY) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_help_category, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contact_support, parent, false);
            return new ContactViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CategoryViewHolder) {
            HelpActivity.HelpCategory category = categories.get(position);
            CategoryViewHolder categoryHolder = (CategoryViewHolder) holder;

            categoryHolder.tvTitle.setText(category.title);
            categoryHolder.tvDescription.setText(category.description);
            categoryHolder.ivIcon.setImageResource(category.iconRes);

            categoryHolder.cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHelpCategoryClick(category);
                }
            });
        } else if (holder instanceof ContactViewHolder) {
            ContactViewHolder contactHolder = (ContactViewHolder) holder;

            contactHolder.cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactSupportClick();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return categories.size() + 1; // +1 for contact support item
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView ivIcon;
        TextView tvTitle, tvDescription;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }
}