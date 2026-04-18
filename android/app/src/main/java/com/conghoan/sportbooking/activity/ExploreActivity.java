package com.conghoan.sportbooking.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.adapter.VenueAdapter;
import com.conghoan.sportbooking.api.ApiClient;
import com.conghoan.sportbooking.api.ApiService;
import com.conghoan.sportbooking.model.VenueItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreActivity extends AppCompatActivity {

    private static final String TAG = "ExploreActivity";

    private RecyclerView rvVenues;
    private ProgressBar progressBar;
    private LinearLayout llEmpty;

    private VenueAdapter venueAdapter;
    private List<VenueItem> allVenues = new ArrayList<>();
    private List<VenueItem> displayedVenues = new ArrayList<>();

    private ApiService apiService;

    // Category chips
    private TextView chipAll, chipPickleball, chipBadminton, chipFootball,
            chipTennis, chipVolleyball, chipBasketball;
    private TextView[] categoryChips;
    private String selectedCategory = null;

    // Sort chips
    private TextView sortRating, sortPrice, sortNearest;
    private TextView[] sortChips;
    private int selectedSort = 0; // 0=rating, 1=price, 2=nearest

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        apiService = ApiClient.getApiService();

        initViews();
        setupRecyclerView();
        setupCategoryChips();
        setupSortChips();
        loadVenuesFromApi();
    }

    private void initViews() {
        rvVenues = findViewById(R.id.rv_venues);
        progressBar = findViewById(R.id.progress_bar);
        llEmpty = findViewById(R.id.ll_empty);

        chipAll = findViewById(R.id.chip_all);
        chipPickleball = findViewById(R.id.chip_pickleball);
        chipBadminton = findViewById(R.id.chip_badminton);
        chipFootball = findViewById(R.id.chip_football);
        chipTennis = findViewById(R.id.chip_tennis);
        chipVolleyball = findViewById(R.id.chip_volleyball);
        chipBasketball = findViewById(R.id.chip_basketball);

        categoryChips = new TextView[]{chipAll, chipPickleball, chipBadminton,
                chipFootball, chipTennis, chipVolleyball, chipBasketball};

        sortRating = findViewById(R.id.sort_rating);
        sortPrice = findViewById(R.id.sort_price);
        sortNearest = findViewById(R.id.sort_nearest);

        sortChips = new TextView[]{sortRating, sortPrice, sortNearest};

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        venueAdapter = new VenueAdapter(this, displayedVenues);
        rvVenues.setLayoutManager(new LinearLayoutManager(this));
        rvVenues.setAdapter(venueAdapter);
    }

    private void setupCategoryChips() {
        String[] categoryNames = {null, "Pickleball", "Cầu lông", "Bóng đá",
                "Tennis", "Bóng chuyền", "Bóng rổ"};

        // Khi click 1 môn thể thao, nó sẽ được lưu vào selectedCategory
        for (int i = 0; i < categoryChips.length; i++) {
            final int index = i;
            categoryChips[i].setOnClickListener(v -> {
                selectedCategory = categoryNames[index];
                updateCategoryChipUI(index);
                applyFilterAndSort();
            });
        }
    }

    // Thay đổi màu của chip các môn thể thao khi được chọn
    private void updateCategoryChipUI(int activeIndex) {
        for (int i = 0; i < categoryChips.length; i++) {
            if (i == activeIndex) {
                categoryChips[i].setBackgroundResource(R.drawable.bg_chip_active);
                categoryChips[i].setTextColor(getResources().getColor(R.color.text_white));
            } else {
                categoryChips[i].setBackgroundResource(R.drawable.bg_chip);
                categoryChips[i].setTextColor(getResources().getColor(R.color.green_primary));
            }
        }
    }

    private void setupSortChips() {
        sortRating.setOnClickListener(v -> {
            selectedSort = 0;
            updateSortChipUI(0);
            applyFilterAndSort();
        });
        sortPrice.setOnClickListener(v -> {
            selectedSort = 1;
            updateSortChipUI(1);
            applyFilterAndSort();
        });
        sortNearest.setOnClickListener(v -> {
            selectedSort = 2;
            updateSortChipUI(2);
            applyFilterAndSort();
        });
    }

    // Thay đổi màu của chip đánh giá khi được chọn
    private void updateSortChipUI(int activeIndex) {
        for (int i = 0; i < sortChips.length; i++) {
            if (i == activeIndex) {
                sortChips[i].setBackgroundResource(R.drawable.bg_chip_active);
                sortChips[i].setTextColor(getResources().getColor(R.color.text_white));
            } else {
                sortChips[i].setBackgroundResource(R.drawable.bg_chip);
                sortChips[i].setTextColor(getResources().getColor(R.color.green_primary));
            }
        }
    }

    // Lọc danh sách các sân theo danh mục (category)
    // và sắp xếp theo đánh giá (rating) hoặc giá tiền (price)
    // sau đó, hiển thị lên UI
    private void applyFilterAndSort() {
        displayedVenues.clear();
        if (selectedCategory == null) {
            displayedVenues.addAll(allVenues);
        } else {
            for (VenueItem v : allVenues) {
                if (v.getCategoryName() != null &&
                        v.getCategoryName().equalsIgnoreCase(selectedCategory)) {
                    displayedVenues.add(v);
                }
            }
        }

        switch (selectedSort) {
            case 0: // Đánh giá cao nhất
                displayedVenues.sort((a, b) ->
                        Double.compare(b.getRating(), a.getRating()));
                break;
            case 1: // Giá thấp nhất
                displayedVenues.sort((a, b) ->
                        Double.compare(a.getPricePerSlot(), b.getPricePerSlot()));
                break;
            case 2: // Gần nhất - placeholder, giữ nguyên thứ tự
                break;
        }

        venueAdapter.updateList(displayedVenues);
        updateEmptyState();
    }

    // Kiểm tra xem có sân nào thỏa mãn điều kiện không
    private void updateEmptyState() {
        if (displayedVenues.isEmpty()) {
            llEmpty.setVisibility(View.VISIBLE);
            rvVenues.setVisibility(View.GONE);
        } else {
            llEmpty.setVisibility(View.GONE);
            rvVenues.setVisibility(View.VISIBLE);
        }
    }

    // Lấy danh sách tất cả các sân từ API
    private void loadVenuesFromApi() {
        showLoading(true);
        apiService.getVenues().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allVenues.clear();
                    Map<String, Object> responseBody = response.body();
                    List<Map<String, Object>> venueList = new ArrayList<>();
                    if (responseBody.containsKey("data") && responseBody.get("data") instanceof List) {
                        venueList = (List<Map<String, Object>>) responseBody.get("data");
                    }
                    for (Map<String, Object> item : venueList) {
                        VenueItem venue = mapToVenueItem(item);
                        if (venue != null) {
                            allVenues.add(venue);
                        }
                    }
                    applyFilterAndSort();
                } else {
                    Log.e(TAG, "Lỗi tải sân: " + response.code());
                    Toast.makeText(ExploreActivity.this,
                            "Không thể tải danh sách sân", Toast.LENGTH_SHORT).show();
                    loadFallbackVenues();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi kết nối", t);
                Toast.makeText(ExploreActivity.this,
                        "Lỗi kết nối. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                loadFallbackVenues();
            }
        });
    }

    // Nếu API bị lỗi thì sẽ lấy dữ liệu này để hiển thị
    private void loadFallbackVenues() {
        allVenues.clear();
        allVenues.add(new VenueItem(1, "Sân Pickleball Quận 1", "123 Lê Lợi, Quận 1, TP.HCM",
                4.8, 128, "06:00", "22:00", 80000, "0901234567", "Pickleball"));
        allVenues.add(new VenueItem(2, "Sân Cầu Lông Thủ Đức", "456 Võ Văn Ngân, Thủ Đức, TP.HCM",
                4.5, 96, "05:30", "22:00", 60000, "0902345678", "Cầu lông"));
        allVenues.add(new VenueItem(3, "Sân Bóng Đá Mini Bình Thạnh", "789 Bạch Đằng, Bình Thạnh, TP.HCM",
                4.3, 74, "06:00", "23:00", 150000, "0903456789", "Bóng đá"));
        allVenues.add(new VenueItem(4, "Tennis Court Phú Nhuận", "321 Phan Xích Long, Phú Nhuận, TP.HCM",
                4.7, 112, "06:00", "21:00", 120000, "0904567890", "Tennis"));
        applyFilterAndSort();
    }

    // Chuyển dữ liệu từ Map sang VenueItem
    private VenueItem mapToVenueItem(Map<String, Object> item) {
        try {
            long id = getIdFromMap(item);
            String name = getStringFromMap(item, "name");
            String address = getStringFromMap(item, "address");
            double rating = getDoubleFromMap(item, "rating");
            int ratingCount = getIntFromMap(item, "ratingCount");
            String openTime = getStringFromMap(item, "openTime");
            String closeTime = getStringFromMap(item, "closeTime");
            double pricePerSlot = getDoubleFromMap(item, "pricePerSlot");
            String phone = getStringFromMap(item, "phone");

            String categoryName = getStringFromMap(item, "categoryName");
            if (categoryName == null || categoryName.isEmpty()) {
                Object catObj = item.get("category");
                if (catObj instanceof Map) {
                    categoryName = getStringFromMap((Map<String, Object>) catObj, "name");
                }
            }

            String imageUrl = getStringFromMap(item, "imageUrl");

            return new VenueItem(id, name, address, rating, ratingCount,
                    openTime, closeTime, pricePerSlot, phone, categoryName, imageUrl);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi mapping venue", e);
            return null;
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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

    private int getIntFromMap(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return 0;
    }
}
