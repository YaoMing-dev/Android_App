// File: OfferAdapter.java - SỬA TOÀN BỘ
package com.example.newtrade.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.Offer;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private static final String TAG = "OfferAdapter";

    // ✅ CONSTANTS for ViewType
    private static final int VIEW_TYPE_SENT = 0;
    private static final int VIEW_TYPE_RECEIVED = 1;

    private List<Offer> offers;
    private OnOfferClickListener listener;
    private OnOfferActionListener actionListener;
    private String currentTab; // "SENT" or "RECEIVED"
    private NumberFormat priceFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    public interface OnOfferClickListener {
        void onOfferClick(Offer offer);
    }

    public interface OnOfferActionListener {
        void onOfferAccepted(Offer offer);
        void onOfferRejected(Offer offer);
        void onOfferCountered(Offer offer, double counterAmount, String message);
        void onOfferCancelled(Offer offer);
    }

    public OfferAdapter(List<Offer> offers, String currentTab, OnOfferClickListener listener) {
        this.offers = offers;
        this.currentTab = currentTab != null ? currentTab : "SENT";
        this.listener = listener;

        android.util.Log.d(TAG, "✅ OfferAdapter created with tab: " + this.currentTab);
    }

    public void setOnOfferActionListener(OnOfferActionListener actionListener) {
        this.actionListener = actionListener;
    }

    // ✅ CRITICAL: getItemViewType để phân biệt layout
    @Override
    public int getItemViewType(int position) {
        int viewType = "RECEIVED".equals(currentTab) ? VIEW_TYPE_RECEIVED : VIEW_TYPE_SENT;
        android.util.Log.d(TAG, "✅ getItemViewType: position=" + position + ", currentTab=" + currentTab + ", viewType=" + viewType);
        return viewType;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        boolean isReceivedLayout;

        if (viewType == VIEW_TYPE_RECEIVED) {
            layoutId = R.layout.item_offer_received;
            isReceivedLayout = true;
            android.util.Log.d(TAG, "✅ Creating RECEIVED ViewHolder");
        } else {
            layoutId = R.layout.item_offer_sent;
            isReceivedLayout = false;
            android.util.Log.d(TAG, "✅ Creating SENT ViewHolder");
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new OfferViewHolder(view, isReceivedLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        if (offers != null && position >= 0 && position < offers.size()) {
            Offer offer = offers.get(position);
            android.util.Log.d(TAG, "✅ Binding offer: " + offer.getId() + ", tab: " + currentTab + ", isReceived: " + holder.isReceivedLayout);
            holder.bind(offer, currentTab);
        }
    }

    @Override
    public int getItemCount() {
        return offers != null ? offers.size() : 0;
    }

    // ✅ CRITICAL: updateTab phải trigger notifyDataSetChanged để rebuild ViewHolders
    public void updateTab(String newTab) {
        android.util.Log.d(TAG, "🔄 updateTab: " + currentTab + " -> " + newTab);
        this.currentTab = newTab != null ? newTab : "SENT";
        notifyDataSetChanged(); // ✅ Trigger ViewHolder recreation
    }

    class OfferViewHolder extends RecyclerView.ViewHolder {
        // Common views
        TextView tvProductTitle, tvOfferAmount, tvOriginalPrice, tvStatus, tvMessage, tvDateTime;
        TextView tvUserName;

        // SENT layout views
        Button btnCancel; // Only in SENT layout

        // RECEIVED layout views
        LinearLayout llActionButtons; // Only in RECEIVED layout
        Button btnAccept, btnReject, btnCounter; // Only in RECEIVED layout

        boolean isReceivedLayout;

        public OfferViewHolder(@NonNull View itemView, boolean isReceivedLayout) {
            super(itemView);
            this.isReceivedLayout = isReceivedLayout;

            android.util.Log.d(TAG, "✅ OfferViewHolder created, isReceivedLayout: " + isReceivedLayout);

            // Initialize common views
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvOfferAmount = itemView.findViewById(R.id.tv_offer_amount);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvUserName = itemView.findViewById(R.id.tv_user_name);

            // ✅ Initialize layout-specific views
            if (isReceivedLayout) {
                // RECEIVED layout - có action buttons
                llActionButtons = itemView.findViewById(R.id.ll_action_buttons);
                btnAccept = itemView.findViewById(R.id.btn_accept);
                btnReject = itemView.findViewById(R.id.btn_reject);
                btnCounter = itemView.findViewById(R.id.btn_counter);

                android.util.Log.d(TAG, "✅ RECEIVED views initialized - llActionButtons: " + (llActionButtons != null));
            } else {
                // SENT layout - có cancel button
                btnCancel = itemView.findViewById(R.id.btn_cancel);

                android.util.Log.d(TAG, "✅ SENT views initialized - btnCancel: " + (btnCancel != null));
            }

            // Click listener for item
            itemView.setOnClickListener(v -> {
                try {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        int position = getAdapterPosition();
                        if (offers != null && position >= 0 && position < offers.size()) {
                            listener.onOfferClick(offers.get(position));
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Click error", e);
                }
            });
        }

        public void bind(Offer offer, String currentTab) {
            if (offer == null) return;

            try {
                // ✅ COMMON DATA BINDING
                setTextSafely(tvProductTitle, offer.getProductTitle(), "Product");
                setTextSafely(tvOfferAmount, formatPrice(offer.getOfferAmount()) + " VNĐ", "0 VNĐ");
                setTextSafely(tvOriginalPrice, "Original: " + formatPrice(offer.getOriginalPrice()) + " VNĐ", "Original: 0 VNĐ");
                setTextSafely(tvStatus, offer.getStatusDisplayText(), "Unknown");
                setTextSafely(tvDateTime, offer.getCreatedAt(), "Unknown date");

                // ✅ MESSAGE
                if (tvMessage != null) {
                    String message = offer.getMessage();
                    if (message != null && !message.isEmpty()) {
                        tvMessage.setText(message);
                        tvMessage.setVisibility(View.VISIBLE);
                    } else {
                        tvMessage.setVisibility(View.GONE);
                    }
                }

                // ✅ USER NAME - phân biệt SENT vs RECEIVED
                if (tvUserName != null) {
                    if ("RECEIVED".equals(currentTab)) {
                        String buyerName = offer.getBuyerName();
                        tvUserName.setText("From: " + (buyerName != null ? buyerName : "Unknown"));
                    } else {
                        String sellerName = offer.getSellerName();
                        tvUserName.setText("To: " + (sellerName != null ? sellerName : "Unknown"));
                    }
                }

                // ✅ STATUS COLOR
                setStatusColor(offer.getStatus());

                // ✅ LAYOUT-SPECIFIC LOGIC
                if (isReceivedLayout) {
                    setupReceivedButtons(offer);
                } else {
                    setupSentButtons(offer);
                }

            } catch (Exception e) {
                android.util.Log.e(TAG, "Error in bind: " + e.getMessage());
                setTextSafely(tvProductTitle, "Error loading offer", "Error");
            }
        }

        // ✅ SETUP BUTTONS cho RECEIVED layout
        private void setupReceivedButtons(Offer offer) {
            if (llActionButtons == null) {
                android.util.Log.e(TAG, "❌ llActionButtons is NULL!");
                return;
            }

            // ✅ CHỈ HIỆN BUTTONS CHO PENDING OFFERS
            if (!offer.isPending()) {
                llActionButtons.setVisibility(View.GONE);
                android.util.Log.d(TAG, "✅ Offer not pending, hiding buttons. Status: " + offer.getStatus());
                return;
            }

            // ✅ PENDING OFFER - HIỆN BUTTONS
            llActionButtons.setVisibility(View.VISIBLE);
            android.util.Log.d(TAG, "✅ Showing action buttons for pending offer");

            // Accept button
            if (btnAccept != null) {
                btnAccept.setOnClickListener(v -> {
                    android.util.Log.d(TAG, "✅ Accept clicked");
                    if (actionListener != null) {
                        actionListener.onOfferAccepted(offer);
                    }
                });
            }

            // Reject button
            if (btnReject != null) {
                btnReject.setOnClickListener(v -> {
                    android.util.Log.d(TAG, "✅ Reject clicked");
                    if (actionListener != null) {
                        actionListener.onOfferRejected(offer);
                    }
                });
            }

            // Counter button
            if (btnCounter != null) {
                btnCounter.setOnClickListener(v -> {
                    android.util.Log.d(TAG, "✅ Counter clicked");
                    showCounterDialog(offer);
                });
            }
        }

        // ✅ SETUP BUTTONS cho SENT layout
        private void setupSentButtons(Offer offer) {
            if (btnCancel == null) {
                android.util.Log.e(TAG, "❌ btnCancel is NULL!");
                return;
            }

            // ✅ CHỈ HIỆN CANCEL CHO PENDING OFFERS
            if (offer.isPending()) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> {
                    android.util.Log.d(TAG, "✅ Cancel clicked");
                    if (actionListener != null) {
                        actionListener.onOfferCancelled(offer);
                    }
                });
            } else {
                btnCancel.setVisibility(View.GONE);
            }
        }

        // ✅ COUNTER OFFER DIALOG
        private void showCounterDialog(Offer offer) {
            // ✅ Tạo simple dialog vì chưa có layout
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());

            // Tạo simple input layout
            LinearLayout layout = new LinearLayout(itemView.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            final android.widget.EditText etAmount = new android.widget.EditText(itemView.getContext());
            etAmount.setHint("Counter amount (VNĐ)");
            etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(etAmount);

            final android.widget.EditText etMessage = new android.widget.EditText(itemView.getContext());
            etMessage.setHint("Message (optional)");
            etMessage.setLines(3);
            layout.addView(etMessage);

            builder.setTitle("Counter Offer")
                    .setView(layout)
                    .setPositiveButton("Send Counter", (dialog, id) -> {
                        try {
                            String amountStr = etAmount.getText().toString().trim();
                            String message = etMessage.getText().toString().trim();

                            if (amountStr.isEmpty()) {
                                etAmount.setError("Please enter counter amount");
                                return;
                            }

                            double counterAmount = Double.parseDouble(amountStr);

                            if (actionListener != null) {
                                actionListener.onOfferCountered(offer, counterAmount, message);
                            }
                        } catch (NumberFormatException e) {
                            etAmount.setError("Please enter valid number");
                        }
                    })
                    .setNegativeButton("Cancel", null);

            builder.create().show();
        }

        // ✅ HELPER METHODS
        private void setTextSafely(TextView textView, String text, String fallback) {
            if (textView != null) {
                textView.setText(text != null ? text : fallback);
            }
        }

        private String formatPrice(BigDecimal price) {
            if (price == null) return "0";
            return priceFormat.format(price);
        }

        private void setStatusColor(Offer.OfferStatus status) {
            if (tvStatus == null || status == null) return;

            Context context = itemView.getContext();
            switch (status) {
                case PENDING:
                    tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(context, R.color.offer_pending)));
                    break;
                case ACCEPTED:
                    tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(context, R.color.offer_accepted)));
                    break;
                case REJECTED:
                    tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(context, R.color.offer_rejected)));
                    break;
                case COUNTERED:
                    tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(context, R.color.offer_countered)));
                    break;
                default:
                    tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(context, android.R.color.darker_gray)));
                    break;
            }
        }
    }
}