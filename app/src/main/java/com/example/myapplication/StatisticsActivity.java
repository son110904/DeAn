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

        TransactionStore.ensureDemoData(this);

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
        updateBudget();
    }

    private void updateBudget() {
        long incomeTotal = TransactionStore.getIncomeTotal(this);
        long expenseTotal = TransactionStore.getExpenseTotal(this);
        long remaining = incomeTotal - expenseTotal;

        tvBudgetExpenseTab.setText("Chi " + TransactionStore.formatCurrency(expenseTotal));
        tvBudgetRemaining.setText(TransactionStore.formatCurrency(remaining));
        tvBudgetTotal.setText(TransactionStore.formatCurrency(incomeTotal));
        tvBudgetSpent.setText(TransactionStore.formatCurrency(expenseTotal));
        tvBudgetRemainingValue.setText(TransactionStore.formatCurrency(Math.max(remaining, 0)));

        int percent = incomeTotal > 0 ? Math.min(100, Math.round((expenseTotal * 100f) / incomeTotal)) : 0;
        tvBudgetPercent.setText(percent + "%");
        progressBudgetTotal.setProgress(percent);

        renderCategories(incomeTotal);
    }

    private void renderCategories(long incomeTotal) {
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

            long spent = TransactionStore.getCategoryTotal(this, category.getKey());
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
