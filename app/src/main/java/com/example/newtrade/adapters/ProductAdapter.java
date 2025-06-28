// app/src/main/java/com/example/newtrade/adapters/ProductAdapter.java
package com.example.newtrade.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.newtrade.R;
import com.example.newtrade.models.Product;
import com.example.newtrade.utils.Constants;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final String TAG = "ProductAdapter";

    private List<Product> products;
    private OnProductClickListener listener;
    private Context context;

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
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        // Set text data
        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText(product.getFormattedPrice());
        holder.tvLocation.setText(product.getLocation());

        // Set condition chip
        if (holder.tvCondition != null) {
            holder.tvCondition.setText(product.getCondition());
        }

        // Load image using Constants helper
        String imageUrl = product.getPrimaryImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullImageUrl = Constants.getFullImageUrl(imageUrl);

            Log.d(TAG, "Loading image for product " + product.getId() + ": " + fullImageUrl);

            Glide.with(context)
                    .load(fullImageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_product)
                            .error(R.drawable.placeholder_product)
                            .centerCrop()
                            .transform(new RoundedCorners(16)))
                    .into(holder.ivProductImage);
        } else {
            Log.d(TAG, "No image URL for product " + product.getId() + ", using placeholder");
            holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    // ✅ FIX: XÓA override notifyDataSetChanged() vì nó là final method
    // Sử dụng super.notifyDataSetChanged() trực tiếp từ bên ngoài

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvTitle;
        TextView tvPrice;
        TextView tvLocation;
        TextView tvCondition;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCondition = itemView.findViewById(R.id.tv_condition);
        }
    }
}