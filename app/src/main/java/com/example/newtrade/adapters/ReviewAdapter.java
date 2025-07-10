// app/src/main/java/com/example/newtrade/adapters/ReviewAdapter.java
package com.example.newtrade.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.models.Review;
import com.example.newtrade.utils.DateUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private static final String TAG = "ReviewAdapter";

    private Context context;
    private List<Review> reviews;

    // ✅ ADD: Long click listener interface and field
    public interface OnReviewLongClickListener {
        void onReviewLongClick(Review review);
    }

    private OnReviewLongClickListener longClickListener;

    // ✅ KEEP: Original constructors
    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    // ✅ ADD: New constructor with long click listener
    public ReviewAdapter(List<Review> reviews, OnReviewLongClickListener longClickListener) {
        this.reviews = reviews;
        this.longClickListener = longClickListener;
    }

    public ReviewAdapter(Context context, List<Review> reviews, OnReviewLongClickListener longClickListener) {
        this.context = context;
        this.reviews = reviews;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);

        // ✅ ADD: Set long click listener
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onReviewLongClick(review);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    // ✅ KEEP: Existing ViewHolder class unchanged
    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivReviewerAvatar;
        private TextView tvReviewerName;
        private TextView tvCreatedAt;
        private RatingBar ratingBar;
        private TextView tvRatingText;
        private TextView tvComment;
        private TextView tvProductTitle;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReviewerAvatar = itemView.findViewById(R.id.iv_reviewer_avatar);
            tvReviewerName = itemView.findViewById(R.id.tv_reviewer_name);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvRatingText = itemView.findViewById(R.id.tv_rating_text);
            tvComment = itemView.findViewById(R.id.tv_comment);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
        }

        public void bind(Review review) {
            // Reviewer info
            tvReviewerName.setText(review.getReviewerName() != null ?
                    review.getReviewerName() : "Anonymous");

            // Reviewer avatar
            if (review.getReviewerAvatarUrl() != null && !review.getReviewerAvatarUrl().isEmpty()) {
                Glide.with(context)
                        .load(review.getReviewerAvatarUrl())
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .into(ivReviewerAvatar);
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_user_placeholder)
                        .into(ivReviewerAvatar);
            }

            // Rating
            if (review.getRating() != null) {
                ratingBar.setRating(review.getRating());
                ratingBar.setVisibility(View.VISIBLE);
                tvRatingText.setText(review.getRatingText());
                tvRatingText.setVisibility(View.VISIBLE);
            } else {
                ratingBar.setVisibility(View.GONE);
                tvRatingText.setVisibility(View.GONE);
            }

            // Comment
            if (review.hasComment()) {
                tvComment.setText(review.getComment());
                tvComment.setVisibility(View.VISIBLE);
            } else {
                tvComment.setVisibility(View.GONE);
            }

            // Product title
            if (review.getProductTitle() != null) {
                tvProductTitle.setText("Review for: " + review.getProductTitle());
                tvProductTitle.setVisibility(View.VISIBLE);
            } else {
                tvProductTitle.setVisibility(View.GONE);
            }

            // Created date
            if (review.getCreatedAt() != null) {
                tvCreatedAt.setText(DateUtils.getRelativeTime(review.getCreatedAt()));
            } else {
                tvCreatedAt.setText("");
            }
        }
    }
}