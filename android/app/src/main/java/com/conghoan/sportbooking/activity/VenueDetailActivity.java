package com.conghoan.sportbooking.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.adapter.ReviewAdapter;
import com.conghoan.sportbooking.api.ApiClient;
import com.conghoan.sportbooking.api.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VenueDetailActivity extends AppCompatActivity {

    private static final String TAG = "VenueDetailActivity";

    private TextView tvVenueName, tvVenueAddress, tvAddress, tvOpenTime, tvPhone, tvRating;
    private TextView tvPrice, tvCourtCount, tvCourtsEmpty;
    private TextView tvReviewRating, tvReviewCount, tvReviewsEmpty;
    private RecyclerView rvReviews;
    private MaterialButton btnWriteReview;
    private ChipGroup chipGroupCourts;
    private Button btnBookNow;
    private ImageView ivVenueImage;
    private FrameLayout flHeaderGreen;

    private ReviewAdapter reviewAdapter;
    private List<Map<String, Object>> reviewsList = new ArrayList<>();

    private long venueId;
    private String venueName, address, openTime, closeTime, phone, imageUrl;
    private double rating;
    private int ratingCount;
    private double pricePerSlot;

    // Danh s\u00e1ch s\u00e2n con t\u1eeb API
    private List<Map<String, Object>> courtsList = new ArrayList<>();

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail);

        apiService = ApiClient.getApiService();

        receiveIntentData();
        initViews();
        setupToolbar();
        populateData();
        setupListeners();

        // Goi API lay chi tiet san va reviews
        if (venueId > 0) {
            loadVenueDetail();
            loadReviews();
        }
    }

    private void receiveIntentData() {
        Intent intent = getIntent();
        venueId = intent.getLongExtra("venueId", 0L);
        venueName = intent.getStringExtra("venueName");
        address = intent.getStringExtra("address");
        rating = intent.getDoubleExtra("rating", 0.0);
        openTime = intent.getStringExtra("openTime");
        closeTime = intent.getStringExtra("closeTime");
        phone = intent.getStringExtra("phone");
        imageUrl = intent.getStringExtra("imageUrl");
    }

    private void initViews() {
        tvVenueName = findViewById(R.id.tv_venue_name);
        tvVenueAddress = findViewById(R.id.tv_venue_address);
        tvAddress = findViewById(R.id.tv_full_address);
        tvOpenTime = findViewById(R.id.tv_open_hours);
        tvPhone = findViewById(R.id.tv_phone);
        tvRating = findViewById(R.id.tv_rating);
        tvPrice = findViewById(R.id.tv_price);
        tvCourtCount = findViewById(R.id.tv_court_count);
        tvCourtsEmpty = findViewById(R.id.tv_courts_empty);
        chipGroupCourts = findViewById(R.id.chip_group_courts);
        tvReviewRating = findViewById(R.id.tv_review_rating);
        tvReviewCount = findViewById(R.id.tv_review_count);
        tvReviewsEmpty = findViewById(R.id.tv_reviews_empty);
        rvReviews = findViewById(R.id.rv_reviews);
        btnWriteReview = findViewById(R.id.btn_write_review);
        btnBookNow = findViewById(R.id.btn_book_now);
        ivVenueImage = findViewById(R.id.iv_venue_image);
        flHeaderGreen = findViewById(R.id.fl_header_green);

        // Setup reviews RecyclerView
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(this, reviewsList);
        rvReviews.setAdapter(reviewAdapter);
    }

    private void setupToolbar() {
        android.widget.ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (tvVenueName != null) {
            tvVenueName.setText(venueName != null ? venueName : "Chi ti\u1EBFt s\u00E2n");
        }
    }

    private void populateData() {
        loadVenueImage(imageUrl);
        if (tvVenueAddress != null) tvVenueAddress.setText(address != null ? address : "");
        if (tvAddress != null) tvAddress.setText(address != null ? address : "");
        if (tvOpenTime != null) {
            tvOpenTime.setText((openTime != null ? openTime : "06:00")
                    + " - " + (closeTime != null ? closeTime : "22:00") + " (T\u1EA5t c\u1EA3 c\u00E1c ng\u00E0y)");
        }
        if (tvPhone != null) tvPhone.setText(phone != null ? phone : "-");
        if (tvRating != null) tvRating.setText(String.format("%.1f (\u0111\u00E1nh gi\u00E1)", rating));
        if (tvReviewRating != null) tvReviewRating.setText(String.format("%.1f", rating));
        if (tvReviewCount != null) tvReviewCount.setText("(0)");
        if (tvPrice != null) tvPrice.setText("--");
    }

    private void loadVenueDetail() {
        apiService.getVenueById(venueId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Backend trả ApiResponse wrapper: {success, message, data}
                    Map<String, Object> responseBody = response.body();
                    Map<String, Object> data = responseBody;
                    if (responseBody.containsKey("data") && responseBody.get("data") instanceof Map) {
                        data = (Map<String, Object>) responseBody.get("data");
                    }
                    parseAndDisplay(data);
                } else {
                    Log.w(TAG, "API tr\u1EA3 v\u1EC1 l\u1ED7i: " + response.code());
                    // Gi\u1EEF nguy\u00EAn d\u1EEF li\u1EC7u t\u1EEB intent
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "L\u1ED7i g\u1ECDi API: " + t.getMessage());
                // Gi\u1EEF nguy\u00EAn d\u1EEF li\u1EC7u t\u1EEB intent nh\u01B0 fallback
            }
        });
    }

    private void loadVenueImage(String url) {
        if (ivVenueImage != null && url != null && !url.isEmpty()) {
            ivVenueImage.setVisibility(View.VISIBLE);
            if (flHeaderGreen != null) flHeaderGreen.setVisibility(View.GONE);
            Glide.with(this)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.ic_sport_pickleball)
                .error(R.drawable.ic_sport_pickleball)
                .into(ivVenueImage);
        }
    }

    private void parseAndDisplay(Map<String, Object> data) {
        // Parse imageUrl from API
        if (data.containsKey("imageUrl")) {
            String apiImageUrl = String.valueOf(data.get("imageUrl"));
            if (apiImageUrl != null && !apiImageUrl.isEmpty() && !"null".equals(apiImageUrl)) {
                imageUrl = apiImageUrl;
                loadVenueImage(imageUrl);
            }
        }

        // Parse th\u00F4ng tin c\u01A1 b\u1EA3n
        if (data.containsKey("name")) {
            venueName = String.valueOf(data.get("name"));
            if (tvVenueName != null) tvVenueName.setText(venueName);
        }

        if (data.containsKey("address")) {
            address = String.valueOf(data.get("address"));
            if (tvVenueAddress != null) tvVenueAddress.setText(address);
            if (tvAddress != null) tvAddress.setText(address);
        }

        if (data.containsKey("phone")) {
            phone = String.valueOf(data.get("phone"));
            if (tvPhone != null) tvPhone.setText(phone);
        }

        if (data.containsKey("openTime")) {
            openTime = String.valueOf(data.get("openTime"));
        }
        if (data.containsKey("closeTime")) {
            closeTime = String.valueOf(data.get("closeTime"));
        }
        if (tvOpenTime != null) {
            tvOpenTime.setText((openTime != null ? openTime : "06:00")
                    + " - " + (closeTime != null ? closeTime : "22:00") + " (T\u1EA5t c\u1EA3 c\u00E1c ng\u00E0y)");
        }

        // Rating
        if (data.containsKey("rating")) {
            Object ratingObj = data.get("rating");
            if (ratingObj instanceof Number) {
                rating = ((Number) ratingObj).doubleValue();
            }
        }

        if (data.containsKey("ratingCount")) {
            Object countObj = data.get("ratingCount");
            if (countObj instanceof Number) {
                ratingCount = ((Number) countObj).intValue();
            }
        }

        if (tvRating != null) {
            tvRating.setText(String.format("%.1f (%d \u0111\u00E1nh gi\u00E1)", rating, ratingCount));
        }
        if (tvReviewRating != null) {
            tvReviewRating.setText(String.format("%.1f", rating));
        }
        if (tvReviewCount != null) {
            tvReviewCount.setText(String.format("(%d)", ratingCount));
        }

        // Gi\u00E1 thu\u00EA s\u00E2n
        if (data.containsKey("pricePerSlot")) {
            Object priceObj = data.get("pricePerSlot");
            if (priceObj instanceof Number) {
                pricePerSlot = ((Number) priceObj).doubleValue();
            }
        }
        if (tvPrice != null) {
            if (pricePerSlot > 0) {
                NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvPrice.setText(nf.format((long) pricePerSlot) + "\u0111 / slot");
            } else {
                tvPrice.setText("Li\u00EAn h\u1EC7");
            }
        }

        // Danh s\u00E1ch s\u00E2n con (courts)
        if (data.containsKey("courts")) {
            Object courtsObj = data.get("courts");
            if (courtsObj instanceof List) {
                courtsList.clear();
                List<?> rawList = (List<?>) courtsObj;
                for (Object item : rawList) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> court = (Map<String, Object>) item;
                        courtsList.add(court);
                    }
                }
                displayCourts();
            }
        }

        // Reviews visibility duoc quan ly boi loadReviews()
        updateReviewsVisibility();
    }

    private void displayCourts() {
        if (chipGroupCourts == null) return;

        chipGroupCourts.removeAllViews();

        if (courtsList.isEmpty()) {
            if (tvCourtsEmpty != null) {
                tvCourtsEmpty.setText("Ch\u01B0a c\u00F3 s\u00E2n n\u00E0o");
                tvCourtsEmpty.setVisibility(View.VISIBLE);
            }
            if (tvCourtCount != null) tvCourtCount.setText("0 s\u00E2n");
            return;
        }

        if (tvCourtsEmpty != null) tvCourtsEmpty.setVisibility(View.GONE);
        if (tvCourtCount != null) tvCourtCount.setText(courtsList.size() + " s\u00E2n");

        for (int i = 0; i < courtsList.size(); i++) {
            Map<String, Object> court = courtsList.get(i);

            String courtName = "S\u00E2n " + (i + 1);
            if (court.containsKey("name")) {
                courtName = String.valueOf(court.get("name"));
            }

            Chip chip = new Chip(this);
            chip.setText(courtName);
            chip.setTextColor(getResources().getColor(R.color.green_dark));
            chip.setChipBackgroundColor(ColorStateList.valueOf(
                    getResources().getColor(R.color.chip_bg)));
            chip.setChipStrokeColor(ColorStateList.valueOf(
                    getResources().getColor(R.color.green_primary)));
            chip.setChipStrokeWidth(1f);
            chip.setClickable(false);
            chip.setCheckable(false);
            chip.setTextSize(13);

            chipGroupCourts.addView(chip);
        }
    }

    private void loadReviews() {
        apiService.getVenueReviews(venueId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseBody = response.body();
                    Object dataObj = responseBody.get("data");
                    if (dataObj instanceof List) {
                        reviewsList.clear();
                        List<?> rawList = (List<?>) dataObj;
                        for (Object item : rawList) {
                            if (item instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> review = (Map<String, Object>) item;
                                reviewsList.add(review);
                            }
                        }
                        reviewAdapter.updateData(reviewsList);
                        updateReviewsVisibility();
                    }
                } else {
                    Log.w(TAG, "Loi load reviews: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Loi goi API reviews: " + t.getMessage());
            }
        });
    }

    private void updateReviewsVisibility() {
        if (reviewsList.isEmpty()) {
            if (tvReviewsEmpty != null) tvReviewsEmpty.setVisibility(View.VISIBLE);
            if (rvReviews != null) rvReviews.setVisibility(View.GONE);
        } else {
            if (tvReviewsEmpty != null) tvReviewsEmpty.setVisibility(View.GONE);
            if (rvReviews != null) rvReviews.setVisibility(View.VISIBLE);
        }
        // Cap nhat so luong review
        if (tvReviewCount != null) {
            tvReviewCount.setText(String.format("(%d)", reviewsList.size()));
        }
    }

    private void showWriteReviewDialog() {
        // Tao layout dong cho dialog
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        // Label
        TextView tvLabel = new TextView(this);
        tvLabel.setText("Đánh giá của bạn");
        tvLabel.setTextSize(14);
        tvLabel.setTextColor(0xFF424242);
        layout.addView(tvLabel);

        // RatingBar
        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(5);
        android.widget.LinearLayout.LayoutParams ratingParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        ratingParams.topMargin = 16;
        ratingParams.bottomMargin = 24;
        ratingBar.setLayoutParams(ratingParams);
        layout.addView(ratingBar);

        // Label comment
        TextView tvCommentLabel = new TextView(this);
        tvCommentLabel.setText("Nhận xét");
        tvCommentLabel.setTextSize(14);
        tvCommentLabel.setTextColor(0xFF424242);
        layout.addView(tvCommentLabel);

        // EditText comment
        EditText etComment = new EditText(this);
        etComment.setHint("Chia sẻ trải nghiệm của bạn...");
        etComment.setMinLines(3);
        etComment.setMaxLines(5);
        etComment.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        etComment.setBackgroundResource(android.R.drawable.edit_text);
        android.widget.LinearLayout.LayoutParams etParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        etParams.topMargin = 8;
        etComment.setLayoutParams(etParams);
        layout.addView(etComment);

        new AlertDialog.Builder(this)
                .setTitle("Viết đánh giá")
                .setView(layout)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    int ratingValue = (int) ratingBar.getRating();
                    String comment = etComment.getText().toString().trim();

                    if (ratingValue == 0) {
                        Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    submitReview(ratingValue, comment);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void submitReview(int rating, String comment) {
        Map<String, Object> body = new HashMap<>();
        body.put("venueId", venueId);
        body.put("rating", rating);
        body.put("comment", comment);

        apiService.createReview(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseBody = response.body();
                    Object successObj = responseBody.get("success");
                    if (successObj instanceof Boolean && (Boolean) successObj) {
                        Toast.makeText(VenueDetailActivity.this,
                                "Đánh giá thành công", Toast.LENGTH_SHORT).show();
                        // Reload reviews
                        loadReviews();
                    } else {
                        String message = responseBody.containsKey("message")
                                ? String.valueOf(responseBody.get("message"))
                                : "Có lỗi xảy ra";
                        Toast.makeText(VenueDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VenueDetailActivity.this,
                            "Gửi đánh giá thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Loi gui danh gia: " + t.getMessage());
                Toast.makeText(VenueDetailActivity.this,
                        "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnWriteReview.setOnClickListener(v -> showWriteReviewDialog());

        btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("venueId", venueId);
            intent.putExtra("venueName", venueName);
            if (openTime != null) intent.putExtra("openTime", openTime);
            if (closeTime != null) intent.putExtra("closeTime", closeTime);
            if (pricePerSlot > 0) intent.putExtra("pricePerSlot", (long) pricePerSlot);

            // Truyền danh sách sân con
            if (!courtsList.isEmpty()) {
                ArrayList<Long> courtIds = new ArrayList<>();
                ArrayList<String> courtNames = new ArrayList<>();
                for (Map<String, Object> court : courtsList) {
                    // Court ID
                    if (court.containsKey("id")) {
                        Object idObj = court.get("id");
                        if (idObj instanceof Number) {
                            courtIds.add(((Number) idObj).longValue());
                        }
                    }
                    // Court name
                    if (court.containsKey("name")) {
                        courtNames.add(String.valueOf(court.get("name")));
                    }
                }
                intent.putExtra("courtIds", courtIds);
                intent.putStringArrayListExtra("courtNames", courtNames);
            }

            startActivity(intent);
        });
    }

}
