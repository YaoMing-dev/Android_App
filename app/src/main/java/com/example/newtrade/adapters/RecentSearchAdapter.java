// app/src/main/java/com/example/newtrade/adapters/RecentSearchAdapter.java
package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;

import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.RecentSearchViewHolder> {

    private List<String> recentSearches;
    private OnRecentSearchClickListener listener;

    public interface OnRecentSearchClickListener {
        void onRecentSearchClick(String query);
        void onRecentSearchDelete(String query);
    }

    public RecentSearchAdapter(List<String> recentSearches, OnRecentSearchClickListener listener) {
        this.recentSearches = recentSearches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecentSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_search, parent, false);
        return new RecentSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentSearchViewHolder holder, int position) {
        String query = recentSearches.get(position);
        holder.bind(query, listener);
    }

    @Override
    public int getItemCount() {
        return recentSearches.size();
    }

    static class RecentSearchViewHolder extends RecyclerView.ViewHolder {
        private TextView tvQuery;
        private ImageView ivDelete;

        public RecentSearchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuery = itemView.findViewById(R.id.tv_query);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }

        public void bind(String query, OnRecentSearchClickListener listener) {
            tvQuery.setText(query);

            // Click to search
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecentSearchClick(query);
                }
            });

            // Click to delete
            ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecentSearchDelete(query);
                }
            });
        }
    }
}