package com.conghoan.sportbooking.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.activity.VenueDetailActivity;
import com.conghoan.sportbooking.model.VenueItem;

import java.util.List;

public class VenueMapAdapter extends RecyclerView.Adapter<VenueMapAdapter.MapViewHolder> {

    private Context context;
    private List<VenueItem> venueList;
    private int lastPosition = -1;

    public VenueMapAdapter(Context context, List<VenueItem> venueList) {
        this.context = context;
        this.venueList = venueList;
    }

    @NonNull
    @Override
    public MapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_venue_map, parent, false);
        return new MapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapViewHolder holder, int position) {
        VenueItem venue = venueList.get(position);

        holder.tvName.setText(venue.getName());
        holder.tvAddress.setText(venue.getAddress());
        holder.tvRating.setText(String.format("%.1f", venue.getRating()));
        holder.tvHours.setText(venue.getOpenTime() + " - " + venue.getCloseTime());
        holder.tvDistance.setText("Đang tính khoảng cách...");

        if (venue.getCategoryName() != null && !venue.getCategoryName().isEmpty()) {
            holder.tvCategory.setText(venue.getCategoryName());
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        // Nút chỉ đường - mở Google Maps
        holder.btnDirection.setOnClickListener(v -> {
            String address = venue.getAddress();
            if (address != null && !address.isEmpty()) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    context.startActivity(mapIntent);
                } catch (Exception e) {
                    // Nếu không có Google Maps, mở trình duyệt
                    // Uri.endcode(address) mã hóa địa chỉ
                    // theo đúng định dạng của Google Maps
                    Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address));
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                    context.startActivity(webIntent);
                }
            } else {
                Toast.makeText(context, "Không có thông tin địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút xem chi tiết
        holder.btnDetail.setOnClickListener(v -> openVenueDetail(venue));

        // Click cả card cũng mở chi tiết
        holder.itemView.setOnClickListener(v -> openVenueDetail(venue));

        // Animation
        setAnimation(holder.itemView, position);
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

    public static class MapViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvDistance, tvRating, tvHours, tvCategory;
        Button btnDirection, btnDetail;

        public MapViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_venue_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvHours = itemView.findViewById(R.id.tv_hours);
            tvCategory = itemView.findViewById(R.id.tv_category);
            btnDirection = itemView.findViewById(R.id.btn_direction);
            btnDetail = itemView.findViewById(R.id.btn_detail);
        }
    }
}
