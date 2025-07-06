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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private static final String TAG = "ReviewAdapter";

    private List<Review> reviews;
    private Context context;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

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
            // Reviewer name
            tvReviewerName.setText(review.getReviewerName() != null ? review.getReviewerName() : "Anonymous");

            // Reviewer avatar
            if (review.getReviewerAvatarUrl() != null && !review.getReviewerAvatarUrl().isEmpty()) {
                Glide.with(context)
                        .load(review.getReviewerAvatarUrl())
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .circleCrop()
                        .into(ivReviewerAvatar);
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_user_placeholder)
                        .circleCrop()
                        .into(ivReviewerAvatar);
            }

            // Date
            tvCreatedAt.setText(review.getCreatedAt() != null ? review.getCreatedAt() : "");

            // Rating
            int rating = review.getRating();
            ratingBar.setRating(rating);
            tvRatingText.setText(rating + "/5 ★");

            // Comment
            if (review.hasComment()) {
                tvComment.setVisibility(View.VISIBLE);
                tvComment.setText(review.getComment());
            } else {
                tvComment.setVisibility(View.GONE);
            }

            // Product title (if available from transaction)
            if (review.getTransaction() != null && review.getTransaction().getProductTitle() != null) {
                tvProductTitle.setVisibility(View.VISIBLE);
                tvProductTitle.setText("Product: " + review.getTransaction().getProductTitle());
            } else {
                tvProductTitle.setVisibility(View.GONE);
            }
        }
    }
}