// app/src/main/java/com/example/newtrade/ui/profile/adapter/TransactionAdapter.java
package com.example.newtrade.ui.profile.adapter;

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
import com.example.newtrade.models.Transaction;
import com.example.newtrade.utils.DateTimeUtils;
import com.google.android.material.chip.Chip;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions, OnTransactionClickListener listener) {
        this.transactions = transactions;
        this.listener = listener;
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
        Transaction transaction = transactions.get(position);

        // Product info
        if (transaction.getProduct() != null) {
            holder.tvProductTitle.setText(transaction.getProduct().getTitle());

            // Load product image
            String imageUrl = transaction.getProduct().getFirstImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .transform(new RoundedCorners(8))
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.ic_error_image)
                        .into(holder.ivProductImage);
            } else {
                holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
            }
        }

        // Other user info (buyer or seller)
        // TODO: Determine if showing buyer or seller based on current user

        // Amount
        holder.tvAmount.setText(transaction.getFormattedAmount());

        // Date
        if (transaction.getTransactionDate() != null) {
            String formattedDate = DateTimeUtils.formatProductDate(transaction.getTransactionDate());
            holder.tvDate.setText(formattedDate);
        }

        // Status
        if (transaction.getStatus() != null) {
            holder.chipStatus.setText(transaction.getStatus().getDisplayName());
            setupStatusChip(holder.chipStatus, transaction.getStatus());
        }

        // Payment method
        if (transaction.getPaymentMethod() != null) {
            holder.tvPaymentMethod.setText(transaction.getPaymentMethod().getDisplayName());
            holder.tvPaymentMethod.setVisibility(View.VISIBLE);
        } else {
            holder.tvPaymentMethod.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(transaction);
            }
        });
    }

    private void setupStatusChip(Chip chip, Transaction.TransactionStatus status) {
        switch (status) {
            case COMPLETED:
                chip.setChipBackgroundColorResource(R.color.status_completed);
                break;
            case PENDING:
                chip.setChipBackgroundColorResource(R.color.status_pending);
                break;
            case CONFIRMED:
                chip.setChipBackgroundColorResource(R.color.status_confirmed);
                break;
            case CANCELLED:
                chip.setChipBackgroundColorResource(R.color.status_cancelled);
                break;
            case DISPUTED:
                chip.setChipBackgroundColorResource(R.color.status_disputed);
                break;
            default:
                chip.setChipBackgroundColorResource(R.color.status_default);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductTitle, tvOtherUser, tvAmount, tvDate, tvPaymentMethod;
        Chip chipStatus;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvOtherUser = itemView.findViewById(R.id.tv_other_user);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            chipStatus = itemView.findViewById(R.id.chip_status);
        }
    }
}