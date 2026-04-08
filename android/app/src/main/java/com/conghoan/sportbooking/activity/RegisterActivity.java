package com.conghoan.sportbooking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPhone, edtPassword;
    private Button btnRegister;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        edtFullName = findViewById(R.id.et_fullname);
        edtEmail = findViewById(R.id.et_email);
        edtPhone = findViewById(R.id.et_phone);
        edtPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validate fields
        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Vui lòng nhập họ tên");
            edtFullName.requestFocus();
            return;
        }

        if (fullName.length() < 2) {
            edtFullName.setError("Họ tên phải có ít nhất 2 ký tự");
            edtFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            edtPhone.requestFocus();
            return;
        }

        if (!phone.matches("^(0|\\+84)[0-9]{9,10}$")) {
            edtPhone.setError("Số điện thoại không hợp lệ");
            edtPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtPassword.requestFocus();
            return;
        }

        // Disable button to prevent double-tap
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang xử lý...");

        // Call real API
        ApiService apiService = ApiClient.getApiService();
        Map<String, String> body = new HashMap<>();
        body.put("fullName", fullName);
        body.put("email", email);
        body.put("phone", phone);
        body.put("password", password);

        apiService.register(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("ĐĂNG KÝ");

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                    // Navigate to LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Đăng ký thất bại";
                    if (response.code() == 409) {
                        errorMsg = "Email đã được sử dụng";
                    } else if (response.code() == 400) {
                        errorMsg = "Thông tin đăng ký không hợp lệ";
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("ĐĂNG KÝ");
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
