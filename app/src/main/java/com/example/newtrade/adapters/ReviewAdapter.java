package com.example.newtrade.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.models.Review;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
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

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivReviewerAvatar;
        private TextView tvReviewerName;
        private TextView tvReviewDate;
        private RatingBar ratingBar;
        private TextView tvReviewComment;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReviewerAvatar = itemView.findViewById(R.id.iv_reviewer_avatar);
            tvReviewerName = itemView.findViewById(R.id.tv_reviewer_name);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvReviewComment = itemView.findViewById(R.id.tv_review_comment);
        }

        public void bind(Review review) {
            // Reviewer name
            tvReviewerName.setText(review.getReviewerName() != null ?
                review.getReviewerName() : "Anonymous");

            // Reviewer avatar
            if (review.getReviewerAvatar() != null && !review.getReviewerAvatar().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(review.getReviewerAvatar())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(ivReviewerAvatar);
            } else {
                ivReviewerAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            // Rating
            if (review.getRating() != null) {
                ratingBar.setRating(review.getRating());
                ratingBar.setVisibility(View.VISIBLE);
            } else {
                ratingBar.setVisibility(View.GONE);
            }

            // Comment
            if (review.getComment() != null && !review.getComment().trim().isEmpty()) {
                tvReviewComment.setText(review.getComment());
                tvReviewComment.setVisibility(View.VISIBLE);
            } else {
                tvReviewComment.setVisibility(View.GONE);
            }

            // Date
            if (review.getCreatedAt() != null) {
                tvReviewDate.setText(formatDate(review.getCreatedAt()));
            } else {
                tvReviewDate.setText("Recently");
            }
        }

        private String formatDate(String dateString) {
            try {
                // Assuming date format is ISO 8601: "2025-06-27T14:16:56"
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                // Fallback to simple substring if parsing fails
                if (dateString.length() >= 10) {
                    return dateString.substring(0, 10);
                }
                return "Recently";
            }
        }
    }
}
