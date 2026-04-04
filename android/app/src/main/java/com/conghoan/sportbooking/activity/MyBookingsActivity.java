package com.conghoan.sportbooking.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.adapter.BookingAdapter;
import com.conghoan.sportbooking.api.ApiClient;
import com.conghoan.sportbooking.api.ApiService;
import com.conghoan.sportbooking.model.BookingItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsActivity extends AppCompatActivity {

    private static final String TAG = "MyBookingsActivity";

    private ImageButton btnBack;
    private RecyclerView rvBookings;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;
    private BookingAdapter bookingAdapter;

    private List<BookingItem> allBookings = new ArrayList<>();
    private List<BookingItem> filteredBookings = new ArrayList<>();
    private ApiService apiService;

    private String currentFilter = null; // null = tất cả

    // Tab views
    private TextView tabAll, tabPending, tabConfirmed, tabCompleted, tabCancelled;
    private TextView[] allTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        ApiClient.loadTokenFromPrefs(this);
        apiService = ApiClient.getApiService();

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupSwipeRefresh();

        loadBookingsFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload khi quay lại từ BookingDetailActivity (có thể đã huỷ booking)
        if (!allBookings.isEmpty()) {
            loadBookingsFromApi();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        rvBookings = findViewById(R.id.rv_bookings);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        llEmptyState = findViewById(R.id.ll_empty_state);

        tabAll = findViewById(R.id.tab_all);
        tabPending = findViewById(R.id.tab_pending);
        tabConfirmed = findViewById(R.id.tab_confirmed);
        tabCompleted = findViewById(R.id.tab_completed);
        tabCancelled = findViewById(R.id.tab_cancelled);
        allTabs = new TextView[]{tabAll, tabPending, tabConfirmed, tabCompleted, tabCancelled};

        // Nút "Tìm sân ngay" trong empty state
        View btnExplore = findViewById(R.id.btn_explore);
        if (btnExplore != null) {
            btnExplore.setOnClickListener(v -> finish());
        }
    }

    private void setupToolbar() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> selectTab(null, 0));
        tabPending.setOnClickListener(v -> selectTab("PENDING", 1));
        tabConfirmed.setOnClickListener(v -> selectTab("CONFIRMED", 2));
        tabCompleted.setOnClickListener(v -> selectTab("COMPLETED", 3));
        tabCancelled.setOnClickListener(v -> selectTab("CANCELLED", 4));
    }

    private void selectTab(String status, int tabIndex) {
        currentFilter = status;

        // Cập nhật giao diện tab
        for (int i = 0; i < allTabs.length; i++) {
            if (allTabs[i] == null) continue;
            if (i == tabIndex) {
                allTabs[i].setBackgroundResource(R.drawable.bg_tab_active);
                allTabs[i].setTextColor(0xFF2E7D32); // Green text on white bg
            } else {
                allTabs[i].setBackgroundResource(R.drawable.bg_tab_inactive);
                allTabs[i].setTextColor(0xCCFFFFFF); // White semi on transparent bg
            }
        }

        filterBookings();
    }

    private void filterBookings() {
        filteredBookings.clear();
        if (currentFilter == null) {
            filteredBookings.addAll(allBookings);
        } else {
            for (BookingItem b : allBookings) {
                if (currentFilter.equals(b.getStatus())) {
                    filteredBookings.add(b);
                }
            }
        }
        bookingAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(this, filteredBookings, (booking, position) -> {
            showCancelConfirmDialog(booking, position);
        });
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(bookingAdapter);
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeColors(0xFF2E7D32, 0xFF4CAF50);
            swipeRefresh.setOnRefreshListener(this::loadBookingsFromApi);
        }
    }

    // --- API: Load bookings ---
    private void loadBookingsFromApi() {
        // Hiện loading lần đầu (nếu chưa có dữ liệu)
        if (allBookings.isEmpty()) {
            showLoading(true);
        }

        apiService.getMyBookings().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    allBookings.clear();
                    // Backend trả ApiResponse wrapper: {success, message, data}
                    Map<String, Object> responseBody = response.body();
                    List<Map<String, Object>> bookingList = new ArrayList<>();
                    if (responseBody.containsKey("data") && responseBody.get("data") instanceof List) {
                        bookingList = (List<Map<String, Object>>) responseBody.get("data");
                    }
                    for (Map<String, Object> item : bookingList) {
                        BookingItem booking = mapToBookingItem(item);
                        if (booking != null) {
                            allBookings.add(booking);
                        }
                    }
                    filterBookings();
                } else {
                    Log.e(TAG, "Lỗi tải lịch đặt: " + response.code());
                    Toast.makeText(MyBookingsActivity.this, "Không thể tải lịch đặt", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Lỗi kết nối khi tải lịch đặt", t);
                Toast.makeText(MyBookingsActivity.this, "Lỗi kết nối. Kéo xuống để thử lại", Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private BookingItem mapToBookingItem(Map<String, Object> item) {
        try {
            long id = getIdFromMap(item);
            String venueName = getStringFromMap(item, "venueName");
            String courtName = getStringFromMap(item, "courtName");
            String date = getStringFromMap(item, "date");
            String startTime = getStringFromMap(item, "startTime");
            String endTime = getStringFromMap(item, "endTime");
            String status = getStringFromMap(item, "status");
            double totalPrice = getDoubleFromMap(item, "totalPrice");

            // Hỗ trợ cả trường hợp venue/court nằm trong object lồng nhau
            if (venueName.isEmpty()) {
                Object venueObj = item.get("venue");
                if (venueObj instanceof Map) {
                    venueName = getStringFromMap((Map<String, Object>) venueObj, "name");
                }
            }
            if (courtName.isEmpty()) {
                Object courtObj = item.get("court");
                if (courtObj instanceof Map) {
                    courtName = getStringFromMap((Map<String, Object>) courtObj, "name");
                }
            }

            return new BookingItem(id, venueName, courtName, date, startTime, endTime, status, totalPrice);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi mapping booking", e);
            return null;
        }
    }

    // --- Hủy booking qua API ---
    private void showCancelConfirmDialog(BookingItem booking, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đặt lịch")
                .setMessage("Bạn có chắc muốn hủy đặt lịch tại " + booking.getVenueName()
                        + " vào " + booking.getDate() + " lúc " + booking.getStartTime() + "?")
                .setPositiveButton("Xác nhận hủy", (dialog, which) -> {
                    cancelBookingApi(booking, position);
                })
                .setNegativeButton("Giữ lại", null)
                .show();
    }

    private void cancelBookingApi(BookingItem booking, int position) {
        apiService.cancelBooking(booking.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    // Cập nhật trạng thái trong danh sách gốc
                    for (BookingItem b : allBookings) {
                        if (b.getId() == booking.getId()) {
                            b.setStatus("CANCELLED");
                            break;
                        }
                    }
                    filterBookings();
                    Toast.makeText(MyBookingsActivity.this, "Đã hủy đặt lịch thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Lỗi hủy booking: " + response.code());
                    Toast.makeText(MyBookingsActivity.this, "Không thể hủy đặt lịch. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối khi hủy booking", t);
                Toast.makeText(MyBookingsActivity.this, "Lỗi kết nối. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Tiện ích ---
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyState() {
        if (filteredBookings.isEmpty()) {
            rvBookings.setVisibility(View.GONE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvBookings.setVisibility(View.VISIBLE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
        }
    }

    private long getIdFromMap(Map<String, Object> map) {
        Object val = map.get("id");
        if (val instanceof Number) return ((Number) val).longValue();
        return 0;
    }

    private String getStringFromMap(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private double getDoubleFromMap(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }
}
