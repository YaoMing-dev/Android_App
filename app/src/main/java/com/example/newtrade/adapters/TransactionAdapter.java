// app/src/main/java/com/example/newtrade/adapters/TransactionAdapter.java
package com.example.newtrade.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.models.Transaction;
import com.example.newtrade.utils.SharedPrefsManager;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private static final String TAG = "TransactionAdapter";

    private Context context;
    private List<Transaction> transactions;
    private OnTransactionClickListener listener;
    private SharedPrefsManager prefsManager;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onReviewClick(Transaction transaction);
        void onContactClick(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> transactions, OnTransactionClickListener listener) {
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
        this.prefsManager = SharedPrefsManager.getInstance(context);
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private CircleImageView ivOtherPartyAvatar;
        private TextView tvProductTitle;
        private TextView tvOtherPartyName;
        private TextView tvTransactionRole;
        private TextView tvFinalAmount;
        private TextView tvStatus;
        private TextView tvCreatedAt;
        private TextView tvDeliveryMethod;
        private TextView btnReview;
        private TextView btnContact;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            ivOtherPartyAvatar = itemView.findViewById(R.id.iv_other_party_avatar);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvOtherPartyName = itemView.findViewById(R.id.tv_other_party_name);
            tvTransactionRole = itemView.findViewById(R.id.tv_transaction_role);
            tvFinalAmount = itemView.findViewById(R.id.tv_final_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            tvDeliveryMethod = itemView.findViewById(R.id.tv_delivery_method);
            btnReview = itemView.findViewById(R.id.btn_review);
            btnContact = itemView.findViewById(R.id.btn_contact);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionClick(transactions.get(getAdapterPosition()));
                }
            });

            btnReview.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReviewClick(transactions.get(getAdapterPosition()));
                }
            });

            btnContact.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactClick(transactions.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Transaction transaction) {
            Long currentUserId = prefsManager.getUserId();

            // Product info
            tvProductTitle.setText(transaction.getProductTitle() != null ?
                    transaction.getProductTitle() : "Product");

            // Product image
            if (transaction.getProductImageUrl() != null && !transaction.getProductImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(transaction.getProductImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .centerCrop()
                        .into(ivProductImage);
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_image_placeholder)
                        .into(ivProductImage);
            }

            // Other party info
            String otherPartyName = transaction.getOtherPartyName(currentUserId);
            tvOtherPartyName.setText(otherPartyName != null ? otherPartyName : "Unknown User");

            // Other party avatar
            String otherPartyAvatarUrl = transaction.getOtherPartyAvatarUrl(currentUserId);
            if (otherPartyAvatarUrl != null && !otherPartyAvatarUrl.isEmpty()) {
                Glide.with(context)
                        .load(otherPartyAvatarUrl)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .circleCrop()
                        .into(ivOtherPartyAvatar);
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_user_placeholder)
                        .circleCrop()
                        .into(ivOtherPartyAvatar);
            }

            // Transaction role
            String role = transaction.getTransactionRole(currentUserId);
            tvTransactionRole.setText(role);

            // Set role color
            if ("Purchase".equals(role)) {
                tvTransactionRole.setTextColor(context.getColor(android.R.color.holo_blue_dark));
            } else if ("Sale".equals(role)) {
                tvTransactionRole.setTextColor(context.getColor(android.R.color.holo_green_dark));
            }

            // Amount
            tvFinalAmount.setText(transaction.getFormattedPrice());

            // Status
            tvStatus.setText(transaction.getStatusDisplayText());
            tvStatus.setTextColor(context.getColor(transaction.getStatusColor()));

            // Date
            tvCreatedAt.setText(transaction.getCreatedAt() != null ?
                    transaction.getCreatedAt() : "");

            // Delivery method
            tvDeliveryMethod.setText(transaction.getDeliveryMethodText());

            // Review button visibility and state
            if (transaction.isCompleted() && transaction.isCanReview() && !transaction.isHasReviewed()) {
                btnReview.setVisibility(View.VISIBLE);
                btnReview.setText("Leave Review");
                btnReview.setEnabled(true);
            } else if (transaction.isHasReviewed()) {
                btnReview.setVisibility(View.VISIBLE);
                btnReview.setText("Reviewed");
                btnReview.setEnabled(false);
            } else {
                btnReview.setVisibility(View.GONE);
            }

            // Contact button
            btnContact.setVisibility(View.VISIBLE);
        }
    }
}