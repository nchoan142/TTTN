package com.conghoan.sportbooking.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.adapter.VenueMapAdapter;
import com.conghoan.sportbooking.api.ApiClient;
import com.conghoan.sportbooking.api.ApiService;
import com.conghoan.sportbooking.model.VenueItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    private EditText etSearch;
    private RecyclerView rvVenues;
    private ProgressBar progressBar;
    private LinearLayout llEmpty;

    private VenueMapAdapter venueMapAdapter;
    private List<VenueItem> allVenues = new ArrayList<>();
    private List<VenueItem> displayedVenues = new ArrayList<>();

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        apiService = ApiClient.getApiService();

        initViews();
        setupRecyclerView();
        setupSearch();
        loadVenuesFromApi();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        rvVenues = findViewById(R.id.rv_venues);
        progressBar = findViewById(R.id.progress_bar);
        llEmpty = findViewById(R.id.ll_empty);

        // Nút quay lại
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        venueMapAdapter = new VenueMapAdapter(this, displayedVenues);
        rvVenues.setLayoutManager(new LinearLayoutManager(this));
        rvVenues.setAdapter(venueMapAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVenues(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterVenues(String query) {
        displayedVenues.clear();
        if (query == null || query.trim().isEmpty()) {
            displayedVenues.addAll(allVenues);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (VenueItem v : allVenues) {
                boolean matchName = v.getName() != null &&
                        v.getName().toLowerCase().contains(lowerQuery);
                boolean matchAddress = v.getAddress() != null &&
                        v.getAddress().toLowerCase().contains(lowerQuery);
                if (matchName || matchAddress) {
                    displayedVenues.add(v);
                }
            }
        }
        venueMapAdapter.updateList(displayedVenues);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (displayedVenues.isEmpty()) {
            llEmpty.setVisibility(View.VISIBLE);
            rvVenues.setVisibility(View.GONE);
        } else {
            llEmpty.setVisibility(View.GONE);
            rvVenues.setVisibility(View.VISIBLE);
        }
    }

    // ========== API ==========

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
                    displayedVenues.clear();
                    displayedVenues.addAll(allVenues);
                    venueMapAdapter.updateList(displayedVenues);
                    updateEmptyState();
                } else {
                    Log.e(TAG, "Lỗi tải sân: " + response.code());
                    Toast.makeText(MapActivity.this,
                            "Không thể tải danh sách sân", Toast.LENGTH_SHORT).show();
                    loadFallbackVenues();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi kết nối", t);
                Toast.makeText(MapActivity.this,
                        "Lỗi kết nối. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                loadFallbackVenues();
            }
        });
    }

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
        displayedVenues.clear();
        displayedVenues.addAll(allVenues);
        venueMapAdapter.updateList(displayedVenues);
        updateEmptyState();
    }

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

    // ========== Utility ==========

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
