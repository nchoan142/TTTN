package com.conghoan.sportbooking.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conghoan.sportbooking.R;

import java.util.List;
import java.util.Map;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    // OnUserLongClickListener <=> CallBack
    public interface OnUserLongClickListener {
        void onUserLongClick(Map<String, Object> user, int position);
    }

    public interface OnUserDeleteListener {
        void onUserDelete(Map<String, Object> user, int position);
    }

    private Context context;
    private List<Map<String, Object>> userList;
    private OnUserLongClickListener longClickListener;
    private OnUserDeleteListener deleteListener;

    public AdminUserAdapter(Context context, List<Map<String, Object>> userList, OnUserLongClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.longClickListener = listener;
    }

    public void setDeleteListener(OnUserDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> user = userList.get(position);

        String fullName = getStr(user, "fullName");
        String email = getStr(user, "email");
        String phone = getStr(user, "phone");
        String role = getStr(user, "role");

        holder.tvName.setText(fullName.isEmpty() ? "N/A" : fullName);
        holder.tvEmail.setText(email);
        holder.tvPhone.setText(phone.isEmpty() ? "--" : phone);

        // Initials
        if (!fullName.isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length >= 2) {
                holder.tvInitials.setText((parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase());
            } else {
                holder.tvInitials.setText(parts[0].substring(0, 1).toUpperCase());
            }
        } else {
            holder.tvInitials.setText("U");
        }

        // Role badge
        holder.tvUserRole.setText(role);
        int badgeColor;
        int textColor;
        switch (role) {
            case "ADMIN":
                badgeColor = 0xFFFFCDD2;
                textColor = 0xFFC62828;
                break;
            case "OWNER":
                badgeColor = 0xFFFFF9C4;
                textColor = 0xFFF57F17;
                break;
            default:
                badgeColor = 0xFFE8F5E9;
                textColor = 0xFF2E7D32;
                break;
        }
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(badgeColor);
        badgeBg.setCornerRadius(20f);
        holder.tvUserRole.setBackground(badgeBg);
        holder.tvUserRole.setTextColor(textColor);

        // Long-press để thay đổi role của các user
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onUserLongClick(user, position);
            }
            return true;
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onUserDelete(user, position);
            }
        });

        // Ẩn button delete user nếu role là ADMIN
        if ("ADMIN".equals(role)) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvEmail, tvPhone, tvUserRole;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tv_user_initials);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvPhone = itemView.findViewById(R.id.tv_user_phone);
            tvUserRole = itemView.findViewById(R.id.tv_user_role);
            btnDelete = itemView.findViewById(R.id.btn_delete_user);
        }
    }
}
