package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView tvIncome, tvExpense, tvBalance;
    BarChart barChart;
    TextView tvSeeAll;
    TextView tvNoTransactions;
    LinearLayout transactionListContainer;
    private boolean showAllTransactions = false;
    private List<TransactionResponse> transactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TransactionStore.ensureDemoData(this);

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
            showAllTransactions = !showAllTransactions;
            renderTransactionList();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
        loadTransactions();
    }

    private void updateDashboard() {
        long income = TransactionStore.getIncomeTotal(this);
        long expense = TransactionStore.getExpenseTotal(this);
        long balance = income - expense;

        tvIncome.setText(TransactionStore.formatCurrency(income));
        tvExpense.setText(TransactionStore.formatCurrency(expense));
        tvBalance.setText(TransactionStore.formatCurrency(balance));

        setupBarChart(income, expense);
    }

    private void loadTransactions() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getTransactions().enqueue(new retrofit2.Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<List<TransactionResponse>> call,
                                   retrofit2.Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transactions = response.body();
                    renderTransactionList();
                } else {
                    showTransactionError("Không lấy được dữ liệu giao dịch.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<TransactionResponse>> call, Throwable t) {
                showTransactionError("Không kết nối được máy chủ.");
            }
        });
    }

    private void showTransactionError(String message) {
        transactions = new ArrayList<>();
        transactionListContainer.setVisibility(View.GONE);
        tvNoTransactions.setVisibility(View.VISIBLE);
        tvNoTransactions.setText(message);
        tvSeeAll.setVisibility(View.GONE);
    }

    private void renderTransactionList() {
        transactionListContainer.removeAllViews();
        if (transactions == null || transactions.isEmpty()) {
            transactionListContainer.setVisibility(View.GONE);
            tvNoTransactions.setVisibility(View.VISIBLE);
            tvNoTransactions.setText("Chưa có giao dịch nào. Hãy thêm giao dịch mới để hiển thị ở đây.");
            tvSeeAll.setVisibility(View.GONE);
            return;
        }

        int total = transactions.size();
        int displayCount = showAllTransactions ? total : Math.min(3, total);
        for (int i = 0; i < displayCount; i++) {
            TransactionResponse transaction = transactions.get(i);
            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_transaction, transactionListContainer, false);

            TextView titleView = itemView.findViewById(R.id.tvTransactionTitle);
            TextView metaView = itemView.findViewById(R.id.tvTransactionMeta);
            TextView amountView = itemView.findViewById(R.id.tvTransactionAmount);

            boolean isExpense = "expense".equalsIgnoreCase(transaction.getType())
                    || "chi".equalsIgnoreCase(transaction.getType());
            String typeLabel = isExpense ? "Chi" : "Thu";
            String categoryLabel = transaction.getCategory() == null ? "Khác" : transaction.getCategory();
            String noteLabel = transaction.getNote();
            if (noteLabel == null || noteLabel.trim().isEmpty()) {
                noteLabel = "Không ghi chú";
            }

            titleView.setText(typeLabel + " • " + categoryLabel);
            metaView.setText(noteLabel);
            amountView.setText((isExpense ? "-" : "+") + TransactionStore.formatCurrency(transaction.getAmount()));
            amountView.setTextColor(getColor(isExpense ? R.color.accent_red : R.color.accent_green));

            transactionListContainer.addView(itemView);
        }

        transactionListContainer.setVisibility(View.VISIBLE);
        tvNoTransactions.setVisibility(View.GONE);

        if (total > 3) {
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
