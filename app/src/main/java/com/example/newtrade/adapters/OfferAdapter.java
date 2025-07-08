// app/src/main/java/com/example/newtrade/adapters/OfferAdapter.java
package com.example.newtrade.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Offer;
import com.example.newtrade.models.StandardResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private List<Offer> offers;
    private OnOfferClickListener listener;
    private OnOfferActionListener actionListener;
    private String currentTab; // "SENT" or "RECEIVED"
    private NumberFormat priceFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    public interface OnOfferClickListener {
        void onOfferClick(Offer offer);
    }

    // ✅ THÊM interface cho offer actions
    public interface OnOfferActionListener {
        void onOfferAccepted(Offer offer);
        void onOfferRejected(Offer offer);
        void onOfferCountered(Offer offer, double counterAmount, String message);
        void onOfferCancelled(Offer offer);
    }

    public OfferAdapter(List<Offer> offers, String currentTab, OnOfferClickListener listener) {
        this.offers = offers;
        this.currentTab = currentTab;
        this.listener = listener;
    }

    // ✅ SETTER cho action listener
    public void setOnOfferActionListener(OnOfferActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ SỬ DỤNG layout khác nhau cho SENT vs RECEIVED
        int layoutId = "RECEIVED".equals(currentTab) ?
                R.layout.item_offer_received : R.layout.item_offer_sent;

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        Offer offer = offers.get(position);
        holder.bind(offer, currentTab);
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    class OfferViewHolder extends RecyclerView.ViewHolder {
        // Common views
        TextView tvProductTitle, tvOfferAmount, tvOriginalPrice, tvStatus, tvMessage, tvDateTime;
        TextView tvUserName; // ✅ THÊM để hiển thị buyer/seller name

        // Action views (for RECEIVED tab)
        LinearLayout llActionButtons;
        Button btnAccept, btnReject, btnCounter, btnCancel;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);

            // Common views
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvOfferAmount = itemView.findViewById(R.id.tv_offer_amount);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvUserName = itemView.findViewById(R.id.tv_user_name); // ✅ THÊM

            // Action views (only in RECEIVED layout)
            llActionButtons = itemView.findViewById(R.id.ll_action_buttons);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnCounter = itemView.findViewById(R.id.btn_counter);
            btnCancel = itemView.findViewById(R.id.btn_cancel);

            // Click listener for item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOfferClick(offers.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Offer offer, String currentTab) {
            // Common data
            tvProductTitle.setText(offer.getProductTitle());
            tvOfferAmount.setText(formatPrice(offer.getOfferAmount()) + " VNĐ");
            tvOriginalPrice.setText("Original: " + formatPrice(offer.getOriginalPrice()) + " VNĐ");
            tvStatus.setText(offer.getStatusDisplayText());
            tvDateTime.setText(offer.getCreatedAt());

            if (offer.getMessage() != null && !offer.getMessage().isEmpty()) {
                tvMessage.setText(offer.getMessage());
                tvMessage.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setVisibility(View.GONE);
            }

            // ✅ HIỂN THỊ USER NAME theo tab
            if ("RECEIVED".equals(currentTab)) {
                // Hiển thị buyer name (người gửi offer)
                tvUserName.setText("From: " + (offer.getBuyerName() != null ? offer.getBuyerName() : "Unknown"));
            } else {
                // Hiển thị seller name (người nhận offer)
                tvUserName.setText("To: " + (offer.getSellerName() != null ? offer.getSellerName() : "Unknown"));
            }

            // ✅ STATUS COLOR
            setStatusColor(offer.getStatus());

            // ✅ ACTION BUTTONS cho RECEIVED tab
            if ("RECEIVED".equals(currentTab) && llActionButtons != null) {
                setupActionButtons(offer);
            }
        }

        private void setStatusColor(Offer.OfferStatus status) {
            int colorRes;
            switch (status) {
                case PENDING:
                    colorRes = R.color.status_pending;
                    break;
                case ACCEPTED:
                    colorRes = R.color.status_success;
                    break;
                case REJECTED:
                    colorRes = R.color.status_error;
                    break;
                case COUNTERED:
                    colorRes = R.color.status_warning;
                    break;
                default:
                    colorRes = R.color.text_secondary;
            }
            tvStatus.setTextColor(itemView.getContext().getResources().getColor(colorRes, null));
        }

        // ✅ SETUP ACTION BUTTONS cho RECEIVED offers
        private void setupActionButtons(Offer offer) {
            if (offer.isPending()) {
                llActionButtons.setVisibility(View.VISIBLE);

                // Accept button
                btnAccept.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onOfferAccepted(offer);
                    }
                });

                // Reject button
                btnReject.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onOfferRejected(offer);
                    }
                });

                // Counter button
                btnCounter.setOnClickListener(v -> showCounterOfferDialog(offer));

            } else {
                llActionButtons.setVisibility(View.GONE);
            }

            // Cancel button for SENT offers with PENDING status
            if (btnCancel != null && "SENT".equals(currentTab) && offer.isPending()) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onOfferCancelled(offer);
                    }
                });
            }
        }

        // ✅ DIALOG cho Counter Offer
        private void showCounterOfferDialog(Offer offer) {
            Context context = itemView.getContext();

            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_counter_offer, null);
            TextInputEditText etCounterAmount = dialogView.findViewById(R.id.et_counter_amount);
            TextInputEditText etCounterMessage = dialogView.findViewById(R.id.et_counter_message);

            new AlertDialog.Builder(context)
                    .setTitle("Counter Offer")
                    .setMessage("Current offer: " + formatPrice(offer.getOfferAmount()) + " VNĐ\nOriginal price: " + formatPrice(offer.getOriginalPrice()) + " VNĐ")
                    .setView(dialogView)
                    .setPositiveButton("Send Counter", (dialog, which) -> {
                        String amount = etCounterAmount.getText().toString().trim();
                        String message = etCounterMessage.getText().toString().trim();

                        if (!amount.isEmpty()) {
                            try {
                                // ✅ FIX: Parse và validate số
                                double counterAmount = Double.parseDouble(amount);

                                // ✅ VALIDATION: Check reasonable range
                                double originalPrice = Double.parseDouble(offer.getOriginalPrice().toString());
                                double minAmount = originalPrice * 0.1; // 10% of original
                                double maxAmount = originalPrice * 2.0; // 200% of original (reasonable max)

                                if (counterAmount < minAmount) {
                                    Toast.makeText(context, "Counter offer too low. Minimum: " + formatPrice(minAmount) + " VNĐ", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (counterAmount > maxAmount) {
                                    Toast.makeText(context, "Counter offer too high. Maximum: " + formatPrice(maxAmount) + " VNĐ", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // ✅ FIX: Round to 2 decimal places để tránh scientific notation
                                counterAmount = Math.round(counterAmount * 100.0) / 100.0;

                                if (actionListener != null) {
                                    actionListener.onOfferCountered(offer, counterAmount, message);
                                }

                            } catch (NumberFormatException e) {
                                Toast.makeText(context, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Please enter counter amount", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private String formatPrice(Object price) {
            if (price == null) return "0";

            try {
                double value = Double.parseDouble(price.toString());
                return priceFormat.format(value);
            } catch (Exception e) {
                return price.toString();
            }
        }
    }

    // ✅ METHOD để update tab
    public void updateTab(String newTab) {
        this.currentTab = newTab;
        notifyDataSetChanged();
    }
}