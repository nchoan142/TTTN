package com.conghoan.sportbooking.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.adapter.CategoryAdapter;
import com.conghoan.sportbooking.adapter.VenueAdapter;
import com.conghoan.sportbooking.api.ApiClient;
import com.conghoan.sportbooking.api.ApiService;
import com.conghoan.sportbooking.model.Category;
import com.conghoan.sportbooking.model.VenueItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREF_NAME = "SportBooking";

    private TextView tvGreeting, tvDate;
    private EditText edtSearch;
    private RecyclerView rvCategories, rvVenues;
    private BottomNavigationView bottomNav;
    private ProgressBar progressBar;

    private CategoryAdapter categoryAdapter;
    private VenueAdapter venueAdapter;

    private List<Category> categoryList = new ArrayList<>();
    private List<VenueItem> allVenues = new ArrayList<>();
    private List<VenueItem> filteredVenues = new ArrayList<>();

    private ApiService apiService;
    private String selectedCategoryName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApiClient.loadTokenFromPrefs(this);
        apiService = ApiClient.getApiService();

        initViews();
        loadUserInfo();
        setCurrentDate();
        setupAdapters();
        setupSearch();
        setupBottomNav();

        loadCategoriesFromApi();
        loadVenuesFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi quay lại MainActivity, reset bottom nav về tab Home
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tv_username);
        tvDate = findViewById(R.id.tv_date);
        edtSearch = findViewById(R.id.et_search);
        rvCategories = findViewById(R.id.rv_categories);
        rvVenues = findViewById(R.id.rv_venues);
        bottomNav = findViewById(R.id.bottom_nav);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String fullName = prefs.getString("fullName", "Bạn");
        tvGreeting.setText("Xin chào, " + fullName + "!");
    }

    private void setCurrentDate() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String dayName = getDayOfWeekInVietnamese(dayOfWeek);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        String formattedDate = dayName + ", " + sdf.format(new Date());
        tvDate.setText(formattedDate);
    }

    private String getDayOfWeekInVietnamese(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "Thứ Hai";
            case Calendar.TUESDAY: return "Thứ Ba";
            case Calendar.WEDNESDAY: return "Thứ Tư";
            case Calendar.THURSDAY: return "Thứ Năm";
            case Calendar.FRIDAY: return "Thứ Sáu";
            case Calendar.SATURDAY: return "Thứ Bảy";
            case Calendar.SUNDAY: return "Chủ Nhật";
            default: return "";
        }
    }

    private void setupAdapters() {
        // Categories adapter
        categoryAdapter = new CategoryAdapter(this, categoryList, (category, position) -> {
            selectedCategoryName = category.getName();
            filterByCategory(selectedCategoryName);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Venues adapter
        venueAdapter = new VenueAdapter(this, filteredVenues);
        rvVenues.setLayoutManager(new LinearLayoutManager(this));
        rvVenues.setNestedScrollingEnabled(false);
        rvVenues.setAdapter(venueAdapter);
    }

    // --- API: Load danh mục ---
    private void loadCategoriesFromApi() {
        apiService.getCategories().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    // Backend trả ApiResponse wrapper: {success, message, data}
                    Map<String, Object> responseBody = response.body();
                    List<Map<String, Object>> data = new ArrayList<>();
                    if (responseBody.containsKey("data") && responseBody.get("data") instanceof List) {
                        data = (List<Map<String, Object>>) responseBody.get("data");
                    }
                    for (int i = 0; i < data.size(); i++) {
                        Map<String, Object> item = data.get(i);
                        long id = getIdFromMap(item);
                        String name = getStringFromMap(item, "name");
                        int iconRes = mapCategoryIcon(name);
                        boolean selected = (i == 0);
                        categoryList.add(new Category(id, name, iconRes, selected));
                    }
                    categoryAdapter.updateList(categoryList);

                    // Tự động lọc theo category đầu tiên
                    if (!categoryList.isEmpty()) {
                        selectedCategoryName = categoryList.get(0).getName();
                        filterByCategory(selectedCategoryName);
                    }
                } else {
                    Log.e(TAG, "Lỗi tải danh mục: " + response.code());
                    loadFallbackCategories();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối khi tải danh mục", t);
                loadFallbackCategories();
            }
        });
    }

    private void loadFallbackCategories() {
        categoryList.clear();
        categoryList.add(new Category(1, "Pickleball", R.drawable.ic_sport_pickleball, true));
        categoryList.add(new Category(2, "Cầu lông", R.drawable.ic_sport_badminton, false));
        categoryList.add(new Category(3, "Bóng đá", R.drawable.ic_sport_football, false));
        categoryList.add(new Category(4, "Tennis", R.drawable.ic_sport_tennis, false));
        categoryList.add(new Category(5, "Bóng chuyền", R.drawable.ic_sport_volleyball, false));
        categoryList.add(new Category(6, "Bóng rổ", R.drawable.ic_sport_basketball, false));
        categoryAdapter.updateList(categoryList);

        if (!categoryList.isEmpty()) {
            selectedCategoryName = categoryList.get(0).getName();
            filterByCategory(selectedCategoryName);
        }
    }

    private int mapCategoryIcon(String name) {
        if (name == null) return R.drawable.ic_sport_pickleball;
        String lower = name.toLowerCase();
        if (lower.contains("pickleball")) return R.drawable.ic_sport_pickleball;
        if (lower.contains("cầu lông") || lower.contains("badminton")) return R.drawable.ic_sport_badminton;
        if (lower.contains("bóng đá") || lower.contains("football")) return R.drawable.ic_sport_football;
        if (lower.contains("tennis")) return R.drawable.ic_sport_tennis;
        if (lower.contains("bóng chuyền") || lower.contains("volleyball")) return R.drawable.ic_sport_volleyball;
        if (lower.contains("bóng rổ") || lower.contains("basketball")) return R.drawable.ic_sport_basketball;
        return R.drawable.ic_sport_pickleball;
    }

    // --- API: Load danh sách sân ---
    private void loadVenuesFromApi() {
        showLoading(true);
        apiService.getVenues().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allVenues.clear();
                    // Backend trả ApiResponse wrapper: {success, message, data}
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
                    filterByCategory(selectedCategoryName);
                } else {
                    Log.e(TAG, "Lỗi tải sân: " + response.code());
                    showRetryToast("Không thể tải danh sách sân");
                    loadFallbackVenues();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi kết nối khi tải sân", t);
                showRetryToast("Lỗi kết nối. Vui lòng thử lại");
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
        filterByCategory(selectedCategoryName);
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

            // Category name có thể nằm ở categoryName hoặc trong object category.name
            String categoryName = getStringFromMap(item, "categoryName");
            if (categoryName == null || categoryName.isEmpty()) {
                Object catObj = item.get("category");
                if (catObj instanceof Map) {
                    categoryName = getStringFromMap((Map<String, Object>) catObj, "name");
                }
            }

            String imageUrl = getStringFromMap(item, "imageUrl");

            return new VenueItem(id, name, address, rating, ratingCount, openTime, closeTime, pricePerSlot, phone, categoryName, imageUrl);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi mapping venue", e);
            return null;
        }
    }

    // --- Filter logic ---
    private void filterByCategory(String categoryName) {
        filteredVenues.clear();
        if (categoryName == null || categoryName.isEmpty()) {
            filteredVenues.addAll(allVenues);
        } else {
            for (VenueItem v : allVenues) {
                if (v.getCategoryName() != null && v.getCategoryName().equalsIgnoreCase(categoryName)) {
                    filteredVenues.add(v);
                }
            }
            // Nếu không có kết quả, hiện tất cả
            if (filteredVenues.isEmpty()) {
                filteredVenues.addAll(allVenues);
            }
        }
        venueAdapter.updateList(filteredVenues);
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVenuesByName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterVenuesByName(String query) {
        List<VenueItem> result = new ArrayList<>();
        if (query.isEmpty()) {
            result.addAll(allVenues);
        } else {
            String lowerQuery = query.toLowerCase();
            for (VenueItem v : allVenues) {
                if (v.getName().toLowerCase().contains(lowerQuery)
                        || v.getAddress().toLowerCase().contains(lowerQuery)
                        || (v.getCategoryName() != null && v.getCategoryName().toLowerCase().contains(lowerQuery))) {
                    result.add(v);
                }
            }
        }
        venueAdapter.updateList(result);
    }

    // --- Bottom Navigation ---
    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Ở lại trang chủ
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, MapActivity.class));
                return true;
            } else if (id == R.id.nav_explore) {
                startActivity(new Intent(this, ExploreActivity.class));
                return true;
            } else if (id == R.id.nav_featured) {
                startActivity(new Intent(this, MyBookingsActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                return true;
            }
            return false;
        });
    }

    // --- Tiện ích ---
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showRetryToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
