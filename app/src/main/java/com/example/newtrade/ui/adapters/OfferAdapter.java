package com.example.newtrade.ui.adapters;

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
import com.example.newtrade.models.Offer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private final Context context;
    private final List<Offer> offers;
    private final OfferActionListener listener;
    private final NumberFormat currencyFormat;
    private final SimpleDateFormat dateFormat;

    public interface OfferActionListener {
        void onAcceptOffer(Offer offer);
        void onRejectOffer(Offer offer);
        void onCounterOffer(Offer offer);
        void onContactUser(Offer offer);
        void onOfferClick(Offer offer);
    }

    public OfferAdapter(Context context, List<Offer> offers, OfferActionListener listener) {
        this.context = context;
        this.offers = offers;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_offer, parent, false);
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

    public void updateOffers(List<Offer> newOffers) {
        offers.clear();
        offers.addAll(newOffers);
        notifyDataSetChanged();
    }

    public void addOffer(Offer offer) {
        offers.add(0, offer);
        notifyItemInserted(0);
    }

    public void removeOffer(int position) {
        if (position >= 0 && position < offers.size()) {
            offers.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateOffer(Offer updatedOffer) {
        for (int i = 0; i < offers.size(); i++) {
            if (offers.get(i).getId().equals(updatedOffer.getId())) {
                offers.set(i, updatedOffer);
                notifyItemChanged(i);
                break;
            }
        }
    }

    class OfferViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProductImage;
        private final TextView tvProductTitle;
        private final Chip chipStatus;
        private final CircleImageView ivUserAvatar;
        private final TextView tvUserName;
        private final TextView tvOfferDate;
        private final TextView tvOfferAmount;
        private final TextView tvOriginalPrice;
        private final TextView tvDiscountPercent;
        private final TextView tvMessage;
        private final MaterialButton btnContact;
        private final MaterialButton btnReject;
        private final MaterialButton btnCounter;
        private final MaterialButton btnAccept;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            chipStatus = itemView.findViewById(R.id.chip_status);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvOfferDate = itemView.findViewById(R.id.tv_offer_date);
            tvOfferAmount = itemView.findViewById(R.id.tv_offer_amount);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent);
            tvMessage = itemView.findViewById(R.id.tv_message);
            btnContact = itemView.findViewById(R.id.btn_contact);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnCounter = itemView.findViewById(R.id.btn_counter);
            btnAccept = itemView.findViewById(R.id.btn_accept);

            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOfferClick(offers.get(getAdapterPosition()));
                }
            });

            btnAccept.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptOffer(offers.get(getAdapterPosition()));
                }
            });

            btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRejectOffer(offers.get(getAdapterPosition()));
                }
            });

            btnCounter.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCounterOffer(offers.get(getAdapterPosition()));
                }
            });

            btnContact.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactUser(offers.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Offer offer) {
            // Product info
            tvProductTitle.setText(offer.getProductTitle());

            // Load product image
            Glide.with(context)
                    .load(offer.getProductImage())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(ivProductImage);

            // Status chip
            chipStatus.setText(offer.getStatusDisplayText());
            updateStatusChipAppearance(offer.getStatus());

            // User info
            tvUserName.setText(offer.getBuyerName());

            // Load user avatar
            Glide.with(context)
                    .load(offer.getBuyerAvatar())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(ivUserAvatar);

            // Date
            tvOfferDate.setText(formatDate(offer.getCreatedAt()));

            // Offer amount
            tvOfferAmount.setText(currencyFormat.format(offer.getOfferAmount()));

            // Original price
            if (offer.getOriginalPrice() != null && offer.getOriginalPrice() > 0) {
                tvOriginalPrice.setVisibility(View.VISIBLE);
                tvOriginalPrice.setText(currencyFormat.format(offer.getOriginalPrice()));

                // Discount percentage
                double discount = offer.getDiscountPercentage();
                if (discount > 0) {
                    tvDiscountPercent.setVisibility(View.VISIBLE);
                    tvDiscountPercent.setText(String.format(Locale.getDefault(), "-%.0f%%", discount));
                } else {
                    tvDiscountPercent.setVisibility(View.GONE);
                }
            } else {
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscountPercent.setVisibility(View.GONE);
            }

            // Message
            if (offer.getMessage() != null && !offer.getMessage().trim().isEmpty()) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(offer.getMessage());
            } else {
                tvMessage.setVisibility(View.GONE);
            }

            // Action buttons visibility based on status
            updateActionButtons(offer.getStatus());
        }

        private void updateStatusChipAppearance(String status) {
            switch (status) {
                case "pending":
                    chipStatus.setChipBackgroundColorResource(R.color.warning_light);
                    chipStatus.setTextColor(context.getResources().getColor(R.color.warning_dark));
                    break;
                case "accepted":
                    chipStatus.setChipBackgroundColorResource(R.color.success_light);
                    chipStatus.setTextColor(context.getResources().getColor(R.color.success_dark));
                    break;
                case "rejected":
                    chipStatus.setChipBackgroundColorResource(R.color.error_light);
                    chipStatus.setTextColor(context.getResources().getColor(R.color.error_dark));
                    break;
                case "countered":
                    chipStatus.setChipBackgroundColorResource(R.color.info_light);
                    chipStatus.setTextColor(context.getResources().getColor(R.color.info_dark));
                    break;
                case "expired":
                    chipStatus.setChipBackgroundColorResource(R.color.text_light);
                    chipStatus.setTextColor(context.getResources().getColor(R.color.text_secondary));
                    break;
                default:
                    chipStatus.setChipBackgroundColorResource(R.color.background_light);
                    chipStatus.setTextColor(context.getResources().getColor(R.color.text_primary));
                    break;
            }
        }

        private void updateActionButtons(String status) {
            // Hide all buttons first
            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnCounter.setVisibility(View.GONE);
            btnContact.setVisibility(View.VISIBLE); // Contact is always visible

            // Show relevant buttons based on status
            switch (status) {
                case "pending":
                    btnAccept.setVisibility(View.VISIBLE);
                    btnReject.setVisibility(View.VISIBLE);
                    btnCounter.setVisibility(View.VISIBLE);
                    break;
                case "countered":
                    // If this is a counter-offer, show accept/reject again
                    btnAccept.setVisibility(View.VISIBLE);
                    btnReject.setVisibility(View.VISIBLE);
                    break;
                case "accepted":
                case "rejected":
                case "expired":
                    // Only show contact button for finalized offers
                    break;
            }
        }

        private String formatDate(String dateString) {
            try {
                // Assuming the date comes in ISO format from API
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = isoFormat.parse(dateString);

                long timeDiff = System.currentTimeMillis() - date.getTime();
                long hours = timeDiff / (1000 * 60 * 60);
                long days = timeDiff / (1000 * 60 * 60 * 24);

                if (hours < 1) {
                    return "Just now";
                } else if (hours < 24) {
                    return hours + " hours ago";
                } else if (days == 1) {
                    return "Yesterday";
                } else if (days < 7) {
                    return days + " days ago";
                } else {
                    return dateFormat.format(date);
                }
            } catch (Exception e) {
                return dateString; // Return original string if parsing fails
            }
        }
    }
}
