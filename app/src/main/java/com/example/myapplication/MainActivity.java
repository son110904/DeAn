package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    TextView tvIncome, tvExpense, tvBalance;
    BarChart barChart;
    TextView tvSeeAll;
    TextView tvNoTransactions;
    LinearLayout transactionListContainer;
    private final List<TransactionResponse> allTransactions = new ArrayList<>();
    private boolean isExpanded = false;

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
        transactionListContainer = findViewById(R.id.transactionListContainer);

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
            isExpanded = !isExpanded;
            renderTransactions();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
        loadTransactions();
    }

    private void updateDashboard() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getTransactions().enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allTransactions.clear();
                    allTransactions.addAll(response.body());
                    TransactionStore.syncFromTransactions(MainActivity.this, allTransactions);
                    updateDashboardFromTransactions(allTransactions);
                } else {
                    updateDashboardFromStore();
                }
            }

            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                updateDashboardFromStore();
            }
        });
    }

    private void updateDashboardFromTransactions(List<TransactionResponse> transactions) {
        long income = 0L;
        long expense = 0L;

        for (TransactionResponse transaction : transactions) {
            if ("income".equalsIgnoreCase(transaction.getType())) {
                income += transaction.getAmount();
            } else {
                expense += transaction.getAmount();
            }
        }

        long balance = income - expense;
        tvIncome.setText(TransactionStore.formatCurrency(income));
        tvExpense.setText(TransactionStore.formatCurrency(expense));
        tvBalance.setText(TransactionStore.formatCurrency(balance));

        renderTransactions();
        setupBarChart(income, expense);
    }

    private void updateDashboardFromStore() {
        long income = TransactionStore.getIncomeTotal(this);
        long expense = TransactionStore.getExpenseTotal(this);
        long balance = income - expense;

        tvIncome.setText(TransactionStore.formatCurrency(income));
        tvExpense.setText(TransactionStore.formatCurrency(expense));
        tvBalance.setText(TransactionStore.formatCurrency(balance));

        renderTransactions();
        setupBarChart(income, expense);
    }

    private void renderTransactions() {
        transactionListContainer.removeAllViews();

        if (allTransactions.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            tvSeeAll.setVisibility(View.GONE);
            return;
        }

        tvNoTransactions.setVisibility(View.GONE);
        int maxItems = isExpanded ? allTransactions.size() : Math.min(3, allTransactions.size());
        List<TransactionResponse> visible = allTransactions.subList(0, maxItems);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (TransactionResponse transaction : visible) {
            View itemView = inflater.inflate(R.layout.item_transaction, transactionListContainer, false);
            TextView titleView = itemView.findViewById(R.id.tvTransactionTitle);
            TextView metaView = itemView.findViewById(R.id.tvTransactionMeta);
            TextView amountView = itemView.findViewById(R.id.tvTransactionAmount);

            String type = transaction.getType();
            boolean isIncome = "income".equalsIgnoreCase(type);
            String category = transaction.getCategory() != null ? transaction.getCategory() : "";
            String note = transaction.getNote() != null ? transaction.getNote() : "";
            String dateLabel = formatDate(transaction.getCreatedAt());

            titleView.setText(category.isEmpty() ? (isIncome ? "Thu nhập" : "Chi tiêu") : category);
            String meta = note.isEmpty() ? dateLabel : note + " • " + dateLabel;
            metaView.setText(meta);

            String amountLabel = (isIncome ? "+ " : "- ") + TransactionStore.formatCurrency(transaction.getAmount());
            amountView.setText(amountLabel);
            amountView.setTextColor(getColor(isIncome ? R.color.accent_green : R.color.accent_red));

            transactionListContainer.addView(itemView);
        }

        if (allTransactions.size() > 3) {
            tvSeeAll.setVisibility(View.VISIBLE);
            tvSeeAll.setText(isExpanded ? "Thu gọn" : "Xem tất cả");
        } else {
            tvSeeAll.setVisibility(View.GONE);
        }
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "";
        }
        String[] parts = rawDate.split("T");
        if (parts.length > 0 && !parts[0].isEmpty()) {
            return parts[0];
        }
        return rawDate;
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
