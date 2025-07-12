// app/src/main/java/com/example/newtrade/adapters/RecommendationSectionAdapter.java
package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.RecommendationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationSectionAdapter extends RecyclerView.Adapter<RecommendationSectionAdapter.SectionViewHolder> {

    private final List<RecommendationSection> sections;
    private final OnProductClickListener onProductClickListener;
    private final OnSectionHeaderClickListener onSectionHeaderClickListener;

    // ✅ Store scroll positions to preserve horizontal scroll state
    private final Map<String, Integer> scrollPositions = new HashMap<>();

    public interface OnProductClickListener {
        void onProductClick(Product product, String recommendationType);
    }

    public interface OnSectionHeaderClickListener {
        void onSectionHeaderClick(String sectionTitle, String sectionType);
    }

    public RecommendationSectionAdapter(List<RecommendationSection> sections,
                                        OnProductClickListener onProductClickListener,
                                        OnSectionHeaderClickListener onSectionHeaderClickListener) {
        this.sections = sections;
        this.onProductClickListener = onProductClickListener;
        this.onSectionHeaderClickListener = onSectionHeaderClickListener;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        RecommendationSection section = sections.get(position);
        holder.bind(section);
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    @Override
    public void onViewRecycled(@NonNull SectionViewHolder holder) {
        super.onViewRecycled(holder);
        // ✅ Save scroll position when view is recycled
        if (holder.currentSectionType != null && holder.rvProducts != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) holder.rvProducts.getLayoutManager();
            if (layoutManager != null) {
                int scrollPosition = layoutManager.findFirstVisibleItemPosition();
                scrollPositions.put(holder.currentSectionType, scrollPosition);
            }
        }
    }

    class SectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSectionTitle;
        private final TextView tvSectionSubtitle;
        private final TextView tvViewAll;
        private final RecyclerView rvProducts;

        private String currentSectionType;
        private ProductAdapter productAdapter;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tv_section_title);
            tvSectionSubtitle = itemView.findViewById(R.id.tv_section_subtitle);
            tvViewAll = itemView.findViewById(R.id.tv_view_all);
            rvProducts = itemView.findViewById(R.id.rv_section_products);
        }

        public void bind(RecommendationSection section) {
            currentSectionType = section.getSectionType();

            // Set section header
            tvSectionTitle.setText(section.getTitle());
            if (section.getSubtitle() != null && !section.getSubtitle().isEmpty()) {
                tvSectionSubtitle.setText(section.getSubtitle());
                tvSectionSubtitle.setVisibility(View.VISIBLE);
            } else {
                tvSectionSubtitle.setVisibility(View.GONE);
            }

            // Show/hide "View All" button
            if (section.isShowViewAll()) {
                tvViewAll.setVisibility(View.VISIBLE);
                tvViewAll.setOnClickListener(v -> {
                    if (onSectionHeaderClickListener != null) {
                        onSectionHeaderClickListener.onSectionHeaderClick(section.getTitle(), section.getSectionType());
                    }
                });
            } else {
                tvViewAll.setVisibility(View.GONE);
            }

            // Setup horizontal RecyclerView for products
            setupProductsRecyclerView(section);
        }

        private void setupProductsRecyclerView(RecommendationSection section) {
            // Create product adapter
            productAdapter = new ProductAdapter(section.getProducts(), product -> {
                if (onProductClickListener != null) {
                    onProductClickListener.onProductClick(product, section.getSectionType());
                }
            });

            // Setup horizontal layout manager
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    itemView.getContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
            );

            // ✅ Performance optimization for nested RecyclerView
            layoutManager.setInitialPrefetchItemCount(4); // Prefetch 4 items for smooth scrolling

            rvProducts.setLayoutManager(layoutManager);
            rvProducts.setAdapter(productAdapter);

            // ✅ Performance optimizations
            rvProducts.setHasFixedSize(true);
            rvProducts.setNestedScrollingEnabled(false);

            // ✅ Restore scroll position if available
            Integer savedPosition = scrollPositions.get(section.getSectionType());
            if (savedPosition != null && savedPosition >= 0) {
                layoutManager.scrollToPosition(savedPosition);
            }
        }
    }
}