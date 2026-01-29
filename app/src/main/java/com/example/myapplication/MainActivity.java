package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private static final int PREVIEW_LIMIT = 3;

    TextView tvIncome, tvExpense, tvBalance;
    BarChart barChart;
    TextView tvSeeAll;
    TextView tvNoTransactions;
    RecyclerView rvTransactions;
    TransactionAdapter transactionAdapter;
    boolean isShowingAll = false;
    List<TransactionResponse> transactions = new ArrayList<>();

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
        rvTransactions = findViewById(R.id.rvTransactions);

        transactionAdapter = new TransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);

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
            isShowingAll = !isShowingAll;
            updateTransactionList();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransactions();
    }

    private void fetchTransactions() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getTransactions().enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transactions = response.body();
                    updateDashboard(transactions);
                } else {
                    transactions = new ArrayList<>();
                    updateDashboard(transactions);
                }
            }

            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                transactions = new ArrayList<>();
                updateDashboard(transactions);
            }
        });
    }

    private void updateDashboard(List<TransactionResponse> transactionList) {
        long income = 0L;
        long expense = 0L;
        for (TransactionResponse transaction : transactionList) {
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

        updateTransactionList();
        setupBarChart(income, expense);
    }

    private void updateTransactionList() {
        if (transactions == null || transactions.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
            tvSeeAll.setVisibility(View.GONE);
            return;
        }

        tvNoTransactions.setVisibility(View.GONE);
        rvTransactions.setVisibility(View.VISIBLE);

        int limit = Math.min(PREVIEW_LIMIT, transactions.size());
        List<TransactionResponse> displayList = isShowingAll
                ? new ArrayList<>(transactions)
                : new ArrayList<>(transactions.subList(0, limit));
        transactionAdapter.submitList(displayList);

        if (transactions.size() > PREVIEW_LIMIT) {
            tvSeeAll.setVisibility(View.VISIBLE);
            tvSeeAll.setText(isShowingAll ? "Thu gọn" : "Xem tất cả");
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
