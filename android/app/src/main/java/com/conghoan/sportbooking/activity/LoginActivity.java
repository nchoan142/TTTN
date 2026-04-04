package com.conghoan.sportbooking.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.api.ApiClient;
import com.conghoan.sportbooking.api.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String PREF_NAME = "SportBooking";

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.et_email);
        edtPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register_link);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validate empty fields
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        // Disable button to prevent double-tap
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        // Call real API
        ApiService apiService = ApiClient.getApiService();
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("ĐĂNG NHẬP");

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseBody = response.body();

                    // Backend trả ApiResponse wrapper: {success, message, data}
                    Map<String, Object> data = responseBody;
                    if (responseBody.containsKey("data") && responseBody.get("data") instanceof Map) {
                        data = (Map<String, Object>) responseBody.get("data");
                    }

                    String token = data.get("token") != null ? data.get("token").toString() : "";
                    String userId = data.get("userId") != null ? data.get("userId").toString() : "";
                    String fullName = data.get("fullName") != null ? data.get("fullName").toString() : "";
                    String userEmail = data.get("email") != null ? data.get("email").toString() : email;
                    String phone = data.get("phone") != null ? data.get("phone").toString() : "";
                    String role = data.get("role") != null ? data.get("role").toString() : "USER";

                    if (token.isEmpty()) {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: Không nhận được token", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Save login info
                    saveLoginInfo(token, userId, fullName, userEmail, phone, role);

                    // Update ApiClient token
                    ApiClient.setAuthToken(token);

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else {
                    String errorMsg = "Đăng nhập thất bại";
                    if (response.code() == 401) {
                        errorMsg = "Email hoặc mật khẩu không đúng";
                    } else if (response.code() == 400) {
                        errorMsg = "Thông tin đăng nhập không hợp lệ";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("ĐĂNG NHẬP");
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveLoginInfo(String token, String userId, String fullName, String email, String phone, String role) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putString("token", token);
        editor.putString("userId", userId);
        editor.putString("fullName", fullName);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.putString("role", role);
        editor.apply();
    }
}
