package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {

    private static final BudgetCategory[] DEFAULT_CATEGORIES = new BudgetCategory[]{
            new BudgetCategory("🍜 Ăn uống", 500_000),
            new BudgetCategory("🎮 Giải trí", 300_000),
            new BudgetCategory("🚗 Giao thông vận tải", 140_000),
            new BudgetCategory("🖼️ Sở thích", 30_000)
    };

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
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
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
                String categoryKey = TransactionStore.normalizeCategory(transaction.getCategory());
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
        Map<String, Long> categoryTotals = new HashMap<>();
        for (BudgetCategory category : DEFAULT_CATEGORIES) {
            String key = category.getKey();
            categoryTotals.put(key, TransactionStore.getCategoryTotal(this, key));
        }
        long remaining = incomeTotal - expenseTotal;
        applyBudgetSummary(incomeTotal, expenseTotal, categoryTotals, remaining);
    }

    private void applyBudgetSummary(long incomeTotal, long expenseTotal, Map<String, Long> categoryTotals, long remaining) {
        tvBudgetExpenseTab.setText("Chi " + TransactionStore.formatCurrency(expenseTotal));
        tvBudgetRemaining.setText(TransactionStore.formatCurrency(remaining));
        tvBudgetTotal.setText(TransactionStore.formatCurrency(incomeTotal));
        tvBudgetSpent.setText(TransactionStore.formatCurrency(expenseTotal));
        tvBudgetRemainingValue.setText(TransactionStore.formatCurrency(Math.max(remaining, 0)));

        int percent = incomeTotal > 0 ? Math.min(100, Math.round((expenseTotal * 100f) / incomeTotal)) : 0;
        tvBudgetPercent.setText(percent + "%");
        progressBudgetTotal.setProgress(percent);

        renderCategories(incomeTotal, categoryTotals);
    }

    private void renderCategories(long incomeTotal, Map<String, Long> categoryTotals) {
        budgetCategoryContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        long defaultSum = 0L;
        for (BudgetCategory category : DEFAULT_CATEGORIES) {
            defaultSum += category.defaultBudget;
        }

        for (BudgetCategory category : DEFAULT_CATEGORIES) {
            View itemView = inflater.inflate(R.layout.item_budget_category, budgetCategoryContainer, false);
            TextView nameView = itemView.findViewById(R.id.tvCategoryName);
            TextView budgetView = itemView.findViewById(R.id.tvCategoryBudget);
            TextView percentView = itemView.findViewById(R.id.tvCategoryPercent);
            TextView spentView = itemView.findViewById(R.id.tvCategorySpent);
            TextView remainingView = itemView.findViewById(R.id.tvCategoryRemaining);
            ProgressBar progressBar = itemView.findViewById(R.id.progressCategory);

            long budgetAmount = incomeTotal > 0
                    ? Math.round((category.defaultBudget / (float) defaultSum) * incomeTotal)
                    : category.defaultBudget;

            long spent = categoryTotals.getOrDefault(category.getKey(), 0L);
            long remaining = Math.max(0, budgetAmount - spent);
            int percent = budgetAmount > 0 ? Math.min(100, Math.round((spent * 100f) / budgetAmount)) : 0;

            nameView.setText(category.label);
            budgetView.setText(TransactionStore.formatCurrency(budgetAmount));
            percentView.setText(percent + "%");
            spentView.setText(TransactionStore.formatCurrency(spent));
            remainingView.setText(TransactionStore.formatCurrency(remaining));
            progressBar.setProgress(percent);

            budgetCategoryContainer.addView(itemView);
        }
    }

    private static class BudgetCategory {
        private final String label;
        private final long defaultBudget;

        BudgetCategory(String label, long defaultBudget) {
            this.label = label;
            this.defaultBudget = defaultBudget;
        }

        String getKey() {
            return TransactionStore.normalizeCategory(label);
        }
    }
}
