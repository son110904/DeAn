package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ForecastActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "finance_prefs";
    private static final String KEY_INCOME_TOTAL = "income_total";
    private static final String KEY_EXPENSE_TOTAL = "expense_total";

    TextView tvForecast;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        tvForecast = findViewById(R.id.tvForecast);
        bottomNav = findViewById(R.id.bottomNav);

        updateForecast();

        // Đánh dấu tab hiện tại
        bottomNav.setSelectedItemId(R.id.menu_forecast);

        // Điều hướng
        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;

            } else if (id == R.id.menu_add) {
                startActivity(new Intent(this, AddTransactionActivity.class));
                return true;

            } else if (id == R.id.menu_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                return true;

            } else if (id == R.id.menu_forecast) {
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateForecast();
    }

    private void updateForecast() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long income = prefs.getLong(KEY_INCOME_TOTAL, 0);
        long expense = prefs.getLong(KEY_EXPENSE_TOTAL, 0);
        long balance = income - expense;

        tvForecast.setText(balance + "đ");
    }
}
