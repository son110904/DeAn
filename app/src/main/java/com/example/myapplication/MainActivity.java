package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView tvIncome, tvExpense, tvBalance;
    BarChart barChart;
    TextView tvSeeAll;
    TextView tvNoTransactions;
    LinearLayout transactionList;
    boolean showAllTransactions = false;
    List<TransactionResponse> cachedTransactions = Collections.emptyList();
    private static final int PREVIEW_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ view
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        barChart = findViewById(R.id.barChart);
        tvSeeAll = findViewById(R.id.tvSeeAll);
        tvNoTransactions = findViewById(R.id.tvNoTransactions);
        transactionList = findViewById(R.id.transactionList);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.menu_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
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
                startActivity(new Intent(this, ForecastActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

        tvSeeAll.setOnClickListener(v -> {
            showAllTransactions = !showAllTransactions;
            renderTransactions();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransactions();
    }

    private void fetchTransactions() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getTransactions().enqueue(new retrofit2.Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<List<TransactionResponse>> call,
                                   retrofit2.Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cachedTransactions = response.body();
                    updateDashboardFromTransactions(cachedTransactions);
                } else {
                    updateDashboardFallback();
                }
                renderTransactions();
            }

            @Override
            public void onFailure(retrofit2.Call<List<TransactionResponse>> call, Throwable t) {
                updateDashboardFallback();
                renderTransactions();
            }
        });
    }

    private void updateDashboardFromTransactions(List<TransactionResponse> transactions) {
        long income = 0L;
        long expense = 0L;

        for (TransactionResponse transaction : transactions) {
            long amount = transaction.getAmount();
            String type = transaction.getType() == null ? "" : transaction.getType().toLowerCase();
            if (type.contains("income") || type.contains("thu")) {
                income += amount;
            } else {
                expense += amount;
            }
        }

        updateDashboardTotals(income, expense);
    }

    private void updateDashboardFallback() {
        long income = TransactionStore.getIncomeTotal(this);
        long expense = TransactionStore.getExpenseTotal(this);
        updateDashboardTotals(income, expense);
    }

    private void updateDashboardTotals(long income, long expense) {
        long balance = income - expense;
        tvIncome.setText(TransactionStore.formatCurrency(income));
        tvExpense.setText(TransactionStore.formatCurrency(expense));
        tvBalance.setText(TransactionStore.formatCurrency(balance));
        setupBarChart(income, expense);
    }

    private void renderTransactions() {
        if (cachedTransactions == null) {
            cachedTransactions = Collections.emptyList();
        }

        transactionList.removeAllViews();
        int totalCount = cachedTransactions.size();
        if (totalCount == 0) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            tvSeeAll.setVisibility(View.GONE);
            return;
        }

        tvNoTransactions.setVisibility(View.GONE);
        int displayCount = showAllTransactions ? totalCount : Math.min(PREVIEW_COUNT, totalCount);
        for (int i = 0; i < displayCount; i++) {
            TransactionResponse transaction = cachedTransactions.get(i);
            View itemView = getLayoutInflater().inflate(R.layout.item_transaction, transactionList, false);
            TextView titleView = itemView.findViewById(R.id.tvTransactionTitle);
            TextView subtitleView = itemView.findViewById(R.id.tvTransactionSubtitle);
            TextView amountView = itemView.findViewById(R.id.tvTransactionAmount);

            String category = transaction.getCategory() == null ? "Khác" : transaction.getCategory();
            String note = transaction.getNote();
            String createdAt = transaction.getCreatedAt();
            titleView.setText(category);

            StringBuilder subtitle = new StringBuilder();
            if (note != null && !note.isEmpty()) {
                subtitle.append(note);
            }
            if (createdAt != null && !createdAt.isEmpty()) {
                if (subtitle.length() > 0) {
                    subtitle.append(" • ");
                }
                String cleaned = createdAt.replace("T", " ");
                int dotIndex = cleaned.indexOf(".");
                if (dotIndex > 0) {
                    cleaned = cleaned.substring(0, dotIndex);
                }
                subtitle.append(cleaned);
            }
            subtitleView.setText(subtitle.toString());

            String type = transaction.getType() == null ? "" : transaction.getType().toLowerCase();
            boolean isIncome = type.contains("income") || type.contains("thu");
            String prefix = isIncome ? "+" : "-";
            amountView.setText(prefix + TransactionStore.formatCurrency(transaction.getAmount()));
            amountView.setTextColor(getColor(isIncome ? R.color.accent_green : R.color.accent_red));

            transactionList.addView(itemView);
        }

        if (totalCount > PREVIEW_COUNT) {
            tvSeeAll.setVisibility(View.VISIBLE);
            tvSeeAll.setText(showAllTransactions ? "Thu gọn" : "Xem tất cả");
        } else {
            tvSeeAll.setVisibility(View.GONE);
        }
    }

    private void setupBarChart(long income, long expense) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        if (income > 0) {
            entries.add(new BarEntry(0f, income));
        }
        if (expense > 0) {
            entries.add(new BarEntry(1f, expense));
        }

        if (entries.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("Chưa có dữ liệu biểu đồ.");
            barChart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");

        // Màu sắc theo style mới
        dataSet.setColors(
                Color.parseColor("#5B8FF9"),
                Color.parseColor("#E8684A")
        );

        dataSet.setValueTextSize(0f); // Ẩn giá trị trên cột
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);

        // Tùy chỉnh biểu đồ theo style mới
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);

        // Trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextColor(Color.parseColor("#8E8E93"));
        xAxis.setAxisLineColor(Color.parseColor("#ECF0F1"));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(entries.size());
        xAxis.setDrawLabels(false);

        // Trục Y trái
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(Color.parseColor("#8E8E93"));
        leftAxis.setGridColor(Color.parseColor("#ECF0F1"));
        leftAxis.setAxisMinimum(0f);

        // Trục Y phải
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        barChart.animateY(800);
        barChart.invalidate();
    }
}
