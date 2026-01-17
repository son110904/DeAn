package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {

    PieChart pieChart;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        pieChart = findViewById(R.id.pieChart);
        bottomNav = findViewById(R.id.bottomNav);

        // Dữ liệu biểu đồ tròn
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f, "Ăn uống"));
        entries.add(new PieEntry(30f, "Di chuyển"));
        entries.add(new PieEntry(30f, "Khác"));

        PieDataSet dataSet = new PieDataSet(entries, "Danh mục chi tiêu");
        dataSet.setColors(
            Color.parseColor("#5B8FF9"),
            Color.parseColor("#61DDAA"),
            Color.parseColor("#F6BD16")
        );
        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();

        // Đánh dấu tab hiện tại
        bottomNav.setSelectedItemId(R.id.menu_stats);

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
                return true;

            } else if (id == R.id.menu_forecast) {
                startActivity(new Intent(this, ForecastActivity.class));
                return true;
            }

            return false;
        });
    }
}
