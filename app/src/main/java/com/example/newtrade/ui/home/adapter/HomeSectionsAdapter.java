// app/src/main/java/com/example/newtrade/ui/home/adapter/HomeSectionsAdapter.java
package com.example.newtrade.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.ui.home.HomeFragment;

import java.util.List;

public class HomeSectionsAdapter extends RecyclerView.Adapter<HomeSectionsAdapter.SectionViewHolder> {

    private List<HomeFragment.HomeSection> sections;
    private ProductSectionAdapter.OnProductClickListener listener;

    public HomeSectionsAdapter(List<HomeFragment.HomeSection> sections,
                               ProductSectionAdapter.OnProductClickListener listener) {
        this.sections = sections;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        HomeFragment.HomeSection section = sections.get(position);

        // Section title
        holder.tvTitle.setText(section.getTitle());

        // See all button
        holder.tvSeeAll.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSeeAllClick(section.getType());
            }
        });

        // Products horizontal list
        ProductSectionAdapter adapter = new ProductSectionAdapter(section.getProducts(), listener);
        holder.rvProducts.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        holder.rvProducts.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSeeAll;
        RecyclerView rvProducts;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSeeAll = itemView.findViewById(R.id.tv_see_all);
            rvProducts = itemView.findViewById(R.id.rv_products);
        }
    }
}