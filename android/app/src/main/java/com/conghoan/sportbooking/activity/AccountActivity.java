package com.conghoan.sportbooking.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.api.ApiClient;
import com.conghoan.sportbooking.api.ApiService;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends AppCompatActivity {

    private static final String PREF_NAME = "SportBooking";

    private TextView tvAvatarInitials, tvHeaderName, tvHeaderEmail;
    private EditText etFullName, etEmail, etPhone;
    private MaterialButton btnUpdateProfile, btnChangePassword, btnLogout, btnAdminPanel;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initViews();
        loadUserInfo();
        setupListeners();
    }

    private void initViews() {
        tvAvatarInitials = findViewById(R.id.tv_avatar_initials);
        tvHeaderName = findViewById(R.id.tv_header_name);
        tvHeaderEmail = findViewById(R.id.tv_header_email);
        etFullName = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        btnUpdateProfile = findViewById(R.id.btn_update_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);
        btnAdminPanel = findViewById(R.id.btn_admin_panel);
        btnBack = findViewById(R.id.btn_back);
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String fullName = prefs.getString("fullName", "");
        String email = prefs.getString("email", "");
        String phone = prefs.getString("phone", "");

        // Set header info
        tvHeaderName.setText(!TextUtils.isEmpty(fullName) ? fullName : "Người dùng");
        tvHeaderEmail.setText(!TextUtils.isEmpty(email) ? email : "");

        // Set avatar initials
        if (!TextUtils.isEmpty(fullName)) {
            String initials = getInitials(fullName);
            tvAvatarInitials.setText(initials);
        } else {
            tvAvatarInitials.setText("U");
        }

        // Set form fields
        etFullName.setText(fullName);
        etEmail.setText(email);
        etPhone.setText(phone);

        // Hiển thị button AdminPanel nếu user có role là ADMIN
        String role = prefs.getString("role", "USER");
        if ("ADMIN".equals(role)) {
            btnAdminPanel.setVisibility(android.view.View.VISIBLE);
        } else {
            btnAdminPanel.setVisibility(android.view.View.GONE);
        }
    }

    private String getInitials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        } else if (parts.length == 1 && !parts[0].isEmpty()) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return "U";
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnUpdateProfile.setOnClickListener(v -> performUpdateProfile());

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnAdminPanel.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminActivity.class));
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void performUpdateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuẩn bị Body (ở đây là 1 Map)
        Map<String, String> body = new HashMap<>();
        body.put("fullName", fullName);
        body.put("email", email);
        body.put("phone", phone);

        // Gọi API
        ApiClient.getApiService().updateProfile(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //Cập nhật thành công, lưu lại vào SharedPreferences
                    SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
                    editor.putString("fullName", fullName);
                    editor.putString("email", email);
                    editor.putString("phone", phone);
                    editor.apply();

                    // Cập nhật lại giao diện
                    loadUserInfo();
                    Toast.makeText(AccountActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AccountActivity.this, "Cập nhật thất bại, thử lại sau", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AccountActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        EditText etOld = dialogView.findViewById(R.id.et_old_password);
        EditText etNew = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirm = dialogView.findViewById(R.id.et_confirm_password);

        AlertDialog realDialog = new AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", null)
                .setNegativeButton("Huỷ", null)
                .create();

        realDialog.setOnShowListener(d -> {
            realDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String oldPw = etOld.getText().toString().trim();
                String newPw = etNew.getText().toString().trim();
                String confirmPw = etConfirm.getText().toString().trim();
                Log.d("AccountActivity", "OldPass: " + oldPw + ", NewPass: " + newPw + ", ConfirmPass: " + confirmPw);

                if (oldPw.isEmpty()) {
                    etOld.setError("Nhập mật khẩu cũ");
                    return;
                }
                if (newPw.length() < 6) {
                    etNew.setError("Mật khẩu mới ít nhất 6 ký tự");
                    return;
                }
                if (!newPw.equals(confirmPw)) {
                    etConfirm.setError("Mật khẩu xác nhận không khớp");
                    return;
                }

                Map<String, String> body = new HashMap<>();
                body.put("oldPassword", oldPw);
                body.put("newPassword", newPw);

                ApiService apiService = ApiClient.getApiService();
                apiService.changePassword(body).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        Log.d("AccountActivity", "Response: " + response.toString());
                        Log.d("AccountActivity", "Response isSuccessful: " + response.isSuccessful());
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> res = response.body();
                            Boolean success = (Boolean) res.get("success");
                            if (Boolean.TRUE.equals(success)) {
                                Toast.makeText(AccountActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                realDialog.dismiss();
                            } else {
                                String msg = (String) res.get("message");
                                Toast.makeText(AccountActivity.this, msg != null ? msg : "Lỗi", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AccountActivity.this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(AccountActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        realDialog.show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void performLogout() {
        // Clear SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Reset ApiClient
        ApiClient.resetClient();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Navigate to LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
