package com.example.newtrade.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.models.Product;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public void addProducts(List<Product> moreProducts) {
        int startPosition = this.products.size();
        this.products.addAll(moreProducts);
        notifyItemRangeInserted(startPosition, moreProducts.size());
    }

    public void clearProducts() {
        this.products.clear();
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProduct;
        private final TextView tvTitle;
        private final TextView tvPrice;
        private final TextView tvLocation;
        private final TextView tvCondition;

        ProductViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvCondition = itemView.findViewById(R.id.tvCondition);
        }

        void bind(Product product, OnProductClickListener listener) {
            tvTitle.setText(product.getTitle());
            tvPrice.setText(String.format("$%.2f", product.getPrice()));
            tvLocation.setText(product.getLocation());
            tvCondition.setText(product.getCondition());

            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(ivProduct.getContext())
                        .load(product.getImages().get(0))
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(ivProduct);
            }

            itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
    }
}
