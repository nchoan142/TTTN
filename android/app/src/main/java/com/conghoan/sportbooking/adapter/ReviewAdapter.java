package com.conghoan.sportbooking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conghoan.sportbooking.R;

import java.util.List;
import java.util.Map;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Map<String, Object>> reviewList;

    public ReviewAdapter(Context context, List<Map<String, Object>> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Map<String, Object> review = reviewList.get(position);

        // User name
        String userName = "Nguoi dung";
        if (review.containsKey("user") && review.get("user") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) review.get("user");
            if (user.containsKey("fullName")) {
                userName = String.valueOf(user.get("fullName"));
            }
        }
        holder.tvUserName.setText(userName);

        // Avatar letter
        if (holder.tvAvatar != null && userName.length() > 0) {
            holder.tvAvatar.setText(String.valueOf(userName.charAt(0)).toUpperCase());
        }

        // Rating
        if (review.containsKey("rating")) {
            Object ratingObj = review.get("rating");
            if (ratingObj instanceof Number) {
                double rating = ((Number) ratingObj).doubleValue();
                holder.tvRating.setText(String.format("%.1f", rating));
            }
        }

        // Comment
        if (review.containsKey("comment")) {
            String comment = String.valueOf(review.get("comment"));
            holder.tvComment.setText(comment);
            holder.tvComment.setVisibility(comment.isEmpty() ? View.GONE : View.VISIBLE);
        } else {
            holder.tvComment.setVisibility(View.GONE);
        }

        // Date
        if (review.containsKey("createdAt")) {
            String createdAt = String.valueOf(review.get("createdAt"));
            // Format: chuyen tu ISO date sang dd/MM/yyyy
            if (createdAt.length() >= 10) {
                try {
                    String datePart = createdAt.substring(0, 10); // yyyy-MM-dd
                    String[] parts = datePart.split("-");
                    if (parts.length == 3) {
                        holder.tvDate.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
                    } else {
                        holder.tvDate.setText(createdAt);
                    }
                } catch (Exception e) {
                    holder.tvDate.setText(createdAt);
                }
            } else {
                holder.tvDate.setText(createdAt);
            }
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public void updateData(List<Map<String, Object>> newData) {
        this.reviewList = newData;
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvUserName, tvRating, tvComment, tvDate;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_review_avatar);
            tvUserName = itemView.findViewById(R.id.tv_review_user_name);
            tvRating = itemView.findViewById(R.id.tv_review_rating);
            tvComment = itemView.findViewById(R.id.tv_review_comment);
            tvDate = itemView.findViewById(R.id.tv_review_date);
        }
    }
}
