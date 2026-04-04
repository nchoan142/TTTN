package com.conghoan.sportbooking.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.api.ApiClient;
import com.conghoan.sportbooking.api.ApiService;
import com.conghoan.sportbooking.model.SlotInfo;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {

    private ImageButton btnBack, btnCalendar, btnDatePrev, btnDateNext;
    private TextView tvSelectedDate, tvVenueName, tvSelectedCount, tvTotalPrice;
    private com.google.android.material.button.MaterialButton btnBook;
    private TableLayout tableGrid;
    private ProgressBar progressLoading;

    private long venueId;
    private String venueName;
    private Calendar selectedDate;
    private ApiService apiService;

    // Grid data
    private int courtCount = 0;
    private int timeSlotCount = 0;
    private String openTime = "05:00";
    private String closeTime = "23:00";
    private List<Long> courtIds = new ArrayList<>();
    private List<String> courtNames = new ArrayList<>();
    private List<String> timeLabels = new ArrayList<>();
    private SlotInfo[][] slotGrid; // [timeIndex][courtIndex]
    private long pricePerSlot = 50000; // default 50k per 30min slot

    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        apiService = ApiClient.getApiService();

        receiveIntentData();
        initViews();
        setupDateDefault();
        setupListeners();
        loadSlots();
    }

    private void receiveIntentData() {
        Intent intent = getIntent();
        venueId = intent.getLongExtra("venueId", 1L);
        venueName = intent.getStringExtra("venueName");
        if (venueName == null) venueName = "Sân thể thao";

        if (intent.hasExtra("openTime")) {
            openTime = intent.getStringExtra("openTime");
        }
        if (intent.hasExtra("closeTime")) {
            closeTime = intent.getStringExtra("closeTime");
        }
        if (intent.hasExtra("pricePerSlot")) {
            pricePerSlot = intent.getLongExtra("pricePerSlot", 50000);
        }
        if (intent.hasExtra("courtIds")) {
            ArrayList<Long> ids = (ArrayList<Long>) intent.getSerializableExtra("courtIds");
            if (ids != null) courtIds = ids;
        }
        if (intent.hasExtra("courtNames")) {
            ArrayList<String> names = intent.getStringArrayListExtra("courtNames");
            if (names != null) courtNames = names;
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnCalendar = findViewById(R.id.btn_calendar);
        btnDatePrev = findViewById(R.id.btn_date_prev);
        btnDateNext = findViewById(R.id.btn_date_next);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvVenueName = findViewById(R.id.tv_venue_name);
        tvSelectedCount = findViewById(R.id.tv_selected_count);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnBook = findViewById(R.id.btn_book);
        tableGrid = findViewById(R.id.table_grid);
        progressLoading = findViewById(R.id.progress_loading);

        if (tvVenueName != null) {
            tvVenueName.setText(venueName);
        }
    }

    private void setupDateDefault() {
        selectedDate = Calendar.getInstance();
        updateDateDisplay();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        if (tvSelectedDate != null) {
            String dateText = sdf.format(selectedDate.getTime());
            // Capitalize first letter
            dateText = dateText.substring(0, 1).toUpperCase() + dateText.substring(1);
            tvSelectedDate.setText(dateText);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (btnCalendar != null) {
            btnCalendar.setOnClickListener(v -> showDatePicker());
        }
        if (btnDatePrev != null) {
            btnDatePrev.setOnClickListener(v -> {
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                Calendar prevDay = (Calendar) selectedDate.clone();
                prevDay.add(Calendar.DAY_OF_MONTH, -1);
                if (!prevDay.before(today)) {
                    selectedDate.add(Calendar.DAY_OF_MONTH, -1);
                    updateDateDisplay();
                    loadSlots();
                }
            });
        }
        if (btnDateNext != null) {
            btnDateNext.setOnClickListener(v -> {
                selectedDate.add(Calendar.DAY_OF_MONTH, 1);
                updateDateDisplay();
                loadSlots();
            });
        }
        if (btnBook != null) {
            btnBook.setOnClickListener(v -> showBookingConfirmDialog());
        }
    }

    private void showDatePicker() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDate.set(y, m, d);
            updateDateDisplay();
            loadSlots();
        }, year, month, day);

        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    // ========== API: Load slots ==========

    private void loadSlots() {
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateStr = apiDateFormat.format(selectedDate.getTime());

        showLoading(true);
        tableGrid.removeAllViews();
        clearSelection();

        apiService.getVenueSlots(venueId, dateStr).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Backend trả ApiResponse wrapper: {success, message, data}
                    Map<String, Object> responseBody = response.body();
                    List<Map<String, Object>> slotList = new ArrayList<>();
                    if (responseBody.containsKey("data") && responseBody.get("data") instanceof List) {
                        slotList = (List<Map<String, Object>>) responseBody.get("data");
                    }
                    parseAndBuildGrid(slotList);
                } else {
                    // Fallback: build demo grid
                    buildDemoGrid();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                // Fallback: build demo grid for offline testing
                buildDemoGrid();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressLoading != null) {
            progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (tableGrid != null) {
            tableGrid.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // ========== Parse API response ==========

    private void parseAndBuildGrid(List<Map<String, Object>> slots) {
        // Determine court count and time slot count from response
        int maxCourt = 0;
        int maxTime = 0;

        for (Map<String, Object> slot : slots) {
            int ci = getIntValue(slot, "courtIndex");
            int ti = getIntValue(slot, "timeIndex");
            if (ci > maxCourt) maxCourt = ci;
            if (ti > maxTime) maxTime = ti;
        }

        courtCount = maxCourt + 1;
        timeSlotCount = maxTime + 1;

        if (courtCount == 0 || timeSlotCount == 0) {
            buildDemoGrid();
            return;
        }

        // Giữ courtNames từ intent nếu đủ, chỉ tạo mới nếu thiếu
        if (courtNames.size() < courtCount) {
            courtNames.clear();
            for (int c = 0; c < courtCount; c++) {
                courtNames.add("Sân " + (c + 1));
            }
        } else if (courtNames.size() > courtCount) {
            // API trả ít sân hơn intent → dùng số từ API
            courtCount = courtNames.size();
        }

        // Build time labels
        buildTimeLabels();

        // Initialize grid
        slotGrid = new SlotInfo[timeSlotCount][courtCount];
        for (int t = 0; t < timeSlotCount; t++) {
            for (int c = 0; c < courtCount; c++) {
                slotGrid[t][c] = new SlotInfo(c, t, SlotInfo.STATUS_AVAILABLE, false);
            }
        }

        // Fill from API data
        for (Map<String, Object> slot : slots) {
            int ci = getIntValue(slot, "courtIndex");
            int ti = getIntValue(slot, "timeIndex");
            String status = getStringValue(slot, "status");
            if (ti < timeSlotCount && ci < courtCount) {
                slotGrid[ti][ci].setStatus(status != null ? status : SlotInfo.STATUS_AVAILABLE);
            }
            // Extract court name if provided
            String courtName = getStringValue(slot, "courtName");
            if (courtName != null && ci < courtNames.size()) {
                courtNames.set(ci, courtName);
            }
            // Extract price if provided
            if (slot.containsKey("price")) {
                long price = getLongValue(slot, "price");
                if (price > 0) pricePerSlot = price;
            }
        }

        buildGridUI();
    }

    private void buildDemoGrid() {
        // Dùng danh sách sân từ intent nếu có, không thì fallback demo
        if (!courtNames.isEmpty()) {
            courtCount = courtNames.size();
        } else {
            courtCount = 4;
            courtNames.clear();
            for (int c = 0; c < courtCount; c++) {
                courtNames.add("Sân " + (c + 1));
            }
        }

        buildTimeLabels();
        timeSlotCount = timeLabels.size();

        slotGrid = new SlotInfo[timeSlotCount][courtCount];
        for (int t = 0; t < timeSlotCount; t++) {
            for (int c = 0; c < courtCount; c++) {
                slotGrid[t][c] = new SlotInfo(c, t, SlotInfo.STATUS_AVAILABLE, false);
            }
        }

        buildGridUI();
    }

    private void buildTimeLabels() {
        timeLabels.clear();
        try {
            String[] openParts = openTime.split(":");
            String[] closeParts = closeTime.split(":");
            int startHour = Integer.parseInt(openParts[0]);
            int startMin = Integer.parseInt(openParts[1]);
            int endHour = Integer.parseInt(closeParts[0]);
            int endMin = Integer.parseInt(closeParts[1]);

            int startTotal = startHour * 60 + startMin;
            int endTotal = endHour * 60 + endMin;

            for (int m = startTotal; m < endTotal; m += 30) {
                int h = m / 60;
                int mi = m % 60;
                timeLabels.add(String.format(Locale.US, "%02d:%02d", h, mi));
            }
        } catch (Exception e) {
            // Default fallback
            for (int h = 5; h < 23; h++) {
                timeLabels.add(String.format(Locale.US, "%02d:00", h));
                timeLabels.add(String.format(Locale.US, "%02d:30", h));
            }
        }
        timeSlotCount = timeLabels.size();
    }

    // ========== Build Grid UI ==========

    private void buildGridUI() {
        tableGrid.removeAllViews();

        int cellWidth = dpToPx(72);
        int cellHeight = dpToPx(42);
        int timeLabelWidth = dpToPx(56);
        int cellMargin = dpToPx(1);

        // === Header row: corner + court names ===
        TableRow headerRow = new TableRow(this);

        // Corner cell (Giờ)
        TextView cornerCell = new TextView(this);
        TableRow.LayoutParams cornerParams = new TableRow.LayoutParams(timeLabelWidth, cellHeight);
        cornerCell.setLayoutParams(cornerParams);
        cornerCell.setGravity(Gravity.CENTER);
        cornerCell.setText("Giờ");
        cornerCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        cornerCell.setTextColor(Color.parseColor("#757575"));
        cornerCell.setTypeface(null, Typeface.BOLD);
        cornerCell.setBackgroundColor(Color.parseColor("#EEEEEE"));
        headerRow.addView(cornerCell);

        // Court name headers
        for (int c = 0; c < courtCount; c++) {
            TextView courtHeader = new TextView(this);
            TableRow.LayoutParams hp = new TableRow.LayoutParams(cellWidth, cellHeight);
            hp.setMargins(cellMargin, 0, 0, 0);
            courtHeader.setLayoutParams(hp);
            courtHeader.setGravity(Gravity.CENTER);
            courtHeader.setText(courtNames.get(c));
            courtHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            courtHeader.setTextColor(Color.parseColor("#1B5E20"));
            courtHeader.setTypeface(null, Typeface.BOLD);
            courtHeader.setBackgroundColor(Color.parseColor("#E8F5E9"));
            courtHeader.setPadding(4, 4, 4, 4);
            headerRow.addView(courtHeader);
        }
        tableGrid.addView(headerRow);

        // === Data rows: time label + slot cells ===
        for (int t = 0; t < timeSlotCount; t++) {
            TableRow row = new TableRow(this);

            // Time label cell
            TextView timeLabel = new TextView(this);
            TableRow.LayoutParams tp = new TableRow.LayoutParams(timeLabelWidth, cellHeight);
            tp.setMargins(0, cellMargin, 0, 0);
            timeLabel.setLayoutParams(tp);
            timeLabel.setGravity(Gravity.CENTER);
            timeLabel.setText(timeLabels.get(t));
            timeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            timeLabel.setTextColor(Color.parseColor("#616161"));
            timeLabel.setBackgroundColor(Color.parseColor("#FAFAFA"));
            timeLabel.setTypeface(null, Typeface.BOLD);
            row.addView(timeLabel);

            // Slot cells
            for (int c = 0; c < courtCount; c++) {
                View cell = createSlotCell(t, c, cellWidth, cellHeight);
                TableRow.LayoutParams cp = (TableRow.LayoutParams) cell.getLayoutParams();
                cp.setMargins(cellMargin, cellMargin, 0, 0);
                row.addView(cell);
            }

            tableGrid.addView(row);
        }
    }

    private View createSlotCell(int timeIdx, int courtIdx, int width, int height) {
        TextView cell = new TextView(this);
        cell.setLayoutParams(new TableRow.LayoutParams(width, height));
        cell.setGravity(Gravity.CENTER);
        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        cell.setPadding(2, 2, 2, 2);

        SlotInfo slot = slotGrid[timeIdx][courtIdx];
        applySlotStyle(cell, slot);

        cell.setOnClickListener(v -> onSlotClicked(timeIdx, courtIdx, (TextView) v));

        return cell;
    }

    private void applySlotStyle(TextView cell, SlotInfo slot) {
        if (slot.isSelected()) {
            cell.setBackgroundResource(R.drawable.bg_slot_selected);
            cell.setTextColor(Color.parseColor("#F57F17"));
            cell.setText("V");
            cell.setTypeface(null, Typeface.BOLD);
        } else {
            String status = slot.getStatus();
            switch (status) {
                case SlotInfo.STATUS_BOOKED:
                    cell.setBackgroundResource(R.drawable.bg_slot_booked);
                    cell.setTextColor(Color.parseColor("#C62828"));
                    cell.setText("X");
                    break;
                case SlotInfo.STATUS_LOCKED:
                    cell.setBackgroundResource(R.drawable.bg_slot_locked);
                    cell.setTextColor(Color.parseColor("#757575"));
                    cell.setText("-");
                    break;
                case SlotInfo.STATUS_EVENT:
                    cell.setBackgroundResource(R.drawable.bg_slot_event);
                    cell.setTextColor(Color.parseColor("#6A1B9A"));
                    cell.setText("SK");
                    break;
                default: // AVAILABLE
                    cell.setBackgroundResource(R.drawable.bg_slot_available);
                    cell.setTextColor(Color.parseColor("#2E7D32"));
                    cell.setText("");
                    break;
            }
            cell.setTypeface(null, Typeface.NORMAL);
        }
    }

    // ========== Slot selection ==========

    private void onSlotClicked(int timeIdx, int courtIdx, TextView cell) {
        SlotInfo slot = slotGrid[timeIdx][courtIdx];

        // Only allow clicking on AVAILABLE slots
        if (!SlotInfo.STATUS_AVAILABLE.equals(slot.getStatus()) && !slot.isSelected()) {
            return;
        }

        // Toggle selection
        slot.setSelected(!slot.isSelected());
        applySlotStyle(cell, slot);

        updateBottomBar();
    }

    private void clearSelection() {
        if (slotGrid == null) return;
        for (int t = 0; t < timeSlotCount; t++) {
            for (int c = 0; c < courtCount; c++) {
                if (slotGrid[t][c] != null) {
                    slotGrid[t][c].setSelected(false);
                }
            }
        }
        updateBottomBar();
    }

    private List<SlotInfo> getSelectedSlots() {
        List<SlotInfo> selected = new ArrayList<>();
        if (slotGrid == null) return selected;
        for (int t = 0; t < timeSlotCount; t++) {
            for (int c = 0; c < courtCount; c++) {
                if (slotGrid[t][c] != null && slotGrid[t][c].isSelected()) {
                    selected.add(slotGrid[t][c]);
                }
            }
        }
        return selected;
    }

    private void updateBottomBar() {
        List<SlotInfo> selected = getSelectedSlots();
        int count = selected.size();

        if (count == 0) {
            tvSelectedCount.setText("Chưa chọn slot nào");
            tvTotalPrice.setVisibility(View.GONE);
            btnBook.setEnabled(false);
            btnBook.setAlpha(0.5f);
        } else {
            tvSelectedCount.setText("Đã chọn: " + count + " slot (" + (count * 30) + " phút)");
            long total = count * pricePerSlot;
            tvTotalPrice.setText(currencyFormat.format(total) + "đ");
            tvTotalPrice.setVisibility(View.VISIBLE);
            btnBook.setEnabled(true);
            btnBook.setAlpha(1.0f);
        }
    }

    // ========== Booking confirmation ==========

    private void showBookingConfirmDialog() {
        List<SlotInfo> selected = getSelectedSlots();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 khung giờ!", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        String dateStr = sdf.format(selectedDate.getTime());

        // Build summary per court
        Map<Integer, List<SlotInfo>> byCourt = new HashMap<>();
        for (SlotInfo s : selected) {
            int ci = s.getCourtIndex();
            if (!byCourt.containsKey(ci)) {
                byCourt.put(ci, new ArrayList<>());
            }
            byCourt.get(ci).add(s);
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Sân: ").append(venueName).append("\n");
        summary.append("Ngày: ").append(dateStr).append("\n\n");

        for (Map.Entry<Integer, List<SlotInfo>> entry : byCourt.entrySet()) {
            int courtIdx = entry.getKey();
            List<SlotInfo> courtSlots = entry.getValue();
            String courtName = courtIdx < courtNames.size() ? courtNames.get(courtIdx) : "Sân " + (courtIdx + 1);
            summary.append(courtName).append(":\n");

            for (SlotInfo s : courtSlots) {
                int ti = s.getTimeIndex();
                String startTime = ti < timeLabels.size() ? timeLabels.get(ti) : "??:??";
                String endTime = getEndTime(startTime);
                summary.append("  ").append(startTime).append(" - ").append(endTime).append("\n");
            }
            summary.append("\n");
        }

        long total = selected.size() * pricePerSlot;
        summary.append("Số slot: ").append(selected.size()).append(" (").append(selected.size() * 30).append(" phút)\n");
        summary.append("Tổng tiền: ").append(currencyFormat.format(total)).append("đ");

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt lịch")
                .setMessage(summary.toString())
                .setPositiveButton("Đặt lịch", (dlg, which) -> submitBooking(selected))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private String getEndTime(String startTime) {
        try {
            String[] parts = startTime.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]) + 30;
            if (m >= 60) {
                m -= 60;
                h++;
            }
            return String.format(Locale.US, "%02d:%02d", h, m);
        } catch (Exception e) {
            return "??:??";
        }
    }

    // ========== Submit booking to API ==========

    private void submitBooking(List<SlotInfo> selectedSlots) {
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateStr = apiDateFormat.format(selectedDate.getTime());

        showLoading(true);

        // Backend BookingRequest: {courtId, bookingDate, startTime, endTime, note}
        // Gui tung slot mot theo dung format backend
        final int totalSlots = selectedSlots.size();
        final int[] successCount = {0};
        final int[] failCount = {0};

        for (SlotInfo s : selectedSlots) {
            Map<String, Object> body = new HashMap<>();

            // Map courtIndex sang courtId thuc te
            long courtId = 0;
            if (s.getCourtIndex() < courtIds.size()) {
                courtId = courtIds.get(s.getCourtIndex());
            }
            body.put("courtId", courtId);
            body.put("bookingDate", dateStr);

            String startTime = s.getTimeIndex() < timeLabels.size() ? timeLabels.get(s.getTimeIndex()) : "00:00";
            body.put("startTime", startTime);
            body.put("endTime", getEndTime(startTime));

            apiService.createBooking(body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        successCount[0]++;
                    } else {
                        failCount[0]++;
                    }
                    checkBookingComplete(totalSlots, successCount[0], failCount[0]);
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    failCount[0]++;
                    checkBookingComplete(totalSlots, successCount[0], failCount[0]);
                }
            });
        }
    }

    private void checkBookingComplete(int total, int success, int fail) {
        if (success + fail < total) return; // Chua xong het
        showLoading(false);
        if (success == total) {
            Toast.makeText(BookingActivity.this, "Đặt lịch thành công!", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        } else if (success > 0) {
            Toast.makeText(BookingActivity.this,
                    "Đặt thành công " + success + "/" + total + " slot", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(BookingActivity.this, "Đặt lịch thất bại!", Toast.LENGTH_LONG).show();
        }
    }

    // ========== Utility ==========

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int getIntValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try { return Integer.parseInt((String) val); } catch (Exception e) { return 0; }
        }
        return 0;
    }

    private long getLongValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) {
            try { return Long.parseLong((String) val); } catch (Exception e) { return 0; }
        }
        return 0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
