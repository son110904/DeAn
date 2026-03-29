package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int PREVIEW_LIMIT = 4;

    TextView tvIncome, tvExpense, tvBalance;
    LinearLayout transactionListContainer;
    ImageButton btnMainQr;
    private final List<TransactionResponse> allTransactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        btnMainQr = findViewById(R.id.btnMainQr);
        transactionListContainer = findViewById(R.id.transactionListContainer);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.menu_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) return true;
            if (id == R.id.menu_budget) {
                startActivity(new Intent(this, BudgetActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == R.id.menu_add) {
                startActivity(new Intent(this, AddTransactionActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == R.id.menu_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        btnMainQr.setOnClickListener(v -> {
            startActivity(new Intent(this, ScannerActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
    }

    private void updateDashboard() {
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        apiService.getTransactions().enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allTransactions.clear();
                    allTransactions.addAll(response.body());
                    updateDashboardFromTransactions(allTransactions);
                }
            }
            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {}
        });
    }

    private void updateDashboardFromTransactions(List<TransactionResponse> transactions) {
        long income = 0L;
        long expense = 0L;
        for (TransactionResponse t : transactions) {
            if ("income".equalsIgnoreCase(t.getType())) income += t.getAmount();
            else expense += t.getAmount();
        }
        long balance = income - expense;
        tvIncome.setText("+" + TransactionStore.formatCurrency(income / 1000) + "k");
        tvExpense.setText("-" + TransactionStore.formatCurrency(expense / 1000) + "k");
        tvBalance.setText(TransactionStore.formatCurrency(balance));
        renderTransactions();
    }

    private void renderTransactions() {
        transactionListContainer.removeAllViews();
        int count = 0;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (TransactionResponse transaction : allTransactions) {
            if (count >= PREVIEW_LIMIT) break;
            View itemView = inflater.inflate(R.layout.item_transaction, transactionListContainer, false);
            TextView titleView = itemView.findViewById(R.id.tvTransactionTitle);
            TextView metaView = itemView.findViewById(R.id.tvTransactionMeta);
            TextView amountView = itemView.findViewById(R.id.tvTransactionAmount);
            
            boolean isIncome = "income".equalsIgnoreCase(transaction.getType());
            titleView.setText(transaction.getCategory());
            metaView.setText(transaction.getDate());
            amountView.setText((isIncome ? "+" : "-") + TransactionStore.formatCurrency(transaction.getAmount()));
            amountView.setTextColor(ContextCompat.getColor(this, isIncome ? R.color.accent_green : R.color.accent_red));
            
            transactionListContainer.addView(itemView);
            count++;
        }
    }
}
