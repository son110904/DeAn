package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    BottomNavigationView bottomNav;

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
        bottomNav = findViewById(R.id.bottomNav);

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
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        apiService.getTransactions().enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TransactionStore.syncFromTransactions(StatisticsActivity.this, response.body());
                    updateBudget(response.body());
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

    private void updateBudget(List<TransactionResponse> transactions) {
        long incomeTotal = 0L;
        long expenseTotal = 0L;
        Map<String, Long> categoryTotals = new HashMap<>();
        for (TransactionResponse transaction : transactions) {
            if ("income".equalsIgnoreCase(transaction.getType())) {
                incomeTotal += transaction.getAmount();
            } else {
                expenseTotal += transaction.getAmount();
                String categoryKey = TransactionStore.normalizeCategory(this, transaction.getCategory());
                long current = categoryTotals.getOrDefault(categoryKey, 0L);
                categoryTotals.put(categoryKey, current + transaction.getAmount());
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
        tvBudgetExpenseTab.setText(getString(R.string.statistics_expense_tab_format,
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

    private void renderCategories(long expenseTotal, Map<String, Long> categoryTotals) {
        budgetCategoryContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        if (expenseTotal <= 0 || categoryTotals.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText(getString(R.string.statistics_empty_categories));
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
            amountView.setText(getString(R.string.statistics_category_share_label));
            percentView.setText(getString(R.string.percent_format, percent));
            spentView.setText(getString(R.string.statistics_spent_format, TransactionStore.formatCurrency(spent)));
            progressBar.setProgress(percent);

            budgetCategoryContainer.addView(itemView);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
