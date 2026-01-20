package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.Locale;

public class ForecastActivity extends AppCompatActivity {

    TextView tvForecast;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        tvForecast = findViewById(R.id.tvForecast);
        bottomNav = findViewById(R.id.bottomNav);

        // Format số tiền
        int forecastAmount = 6200000;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvForecast.setText(formatter.format(forecastAmount) + "đ");

        // Đánh dấu tab hiện tại
        bottomNav.setSelectedItemId(R.id.menu_forecast);

        // Điều hướng
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_add) {
                startActivity(new Intent(this, AddTransactionActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_forecast) {
                return true;
            }

            return false;
        });
    }
}