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
import com.example.newtrade.models.Offer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private List<Offer> offers;
    private OnOfferActionListener listener;
    private boolean isSellerView; // true if viewing as seller, false if viewing as buyer

    public interface OnOfferActionListener {
        void onAcceptOffer(Offer offer);
        void onRejectOffer(Offer offer);
        void onCounterOffer(Offer offer);
        void onViewOffer(Offer offer);
        void onContactUser(Offer offer);
    }

    public OfferAdapter(List<Offer> offers, OnOfferActionListener listener, boolean isSellerView) {
        this.offers = offers;
        this.listener = listener;
        this.isSellerView = isSellerView;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        Offer offer = offers.get(position);
        holder.bind(offer);
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    class OfferViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductTitle;
        private CircleImageView ivUserAvatar;
        private TextView tvUserName;
        private TextView tvOfferAmount;
        private TextView tvOriginalPrice;
        private TextView tvDiscountPercent;
        private TextView tvMessage;
        private TextView tvOfferDate;
        private Chip chipStatus;
        private MaterialButton btnAccept, btnReject, btnCounter, btnContact;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvOfferAmount = itemView.findViewById(R.id.tv_offer_amount);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvOfferDate = itemView.findViewById(R.id.tv_offer_date);
            chipStatus = itemView.findViewById(R.id.chip_status);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnCounter = itemView.findViewById(R.id.btn_counter);
            btnContact = itemView.findViewById(R.id.btn_contact);
        }

        public void bind(Offer offer) {
            // Product info
            tvProductTitle.setText(offer.getProductTitle());
            if (offer.getProductImage() != null && !offer.getProductImage().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(offer.getProductImage())
                    .placeholder(R.drawable.placeholder_image)
                    .into(ivProductImage);
            }

            // User info (buyer or seller based on view)
            String userName, userAvatar;
            if (isSellerView) {
                userName = offer.getBuyerName();
                userAvatar = offer.getBuyerAvatar();
            } else {
                userName = offer.getSellerName();
                userAvatar = offer.getSellerAvatar();
            }

            tvUserName.setText(userName != null ? userName : "Unknown User");
            if (userAvatar != null && !userAvatar.isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(userAvatar)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(ivUserAvatar);
            }

            // Offer amounts
            tvOfferAmount.setText("$" + String.format("%.2f", offer.getOfferAmount()));

            if (offer.getOriginalPrice() != null) {
                tvOriginalPrice.setText("$" + String.format("%.2f", offer.getOriginalPrice()));
                tvOriginalPrice.setVisibility(View.VISIBLE);

                double discount = offer.getDiscountPercentage();
                if (discount > 0) {
                    tvDiscountPercent.setText("-" + String.format("%.0f", discount) + "%");
                    tvDiscountPercent.setVisibility(View.VISIBLE);
                } else {
                    tvDiscountPercent.setVisibility(View.GONE);
                }
            } else {
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscountPercent.setVisibility(View.GONE);
            }

            // Message
            if (offer.getMessage() != null && !offer.getMessage().trim().isEmpty()) {
                tvMessage.setText("\"" + offer.getMessage() + "\"");
                tvMessage.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setVisibility(View.GONE);
            }

            // Date
            tvOfferDate.setText(formatDate(offer.getCreatedAt()));

            // Status chip
            chipStatus.setText(offer.getStatusDisplayText());
            setStatusChipColor(offer.getStatus());

            // Action buttons visibility based on status and view type
            setupActionButtons(offer);

            // Click listeners
            setupClickListeners(offer);
        }

        private void setStatusChipColor(String status) {
            int colorRes;
            switch (status) {
                case "pending":
                    colorRes = R.color.status_pending;
                    break;
                case "accepted":
                    colorRes = R.color.status_accepted;
                    break;
                case "rejected":
                    colorRes = R.color.status_rejected;
                    break;
                case "countered":
                    colorRes = R.color.status_countered;
                    break;
                case "expired":
                    colorRes = R.color.status_expired;
                    break;
                default:
                    colorRes = R.color.text_secondary;
            }
            chipStatus.setChipBackgroundColorResource(colorRes);
        }

        private void setupActionButtons(Offer offer) {
            // Hide all buttons first
            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnCounter.setVisibility(View.GONE);
            btnContact.setVisibility(View.VISIBLE);

            if (isSellerView && offer.isPending()) {
                // Seller can accept, reject, or counter pending offers
                btnAccept.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                btnCounter.setVisibility(View.VISIBLE);
            } else if (!isSellerView && offer.isCountered()) {
                // Buyer can accept or reject counter offers
                btnAccept.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
            }
        }

        private void setupClickListeners(Offer offer) {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOffer(offer);
                }
            });

            btnAccept.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptOffer(offer);
                }
            });

            btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRejectOffer(offer);
                }
            });

            btnCounter.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCounterOffer(offer);
                }
            });

            btnContact.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactUser(offer);
                }
            });
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                return "Recently";
            }
        }
    }
}
