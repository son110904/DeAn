package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.example.myapplication.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView tvIncome, tvExpense, tvBalance;
    BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ view
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        barChart = findViewById(R.id.barChart);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.menu_home);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;

            } else if (id == R.id.menu_add) {
                return true;

            } else if (id == R.id.menu_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                return true;

            } else if (id == R.id.menu_forecast) {
                startActivity(new Intent(this, ForecastActivity.class));
                return true;
            }

            return false;
        });
        // Dữ liệu giả
        int income = 10000000;
        int expense = 5000000;
        int balance = income - expense;

        // Hiển thị text
        tvIncome.setText("Tổng thu: " + income + "đ");
        tvExpense.setText("Tổng chi: " + expense + "đ");
        tvBalance.setText("Số dư: " + balance + "đ");

        // Load biểu đồ
        setupBarChart();
    }

    private void setupBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 2000000)); // Tháng 1
        entries.add(new BarEntry(2, 3500000)); // Tháng 2
        entries.add(new BarEntry(3, 1500000)); // Tháng 3

        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu theo tháng");
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }
}