// app/src/main/java/com/example/newtrade/adapters/PaymentHistoryAdapter.java
package com.example.newtrade.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.Payment;
import com.example.newtrade.models.PaymentStatus;
import com.example.newtrade.utils.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder> {

    private static final String TAG = "PaymentHistoryAdapter";

    private Context context;
    private List<Payment> payments;
    private OnPaymentClickListener listener;

    public interface OnPaymentClickListener {
        void onPaymentClick(Payment payment);
        void onRefundClick(Payment payment);
    }

    public PaymentHistoryAdapter(Context context, List<Payment> payments, OnPaymentClickListener listener) {
        this.context = context;
        this.payments = payments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_history, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return payments != null ? payments.size() : 0;
    }

    class PaymentViewHolder extends RecyclerView.ViewHolder {

        private MaterialCardView cardPayment;
        private ImageView ivPaymentMethod;
        private TextView tvTransactionId;
        private TextView tvAmount;
        private TextView tvPaymentMethod;
        private TextView tvStatus;
        private TextView tvDate;
        private TextView tvDescription;
        private MaterialButton btnRefund;
        private View statusIndicator;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);

            cardPayment = itemView.findViewById(R.id.card_payment);
            ivPaymentMethod = itemView.findViewById(R.id.iv_payment_method);
            tvTransactionId = itemView.findViewById(R.id.tv_transaction_id);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDescription = itemView.findViewById(R.id.tv_description);
            btnRefund = itemView.findViewById(R.id.btn_refund);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
        }

        public void bind(Payment payment) {
            // Transaction ID
            tvTransactionId.setText("Transaction #" + payment.getTransactionId());

            // Amount
            tvAmount.setText(payment.getDisplayAmount());

            // Payment method
            tvPaymentMethod.setText(payment.getPaymentMethod().getDisplayName());
            setPaymentMethodIcon(payment.getPaymentMethod());

            // Status
            tvStatus.setText(payment.getStatusDisplay());
            setStatusColor(payment.getStatus());

            // Date
            if (payment.getCreatedAt() != null) {
                tvDate.setText(DateUtils.formatDateTime(payment.getCreatedAt().toString()));
            }


            // Description
            if (payment.getTransaction() != null && payment.getTransaction().getProduct() != null) {
                tvDescription.setText(payment.getTransaction().getProduct().getTitle());
            } else {
                tvDescription.setText("Payment transaction");
            }

            // Refund button visibility
            btnRefund.setVisibility(canRefund(payment) ? View.VISIBLE : View.GONE);

            // Click listeners
            cardPayment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentClick(payment);
                }
            });

            btnRefund.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRefundClick(payment);
                }
            });
        }

        private void setPaymentMethodIcon(com.example.newtrade.models.PaymentMethod paymentMethod) {
            int iconRes;
            switch (paymentMethod) {
                case CARD:
                    iconRes = R.drawable.ic_credit_card;
                    break;
                case DIGITAL_WALLET:
                    iconRes = R.drawable.ic_wallet;
                    break;
                case BANK_TRANSFER:
                    iconRes = R.drawable.ic_bank;
                    break;
                case CASH:
                    iconRes = R.drawable.ic_cash;
                    break;
                default:
                    iconRes = R.drawable.ic_payment;
                    break;
            }
            ivPaymentMethod.setImageResource(iconRes);
        }

        private void setStatusColor(PaymentStatus status) {
            int colorRes;
            int indicatorColorRes;

            switch (status) {
                case SUCCEEDED:
                    colorRes = R.color.success;
                    indicatorColorRes = R.color.success;
                    break;
                case PENDING:
                case PROCESSING:
                    colorRes = R.color.warning;
                    indicatorColorRes = R.color.warning;
                    break;
                case FAILED:
                case CANCELED:
                    colorRes = R.color.error;
                    indicatorColorRes = R.color.error;
                    break;
                case REFUNDED:
                case PARTIALLY_REFUNDED:
                    colorRes = R.color.info;
                    indicatorColorRes = R.color.info;
                    break;
                default:
                    colorRes = R.color.on_surface_variant;
                    indicatorColorRes = R.color.outline;
                    break;
            }

            tvStatus.setTextColor(context.getResources().getColor(colorRes, null));
            statusIndicator.setBackgroundColor(context.getResources().getColor(indicatorColorRes, null));
        }

        private boolean canRefund(Payment payment) {
            // Only show refund button for successful payments
            return payment.getStatus() == PaymentStatus.SUCCEEDED &&
                    payment.getPaymentMethod() != com.example.newtrade.models.PaymentMethod.CASH;
        }
    }

    public void updatePayments(List<Payment> newPayments) {
        this.payments = newPayments;
        notifyDataSetChanged();
    }

    public void addPayments(List<Payment> newPayments) {
        int startPosition = this.payments.size();
        this.payments.addAll(newPayments);
        notifyItemRangeInserted(startPosition, newPayments.size());
    }
}