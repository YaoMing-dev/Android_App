package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Map<String, Object>> transactions;

    public TransactionAdapter(List<Map<String, Object>> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Map<String, Object> transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductTitle;
        private TextView tvTransactionType;
        private TextView tvAmount;
        private TextView tvDate;
        private TextView tvStatus;
        private TextView tvOtherUser;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvTransactionType = itemView.findViewById(R.id.tv_transaction_type);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvOtherUser = itemView.findViewById(R.id.tv_other_user);
        }

        public void bind(Map<String, Object> transaction) {
            // Product info
            String productTitle = (String) transaction.get("product_title");
            String productImage = (String) transaction.get("product_image");

            tvProductTitle.setText(productTitle != null ? productTitle : "Unknown Product");

            if (productImage != null && !productImage.isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(productImage)
                    .placeholder(R.drawable.placeholder_image)
                    .into(ivProductImage);
            }

            // Transaction details
            String type = (String) transaction.get("type");
            Double amount = (Double) transaction.get("amount");
            String status = (String) transaction.get("status");
            String otherUser = (String) transaction.get("other_user_name");
            String dateStr = (String) transaction.get("created_at");

            tvTransactionType.setText(type != null ? type.toUpperCase() : "UNKNOWN");
            tvAmount.setText(amount != null ? "$" + String.format("%.2f", amount) : "$0.00");
            tvStatus.setText(status != null ? status.toUpperCase() : "UNKNOWN");
            tvOtherUser.setText(otherUser != null ?
                ("SOLD".equals(type) ? "Buyer: " : "Seller: ") + otherUser : "Unknown User");

            // Date
            tvDate.setText(formatDate(dateStr));

            // Set colors based on transaction type
            if ("SOLD".equals(type)) {
                tvTransactionType.setTextColor(itemView.getContext().getColor(R.color.success));
                tvAmount.setTextColor(itemView.getContext().getColor(R.color.success));
            } else {
                tvTransactionType.setTextColor(itemView.getContext().getColor(R.color.primary));
                tvAmount.setTextColor(itemView.getContext().getColor(R.color.text_primary));
            }
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                return "Recently";
            }
        }
    }
}
