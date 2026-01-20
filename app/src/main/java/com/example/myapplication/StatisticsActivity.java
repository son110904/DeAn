package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "finance_prefs";
    private static final String KEY_INCOME_TOTAL = "income_total";
    private static final String KEY_EXPENSE_TOTAL = "expense_total";

    PieChart pieChart;
    BottomNavigationView bottomNav;
    TextView tvStatsIncome;
    TextView tvStatsExpense;
    TextView tvStatsEmpty;
    FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        pieChart = findViewById(R.id.pieChart);
        bottomNav = findViewById(R.id.bottomNav);
        tvStatsIncome = findViewById(R.id.tvStatsIncome);
        tvStatsExpense = findViewById(R.id.tvStatsExpense);
        tvStatsEmpty = findViewById(R.id.tvStatsEmpty);
        fabAdd = findViewById(R.id.fabAdd);

        updatePieChart();

        // Đánh dấu tab hiện tại
        bottomNav.setSelectedItemId(R.id.menu_stats);

        // Điều hướng
        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;

            } else if (id == R.id.menu_stats) {
                return true;

            } else if (id == R.id.menu_forecast) {
                startActivity(new Intent(this, ForecastActivity.class));
                return true;
            }

            return false;
        });

        fabAdd.setOnClickListener(v ->
            startActivity(new Intent(this, AddTransactionActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePieChart();
    }

    private void updatePieChart() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long income = prefs.getLong(KEY_INCOME_TOTAL, 0);
        long expense = prefs.getLong(KEY_EXPENSE_TOTAL, 0);

        tvStatsIncome.setText(income + "đ");
        tvStatsExpense.setText(expense + "đ");

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(income, "Thu nhập"));
        entries.add(new PieEntry(expense, "Chi tiêu"));

        PieDataSet dataSet = new PieDataSet(entries, "Tổng quan thu/chi");
        dataSet.setColors(
            Color.parseColor("#5B8FF9"),
            Color.parseColor("#F6BD16")
        );
        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();

        boolean hasData = income > 0 || expense > 0;
        tvStatsEmpty.setVisibility(hasData ? View.GONE : View.VISIBLE);
    }
}
