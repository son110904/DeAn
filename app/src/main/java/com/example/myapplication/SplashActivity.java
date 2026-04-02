package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY_MS = 1200L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String baseUrl = ApiConfigStore.getBaseUrl(this);
            Intent intent;
            if (baseUrl.isEmpty()) {
                intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(SettingsActivity.EXTRA_SETUP_MODE, true);
            } else {
                Class<?> nextScreen = AuthStore.isLoggedIn(this)
                        ? MainActivity.class
                        : LoginActivity.class;
                intent = new Intent(this, nextScreen);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
