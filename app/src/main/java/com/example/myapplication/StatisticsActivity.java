package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {

    TextView tvBudgetExpenseTab;
    TextView tvBudgetRemaining;
    TextView tvBudgetPercent;
    TextView tvBudgetSpent;
    TextView tvBudgetRemainingValue;
    TextView tvBudgetTotal;
    ProgressBar progressBudgetTotal;
    LinearLayout budgetCategoryContainer;
    Spinner spinnerMonthYear;
    BarChart barChartMonthly;
    BottomNavigationView bottomNav;

    private final List<TransactionResponse> allTransactions = new ArrayList<>();
    private final List<String> monthKeys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tvBudgetExpenseTab = findViewById(R.id.tvBudgetExpenseTab);
        tvBudgetRemaining = findViewById(R.id.tvBudgetRemaining);
        tvBudgetPercent = findViewById(R.id.tvBudgetPercent);
        tvBudgetSpent = findViewById(R.id.tvBudgetSpent);
        tvBudgetRemainingValue = findViewById(R.id.tvBudgetRemainingValue);
        tvBudgetTotal = findViewById(R.id.tvBudgetTotal);
        progressBudgetTotal = findViewById(R.id.progressBudgetTotal);
        budgetCategoryContainer = findViewById(R.id.budgetCategoryContainer);
        spinnerMonthYear = findViewById(R.id.spinnerMonthYear);
        barChartMonthly = findViewById(R.id.barChartMonthly);
        bottomNav = findViewById(R.id.bottomNav);

        setupMonthlyChart();
        bottomNav.setSelectedItemId(R.id.menu_stats);

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
            } else if (id == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransactions();
    }

    private void fetchTransactions() {
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        apiService.getTransactions().enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allTransactions.clear();
                    allTransactions.addAll(response.body());
                    setupMonthSpinner();
                    fetchMonthlyStatistics();
                } else {
                    updateBudgetFromStore();
                }
            }

            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                updateBudgetFromStore();
            }
        });
    }

    private void fetchMonthlyStatistics() {
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        apiService.getMonthlyStatistics().enqueue(new Callback<List<MonthlyStatisticResponse>>() {
            @Override
            public void onResponse(Call<List<MonthlyStatisticResponse>> call, Response<List<MonthlyStatisticResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    renderMonthlyChart(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<MonthlyStatisticResponse>> call, Throwable t) {
                barChartMonthly.clear();
            }
        });
    }

    private void setupMonthSpinner() {
        monthKeys.clear();
        for (TransactionResponse transaction : allTransactions) {
            String key = extractMonthKey(transaction.getCreatedAt());
            if (!key.isEmpty() && !monthKeys.contains(key)) {
                monthKeys.add(key);
            }
        }
        monthKeys.sort(Comparator.reverseOrder());

        if (monthKeys.isEmpty()) {
            monthKeys.add(getString(R.string.statistics_all_months));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                monthKeys
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonthYear.setAdapter(adapter);
        spinnerMonthYear.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateBudgetByMonth(monthKeys.get(position));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        updateBudgetByMonth(monthKeys.get(0));
    }

    private void updateBudgetByMonth(String monthKey) {
        long incomeTotal = 0L;
        long expenseTotal = 0L;
        Map<String, Long> categoryTotals = new HashMap<>();

        for (TransactionResponse transaction : allTransactions) {
            String txMonth = extractMonthKey(transaction.getCreatedAt());
            if (!monthKey.equals(getString(R.string.statistics_all_months)) && !monthKey.equals(txMonth)) {
                continue;
            }
            if ("income".equalsIgnoreCase(transaction.getType())) {
                incomeTotal += transaction.getAmount();
            } else {
                expenseTotal += transaction.getAmount();
                String categoryKey = TransactionStore.normalizeCategory(this, transaction.getCategory());
                categoryTotals.put(categoryKey, categoryTotals.getOrDefault(categoryKey, 0L) + transaction.getAmount());
            }
        }

        long remaining = incomeTotal - expenseTotal;
        applyBudgetSummary(incomeTotal, expenseTotal, categoryTotals, remaining);
    }

    private void updateBudgetFromStore() {
        long incomeTotal = TransactionStore.getIncomeTotal(this);
        long expenseTotal = TransactionStore.getExpenseTotal(this);
        Map<String, Long> categoryTotals = TransactionStore.getCategoryTotals(this);
        long remaining = incomeTotal - expenseTotal;
        applyBudgetSummary(incomeTotal, expenseTotal, categoryTotals, remaining);
    }

    private void applyBudgetSummary(long incomeTotal, long expenseTotal, Map<String, Long> categoryTotals, long remaining) {
        tvBudgetExpenseTab.setText(getString(R.string.statistics_expense_tab_amount,
                TransactionStore.formatCurrency(expenseTotal)));
        tvBudgetRemaining.setText(TransactionStore.formatCurrency(remaining));
        tvBudgetTotal.setText(TransactionStore.formatCurrency(incomeTotal));
        tvBudgetSpent.setText(TransactionStore.formatCurrency(expenseTotal));
        tvBudgetRemainingValue.setText(TransactionStore.formatCurrency(Math.max(remaining, 0)));

        int percent = incomeTotal > 0 ? Math.min(100, Math.round((expenseTotal * 100f) / incomeTotal)) : 0;
        tvBudgetPercent.setText(getString(R.string.percent_format, percent));
        progressBudgetTotal.setProgress(percent);

        renderCategories(expenseTotal, categoryTotals);
    }

    private void setupMonthlyChart() {
        barChartMonthly.getDescription().setEnabled(false);
        barChartMonthly.getLegend().setEnabled(true);
        barChartMonthly.setNoDataText(getString(R.string.chart_no_data));
        barChartMonthly.setFitBars(true);
        XAxis xAxis = barChartMonthly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        barChartMonthly.getAxisRight().setEnabled(false);
    }

    private void renderMonthlyChart(List<MonthlyStatisticResponse> statistics) {
        if (statistics.isEmpty()) {
            barChartMonthly.clear();
            return;
        }

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < statistics.size(); i++) {
            MonthlyStatisticResponse stat = statistics.get(i);
            incomeEntries.add(new BarEntry(i, stat.getIncome()));
            expenseEntries.add(new BarEntry(i, stat.getExpense()));
            labels.add(stat.getMonth());
        }

        BarDataSet incomeSet = new BarDataSet(incomeEntries, getString(R.string.statistics_income_tab));
        incomeSet.setColor(ContextCompat.getColor(this, R.color.primary_blue));
        BarDataSet expenseSet = new BarDataSet(expenseEntries, getString(R.string.statistics_expense_tab));
        expenseSet.setColor(ContextCompat.getColor(this, R.color.accent_red));

        BarData data = new BarData(incomeSet, expenseSet);
        float groupSpace = 0.24f;
        float barSpace = 0.02f;
        float barWidth = 0.36f;
        data.setBarWidth(barWidth);

        XAxis xAxis = barChartMonthly.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        barChartMonthly.setData(data);
        barChartMonthly.getXAxis().setAxisMinimum(0f);
        barChartMonthly.getXAxis().setAxisMaximum(0f + data.getGroupWidth(groupSpace, barSpace) * labels.size());
        barChartMonthly.groupBars(0f, groupSpace, barSpace);
        barChartMonthly.invalidate();
    }

    private void renderCategories(long expenseTotal, Map<String, Long> categoryTotals) {
        budgetCategoryContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        if (expenseTotal <= 0 || categoryTotals.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText(getString(R.string.statistics_no_category));
            emptyView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            emptyView.setTextSize(14);
            int padding = dpToPx(16);
            emptyView.setPadding(padding, padding, padding, padding);
            budgetCategoryContainer.addView(emptyView);
            return;
        }

        List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>(categoryTotals.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (Map.Entry<String, Long> entry : sortedEntries) {
            long spent = entry.getValue();
            if (spent <= 0) {
                continue;
            }

            View itemView = inflater.inflate(R.layout.item_budget_category, budgetCategoryContainer, false);
            TextView nameView = itemView.findViewById(R.id.tvCategoryName);
            TextView amountView = itemView.findViewById(R.id.tvCategoryAmount);
            TextView percentView = itemView.findViewById(R.id.tvCategoryPercent);
            TextView spentView = itemView.findViewById(R.id.tvCategorySpent);
            ProgressBar progressBar = itemView.findViewById(R.id.progressCategory);

            int percent = expenseTotal > 0 ? Math.min(100, Math.round((spent * 100f) / expenseTotal)) : 0;

            nameView.setText(entry.getKey());
            amountView.setText(getString(R.string.statistics_weighted_label));
            percentView.setText(getString(R.string.percent_format, percent));
            spentView.setText(getString(R.string.statistics_spent_prefix, TransactionStore.formatCurrency(spent)));
            progressBar.setProgress(percent);

            budgetCategoryContainer.addView(itemView);
        }
    }

    private String extractMonthKey(String createdAt) {
        if (createdAt == null || createdAt.length() < 7) {
            return "";
        }
        return createdAt.substring(0, 7);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
