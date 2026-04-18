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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {

    public interface OnBookingConfirmListener {
        void onConfirmClick(Map<String, Object> booking, int position);
    }

    public interface OnBookingClickListener {
        void onBookingClick(Map<String, Object> booking, int position);
    }

    public interface OnBookingCancelListener {
        void onCancelClick(Map<String, Object> booking, int position);
    }

    private Context context;
    private List<Map<String, Object>> bookingList;
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    private OnBookingConfirmListener confirmListener;
    private OnBookingClickListener clickListener;
    private OnBookingCancelListener cancelListener;

    public AdminBookingAdapter(Context context, List<Map<String, Object>> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    public void setConfirmListener(OnBookingConfirmListener listener) {
        this.confirmListener = listener;
    }

    public void setClickListener(OnBookingClickListener listener) {
        this.clickListener = listener;
    }

    public void setCancelListener(OnBookingCancelListener listener) {
        this.cancelListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> booking = bookingList.get(position);

        // User info
        String userName = "";
        Object userObj = booking.get("user");
        if (userObj instanceof Map) {
            Map<String, Object> user = (Map<String, Object>) userObj;
            userName = getStr(user, "fullName");
            if (userName.isEmpty()) userName = getStr(user, "email");
        }
        if (userName.isEmpty()) userName = getStr(booking, "userName");
        holder.tvUser.setText(userName.isEmpty() ? "N/A" : userName);

        // Court info
        String courtInfo = "";
        Object courtObj = booking.get("court");
        if (courtObj instanceof Map) {
            Map<String, Object> court = (Map<String, Object>) courtObj;
            courtInfo = getStr(court, "name");

            Object venueObj = court.get("venue");
            if (venueObj instanceof Map) {
                Map<String, Object> venue = (Map<String, Object>) venueObj;
                String venueName = getStr(venue, "name");

                // Lấy tên Category (Môn thể thao)
                String categoryName = "";
                Object categoryObj = venue.get("category");
                if (categoryObj instanceof Map) {
                    categoryName = getStr((Map<String, Object>) categoryObj, "name");
                }

                // Kết hợp chuỗi: Ví dụ: Sân 2 - Tên Sân - Môn thể thao
                if (!venueName.isEmpty()) {
                    courtInfo += " - " + venueName;
                }
                if (!categoryName.isEmpty()) {
                    courtInfo += " - " + categoryName;
                }
            }
        }

        if (courtInfo.isEmpty()) {
            courtInfo = getStr(booking, "courtName");
        }
        holder.tvCourt.setText(courtInfo.isEmpty() ? "--" : courtInfo);

        // Date
        holder.tvDate.setText(getStr(booking, "bookingDate"));

        // Time
        String startTime = getStr(booking, "startTime");
        String endTime = getStr(booking, "endTime");
        holder.tvTime.setText(startTime + " - " + endTime);

        // Price
        double totalPrice = getDouble(booking, "totalPrice");
        if (totalPrice > 0) {
            holder.tvPrice.setText(currencyFormat.format(totalPrice) + "d");
        } else {
            holder.tvPrice.setText("--");
        }

        // Status badge
        String status = getStr(booking, "status");
        holder.tvStatus.setText(getStatusLabel(status));
        applyStatusStyle(holder.tvStatus, status);

        // Hiển thị nút Xác nhận
        if ("PENDING".equals(status)) {
            holder.btnConfirm.setVisibility(View.VISIBLE);
            holder.btnConfirm.setOnClickListener(v -> {
                if (confirmListener != null) {
                    confirmListener.onConfirmClick(booking, holder.getAdapterPosition());
                }
            });
        } else {
            holder.btnConfirm.setVisibility(View.GONE);
        }

        // Hiển thị nút Hủy lịch
        if ("PENDING".equals(status) || "CONFIRMED".equals(status)) {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> {
                if (cancelListener != null) {
                    cancelListener.onCancelClick(booking, holder.getAdapterPosition());
                }
            });
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }

        // Click to show detail
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onBookingClick(booking, position);
            }
        });
    }

    private void applyStatusStyle(TextView badge, String status) {
        int bgColor;
        int textColor;
        switch (status) {
            case "PENDING":
                bgColor = 0xFFFFF9C4;
                textColor = 0xFFF57F17;
                break;
            case "CONFIRMED":
                bgColor = 0xFFE8F5E9;
                textColor = 0xFF2E7D32;
                break;
            case "COMPLETED":
                bgColor = 0xFFE3F2FD;
                textColor = 0xFF1565C0;
                break;
            case "CANCELLED":
                bgColor = 0xFFFFCDD2;
                textColor = 0xFFC62828;
                break;
            default:
                bgColor = 0xFFEEEEEE;
                textColor = 0xFF757575;
                break;
        }
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(20f);
        badge.setBackground(bg);
        badge.setTextColor(textColor);
    }

    private String getStatusLabel(String status) {
        switch (status) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã huỷ";
            default: return status;
        }
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(val));
        } catch (Exception e) {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvCourt, tvDate, tvTime, tvPrice, tvStatus;
        MaterialButton btnConfirm, btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_booking_user);
            tvCourt = itemView.findViewById(R.id.tv_booking_court);
            tvDate = itemView.findViewById(R.id.tv_booking_date);
            tvTime = itemView.findViewById(R.id.tv_booking_time);
            tvPrice = itemView.findViewById(R.id.tv_booking_price);
            tvStatus = itemView.findViewById(R.id.tv_booking_status);
            btnConfirm = itemView.findViewById(R.id.btn_confirm_booking);
            btnCancel = itemView.findViewById(R.id.btn_cancel_booking);
        }
    }
}