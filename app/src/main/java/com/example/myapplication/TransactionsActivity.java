package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsActivity extends AppCompatActivity {

    private TransactionAdapter adapter;
    private TextView emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        emptyState = findViewById(R.id.tvEmptyState);
        RecyclerView recyclerView = findViewById(R.id.recyclerTransactions);
        adapter = new TransactionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button btnAddTransaction = findViewById(R.id.btnAddTransaction);
        btnAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class))
        );

        adapter.setOnTransactionClickListener(transaction -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("transactionId", transaction.getId());
            intent.putExtra("amount", (long) transaction.getAmount());
            intent.putExtra("category", transaction.getCategory());
            intent.putExtra("type", transaction.getType());
            intent.putExtra("note", transaction.getNote());
            intent.putExtra("date", transaction.getDate());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void loadTransactions() {
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        apiService.getTransactions().enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TransactionResponse> transactions = response.body();
                    adapter.submitList(transactions);
                    emptyState.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    if (response.code() == 401) {
                        AuthStore.clear(TransactionsActivity.this);
                        startActivity(new Intent(TransactionsActivity.this, LoginActivity.class));
                        finish();
                        return;
                    }
                    Toast.makeText(TransactionsActivity.this,
                            getString(R.string.toast_transactions_failed),
                            Toast.LENGTH_SHORT).show();
                    adapter.submitList(null);
                    emptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                Toast.makeText(TransactionsActivity.this,
                        getString(R.string.toast_connection_error, t.getMessage()),
                        Toast.LENGTH_SHORT).show();
                adapter.submitList(null);
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
