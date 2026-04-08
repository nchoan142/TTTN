package com.conghoan.sportbooking.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conghoan.sportbooking.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;

public class AdminVenueAdapter extends RecyclerView.Adapter<AdminVenueAdapter.ViewHolder> {

    public interface OnToggleClickListener {
        void onToggleClick(Map<String, Object> venue, int position);
    }

    public interface OnVenueDeleteListener {
        void onDeleteClick(Map<String, Object> venue, int position);
    }

    private Context context;
    private List<Map<String, Object>> venueList;
    private OnToggleClickListener toggleListener;
    private OnVenueDeleteListener deleteListener;

    public AdminVenueAdapter(Context context, List<Map<String, Object>> venueList, OnToggleClickListener listener) {
        this.context = context;
        this.venueList = venueList;
        this.toggleListener = listener;
    }

    public void setOnVenueDeleteListener(OnVenueDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_venue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> venue = venueList.get(position);

        String name = getStr(venue, "name");
        String address = getStr(venue, "address");
        boolean active = getBool(venue, "active");

        holder.tvName.setText(name.isEmpty() ? "N/A" : name);
        holder.tvAddress.setText(address.isEmpty() ? "--" : address);

        // Active dot
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setSize(24, 24);
        dot.setColor(active ? 0xFF2E7D32 : 0xFFEF5350);
        holder.viewDot.setBackground(dot);

        // Status text
        holder.tvStatus.setText(active ? "Đang hoạt động" : "Đã khoá");
        holder.tvStatus.setTextColor(active ? 0xFF2E7D32 : 0xFFEF5350);

        // Toggle button
        holder.btnToggle.setText(active ? "Khoá" : "Mở");
        holder.btnToggle.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(active ? 0xFFEF5350 : 0xFF2E7D32));
        holder.btnToggle.setOnClickListener(v -> {
            if (toggleListener != null) {
                toggleListener.onToggleClick(venue, position);
            }
        });

        // Long-press để xoá sân
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(venue, holder.getAdapterPosition());
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return venueList != null ? venueList.size() : 0;
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private boolean getBool(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        return "true".equalsIgnoreCase(String.valueOf(val));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewDot;
        TextView tvName, tvAddress, tvStatus;
        MaterialButton btnToggle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewDot = itemView.findViewById(R.id.view_active_dot);
            tvName = itemView.findViewById(R.id.tv_venue_name);
            tvAddress = itemView.findViewById(R.id.tv_venue_address);
            tvStatus = itemView.findViewById(R.id.tv_venue_status);
            btnToggle = itemView.findViewById(R.id.btn_toggle);
        }
    }
}