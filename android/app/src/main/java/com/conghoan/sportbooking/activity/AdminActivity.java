package com.conghoan.sportbooking.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.adapter.AdminBookingAdapter;
import com.conghoan.sportbooking.adapter.AdminCategoryAdapter;
import com.conghoan.sportbooking.adapter.AdminUserAdapter;
import com.conghoan.sportbooking.adapter.AdminVenueAdapter;
import com.conghoan.sportbooking.api.ApiClient;
import com.conghoan.sportbooking.api.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private static final int TAB_USERS = 0;
    private static final int TAB_VENUES = 1;
    private static final int TAB_BOOKINGS = 2;
    private static final int TAB_CATEGORIES = 3;

    private TextView tvStatUsers, tvStatVenues, tvStatBookings, tvStatRevenue;
    private MaterialButton btnTabUsers, btnTabVenues, btnTabBookings, btnTabCategories;
    private RecyclerView rvList;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ImageButton btnBack;
    private FloatingActionButton faBtnAdd;

    private ApiService apiService;
    private int currentTab = TAB_USERS;
    private List<Map<String, Object>> userList = new ArrayList<>();
    private List<Map<String, Object>> venueList = new ArrayList<>();
    private List<Map<String, Object>> bookingList = new ArrayList<>();
    private List<Map<String, Object>> categoryList = new ArrayList<>();

    private AdminUserAdapter adminUserAdapter;
    private AdminVenueAdapter adminVenueAdapter;
    private AdminBookingAdapter adminBookingAdapter;
    private AdminCategoryAdapter adminCategoryAdapter;

    // format number theo định dạng Việt Nam
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        apiService = ApiClient.getApiService();

        initViews();
        setupAdapters();
        setupListeners();
        loadStats();
        switchTab(TAB_USERS);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvStatUsers = findViewById(R.id.tv_stat_users);
        tvStatVenues = findViewById(R.id.tv_stat_venues);
        tvStatBookings = findViewById(R.id.tv_stat_bookings);
        tvStatRevenue = findViewById(R.id.tv_stat_revenue);
        btnTabUsers = findViewById(R.id.btn_tab_users);
        btnTabVenues = findViewById(R.id.btn_tab_venues);
        btnTabBookings = findViewById(R.id.btn_tab_bookings);
        btnTabCategories = findViewById(R.id.btn_tab_categories);
        rvList = findViewById(R.id.rv_admin_list);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        faBtnAdd = findViewById(R.id.fab_add);

        rvList.setLayoutManager(new LinearLayoutManager(this));
    }

    // Khởi tạo các adapter
    // và xử lý các sự kiện của adapter đó
    // các Listener của các adapter hoạt động tương tự các callback
    private void setupAdapters() {
        adminUserAdapter = new AdminUserAdapter(this, userList, (user, position) -> showRoleDialog(user, position));
        adminUserAdapter.setDeleteListener((user, position) -> showDeleteUserDialog(user, position));

        adminVenueAdapter = new AdminVenueAdapter(this, venueList, (venue, position) -> toggleVenue(venue, position));
        adminVenueAdapter.setOnVenueDeleteListener((venue, position) -> deleteVenue(venue, position));

        adminBookingAdapter = new AdminBookingAdapter(this, bookingList);
        adminBookingAdapter.setConfirmListener((booking, position) -> confirmBookingDialog(booking, position));
        adminBookingAdapter.setClickListener((booking, position) -> showBookingDetailDialog(booking));
        adminBookingAdapter.setCancelListener((booking, position) -> showCancelBookingDialog(booking, position));

        adminCategoryAdapter = new AdminCategoryAdapter(this, categoryList, new AdminCategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEditClick(Map<String, Object> category, int position) {
                showEditCategoryDialog(category, position);
            }

            @Override
            public void onDeleteClick(Map<String, Object> category, int position) {
                showDeleteCategoryDialog(category, position);
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnTabUsers.setOnClickListener(v -> switchTab(TAB_USERS));
        btnTabVenues.setOnClickListener(v -> switchTab(TAB_VENUES));
        btnTabBookings.setOnClickListener(v -> switchTab(TAB_BOOKINGS));
        btnTabCategories.setOnClickListener(v -> switchTab(TAB_CATEGORIES));

        faBtnAdd.setOnClickListener(v -> {
            if (currentTab == TAB_CATEGORIES) {
                showCreateCategoryDialog();
            } else if (currentTab == TAB_VENUES) {
                showCreateVenueDialog();
            }
        });
    }

    // Chuyển tab và hiển thị danh sách phù hợp
    private void switchTab(int tab) {
        currentTab = tab;
        setTabActive(btnTabUsers, tab == TAB_USERS);
        setTabActive(btnTabVenues, tab == TAB_VENUES);
        setTabActive(btnTabBookings, tab == TAB_BOOKINGS);
        setTabActive(btnTabCategories, tab == TAB_CATEGORIES);

        // Hiển thị nút add cho tab Category và Venue
        faBtnAdd.setVisibility((tab == TAB_CATEGORIES || tab == TAB_VENUES) ? View.VISIBLE : View.GONE);

        switch (tab) {
            case TAB_USERS:
                rvList.setAdapter(adminUserAdapter);
                loadUsers();
                break;
            case TAB_VENUES:
                rvList.setAdapter(adminVenueAdapter);
                loadVenues();
                break;
            case TAB_BOOKINGS:
                rvList.setAdapter(adminBookingAdapter);
                loadBookings();
                break;
            case TAB_CATEGORIES:
                rvList.setAdapter(adminCategoryAdapter);
                loadCategories();
                break;
        }
    }

    private void setTabActive(MaterialButton btn, boolean active) {
        if (active) {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2E7D32));
            btn.setTextColor(0xFFFFFFFF);
        } else {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0));
            btn.setTextColor(0xFF757575);
        }
    }

    // Lấy dữ liệu từ API và hiển thị lên UI
    // Ví dụ Số lượng user, sân thể thao, doanh thu, lịch đặt
    private void loadStats() {
        apiService.getAdminStats().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    Map<String, Object> data = body;
                    if (body.containsKey("data") && body.get("data") instanceof Map) {
                        data = (Map<String, Object>) body.get("data");
                    }

                    tvStatUsers.setText(String.valueOf(getInt(data, "totalUsers")));
                    tvStatVenues.setText(String.valueOf(getInt(data, "totalVenues")));
                    tvStatBookings.setText(String.valueOf(getInt(data, "totalBookings")));

                    // Doanh thu
                    double revenue = getDouble(data, "totalRevenue");
                    tvStatRevenue.setText(currencyFormat.format(revenue) + "đ");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Lỗi tải thống kê: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load danh sách User đã có cho UI
    private void loadUsers() {
        showLoading(true);
        apiService.getAdminUsers().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    List<Map<String, Object>> data = extractList(body);
                    userList.clear();
                    userList.addAll(data);
                    adminUserAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(userList.isEmpty() ? View.VISIBLE : View.GONE);
                    btnTabUsers.setText("Người dùng (" + userList.size() + ")");
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(AdminActivity.this, "Lỗi tải danh sách người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load danh sách Venue(sân thể thao) đã có cho UI
    private void loadVenues() {
        showLoading(true);
        apiService.getAdminVenues().enqueue(new Callback<Map<String, Object>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    List<Map<String, Object>> data = extractList(body);
                    venueList.clear();
                    venueList.addAll(data);
                    adminVenueAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(venueList.isEmpty() ? View.VISIBLE : View.GONE);
                    btnTabVenues.setText("Sân thể thao (" + venueList.size() + ")");
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(AdminActivity.this, "Lỗi tải danh sách sân", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load danh sách Booking đã có cho UI
    private void loadBookings() {
        showLoading(true);
        apiService.getAdminBookings().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    List<Map<String, Object>> data = extractList(body);
                    bookingList.clear();
                    bookingList.addAll(data);
                    adminBookingAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
                    btnTabBookings.setText("Lịch đặt (" + bookingList.size() + ")");
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(AdminActivity.this, "Lỗi tải danh sách lịch đặt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load danh sách Category(danh mục) đã có cho UI
    private void loadCategories() {
        showLoading(true);
        apiService.getAdminCategories().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    List<Map<String, Object>> data = extractList(body);
                    categoryList.clear();
                    categoryList.addAll(data);
                    adminCategoryAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(categoryList.isEmpty() ? View.VISIBLE : View.GONE);
                    btnTabCategories.setText("Danh mục (" + categoryList.size() + ")");
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(AdminActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị dialog đổi role khi nhấn giữ vào User trong tab Người dùng
    private void showRoleDialog(Map<String, Object> user, int position) {
        String currentRole = user.get("role") != null ? user.get("role").toString() : "USER";
        String userName = user.get("fullName") != null ? user.get("fullName").toString() : "N/A";
        String[] roles = {"USER", "OWNER", "ADMIN"};
        int checkedIndex = 0;
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(currentRole)) {
                checkedIndex = i;
                break;
            }
        }

        final int[] selected = {checkedIndex};

        new AlertDialog.Builder(this)
                .setTitle("Đổi vai trò: " + userName)
                .setSingleChoiceItems(roles, checkedIndex, (dialog, which) -> selected[0] = which)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String newRole = roles[selected[0]];
                    if (!newRole.equals(currentRole)) {
                        updateUserRole(user, position, newRole);
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // Update role của user vào database
    private void updateUserRole(Map<String, Object> user, int position, String newRole) {
        long userId = getLongId(user);
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được ID người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("role", newRole);

        apiService.updateUserRole(userId, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    user.put("role", newRole);
                    adminUserAdapter.notifyItemChanged(position);
                    Toast.makeText(AdminActivity.this, "Đã cập nhật vai trò thành " + newRole, Toast.LENGTH_SHORT).show();
                    loadStats();
                } else {
                    Toast.makeText(AdminActivity.this, "Lỗi cập nhật vai trò", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị dialog khi click vào nút xóa user
    private void showDeleteUserDialog(Map<String, Object> user, int position) {
        String userName = user.get("fullName") != null ? user.get("fullName").toString() : "N/A";

        new AlertDialog.Builder(this)
                .setTitle("Xóa người dùng")
                .setMessage("Bạn có chắc muốn xóa người dùng \"" + userName + "\"?\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteUser(user, position))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // Xóa user khỏi database
    private void deleteUser(Map<String, Object> user, int position) {
        long userId = getLongId(user);
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được ID người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteUser(userId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    userList.remove(position);
                    adminUserAdapter.notifyItemRemoved(position);
                    adminUserAdapter.notifyItemRangeChanged(position, userList.size());
                    btnTabUsers.setText("Người dùng (" + userList.size() + ")");
                    Toast.makeText(AdminActivity.this, "Đã xóa người dùng thành công", Toast.LENGTH_SHORT).show();
                    loadStats();
                } else {
                    Toast.makeText(AdminActivity.this, "Lỗi xóa người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thay đổi trạng thái của sân thể thao
    // khi khóa sân thể thao, thì sân đó sẽ không hiển thị trong danh sách
    private void toggleVenue(Map<String, Object> venue, int position) {
        long venueId = getLongId(venue);
        if (venueId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được ID sân", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.toggleVenue(venueId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    boolean currentActive = getBool(venue, "active");
                    venue.put("active", !currentActive);
                    adminVenueAdapter.notifyItemChanged(position);
                    String msg = !currentActive ? "Đã mở sân hoạt động" : "Đã khoá sân";
                    Toast.makeText(AdminActivity.this, msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivity.this, "Lỗi chuyển đổi trạng thái sân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Gửi yêu cầu CONFIRM tới backend
    // nếu thành công thì sẽ chuyển status trong bảng bookings thành CONFIRMED
    private void confirmBookingDialog(Map<String, Object> booking, int position) {
        long bookingId = getLongId(booking);
        if (bookingId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được ID lịch đặt", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt sân")
                .setMessage("Bạn có chắc muốn xác nhận lịch đặt này?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("status", "CONFIRMED");

                    apiService.updateBookingStatus(bookingId, body).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                booking.put("status", "CONFIRMED");
                                adminBookingAdapter.notifyItemChanged(position);
                                Toast.makeText(AdminActivity.this, "Đã xác nhận lịch đặt", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminActivity.this, "Lỗi xác nhận lịch đặt", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(AdminActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // Hiển thị dialog hủy booking khi click vào nút huỷ lịch
    // Khi xác nhận hủy lịch, thay đổi status trong bảng bookings
    // thành CANCELLED
    private void showCancelBookingDialog(Map<String, Object> booking, int position) {
        long bookingId = getLongId(booking);
        if (bookingId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được ID lịch đặt", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Huỷ lịch đặt")
                .setMessage("Bạn có chắc muốn huỷ lịch đặt này? Người dùng sẽ thấy lịch chuyển sang trạng thái 'Đã huỷ'.")
                .setPositiveButton("Huỷ lịch", (dialog, which) -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("status", "CANCELLED");

                    apiService.updateBookingStatus(bookingId, body).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                booking.put("status", "CANCELLED");
                                adminBookingAdapter.notifyItemChanged(position);
                                Toast.makeText(AdminActivity.this, "Đã huỷ lịch đặt", Toast.LENGTH_SHORT).show();
                                loadStats();
                            } else {
                                Toast.makeText(AdminActivity.this, "Lỗi huỷ lịch đặt", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(AdminActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Không", null)
                .show();
    }

    // Hiển thị dialog chi tiết lịch đặt khi click vào 1 booking
    private void showBookingDetailDialog(Map<String, Object> booking) {
        // User info
        String userName = "";
        String userEmail = "";
        String userPhone = "";
        Object userObj = booking.get("user");
        if (userObj instanceof Map) {
            Map<String, Object> user = (Map<String, Object>) userObj;
            userName = getStr(user, "fullName");
            userEmail = getStr(user, "email");
            userPhone = getStr(user, "phone");
        }
        if (userName.isEmpty()) userName = getStr(booking, "userName");

        // Court info
        String courtInfo = "";
        Object courtObj = booking.get("court");
        if (courtObj instanceof Map) {
            Map<String, Object> court = (Map<String, Object>) courtObj;
            courtInfo = getStr(court, "name");
            Object venueObj = court.get("venue");
            if (venueObj instanceof Map) {
                String venueName = getStr((Map<String, Object>) venueObj, "name");
                if (!venueName.isEmpty()) courtInfo += " - " + venueName;
            }
        }
        if (courtInfo.isEmpty()) courtInfo = getStr(booking, "courtName");

        String date = getStr(booking, "bookingDate");
        String startTime = getStr(booking, "startTime");
        String endTime = getStr(booking, "endTime");
        String status = getStr(booking, "status");
        double totalPrice = getDouble(booking, "totalPrice");

        String statusLabel;
        switch (status) {
            case "PENDING": statusLabel = "Chờ xác nhận"; break;
            case "CONFIRMED": statusLabel = "Đã xác nhận"; break;
            case "COMPLETED": statusLabel = "Hoàn thành"; break;
            case "CANCELLED": statusLabel = "Đã huỷ"; break;
            default: statusLabel = status; break;
        }

        StringBuilder detail = new StringBuilder();
        detail.append("Người đặt: ").append(userName.isEmpty() ? "N/A" : userName).append("\n");
        if (!userEmail.isEmpty()) detail.append("Email: ").append(userEmail).append("\n");
        if (!userPhone.isEmpty()) detail.append("SĐT: ").append(userPhone).append("\n");
        detail.append("\nSân: ").append(courtInfo.isEmpty() ? "--" : courtInfo).append("\n");
        detail.append("Ngày: ").append(date).append("\n");
        detail.append("Giờ: ").append(startTime).append(" - ").append(endTime).append("\n");
        detail.append("Giá: ").append(totalPrice > 0 ? currencyFormat.format(totalPrice) + "đ" : "--").append("\n");
        detail.append("Trạng thái: ").append(statusLabel);

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết lịch đặt")
                .setMessage(detail.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    // Hiện thị dialog thêm mới sân thể thao
    // khi bấm nút add
    private void showCreateVenueDialog() {
        // Cần load categories trước để show vào spinner
        apiService.getAdminCategories().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> categories = extractList(response.body());
                    showVenueDialogWithCategories(categories);
                } else {
                    Toast.makeText(AdminActivity.this, "Không tải được danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị dialog thêm mới sân thể thao
    private void showVenueDialogWithCategories(List<Map<String, Object>> categories) {
        android.view.View dialogView = android.view.LayoutInflater.from(this)
                .inflate(R.layout.dialog_create_venue, null);

        EditText etName = dialogView.findViewById(R.id.et_venue_name);
        EditText etAddress = dialogView.findViewById(R.id.et_venue_address);
        EditText etPhone = dialogView.findViewById(R.id.et_venue_phone);
        EditText etOpenTime = dialogView.findViewById(R.id.et_open_time);
        EditText etCloseTime = dialogView.findViewById(R.id.et_close_time);
        EditText etPrice = dialogView.findViewById(R.id.et_price);
        EditText etImageUrl = dialogView.findViewById(R.id.et_image_url);
        EditText etCourtNames = dialogView.findViewById(R.id.et_court_names);
        android.widget.Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);

        // Spinner chứa tên các danh mục (Pickleball, Tennis,...)
        List<String> categoryNames = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();
        for (Map<String, Object> cat : categories) {
            categoryNames.add(getStr(cat, "name"));
            Object idObj = cat.get("id");
            if (idObj instanceof Number) categoryIds.add(((Number) idObj).longValue());
            else categoryIds.add(0L);
        }
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spinnerCategory.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Thêm sân mới")
                .setView(dialogView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên sân", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> body = new HashMap<>();
                    body.put("name", name);
                    body.put("address", etAddress.getText().toString().trim());
                    body.put("phone", etPhone.getText().toString().trim());
                    body.put("openTime", etOpenTime.getText().toString().trim());
                    body.put("closeTime", etCloseTime.getText().toString().trim());
                    body.put("imageUrl", etImageUrl.getText().toString().trim());

                    String priceStr = etPrice.getText().toString().trim();
                    if (!priceStr.isEmpty()) {
                        try {
                            body.put("pricePerSlot", Double.parseDouble(priceStr));
                        } catch (NumberFormatException ignored) {}
                    }

                    int catPos = spinnerCategory.getSelectedItemPosition();
                    if (catPos >= 0 && catPos < categoryIds.size()) {
                        body.put("categoryId", categoryIds.get(catPos));
                    }

                    String courtNamesStr = etCourtNames.getText().toString().trim();
                    if (!courtNamesStr.isEmpty()) {
                        List<String> courtNames = new ArrayList<>();
                        for (String cn : courtNamesStr.split(",")) {
                            String trimmed = cn.trim();
                            if (!trimmed.isEmpty()) courtNames.add(trimmed);
                        }
                        body.put("courtNames", courtNames);
                    }

                    createVenueApi(body);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void createVenueApi(Map<String, Object> body) {
        apiService.createVenue(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean success = (Boolean) response.body().get("success");
                    if (Boolean.TRUE.equals(success)) {
                        Toast.makeText(AdminActivity.this, "Tạo sân thành công", Toast.LENGTH_SHORT).show();
                        loadVenues();
                        loadStats();
                    } else {
                        String msg = (String) response.body().get("message");
                        Toast.makeText(AdminActivity.this, msg != null ? msg : "Lỗi tạo sân", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminActivity.this, "Lỗi tạo sân: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Giữ item sân thể thao sẽ hiển thị dialog để xác nhận
    // xóa sân thể thao
    // Xóa sân thể thao khỏi database
    private void deleteVenue(Map<String, Object> venue, int position) {
        Object idObj = venue.get("id");
        long venueId;
        if (idObj instanceof Number) venueId = ((Number) idObj).longValue();
        else return;

        String venueName = getStr(venue, "name");
        new AlertDialog.Builder(this)
                .setTitle("Xoá sân")
                .setMessage("Bạn có chắc muốn xoá sân \"" + venueName + "\"? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xoá", (d, w) -> {
                    apiService.deleteVenue(venueId).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminActivity.this, "Đã xoá sân", Toast.LENGTH_SHORT).show();
                                loadVenues();
                                loadStats();
                            } else {
                                Toast.makeText(AdminActivity.this, "Không xoá được sân (có thể đang có lịch đặt)", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(AdminActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showCreateCategoryDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 0);

        EditText edtName = new EditText(this);
        edtName.setHint("Tên danh mục");
        edtName.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(edtName);

        EditText edtIconUrl = new EditText(this);
        edtIconUrl.setHint("URL biểu tượng (không bắt buộc)");
        edtIconUrl.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        layout.addView(edtIconUrl);

        new AlertDialog.Builder(this)
                .setTitle("Thêm danh mục mới")
                .setView(layout)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String iconUrl = edtIconUrl.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, String> body = new HashMap<>();
                    body.put("name", name);
                    if (!iconUrl.isEmpty()) {
                        body.put("iconUrl", iconUrl);
                    }

                    apiService.createCategory(body).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminActivity.this, "Đã tạo danh mục \"" + name + "\"", Toast.LENGTH_SHORT).show();
                                loadCategories();
                            } else {
                                Toast.makeText(AdminActivity.this, "Lỗi tạo danh mục", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(AdminActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // Hiển thị dialog, sửa thông tin của danh mục (Category)
    private void showEditCategoryDialog(Map<String, Object> category, int position) {
        String currentName = getStr(category, "name");
        String currentIconUrl = getStr(category, "iconUrl");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 0);

        EditText edtName = new EditText(this);
        edtName.setHint("Tên danh mục");
        edtName.setText(currentName);
        edtName.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(edtName);

        EditText edtIconUrl = new EditText(this);
        edtIconUrl.setHint("URL biểu tượng");
        edtIconUrl.setText(currentIconUrl);
        edtIconUrl.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        layout.addView(edtIconUrl);

        new AlertDialog.Builder(this)
                .setTitle("Sửa danh mục")
                .setView(layout)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String iconUrl = edtIconUrl.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long categoryId = getLongId(category);
                    if (categoryId == -1) {
                        Toast.makeText(this, "Lỗi: Không xác định được ID danh mục", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, String> body = new HashMap<>();
                    body.put("name", name);
                    body.put("iconUrl", iconUrl);

                    // Cập nhật tên category trong database sau khi nhấn Lưu
                    apiService.updateCategory(categoryId, body).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                category.put("name", name);
                                category.put("iconUrl", iconUrl);
                                adminCategoryAdapter.notifyItemChanged(position);
                                Toast.makeText(AdminActivity.this, "Đã cập nhật danh mục", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminActivity.this, "Lỗi cập nhật danh mục", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(AdminActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // Hiển thị dialog, xóa danh mục (Category)
    private void showDeleteCategoryDialog(Map<String, Object> category, int position) {
        String name = getStr(category, "name");

        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa danh mục \"" + name + "\"?\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    long categoryId = getLongId(category);
                    if (categoryId == -1) {
                        Toast.makeText(this, "Lỗi: Không xác định được ID danh mục", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    apiService.deleteCategory(categoryId).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                categoryList.remove(position);
                                adminCategoryAdapter.notifyItemRemoved(position);
                                adminCategoryAdapter.notifyItemRangeChanged(position, categoryList.size());
                                Toast.makeText(AdminActivity.this, "Đã xóa danh mục \"" + name + "\"", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminActivity.this, "Lỗi xóa danh mục", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(AdminActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // Helpers
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    // id của các object trong database
    private long getLongId(Map<String, Object> map) {
        Object idObj = map.get("id");
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(idObj));
        } catch (Exception e) {
            return -1;
        }
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractList(Map<String, Object> body) {
        Object data = body.get("data");
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }
        return new ArrayList<>();
    }

    private int getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (Exception e) {
            return 0;
        }
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

    private boolean getBool(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        return "true".equalsIgnoreCase(String.valueOf(val));
    }
}