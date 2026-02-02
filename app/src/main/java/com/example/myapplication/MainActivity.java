package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    TextView tvGreetingName;
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
        tvGreetingName = findViewById(R.id.tvGreetingName);
        barChart = findViewById(R.id.barChart);
        tvSeeAll = findViewById(R.id.tvSeeAll);
        tvNoTransactions = findViewById(R.id.tvNoTransactions);
        transactionListContainer = findViewById(R.id.transactionListContainer);
        tvGreetingName.setText(AuthStore.getName(this));

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
            } else if (id == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
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
        tvGreetingName.setText(AuthStore.getName(this));
        updateDashboard();
    }

    private void updateDashboard() {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
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
        updateDashboardTotals(income, expense);
        allTransactions.clear();
        renderTransactions();
    }

    private void updateDashboardTotals(long income, long expense) {
        long balance = income - expense;
        tvIncome.setText(TransactionStore.formatCurrency(income));
        tvExpense.setText(TransactionStore.formatCurrency(expense));
        tvBalance.setText(TransactionStore.formatCurrency(balance));
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
        int maxItems = isExpanded ? allTransactions.size() : Math.min(PREVIEW_LIMIT, allTransactions.size());
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

            String fallbackTitle = isIncome
                    ? getString(R.string.transaction_income_default)
                    : getString(R.string.transaction_expense_default);
            titleView.setText(category.isEmpty() ? fallbackTitle : category);
            String meta = note.isEmpty()
                    ? dateLabel
                    : note + getString(R.string.separator_dot) + dateLabel;
            metaView.setText(meta);

            String amountValue = TransactionStore.formatCurrency(transaction.getAmount());
            String amountLabel = isIncome
                    ? getString(R.string.amount_prefix_income, amountValue)
                    : getString(R.string.amount_prefix_expense, amountValue);
            amountView.setText(amountLabel);
            amountView.setTextColor(getColor(isIncome ? R.color.accent_green : R.color.accent_red));

            transactionListContainer.addView(itemView);
        }

        if (allTransactions.size() > PREVIEW_LIMIT) {
            tvSeeAll.setVisibility(View.VISIBLE);
            tvSeeAll.setText(isExpanded ? getString(R.string.main_collapse) : getString(R.string.main_see_all));
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
            barChart.setNoDataText(getString(R.string.chart_no_data));
            barChart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");

        // Màu sắc theo style mới
        dataSet.setColors(
                ContextCompat.getColor(this, R.color.chart_blue),
                ContextCompat.getColor(this, R.color.chart_red)
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
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        xAxis.setAxisLineColor(ContextCompat.getColor(this, R.color.divider_light));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(entries.size());
        xAxis.setDrawLabels(false);

        // Trục Y trái
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        leftAxis.setGridColor(ContextCompat.getColor(this, R.color.divider_light));
        leftAxis.setAxisMinimum(0f);

        // Trục Y phải
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        barChart.animateY(800);
        barChart.invalidate();
    }
}
