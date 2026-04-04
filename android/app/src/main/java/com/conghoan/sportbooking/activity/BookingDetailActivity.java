package com.conghoan.sportbooking.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.api.ApiClient;
import com.conghoan.sportbooking.api.ApiService;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvStatusLabel, tvStatusDesc, tvBookingCode;
    private TextView tvVenueName, tvCourtName, tvDate, tvTime, tvTotalPrice;
    private View viewStatusBg;
    private ImageView ivStatusIcon;
    private LinearLayout llBottomAction;
    private MaterialButton btnCancelBooking;

    private long bookingId;
    private String status;
    private ApiService apiService;

    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        apiService = ApiClient.getApiService();

        initViews();
        loadIntentData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvStatusLabel = findViewById(R.id.tv_status_label);
        tvStatusDesc = findViewById(R.id.tv_status_desc);
        tvBookingCode = findViewById(R.id.tv_booking_code);
        viewStatusBg = findViewById(R.id.view_status_bg);
        ivStatusIcon = findViewById(R.id.iv_status_icon);
        tvVenueName = findViewById(R.id.tv_venue_name);
        tvCourtName = findViewById(R.id.tv_court_name);
        tvDate = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        llBottomAction = findViewById(R.id.ll_bottom_action);
        btnCancelBooking = findViewById(R.id.btn_cancel_booking);
    }

    private void loadIntentData() {
        bookingId = getIntent().getLongExtra("bookingId", 0);
        String venueName = getIntent().getStringExtra("venueName");
        String courtName = getIntent().getStringExtra("courtName");
        String date = getIntent().getStringExtra("date");
        String startTime = getIntent().getStringExtra("startTime");
        String endTime = getIntent().getStringExtra("endTime");
        status = getIntent().getStringExtra("status");
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0);

        tvVenueName.setText(venueName != null ? venueName : "--");
        tvCourtName.setText(courtName != null ? courtName : "--");
        tvDate.setText(date != null ? date : "--");
        tvTime.setText((startTime != null ? startTime : "") + " - " + (endTime != null ? endTime : ""));
        tvBookingCode.setText("#BK" + String.format("%06d", bookingId));

        if (totalPrice > 0) {
            tvTotalPrice.setText(currencyFormat.format(totalPrice) + "đ");
        } else {
            tvTotalPrice.setText("--");
        }

        applyStatusUI(status);
    }

    private void applyStatusUI(String status) {
        if (status == null) status = "";

        switch (status) {
            case "PENDING":
                tvStatusLabel.setText("Chờ xác nhận");
                tvStatusDesc.setText("Đang chờ chủ sân xác nhận đặt lịch");
                viewStatusBg.setBackgroundResource(R.drawable.bg_status_pending);
                ivStatusIcon.setImageResource(R.drawable.ic_time);
                tvStatusLabel.setTextColor(0xFFF57F17);
                llBottomAction.setVisibility(View.VISIBLE);
                break;
            case "CONFIRMED":
                tvStatusLabel.setText("Đã xác nhận");
                tvStatusDesc.setText("Chủ sân đã xác nhận. Hãy đến đúng giờ!");
                viewStatusBg.setBackgroundResource(R.drawable.bg_status_confirmed);
                ivStatusIcon.setImageResource(R.drawable.ic_check);
                tvStatusLabel.setTextColor(0xFF2E7D32);
                llBottomAction.setVisibility(View.VISIBLE);
                break;
            case "COMPLETED":
                tvStatusLabel.setText("Hoàn thành");
                tvStatusDesc.setText("Buổi đặt sân đã hoàn thành");
                viewStatusBg.setBackgroundResource(R.drawable.bg_status_completed);
                ivStatusIcon.setImageResource(R.drawable.ic_check);
                tvStatusLabel.setTextColor(0xFF1565C0);
                llBottomAction.setVisibility(View.GONE);
                break;
            case "CANCELLED":
                tvStatusLabel.setText("Đã huỷ");
                tvStatusDesc.setText("Đặt lịch đã bị huỷ");
                viewStatusBg.setBackgroundResource(R.drawable.bg_status_cancelled);
                ivStatusIcon.setImageResource(R.drawable.ic_close);
                tvStatusLabel.setTextColor(0xFFC62828);
                llBottomAction.setVisibility(View.GONE);
                break;
            default:
                tvStatusLabel.setText(status);
                tvStatusDesc.setText("");
                llBottomAction.setVisibility(View.GONE);
                break;
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancelBooking.setOnClickListener(v -> showCancelConfirmDialog());
    }

    private void showCancelConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Huỷ đặt lịch")
                .setMessage("Bạn có chắc chắn muốn huỷ đặt lịch này?")
                .setPositiveButton("Xác nhận huỷ", (dialog, which) -> cancelBooking())
                .setNegativeButton("Giữ lại", null)
                .show();
    }

    private void cancelBooking() {
        apiService.cancelBooking(bookingId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    status = "CANCELLED";
                    applyStatusUI(status);
                    Toast.makeText(BookingDetailActivity.this, "Đã huỷ đặt lịch thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                } else {
                    Toast.makeText(BookingDetailActivity.this, "Không thể huỷ đặt lịch. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(BookingDetailActivity.this, "Lỗi kết nối. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
