// app/src/main/java/com/example/newtrade/adapters/OfferAdapter.java
package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.Offer;

import java.text.DecimalFormat;
import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private List<Offer> offers;
    private OnOfferClickListener listener;
    private DecimalFormat priceFormat = new DecimalFormat("#,###");

    public interface OnOfferClickListener {
        void onOfferClick(Offer offer);
    }

    public OfferAdapter(List<Offer> offers, OnOfferClickListener listener) {
        this.offers = offers;
        this.listener = listener;
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
        TextView tvProductTitle, tvOfferAmount, tvOriginalPrice, tvStatus, tvMessage, tvDateTime;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvOfferAmount = itemView.findViewById(R.id.tv_offer_amount);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOfferClick(offers.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Offer offer) {
            tvProductTitle.setText(offer.getProductTitle());
            tvOfferAmount.setText(priceFormat.format(offer.getOfferAmount()) + " VNĐ");
            tvOriginalPrice.setText("Original: " + priceFormat.format(offer.getOriginalPrice()) + " VNĐ");
            tvStatus.setText(offer.getStatus().toString());
            tvMessage.setText(offer.getMessage());
            tvDateTime.setText(offer.getCreatedAt());

            // Set status color
            int statusColor;
            switch (offer.getStatus()) {
                case ACCEPTED:
                    statusColor = itemView.getContext().getColor(android.R.color.holo_green_dark);
                    break;
                case REJECTED:
                    statusColor = itemView.getContext().getColor(android.R.color.holo_red_dark);
                    break;
                case PENDING:
                    statusColor = itemView.getContext().getColor(android.R.color.holo_orange_dark);
                    break;
                default:
                    statusColor = itemView.getContext().getColor(android.R.color.darker_gray);
                    break;
            }
            tvStatus.setTextColor(statusColor);
        }
    }
}