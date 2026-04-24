package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {

    TextView tvBudgetRemaining;
    TextView tvBudgetPercent;
    TextView tvBudgetSpent;
    TextView tvBudgetRemainingValue;
    TextView tvBudgetTotal;
    ProgressBar progressBudgetTotal;
    LinearLayout budgetCategoryContainer;
    LinearLayout topTransactionsContainer;
    Spinner spinnerMonthYear;
    PieChart pieChartCategory;
    LineChart lineChartDaily;
    BottomNavigationView bottomNav;

    private final List<TransactionResponse> allTransactions = new ArrayList<>();
    private final List<String> monthKeys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tvBudgetRemaining = findViewById(R.id.tvBudgetRemaining);
        tvBudgetPercent = findViewById(R.id.tvBudgetPercent);
        tvBudgetSpent = findViewById(R.id.tvBudgetSpent);
        tvBudgetRemainingValue = findViewById(R.id.tvBudgetRemainingValue);
        tvBudgetTotal = findViewById(R.id.tvBudgetTotal);
        progressBudgetTotal = findViewById(R.id.progressBudgetTotal);
        budgetCategoryContainer = findViewById(R.id.budgetCategoryContainer);
        topTransactionsContainer = findViewById(R.id.topTransactionsContainer);
        spinnerMonthYear = findViewById(R.id.spinnerMonthYear);
        pieChartCategory = findViewById(R.id.pieChartCategory);
        lineChartDaily = findViewById(R.id.lineChartDaily);
        bottomNav = findViewById(R.id.bottomNav);

        setupPieChart();
        setupLineChart();
        bottomNav.setSelectedItemId(R.id.menu_stats);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_budget) {
                startActivity(new Intent(this, BudgetActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_add) {
                startActivity(new Intent(this, AddTransactionActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_stats) {
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

    private void setupMonthSpinner() {
        monthKeys.clear();
        for (TransactionResponse transaction : allTransactions) {
            String key = extractMonthKey(transaction.getDate());
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
                String selectedMonth = monthKeys.get(position);
                updateBudgetByMonth(selectedMonth);
                fetchDailySummary(resolveDailySummaryMonth(selectedMonth));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        spinnerMonthYear.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showMonthYearPicker();
            }
            return true;
        });
        String initialMonth = monthKeys.get(0);
        updateBudgetByMonth(initialMonth);
        fetchDailySummary(resolveDailySummaryMonth(initialMonth));
    }

    private void showMonthYearPicker() {
        Calendar cal = Calendar.getInstance();
        int initialYear = cal.get(Calendar.YEAR);
        int initialMonth = cal.get(Calendar.MONTH) + 1; // 1..12

        Object selectedItem = spinnerMonthYear.getSelectedItem();
        if (selectedItem instanceof String) {
            String selected = (String) selectedItem;
            if (selected.matches("\\d{4}-\\d{2}")) {
                try {
                    initialYear = Integer.parseInt(selected.substring(0, 4));
                    initialMonth = Integer.parseInt(selected.substring(5, 7));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        NumberPicker monthPicker = new NumberPicker(this);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(Math.min(12, Math.max(1, initialMonth)));

        NumberPicker yearPicker = new NumberPicker(this);
        yearPicker.setMinValue(2000);
        yearPicker.setMaxValue(2100);
        yearPicker.setValue(initialYear);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        int pad = dpToPx(16);
        container.setPadding(pad, pad, pad, pad);
        container.addView(monthPicker, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        container.addView(yearPicker, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.statistics_select_month_year))
                .setView(container)
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    String monthKey = String.format(Locale.US, "%04d-%02d", yearPicker.getValue(), monthPicker.getValue());
                    applySelectedMonth(monthKey);
                })
                .setNeutralButton(getString(R.string.statistics_all_months), (d, which) -> applySelectedMonth(getString(R.string.statistics_all_months)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void applySelectedMonth(String monthKey) {
        if (monthKey == null || monthKey.isEmpty()) return;

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerMonthYear.getAdapter();
        if (adapter == null) return;

        int index = monthKeys.indexOf(monthKey);
        if (index < 0) {
            int insertAt = 0;
            String allLabel = getString(R.string.statistics_all_months);
            if (!monthKeys.isEmpty() && allLabel.equals(monthKeys.get(0))) {
                insertAt = 1;
            }
            monthKeys.add(insertAt, monthKey);
            adapter.notifyDataSetChanged();
            index = insertAt;
        }

        spinnerMonthYear.setSelection(index);
        updateBudgetByMonth(monthKey);
        fetchDailySummary(resolveDailySummaryMonth(monthKey));
    }

    private void updateBudgetByMonth(String monthKey) {
        long incomeTotal = 0L;
        long expenseTotal = 0L;
        Map<String, Long> categoryTotals = new HashMap<>();
        List<TransactionResponse> filteredTransactions = new ArrayList<>();

        for (TransactionResponse transaction : allTransactions) {
            String txMonth = extractMonthKey(transaction.getDate());
            if (!monthKey.equals(getString(R.string.statistics_all_months)) && !monthKey.equals(txMonth)) {
                continue;
            }
            filteredTransactions.add(transaction);
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
        renderTopTransactions(filteredTransactions);
    }

    private void updateBudgetFromStore() {
        long incomeTotal = TransactionStore.getIncomeTotal(this);
        long expenseTotal = TransactionStore.getExpenseTotal(this);
        Map<String, Long> categoryTotals = TransactionStore.getCategoryTotals(this);
        long remaining = incomeTotal - expenseTotal;
        applyBudgetSummary(incomeTotal, expenseTotal, categoryTotals, remaining);
    }

    private void applyBudgetSummary(long incomeTotal, long expenseTotal, Map<String, Long> categoryTotals, long remaining) {
        tvBudgetRemaining.setText(TransactionStore.formatCurrency(remaining));
        tvBudgetTotal.setText(TransactionStore.formatCurrency(incomeTotal));
        tvBudgetSpent.setText(TransactionStore.formatCurrency(expenseTotal));
        tvBudgetRemainingValue.setText(TransactionStore.formatCurrency(Math.max(remaining, 0)));

        int percent = incomeTotal > 0 ? Math.min(100, Math.round((expenseTotal * 100f) / incomeTotal)) : 0;
        tvBudgetPercent.setText(getString(R.string.percent_format, percent));
        progressBudgetTotal.setProgress(percent);

        renderCategories(expenseTotal, categoryTotals);
        renderPieChart(categoryTotals, expenseTotal);
    }

    private void setupPieChart() {
        pieChartCategory.setUsePercentValues(true);
        pieChartCategory.getDescription().setEnabled(false);
        pieChartCategory.setExtraOffsets(5, 10, 5, 5);
        pieChartCategory.setDragDecelerationFrictionCoef(0.95f);
        pieChartCategory.setDrawHoleEnabled(true);
        pieChartCategory.setHoleColor(Color.WHITE);
        pieChartCategory.setTransparentCircleColor(Color.WHITE);
        pieChartCategory.setTransparentCircleAlpha(110);
        pieChartCategory.setHoleRadius(58f);
        pieChartCategory.setTransparentCircleRadius(61f);
        pieChartCategory.setDrawEntryLabels(false);
        pieChartCategory.setDrawCenterText(false);
        pieChartCategory.setRotationAngle(0);
        pieChartCategory.setRotationEnabled(true);
        pieChartCategory.setHighlightPerTapEnabled(true);
        pieChartCategory.setCenterTextSize(10f);
        pieChartCategory.getLegend().setEnabled(true);
        pieChartCategory.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        pieChartCategory.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        pieChartCategory.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
    }

    private void setupLineChart() {
        lineChartDaily.getDescription().setEnabled(false);
        lineChartDaily.setDrawGridBackground(false);
        XAxis xAxis = lineChartDaily.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        lineChartDaily.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return TransactionStore.formatCurrency((long) value);
            }
        });
        lineChartDaily.getAxisRight().setEnabled(false);
    }

    @SuppressLint("StringFormatInvalid")
    private void renderPieChart(Map<String, Long> categoryTotals, long expenseTotal) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (categoryTotals.isEmpty() || expenseTotal <= 0) {
            pieChartCategory.clear();
            pieChartCategory.setNoDataText(getString(R.string.chart_no_data));
            pieChartCategory.invalidate();
            return;
        }
        pieChartCategory.setDrawCenterText(false);
        pieChartCategory.setCenterText("");

        List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>(categoryTotals.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (Map.Entry<String, Long> entry : sortedEntries) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setDrawValues(false);

        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartCategory));
        data.setValueTextSize(0f);
        data.setValueTextColor(Color.BLACK);
        
        pieChartCategory.setData(data);
        pieChartCategory.invalidate();
    }

    private void fetchDailySummary(String month) {
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        apiService.getDailySummary(month).enqueue(new Callback<List<DailySpendingResponse>>() {
            @Override
            public void onResponse(Call<List<DailySpendingResponse>> call, Response<List<DailySpendingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    renderLineChart(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<DailySpendingResponse>> call, Throwable t) {}
        });
    }

    private String resolveDailySummaryMonth(String selectedMonth) {
        if (selectedMonth == null || selectedMonth.isEmpty()) {
            return "";
        }
        if (selectedMonth.equals(getString(R.string.statistics_all_months))) {
            return "";
        }
        return selectedMonth;
    }

    private void renderLineChart(List<DailySpendingResponse> dailyData) {
        if (dailyData == null || dailyData.isEmpty()) {
            lineChartDaily.clear();
            lineChartDaily.setNoDataText(getString(R.string.chart_no_data));
            lineChartDaily.invalidate();
            return;
        }

        List<DailySpendingResponse> sorted = new ArrayList<>(dailyData);
        sorted.sort(Comparator.comparing(DailySpendingResponse::getDate));

        ArrayList<Entry> entries = new ArrayList<>();
        int maxDay = 1;
        for (DailySpendingResponse item : sorted) {
            int day = extractDayOfMonth(item.getDate());
            if (day <= 0) continue;
            maxDay = Math.max(maxDay, day);
            entries.add(new Entry(day, item.getAmount()));
        }

        XAxis xAxis = lineChartDaily.getXAxis();
        xAxis.setAxisMinimum(1f);
        xAxis.setAxisMaximum((float) maxDay);
        xAxis.setLabelCount(Math.min(Math.max(maxDay, 2), 7), false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int day = Math.round(value);
                return String.valueOf(day);
            }
        });

        LineDataSet dataSet = new LineDataSet(entries, "Chi tiêu theo ngày");
        dataSet.setColor(ContextCompat.getColor(this, R.color.primary_blue));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData data = new LineData(dataSet);
        lineChartDaily.setData(data);
        lineChartDaily.invalidate();
    }

    private int extractDayOfMonth(String date) {
        if (date == null || date.isEmpty()) return -1;
        try {
            String d = date.split("T")[0];
            String[] parts = d.split("-");
            if (parts.length >= 3) {
                return Integer.parseInt(parts[2]);
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    private void renderTopTransactions(List<TransactionResponse> transactions) {
        topTransactionsContainer.removeAllViews();
        transactions.sort((t1, t2) -> Long.compare(t2.getAmount(), t1.getAmount()));
        
        LayoutInflater inflater = LayoutInflater.from(this);
        int count = 0;
        for (TransactionResponse t : transactions) {
            if (count >= 5) break;
            View view = inflater.inflate(R.layout.item_transaction, topTransactionsContainer, false);
            TextView title = view.findViewById(R.id.tvTransactionTitle);
            TextView amount = view.findViewById(R.id.tvTransactionAmount);
            title.setText(t.getCategory());
            amount.setText(TransactionStore.formatCurrency(t.getAmount()));
            amount.setTextColor(ContextCompat.getColor(this, "expense".equals(t.getType()) ? R.color.accent_red : R.color.primary_blue));
            topTransactionsContainer.addView(view);
            count++;
        }
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
            if (spent <= 0) continue;

            View itemView = inflater.inflate(R.layout.item_budget_category, budgetCategoryContainer, false);
            TextView nameView = itemView.findViewById(R.id.tvCategoryName);
            TextView amountView = itemView.findViewById(R.id.tvCategoryAmount);
            TextView percentView = itemView.findViewById(R.id.tvCategoryPercent);
            TextView spentView = itemView.findViewById(R.id.tvCategorySpent);
            ProgressBar progressBar = itemView.findViewById(R.id.progressCategory);

            nameView.setText(entry.getKey());
            amountView.setText(TransactionStore.formatCurrency(spent));
            int percent = (int) ((spent * 100f) / expenseTotal);
            percentView.setText(getString(R.string.percent_format, percent));
            spentView.setText(getString(R.string.statistics_spent_prefix, TransactionStore.formatCurrency(spent)));
            progressBar.setProgress(percent);

            budgetCategoryContainer.addView(itemView);
        }
    }

    private String extractMonthKey(String date) {
        if (date == null || !date.contains("-")) return "";
        String[] parts = date.split("T")[0].split("-");
        if (parts.length >= 2) {
            return parts[0] + "-" + parts[1];
        }
        return "";
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
