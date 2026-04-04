package com.conghoan.sportbooking.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.activity.BookingDetailActivity;
import com.conghoan.sportbooking.model.BookingItem;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnCancelClickListener {
        void onCancelClick(BookingItem booking, int position);
    }

    private Context context;
    private List<BookingItem> bookingList;
    private OnCancelClickListener cancelListener;
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    public BookingAdapter(Context context, List<BookingItem> bookingList, OnCancelClickListener cancelListener) {
        this.context = context;
        this.bookingList = bookingList;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingItem booking = bookingList.get(position);

        holder.tvVenueName.setText(booking.getVenueName());
        holder.tvCourtName.setText(booking.getCourtName());
        holder.tvDate.setText(booking.getDate());
        holder.tvTime.setText(booking.getStartTime() + " - " + booking.getEndTime());

        // Format giá tiền
        if (booking.getTotalPrice() > 0) {
            holder.tvTotalPrice.setText(currencyFormat.format(booking.getTotalPrice()) + "đ");
        } else {
            holder.tvTotalPrice.setText("--");
        }

        // Mã đặt sân
        holder.tvBookingId.setText("#BK" + String.format("%06d", booking.getId()));

        // Badge trạng thái
        String status = booking.getStatus();
        holder.tvStatus.setText(getStatusLabel(status));
        applyStatusBadgeStyle(holder.tvStatus, status);

        // Chỉ hiện nút huỷ khi PENDING
        if ("PENDING".equals(status)) {
            holder.llActions.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && cancelListener != null) {
                    cancelListener.onCancelClick(booking, adapterPosition);
                }
            });
        } else {
            holder.llActions.setVisibility(View.GONE);
        }

        // Click item → mở chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingDetailActivity.class);
            intent.putExtra("bookingId", booking.getId());
            intent.putExtra("venueName", booking.getVenueName());
            intent.putExtra("courtName", booking.getCourtName());
            intent.putExtra("date", booking.getDate());
            intent.putExtra("startTime", booking.getStartTime());
            intent.putExtra("endTime", booking.getEndTime());
            intent.putExtra("status", booking.getStatus());
            intent.putExtra("totalPrice", booking.getTotalPrice());
            context.startActivity(intent);
        });
    }

    private void applyStatusBadgeStyle(TextView badge, String status) {
        int bgRes;
        int textColor;
        switch (status) {
            case "PENDING":
                bgRes = R.drawable.bg_status_pending;
                textColor = 0xFFF57F17;
                break;
            case "CONFIRMED":
                bgRes = R.drawable.bg_status_confirmed;
                textColor = 0xFF2E7D32;
                break;
            case "COMPLETED":
                bgRes = R.drawable.bg_status_completed;
                textColor = 0xFF1565C0;
                break;
            case "CANCELLED":
                bgRes = R.drawable.bg_status_cancelled;
                textColor = 0xFFC62828;
                break;
            default:
                bgRes = R.drawable.bg_status_pending;
                textColor = 0xFF757575;
                break;
        }
        badge.setBackgroundResource(bgRes);
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

    public void removeItem(int position) {
        bookingList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, bookingList.size());
    }

    public void updateStatus(int position, String newStatus) {
        bookingList.get(position).setStatus(newStatus);
        notifyItemChanged(position);
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvVenueName, tvCourtName, tvDate, tvTime, tvStatus, tvTotalPrice, tvBookingId;
        LinearLayout llActions;
        MaterialButton btnCancel;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVenueName = itemView.findViewById(R.id.tv_venue_name);
            tvCourtName = itemView.findViewById(R.id.tv_court_name);
            tvDate = itemView.findViewById(R.id.tv_booking_date);
            tvTime = itemView.findViewById(R.id.tv_booking_time);
            tvStatus = itemView.findViewById(R.id.tv_status_badge);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvBookingId = itemView.findViewById(R.id.tv_booking_id);
            llActions = itemView.findViewById(R.id.ll_actions);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }
    }
}
