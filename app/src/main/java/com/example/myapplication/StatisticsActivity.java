package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.components.Legend;
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

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Chưa có dữ liệu chi tiêu.");
            pieChart.invalidate();
        } else {
            PieDataSet dataSet = new PieDataSet(entries, "");

            // Màu sắc theo style mới
            dataSet.setColors(
                    Color.parseColor("#5B8FF9"),
                    Color.parseColor("#61DDAA"),
                    Color.parseColor("#F6BD16")
            );

            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(8f);
            dataSet.setValueTextSize(14f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueFormatter(new PercentFormatter(pieChart));

            PieData data = new PieData(dataSet);

            pieChart.setData(data);
            pieChart.getDescription().setEnabled(false);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(Color.TRANSPARENT);
            pieChart.setHoleRadius(60f);
            pieChart.setTransparentCircleRadius(65f);
            pieChart.setDrawCenterText(false);
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(true);

            // Tùy chỉnh legend
            Legend legend = pieChart.getLegend();
            legend.setEnabled(false); // Tắt legend vì đã có trong layout

            pieChart.setUsePercentValues(true);
            pieChart.setEntryLabelColor(Color.TRANSPARENT);
            pieChart.animateY(1000);
            pieChart.invalidate();
        }

        // Đánh dấu tab hiện tại
        bottomNav.setSelectedItemId(R.id.menu_stats);

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
                return true;
            } else if (id == R.id.menu_forecast) {
                startActivity(new Intent(this, ForecastActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}
