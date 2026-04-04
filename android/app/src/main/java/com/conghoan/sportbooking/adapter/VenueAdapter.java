package com.conghoan.sportbooking.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.activity.VenueDetailActivity;
import com.conghoan.sportbooking.model.VenueItem;

import java.util.List;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.VenueViewHolder> {

    private Context context;
    private List<VenueItem> venueList;
    private int lastPosition = -1;

    public VenueAdapter(Context context, List<VenueItem> venueList) {
        this.context = context;
        this.venueList = venueList;
    }

    @NonNull
    @Override
    public VenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_venue, parent, false);
        return new VenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueViewHolder holder, int position) {
        VenueItem venue = venueList.get(position);

        holder.tvName.setText(venue.getName());
        holder.tvAddress.setText(venue.getAddress());
        if (holder.tvTime != null) {
            holder.tvTime.setText(venue.getOpenTime() + " - " + venue.getCloseTime());
        }
        if (holder.tvRating != null) {
            holder.tvRating.setText(String.format("%.1f", venue.getRating()));
        }

        // Load venue image with Glide
        if (holder.ivVenueImage != null && venue.getImageUrl() != null && !venue.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(venue.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_sport_pickleball)
                .error(R.drawable.ic_sport_pickleball)
                .into(holder.ivVenueImage);
        }

        // Stagger animation
        setAnimation(holder.itemView, position);

        holder.itemView.setOnClickListener(v -> openVenueDetail(venue));
        if (holder.btnBook != null) {
            holder.btnBook.setOnClickListener(v -> openVenueDetail(venue));
        }
    }

    private void openVenueDetail(VenueItem venue) {
        Intent intent = new Intent(context, VenueDetailActivity.class);
        intent.putExtra("venueId", venue.getId());
        intent.putExtra("venueName", venue.getName());
        intent.putExtra("address", venue.getAddress());
        intent.putExtra("rating", venue.getRating());
        intent.putExtra("openTime", venue.getOpenTime());
        intent.putExtra("closeTime", venue.getCloseTime());
        intent.putExtra("phone", venue.getPhone());
        intent.putExtra("imageUrl", venue.getImageUrl());
        context.startActivity(intent);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            view.setAlpha(0f);
            view.setTranslationY(50f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(position * 50L)
                    .start();
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return venueList != null ? venueList.size() : 0;
    }

    public void updateList(List<VenueItem> newList) {
        this.venueList = newList;
        lastPosition = -1;
        notifyDataSetChanged();
    }

    public static class VenueViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvTime, tvRating;
        ImageView ivVenueImage;
        Button btnBook;

        public VenueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_venue_name);
            tvAddress = itemView.findViewById(R.id.tv_venue_address);
            tvTime = itemView.findViewById(R.id.tv_venue_hours);
            tvRating = itemView.findViewById(R.id.tv_rating);
            ivVenueImage = itemView.findViewById(R.id.iv_venue_image);
            btnBook = itemView.findViewById(R.id.btn_book);
        }
    }
}
