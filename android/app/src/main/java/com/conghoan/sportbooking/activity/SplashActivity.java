package com.conghoan.sportbooking.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.conghoan.sportbooking.R;
import com.conghoan.sportbooking.api.ApiClient;

public class SplashActivity extends AppCompatActivity {

    private static final String PREF_NAME = "SportBooking";
    private static final String KEY_TOKEN = "token";
    private static final long SPLASH_DELAY = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginState, SPLASH_DELAY);
    }

    private void checkLoginState() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String token = prefs.getString(KEY_TOKEN, null);

        Intent intent;
        // Nếu có token thì sẽ lưu token vào ApiClient, những lần mở app sau
        // sẽ tự động chuyển tới MainActivity
        if (!TextUtils.isEmpty(token)) {
            ApiClient.setAuthToken(token);
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
